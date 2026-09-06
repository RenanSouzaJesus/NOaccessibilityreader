package com.noapp.ridesimulator;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.util.Locale;

public class RideSimulatorActivity extends Activity {

    private final int navy = Color.rgb(7, 31, 52);
    private final int navyDeep = Color.rgb(4, 23, 39);
    private final int card = Color.rgb(12, 42, 67);
    private final int metric = Color.rgb(8, 34, 56);
    private final int cyan = Color.rgb(22, 199, 232);
    private final int cyanSoft = Color.rgb(142, 231, 244);
    private final int orange = Color.rgb(243, 154, 24);
    private final int white = Color.rgb(247, 250, 252);
    private final int muted = Color.rgb(173, 192, 206);
    private final int mutedDark = Color.rgb(127, 154, 174);
    private final int border = Color.rgb(26, 72, 100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(navyDeep);
        getWindow().setNavigationBarColor(navyDeep);
        showHome();
    }

    private void showHome() {
        LinearLayout root = baseLayout();
        addBrandHeader(root, "LAB • SIMULADOR");

        root.addView(sectionLabel("AMBIENTE DE TESTE"));
        root.addView(title("Teste o HUD do NÓ\nantes de uma corrida real", 29));
        root.addView(subtitle("As telas abaixo imitam ofertas de mobilidade para validar leitura, parser, cálculos e overlay."));

        LinearLayout uberCard = optionCard("UberX", "R$ 28,40", "3,1 km coleta • 6,2 km trajeto • 14 min", cyan);
        uberCard.setOnClickListener(v -> showOffer(true));
        uberCard.setContentDescription("Simular oferta UberX");
        root.addView(uberCard);

        LinearLayout popCard = optionCard("99Pop", "R$ 31,20", "2,4 km coleta • 7,0 km trajeto • 16 min", orange);
        popCard.setOnClickListener(v -> showOffer(false));
        popCard.setContentDescription("Simular oferta 99Pop");
        root.addView(popCard);

        TextView info = subtitle("Fluxo de teste: mantenha a acessibilidade do NÓ ativa, abra uma oferta e confirme se o HUD aparece por cima da tela. O HUD só fecha ao tocar em OK.");
        info.setPadding(0, dp(18), 0, 0);
        root.addView(info);

        setContentView(wrap(root));
    }

