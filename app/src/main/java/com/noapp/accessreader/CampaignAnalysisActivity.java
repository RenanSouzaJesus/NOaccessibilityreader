package com.noapp.accessreader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tela de revisão e análise do último briefing criado no NÓ Mídia.
 *
 * As métricas mostradas aqui são derivadas exclusivamente dos dados informados
 * pelo anunciante. Elas não representam preço final, alcance garantido ou
 * disponibilidade real de inventário.
 */
public class CampaignAnalysisActivity extends Activity {

    private static final String PREFS = "no_media";
    private final Locale ptBr = new Locale("pt", "BR");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
    }

    private void buildScreen() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String requestId = prefs.getString("last_request_id", "");
        String status = prefs.getString("last_request_status", "");
        String campaign = prefs.getString("last_request_campaign", "");
        String company = prefs.getString("last_request_company", "");
        String summary = prefs.getString("last_request_summary", "");

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setBackgroundColor(getColor(R.color.no_navy));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(14), dp(16), dp(28));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(toolbar, new LinearLayout.LayoutParams(-1, -2));

        TextView back = text("‹  Central", 13, getColor(R.color.no_text_secondary), true);
        back.setPadding(0, dp(8), dp(12), dp(8));
        back.setOnClickListener(v -> finish());
        toolbar.addView(back);
        toolbar.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));

        TextView mediaBadge = text("NÓ MÍDIA", 10, getColor(R.color.no_orange), true);
        mediaBadge.setLetterSpacing(0.08f);
        mediaBadge.setPadding(dp(10), dp(6), dp(10), dp(6));
        mediaBadge.setBackgroundResource(R.drawable.bg_no_chip);
        toolbar.addView(mediaBadge);

        LinearLayout logoRow = new LinearLayout(this);
        logoRow.setOrientation(LinearLayout.HORIZONTAL);
        logoRow.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(-1, -2);
        logoLp.setMargins(0, dp(12), 0, 0);
        page.addView(logoRow, logoLp);

        TextView logo = text("NÓ", 32, getColor(R.color.no_white), true);
        logo.setIncludeFontPadding(false);
        logoRow.addView(logo);
        TextView dot = text(".", 32, getColor(R.color.no_cyan), true);
        dot.setIncludeFontPadding(false);
        logoRow.addView(dot);

        TextView heading = text("Análise da campanha", 25, getColor(R.color.no_white), true);
        heading.setPadding(0, dp(6), 0, 0);
        page.addView(heading);

        TextView intro = text(
                "Revise o briefing criado e veja a distribuição do orçamento antes da consulta de inventário.",
                13,
                getColor(R.color.no_text_secondary),
                false
        );
        intro.setPadding(0, dp(5), 0, 0);
        page.addView(intro);

        addAccent(page);

        if (TextUtils.isEmpty(requestId) || TextUtils.isEmpty(summary)) {
            buildEmptyState(page);
            setContentView(scroll);
            return;
        }

        CampaignData data = CampaignData.from(summary, company, campaign, requestId, status, ptBr);
        buildStatusCard(page, data);
        buildMetrics(page, data);
        buildBriefingCard(page, data);
        buildInterpretationCard(page, data);
        buildActions(page);

        setContentView(scroll);
    }

    private void buildEmptyState(LinearLayout page) {
        LinearLayout card = card();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(18), 0, 0);
        page.addView(card, lp);

        TextView title = text("Nenhuma campanha criada", 18, getColor(R.color.no_white), true);
        card.addView(title);

        TextView body = text(
                "Crie um briefing no NÓ Mídia para poder revisar e analisar a solicitação aqui.",
                12,
                getColor(R.color.no_text_secondary),
                false
        );
        body.setPadding(0, dp(6), 0, 0);
        card.addView(body);

        Button create = primaryButton("Criar campanha");
        create.setOnClickListener(v -> startActivity(new Intent(this, AdvertiserActivity.class)));
        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(-1, dp(48));
        buttonLp.setMargins(0, dp(14), 0, 0);
        card.addView(create, buttonLp);
    }

    private void buildStatusCard(LinearLayout page, CampaignData data) {
        LinearLayout card = card();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(18), 0, 0);
        page.addView(card, lp);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top);

        TextView label = text("CAMPANHA", 10, getColor(R.color.no_cyan), true);
        label.setLetterSpacing(0.08f);
        top.addView(label);
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));

        TextView status = text(data.status.isEmpty() ? "BRIEFING SALVO" : data.status,
                9, getColor(R.color.no_cyan_soft), true);
        status.setPadding(dp(8), dp(5), dp(8), dp(5));
        status.setBackgroundResource(R.drawable.bg_no_status_active);
        top.addView(status);

        TextView title = text(data.campaign.isEmpty() ? "Campanha sem nome" : data.campaign,
                21, getColor(R.color.no_white), true);
        title.setPadding(0, dp(10), 0, 0);
        card.addView(title);

        String companyLine = data.company.isEmpty() ? data.requestId : data.company + "  •  " + data.requestId;
        TextView meta = text(companyLine, 11, getColor(R.color.no_text_secondary), false);
        meta.setPadding(0, dp(4), 0, 0);
        card.addView(meta);
    }

    private void buildMetrics(LinearLayout page, CampaignData data) {
        TextView label = sectionTitle("ANÁLISE DO BRIEFING");
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(-1, -2);
        labelLp.setMargins(0, dp(20), 0, dp(8));
        page.addView(label, labelLp);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        page.addView(row1, new LinearLayout.LayoutParams(-1, -2));
        row1.addView(metricBox("ORÇAMENTO", data.budget > 0 ? money(data.budget) : "—"), metricLp(0, 5));
        row1.addView(metricBox("VEÍCULOS", data.quantity > 0 ? String.valueOf(data.quantity) : "—"), metricLp(5, 0));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams row2Lp = new LinearLayout.LayoutParams(-1, -2);
        row2Lp.setMargins(0, dp(10), 0, 0);
        page.addView(row2, row2Lp);
        row2.addView(metricBox("DURAÇÃO", data.days > 0 ? data.days + " dias" : "—"), metricLp(0, 5));
        row2.addView(metricBox("R$/VEÍCULO/DIA", data.budgetPerVehicleDay > 0
                ? money(data.budgetPerVehicleDay)
                : "—"), metricLp(5, 0));

        if (data.budgetPerVehicle > 0) {
            TextView note = text(
                    "Divisão do briefing: " + money(data.budgetPerVehicle) + " por veículo no período completo.",
                    11,
                    getColor(R.color.no_text_muted),
                    false
            );
            note.setPadding(0, dp(8), 0, 0);
            page.addView(note);
        }
    }

    private void buildBriefingCard(LinearLayout page, CampaignData data) {
        TextView label = sectionTitle("O QUE FOI SOLICITADO");
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(-1, -2);
        labelLp.setMargins(0, dp(20), 0, dp(8));
        page.addView(label, labelLp);

        LinearLayout card = card();
        page.addView(card, new LinearLayout.LayoutParams(-1, -2));

        addInfo(card, "REGIÃO", data.region);
        addInfo(card, "VEÍCULOS", data.vehicles);
        addInfo(card, "FORMATO", data.format);
        addInfo(card, "PERÍODO", data.period);
        addInfo(card, "ATIVAÇÃO", data.activation);
    }

    private void buildInterpretationCard(LinearLayout page, CampaignData data) {
        TextView label = sectionTitle("LEITURA DO NÓ");
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(-1, -2);
        labelLp.setMargins(0, dp(20), 0, dp(8));
        page.addView(label, labelLp);

        LinearLayout card = card();
        page.addView(card, new LinearLayout.LayoutParams(-1, -2));

        TextView ready = text(data.isComplete ? "✓ Briefing pronto para consulta de inventário" : "Briefing precisa de revisão",
                15,
                data.isComplete ? getColor(R.color.no_cyan_soft) : getColor(R.color.no_orange),
                true);
        card.addView(ready);

        StringBuilder interpretation = new StringBuilder();
        if (data.quantity > 0) {
            interpretation.append("• A solicitação pede ").append(data.quantity).append(" veículo(s) compatível(is).\n");
        }
        if (data.days > 0) {
            interpretation.append("• Período calculado em ").append(data.days).append(" dia(s), incluindo início e fim.\n");
        }
        if (data.budgetPerVehicleDay > 0) {
            interpretation.append("• A divisão matemática do orçamento equivale a ")
                    .append(money(data.budgetPerVehicleDay)).append(" por veículo/dia.\n");
        }
        if (!data.activation.isEmpty()) {
            interpretation.append("• Mensuração escolhida: ").append(data.activation).append(".\n");
        }
        interpretation.append("• Disponibilidade, alcance e preço final dependem do inventário real e ainda não são estimados nesta versão.");

        TextView body = text(interpretation.toString(), 12, getColor(R.color.no_text_secondary), false);
        body.setLineSpacing(dp(2), 1f);
        body.setPadding(0, dp(8), 0, 0);
        card.addView(body);
    }

    private void buildActions(LinearLayout page) {
        Button newCampaign = secondaryButton("Criar nova campanha");
        newCampaign.setOnClickListener(v -> startActivity(new Intent(this, AdvertiserActivity.class)));
        LinearLayout.LayoutParams newLp = new LinearLayout.LayoutParams(-1, dp(48));
        newLp.setMargins(0, dp(18), 0, 0);
        page.addView(newCampaign, newLp);

        Button central = primaryButton("Voltar para a Central");
        central.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams centralLp = new LinearLayout.LayoutParams(-1, dp(48));
        centralLp.setMargins(0, dp(10), 0, 0);
        page.addView(central, centralLp);
    }

    private void addInfo(LinearLayout card, String label, String value) {
        if (TextUtils.isEmpty(value)) return;
        TextView l = text(label, 9, getColor(R.color.no_cyan), true);
        l.setLetterSpacing(0.07f);
        if (card.getChildCount() > 0) l.setPadding(0, dp(12), 0, 0);
        card.addView(l);

        TextView v = text(value, 13, getColor(R.color.no_white), false);
        v.setPadding(0, dp(4), 0, 0);
        card.addView(v);
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundResource(R.drawable.bg_no_card);
        return card;
    }

    private LinearLayout metricBox(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(11), dp(12), dp(11));
        box.setBackgroundResource(R.drawable.bg_no_metric);

        TextView l = text(label, 9, getColor(R.color.no_text_muted), true);
        l.setLetterSpacing(0.05f);
        box.addView(l);

        TextView v = text(value, 17, getColor(R.color.no_white), true);
        v.setPadding(0, dp(5), 0, 0);
        box.addView(v);
        return box;
    }

    private LinearLayout.LayoutParams metricLp(int left, int right) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(dp(left), 0, dp(right), 0);
        return lp;
    }

    private TextView sectionTitle(String value) {
        TextView label = text(value, 10, getColor(R.color.no_cyan), true);
        label.setLetterSpacing(0.08f);
        return label;
    }

    private void addAccent(LinearLayout page) {
        LinearLayout accent = new LinearLayout(this);
        accent.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(-1, dp(3));
        accentLp.setMargins(0, dp(14), 0, 0);
        page.addView(accent, accentLp);

        View cyan = new View(this);
        cyan.setBackgroundColor(getColor(R.color.no_cyan));
        accent.addView(cyan, new LinearLayout.LayoutParams(0, -1, 3f));

        View orange = new View(this);
        orange.setBackgroundColor(getColor(R.color.no_orange));
        accent.addView(orange, new LinearLayout.LayoutParams(0, -1, 1f));
    }

    private Button primaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(getColor(R.color.no_navy_deep));
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackgroundResource(R.drawable.bg_no_button_primary);
        button.setStateListAnimator(null);
        return button;
    }

    private Button secondaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(getColor(R.color.no_white));
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackgroundResource(R.drawable.bg_no_button_secondary);
        button.setStateListAnimator(null);
        return button;
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

    private String money(double value) {
        return String.format(ptBr, "R$ %.2f", value);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class CampaignData {
        String requestId = "";
        String status = "";
        String company = "";
        String campaign = "";
        String region = "";
        String vehicles = "";
        String format = "";
        String period = "";
        String activation = "";
        int quantity = 0;
        int days = 0;
        double budget = 0;
        double budgetPerVehicle = 0;
        double budgetPerVehicleDay = 0;
        boolean isComplete = false;

        static CampaignData from(String summary, String company, String campaign,
                                 String requestId, String status, Locale locale) {
            CampaignData data = new CampaignData();
            data.company = company == null ? "" : company;
            data.campaign = campaign == null ? "" : campaign;
            data.requestId = requestId == null ? "" : requestId;
            data.status = status == null ? "" : status;

            data.region = section(summary, "REGIÃO", "VEÍCULOS");
            data.vehicles = section(summary, "VEÍCULOS", "FORMATO");
            data.format = firstLine(section(summary, "FORMATO", "PERÍODO"));
            data.period = firstLine(section(summary, "PERÍODO", "ORÇAMENTO"));
            data.activation = firstLine(section(summary, "ATIVAÇÃO", null));

            Matcher qty = Pattern.compile("(?m)^(\\d+)\\s*[×x]\\s*(.+)$").matcher(data.vehicles);
            if (qty.find()) {
                data.quantity = safeInt(qty.group(1));
            }

            String budgetSection = section(summary, "ORÇAMENTO", "ATIVAÇÃO");
            Matcher budgetMatcher = Pattern.compile("R\\$\\s*([0-9.]+(?:,[0-9]{1,2})?)").matcher(budgetSection);
            if (budgetMatcher.find()) {
                data.budget = safeMoney(budgetMatcher.group(1));
            }

            Matcher periodMatcher = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s*→\\s*(\\d{2}/\\d{2}/\\d{4})").matcher(data.period);
            if (periodMatcher.find()) {
                data.days = inclusiveDays(periodMatcher.group(1), periodMatcher.group(2), locale);
            }

            if (data.budget > 0 && data.quantity > 0) {
                data.budgetPerVehicle = data.budget / data.quantity;
            }
            if (data.budgetPerVehicle > 0 && data.days > 0) {
                data.budgetPerVehicleDay = data.budgetPerVehicle / data.days;
            }

            data.isComplete = !TextUtils.isEmpty(data.campaign)
                    && !TextUtils.isEmpty(data.region)
                    && !TextUtils.isEmpty(data.vehicles)
                    && !TextUtils.isEmpty(data.format)
                    && !TextUtils.isEmpty(data.period)
                    && data.budget > 0;
            return data;
        }

        private static String section(String summary, String start, String end) {
            if (summary == null) return "";
            String marker = start + "\n";
            int from = summary.indexOf(marker);
            if (from < 0) return "";
            from += marker.length();
            int to = end == null ? summary.length() : summary.indexOf("\n\n" + end + "\n", from);
            if (to < 0) to = summary.length();
            return summary.substring(from, to).trim();
        }

        private static String firstLine(String value) {
            if (value == null) return "";
            int newline = value.indexOf('\n');
            return (newline >= 0 ? value.substring(0, newline) : value).trim();
        }

        private static int safeInt(String value) {
            try {
                return Integer.parseInt(value.trim());
            } catch (Exception ignored) {
                return 0;
            }
        }

        private static double safeMoney(String value) {
            try {
                String normalized = value.replace(".", "").replace(',', '.');
                return Double.parseDouble(normalized);
            } catch (Exception ignored) {
                return 0;
            }
        }

        private static int inclusiveDays(String start, String end, Locale locale) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", locale);
            format.setLenient(false);
            try {
                Date startDate = format.parse(start);
                Date endDate = format.parse(end);
                if (startDate == null || endDate == null || endDate.before(startDate)) return 0;
                long delta = endDate.getTime() - startDate.getTime();
                return (int) TimeUnit.MILLISECONDS.toDays(delta) + 1;
            } catch (ParseException ignored) {
                return 0;
            }
        }
    }
}
