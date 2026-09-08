package com.noapp.accessreader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Dashboard das corridas efetivamente aceitas pelo motorista. */
public class TripsActivityPremium extends Activity {

    private final Locale ptBr = new Locale("pt", "BR");
    private LinearLayout content;
    private String period = "TODAY";
    private List<TripRecord> trips = new ArrayList<>();
    private VehicleProfile vehicleProfile = VehicleProfile.empty();

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
        trips = TripStore.listSince(this, periodStart());
        vehicleProfile = VehicleProfileStore.load(this);
        Stats stats = stats();

        addHeader();
        addPeriodTabs();
        addVehicleCard(vehicleProfile);
        addHero(stats);
        addMetrics(stats);
        addCostAnalysis(stats);
        addEfficiency(stats);
        addHistory();
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
        row.addView(PremiumUi.chip(this, "VIAGENS", PremiumUi.NAVY, PremiumUi.CYAN_SOFT));
        content.addView(row);

        TextView title = PremiumUi.text(this, "Seu resultado na rua", 25, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        content.addView(title, titleLp);

        TextView sub = PremiumUi.text(this,
                "Veja o que entrou, quanto o veículo consumiu para gerar esse faturamento e o lucro estimado.",
                12, PremiumUi.MUTED, false);
        sub.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.setMargins(0, PremiumUi.dp(this, 5), 0, PremiumUi.dp(this, 14));
        content.addView(sub, subLp);
    }

