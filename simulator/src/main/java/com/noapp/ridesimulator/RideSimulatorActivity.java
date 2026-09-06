package com.noapp.ridesimulator;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class RideSimulatorActivity extends Activity {

    private final int bg = Color.rgb(13, 18, 24);
    private final int card = Color.rgb(24, 31, 39);
    private final int text = Color.rgb(245, 247, 250);
    private final int muted = Color.rgb(170, 180, 190);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(bg);
        showHome();
    }

    private void showHome() {
        LinearLayout root = baseLayout();
        root.addView(title("Simulador de Corridas", 30));
        root.addView(subtitle("App separado para testar a leitura do NO via AccessibilityService."));

        root.addView(actionButton("Simular oferta UberX", v -> showOffer(true)));
        root.addView(actionButton("Simular oferta 99Pop", v -> showOffer(false)));

        TextView info = subtitle("Use assim: deixe a acessibilidade do NO ativada, abra uma oferta aqui e depois volte ao NO para conferir o que foi capturado.");
        info.setPadding(0, dp(24), 0, 0);
        root.addView(info);
        setContentView(wrap(root));
    }

    private void showOffer(boolean uber) {
        LinearLayout root = baseLayout();

        Button back = actionButton("← Voltar", v -> showHome());
        root.addView(back);

        String platform = uber ? "Uber" : "99";
        String category = uber ? "UberX" : "99Pop";
        String price = uber ? "28,40" : "31,20";
        String pickupKm = uber ? "3,1" : "2,4";
        String tripKm = uber ? "6,2" : "7,0";
        String tripMin = uber ? "14" : "16";
        String pickupAddress = uber ? "Rua das Flores, 123 - Centro" : "Av. Brasil, 456 - Centro";
        String destination = uber ? "Shopping Sul" : "Mercado Municipal";

        root.addView(title("Simulação - " + platform, 27));

        LinearLayout offer = cardLayout();
        offer.addView(title(platform, 34));
        offer.addView(title(category, 22));
        offer.addView(title("R$ " + price, 40));

        TextView rating = line(uber ? "★ 4,85 (1200+ viagens)" : "★ 4,78 (890+ viagens)");
        rating.setContentDescription("Avaliação do passageiro");
        offer.addView(rating);

        TextView pickup = line(pickupKm + " km até o passageiro");
        pickup.setContentDescription("Distância até o passageiro: " + pickupKm + " quilômetros");
        offer.addView(pickup);
        offer.addView(smallLine(pickupAddress));

        TextView trip = line(tripKm + " km • " + tripMin + " min de viagem");
        trip.setContentDescription("Viagem: " + tripKm + " quilômetros e " + tripMin + " minutos");
        offer.addView(trip);
        offer.addView(smallLine("Destino: " + destination));

        Button accept = actionButton(uber ? "Aceitar" : "Aceitar corrida", v -> { });
        accept.setContentDescription("Botão simulado de aceitar corrida");
        offer.addView(accept);
        offer.addView(smallLine("Esta é uma tela simulada para testes. Nenhuma corrida real será aceita."));
        root.addView(offer);

        root.addView(title("Alterar dados da simulação", 21));
        EditText priceInput = numberInput("Valor em R$", price);
        EditText pickupInput = numberInput("Km até o passageiro", pickupKm);
        EditText tripInput = numberInput("Km da viagem", tripKm);
        EditText minInput = numberInput("Minutos da viagem", tripMin);
        root.addView(priceInput);
        root.addView(pickupInput);
        root.addView(tripInput);
        root.addView(minInput);

        Button update = actionButton("Atualizar oferta", v -> {
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
        root.addView(actionButton("← Voltar", v -> showHome()));

        String platform = uber ? "Uber" : "99";
        String category = uber ? "UberX" : "99Pop";
        root.addView(title("Simulação - " + platform, 27));

        LinearLayout offer = cardLayout();
        offer.addView(title(platform, 34));
        offer.addView(title(category, 22));
        offer.addView(title("R$ " + price, 40));
        offer.addView(line(pickupKm + " km até o passageiro"));
        offer.addView(line(tripKm + " km • " + tripMin + " min de viagem"));
        offer.addView(smallLine("Dados alterados manualmente para teste do leitor."));

        double totalKm = parseNumber(pickupKm) + parseNumber(tripKm);
        double amount = parseNumber(price);
        if (totalKm > 0) {
            offer.addView(line(String.format(Locale.getDefault(), "Total: %.1f km", totalKm)));
            offer.addView(line(String.format(Locale.getDefault(), "R$ %.2f/km", amount / totalKm)));
        }
        root.addView(offer);
        root.addView(actionButton("Editar novamente", v -> showOffer(uber)));
        setContentView(wrap(root));
    }

    private ScrollView wrap(LinearLayout root) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);
        scroll.addView(root);
        return scroll;
    }

    private LinearLayout baseLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(32));
        root.setBackgroundColor(bg);
        return root;
    }

    private LinearLayout cardLayout() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(20), dp(20), dp(20));
        box.setBackgroundColor(card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(18), 0, dp(22));
        box.setLayoutParams(lp);
        return box;
    }

    private TextView title(String value, int size) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(text);
        tv.setTextSize(size);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setPadding(0, dp(8), 0, dp(4));
        return tv;
    }

    private TextView subtitle(String value) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(muted);
        tv.setTextSize(16);
        tv.setPadding(0, dp(4), 0, dp(10));
        return tv;
    }

    private TextView line(String value) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(text);
        tv.setTextSize(19);
        tv.setPadding(0, dp(11), 0, dp(3));
        return tv;
    }

    private TextView smallLine(String value) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(muted);
        tv.setTextSize(15);
        tv.setPadding(0, dp(2), 0, dp(8));
        return tv;
    }

    private Button actionButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(58));
        lp.setMargins(0, dp(12), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private EditText numberInput(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(muted);
        input.setText(value);
        input.setTextColor(text);
        input.setTextSize(17);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setContentDescription(hint);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(8), 0, 0);
        input.setLayoutParams(lp);
        return input;
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
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
