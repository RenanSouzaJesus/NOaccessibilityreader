package com.noapp.accessreader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
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
        addHeader();
        addPeriodTabs();
        addHero();
        addMetrics();
        addEfficiency();
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
                "Só entram aqui corridas que o NÓ identificou como aceitas. Totais de faturamento e km consideram apenas as concluídas.",
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

    private void addHero() {
        Stats stats = stats();
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18));
        hero.setBackground(PremiumUi.shape(PremiumUi.NAVY, PremiumUi.dp(this, 22), PremiumUi.NAVY, 0));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(PremiumUi.text(this, "FATURAMENTO CONCLUÍDO", 10, Color.rgb(190, 211, 222), true));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        String status = stats.open > 0 ? stats.completed + " concluídas • " + stats.open + " em andamento" : stats.completed + " concluídas";
        top.addView(PremiumUi.chip(this, status, PremiumUi.NAVY, PremiumUi.CYAN));
        hero.addView(top);

        TextView value = PremiumUi.text(this, money(stats.revenue), 37, Color.WHITE, true);
        value.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        hero.addView(value);

        String helper = stats.completed == 0
                ? "Aceite uma corrida e finalize a simulação para alimentar o dashboard."
                : String.format(ptBr, "Ticket médio %s  •  %.1f km concluídos", money(stats.ticket), stats.totalKm);
        TextView h = PremiumUi.text(this, helper, 11, Color.rgb(190, 211, 222), false);
        h.setPadding(0, PremiumUi.dp(this, 6), 0, 0);
        hero.addView(h);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 12));
        content.addView(hero, lp);
    }

    private void addMetrics() {
        Stats s = stats();
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(metricCard("KM RODADOS", String.format(ptBr, "%.1f km", s.totalKm), "coleta + trajeto"), weight(0, 5));
        row1.addView(metricCard("TEMPO", formatMinutes(s.minutes), "tempo das corridas"), weight(5, 0));
        content.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
        rowLp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        content.addView(row2, rowLp);
        row2.addView(metricCard("R$/KM", s.perKm > 0 ? String.format(ptBr, "R$ %.2f", s.perKm) : "—", "receita por km"), weight(0, 5));
        row2.addView(metricCard("R$/H", s.perHour > 0 ? String.format(ptBr, "R$ %.2f", s.perHour) : "—", "produtividade"), weight(5, 0));
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

    private void addEfficiency() {
        Stats s = stats();
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

        String metrics = String.format(ptBr, "%.1f km  •  %d min  •  R$ %.2f/km",
                trip.totalKm, trip.minutes, trip.grossPerKm);
        TextView m = PremiumUi.text(this, metrics, 11, PremiumUi.MUTED, true);
        m.setPadding(0, PremiumUi.dp(this, 7), 0, 0);
        card.addView(m);

        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new java.util.Date(trip.acceptedAt));
        String footer = "Aceita às " + time;
        if (trip.isCompleted() && trip.completedAt > 0) {
            footer += "  •  finalizada às " + DateFormat.getTimeInstance(DateFormat.SHORT).format(new java.util.Date(trip.completedAt));
        }
        TextView f = PremiumUi.text(this, footer, 10, PremiumUi.MUTED, false);
        f.setPadding(0, PremiumUi.dp(this, 7), 0, 0);
        card.addView(f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, PremiumUi.dp(this, 9));
        card.setLayoutParams(lp);
        return card;
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
            Double old = revenueByPlatform.get(trip.platform);
            revenueByPlatform.put(trip.platform, (old == null ? 0 : old) + trip.price);
        }

        s.ticket = s.completed > 0 ? s.revenue / s.completed : 0;
        s.perKm = s.totalKm > 0 ? s.revenue / s.totalKm : 0;
        s.perHour = s.minutes > 0 ? s.revenue / (s.minutes / 60.0) : 0;
        s.emptyPct = s.totalKm > 0 ? (s.pickupKm / s.totalKm) * 100.0 : 0;

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
        bar.addView(navItem("Ajustes", false, v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))), new LinearLayout.LayoutParams(0, -1, 1f));
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
        double totalKm;
        double pickupKm;
        double routeKm;
        double ticket;
        double perKm;
        double perHour;
        double emptyPct;
        String bestPlatform = "";
    }
}