    private void addPeriodTabs() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(periodChip("Hoje", "TODAY"), weight(0, 4));
        row.addView(periodChip("7 dias", "7D"), weight(4, 4));
        row.addView(periodChip("30 dias", "30D"), weight(4, 0));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 12));
        content.addView(row, lp);
    }

    private TextView periodChip(String label, String value) {
        boolean active = value.equals(period);
        TextView chip = PremiumUi.text(this, label, 12, active ? PremiumUi.NAVY : PremiumUi.MUTED, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 10), PremiumUi.dp(this, 8), PremiumUi.dp(this, 10));
        chip.setBackground(PremiumUi.shape(
                active ? PremiumUi.CYAN_SOFT : PremiumUi.SURFACE,
                PremiumUi.dp(this, 14), PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        chip.setOnClickListener(v -> {
            period = value;
            render();
        });
        return chip;
    }

    private void addVehicleCard(VehicleProfile profile) {
        LinearLayout card = PremiumUi.card(this);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView title = PremiumUi.text(this,
                profile.isConfigured() ? profile.displayName() : "Cadastre seu veículo",
                14, PremiumUi.TEXT, true);
        texts.addView(title);

        String detail;
        if (profile.isConfigured()) {
            VehicleCostEngine.CostEstimate oneKm = VehicleCostEngine.estimate(profile, 1, 0, 0);
            detail = String.format(ptBr, "Custo estimado R$ %.2f/km • %s",
                    oneKm.totalPerKm,
                    profile.fuelType.isEmpty() ? "combustível" : profile.fuelType);
        } else {
            detail = "Informe combustível, consumo e custos para liberar o lucro líquido estimado.";
        }
        TextView sub = PremiumUi.text(this, detail, 11, PremiumUi.MUTED, false);
        sub.setPadding(0, PremiumUi.dp(this, 3), 0, 0);
        texts.addView(sub);
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView action = PremiumUi.chip(this,
                profile.isConfigured() ? "EDITAR" : "CONFIGURAR",
                profile.isConfigured() ? PremiumUi.NAVY : PremiumUi.ORANGE,
                profile.isConfigured() ? PremiumUi.CYAN_SOFT : Color.rgb(255, 246, 232));
        action.setOnClickListener(v -> startActivity(new Intent(this, VehicleSettingsActivity.class)));
        row.addView(action);
        card.addView(row);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 12));
        content.addView(card, lp);
    }

    private void addHero(Stats s) {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18));
        hero.setBackground(PremiumUi.shape(PremiumUi.NAVY, PremiumUi.dp(this, 22), PremiumUi.NAVY, 0));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(PremiumUi.text(this,
                s.hasVehicleCost ? "LUCRO ESTIMADO" : "FATURAMENTO CONCLUÍDO",
                10, Color.rgb(190, 211, 222), true));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        String status = s.open > 0 ? s.completed + " concluídas • " + s.open + " em andamento" : s.completed + " concluídas";
        top.addView(PremiumUi.chip(this, status, PremiumUi.NAVY, PremiumUi.CYAN));
        hero.addView(top);

        TextView value = PremiumUi.text(this,
                money(s.hasVehicleCost ? s.profit : s.revenue),
                37, Color.WHITE, true);
        value.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        hero.addView(value);

        String helper;
        if (s.completed == 0) {
            helper = "Aceite uma corrida e finalize a simulação para alimentar o dashboard.";
        } else if (s.hasVehicleCost) {
            helper = String.format(ptBr,
                    "Bruto %s  •  custo %s  •  margem %.1f%%",
                    money(s.revenue), money(s.cost), s.marginPct);
        } else {
            helper = String.format(ptBr, "Ticket médio %s  •  %.1f km concluídos", money(s.ticket), s.totalKm);
        }
        TextView h = PremiumUi.text(this, helper, 11, Color.rgb(190, 211, 222), false);
        h.setPadding(0, PremiumUi.dp(this, 6), 0, 0);
        hero.addView(h);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 12));
        content.addView(hero, lp);
    }

    private void addMetrics(Stats s) {
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(metricCard("FATURAMENTO", money(s.revenue), "bruto concluído"), weight(0, 5));
        row1.addView(metricCard("CUSTO VEÍCULO", s.hasVehicleCost ? money(s.cost) : "—", "estimado"), weight(5, 0));
        content.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
        rowLp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        content.addView(row2, rowLp);
        row2.addView(metricCard("KM RODADOS", String.format(ptBr, "%.1f km", s.totalKm), "coleta + trajeto"), weight(0, 5));
        row2.addView(metricCard("TEMPO", formatMinutes(s.minutes), "tempo das corridas"), weight(5, 0));

        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams row3Lp = new LinearLayout.LayoutParams(-1, -2);
        row3Lp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        content.addView(row3, row3Lp);
        row3.addView(metricCard("R$/KM BRUTO", s.perKm > 0 ? money(s.perKm) : "—", "receita por km"), weight(0, 5));
        row3.addView(metricCard("LUCRO/KM", s.hasVehicleCost && s.totalKm > 0 ? money(s.profitPerKm) : "—", "líquido estimado"), weight(5, 0));
    }

    private LinearLayout metricCard(String label, String value, String sub) {
        LinearLayout card = PremiumUi.card(this);
        TextView l = PremiumUi.text(this, label, 9, PremiumUi.MUTED, true);
        l.setLetterSpacing(0.06f);
        card.addView(l);
        TextView v = PremiumUi.text(this, value, 20, PremiumUi.TEXT, true);
        v.setPadding(0, PremiumUi.dp(this, 7), 0, 0);
        card.addView(v);
        TextView sm = PremiumUi.text(this, sub, 10, PremiumUi.MUTED, false);
        sm.setPadding(0, PremiumUi.dp(this, 4), 0, 0);
        card.addView(sm);
        return card;
    }

    private void addCostAnalysis(Stats s) {
        TextView heading = PremiumUi.text(this, "Quanto o carro consumiu", 18, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.setMargins(0, PremiumUi.dp(this, 22), 0, PremiumUi.dp(this, 10));
        content.addView(heading, hlp);

        LinearLayout card = PremiumUi.card(this);
        if (!s.hasVehicleCost) {
            card.addView(PremiumUi.text(this, "Lucro ainda não configurado", 15, PremiumUi.TEXT, true));
            TextView body = PremiumUi.text(this,
                    "Cadastre o veículo para o NÓ separar faturamento, custo e o valor que realmente sobra.",
                    12, PremiumUi.MUTED, false);
            body.setPadding(0, PremiumUi.dp(this, 5), 0, PremiumUi.dp(this, 12));
            card.addView(body);
            TextView action = PremiumUi.text(this, "Configurar meu veículo →", 13, PremiumUi.NAVY, true);
            action.setPadding(0, PremiumUi.dp(this, 4), 0, PremiumUi.dp(this, 4));
            action.setOnClickListener(v -> startActivity(new Intent(this, VehicleSettingsActivity.class)));
            card.addView(action);
            content.addView(card);
            return;
        }

        addInfo(card, "COMBUSTÍVEL", money(s.fuelCost));
        addDivider(card);
        addInfo(card, "MANUTENÇÃO RESERVADA", money(s.maintenanceCost));
        addDivider(card);
        addInfo(card, "DEPRECIAÇÃO ESTIMADA", money(s.depreciationCost));
        addDivider(card);
        addInfo(card, "FIXOS RATEADOS", money(s.fixedCost));
        addDivider(card);
        addInfo(card, "CUSTO TOTAL ESTIMADO", money(s.cost));
        addDivider(card);
        addInfo(card, "LUCRO ESTIMADO", money(s.profit));
        addDivider(card);
        addInfo(card, "MARGEM ESTIMADA", String.format(ptBr, "%.1f%%", s.marginPct));
        content.addView(card);

        LinearLayout insight = PremiumUi.card(this);
        LinearLayout.LayoutParams insightLp = new LinearLayout.LayoutParams(-1, -2);
        insightLp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        content.addView(insight, insightLp);
        insight.addView(PremiumUi.text(this, "LEITURA NÓ", 9, PremiumUi.CYAN, true));
        TextView main = PremiumUi.text(this,
                String.format(ptBr, "Para faturar R$ 100, o veículo consumiu cerca de R$ %.2f.", s.costPer100Revenue),
                15, PremiumUi.TEXT, true);
        main.setPadding(0, PremiumUi.dp(this, 8), 0, 0);
        insight.addView(main);
        String secondary = s.revenuePerCost > 0
                ? String.format(ptBr, "Cada R$ 1 de custo gerou R$ %.2f de faturamento.", s.revenuePerCost)
                : "Complete mais viagens para formar uma relação de custo e faturamento.";
        TextView sec = PremiumUi.text(this, secondary, 11, PremiumUi.MUTED, false);
        sec.setPadding(0, PremiumUi.dp(this, 5), 0, 0);
        insight.addView(sec);
    }

    private void addEfficiency(Stats s) {
        TextView heading = PremiumUi.text(this, "Eficiência operacional", 18, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.setMargins(0, PremiumUi.dp(this, 22), 0, PremiumUi.dp(this, 10));
        content.addView(heading, hlp);

        LinearLayout card = PremiumUi.card(this);
        addInfo(card, "KM COM PASSAGEIRO", String.format(ptBr, "%.1f km", s.routeKm));
        addDivider(card);
        addInfo(card, "KM ATÉ A COLETA", String.format(ptBr, "%.1f km", s.pickupKm));
        addDivider(card);
        addInfo(card, "% DE KM NÃO REMUNERADOS", s.totalKm > 0 ? String.format(ptBr, "%.1f%%", s.emptyPct) : "—");
        addDivider(card);
        addInfo(card, "TICKET MÉDIO", s.completed > 0 ? money(s.ticket) : "—");
        addDivider(card);
        addInfo(card, "R$/H BRUTO", s.perHour > 0 ? money(s.perHour) : "—");
        addDivider(card);
        addInfo(card, "LUCRO/H ESTIMADO", s.hasVehicleCost && s.profitPerHour != 0 ? money(s.profitPerHour) : "—");
        addDivider(card);
        addInfo(card, "PLATAFORMA QUE MAIS FATUROU", s.bestPlatform.isEmpty() ? "—" : s.bestPlatform);
        content.addView(card);
    }

    private void addHistory() {
        TextView heading = PremiumUi.text(this, "Viagens", 18, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.setMargins(0, PremiumUi.dp(this, 22), 0, PremiumUi.dp(this, 10));
        content.addView(heading, hlp);

        if (trips.isEmpty()) {
            LinearLayout empty = PremiumUi.card(this);
            empty.addView(PremiumUi.text(this, "Nenhuma viagem registrada", 15, PremiumUi.TEXT, true));
            TextView body = PremiumUi.text(this,
                    "Uma oportunidade rejeitada continua em Oportunidades e não aparece aqui.",
                    12, PremiumUi.MUTED, false);
            body.setPadding(0, PremiumUi.dp(this, 5), 0, 0);
            empty.addView(body);
            content.addView(empty);
            return;
        }

        for (TripRecord trip : trips) content.addView(tripCard(trip));
    }

    private View tripCard(TripRecord trip) {
        LinearLayout card = PremiumUi.card(this);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(PremiumUi.text(this, trip.platform + " • " + trip.category, 13, PremiumUi.TEXT, true));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        top.addView(PremiumUi.chip(this,
                trip.isCompleted() ? "CONCLUÍDA" : "EM ANDAMENTO",
                trip.isCompleted() ? PremiumUi.GREEN : PremiumUi.ORANGE,
                trip.isCompleted() ? Color.rgb(232, 250, 242) : Color.rgb(255, 246, 232)));
        card.addView(top);

        TextView price = PremiumUi.text(this, money(trip.price), 26, PremiumUi.TEXT, true);
        price.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        card.addView(price);

        VehicleCostEngine.CostEstimate cost = estimateFor(trip);
        String metrics = String.format(ptBr, "%.1f km  •  %d min  •  R$ %.2f/km",
                trip.totalKm, trip.minutes, trip.grossPerKm);
        TextView m = PremiumUi.text(this, metrics, 11, PremiumUi.MUTED, true);
        m.setPadding(0, PremiumUi.dp(this, 7), 0, 0);
        card.addView(m);

        if (cost.configured) {
            LinearLayout moneyRow = new LinearLayout(this);
            moneyRow.setOrientation(LinearLayout.HORIZONTAL);
            moneyRow.setPadding(0, PremiumUi.dp(this, 10), 0, 0);
            moneyRow.addView(smallMoney("CUSTO", money(cost.totalCost)), weight(0, 5));
            moneyRow.addView(smallMoney("LUCRO", money(cost.profit)), weight(5, 5));
            moneyRow.addView(smallMoney("MARGEM", String.format(ptBr, "%.0f%%", cost.marginPct)), weight(5, 0));
            card.addView(moneyRow);
        }

        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new java.util.Date(trip.acceptedAt));
        String footer = "Aceita às " + time;
        if (trip.isCompleted() && trip.completedAt > 0) {
            footer += "  •  finalizada às " + DateFormat.getTimeInstance(DateFormat.SHORT).format(new java.util.Date(trip.completedAt));
        }
        TextView f = PremiumUi.text(this, footer, 10, PremiumUi.MUTED, false);
        f.setPadding(0, PremiumUi.dp(this, 8), 0, 0);
        card.addView(f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 9));
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout smallMoney(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 8), PremiumUi.dp(this, 8), PremiumUi.dp(this, 8));
        box.setBackground(PremiumUi.shape(PremiumUi.SOFT, PremiumUi.dp(this, 12), PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        box.addView(PremiumUi.text(this, label, 8, PremiumUi.MUTED, true));
        TextView v = PremiumUi.text(this, value, 12, PremiumUi.TEXT, true);
        v.setPadding(0, PremiumUi.dp(this, 3), 0, 0);
        box.addView(v);
        return box;
    }

    private VehicleCostEngine.CostEstimate estimateFor(TripRecord trip) {
        if (trip.hasCostSnapshot()) {
            VehicleProfile snapshot = new VehicleProfile(
                    trip.vehicleName,
                    "",
                    0,
                    0,
                    trip.maintenanceCostPerKm,
                    trip.depreciationCostPerKm,
                    0,
                    0,
                    trip.acceptedAt
            );
            // O motor precisa dos quatro componentes. Como combustível/fixos já
            // estão congelados no registro, reconstruímos o resultado diretamente.
            double totalCost = trip.estimatedCost;
            double profit = trip.estimatedProfit;
            double margin = trip.estimatedMarginPct;
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
                    margin,
                    cost100,
                    revenuePerCost,
                    profitPerKm,
                    profitPerHour
            );
        }
        return VehicleCostEngine.estimate(vehicleProfile, trip.totalKm, trip.price, trip.minutes);
    }

    private void addInfo(LinearLayout card, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(PremiumUi.text(this, label, 10, PremiumUi.MUTED, true), new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(PremiumUi.text(this, value, 13, PremiumUi.TEXT, true));
        card.addView(row);
    }

    private void addDivider(LinearLayout card) {
        View d = new View(this);
        d.setBackgroundColor(PremiumUi.BORDER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 1));
        lp.setMargins(0, PremiumUi.dp(this, 12), 0, PremiumUi.dp(this, 12));
        card.addView(d, lp);
    }

    private Stats stats() {
        Stats s = new Stats();
        Map<String, Double> revenueByPlatform = new LinkedHashMap<>();

        for (TripRecord trip : trips) {
            if (!trip.isCompleted()) {
                s.open++;
                continue;
            }

            s.completed++;
            s.revenue += trip.price;
            s.totalKm += trip.totalKm;
            s.pickupKm += trip.pickupKm;
            s.routeKm += trip.routeKm;
            s.minutes += trip.minutes;

            VehicleCostEngine.CostEstimate cost = estimateFor(trip);
            if (cost.configured) {
                s.hasVehicleCost = true;
                s.cost += cost.totalCost;
                s.profit += cost.profit;
                s.fuelCost += cost.fuelCost;
                s.maintenanceCost += cost.maintenanceCost;
                s.depreciationCost += cost.depreciationCost;
                s.fixedCost += cost.fixedCost;
            }

            Double old = revenueByPlatform.get(trip.platform);
            revenueByPlatform.put(trip.platform, (old == null ? 0 : old) + trip.price);
        }

        if (!s.hasVehicleCost) s.profit = s.revenue;
        s.ticket = s.completed > 0 ? s.revenue / s.completed : 0;
        s.perKm = s.totalKm > 0 ? s.revenue / s.totalKm : 0;
        s.perHour = s.minutes > 0 ? s.revenue / (s.minutes / 60.0) : 0;
        s.emptyPct = s.totalKm > 0 ? (s.pickupKm / s.totalKm) * 100.0 : 0;
        s.marginPct = s.hasVehicleCost && s.revenue > 0 ? (s.profit / s.revenue) * 100.0 : 0;
        s.profitPerKm = s.hasVehicleCost && s.totalKm > 0 ? s.profit / s.totalKm : 0;
        s.profitPerHour = s.hasVehicleCost && s.minutes > 0 ? s.profit / (s.minutes / 60.0) : 0;
        s.costPer100Revenue = s.hasVehicleCost && s.revenue > 0 ? s.cost / s.revenue * 100.0 : 0;
        s.revenuePerCost = s.hasVehicleCost && s.cost > 0 ? s.revenue / s.cost : 0;

        double best = -1;
        for (Map.Entry<String, Double> entry : revenueByPlatform.entrySet()) {
            if (entry.getValue() > best) {
                best = entry.getValue();
                s.bestPlatform = entry.getKey() + " • " + money(entry.getValue());
            }
        }
        return s;
    }

    private long periodStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if ("7D".equals(period)) c.add(Calendar.DAY_OF_YEAR, -6);
        else if ("30D".equals(period)) c.add(Calendar.DAY_OF_YEAR, -29);
        return c.getTimeInMillis();
    }

    private LinearLayout.LayoutParams weight(int left, int right) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(PremiumUi.dp(this, left), 0, PremiumUi.dp(this, right), 0);
        return lp;
    }

    private String formatMinutes(int minutes) {
        if (minutes <= 0) return "—";
        int h = minutes / 60;
        int m = minutes % 60;
        return h > 0 ? h + "h " + m + "min" : m + " min";
    }

    private String money(double value) {
        return String.format(ptBr, "R$ %.2f", value);
    }

    private View buildBottomNav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(PremiumUi.dp(this, 6), PremiumUi.dp(this, 6), PremiumUi.dp(this, 6), PremiumUi.dp(this, 6));
        bar.setBackground(PremiumUi.shape(PremiumUi.SURFACE, 0, PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        bar.addView(navItem("Oportunidades", false, v -> open(MainActivityPremium.class)), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Viagens", true, v -> {}), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Campanhas", false, v -> open(CampaignsActivityPremium.class)), new LinearLayout.LayoutParams(0, -1, 1f));
        bar.addView(navItem("Ajustes", false, v -> open(VehicleSettingsActivity.class)), new LinearLayout.LayoutParams(0, -1, 1f));
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

    private void open(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private static final class Stats {
        int completed;
        int open;
        int minutes;
        double revenue;
        double cost;
        double profit;
        double totalKm;
        double pickupKm;
        double routeKm;
        double ticket;
        double perKm;
        double perHour;
        double emptyPct;
        double marginPct;
        double profitPerKm;
        double profitPerHour;
        double costPer100Revenue;
        double revenuePerCost;
        double fuelCost;
        double maintenanceCost;
        double depreciationCost;
        double fixedCost;
        boolean hasVehicleCost;
        String bestPlatform = "";
    }
}
