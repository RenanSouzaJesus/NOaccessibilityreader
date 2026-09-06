package com.noapp.accessreader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private final Locale ptBr = new Locale("pt", "BR");

    private TextView statusText;
    private TextView statusDetailText;
    private TextView platformChipText;
    private TextView lastAnalysisText;
    private TextView emptyOfferText;
    private LinearLayout offerContent;
    private TextView classificationBadge;
    private TextView priceText;
    private TextView routeSummaryText;
    private TextView perKmText;
    private TextView perHourText;
    private TextView pickupKmText;
    private TextView tripKmText;
    private TextView resultText;
    private LinearLayout debugPanel;
    private Button accessButton;
    private Button debugToggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        statusDetailText = findViewById(R.id.statusDetailText);
        platformChipText = findViewById(R.id.platformChipText);
        lastAnalysisText = findViewById(R.id.lastAnalysisText);
        emptyOfferText = findViewById(R.id.emptyOfferText);
        offerContent = findViewById(R.id.offerContent);
        classificationBadge = findViewById(R.id.classificationBadge);
        priceText = findViewById(R.id.priceText);
        routeSummaryText = findViewById(R.id.routeSummaryText);
        perKmText = findViewById(R.id.perKmText);
        perHourText = findViewById(R.id.perHourText);
        pickupKmText = findViewById(R.id.pickupKmText);
        tripKmText = findViewById(R.id.tripKmText);
        resultText = findViewById(R.id.resultText);
        debugPanel = findViewById(R.id.debugPanel);
        accessButton = findViewById(R.id.accessButton);
        debugToggleButton = findViewById(R.id.debugToggleButton);
        Button refreshButton = findViewById(R.id.refreshButton);

        accessButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );

        refreshButton.setOnClickListener(v -> refreshData());

        debugToggleButton.setOnClickListener(v -> {
            boolean opening = debugPanel.getVisibility() != View.VISIBLE;
            debugPanel.setVisibility(opening ? View.VISIBLE : View.GONE);
            debugToggleButton.setText(opening
                    ? "Ocultar diagnóstico técnico"
                    : "Mostrar diagnóstico técnico");
        });

        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        boolean accessibilityEnabled = isAccessibilityEnabled();
        updateAccessibilityStatus(accessibilityEnabled);

        SharedPreferences prefs = getSharedPreferences("no_accessibility", MODE_PRIVATE);
        boolean hasOffer = prefs.getBoolean("has_offer", false);

        if (!hasOffer) {
            showEmptyOffer(prefs);
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

        emptyOfferText.setVisibility(View.GONE);
        offerContent.setVisibility(View.VISIBLE);

        platformChipText.setText((platform + " • " + category).toUpperCase(ptBr));
        priceText.setText(money(price));
        routeSummaryText.setText(String.format(ptBr,
                "%.1f km total  •  %d min de viagem", totalKm, tripMinutes));
        perKmText.setText(String.format(ptBr, "R$ %.2f/km", perKm));
        perHourText.setText(String.format(ptBr, "R$ %.2f/h", perHour));
        pickupKmText.setText(String.format(ptBr, "%.1f km", pickupKm));
        tripKmText.setText(String.format(ptBr, "%.1f km", tripKm));
        applyClassification(rating);

        if (time > 0) {
            String when = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(time));
            lastAnalysisText.setText("às " + when);
        } else {
            lastAnalysisText.setText("");
        }

        String detailedWhen = time > 0
                ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date(time))
                : "-";

        resultText.setText(
                "Pacote: " + pkg +
                        "\nCapturado em: " + detailedWhen +
                        "\n\nPlataforma: " + platform +
                        "\nCategoria: " + category +
                        "\nValor: " + money(price) +
                        "\nColeta: " + String.format(ptBr, "%.1f km", pickupKm) +
                        "\nViagem: " + String.format(ptBr, "%.1f km / %d min", tripKm, tripMinutes) +
                        "\nTotal: " + String.format(ptBr, "%.1f km", totalKm) +
                        "\nRetorno/km: " + String.format(ptBr, "R$ %.2f", perKm) +
                        "\nRetorno/h: " + String.format(ptBr, "R$ %.2f", perHour) +
                        "\nClassificação interna: " + rating +
                        "\n\n--- CONTEÚDO ACESSÍVEL ---\n" + content
        );
    }

    private void updateAccessibilityStatus(boolean enabled) {
        if (enabled) {
            statusText.setText("ATIVO");
            statusText.setTextColor(getColor(R.color.no_cyan_soft));
            statusText.setBackgroundResource(R.drawable.bg_no_status_active);
            statusDetailText.setText("O NÓ está pronto para transformar campos visíveis e autorizados em dados comparáveis.");
            accessButton.setText("Configurar acessibilidade");
        } else {
            statusText.setText("DESATIVADO");
            statusText.setTextColor(getColor(R.color.no_orange));
            statusText.setBackgroundResource(R.drawable.bg_no_status_inactive);
            statusDetailText.setText("Ative o serviço para permitir a leitura dos campos exibidos na tela durante os testes.");
            accessButton.setText("Ativar acessibilidade");
        }
    }

    private void showEmptyOffer(SharedPreferences prefs) {
        offerContent.setVisibility(View.GONE);
        emptyOfferText.setVisibility(View.VISIBLE);
        platformChipText.setText("AGUARDANDO OFERTA");
        lastAnalysisText.setText("");

        String debugPkg = prefs.getString("debug_package", "");
        String debugContent = prefs.getString("debug_content", "");
        long debugTime = prefs.getLong("debug_time", 0L);

        if (TextUtils.isEmpty(debugPkg)) {
            emptyOfferText.setText("Nenhuma oportunidade analisada ainda. Abra o Simulador de Corridas e selecione UberX ou 99Pop.");
            resultText.setText("Nenhuma leitura capturada ainda.");
        } else {
            emptyOfferText.setText("O NÓ está lendo as telas, mas ainda não encontrou uma oferta completa com valor, distância e duração.");
            String when = debugTime > 0
                    ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date(debugTime))
                    : "-";
            resultText.setText("Última tela observada: " + debugPkg +
                    "\nCapturada em: " + when +
                    "\n\n" + debugContent);
        }
    }

    private void applyClassification(String rating) {
        String normalized = rating == null ? "" : rating.toUpperCase(ptBr);

        if (normalized.contains("BOA") || normalized.contains("EXCELENTE")) {
            classificationBadge.setText("OPORTUNIDADE ALTA");
            classificationBadge.setTextColor(getColor(R.color.no_cyan_soft));
            classificationBadge.setBackgroundResource(R.drawable.bg_no_badge_high);
            return;
        }

        if (normalized.contains("MÉDIA") || normalized.contains("MEDIA")) {
            classificationBadge.setText("OPORTUNIDADE MÉDIA");
            classificationBadge.setTextColor(getColor(R.color.no_orange));
            classificationBadge.setBackgroundResource(R.drawable.bg_no_badge_medium);
            return;
        }

        classificationBadge.setText("OPORTUNIDADE BAIXA");
        classificationBadge.setTextColor(getColor(R.color.no_low));
        classificationBadge.setBackgroundResource(R.drawable.bg_no_badge_low);
    }

    private String money(double value) {
        return String.format(ptBr, "R$ %.2f", value);
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
