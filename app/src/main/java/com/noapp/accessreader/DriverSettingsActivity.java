package com.noapp.accessreader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.util.Locale;

/** Central simples de ajustes: veículo, meta e conexões. */
public class DriverSettingsActivity extends Activity {

    private final Locale ptBr = new Locale("pt", "BR");
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumUi.lightSystemBars(this);
        build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(PremiumUi.BG);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(PremiumUi.dp(this, 16), PremiumUi.dp(this, 14), PremiumUi.dp(this, 16), PremiumUi.dp(this, 28));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);
    }

    private void render() {
        if (content == null) return;
        content.removeAllViews();

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = PremiumUi.text(this, "‹  Voltar", 13, PremiumUi.NAVY, true);
        back.setPadding(0, PremiumUi.dp(this, 8), PremiumUi.dp(this, 8), PremiumUi.dp(this, 8));
        back.setOnClickListener(v -> finish());
        top.addView(back);
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        top.addView(PremiumUi.chip(this, "AJUSTES", PremiumUi.NAVY, PremiumUi.CYAN_SOFT));
        content.addView(top);

        TextView title = PremiumUi.text(this, "Seu NÓ, do seu jeito", 25, PremiumUi.TEXT, true);
        title.setPadding(0, PremiumUi.dp(this, 16), 0, 0);
        content.addView(title);

        TextView sub = PremiumUi.text(this,
                "Configure só o que muda suas decisões na rua.",
                12, PremiumUi.MUTED, false);
        sub.setPadding(0, PremiumUi.dp(this, 5), 0, PremiumUi.dp(this, 16));
        content.addView(sub);

        VehicleProfile vehicle = VehicleProfileStore.load(this);
        DriverGoalStore.DriverGoal goal = DriverGoalStore.load(this);

        LinearLayout vehicleCard = settingCard(
                "Meu veículo",
                vehicle.isConfigured()
                        ? vehicle.displayName() + " • custo estimado " + costPerKm(vehicle)
                        : "Cadastre combustível, consumo e custos do carro.",
                vehicle.isConfigured() ? "EDITAR" : "CONFIGURAR",
                v -> startActivity(new Intent(this, VehicleSettingsActivity.class))
        );
        content.addView(vehicleCard);

        LinearLayout goalCard = settingCard(
                "Minha meta",
                goal.hasDailyGoal()
                        ? "Quero que sobrem " + money(goal.dailyNetGoal) + " no dia" + (goal.hasHourLimit() ? " • limite " + trim(goal.maxHours) + "h" : "")
                        : "Defina uma meta de lucro estimado para o dia.",
                goal.hasDailyGoal() ? "EDITAR" : "DEFINIR",
                v -> startActivity(new Intent(this, DriverGoalSettingsActivity.class))
        );
        LinearLayout.LayoutParams goalLp = new LinearLayout.LayoutParams(-1, -2);
        goalLp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        goalCard.setLayoutParams(goalLp);
        content.addView(goalCard);

        TextView section = PremiumUi.text(this, "Conexões", 17, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams sectionLp = new LinearLayout.LayoutParams(-1, -2);
        sectionLp.setMargins(0, PremiumUi.dp(this, 22), 0, PremiumUi.dp(this, 9));
        content.addView(section, sectionLp);

        boolean access = isAccessibilityEnabled();
        boolean notifications = isNotificationListenerEnabled();

        LinearLayout accessCard = settingCard(
                "Leitura da tela",
                access ? "Ativa • o NÓ pode identificar oportunidades e estados de viagem." : "Desativada • necessária para leitura autorizada.",
                access ? "ATIVA" : "CONFIGURAR",
                v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );
        content.addView(accessCard);

        LinearLayout notificationsCard = settingCard(
                "Alertas dos aplicativos",
                notifications ? "Ativos • o NÓ pode usar notificações como sinal de oportunidade." : "Desativados • opcional para sinais de novas oportunidades.",
                notifications ? "ATIVOS" : "CONFIGURAR",
                v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        );
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(-1, -2);
        nLp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        notificationsCard.setLayoutParams(nLp);
        content.addView(notificationsCard);
    }

    private LinearLayout settingCard(String title, String body, String action, View.OnClickListener listener) {
        LinearLayout card = PremiumUi.card(this);
        card.setOnClickListener(listener);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(PremiumUi.text(this, title, 14, PremiumUi.TEXT, true));
        TextView b = PremiumUi.text(this, body, 11, PremiumUi.MUTED, false);
        b.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        b.setPadding(0, PremiumUi.dp(this, 4), PremiumUi.dp(this, 8), 0);
        text.addView(b);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView chip = PremiumUi.chip(this, action, PremiumUi.NAVY, PremiumUi.CYAN_SOFT);
        chip.setOnClickListener(listener);
        row.addView(chip);
        card.addView(row);
        return card;
    }

    private boolean isAccessibilityEnabled() {
        String expected = new ComponentName(this, TripAwareAccessibilityService.class).flattenToString();
        String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return containsComponent(enabled, expected);
    }

    private boolean isNotificationListenerEnabled() {
        String expected = new ComponentName(this, NoNotificationListenerService.class).flattenToString();
        String enabled = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return containsComponent(enabled, expected);
    }

    private boolean containsComponent(String enabled, String expected) {
        if (TextUtils.isEmpty(enabled) || TextUtils.isEmpty(expected)) return false;
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);
        while (splitter.hasNext()) if (expected.equalsIgnoreCase(splitter.next())) return true;
        return false;
    }

    private String costPerKm(VehicleProfile vehicle) {
        VehicleCostEngine.CostEstimate e = VehicleCostEngine.estimate(vehicle, 1, 0, 0);
        return String.format(ptBr, "R$ %.2f/km", e.totalPerKm);
    }

    private String money(double value) {
        return String.format(ptBr, "R$ %.2f", value);
    }

    private String trim(double value) {
        if (Math.rint(value) == value) return String.format(ptBr, "%.0f", value);
        return String.format(ptBr, "%.1f", value);
    }
}
