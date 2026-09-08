package com.noapp.accessreader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Central premium do NÓ: UX mais limpa, rápida e orientada à ação.
 * Inspiração de usabilidade em apps de mobilidade, sem copiar identidade visual externa.
 */
public class MainActivityPremium extends Activity {

    private static final long INBOX_MAX_AGE_MS = 15 * 60 * 1000L;
    private static final int REQUEST_NOTIFICATIONS = 701;
    private final Locale ptBr = new Locale("pt", "BR");

    private LinearLayout content;
    private String activeFilter = "ALL";
    private List<Opportunity> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumUi.lightSystemBars(this);
        NoNotificationHelper.ensureChannel(this);
        buildShell();
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATIONS) openNotificationListenerSettings();
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PremiumUi.BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(PremiumUi.dp(this, 16), PremiumUi.dp(this, 14), PremiumUi.dp(this, 16), PremiumUi.dp(this, 24));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        root.addView(buildBottomNav(), new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 68)));
        setContentView(root);
    }

    private void render() {
        if (content == null) return;
        content.removeAllViews();
        items = OpportunityStore.listFresh(this, INBOX_MAX_AGE_MS);

        addHeader();
        addMediaCard();
        addPermissionsCard();
        addBestCard();
        addRecentSection();
    }

    private void addHeader() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout logo = new LinearLayout(this);
        logo.setOrientation(LinearLayout.HORIZONTAL);
        logo.setGravity(Gravity.BOTTOM);
        TextView n = PremiumUi.text(this, "NÓ", 31, PremiumUi.NAVY, true);
        TextView dot = PremiumUi.text(this, ".", 31, PremiumUi.CYAN, true);
        logo.addView(n);
        logo.addView(dot);
        row.addView(logo);
        row.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        row.addView(PremiumUi.chip(this, "CENTRAL", PremiumUi.NAVY, PremiumUi.CYAN_SOFT));
        content.addView(row);

        TextView title = PremiumUi.text(this, "Tudo importante, sem complicação", 23, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        content.addView(title, titleLp);

        TextView sub = PremiumUi.text(this,
                "Compare oportunidades e acompanhe suas campanhas em um só lugar.",
                13, PremiumUi.MUTED, false);
        sub.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.setMargins(0, PremiumUi.dp(this, 5), 0, PremiumUi.dp(this, 18));
        content.addView(sub, subLp);
    }

    private void addMediaCard() {
        SharedPreferences prefs = getSharedPreferences("no_media", MODE_PRIVATE);
        String requestId = prefs.getString("last_request_id", "");
        String campaign = prefs.getString("last_request_campaign", "");
        String status = prefs.getString("last_request_status", "");
        boolean hasCampaign = !TextUtils.isEmpty(requestId);

        LinearLayout card = PremiumUi.card(this);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(PremiumUi.chip(this, "NÓ MÍDIA", PremiumUi.ORANGE, Color.rgb(255, 246, 232)));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        if (hasCampaign) {
            top.addView(PremiumUi.chip(this, "RECEBIDA", PremiumUi.GREEN, Color.rgb(232, 250, 242)));
        }
        card.addView(top);

        TextView title = PremiumUi.text(this,
                hasCampaign ? (TextUtils.isEmpty(campaign) ? "Sua campanha" : campaign) : "Quer anunciar nas ruas?",
                19, PremiumUi.TEXT, true);
        title.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        card.addView(title);

        TextView body = PremiumUi.text(this,
                hasCampaign
                        ? "A solicitação está salva. Veja o briefing, orçamento e próximos passos."
                        : "Monte uma campanha por região, tipo de veículo e formato de mídia.",
                12, PremiumUi.MUTED, false);
        body.setPadding(0, PremiumUi.dp(this, 5), 0, 0);
        card.addView(body);

        if (hasCampaign && !TextUtils.isEmpty(status)) {
            TextView id = PremiumUi.text(this, status + "  •  " + requestId, 10, PremiumUi.MUTED, true);
            id.setPadding(0, PremiumUi.dp(this, 8), 0, 0);
            card.addView(id);
        }

        Button main = PremiumUi.primaryButton(this, hasCampaign ? "Ver campanha" : "Criar campanha");
        main.setOnClickListener(v -> {
            if (hasCampaign) startActivity(new Intent(this, CampaignAnalysisPremiumActivity.class));
            else startActivity(new Intent(this, AdvertiserActivityPremium.class));
        });
        LinearLayout.LayoutParams mainLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 48));
        mainLp.setMargins(0, PremiumUi.dp(this, 14), 0, 0);
        card.addView(main, mainLp);

        if (hasCampaign) {
            TextView newCampaign = PremiumUi.text(this, "+ Criar nova campanha", 12, PremiumUi.NAVY, true);
            newCampaign.setGravity(Gravity.CENTER);
            newCampaign.setPadding(0, PremiumUi.dp(this, 13), 0, PremiumUi.dp(this, 2));
            newCampaign.setOnClickListener(v -> startActivity(new Intent(this, AdvertiserActivityPremium.class)));
            card.addView(newCampaign);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 12));
        content.addView(card, lp);
    }

    private void addPermissionsCard() {
        boolean access = isAccessibilityEnabled();
        boolean notifications = isNotificationListenerEnabled();

        LinearLayout card = PremiumUi.card(this);
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = PremiumUi.text(this, "Conexões do NÓ", 16, PremiumUi.TEXT, true);
        head.addView(title);
        head.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        TextView manage = PremiumUi.text(this, "Configurar", 12, PremiumUi.NAVY, true);
        manage.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 6), 0, PremiumUi.dp(this, 6));
        manage.setOnClickListener(v -> showSettings());
        head.addView(manage);
        card.addView(head);

        card.addView(statusRow("Leitura da tela", "Analisa a oferta aberta", access));
        card.addView(statusRow("Alertas dos apps", "Detecta novas oportunidades", notifications));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 12));
        content.addView(card, lp);
    }

    private View statusRow(String title, String subtitle, boolean enabled) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, PremiumUi.dp(this, 12), 0, 0);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(PremiumUi.text(this, title, 13, PremiumUi.TEXT, true));
        TextView sub = PremiumUi.text(this, subtitle, 11, PremiumUi.MUTED, false);
        sub.setPadding(0, PremiumUi.dp(this, 2), 0, 0);
        texts.addView(sub);
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(PremiumUi.chip(this,
                enabled ? "ATIVO" : "OFF",
                enabled ? PremiumUi.GREEN : PremiumUi.ORANGE,
                enabled ? Color.rgb(232, 250, 242) : Color.rgb(255, 246, 232)));
        return row;
    }

    private void addBestCard() {
        OpportunityRanker.RankingResult ranking = OpportunityRanker.rank(items);
        if (ranking == null || ranking.best == null) return;
        Opportunity best = ranking.best;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18));
        card.setBackground(PremiumUi.shape(PremiumUi.NAVY, PremiumUi.dp(this, 22), PremiumUi.NAVY, 0));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(PremiumUi.chip(this, "MELHOR AGORA", PremiumUi.NAVY, PremiumUi.CYAN));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        top.addView(PremiumUi.text(this, best.platform + " • " + best.category, 12, Color.WHITE, true));
        card.addView(top);

        TextView price = PremiumUi.text(this, money(best.price), 36, Color.WHITE, true);
        price.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        price.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        card.addView(price);

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        metrics.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        metrics.addView(metricDark("DISTÂNCIA", String.format(ptBr, "%.1f km", best.totalKm)), weighted(0, 5));
        metrics.addView(metricDark("R$/KM", String.format(ptBr, "R$ %.2f", best.grossPerKm)), weighted(5, 5));
        metrics.addView(metricDark("R$/H", best.grossPerHour > 0 ? String.format(ptBr, "R$ %.0f", best.grossPerHour) : "—"), weighted(5, 0));
        card.addView(metrics);

        TextView reason = PremiumUi.text(this, ranking.reason, 11, Color.rgb(190, 225, 235), false);
        reason.setPadding(0, PremiumUi.dp(this, 11), 0, 0);
        card.addView(reason);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 20));
        content.addView(card, lp);
    }

    private LinearLayout metricDark(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(PremiumUi.dp(this, 10), PremiumUi.dp(this, 9), PremiumUi.dp(this, 10), PremiumUi.dp(this, 9));
        box.setBackground(PremiumUi.shape(PremiumUi.NAVY_SOFT, PremiumUi.dp(this, 13), PremiumUi.NAVY_SOFT, 0));
        box.addView(PremiumUi.text(this, label, 9, Color.rgb(173, 205, 219), true));
        TextView v = PremiumUi.text(this, value, 13, Color.WHITE, true);
        v.setPadding(0, PremiumUi.dp(this, 3), 0, 0);
        box.addView(v);
        return box;
    }

    private LinearLayout.LayoutParams weighted(int left, int right) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(PremiumUi.dp(this, left), 0, PremiumUi.dp(this, right), 0);
        return lp;
    }

    private void addRecentSection() {
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.addView(PremiumUi.text(this, "Oportunidades recentes", 18, PremiumUi.TEXT, true));
        heading.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        TextView refresh = PremiumUi.text(this, "Atualizar", 12, PremiumUi.NAVY, true);
        refresh.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 8), 0, PremiumUi.dp(this, 8));
        refresh.setOnClickListener(v -> render());
        heading.addView(refresh);
        content.addView(heading);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.addView(filterChip("Todas", "ALL"));
        chips.addView(filterChip("Corridas", Opportunity.TYPE_RIDE));
        chips.addView(filterChip("Entregas", Opportunity.TYPE_DELIVERY));
        chips.addView(filterChip("Rotas", Opportunity.TYPE_ROUTE));
        hsv.addView(chips);
        LinearLayout.LayoutParams hsvLp = new LinearLayout.LayoutParams(-1, -2);
        hsvLp.setMargins(0, PremiumUi.dp(this, 10), 0, PremiumUi.dp(this, 10));
        content.addView(hsv, hsvLp);

        List<Opportunity> filtered = new ArrayList<>();
        for (Opportunity item : items) {
            if ("ALL".equals(activeFilter) || activeFilter.equals(item.type)) filtered.add(item);
        }

        if (filtered.isEmpty()) {
            LinearLayout empty = PremiumUi.card(this);
            TextView icon = PremiumUi.text(this, "◎", 26, PremiumUi.CYAN, true);
            empty.addView(icon);
            TextView t = PremiumUi.text(this, "Nenhuma oportunidade por enquanto", 15, PremiumUi.TEXT, true);
            t.setPadding(0, PremiumUi.dp(this, 8), 0, 0);
            empty.addView(t);
            TextView b = PremiumUi.text(this, "Abra uma simulação ou um app compatível para começar.", 12, PremiumUi.MUTED, false);
            b.setPadding(0, PremiumUi.dp(this, 4), 0, 0);
            empty.addView(b);
            content.addView(empty);
            return;
        }

        OpportunityRanker.RankingResult ranking = OpportunityRanker.rank(items);
        Opportunity best = ranking == null ? null : ranking.best;
        for (Opportunity item : filtered) content.addView(opportunityCard(item, item == best));
    }

    private TextView filterChip(String label, String filter) {
        int count = 0;
        for (Opportunity item : items) {
            if ("ALL".equals(filter) || filter.equals(item.type)) count++;
        }
        boolean active = filter.equals(activeFilter);
        TextView chip = PremiumUi.chip(this, label + "  " + count,
                active ? PremiumUi.NAVY : PremiumUi.MUTED,
                active ? PremiumUi.CYAN_SOFT : PremiumUi.SURFACE);
        chip.setBackground(PremiumUi.shape(
                active ? PremiumUi.CYAN_SOFT : PremiumUi.SURFACE,
                PremiumUi.dp(this, 999), PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(0, 0, PremiumUi.dp(this, 8), 0);
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> {
            activeFilter = filter;
            render();
        });
        return chip;
    }

    private View opportunityCard(Opportunity item, boolean best) {
        LinearLayout card = PremiumUi.card(this);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = PremiumUi.text(this, item.platform, 12, PremiumUi.TEXT, true);
        top.addView(brand);
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        if (best) top.addView(PremiumUi.chip(this, "MELHOR", PremiumUi.GREEN, Color.rgb(232, 250, 242)));
        card.addView(top);

        TextView category = PremiumUi.text(this, item.category, 12, PremiumUi.MUTED, false);
        category.setPadding(0, PremiumUi.dp(this, 2), 0, 0);
        card.addView(category);

        LinearLayout value = new LinearLayout(this);
        value.setOrientation(LinearLayout.HORIZONTAL);
        value.setGravity(Gravity.BOTTOM);
        value.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        TextView price = PremiumUi.text(this, money(item.price), 26, PremiumUi.TEXT, true);
        value.addView(price);
        value.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        String stats = String.format(ptBr, "%.1f km   •   R$ %.2f/km", item.totalKm, item.grossPerKm);
        TextView metric = PremiumUi.text(this, stats, 11, PremiumUi.MUTED, true);
        value.addView(metric);
        card.addView(value);

        String footer = item.minutes > 0 ? item.minutes + " min" : "tempo não informado";
        if (item.grossPerHour > 0) footer += String.format(ptBr, "   •   R$ %.0f/h", item.grossPerHour);
        TextView ft = PremiumUi.text(this, footer, 11, PremiumUi.MUTED, false);
        ft.setPadding(0, PremiumUi.dp(this, 8), 0, 0);
        card.addView(ft);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 9));
        card.setLayoutParams(lp);
        return card;
    }

    private View buildBottomNav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 6), PremiumUi.dp(this, 8), PremiumUi.dp(this, 6));
        bar.setBackground(PremiumUi.shape(PremiumUi.SURFACE, 0, PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        bar.addView(navItem("Central", true, v -> {}), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Mídia", false, v -> openMedia()), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Ajustes", false, v -> showSettings()), new LinearLayout.LayoutParams(0, -1, 1f));
        return bar;
    }

    private TextView navItem(String label, boolean active, View.OnClickListener listener) {
        TextView item = PremiumUi.text(this, label, 12, active ? PremiumUi.NAVY : PremiumUi.MUTED, true);
        item.setGravity(Gravity.CENTER);
        item.setBackground(PremiumUi.shape(active ? PremiumUi.CYAN_SOFT : Color.TRANSPARENT,
                PremiumUi.dp(this, 14), Color.TRANSPARENT, 0));
        item.setOnClickListener(listener);
        return item;
    }

    private void openMedia() {
        SharedPreferences prefs = getSharedPreferences("no_media", MODE_PRIVATE);
        boolean has = !TextUtils.isEmpty(prefs.getString("last_request_id", ""));
        startActivity(new Intent(this, has ? CampaignAnalysisPremiumActivity.class : AdvertiserActivityPremium.class));
    }

    private void showSettings() {
        String[] options = {
                "Acessibilidade — " + (isAccessibilityEnabled() ? "Ativa" : "Desativada"),
                "Alertas dos aplicativos — " + (isNotificationListenerEnabled() ? "Ativos" : "Desativados")
        };
        new AlertDialog.Builder(this)
                .setTitle("Conexões do NÓ")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    else configureNotifications();
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

    private void configureNotifications() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
            return;
        }
        openNotificationListenerSettings();
    }

    private void openNotificationListenerSettings() {
        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    private boolean isAccessibilityEnabled() {
        String expected = new ComponentName(this, ScreenAccessibilityService.class).flattenToString();
        String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return containsComponent(enabled, expected);
    }

    private boolean isNotificationListenerEnabled() {
        String expected = new ComponentName(this, NoNotificationListenerService.class).flattenToString();
        String enabled = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return containsComponent(enabled, expected);
    }

    private boolean containsComponent(String enabled, String expected) {
        if (enabled == null || expected == null) return false;
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);
        while (splitter.hasNext()) if (expected.equalsIgnoreCase(splitter.next())) return true;
        return false;
    }

    private String money(double value) {
        return String.format(ptBr, "R$ %.2f", value);
    }
}
