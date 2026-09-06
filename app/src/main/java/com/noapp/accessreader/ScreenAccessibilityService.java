package com.noapp.accessreader;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class ScreenAccessibilityService extends AccessibilityService {

    private static final long HIDE_DELAY_MS = 1600L;

    private long lastRead = 0L;
    private WindowManager windowManager;
    private TextView overlayView;
    private boolean overlayAttached;
    private String overlayPackage = "";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable delayedHide = () -> {
        hideOverlay();
        overlayPackage = "";
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        CharSequence packageName = event.getPackageName();
        String currentPackage = packageName == null ? "" : packageName.toString();

        // Nunca deixa a tela do próprio NO substituir a última corrida analisada.
        if (getPackageName().equals(currentPackage)) {
            cancelPendingHide();
            hideOverlay();
            overlayPackage = "";
            return;
        }

        // Eventos rápidos da barra de status não devem derrubar o HUD de uma corrida válida.
        if ("com.android.systemui".equals(currentPackage)) {
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (now - lastRead < 250) return;
        lastRead = now;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            if (overlayAttached && !currentPackage.equals(overlayPackage)) {
                scheduleHide();
            }
            return;
        }

        Set<String> lines = new LinkedHashSet<>();
        collectNodeData(root, lines);

        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            if (out.length() > 0) out.append("\n");
            out.append(line);
        }

        root.recycle();

        String content = out.toString();
        RideOfferParser.RideOffer offer = RideOfferParser.parse(content);

        if (offer == null) {
            saveDebugCapture(currentPackage, content);

            // Alguns eventos de acessibilidade trazem só parte da árvore da tela.
            // Se ainda houver sinais claros de que estamos na mesma oferta,
            // preserva o HUD em vez de fazê-lo piscar/desaparecer.
            if (overlayAttached
                    && currentPackage.equals(overlayPackage)
                    && looksLikeRideOffer(content)) {
                cancelPendingHide();
                return;
            }

            // Se realmente saiu da tela da oferta, remove com um pequeno debounce
            // para não reagir a eventos transitórios do Android.
            if (overlayAttached) {
                scheduleHide();
            }
            return;
        }

        cancelPendingHide();
        overlayPackage = currentPackage;
        saveOffer(currentPackage, content, offer);
        showOverlay(offer);
    }

    private boolean looksLikeRideOffer(String content) {
        if (content == null || content.isEmpty()) return false;

        String lower = content.toLowerCase(Locale.ROOT);
        boolean hasPrice = lower.contains("r$");
        boolean hasDistance = lower.contains(" km") || lower.contains("quilômetro") || lower.contains("quilometro");
        boolean hasRideMarker = lower.contains("uber")
                || lower.contains("99pop")
                || lower.contains("99 pop")
                || lower.contains("passageiro")
                || lower.contains("viagem")
                || lower.contains("aceitar corrida")
                || lower.contains("aceitar");

        return (hasPrice && hasRideMarker) || (hasPrice && hasDistance) || (hasDistance && hasRideMarker);
    }

    private void scheduleHide() {
        mainHandler.removeCallbacks(delayedHide);
        mainHandler.postDelayed(delayedHide, HIDE_DELAY_MS);
    }

    private void cancelPendingHide() {
        mainHandler.removeCallbacks(delayedHide);
    }

    private void saveOffer(String pkg, String content, RideOfferParser.RideOffer offer) {
        SharedPreferences prefs = getSharedPreferences("no_accessibility", MODE_PRIVATE);
        prefs.edit()
                .putString("package", pkg)
                .putString("content", content)
                .putLong("time", System.currentTimeMillis())
                .putBoolean("has_offer", true)
                .putString("platform", offer.platform)
                .putString("category", offer.category)
                .putFloat("price", (float) offer.price)
                .putFloat("pickup_km", (float) offer.pickupKm)
                .putFloat("trip_km", (float) offer.tripKm)
                .putInt("trip_minutes", offer.tripMinutes)
                .putFloat("total_km", (float) offer.totalKm)
                .putFloat("gross_per_km", (float) offer.grossPerKm)
                .putFloat("gross_per_hour", (float) offer.grossPerHour)
                .putString("rating", offer.rating)
                .apply();
    }

    private void saveDebugCapture(String pkg, String content) {
        getSharedPreferences("no_accessibility", MODE_PRIVATE)
                .edit()
                .putString("debug_package", pkg)
                .putString("debug_content", content)
                .putLong("debug_time", System.currentTimeMillis())
                .apply();
    }

    private void showOverlay(RideOfferParser.RideOffer offer) {
        if (windowManager == null) return;

        String hud = String.format(Locale.getDefault(),
                "NO • %s %s\nR$ %.2f\n%.1f km total • %d min\nR$ %.2f/km • R$ %.2f/h\n%s",
                offer.platform,
                offer.category,
                offer.price,
                offer.totalKm,
                offer.tripMinutes,
                offer.grossPerKm,
                offer.grossPerHour,
                offer.rating);

        if (overlayView == null) {
            overlayView = new TextView(this);
            overlayView.setTextColor(Color.WHITE);
            overlayView.setTextSize(15);
            overlayView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            overlayView.setPadding(dp(14), dp(10), dp(14), dp(10));

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.argb(225, 18, 23, 29));
            bg.setCornerRadius(dp(14));
            bg.setStroke(dp(1), Color.argb(180, 70, 180, 110));
            overlayView.setBackground(bg);
        }

        overlayView.setText(hud);

        if (!overlayAttached) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.END;
            params.x = dp(12);
            params.y = dp(72);

            try {
                windowManager.addView(overlayView, params);
                overlayAttached = true;
            } catch (Exception ignored) {
                overlayAttached = false;
            }
        }
    }

    private void hideOverlay() {
        if (!overlayAttached || windowManager == null || overlayView == null) return;
        try {
            windowManager.removeView(overlayView);
        } catch (Exception ignored) {
        }
        overlayAttached = false;
    }

    private void collectNodeData(AccessibilityNodeInfo node, Set<String> lines) {
        if (node == null) return;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();

        if (text != null && text.length() > 0) lines.add("TEXT: " + text);
        if (desc != null && desc.length() > 0) lines.add("DESC: " + desc);
        if (viewId != null && !viewId.isEmpty()) lines.add("VIEW_ID: " + viewId);

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectNodeData(child, lines);
                child.recycle();
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onInterrupt() {
        cancelPendingHide();
        hideOverlay();
    }

    @Override
    public void onDestroy() {
        cancelPendingHide();
        hideOverlay();
        super.onDestroy();
    }
}
