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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class ScreenAccessibilityService extends AccessibilityService {

    private long lastRead = 0L;
    private WindowManager windowManager;

    private LinearLayout overlayContainer;
    private LinearLayout overlayHeader;
    private TextView overlayPlatformText;
    private TextView overlayBadgeText;
    private TextView overlayPriceText;
    private TextView overlayRouteText;
    private TextView overlayPerKmText;
    private TextView overlayPerHourText;
    private TextView overlayPickupText;
    private TextView overlayTripText;
    private TextView overlayOkButton;

    private WindowManager.LayoutParams overlayParams;
    private boolean overlayAttached;
    private String activeOfferKey = "";
    private String dismissedOfferKey = "";

    private float dragDownX;
    private float dragDownY;
    private int dragStartX;
    private int dragStartY;

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

        // O HUD é persistente: eventos do próprio NÓ ou do sistema não o derrubam.
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

        // Eventos parciais nunca fecham o HUD. São mantidos apenas para diagnóstico.
        if (offer == null) {
            saveDebugCapture(currentPackage, content);
            return;
        }

        String offerKey = createOfferKey(currentPackage, offer);
        saveOffer(currentPackage, content, offer);

        // Depois do OK, a mesma oferta não reaparece. Uma oferta diferente abre o HUD novamente.
        if (offerKey.equals(dismissedOfferKey)) return;

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

        ensureOverlayViews();
        updateOverlayContent(offer);

        if (overlayAttached) return;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int overlayWidth = Math.min(dp(320), screenWidth - dp(24));

        overlayParams = new WindowManager.LayoutParams(
                overlayWidth,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        overlayParams.gravity = Gravity.TOP | Gravity.START;
        overlayParams.x = Math.max(dp(12), screenWidth - overlayWidth - dp(12));
        overlayParams.y = dp(58);

        try {
            overlayContainer.setAlpha(0f);
            overlayContainer.setTranslationY(-dp(10));
            windowManager.addView(overlayContainer, overlayParams);
            overlayAttached = true;
            overlayContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(180L)
                    .start();
        } catch (Exception ignored) {
            overlayAttached = false;
        }
    }

    private void ensureOverlayViews() {
        if (overlayContainer != null) return;

        overlayContainer = new LinearLayout(this);
        overlayContainer.setOrientation(LinearLayout.VERTICAL);
        overlayContainer.setPadding(dp(16), dp(14), dp(16), dp(16));
        overlayContainer.setElevation(dp(12));
        overlayContainer.setBackground(roundRect(
                Color.argb(248, 7, 31, 52),
                dp(20),
                Color.argb(230, 22, 199, 232),
                dp(1)));

        overlayHeader = new LinearLayout(this);
        overlayHeader.setOrientation(LinearLayout.HORIZONTAL);
        overlayHeader.setGravity(Gravity.CENTER_VERTICAL);
        overlayHeader.setPadding(0, 0, 0, dp(8));
        overlayHeader.setContentDescription("Cabeçalho do NÓ. Arraste para mover o painel.");
        overlayHeader.setOnTouchListener(this::handleOverlayDrag);

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.BOTTOM);

        TextView logo = text("NÓ", 26, getColor(R.color.no_white), Typeface.BOLD);
        logo.setFontFeatureSettings("kern");
        brand.addView(logo);

        TextView dot = text(".", 26, getColor(R.color.no_cyan), Typeface.BOLD);
        brand.addView(dot);

        overlayHeader.addView(brand);
        overlayHeader.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));

        overlayPlatformText = text("APP • CORRIDA", 11, getColor(R.color.no_cyan_soft), Typeface.BOLD);
        overlayPlatformText.setGravity(Gravity.CENTER);
        overlayPlatformText.setPadding(dp(10), dp(6), dp(10), dp(6));
        overlayPlatformText.setBackground(roundRect(
                getColor(R.color.no_card_soft),
                dp(999),
                getColor(R.color.no_border),
                dp(1)));
        overlayHeader.addView(overlayPlatformText);

        overlayContainer.addView(overlayHeader,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout accent = new LinearLayout(this);
        accent.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(3));
        accentLp.setMargins(0, 0, 0, dp(12));
        overlayContainer.addView(accent, accentLp);

        View cyan = new View(this);
        cyan.setBackgroundColor(getColor(R.color.no_cyan));
        accent.addView(cyan, new LinearLayout.LayoutParams(0, dp(3), 3f));

        View orange = new View(this);
        orange.setBackgroundColor(getColor(R.color.no_orange));
        accent.addView(orange, new LinearLayout.LayoutParams(0, dp(3), 1f));

        TextView eyebrow = text("DECISÃO ECONÔMICA", 10, getColor(R.color.no_cyan), Typeface.BOLD);
        eyebrow.setLetterSpacing(0.12f);
        overlayContainer.addView(eyebrow);

        overlayBadgeText = text("OPORTUNIDADE ALTA", 11, getColor(R.color.no_cyan_soft), Typeface.BOLD);
        overlayBadgeText.setGravity(Gravity.CENTER);
        overlayBadgeText.setPadding(dp(10), dp(5), dp(10), dp(5));
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        badgeLp.setMargins(0, dp(8), 0, 0);
        overlayContainer.addView(overlayBadgeText, badgeLp);

        overlayPriceText = text("R$ 0,00", 34, getColor(R.color.no_white), Typeface.BOLD);
        overlayPriceText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        LinearLayout.LayoutParams priceLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        priceLp.setMargins(0, dp(8), 0, 0);
        overlayContainer.addView(overlayPriceText, priceLp);

        overlayRouteText = text("0,0 km total • 0 min", 13, getColor(R.color.no_text_secondary), Typeface.NORMAL);
        overlayContainer.addView(overlayRouteText);

        LinearLayout metricRow = new LinearLayout(this);
        metricRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams metricRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        metricRowLp.setMargins(0, dp(12), 0, 0);
        overlayContainer.addView(metricRow, metricRowLp);

        overlayPerKmText = text("R$ 0,00/km", 16, getColor(R.color.no_white), Typeface.BOLD);
        LinearLayout perKmBox = metricBox("RETORNO / KM", overlayPerKmText);
        LinearLayout.LayoutParams metricLpA = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        metricLpA.setMargins(0, 0, dp(5), 0);
        metricRow.addView(perKmBox, metricLpA);

        overlayPerHourText = text("R$ 0,00/h", 16, getColor(R.color.no_white), Typeface.BOLD);
        LinearLayout perHourBox = metricBox("RETORNO / HORA", overlayPerHourText);
        LinearLayout.LayoutParams metricLpB = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        metricLpB.setMargins(dp(5), 0, 0, 0);
        metricRow.addView(perHourBox, metricLpB);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams detailsLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        detailsLp.setMargins(0, dp(10), 0, 0);
        overlayContainer.addView(details, detailsLp);

        overlayPickupText = text("Coleta 0,0 km", 12, getColor(R.color.no_text_secondary), Typeface.BOLD);
        overlayPickupText.setPadding(0, dp(5), 0, dp(5));
        details.addView(overlayPickupText, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        overlayTripText = text("Trajeto 0,0 km", 12, getColor(R.color.no_text_secondary), Typeface.BOLD);
        overlayTripText.setGravity(Gravity.END);
        overlayTripText.setPadding(0, dp(5), 0, dp(5));
        details.addView(overlayTripText, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView hint = text("Arraste pelo topo • o painel só fecha quando você tocar em OK", 10,
                getColor(R.color.no_text_muted), Typeface.NORMAL);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hintLp.setMargins(0, dp(8), 0, 0);
        overlayContainer.addView(hint, hintLp);

        overlayOkButton = text("OK  •  FECHAR ANÁLISE", 14, getColor(R.color.no_navy_deep), Typeface.BOLD);
        overlayOkButton.setGravity(Gravity.CENTER);
        overlayOkButton.setClickable(true);
        overlayOkButton.setFocusable(true);
        overlayOkButton.setContentDescription("OK. Fechar análise da oportunidade");
        overlayOkButton.setBackground(roundRect(
                getColor(R.color.no_cyan),
                dp(14),
                Color.TRANSPARENT,
                0));
        overlayOkButton.setOnClickListener(v -> {
            dismissedOfferKey = activeOfferKey;
            animateDismissOverlay();
        });

        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48));
        buttonLp.setMargins(0, dp(12), 0, 0);
        overlayContainer.addView(overlayOkButton, buttonLp);
    }

    private void updateOverlayContent(RideOfferParser.RideOffer offer) {
        Locale ptBr = new Locale("pt", "BR");
        overlayPlatformText.setText((offer.platform + " • " + offer.category).toUpperCase(ptBr));
        overlayPriceText.setText(String.format(ptBr, "R$ %.2f", offer.price));
        overlayRouteText.setText(String.format(ptBr,
                "%.1f km total  •  %d min de viagem", offer.totalKm, offer.tripMinutes));
        overlayPerKmText.setText(String.format(ptBr, "R$ %.2f/km", offer.grossPerKm));
        overlayPerHourText.setText(String.format(ptBr, "R$ %.2f/h", offer.grossPerHour));
        overlayPickupText.setText(String.format(ptBr, "Coleta  %.1f km", offer.pickupKm));
        overlayTripText.setText(String.format(ptBr, "Trajeto  %.1f km", offer.tripKm));
        applyOverlayBadge(offer.rating);
    }

    private void applyOverlayBadge(String rating) {
        String normalized = rating == null ? "" : rating.toUpperCase(Locale.ROOT);

        if (normalized.contains("BOA") || normalized.contains("EXCELENTE")) {
            overlayBadgeText.setText("OPORTUNIDADE ALTA");
            overlayBadgeText.setTextColor(getColor(R.color.no_cyan_soft));
            overlayBadgeText.setBackgroundResource(R.drawable.bg_no_badge_high);
            return;
        }

        if (normalized.contains("MÉDIA") || normalized.contains("MEDIA")) {
            overlayBadgeText.setText("OPORTUNIDADE MÉDIA");
            overlayBadgeText.setTextColor(getColor(R.color.no_orange));
            overlayBadgeText.setBackgroundResource(R.drawable.bg_no_badge_medium);
            return;
        }

        overlayBadgeText.setText("OPORTUNIDADE BAIXA");
        overlayBadgeText.setTextColor(getColor(R.color.no_low));
        overlayBadgeText.setBackgroundResource(R.drawable.bg_no_badge_low);
    }

    private LinearLayout metricBox(String label, TextView value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(11), dp(10), dp(11), dp(10));
        box.setBackground(roundRect(
                getColor(R.color.no_metric),
                dp(13),
                Color.rgb(18, 62, 89),
                dp(1)));

        TextView labelView = text(label, 9, getColor(R.color.no_text_muted), Typeface.BOLD);
        labelView.setLetterSpacing(0.08f);
        box.addView(labelView);

        LinearLayout.LayoutParams valueLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        valueLp.setMargins(0, dp(4), 0, 0);
        box.addView(value, valueLp);
        return box;
    }

    private TextView text(String value, int sizeSp, int color, int style) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(color);
        tv.setTextSize(sizeSp);
        tv.setTypeface(Typeface.DEFAULT, style);
        tv.setIncludeFontPadding(false);
        return tv;
    }

    private GradientDrawable roundRect(int fillColor, int radiusPx, int strokeColor, int strokePx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radiusPx);
        if (strokePx > 0) drawable.setStroke(strokePx, strokeColor);
        return drawable;
    }

    private boolean handleOverlayDrag(View view, MotionEvent event) {
        if (!overlayAttached || overlayParams == null || windowManager == null) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragDownX = event.getRawX();
                dragDownY = event.getRawY();
                dragStartX = overlayParams.x;
                dragStartY = overlayParams.y;
                return true;

            case MotionEvent.ACTION_MOVE:
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int maxX = Math.max(0, screenWidth - overlayParams.width);
                int maxY = Math.max(dp(24), screenHeight - dp(140));

                int newX = dragStartX + Math.round(event.getRawX() - dragDownX);
                int newY = dragStartY + Math.round(event.getRawY() - dragDownY);

                overlayParams.x = clamp(newX, 0, maxX);
                overlayParams.y = clamp(newY, dp(24), maxY);

                try {
                    windowManager.updateViewLayout(overlayContainer, overlayParams);
                } catch (Exception ignored) {
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return true;

            default:
                return false;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void animateDismissOverlay() {
        if (!overlayAttached || overlayContainer == null) return;
        overlayContainer.animate()
                .alpha(0f)
                .translationY(-dp(8))
                .setDuration(150L)
                .withEndAction(this::removeOverlayNow)
                .start();
    }

    private void removeOverlayNow() {
        if (!overlayAttached || windowManager == null || overlayContainer == null) return;
        try {
            windowManager.removeView(overlayContainer);
        } catch (Exception ignored) {
        }
        overlayAttached = false;
        overlayContainer.setAlpha(1f);
        overlayContainer.setTranslationY(0f);
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
        removeOverlayNow();
    }

    @Override
    public void onDestroy() {
        removeOverlayNow();
        super.onDestroy();
    }
}
