package com.noapp.accessreader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

/** Tela exclusiva para criação e acompanhamento das campanhas do NÓ Mídia. */
public class CampaignsActivityPremium extends Activity {

    private static final int REQUEST_NOTIFICATIONS = 702;
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumUi.lightSystemBars(this);
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
        content.setPadding(
                PremiumUi.dp(this, 16),
                PremiumUi.dp(this, 14),
                PremiumUi.dp(this, 16),
                PremiumUi.dp(this, 24)
        );
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        root.addView(buildBottomNav(), new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 68)));
        setContentView(root);
    }

    private void render() {
        if (content == null) return;
        content.removeAllViews();
        addHeader();
        addCampaignContent();
    }

    private void addHeader() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout logo = new LinearLayout(this);
        logo.setOrientation(LinearLayout.HORIZONTAL);
        logo.setGravity(Gravity.BOTTOM);
        logo.addView(PremiumUi.text(this, "NÓ", 31, PremiumUi.NAVY, true));
        logo.addView(PremiumUi.text(this, ".", 31, PremiumUi.CYAN, true));
        row.addView(logo);
        row.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        row.addView(PremiumUi.chip(this, "NÓ MÍDIA", PremiumUi.ORANGE, Color.rgb(255, 246, 232)));
        content.addView(row);

        TextView title = PremiumUi.text(this, "Campanhas", 25, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        content.addView(title, titleLp);

        TextView sub = PremiumUi.text(this,
                "Crie e acompanhe suas solicitações de publicidade em uma área separada das oportunidades.",
                13, PremiumUi.MUTED, false);
        sub.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.setMargins(0, PremiumUi.dp(this, 5), 0, PremiumUi.dp(this, 18));
        content.addView(sub, subLp);
    }

    private void addCampaignContent() {
        SharedPreferences prefs = getSharedPreferences("no_media", MODE_PRIVATE);
        String requestId = prefs.getString("last_request_id", "");
        String campaign = prefs.getString("last_request_campaign", "");
        String company = prefs.getString("last_request_company", "");
        String status = prefs.getString("last_request_status", "");
        long requestTime = prefs.getLong("last_request_time", 0L);

        boolean hasCampaign = !TextUtils.isEmpty(requestId);
        if (!hasCampaign) {
            addEmptyCampaignState();
            return;
        }

        TextView section = PremiumUi.text(this, "Campanha atual", 19, PremiumUi.TEXT, true);
        content.addView(section);

        LinearLayout card = PremiumUi.card(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        content.addView(card, cardLp);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(PremiumUi.chip(this,
                TextUtils.isEmpty(status) ? "RECEBIDA" : status,
                PremiumUi.GREEN,
                Color.rgb(232, 250, 242)));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        top.addView(PremiumUi.chip(this, "ANUNCIANTE", PremiumUi.NAVY, PremiumUi.CYAN_SOFT));
        card.addView(top);

        TextView name = PremiumUi.text(this,
                TextUtils.isEmpty(campaign) ? "Campanha sem nome" : campaign,
                21,
                PremiumUi.TEXT,
                true);
        name.setPadding(0, PremiumUi.dp(this, 14), 0, 0);
        card.addView(name);

        if (!TextUtils.isEmpty(company)) {
            TextView companyView = PremiumUi.text(this, company, 12, PremiumUi.MUTED, false);
            companyView.setPadding(0, PremiumUi.dp(this, 4), 0, 0);
            card.addView(companyView);
        }

        String meta = requestId;
        if (requestTime > 0) {
            String when = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(requestTime));
            meta += "  •  " + when;
        }
        TextView metaView = PremiumUi.text(this, meta, 10, PremiumUi.MUTED, false);
        metaView.setPadding(0, PremiumUi.dp(this, 8), 0, 0);
        card.addView(metaView);

        Button viewCampaign = PremiumUi.primaryButton(this, "Ver campanha e análise");
        viewCampaign.setOnClickListener(v -> startActivity(
                new Intent(this, CampaignAnalysisPremiumActivity.class)
        ));
        LinearLayout.LayoutParams viewLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50));
        viewLp.setMargins(0, PremiumUi.dp(this, 16), 0, 0);
        card.addView(viewCampaign, viewLp);

        Button newCampaign = PremiumUi.secondaryButton(this, "Criar nova campanha");
        newCampaign.setOnClickListener(v -> startActivity(
                new Intent(this, AdvertiserActivityPremium.class)
        ));
        LinearLayout.LayoutParams newLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 48));
        newLp.setMargins(0, PremiumUi.dp(this, 9), 0, 0);
        card.addView(newCampaign, newLp);

        TextView note = PremiumUi.text(this,
                "O histórico completo de várias campanhas entra quando conectarmos o backend. Nesta versão, mostramos a solicitação mais recente salva no aparelho.",
                11, PremiumUi.MUTED, false);
        note.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(-1, -2);
        noteLp.setMargins(0, PremiumUi.dp(this, 14), 0, 0);
        content.addView(note, noteLp);
    }

    private void addEmptyCampaignState() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(
                PremiumUi.dp(this, 20),
                PremiumUi.dp(this, 20),
                PremiumUi.dp(this, 20),
                PremiumUi.dp(this, 20)
        );
        hero.setBackground(PremiumUi.shape(PremiumUi.NAVY, PremiumUi.dp(this, 22), PremiumUi.NAVY, 0));

        hero.addView(PremiumUi.chip(this, "NÓ MÍDIA", PremiumUi.NAVY, PremiumUi.CYAN));

        TextView title = PremiumUi.text(this, "Sua próxima campanha começa aqui", 22, Color.WHITE, true);
        title.setPadding(0, PremiumUi.dp(this, 16), 0, 0);
        hero.addView(title);

        TextView body = PremiumUi.text(this,
                "Escolha região, quantidade de veículos, faixa de ano, formato da publicidade e orçamento.",
                12, Color.rgb(190, 211, 222), false);
        body.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        body.setPadding(0, PremiumUi.dp(this, 6), 0, 0);
        hero.addView(body);

        Button create = PremiumUi.primaryButton(this, "Criar campanha");
        create.setOnClickListener(v -> startActivity(
                new Intent(this, AdvertiserActivityPremium.class)
        ));
        LinearLayout.LayoutParams createLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50));
        createLp.setMargins(0, PremiumUi.dp(this, 18), 0, 0);
        hero.addView(create, createLp);

        content.addView(hero);
    }

    private View buildBottomNav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 6), PremiumUi.dp(this, 8), PremiumUi.dp(this, 6));
        bar.setBackground(PremiumUi.shape(PremiumUi.SURFACE, 0, PremiumUi.BORDER, PremiumUi.dp(this, 1)));

        bar.addView(navItem("Oportunidades", false, v -> openOpportunities()), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Campanhas", true, v -> {}), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Ajustes", false, v -> showSettings()), new LinearLayout.LayoutParams(0, -1, 1f));
        return bar;
    }

    private TextView navItem(String label, boolean active, View.OnClickListener listener) {
        TextView item = PremiumUi.text(this, label, 11, active ? PremiumUi.NAVY : PremiumUi.MUTED, true);
        item.setGravity(Gravity.CENTER);
        item.setBackground(PremiumUi.shape(
                active ? PremiumUi.CYAN_SOFT : Color.TRANSPARENT,
                PremiumUi.dp(this, 14),
                Color.TRANSPARENT,
                0
        ));
        item.setOnClickListener(listener);
        return item;
    }

    private void openOpportunities() {
        Intent intent = new Intent(this, MainActivityPremium.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
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
        String enabled = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
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
        while (splitter.hasNext()) {
            if (expected.equalsIgnoreCase(splitter.next())) return true;
        }
        return false;
    }
}