    private void showOffer(boolean uber) {
        LinearLayout root = baseLayout();
        addBrandHeader(root, "LAB • OFERTA SIMULADA");
        root.addView(secondaryButton("← Voltar ao simulador", v -> showHome()));

        String platform = uber ? "Uber" : "99";
        String category = uber ? "UberX" : "99Pop";
        String price = uber ? "28,40" : "31,20";
        String pickupKm = uber ? "3,1" : "2,4";
        String tripKm = uber ? "6,2" : "7,0";
        String tripMin = uber ? "14" : "16";
        String pickupAddress = uber ? "Rua das Flores, 123 - Centro" : "Av. Brasil, 456 - Centro";
        String destination = uber ? "Shopping Sul" : "Mercado Municipal";

        root.addView(sectionLabel("SIMULAÇÃO • " + platform.toUpperCase(Locale.ROOT)));
        root.addView(title("Oferta " + category, 27));

        LinearLayout offer = cardLayout();

        LinearLayout offerHeader = new LinearLayout(this);
        offerHeader.setOrientation(LinearLayout.HORIZONTAL);
        offerHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView platformText = title(platform, 30);
        offerHeader.addView(platformText);
        offerHeader.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));
        TextView categoryChip = chip(category, uber ? cyan : orange);
        offerHeader.addView(categoryChip);
        offer.addView(offerHeader);

        TextView priceView = title("R$ " + price, 42);
        priceView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        priceView.setPadding(0, dp(12), 0, dp(2));
        offer.addView(priceView);

        TextView rating = line(uber ? "★ 4,85 (1200+ viagens)" : "★ 4,78 (890+ viagens)");
        rating.setContentDescription("Avaliação do passageiro");
        offer.addView(rating);

        addAccentDivider(offer);

        TextView pickup = line(pickupKm + " km até o passageiro");
        pickup.setContentDescription("Distância até o passageiro: " + pickupKm + " quilômetros");
        offer.addView(pickup);
        offer.addView(smallLine(pickupAddress));

        TextView trip = line(tripKm + " km • " + tripMin + " min de viagem");
        trip.setContentDescription("Viagem: " + tripKm + " quilômetros e " + tripMin + " minutos");
        offer.addView(trip);
        offer.addView(smallLine("Destino: " + destination));

        Button accept = primaryButton(uber ? "Aceitar" : "Aceitar corrida", v -> { });
        accept.setContentDescription("Botão simulado de aceitar corrida");
        offer.addView(accept);

        TextView disclaimer = smallLine("Tela simulada para testes. Nenhuma corrida real será aceita.");
        disclaimer.setPadding(0, dp(10), 0, 0);
        offer.addView(disclaimer);
        root.addView(offer);

        root.addView(sectionLabel("AJUSTE MANUAL"));
        root.addView(title("Crie outros cenários", 23));
        root.addView(subtitle("Altere os números para conferir se o NÓ atualiza o cálculo e abre uma nova análise."));

        root.addView(inputLabel("VALOR EM R$"));
        EditText priceInput = numberInput("Ex.: 31,20", price);
        root.addView(priceInput);

        root.addView(inputLabel("KM ATÉ O PASSAGEIRO"));
        EditText pickupInput = numberInput("Ex.: 2,4", pickupKm);
        root.addView(pickupInput);

        root.addView(inputLabel("KM DA VIAGEM"));
        EditText tripInput = numberInput("Ex.: 7,0", tripKm);
        root.addView(tripInput);

        root.addView(inputLabel("MINUTOS DA VIAGEM"));
        EditText minInput = numberInput("Ex.: 16", tripMin);
        root.addView(minInput);

        Button update = primaryButton("Atualizar oferta simulada", v -> {
            String p = valueOr(priceInput, price);
            String pk = valueOr(pickupInput, pickupKm);
            String tk = valueOr(tripInput, tripKm);
            String tm = valueOr(minInput, tripMin);
            showCustomOffer(uber, p, pk, tk, tm);
        });
        root.addView(update);

        setContentView(wrap(root));
    }

    private void showCustomOffer(boolean uber, String price, String pickupKm, String tripKm, String tripMin) {
        LinearLayout root = baseLayout();
        addBrandHeader(root, "LAB • CENÁRIO PERSONALIZADO");
        root.addView(secondaryButton("← Voltar ao início", v -> showHome()));

        String platform = uber ? "Uber" : "99";
        String category = uber ? "UberX" : "99Pop";

        root.addView(sectionLabel("OFERTA ATUALIZADA"));
        root.addView(title(platform + " • " + category, 27));

        LinearLayout offer = cardLayout();
        offer.addView(chip(platform + " • " + category, uber ? cyan : orange));
        TextView priceView = title("R$ " + price, 42);
        priceView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        priceView.setPadding(0, dp(12), 0, dp(2));
        offer.addView(priceView);
        offer.addView(line(pickupKm + " km até o passageiro"));
        offer.addView(line(tripKm + " km • " + tripMin + " min de viagem"));

        double totalKm = parseNumber(pickupKm) + parseNumber(tripKm);
        double amount = parseNumber(price);
        if (totalKm > 0) {
            addAccentDivider(offer);
            LinearLayout metrics = new LinearLayout(this);
            metrics.setOrientation(LinearLayout.HORIZONTAL);
            TextView total = metricValue("TOTAL", String.format(Locale.getDefault(), "%.1f km", totalKm));
            TextView perKm = metricValue("R$/KM", String.format(Locale.getDefault(), "R$ %.2f", amount / totalKm));
            LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, -2, 1f);
            a.setMargins(0, 0, dp(5), 0);
            metrics.addView(total, a);
            LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(0, -2, 1f);
            b.setMargins(dp(5), 0, 0, 0);
            metrics.addView(perKm, b);
            offer.addView(metrics);
        }

        offer.addView(smallLine("Dados alterados manualmente para validar uma nova chave de oferta no leitor."));
        root.addView(offer);
        root.addView(primaryButton("Editar novamente", v -> showOffer(uber)));
        setContentView(wrap(root));
    }

    private void addBrandHeader(LinearLayout root, String label) {
        LinearLayout logoRow = new LinearLayout(this);
        logoRow.setOrientation(LinearLayout.HORIZONTAL);
        logoRow.setGravity(Gravity.BOTTOM);
        TextView logo = title("NÓ", 39);
        logo.setPadding(0, 0, 0, 0);
        logoRow.addView(logo);
        TextView dot = title(".", 39);
        dot.setTextColor(cyan);
        dot.setPadding(0, 0, 0, 0);
        logoRow.addView(dot);
        logoRow.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));
        TextView lab = chip(label, cyan);
        logoRow.addView(lab);
        root.addView(logoRow);
        addAccentDivider(root);
    }

    private LinearLayout optionCard(String name, String price, String detail, int accentColor) {
        LinearLayout box = cardLayout();
        box.setClickable(true);
        box.setFocusable(true);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(title(name, 26));
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));
        top.addView(chip("SIMULAR", accentColor));
        box.addView(top);

        TextView p = title(price, 30);
        p.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        box.addView(p);
        box.addView(smallLine(detail));
        return box;
    }

    private TextView metricValue(String label, String value) {
        TextView tv = new TextView(this);
        tv.setText(label + "\n" + value);
        tv.setTextColor(white);
        tv.setTextSize(15);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setLineSpacing(dp(4), 1f);
        tv.setPadding(dp(12), dp(10), dp(12), dp(10));
        tv.setBackground(roundRect(metric, dp(13), border, dp(1)));
        return tv;
    }

    private ScrollView wrap(LinearLayout root) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(navy);
        scroll.setFillViewport(true);
        scroll.addView(root);
        return scroll;
    }

    private LinearLayout baseLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(34));
        root.setBackgroundColor(navy);
        return root;
    }

    private LinearLayout cardLayout() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(16), dp(18), dp(18));
        box.setBackground(roundRect(card, dp(20), border, dp(1)));
        box.setElevation(dp(3));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(14), 0, dp(8));
        box.setLayoutParams(lp);
        return box;
    }

    private TextView title(String value, int size) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(white);
        tv.setTextSize(size);
        tv.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        tv.setPadding(0, dp(5), 0, dp(3));
        tv.setIncludeFontPadding(false);
        return tv;
    }

    private TextView sectionLabel(String value) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(cyan);
        tv.setTextSize(11);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setLetterSpacing(0.10f);
        tv.setPadding(0, dp(20), 0, dp(3));
        return tv;
    }

    private TextView subtitle(String value) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(muted);
        tv.setTextSize(14);
        tv.setLineSpacing(dp(2), 1f);
        tv.setPadding(0, dp(3), 0, dp(8));
        return tv;
    }

    private TextView line(String value) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(white);
        tv.setTextSize(17);
        tv.setPadding(0, dp(9), 0, dp(2));
        return tv;
    }

    private TextView smallLine(String value) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(muted);
        tv.setTextSize(13);
        tv.setPadding(0, dp(2), 0, dp(7));
        return tv;
    }

    private TextView chip(String value, int accentColor) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(accentColor);
        tv.setTextSize(10);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(10), dp(6), dp(10), dp(6));
        tv.setBackground(roundRect(metric, dp(999), accentColor, dp(1)));
        return tv;
    }

    private Button primaryButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(navyDeep);
        button.setTextSize(15);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        button.setBackground(roundRect(cyan, dp(14), Color.TRANSPARENT, 0));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
        lp.setMargins(0, dp(12), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private Button secondaryButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(white);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        button.setBackground(roundRect(card, dp(14), border, dp(1)));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
        lp.setMargins(0, dp(8), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private TextView inputLabel(String label) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(mutedDark);
        tv.setTextSize(10);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setLetterSpacing(0.08f);
        tv.setPadding(0, dp(10), 0, dp(4));
        return tv;
    }

    private EditText numberInput(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(mutedDark);
        input.setText(value);
        input.setTextColor(white);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setPadding(dp(14), 0, dp(14), 0);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setContentDescription(hint);
        input.setBackground(roundRect(metric, dp(13), border, dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(50));
        input.setLayoutParams(lp);
        return input;
    }

    private void addAccentDivider(LinearLayout parent) {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(3));
        lp.setMargins(0, dp(14), 0, dp(12));
        parent.addView(line, lp);

        View c = new View(this);
        c.setBackgroundColor(cyan);
        line.addView(c, new LinearLayout.LayoutParams(0, dp(3), 3f));

        View o = new View(this);
        o.setBackgroundColor(orange);
        line.addView(o, new LinearLayout.LayoutParams(0, dp(3), 1f));
    }

    private GradientDrawable roundRect(int fill, int radiusPx, int stroke, int strokePx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(radiusPx);
        if (strokePx > 0) d.setStroke(strokePx, stroke);
        return d;
    }

    private String valueOr(EditText input, String fallback) {
        String s = input.getText().toString().trim();
        return s.isEmpty() ? fallback : s;
    }

    private double parseNumber(String value) {
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
