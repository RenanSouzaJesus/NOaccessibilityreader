package com.noapp.accessreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Tela de leitura rápida para o motorista. Mostra primeiro o que entrou,
 * quanto o carro consumiu e quanto tende a ter sobrado. A análise extensa
 * continua disponível em TripsActivityV2.
 */
public class TripsOverviewActivity extends Activity {

    private final Locale ptBr = new Locale("pt", "BR");
    private LinearLayout content;
    private VehicleProfile vehicleProfile = VehicleProfile.empty();
    private DriverGoalStore.DriverGoal goal = new DriverGoalStore.DriverGoal(0, 0);
    private final List<TripRecord> trips = new ArrayList<>();
    private DriverDayEngine.DaySummary summary;
    private boolean hasVehicleCost;
    private int openTrips;

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
        trips.clear();
        trips.addAll(TripStore.listSince(this, todayStart()));
        vehicleProfile = VehicleProfileStore.load(this);
        goal = DriverGoalStore.load(this);
        summary = buildSummary();

        addHeader();
        addResultHero();
        addGoalCard();
        addQuickNumbers();
        addVehicleCard();
        addNoInsight();
        addRecentTrips();
        addAnalysisButton();
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
        row.addView(PremiumUi.chip(this, "HOJE", PremiumUi.NAVY, PremiumUi.CYAN_SOFT));
        content.addView(row);

        TextView title = PremiumUi.text(this, "Quanto sobrou hoje?", 25, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        content.addView(title, titleLp);

        TextView sub = PremiumUi.text(this,
                "Uma leitura rápida do seu dia. Detalhes ficam na análise completa.",
                12, PremiumUi.MUTED, false);
        sub.setPadding(0, PremiumUi.dp(this, 5), 0, PremiumUi.dp(this, 14));
        content.addView(sub);
    }

