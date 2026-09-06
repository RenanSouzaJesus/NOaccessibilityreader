package com.noapp.accessreader;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final long INBOX_MAX_AGE_MS = 15 * 60 * 1000L;
    private static final int REQUEST_NOTIFICATIONS = 501;

    private final Locale ptBr = new Locale("pt", "BR");

    private TextView accessibilityStatusText;
    private TextView notificationStatusText;
    private LinearLayout bestCard;
    private TextView bestPlatformText;
    private TextView bestPriceText;
    private TextView bestDistanceText;
    private TextView bestPerKmText;
    private TextView bestPerHourText;
    private TextView bestReasonText;
    private TextView emptyInboxText;
    private LinearLayout opportunitiesContainer;
    private TextView sourceNotificationText;
    private LinearLayout debugPanel;
    private TextView resultText;
    private Button tabAll;
    private Button tabRides;
    private Button tabDeliveries;
    private Button tabRoutes;
    private Button debugToggleButton;

    private String activeFilter = "ALL";
    private List<Opportunity> currentItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NoNotificationHelper.ensureChannel(this);

        accessibilityStatusText = findViewById(R.id.accessibilityStatusText);
        notificationStatusText = findViewById(R.id.notificationStatusText);
        bestCard = findViewById(R.id.bestCard);
        bestPlatformText = findViewById(R.id.bestPlatformText);
        bestPriceText = findViewById(R.id.bestPriceText);
        bestDistanceText = findViewById(R.id.bestDistanceText);
        bestPerKmText = findViewById(R.id.bestPerKmText);
        bestPerHourText = findViewById(R.id.bestPerHourText);
        bestReasonText = findViewById(R.id.bestReasonText);
        emptyInboxText = findViewById(R.id.emptyInboxText);
        opportunitiesContainer = findViewById(R.id.opportunitiesContainer);
        sourceNotificationText = findViewById(R.id.sourceNotificationText);
        debugPanel = findViewById(R.id.debugPanel);
        resultText = findViewById(R.id.resultText);
        tabAll = findViewById(R.id.tabAll);
        tabRides = findViewById(R.id.tabRides);
        tabDeliveries = findViewById(R.id.tabDeliveries);
        tabRoutes = findViewById(R.id.tabRoutes);
        debugToggleButton = findViewById(R.id.debugToggleButton);

        Button accessibilityButton = findViewById(R.id.accessibilityButton);
        Button notificationButton = findViewById(R.id.notificationButton);
        Button refreshButton = findViewById(R.id.refreshButton);

        accessibilityButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );

        notificationButton.setOnClickListener(v -> configureNotifications());
        refreshButton.setOnClickListener(v -> refreshData());

        tabAll.setOnClickListener(v -> setFilter("ALL"));
        tabRides.setOnClickListener(v -> setFilter(Opportunity.TYPE_RIDE));
        tabDeliveries.setOnClickListener(v -> setFilter(Opportunity.TYPE_DELIVERY));
        tabRoutes.setOnClickListener(v -> setFilter(Opportunity.TYPE_ROUTE));

        debugToggleButton.setOnClickListener(v -> {
            boolean opening = debugPanel.getVisibility() != View.VISIBLE;
            debugPanel.setVisibility(opening ? View.VISIBLE : View.GONE);
            debugToggleButton.setText(opening
                    ? "Ocultar diagnóstico técnico"
                    : "Mostrar diagnóstico técnico");
        });

        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATIONS) {
            openNotificationListenerSettings();
        }
    }

    private void configureNotifications() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS
            );
            return;
        }
        openNotificationListenerSettings();
    }

    private void openNotificationListenerSettings() {
        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    private void setFilter(String filter) {
        activeFilter = filter;
        updateTabAppearance();
        renderInbox();
    }

    private void refreshData() {
        updateCaptureStatus();
        updateSourceNotification();
        currentItems = OpportunityStore.listFresh(this, INBOX_MAX_AGE_MS);
        updateTabLabels();
        updateTabAppearance();
        renderBestOpportunity();
        renderInbox();
        updateDebugPanel();
    }

    private void updateCaptureStatus() {
        boolean accessibilityEnabled = isAccessibilityEnabled();
        boolean notificationEnabled = isNotificationListenerEnabled();

        applyStatus(accessibilityStatusText, accessibilityEnabled);
        applyStatus(notificationStatusText, notificationEnabled);
    }

    private void applyStatus(TextView target, boolean enabled) {
        if (enabled) {
            target.setText("ATIVO");
            target.setTextColor(getColor(R.color.no_cyan_soft));
            target.setBackgroundResource(R.drawable.bg_no_status_active);
        } else {
            target.setText("DESATIVADO");
            target.setTextColor(getColor(R.color.no_orange));
            target.setBackgroundResource(R.drawable.bg_no_status_inactive);
        }
    }

    private void renderBestOpportunity() {
        OpportunityRanker.RankingResult ranking = OpportunityRanker.rank(currentItems);
        if (ranking == null || ranking.best == null) {
            bestCard.setVisibility(View.GONE);
            return;
        }

        Opportunity best = ranking.best;
        bestCard.setVisibility(View.VISIBLE);
        bestPlatformText.setText(best.platform + " • " + best.category);
        bestPriceText.setText(money(best.price));
        bestDistanceText.setText(String.format(ptBr, "%.1f km", best.totalKm));
        bestPerKmText.setText(String.format(ptBr, "R$ %.2f", best.grossPerKm));
        bestPerHourText.setText(best.grossPerHour > 0
                ? String.format(ptBr, "R$ %.0f", best.grossPerHour)
                : "—");

        String reason = ranking.reason;
        if (ranking.second != null && ranking.advantagePct > 0.1) {
            reason += String.format(ptBr, " • %.1f%% de vantagem na comparação atual.",
                    ranking.advantagePct);
        }
        bestReasonText.setText(reason);
    }

    private void renderInbox() {
        opportunitiesContainer.removeAllViews();

        List<Opportunity> filtered = new ArrayList<>();
        for (Opportunity item : currentItems) {
            if ("ALL".equals(activeFilter) || activeFilter.equals(item.type)) {
                filtered.add(item);
            }
        }

        emptyInboxText.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        if (filtered.isEmpty()) {
            emptyInboxText.setText(emptyMessageForFilter());
            return;
        }

        OpportunityRanker.RankingResult ranking = OpportunityRanker.rank(currentItems);
        Opportunity best = ranking == null ? null : ranking.best;

        for (Opportunity item : filtered) {
            opportunitiesContainer.addView(buildOpportunityCard(item, item == best));
        }
    }

    private View buildOpportunityCard(Opportunity item, boolean isBest) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(13), dp(14), dp(13));
        card.setBackground(roundRect(
                getColor(R.color.no_card),
                dp(18),
                isBest ? getColor(R.color.no_cyan) : getColor(R.color.no_border),
                isBest ? dp(2) : dp(1)
        ));

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.setMargins(0, 0, 0, dp(9));
        card.setLayoutParams(cardLp);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top);

        TextView brand = text(item.platform, 11, brandTextColor(item.platform), true);
        brand.setPadding(dp(9), dp(5), dp(9), dp(5));
        brand.setBackground(roundRect(
                brandColor(item.platform),
                dp(999),
                Color.TRANSPARENT,
                0
        ));
        top.addView(brand);

        TextView category = text("  " + item.category, 14, getColor(R.color.no_white), true);
        top.addView(category);
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));

        TextView typeBadge = text(typeLabel(item.type), 9,
                getColor(R.color.no_text_secondary), true);
        typeBadge.setLetterSpacing(0.06f);
        typeBadge.setPadding(dp(8), dp(4), dp(8), dp(4));
        typeBadge.setBackground(roundRect(
                getColor(R.color.no_metric),
                dp(999),
                getColor(R.color.no_border),
                dp(1)
        ));
        top.addView(typeBadge);

        LinearLayout valueRow = new LinearLayout(this);
        valueRow.setOrientation(LinearLayout.HORIZONTAL);
        valueRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams valueRowLp = new LinearLayout.LayoutParams(-1, -2);
        valueRowLp.setMargins(0, dp(10), 0, 0);
        card.addView(valueRow, valueRowLp);

        TextView price = text(money(item.price), 28, getColor(R.color.no_white), true);
        price.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        valueRow.addView(price);
        valueRow.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));

        if (isBest) {
            TextView best = text("MELHOR", 9, getColor(R.color.no_cyan_soft), true);
            best.setPadding(dp(8), dp(5), dp(8), dp(5));
            best.setBackgroundResource(R.drawable.bg_no_badge_high);
            valueRow.addView(best);
        }

        LinearLayout metricsRow = new LinearLayout(this);
        metricsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams metricsLp = new LinearLayout.LayoutParams(-1, -2);
        metricsLp.setMargins(0, dp(10), 0, 0);
        card.addView(metricsRow, metricsLp);

        LinearLayout distanceBox = metricBox(
                "DISTÂNCIA",
                String.format(ptBr, "%.1f km", item.totalKm)
        );
        metricsRow.addView(distanceBox, weightedBoxParams(0, 4));

        LinearLayout kmBox = metricBox(
                "R$/KM",
                String.format(ptBr, "R$ %.2f", item.grossPerKm)
        );
        LinearLayout.LayoutParams kmLp = weightedBoxParams(4, 4);
        metricsRow.addView(kmBox, kmLp);

        String perHour = item.grossPerHour > 0
                ? String.format(ptBr, "R$ %.0f", item.grossPerHour)
                : "—";
        LinearLayout hourBox = metricBox("R$/H", perHour);
        metricsRow.addView(hourBox, weightedBoxParams(4, 0));

        String details;
        if (item.minutes > 0) {
            details = item.minutes + " min";
        } else {
            details = "tempo não informado";
        }

        if (item.pickupKm > 0) {
            details += String.format(ptBr,
                    "  •  coleta %.1f km  •  percurso %.1f km",
                    item.pickupKm,
                    item.routeKm);
        }

        TextView footer = text(details, 11, getColor(R.color.no_text_muted), false);
        footer.setPadding(0, dp(8), 0, 0);
        card.addView(footer);

        return card;
    }

    private LinearLayout metricBox(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(9), dp(8), dp(9), dp(8));
        box.setBackground(roundRect(
                getColor(R.color.no_metric),
                dp(12),
                Color.TRANSPARENT,
                0
        ));

        TextView labelView = text(label, 9, getColor(R.color.no_text_muted), true);
        labelView.setLetterSpacing(0.05f);
        box.addView(labelView);

        TextView valueView = text(value, 13, getColor(R.color.no_white), true);
        valueView.setPadding(0, dp(3), 0, 0);
        box.addView(valueView);
        return box;
    }

    private LinearLayout.LayoutParams weightedBoxParams(int left, int right) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(dp(left), 0, dp(right), 0);
        return lp;
    }

    private String typeLabel(String type) {
        if (Opportunity.TYPE_RIDE.equals(type)) return "CORRIDA";
        if (Opportunity.TYPE_DELIVERY.equals(type)) return "ENTREGA";
        if (Opportunity.TYPE_ROUTE.equals(type)) return "ROTA";
        return "OFERTA";
    }

    private void updateSourceNotification() {
        SharedPreferences prefs = getSharedPreferences("no_accessibility", MODE_PRIVATE);
        long time = prefs.getLong("source_notification_time", 0L);
        String platform = prefs.getString("source_notification_platform", "");
        String title = prefs.getString("source_notification_title", "");
        String text = prefs.getString("source_notification_text", "");

        if (time <= 0 || TextUtils.isEmpty(platform)) {
            sourceNotificationText.setText(
                    "Nenhum alerta compatível detectado. Ative Notificações para identificar novas oportunidades."
            );
            return;
        }

        String when = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(time));
        String body = platform + " • " + when;
        if (!TextUtils.isEmpty(title)) body += "\n" + title;
        if (!TextUtils.isEmpty(text) && !text.equals(title)) body += "\n" + text;
        sourceNotificationText.setText(body);
    }

    private void updateDebugPanel() {
        SharedPreferences prefs = getSharedPreferences("no_accessibility", MODE_PRIVATE);
        String debugPkg = prefs.getString("debug_package", "");
        String debugContent = prefs.getString("debug_content", "");
        long debugTime = prefs.getLong("debug_time", 0L);
        String pkg = prefs.getString("package", "");
        String content = prefs.getString("content", "");
        long time = prefs.getLong("time", 0L);

        if (!TextUtils.isEmpty(pkg) && time > 0) {
            String when = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.MEDIUM
            ).format(new Date(time));
            resultText.setText("Última oportunidade completa: " + pkg
                    + "\nCapturada em: " + when
                    + "\n\n" + content);
            return;
        }

        if (!TextUtils.isEmpty(debugPkg)) {
            String when = debugTime > 0
                    ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(new Date(debugTime))
                    : "-";
            resultText.setText("Última tela observada: " + debugPkg
                    + "\nCapturada em: " + when
                    + "\n\n" + debugContent);
            return;
        }

        resultText.setText("Nenhuma leitura capturada ainda.");
    }

    private void updateTabLabels() {
        int rides = 0;
        int deliveries = 0;
        int routes = 0;

        for (Opportunity item : currentItems) {
            if (Opportunity.TYPE_RIDE.equals(item.type)) rides++;
            else if (Opportunity.TYPE_DELIVERY.equals(item.type)) deliveries++;
            else if (Opportunity.TYPE_ROUTE.equals(item.type)) routes++;
        }

        tabAll.setText("Todas  " + currentItems.size());
        tabRides.setText("Corridas  " + rides);
        tabDeliveries.setText("Entregas  " + deliveries);
        tabRoutes.setText("Rotas  " + routes);
    }

    private void updateTabAppearance() {
        styleTab(tabAll, "ALL".equals(activeFilter));
        styleTab(tabRides, Opportunity.TYPE_RIDE.equals(activeFilter));
        styleTab(tabDeliveries, Opportunity.TYPE_DELIVERY.equals(activeFilter));
        styleTab(tabRoutes, Opportunity.TYPE_ROUTE.equals(activeFilter));
    }

    private void styleTab(Button button, boolean active) {
        button.setBackgroundResource(active
                ? R.drawable.bg_no_button_primary
                : R.drawable.bg_no_button_secondary);
        button.setTextColor(active
                ? getColor(R.color.no_navy_deep)
                : getColor(R.color.no_white));
    }

    private String emptyMessageForFilter() {
        if (Opportunity.TYPE_RIDE.equals(activeFilter)) {
            return "Nenhuma corrida recente.";
        }
        if (Opportunity.TYPE_DELIVERY.equals(activeFilter)) {
            return "Nenhuma entrega recente.";
        }
        if (Opportunity.TYPE_ROUTE.equals(activeFilter)) {
            return "Nenhuma rota recente.";
        }
        return "Nenhuma oportunidade recente. Abra uma simulação para começar.";
    }

    private int brandColor(String platform) {
        if ("Uber".equalsIgnoreCase(platform)) return Color.BLACK;
        if ("99".equalsIgnoreCase(platform)) return Color.rgb(255, 214, 0);
        if ("iFood".equalsIgnoreCase(platform)) return Color.rgb(234, 29, 44);
        if ("Mercado Livre".equalsIgnoreCase(platform)) return Color.rgb(255, 220, 0);
        if ("Shopee".equalsIgnoreCase(platform)) return Color.rgb(238, 77, 45);
        return getColor(R.color.no_card_soft);
    }

    private int brandTextColor(String platform) {
        if ("99".equalsIgnoreCase(platform) || "Mercado Livre".equalsIgnoreCase(platform)) {
            return Color.rgb(30, 30, 30);
        }
        return Color.WHITE;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(color);
        tv.setTextSize(size);
        tv.setIncludeFontPadding(false);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
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

    private String money(double value) {
        return String.format(ptBr, "R$ %.2f", value);
    }

    private boolean isAccessibilityEnabled() {
        String expected = new ComponentName(this, ScreenAccessibilityService.class).flattenToString();
        String enabled = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        return containsComponent(enabled, expected);
    }

    private boolean isNotificationListenerEnabled() {
        String expected = new ComponentName(this, NoNotificationListenerService.class).flattenToString();
        String enabled = Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners"
        );
        return containsComponent(enabled, expected);
    }

    private boolean containsComponent(String enabled, String expected) {
        if (enabled == null || expected == null) return false;

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);
        while (splitter.hasNext()) {
            if (expected.equalsIgnoreCase(splitter.next())) return true;
        }
        return false;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
