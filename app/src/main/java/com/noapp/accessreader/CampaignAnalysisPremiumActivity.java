package com.noapp.accessreader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Painel premium de leitura da campanha criada no NÓ Mídia. */
public class CampaignAnalysisPremiumActivity extends Activity {

    private static final String PREFS = "no_media";
    private final Locale ptBr = new Locale("pt", "BR");

    private String requestId;
    private String campaign;
    private String company;
    private String status;
    private String summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumUi.lightSystemBars(this);
        load();
        build();
    }

    private void load() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        requestId = p.getString("last_request_id", "");
        campaign = p.getString("last_request_campaign", "Campanha");
        company = p.getString("last_request_company", "");
        status = p.getString("last_request_status", "SOLICITAÇÃO RECEBIDA");
        summary = p.getString("last_request_summary", "");
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setBackgroundColor(PremiumUi.BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(PremiumUi.dp(this, 16), PremiumUi.dp(this, 14), PremiumUi.dp(this, 16), PremiumUi.dp(this, 28));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = PremiumUi.text(this, "‹  Central", 13, PremiumUi.NAVY, true);
        back.setPadding(0, PremiumUi.dp(this, 8), PremiumUi.dp(this, 8), PremiumUi.dp(this, 8));
        back.setOnClickListener(v -> finish());
        toolbar.addView(back);
        toolbar.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        toolbar.addView(PremiumUi.chip(this, "NÓ MÍDIA", PremiumUi.ORANGE, Color.rgb(255, 246, 232)));
        page.addView(toolbar);

        TextView title = PremiumUi.text(this,
                TextUtils.isEmpty(campaign) ? "Sua campanha" : campaign,
                25, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, PremiumUi.dp(this, 14), 0, 0);
        page.addView(title, titleLp);

        TextView meta = PremiumUi.text(this,
                (TextUtils.isEmpty(company) ? "NÓ Mídia" : company) + (TextUtils.isEmpty(requestId) ? "" : "  •  " + requestId),
                12, PremiumUi.MUTED, false);
        meta.setPadding(0, PremiumUi.dp(this, 5), 0, 0);
        page.addView(meta);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18), PremiumUi.dp(this, 18));
        hero.setBackground(PremiumUi.shape(PremiumUi.NAVY, PremiumUi.dp(this, 22), PremiumUi.NAVY, 0));
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(-1, -2);
        heroLp.setMargins(0, PremiumUi.dp(this, 18), 0, 0);
        page.addView(hero, heroLp);

        LinearLayout heroTop = new LinearLayout(this);
        heroTop.setOrientation(LinearLayout.HORIZONTAL);
        heroTop.setGravity(Gravity.CENTER_VERTICAL);
        heroTop.addView(PremiumUi.text(this, "STATUS DA CAMPANHA", 10, Color.rgb(185, 205, 219), true));
        heroTop.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        heroTop.addView(PremiumUi.chip(this, "RECEBIDA", PremiumUi.NAVY, PremiumUi.CYAN));
        hero.addView(heroTop);

        double budget = extractMoney(section("ORÇAMENTO"));
        TextView budgetTitle = PremiumUi.text(this, "Orçamento informado", 12, Color.rgb(190, 211, 222), false);
        budgetTitle.setPadding(0, PremiumUi.dp(this, 14), 0, 0);
        hero.addView(budgetTitle);
        TextView budgetValue = PremiumUi.text(this, budget > 0 ? money(budget) : "—", 34, Color.WHITE, true);
        hero.addView(budgetValue);

        TextView heroBody = PremiumUi.text(this,
                "Briefing recebido. O próximo passo é cruzar região, veículo e formato com o inventário disponível.",
                11, Color.rgb(190, 211, 222), false);
        heroBody.setPadding(0, PremiumUi.dp(this, 8), 0, 0);
        hero.addView(heroBody);

        addMetrics(page, budget);
        addTimeline(page);
        addBriefing(page);
        addActions(page);

        setContentView(scroll);
    }

    private void addMetrics(LinearLayout page, double budget) {
        int vehicles = extractFirstInt(section("VEÍCULOS"));
        int days = campaignDays(section("PERÍODO"));
        double perVehicle = vehicles > 0 && budget > 0 ? budget / vehicles : 0;
        double perVehicleDay = vehicles > 0 && days > 0 && budget > 0 ? budget / vehicles / days : 0;

        TextView heading = PremiumUi.text(this, "Visão rápida", 18, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.setMargins(0, PremiumUi.dp(this, 22), 0, PremiumUi.dp(this, 10));
        page.addView(heading, hlp);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(metricCard("VEÍCULOS", vehicles > 0 ? String.valueOf(vehicles) : "—", "solicitados"), metricLp(0, 5));
        row1.addView(metricCard("DURAÇÃO", days > 0 ? days + " dias" : "—", "período informado"), metricLp(5, 0));
        page.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r2lp = new LinearLayout.LayoutParams(-1, -2);
        r2lp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        page.addView(row2, r2lp);
        row2.addView(metricCard("POR VEÍCULO", perVehicle > 0 ? money(perVehicle) : "—", "no período"), metricLp(0, 5));
        row2.addView(metricCard("VEÍCULO/DIA", perVehicleDay > 0 ? money(perVehicleDay) : "—", "média matemática"), metricLp(5, 0));
    }

    private LinearLayout metricCard(String label, String value, String sub) {
        LinearLayout card = PremiumUi.card(this);
        TextView l = PremiumUi.text(this, label, 10, PremiumUi.MUTED, true);
        l.setLetterSpacing(0.06f);
        card.addView(l);
        TextView v = PremiumUi.text(this, value, 19, PremiumUi.TEXT, true);
        v.setPadding(0, PremiumUi.dp(this, 7), 0, 0);
        card.addView(v);
        TextView s = PremiumUi.text(this, sub, 10, PremiumUi.MUTED, false);
        s.setPadding(0, PremiumUi.dp(this, 4), 0, 0);
        card.addView(s);
        return card;
    }

    private LinearLayout.LayoutParams metricLp(int left, int right) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(PremiumUi.dp(this, left), 0, PremiumUi.dp(this, right), 0);
        return lp;
    }

    private void addTimeline(LinearLayout page) {
        TextView heading = PremiumUi.text(this, "Andamento", 18, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.setMargins(0, PremiumUi.dp(this, 22), 0, PremiumUi.dp(this, 10));
        page.addView(heading, hlp);

        LinearLayout card = PremiumUi.card(this);
        card.addView(stage("1", "Solicitação recebida", "Briefing salvo no NÓ", true));
        card.addView(stage("2", "Consulta de inventário", "Veículos compatíveis por região e perfil", false));
        card.addView(stage("3", "Proposta", "Cobertura, disponibilidade e preço final", false));
        card.addView(stage("4", "Campanha ativa", "Veículos e mídia em circulação", false));
        page.addView(card);
    }

    private View stage(String number, String title, String subtitle, boolean done) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, PremiumUi.dp(this, 8), 0, PremiumUi.dp(this, 8));
        TextView bubble = PremiumUi.text(this, done ? "✓" : number, 12, done ? Color.WHITE : PremiumUi.MUTED, true);
        bubble.setGravity(Gravity.CENTER);
        bubble.setBackground(PremiumUi.shape(done ? PremiumUi.GREEN : PremiumUi.SOFT,
                PremiumUi.dp(this, 999), done ? PremiumUi.GREEN : PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        row.addView(bubble, new LinearLayout.LayoutParams(PremiumUi.dp(this, 32), PremiumUi.dp(this, 32)));
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView t = PremiumUi.text(this, title, 13, done ? PremiumUi.TEXT : PremiumUi.MUTED, true);
        texts.addView(t);
        TextView s = PremiumUi.text(this, subtitle, 11, PremiumUi.MUTED, false);
        s.setPadding(0, PremiumUi.dp(this, 2), 0, 0);
        texts.addView(s);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, -2, 1f);
        tlp.setMargins(PremiumUi.dp(this, 12), 0, 0, 0);
        row.addView(texts, tlp);
        return row;
    }

    private void addBriefing(LinearLayout page) {
        TextView heading = PremiumUi.text(this, "Resumo do briefing", 18, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.setMargins(0, PremiumUi.dp(this, 22), 0, PremiumUi.dp(this, 10));
        page.addView(heading, hlp);

        LinearLayout card = PremiumUi.card(this);
        addInfo(card, "REGIÃO", section("REGIÃO"));
        addDivider(card);
        addInfo(card, "VEÍCULOS", section("VEÍCULOS"));
        addDivider(card);
        addInfo(card, "FORMATO", section("FORMATO"));
        addDivider(card);
        addInfo(card, "PERÍODO", section("PERÍODO"));
        addDivider(card);
        addInfo(card, "ATIVAÇÃO", section("ATIVAÇÃO"));
        page.addView(card);
    }

    private void addInfo(LinearLayout card, String label, String value) {
        TextView l = PremiumUi.text(this, label, 10, PremiumUi.MUTED, true);
        l.setLetterSpacing(0.06f);
        card.addView(l);
        TextView v = PremiumUi.text(this, TextUtils.isEmpty(value) ? "Não informado" : value, 13, PremiumUi.TEXT, false);
        v.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        v.setPadding(0, PremiumUi.dp(this, 5), 0, 0);
        card.addView(v);
    }

    private void addDivider(LinearLayout card) {
        View d = new View(this);
        d.setBackgroundColor(PremiumUi.BORDER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 1));
        lp.setMargins(0, PremiumUi.dp(this, 13), 0, PremiumUi.dp(this, 13));
        card.addView(d, lp);
    }

    private void addActions(LinearLayout page) {
        Button newCampaign = PremiumUi.primaryButton(this, "Criar nova campanha");
        newCampaign.setOnClickListener(v -> startActivity(new Intent(this, AdvertiserActivityPremium.class)));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50));
        nlp.setMargins(0, PremiumUi.dp(this, 20), 0, 0);
        page.addView(newCampaign, nlp);

        Button central = PremiumUi.secondaryButton(this, "Voltar para a Central");
        central.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 48));
        clp.setMargins(0, PremiumUi.dp(this, 9), 0, 0);
        page.addView(central, clp);

        TextView note = PremiumUi.text(this,
                "Os valores por veículo e por dia são divisões do orçamento informado, não uma proposta comercial final.",
                10, PremiumUi.MUTED, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(PremiumUi.dp(this, 10), PremiumUi.dp(this, 12), PremiumUi.dp(this, 10), 0);
        page.addView(note);
    }

    private String section(String name) {
        if (TextUtils.isEmpty(summary)) return "";
        String[] names = {"EMPRESA", "CAMPANHA", "REGIÃO", "VEÍCULOS", "FORMATO", "PERÍODO", "ORÇAMENTO", "ATIVAÇÃO"};
        int start = summary.indexOf(name + "\n");
        if (start < 0) return "";
        start += name.length() + 1;
        int end = summary.length();
        for (String other : names) {
            if (other.equals(name)) continue;
            int idx = summary.indexOf("\n\n" + other + "\n", start);
            if (idx >= 0 && idx < end) end = idx;
        }
        return summary.substring(start, end).trim();
    }

    private double extractMoney(String value) {
        if (TextUtils.isEmpty(value)) return 0;
        Matcher m = Pattern.compile("([0-9]+(?:[.,][0-9]{1,2})?)").matcher(value.replace(".", ""));
        if (!m.find()) return 0;
        try { return Double.parseDouble(m.group(1).replace(',', '.')); }
        catch (Exception ignored) { return 0; }
    }

    private int extractFirstInt(String value) {
        if (TextUtils.isEmpty(value)) return 0;
        Matcher m = Pattern.compile("\\d+").matcher(value);
        if (!m.find()) return 0;
        try { return Integer.parseInt(m.group()); } catch (Exception ignored) { return 0; }
    }

    private int campaignDays(String value) {
        if (TextUtils.isEmpty(value)) return 0;
        Matcher m = Pattern.compile("(\\d{2}/\\d{2}/\\d{4}).*?(\\d{2}/\\d{2}/\\d{4})").matcher(value);
        if (!m.find()) return 0;
        Date a = parseDate(m.group(1));
        Date b = parseDate(m.group(2));
        if (a == null || b == null || b.before(a)) return 0;
        long diff = b.getTime() - a.getTime();
        return (int) (diff / 86400000L) + 1;
    }

    private Date parseDate(String value) {
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", ptBr);
        f.setLenient(false);
        try { return f.parse(value); } catch (ParseException ignored) { return null; }
    }

    private String money(double value) {
        return String.format(ptBr, "R$ %.2f", value);
    }
}
