package com.noapp.accessreader;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class ScreenAccessibilityService extends AccessibilityService {

    private long lastRead = 0L;
    private WindowManager windowManager;

    private LinearLayout overlayContainer;
    private TextView overlayView;
    private Button overlayOkButton;
    private boolean overlayAttached;

    private String overlayPackage = "";
    private String activeOfferKey = "";
    private String dismissedOfferKey = "";

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

        // O HUD só deve ser fechado pelo botão OK.
        // Eventos do próprio NO ou da barra do sistema não alteram o HUD atual.
        if (getPackageName().equals(currentPackage)
                || "com.android.systemui".equals(currentPackage)) {
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (now - lastRead < 250) return;
        lastRead = now;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

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

        // Eventos parciais ou telas sem corrida nunca derrubam o HUD.
        // Guardamos somente para diagnóstico.
        if (offer == null) {
            saveDebugCapture(currentPackage, content);
            return;
        }

        String offerKey = createOfferKey(currentPackage, offer);
        overlayPackage = currentPackage;
        saveOffer(currentPackage, content, offer);

        // Se o usuário já clicou OK nesta mesma oferta, não a mostra novamente.
        // Uma oferta diferente gera uma chave diferente e volta a abrir o HUD.
        if (offerKey.equals(dismissedOfferKey)) {
            return;
        }

        activeOfferKey = offerKey;
        showOverlay(offer);
    }

    private String createOfferKey(String pkg, RideOfferParser.RideOffer offer) {
        return String.format(Locale.ROOT,
                "%s|%s|%s|%.2f|%.2f|%.2f|%d",
                pkg,
                offer.platform,
                offer.category,
                offer.price,
                offer.pickupKm,
                offer.tripKm,
                offer.tripMinutes);
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

        ensureOverlayViews();
        overlayView.setText(hud);

        if (!overlayAttached) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.END;
            params.x = dp(12);
            params.y = dp(72);

            try {
                windowManager.addView(overlayContainer, params);
                overlayAttached = true;
            } catch (Exception ignored) {
                overlayAttached = false;
            }
        }
    }

    private void ensureOverlayViews() {
        if (overlayContainer != null) return;

        overlayContainer = new LinearLayout(this);
        overlayContainer.setOrientation(LinearLayout.VERTICAL);
        overlayContainer.setPadding(dp(14), dp(10), dp(14), dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(235, 18, 23, 29));
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1), Color.argb(210, 70, 180, 110));
        overlayContainer.setBackground(bg);

        overlayView = new TextView(this);
        overlayView.setTextColor(Color.WHITE);
        overlayView.setTextSize(15);
        overlayView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        overlayView.setPadding(0, 0, 0, dp(8));
        overlayContainer.addView(overlayView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        overlayOkButton = new Button(this);
        overlayOkButton.setText("OK");
        overlayOkButton.setAllCaps(false);
        overlayOkButton.setTextSize(15);
        overlayOkButton.setContentDescription("Fechar análise da corrida");
        overlayOkButton.setOnClickListener(v -> {
            dismissedOfferKey = activeOfferKey;
            hideOverlay();
        });

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48));
        overlayContainer.addView(overlayOkButton, buttonParams);
    }

    private void hideOverlay() {
        if (!overlayAttached || windowManager == null || overlayContainer == null) return;
        try {
            windowManager.removeView(overlayContainer);
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
        hideOverlay();
    }

    @Override
    public void onDestroy() {
        hideOverlay();
        super.onDestroy();
    }
}
