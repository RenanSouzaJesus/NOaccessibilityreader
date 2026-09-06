package com.noapp.accessreader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView statusText;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        resultText = findViewById(R.id.resultText);

        Button accessButton = findViewById(R.id.accessButton);
        Button refreshButton = findViewById(R.id.refreshButton);

        accessButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );

        refreshButton.setOnClickListener(v -> refreshData());
        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        statusText.setText(isAccessibilityEnabled()
                ? "Status: acessibilidade ATIVA"
                : "Status: acessibilidade DESATIVADA");

        SharedPreferences prefs = getSharedPreferences("no_accessibility", MODE_PRIVATE);
        boolean hasOffer = prefs.getBoolean("has_offer", false);

        if (!hasOffer) {
            String debugPkg = prefs.getString("debug_package", "");
            if (TextUtils.isEmpty(debugPkg)) {
                resultText.setText("Nenhuma corrida analisada ainda.\n\nAbra o Simulador de Corridas e toque em UberX ou 99Pop.");
            } else {
                resultText.setText("Nenhuma oferta válida encontrada ainda.\n\nÚltima tela observada: " + debugPkg);
            }
            return;
        }

        String pkg = prefs.getString("package", "");
        String content = prefs.getString("content", "");
        long time = prefs.getLong("time", 0L);
        String platform = prefs.getString("platform", "App de mobilidade");
        String category = prefs.getString("category", "Corrida");
        float price = prefs.getFloat("price", 0f);
        float pickupKm = prefs.getFloat("pickup_km", 0f);
        float tripKm = prefs.getFloat("trip_km", 0f);
        int tripMinutes = prefs.getInt("trip_minutes", 0);
        float totalKm = prefs.getFloat("total_km", 0f);
        float perKm = prefs.getFloat("gross_per_km", 0f);
        float perHour = prefs.getFloat("gross_per_hour", 0f);
        String rating = prefs.getString("rating", "");

        String when = time > 0
                ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date(time))
                : "-";

        String summary = String.format(Locale.getDefault(),
                "ÚLTIMA CORRIDA ANALISADA\n\n" +
                        "%s • %s\n" +
                        "Valor: R$ %.2f\n" +
                        "Até passageiro: %.1f km\n" +
                        "Viagem: %.1f km • %d min\n" +
                        "Total: %.1f km\n\n" +
                        "R$ %.2f/km\n" +
                        "R$ %.2f/h (tempo da viagem)\n" +
                        "Classificação: %s\n\n" +
                        "Pacote: %s\n" +
                        "Capturado em: %s\n\n" +
                        "--- CONTEÚDO ACESSÍVEL ---\n%s",
                platform, category, price, pickupKm, tripKm, tripMinutes, totalKm,
                perKm, perHour, rating, pkg, when, content);

        resultText.setText(summary);
    }

    private boolean isAccessibilityEnabled() {
        String expected = new ComponentName(this, ScreenAccessibilityService.class).flattenToString();

        String enabled = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (enabled == null) return false;

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);

        while (splitter.hasNext()) {
            if (expected.equalsIgnoreCase(splitter.next())) return true;
        }

        return false;
    }
}