    private void addResultHero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18));
        hero.setBackground(PremiumUi.shape(PremiumUi.NAVY, PremiumUi.dp(this, 22), PremiumUi.NAVY, 0));

        TextView label = PremiumUi.text(this,
                hasVehicleCost ? "LUCRO ESTIMADO" : "FATURAMENTO CONCLUÍDO",
                10, Color.rgb(190, 211, 222), true);
        hero.addView(label);

        TextView value = PremiumUi.text(this,
                money(hasVehicleCost ? summary.profit : summary.revenue),
                38, Color.WHITE, true);
        value.setPadding(0, PremiumUi.dp(this, 10), 0, 0);
        hero.addView(value);

        LinearLayout split = new LinearLayout(this);
        split.setOrientation(LinearLayout.HORIZONTAL);
        split.setPadding(0, PremiumUi.dp(this, 14), 0, 0);
        split.addView(heroMetric("FATUROU", money(summary.revenue)), weight(0, 5));
        split.addView(heroMetric("VEÍCULO", hasVehicleCost ? money(summary.cost) : "configurar"), weight(5, 0));
        hero.addView(split);

        if (openTrips > 0) {
            TextView open = PremiumUi.text(this,
                    openTrips + (openTrips == 1 ? " viagem em andamento" : " viagens em andamento"),
                    10, Color.rgb(190, 211, 222), false);
            open.setPadding(0, PremiumUi.dp(this, 10), 0, 0);
            hero.addView(open);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 12));
        content.addView(hero, lp);
    }

    private LinearLayout heroMetric(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(PremiumUi.dp(this, 11), PremiumUi.dp(this, 10), PremiumUi.dp(this, 11), PremiumUi.dp(this, 10));
        box.setBackground(PremiumUi.shape(PremiumUi.NAVY_SOFT, PremiumUi.dp(this, 14), PremiumUi.NAVY_SOFT, 0));
        box.addView(PremiumUi.text(this, label, 9, Color.rgb(173, 205, 219), true));
        TextView v = PremiumUi.text(this, value, 15, Color.WHITE, true);
        v.setPadding(0, PremiumUi.dp(this, 4), 0, 0);
        box.addView(v);
        return box;
    }

    private void addGoalCard() {
        LinearLayout card = PremiumUi.card(this);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(PremiumUi.text(this, "Meta do dia", 15, PremiumUi.TEXT, true));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        TextView edit = PremiumUi.text(this, goal.hasDailyGoal() ? "Editar" : "Definir", 12, PremiumUi.NAVY, true);
        edit.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 6), 0, PremiumUi.dp(this, 6));
        edit.setOnClickListener(v -> startActivity(new Intent(this, DriverGoalSettingsActivity.class)));
        top.addView(edit);
        card.addView(top);

        if (!goal.hasDailyGoal()) {
            TextView body = PremiumUi.text(this,
                    "Defina quanto você quer que sobre no dia. O NÓ acompanha lucro estimado, não apenas faturamento.",
                    11, PremiumUi.MUTED, false);
            body.setLineSpacing(PremiumUi.dp(this, 2), 1f);
            body.setPadding(0, PremiumUi.dp(this, 7), 0, 0);
            card.addView(body);
        } else {
            LinearLayout numbers = new LinearLayout(this);
            numbers.setOrientation(LinearLayout.HORIZONTAL);
            numbers.setGravity(Gravity.BOTTOM);
            numbers.setPadding(0, PremiumUi.dp(this, 10), 0, 0);
            numbers.addView(PremiumUi.text(this,
                    String.format(ptBr, "%.0f%%", summary.goalProgressPct),
                    27, PremiumUi.NAVY, true));
            numbers.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
            numbers.addView(PremiumUi.text(this, "meta " + money(goal.dailyNetGoal), 11, PremiumUi.MUTED, true));
            card.addView(numbers);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(1000);
            bar.setProgress((int) Math.round(summary.goalProgressPct * 10));
            bar.setProgressTintList(ColorStateList.valueOf(PremiumUi.CYAN));
            bar.setProgressBackgroundTintList(ColorStateList.valueOf(PremiumUi.SOFT));
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 8));
            barLp.setMargins(0, PremiumUi.dp(this, 9), 0, 0);
            card.addView(bar, barLp);

            String line = summary.goalReached
                    ? "Meta líquida atingida."
                    : "Faltam " + money(summary.remainingToGoal) + " para sua meta.";
            TextView status = PremiumUi.text(this, line, 12,
                    summary.goalReached ? PremiumUi.GREEN : PremiumUi.MUTED,
                    summary.goalReached);
            status.setPadding(0, PremiumUi.dp(this, 9), 0, 0);
            card.addView(status);

            if (goal.hasHourLimit()) {
                TextView hours = PremiumUi.text(this,
                        formatMinutes(summary.minutes) + " em viagens • limite pessoal " + formatHours(goal.maxHours),
                        10, PremiumUi.MUTED, false);
                hours.setPadding(0, PremiumUi.dp(this, 5), 0, 0);
                card.addView(hours);
            }
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 12));
        content.addView(card, lp);
    }

    private void addQuickNumbers() {
        TextView heading = PremiumUi.text(this, "Seu dia em 4 números", 17, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams headingLp = new LinearLayout.LayoutParams(-1, -2);
        headingLp.setMargins(0, PremiumUi.dp(this, 6), 0, PremiumUi.dp(this, 9));
        content.addView(heading, headingLp);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(quickMetric("VIAGENS", String.valueOf(summary.completedTrips)), weight(0, 5));
        row1.addView(quickMetric("KM", String.format(ptBr, "%.1f", summary.totalKm)), weight(5, 0));
        content.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams row2Lp = new LinearLayout.LayoutParams(-1, -2);
        row2Lp.setMargins(0, PremiumUi.dp(this, 9), 0, 0);
        content.addView(row2, row2Lp);
        row2.addView(quickMetric("TEMPO EM VIAGEM", formatMinutes(summary.minutes)), weight(0, 5));
        row2.addView(quickMetric(hasVehicleCost ? "LUCRO/KM" : "R$/KM",
                hasVehicleCost ? money(summary.profitPerKm) : grossPerKm()), weight(5, 0));
    }

    private LinearLayout quickMetric(String label, String value) {
        LinearLayout card = PremiumUi.card(this);
        card.addView(PremiumUi.text(this, label, 9, PremiumUi.MUTED, true));
        TextView v = PremiumUi.text(this, value, 19, PremiumUi.TEXT, true);
        v.setPadding(0, PremiumUi.dp(this, 6), 0, 0);
        card.addView(v);
        return card;
    }

    private void addVehicleCard() {
        TextView heading = PremiumUi.text(this, "Seu veículo", 17, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(-1, -2);
        hLp.setMargins(0, PremiumUi.dp(this, 20), 0, PremiumUi.dp(this, 9));
        content.addView(heading, hLp);

        LinearLayout card = PremiumUi.card(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.addView(PremiumUi.text(this,
                vehicleProfile.isConfigured() ? vehicleProfile.displayName() : "Veículo não configurado",
                14, PremiumUi.TEXT, true));

        String detail;
        if (vehicleProfile.isConfigured()) {
            VehicleCostEngine.CostEstimate km = VehicleCostEngine.estimate(vehicleProfile, 1, 0, 0);
            detail = String.format(ptBr, "R$ %.2f/km estimados • reserva hoje %s",
                    km.totalPerKm,
                    money(summary.maintenanceReserve));
        } else {
            detail = "Cadastre combustível, consumo e custos do carro.";
        }
        TextView d = PremiumUi.text(this, detail, 11, PremiumUi.MUTED, false);
        d.setPadding(0, PremiumUi.dp(this, 4), 0, 0);
        text.addView(d);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView edit = PremiumUi.chip(this,
                vehicleProfile.isConfigured() ? "EDITAR" : "CONFIGURAR",
                PremiumUi.NAVY,
                PremiumUi.CYAN_SOFT);
        edit.setOnClickListener(v -> startActivity(new Intent(this, VehicleSettingsActivity.class)));
        row.addView(edit);
        card.addView(row);
        content.addView(card);
    }

    private void addNoInsight() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(PremiumUi.dp(this, 16), PremiumUi.dp(this, 15), PremiumUi.dp(this, 16), PremiumUi.dp(this, 15));
        card.setBackground(PremiumUi.shape(PremiumUi.CYAN_SOFT, PremiumUi.dp(this, 18), PremiumUi.CYAN_SOFT, 0));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        content.addView(card, lp);

        card.addView(PremiumUi.text(this, "LEITURA NÓ", 9, PremiumUi.NAVY, true));
        TextView main = PremiumUi.text(this,
                DriverDayEngine.insight(summary, hasVehicleCost),
                14, PremiumUi.TEXT, true);
        main.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        main.setPadding(0, PremiumUi.dp(this, 7), 0, 0);
        card.addView(main);

        if (hasVehicleCost && summary.revenue > 0) {
            TextView secondary = PremiumUi.text(this,
                    String.format(ptBr, "Para faturar R$ 100, o veículo consumiu cerca de R$ %.2f.", summary.costPer100Revenue),
                    10, PremiumUi.MUTED, false);
            secondary.setPadding(0, PremiumUi.dp(this, 6), 0, 0);
            card.addView(secondary);
        }
    }

    private void addRecentTrips() {
        TextView heading = PremiumUi.text(this, "Últimas viagens", 17, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(-1, -2);
        hLp.setMargins(0, PremiumUi.dp(this, 20), 0, PremiumUi.dp(this, 9));
        content.addView(heading, hLp);

        if (trips.isEmpty()) {
            LinearLayout empty = PremiumUi.card(this);
            empty.addView(PremiumUi.text(this, "Nenhuma viagem registrada hoje", 14, PremiumUi.TEXT, true));
            TextView body = PremiumUi.text(this,
                    "Ofertas rejeitadas continuam em Oportunidades e não entram aqui.",
                    11, PremiumUi.MUTED, false);
            body.setPadding(0, PremiumUi.dp(this, 5), 0, 0);
            empty.addView(body);
            content.addView(empty);
            return;
        }

        int limit = Math.min(4, trips.size());
        for (int i = 0; i < limit; i++) content.addView(compactTripCard(trips.get(i)));
    }

    private View compactTripCard(TripRecord trip) {
        LinearLayout card = PremiumUi.card(this);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(PremiumUi.text(this, trip.platform + " • " + trip.category, 13, PremiumUi.TEXT, true));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new java.util.Date(trip.acceptedAt));
        top.addView(PremiumUi.text(this, time, 10, PremiumUi.MUTED, false));
        card.addView(top);

        LinearLayout valueRow = new LinearLayout(this);
        valueRow.setOrientation(LinearLayout.HORIZONTAL);
        valueRow.setGravity(Gravity.BOTTOM);
        valueRow.setPadding(0, PremiumUi.dp(this, 10), 0, 0);
        valueRow.addView(PremiumUi.text(this, money(trip.price), 23, PremiumUi.TEXT, true));
        valueRow.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));

        VehicleCostEngine.CostEstimate cost = estimateFor(trip);
        if (cost.configured && trip.isCompleted()) {
            valueRow.addView(PremiumUi.text(this, "lucro " + money(cost.profit), 11, PremiumUi.GREEN, true));
        } else {
            valueRow.addView(PremiumUi.chip(this,
                    trip.isCompleted() ? "CONCLUÍDA" : "EM ANDAMENTO",
                    trip.isCompleted() ? PremiumUi.GREEN : PremiumUi.ORANGE,
                    trip.isCompleted() ? Color.rgb(232, 250, 242) : Color.rgb(255, 246, 232)));
        }
        card.addView(valueRow);

        TextView meta = PremiumUi.text(this,
                String.format(ptBr, "%.1f km • %d min", trip.totalKm, trip.minutes),
                10, PremiumUi.MUTED, false);
        meta.setPadding(0, PremiumUi.dp(this, 6), 0, 0);
        card.addView(meta);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 8));
        card.setLayoutParams(lp);
        return card;
    }

    private void addAnalysisButton() {
        Button button = PremiumUi.secondaryButton(this, "Ver análise completa");
        button.setOnClickListener(v -> startActivity(new Intent(this, TripsActivityV2.class)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50));
        lp.setMargins(0, PremiumUi.dp(this, 4), 0, 0);
        content.addView(button, lp);
    }

    private DriverDayEngine.DaySummary buildSummary() {
        double revenue = 0;
        double cost = 0;
        double profit = 0;
        double totalKm = 0;
        double pickupKm = 0;
        double maintenanceReserve = 0;
        int minutes = 0;
        int completed = 0;
        openTrips = 0;
        hasVehicleCost = false;

        for (TripRecord trip : trips) {
            if (!trip.isCompleted()) {
                openTrips++;
                continue;
            }

            completed++;
            revenue += trip.price;
            totalKm += trip.totalKm;
            pickupKm += trip.pickupKm;
            minutes += trip.minutes;

            VehicleCostEngine.CostEstimate e = estimateFor(trip);
            if (e.configured) {
                hasVehicleCost = true;
                cost += e.totalCost;
                profit += e.profit;
                maintenanceReserve += e.maintenanceCost;
            }
        }

        if (!hasVehicleCost) profit = revenue;

        return DriverDayEngine.summarize(
                revenue,
                cost,
                profit,
                totalKm,
                pickupKm,
                minutes,
                completed,
                maintenanceReserve,
                goal.dailyNetGoal,
                goal.maxHours
        );
    }

    private VehicleCostEngine.CostEstimate estimateFor(TripRecord trip) {
        if (trip.hasCostSnapshot()) {
            double totalCost = trip.estimatedCost;
            double profit = trip.estimatedProfit;
            double cost100 = trip.price > 0 ? totalCost / trip.price * 100.0 : 0;
            double revenuePerCost = totalCost > 0 ? trip.price / totalCost : 0;
            double profitPerKm = trip.totalKm > 0 ? profit / trip.totalKm : 0;
            double profitPerHour = trip.minutes > 0 ? profit / (trip.minutes / 60.0) : 0;
            return new VehicleCostEngine.CostEstimate(
                    trip.vehicleName,
                    true,
                    trip.fuelCostPerKm,
                    trip.maintenanceCostPerKm,
                    trip.depreciationCostPerKm,
                    trip.fixedCostPerKm,
                    trip.totalCostPerKm,
                    trip.fuelCostPerKm * trip.totalKm,
                    trip.maintenanceCostPerKm * trip.totalKm,
                    trip.depreciationCostPerKm * trip.totalKm,
                    trip.fixedCostPerKm * trip.totalKm,
                    totalCost,
                    profit,
                    trip.estimatedMarginPct,
                    cost100,
                    revenuePerCost,
                    profitPerKm,
                    profitPerHour
            );
        }
        return VehicleCostEngine.estimate(vehicleProfile, trip.totalKm, trip.price, trip.minutes);
    }

    private String grossPerKm() {
        return summary.totalKm > 0 ? money(summary.revenue / summary.totalKm) : "—";
    }

    private long todayStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private LinearLayout.LayoutParams weight(int left, int right) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(PremiumUi.dp(this, left), 0, PremiumUi.dp(this, right), 0);
        return lp;
    }

    private String money(double value) {
        return String.format(ptBr, "R$ %.2f", value);
    }

    private String formatMinutes(int minutes) {
        if (minutes <= 0) return "0 min";
        int h = minutes / 60;
        int m = minutes % 60;
        return h > 0 ? h + "h " + m + "min" : m + " min";
    }

    private String formatHours(double hours) {
        if (hours <= 0) return "—";
        if (Math.rint(hours) == hours) return String.format(ptBr, "%.0fh", hours);
        return String.format(ptBr, "%.1fh", hours);
    }

    private View buildBottomNav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(PremiumUi.dp(this, 6), PremiumUi.dp(this, 6), PremiumUi.dp(this, 6), PremiumUi.dp(this, 6));
        bar.setBackground(PremiumUi.shape(PremiumUi.SURFACE, 0, PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        bar.addView(navItem("Oportunidades", false, v -> open(OpportunitiesTripsActivity.class)), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Viagens", true, v -> {}), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Campanhas", false, v -> open(CampaignsTripsActivity.class)), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Ajustes", false, v -> open(DriverSettingsActivity.class)), new LinearLayout.LayoutParams(0, -1, 1f));
        return bar;
    }

    private TextView navItem(String label, boolean active, View.OnClickListener listener) {
        TextView item = PremiumUi.text(this, label, 10, active ? PremiumUi.NAVY : PremiumUi.MUTED, true);
        item.setGravity(Gravity.CENTER);
        item.setBackground(PremiumUi.shape(active ? PremiumUi.CYAN_SOFT : Color.TRANSPARENT,
                PremiumUi.dp(this, 14), Color.TRANSPARENT, 0));
        item.setOnClickListener(listener);
        return item;
    }

    private void open(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
