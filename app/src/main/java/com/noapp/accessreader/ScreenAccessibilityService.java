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

    private static final long COMPARISON_WINDOW_MS = 5 * 60 * 1000L;
    private static final String PREFS = "no_accessibility";
    private static final String UBER_PREFIX = "compare_uber_";
    private static final String NINETY_NINE_PREFIX = "compare_99_";

    private long lastRead = 0L;
    private WindowManager windowManager;

    private LinearLayout overlayContainer;
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

        if (offer == null) {
            saveDebugCapture(currentPackage, content);
            return;
        }

        saveOffer(currentPackage, content, offer);
        saveComparisonCandidate(offer);

        ComparisonPair pair = loadFreshComparisonPair();
        if (pair != null) {
            RideComparisonEngine.ComparisonResult comparison =
                    RideComparisonEngine.compare(pair.uber, pair.ninetyNine);

            if (comparison != null) {
                String comparisonKey = createComparisonKey(pair.uber, pair.ninetyNine);
                saveComparison(comparison);

                if (comparisonKey.equals(dismissedOfferKey)) return;

                activeOfferKey = comparisonKey;
                showComparisonOverlay(pair.uber, pair.ninetyNine, comparison);
                return;
            }
        }

        String offerKey = createOfferKey(currentPackage, offer);
        if (offerKey.equals(dismissedOfferKey)) return;

        activeOfferKey = offerKey;
        showSingleOfferOverlay(offer);
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

    private String createComparisonKey(
            RideOfferParser.RideOffer uber,
            RideOfferParser.RideOffer ninetyNine
    ) {
        return "COMPARE|"
                + createOfferKey("Uber", uber)
                + "|VS|"
                + createOfferKey("99", ninetyNine);
    }

    private void saveOffer(String pkg, String content, RideOfferParser.RideOffer offer) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
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

    private void saveComparisonCandidate(RideOfferParser.RideOffer offer) {
        String prefix;
        if ("Uber".equalsIgnoreCase(offer.platform)) {
            prefix = UBER_PREFIX;
        } else if ("99".equalsIgnoreCase(offer.platform)) {
            prefix = NINETY_NINE_PREFIX;
        } else {
            return;
        }

        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putLong(prefix + "time", System.currentTimeMillis())
                .putString(prefix + "platform", offer.platform)
                .putString(prefix + "category", offer.category)
                .putFloat(prefix + "price", (float) offer.price)
                .putFloat(prefix + "pickup_km", (float) offer.pickupKm)
                .putFloat(prefix + "trip_km", (float) offer.tripKm)
                .putInt(prefix + "trip_minutes", offer.tripMinutes)
                .putFloat(prefix + "total_km", (float) offer.totalKm)
                .putFloat(prefix + "per_km", (float) offer.grossPerKm)
                .putFloat(prefix + "per_hour", (float) offer.grossPerHour)
                .putString(prefix + "rating", offer.rating)
                .apply();
    }

    private ComparisonPair loadFreshComparisonPair() {
        RideOfferParser.RideOffer uber = loadCandidate(UBER_PREFIX);
        RideOfferParser.RideOffer ninetyNine = loadCandidate(NINETY_NINE_PREFIX);
        if (uber == null || ninetyNine == null) return null;
        return new ComparisonPair(uber, ninetyNine);
    }

    private RideOfferParser.RideOffer loadCandidate(String prefix) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        long savedAt = prefs.getLong(prefix + "time", 0L);
        if (savedAt <= 0 || System.currentTimeMillis() - savedAt > COMPARISON_WINDOW_MS) {
            return null;
        }

        String platform = prefs.getString(prefix + "platform", "");
        String category = prefs.getString(prefix + "category", "Corrida");
        double price = prefs.getFloat(prefix + "price", 0f);
        double pickupKm = prefs.getFloat(prefix + "pickup_km", 0f);
        double tripKm = prefs.getFloat(prefix + "trip_km", 0f);
        int tripMinutes = prefs.getInt(prefix + "trip_minutes", 0);
        double totalKm = prefs.getFloat(prefix + "total_km", 0f);
        double perKm = prefs.getFloat(prefix + "per_km", 0f);
        double perHour = prefs.getFloat(prefix + "per_hour", 0f);
        String rating = prefs.getString(prefix + "rating", "");

        if (platform.isEmpty() || price <= 0 || tripKm <= 0 || tripMinutes <= 0) {
            return null;
        }

        return new RideOfferParser.RideOffer(
                platform,
                category,
                price,
                pickupKm,
                tripKm,
                tripMinutes,
                totalKm,
                perKm,
                perHour,
                rating
        );
    }

    private void saveComparison(RideComparisonEngine.ComparisonResult result) {
        String winner = result.technicalTie
                ? "EMPATE TÉCNICO"
                : result.winner.platform + " • " + result.winner.category;

        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean("has_comparison", true)
                .putLong("comparison_time", System.currentTimeMillis())
                .putString("comparison_winner", winner)
                .putString("comparison_reason", result.reason)
                .putFloat("comparison_advantage_pct", (float) result.scoreAdvantagePct)
                .apply();
    }

    private void saveDebugCapture(String pkg, String content) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString("debug_package", pkg)
                .putString("debug_content", content)
                .putLong("debug_time", System.currentTimeMillis())
                .apply();
    }

    private void showSingleOfferOverlay(RideOfferParser.RideOffer offer) {
        ensureOverlayContainer();
        overlayContainer.removeAllViews();

        addHeader(offer.platform + " • " + offer.category);
        addAccentLine();

        TextView eyebrow = text("DECISÃO ECONÔMICA", 10, getColor(R.color.no_cyan), Typeface.BOLD);
        eyebrow.setLetterSpacing(0.12f);
        overlayContainer.addView(eyebrow);

        TextView badge = opportunityBadge(offer.rating);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(-2, -2);
        badgeLp.setMargins(0, dp(8), 0, 0);
        overlayContainer.addView(badge, badgeLp);

        Locale ptBr = new Locale("pt", "BR");
        TextView price = text(String.format(ptBr, "R$ %.2f", offer.price),
                34, getColor(R.color.no_white), Typeface.BOLD);
        price.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        LinearLayout.LayoutParams priceLp = new LinearLayout.LayoutParams(-1, -2);
        priceLp.setMargins(0, dp(8), 0, 0);
        overlayContainer.addView(price, priceLp);

        overlayContainer.addView(text(String.format(ptBr,
                "%.1f km total  •  %d min de viagem",
                offer.totalKm,
                offer.tripMinutes),
                13, getColor(R.color.no_text_secondary), Typeface.NORMAL));

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams metricsLp = new LinearLayout.LayoutParams(-1, -2);
        metricsLp.setMargins(0, dp(12), 0, 0);
        overlayContainer.addView(metrics, metricsLp);

        LinearLayout kmBox = metricBox("RETORNO / KM",
                String.format(ptBr, "R$ %.2f/km", offer.grossPerKm));
        LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, -2, 1f);
        a.setMargins(0, 0, dp(5), 0);
        metrics.addView(kmBox, a);

        LinearLayout hourBox = metricBox("RETORNO / HORA",
                String.format(ptBr, "R$ %.2f/h", offer.grossPerHour));
        LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(0, -2, 1f);
        b.setMargins(dp(5), 0, 0, 0);
        metrics.addView(hourBox, b);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams detailsLp = new LinearLayout.LayoutParams(-1, -2);
        detailsLp.setMargins(0, dp(10), 0, 0);
        overlayContainer.addView(details, detailsLp);

        TextView pickup = text(String.format(ptBr, "Coleta  %.1f km", offer.pickupKm),
                12, getColor(R.color.no_text_secondary), Typeface.BOLD);
        pickup.setPadding(0, dp(5), 0, dp(5));
        details.addView(pickup, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView trip = text(String.format(ptBr, "Trajeto  %.1f km", offer.tripKm),
                12, getColor(R.color.no_text_secondary), Typeface.BOLD);
        trip.setGravity(Gravity.END);
        trip.setPadding(0, dp(5), 0, dp(5));
        details.addView(trip, new LinearLayout.LayoutParams(0, -2, 1f));

        addHint("Se outra plataforma aparecer nos próximos 5 min, o NÓ compara as duas automaticamente.");
        addOkButton("OK  •  FECHAR ANÁLISE");
        attachOverlayIfNeeded();
    }

    private void showComparisonOverlay(
            RideOfferParser.RideOffer uber,
            RideOfferParser.RideOffer ninetyNine,
            RideComparisonEngine.ComparisonResult comparison
    ) {
        ensureOverlayContainer();
        overlayContainer.removeAllViews();

        addHeader("COMPARADOR");
        addAccentLine();

        TextView eyebrow = text("DUAS OFERTAS • UMA DECISÃO", 10,
                getColor(R.color.no_cyan), Typeface.BOLD);
        eyebrow.setLetterSpacing(0.10f);
        overlayContainer.addView(eyebrow);

        String winnerTitle = comparison.technicalTie
                ? "EMPATE TÉCNICO"
                : comparison.winner.category.toUpperCase(Locale.ROOT) + " COMPENSA MAIS";

        TextView winner = text(winnerTitle, 24, getColor(R.color.no_white), Typeface.BOLD);
        winner.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        LinearLayout.LayoutParams winnerLp = new LinearLayout.LayoutParams(-1, -2);
        winnerLp.setMargins(0, dp(8), 0, dp(2));
        overlayContainer.addView(winner, winnerLp);

        TextView reason = text(comparison.reason, 12,
                getColor(R.color.no_text_secondary), Typeface.NORMAL);
        reason.setLineSpacing(dp(2), 1f);
        overlayContainer.addView(reason);

        overlayContainer.addView(comparisonCard(
                uber,
                !comparison.technicalTie && comparison.winner == uber,
                "UBER"));

        overlayContainer.addView(comparisonCard(
                ninetyNine,
                !comparison.technicalTie && comparison.winner == ninetyNine,
                "99"));

        addComparisonInsight(uber, ninetyNine, comparison);
        addHint("Comparação válida entre as últimas ofertas capturadas nos últimos 5 minutos.");
        addOkButton("OK  •  FECHAR COMPARAÇÃO");
        attachOverlayIfNeeded();
    }

    private LinearLayout comparisonCard(
            RideOfferParser.RideOffer offer,
            boolean winner,
            String platformLabel
    ) {
        Locale ptBr = new Locale("pt", "BR");

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackground(roundRect(
                getColor(R.color.no_metric),
                dp(14),
                winner ? getColor(R.color.no_cyan) : getColor(R.color.no_border),
                winner ? dp(2) : dp(1)));

        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(-1, -2);
        boxLp.setMargins(0, dp(10), 0, 0);
        box.setLayoutParams(boxLp);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        box.addView(top);

        TextView title = text(platformLabel + " • " + offer.category,
                15, getColor(R.color.no_white), Typeface.BOLD);
        top.addView(title);
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));

        if (winner) {
            TextView best = text("MELHOR", 9, getColor(R.color.no_cyan_soft), Typeface.BOLD);
            best.setPadding(dp(8), dp(4), dp(8), dp(4));
            best.setBackground(roundRect(
                    getColor(R.color.no_high_bg),
                    dp(999),
                    getColor(R.color.no_cyan),
                    dp(1)));
            top.addView(best);
        }

        TextView price = text(String.format(ptBr, "R$ %.2f", offer.price),
                25, getColor(R.color.no_white), Typeface.BOLD);
        price.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        box.addView(price);

        String metrics = String.format(ptBr,
                "%.1f km • %d min   |   R$ %.2f/km   |   R$ %.2f/h",
                offer.totalKm,
                offer.tripMinutes,
                offer.grossPerKm,
                offer.grossPerHour);
        TextView metricText = text(metrics, 11,
                getColor(R.color.no_text_secondary), Typeface.BOLD);
        metricText.setLineSpacing(dp(1), 1f);
        box.addView(metricText);

        return box;
    }

    private void addComparisonInsight(
            RideOfferParser.RideOffer uber,
            RideOfferParser.RideOffer ninetyNine,
            RideComparisonEngine.ComparisonResult comparison
    ) {
        Locale ptBr = new Locale("pt", "BR");

        double ninetyNineKmVsUber = RideComparisonEngine.percentDifference(
                ninetyNine.grossPerKm, uber.grossPerKm);
        double ninetyNineHourVsUber = RideComparisonEngine.percentDifference(
                ninetyNine.grossPerHour, uber.grossPerHour);

        LinearLayout insight = new LinearLayout(this);
        insight.setOrientation(LinearLayout.VERTICAL);
        insight.setPadding(dp(12), dp(10), dp(12), dp(10));
        insight.setBackground(roundRect(
                getColor(R.color.no_card_soft),
                dp(14),
                getColor(R.color.no_border),
                dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(10), 0, 0);
        overlayContainer.addView(insight, lp);

        TextView title = text("LEITURA RÁPIDA", 9,
                getColor(R.color.no_cyan), Typeface.BOLD);
        title.setLetterSpacing(0.08f);
        insight.addView(title);

        String kmLine = ninetyNineKmVsUber >= 0
                ? String.format(ptBr, "99: +%.1f%% no retorno por km", ninetyNineKmVsUber)
                : String.format(ptBr, "Uber: +%.1f%% no retorno por km", -ninetyNineKmVsUber);

        String hourLine = ninetyNineHourVsUber >= 0
                ? String.format(ptBr, "99: +%.1f%% no retorno por hora", ninetyNineHourVsUber)
                : String.format(ptBr, "Uber: +%.1f%% no retorno por hora", -ninetyNineHourVsUber);

        insight.addView(text(kmLine, 12, getColor(R.color.no_white), Typeface.BOLD));
        insight.addView(text(hourLine, 12, getColor(R.color.no_white), Typeface.BOLD));

        if (!comparison.technicalTie) {
            TextView balance = text(String.format(ptBr,
                    "Vantagem no equilíbrio geral: %.1f%%",
                    comparison.scoreAdvantagePct),
                    11, getColor(R.color.no_text_secondary), Typeface.NORMAL);
            LinearLayout.LayoutParams balanceLp = new LinearLayout.LayoutParams(-1, -2);
            balanceLp.setMargins(0, dp(4), 0, 0);
            insight.addView(balance, balanceLp);
        }
    }

    private void ensureOverlayContainer() {
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
    }

    private void addHeader(String label) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(8));
        header.setContentDescription("Cabeçalho do NÓ. Arraste para mover o painel.");
        header.setOnTouchListener(this::handleOverlayDrag);

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.BOTTOM);

        brand.addView(text("NÓ", 26, getColor(R.color.no_white), Typeface.BOLD));
        brand.addView(text(".", 26, getColor(R.color.no_cyan), Typeface.BOLD));
        header.addView(brand);
        header.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));

        TextView chip = text(label.toUpperCase(Locale.ROOT), 10,
                getColor(R.color.no_cyan_soft), Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(9), dp(5), dp(9), dp(5));
        chip.setBackground(roundRect(
                getColor(R.color.no_card_soft),
                dp(999),
                getColor(R.color.no_border),
                dp(1)));
        header.addView(chip);

        overlayContainer.addView(header, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addAccentLine() {
        LinearLayout accent = new LinearLayout(this);
        accent.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(-1, dp(3));
        accentLp.setMargins(0, 0, 0, dp(12));
        overlayContainer.addView(accent, accentLp);

        View cyan = new View(this);
        cyan.setBackgroundColor(getColor(R.color.no_cyan));
        accent.addView(cyan, new LinearLayout.LayoutParams(0, dp(3), 3f));

        View orange = new View(this);
        orange.setBackgroundColor(getColor(R.color.no_orange));
        accent.addView(orange, new LinearLayout.LayoutParams(0, dp(3), 1f));
    }

    private TextView opportunityBadge(String rating) {
        String normalized = rating == null ? "" : rating.toUpperCase(Locale.ROOT);
        TextView badge;

        if (normalized.contains("BOA") || normalized.contains("EXCELENTE")) {
            badge = text("OPORTUNIDADE ALTA", 11,
                    getColor(R.color.no_cyan_soft), Typeface.BOLD);
            badge.setBackgroundResource(R.drawable.bg_no_badge_high);
        } else if (normalized.contains("MÉDIA") || normalized.contains("MEDIA")) {
            badge = text("OPORTUNIDADE MÉDIA", 11,
                    getColor(R.color.no_orange), Typeface.BOLD);
            badge.setBackgroundResource(R.drawable.bg_no_badge_medium);
        } else {
            badge = text("OPORTUNIDADE BAIXA", 11,
                    getColor(R.color.no_low), Typeface.BOLD);
            badge.setBackgroundResource(R.drawable.bg_no_badge_low);
        }

        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(5), dp(10), dp(5));
        return badge;
    }

    private LinearLayout metricBox(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(11), dp(10), dp(11), dp(10));
        box.setBackground(roundRect(
                getColor(R.color.no_metric),
                dp(13),
                Color.rgb(18, 62, 89),
                dp(1)));

        TextView labelView = text(label, 9,
                getColor(R.color.no_text_muted), Typeface.BOLD);
        labelView.setLetterSpacing(0.08f);
        box.addView(labelView);
        box.addView(text(value, 15, getColor(R.color.no_white), Typeface.BOLD));
        return box;
    }

    private void addHint(String value) {
        TextView hint = text(value, 10, getColor(R.color.no_text_muted), Typeface.NORMAL);
        hint.setLineSpacing(dp(1), 1f);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(-1, -2);
        hintLp.setMargins(0, dp(8), 0, 0);
        overlayContainer.addView(hint, hintLp);
    }

    private void addOkButton(String label) {
        TextView ok = text(label, 14, getColor(R.color.no_navy_deep), Typeface.BOLD);
        ok.setGravity(Gravity.CENTER);
        ok.setClickable(true);
        ok.setFocusable(true);
        ok.setContentDescription("OK. Fechar painel do NÓ");
        ok.setBackground(roundRect(
                getColor(R.color.no_cyan),
                dp(14),
                Color.TRANSPARENT,
                0));
        ok.setOnClickListener(v -> {
            dismissedOfferKey = activeOfferKey;
            animateDismissOverlay();
        });

        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(-1, dp(48));
        buttonLp.setMargins(0, dp(12), 0, 0);
        overlayContainer.addView(ok, buttonLp);
    }

    private void attachOverlayIfNeeded() {
        if (windowManager == null) return;
        if (overlayAttached) return;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int overlayWidth = Math.min(dp(334), screenWidth - dp(20));

        overlayParams = new WindowManager.LayoutParams(
                overlayWidth,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        overlayParams.gravity = Gravity.TOP | Gravity.START;
        overlayParams.x = Math.max(dp(10), screenWidth - overlayWidth - dp(10));
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

    private boolean handleOverlayDrag(View view, MotionEvent event) {
        if (overlayParams == null || windowManager == null || !overlayAttached) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragDownX = event.getRawX();
                dragDownY = event.getRawY();
                dragStartX = overlayParams.x;
                dragStartY = overlayParams.y;
                return true;

            case MotionEvent.ACTION_MOVE:
                int nextX = dragStartX + Math.round(event.getRawX() - dragDownX);
                int nextY = dragStartY + Math.round(event.getRawY() - dragDownY);

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int width = overlayContainer.getWidth() > 0
                        ? overlayContainer.getWidth()
                        : Math.min(dp(334), screenWidth - dp(20));

                overlayParams.x = Math.max(0, Math.min(nextX, screenWidth - width));
                overlayParams.y = Math.max(dp(24), Math.min(nextY, screenHeight - dp(80)));

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

    private void animateDismissOverlay() {
        if (!overlayAttached || overlayContainer == null) return;

        overlayContainer.animate()
                .alpha(0f)
                .translationY(-dp(8))
                .setDuration(140L)
                .withEndAction(this::hideOverlay)
                .start();
    }

    private void hideOverlay() {
        if (!overlayAttached || windowManager == null || overlayContainer == null) return;
        try {
            windowManager.removeView(overlayContainer);
        } catch (Exception ignored) {
        }
        overlayAttached = false;
        overlayContainer.setAlpha(1f);
        overlayContainer.setTranslationY(0f);
    }

    private TextView text(String value, int sizeSp, int color, int style) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(color);
        tv.setTextSize(sizeSp);
        tv.setTypeface(Typeface.create("sans-serif", style));
        tv.setIncludeFontPadding(false);
        return tv;
    }

    private GradientDrawable roundRect(int fill, int radiusPx, int strokeColor, int strokePx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radiusPx);
        if (strokePx > 0 && strokeColor != Color.TRANSPARENT) {
            drawable.setStroke(strokePx, strokeColor);
        }
        return drawable;
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

    private static final class ComparisonPair {
        final RideOfferParser.RideOffer uber;
        final RideOfferParser.RideOffer ninetyNine;

        ComparisonPair(
                RideOfferParser.RideOffer uber,
                RideOfferParser.RideOffer ninetyNine
        ) {
            this.uber = uber;
            this.ninetyNine = ninetyNine;
        }
    }
}
