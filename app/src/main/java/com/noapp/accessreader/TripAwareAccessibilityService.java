package com.noapp.accessreader;

import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Mantém toda a leitura/overlay existente e acrescenta o ciclo de viagens:
 * oportunidade -> aceita -> concluída.
 *
 * Não registra a simples presença do botão "Aceitar". Só registra quando a
 * tela muda para um estado inequívoco posterior ao aceite.
 */
public class TripAwareAccessibilityService extends ScreenAccessibilityService {

    private static final long MATCH_WINDOW_MS = 15 * 60 * 1000L;
    private long lastTripReadElapsed = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        super.onAccessibilityEvent(event);
        if (event == null) return;

        String pkg = event.getPackageName() == null ? "" : event.getPackageName().toString();
        if (getPackageName().equals(pkg) || "com.android.systemui".equals(pkg)) return;

        long elapsed = SystemClock.elapsedRealtime();
        if (elapsed - lastTripReadElapsed < 300) return;
        lastTripReadElapsed = elapsed;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        Set<String> values = new LinkedHashSet<>();
        collect(root, values);
        root.recycle();

        StringBuilder text = new StringBuilder();
        for (String value : values) {
            if (text.length() > 0) text.append('\n');
            text.append(value);
        }

        String content = text.toString();
        String normalized = content.toLowerCase(Locale.ROOT);
        String platform = detectPlatform(pkg, normalized);
        long now = System.currentTimeMillis();

        if (isCompletedState(normalized)) {
            TripStore.completeLatest(this, platform, now);
            return;
        }

        if (!isAcceptedState(normalized)) return;

        Opportunity matching = findLatestRide(platform);
        if (matching != null) TripStore.accept(this, matching, now);
    }

    private Opportunity findLatestRide(String platform) {
        List<Opportunity> recent = OpportunityStore.listFresh(this, MATCH_WINDOW_MS);
        Opportunity fallback = null;
        for (Opportunity item : recent) {
            if (!Opportunity.TYPE_RIDE.equals(item.type)) continue;
            if (fallback == null) fallback = item;
            if (platform != null && !platform.isEmpty() && platform.equalsIgnoreCase(item.platform)) {
                return item;
            }
        }
        // Só usa fallback quando não foi possível identificar a plataforma na tela.
        return platform == null || platform.isEmpty() ? fallback : null;
    }

    private String detectPlatform(String pkg, String text) {
        if (pkg != null) {
            if (pkg.contains("ubercab")) return "Uber";
            if (pkg.contains("taxis99")) return "99";
        }
        if (text.contains("uberx") || text.contains("uber driver") || text.contains("uber •")) return "Uber";
        if (text.contains("99pop") || text.contains("99 motorista") || text.contains("99 •")) return "99";
        if (text.contains("indrive")) return "inDrive";
        return "";
    }

    private boolean isAcceptedState(String text) {
        return containsAny(text,
                "corrida aceita",
                "viagem aceita",
                "a caminho do passageiro",
                "a caminho do embarque",
                "dirija até o passageiro",
                "navegar até o passageiro",
                "chegar ao passageiro",
                "cheguei ao local de embarque",
                "embarque do passageiro");
    }

    private boolean isCompletedState(String text) {
        return containsAny(text,
                "viagem concluída",
                "viagem concluida",
                "corrida concluída",
                "corrida concluida",
                "viagem finalizada",
                "corrida finalizada",
                "viagem encerrada",
                "ganhos da viagem");
    }

    private boolean containsAny(String text, String... values) {
        if (text == null) return false;
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }

    private void collect(AccessibilityNodeInfo node, Set<String> values) {
        if (node == null) return;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (text != null && text.length() > 0) values.add(text.toString());
        if (desc != null && desc.length() > 0) values.add(desc.toString());

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collect(child, values);
                child.recycle();
            }
        }
    }
}
