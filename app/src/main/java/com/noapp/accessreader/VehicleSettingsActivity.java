package com.noapp.accessreader;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/** Cadastro simples do veículo e dos custos usados nas estimativas de lucro. */
public class VehicleSettingsActivity extends Activity {

    private final Locale ptBr = new Locale("pt", "BR");

    private EditText name;
    private EditText fuelType;
    private EditText fuelPrice;
    private EditText consumption;
    private EditText maintenance;
    private EditText depreciation;
    private EditText fixedMonthly;
    private EditText monthlyKm;
    private LinearLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumUi.lightSystemBars(this);
        build();
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(android.view.View.OVER_SCROLL_NEVER);
        scroll.setBackgroundColor(PremiumUi.BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(PremiumUi.dp(this, 16), PremiumUi.dp(this, 14), PremiumUi.dp(this, 16), PremiumUi.dp(this, 28));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = PremiumUi.text(this, "‹  Voltar", 13, PremiumUi.NAVY, true);
        back.setPadding(0, PremiumUi.dp(this, 8), PremiumUi.dp(this, 8), PremiumUi.dp(this, 8));
        back.setOnClickListener(v -> finish());
        toolbar.addView(back);
        toolbar.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        toolbar.addView(PremiumUi.chip(this, "MEU VEÍCULO", PremiumUi.NAVY, PremiumUi.CYAN_SOFT));
        page.addView(toolbar);

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(-1, -2);
        brandLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        page.addView(brand, brandLp);
        brand.addView(PremiumUi.text(this, "NÓ", 28, PremiumUi.NAVY, true));
        brand.addView(PremiumUi.text(this, ".", 28, PremiumUi.CYAN, true));

        TextView title = PremiumUi.text(this, "Quanto custa rodar?", 25, PremiumUi.TEXT, true);
        title.setPadding(0, PremiumUi.dp(this, 10), 0, 0);
        page.addView(title);

        TextView sub = PremiumUi.text(this,
                "Cadastre os custos do veículo uma vez. O NÓ passa a estimar custo, lucro e margem das viagens.",
                12, PremiumUi.MUTED, false);
        sub.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        sub.setPadding(0, PremiumUi.dp(this, 5), 0, PremiumUi.dp(this, 16));
        page.addView(sub);

        LinearLayout identity = PremiumUi.card(this);
        identity.addView(sectionTitle("Veículo"));
        name = field("Ex.: HB20 1.0", InputType.TYPE_CLASS_TEXT);
        addField(identity, "Nome ou apelido", name);
        fuelType = field("Ex.: Gasolina", InputType.TYPE_CLASS_TEXT);
        addField(identity, "Combustível", fuelType);
        page.addView(identity);

        LinearLayout variable = PremiumUi.card(this);
        LinearLayout.LayoutParams variableLp = new LinearLayout.LayoutParams(-1, -2);
        variableLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        page.addView(variable, variableLp);
        variable.addView(sectionTitle("Custos por uso"));
        fuelPrice = numberField("Ex.: 6,00");
        addField(variable, "Preço do combustível (R$/litro)", fuelPrice);
        consumption = numberField("Ex.: 10,0");
        addField(variable, "Consumo médio (km/l)", consumption);
        maintenance = numberField("Ex.: 0,15");
        addField(variable, "Reserva de manutenção (R$/km)", maintenance);
        depreciation = numberField("Ex.: 0,20");
        addField(variable, "Depreciação estimada (R$/km)", depreciation);

        LinearLayout fixed = PremiumUi.card(this);
        LinearLayout.LayoutParams fixedLp = new LinearLayout.LayoutParams(-1, -2);
        fixedLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        page.addView(fixed, fixedLp);
        fixed.addView(sectionTitle("Custos fixos"));
        TextView fixedHelp = PremiumUi.text(this,
                "Some seguro, IPVA mensalizado, financiamento/aluguel e outros custos que existem mesmo sem corrida.",
                11, PremiumUi.MUTED, false);
        fixedHelp.setPadding(0, PremiumUi.dp(this, 5), 0, PremiumUi.dp(this, 2));
        fixed.addView(fixedHelp);
        fixedMonthly = numberField("Ex.: 900,00");
        addField(fixed, "Custos fixos por mês (R$)", fixedMonthly);
        monthlyKm = numberField("Ex.: 3000");
        addField(fixed, "Km que pretende rodar por mês", monthlyKm);

        preview = PremiumUi.card(this);
        LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(-1, -2);
        previewLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        page.addView(preview, previewLp);

        Button save = PremiumUi.primaryButton(this, "Salvar perfil do veículo");
        save.setOnClickListener(v -> save());
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50));
        saveLp.setMargins(0, PremiumUi.dp(this, 16), 0, 0);
        page.addView(save, saveLp);

        Button example = PremiumUi.secondaryButton(this, "Preencher exemplo para teste");
        example.setOnClickListener(v -> fillExample());
        LinearLayout.LayoutParams exampleLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 48));
        exampleLp.setMargins(0, PremiumUi.dp(this, 9), 0, 0);
        page.addView(example, exampleLp);

        TextView note = PremiumUi.text(this,
                "Os valores de manutenção, depreciação e custos fixos são estimativas. Eles servem para análise financeira e não representam uma despesa cobrada pelo NÓ.",
                10, PremiumUi.MUTED, false);
        note.setGravity(Gravity.CENTER);
        note.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        note.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 13), PremiumUi.dp(this, 8), 0);
        page.addView(note);

        setContentView(scroll);
        loadCurrent();
    }

    private TextView sectionTitle(String value) {
        TextView t = PremiumUi.text(this, value, 17, PremiumUi.TEXT, true);
        t.setPadding(0, 0, 0, PremiumUi.dp(this, 2));
        return t;
    }

    private void addField(LinearLayout parent, String label, EditText input) {
        TextView l = PremiumUi.text(this, label, 11, PremiumUi.MUTED, true);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(-1, -2);
        labelLp.setMargins(0, PremiumUi.dp(this, 12), 0, PremiumUi.dp(this, 6));
        parent.addView(l, labelLp);
        parent.addView(input, new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50)));
    }

    private EditText field(String hint, int type) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(type);
        input.setHint(hint);
        input.setTextSize(14);
        input.setTextColor(PremiumUi.TEXT);
        input.setHintTextColor(Color.rgb(152, 162, 179));
        input.setPadding(PremiumUi.dp(this, 14), 0, PremiumUi.dp(this, 14), 0);
        input.setBackground(PremiumUi.shape(PremiumUi.SURFACE, PremiumUi.dp(this, 15), PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        return input;
    }

    private EditText numberField(String hint) {
        return field(hint, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private void loadCurrent() {
        VehicleProfile p = VehicleProfileStore.load(this);
        if (!p.name.isEmpty()) name.setText(p.name);
        fuelType.setText(p.fuelType.isEmpty() ? "Gasolina" : p.fuelType);
        setIfPositive(fuelPrice, p.fuelPrice);
        setIfPositive(consumption, p.consumptionKmPerLiter);
        setIfPositive(maintenance, p.maintenancePerKm);
        setIfPositive(depreciation, p.depreciationPerKm);
        setIfPositive(fixedMonthly, p.fixedMonthlyCost);
        setIfPositive(monthlyKm, p.expectedMonthlyKm);
        renderPreview(p);
    }

    private void setIfPositive(EditText target, double value) {
        if (value > 0) target.setText(trim(value));
    }

    private void fillExample() {
        name.setText("HB20 1.0");
        fuelType.setText("Gasolina");
        fuelPrice.setText("6,00");
        consumption.setText("10,0");
        maintenance.setText("0,15");
        depreciation.setText("0,20");
        fixedMonthly.setText("900,00");
        monthlyKm.setText("3000");
        renderPreview(readForm());
    }

    private void save() {
        VehicleProfile p = readForm();
        if (p.fuelPrice <= 0) {
            error("Informe o preço do combustível.");
            return;
        }
        if (p.consumptionKmPerLiter <= 0) {
            error("Informe quantos km por litro o veículo faz.");
            return;
        }
        if (p.fixedMonthlyCost > 0 && p.expectedMonthlyKm <= 0) {
            error("Para ratear custos fixos, informe a quilometragem mensal.");
            return;
        }
        VehicleProfileStore.save(this, p);
        renderPreview(p);
        Toast.makeText(this, "Perfil do veículo salvo", Toast.LENGTH_SHORT).show();
    }

    private VehicleProfile readForm() {
        return new VehicleProfile(
                value(name),
                value(fuelType),
                number(fuelPrice),
                number(consumption),
                number(maintenance),
                number(depreciation),
                number(fixedMonthly),
                number(monthlyKm),
                System.currentTimeMillis()
        );
    }

    private void renderPreview(VehicleProfile p) {
        if (preview == null) return;
        preview.removeAllViews();
        preview.addView(sectionTitle("Resumo do custo"));

        if (p == null || !p.isConfigured()) {
            TextView empty = PremiumUi.text(this,
                    "Preencha preço do combustível e consumo para o NÓ calcular seu custo por km.",
                    12, PremiumUi.MUTED, false);
            empty.setPadding(0, PremiumUi.dp(this, 8), 0, 0);
            preview.addView(empty);
            return;
        }

        VehicleCostEngine.CostEstimate e = VehicleCostEngine.estimate(p, 1.0, 0, 0);
        addSummary(preview, "Combustível", moneyPerKm(e.fuelPerKm));
        addDivider(preview);
        addSummary(preview, "Manutenção", moneyPerKm(e.maintenancePerKm));
        addDivider(preview);
        addSummary(preview, "Depreciação", moneyPerKm(e.depreciationPerKm));
        addDivider(preview);
        addSummary(preview, "Fixos rateados", moneyPerKm(e.fixedPerKm));
        addDivider(preview);

        TextView label = PremiumUi.text(this, "CUSTO ESTIMADO POR KM", 10, PremiumUi.MUTED, true);
        label.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        preview.addView(label);
        TextView total = PremiumUi.text(this, moneyPerKm(e.totalPerKm), 27, PremiumUi.NAVY, true);
        total.setPadding(0, PremiumUi.dp(this, 5), 0, 0);
        preview.addView(total);
    }

    private void addSummary(LinearLayout card, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(PremiumUi.text(this, label, 11, PremiumUi.MUTED, false), new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(PremiumUi.text(this, value, 12, PremiumUi.TEXT, true));
        card.addView(row);
    }

    private void addDivider(LinearLayout card) {
        android.view.View d = new android.view.View(this);
        d.setBackgroundColor(PremiumUi.BORDER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 1));
        lp.setMargins(0, PremiumUi.dp(this, 10), 0, PremiumUi.dp(this, 10));
        card.addView(d, lp);
    }

    private void error(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String value(EditText input) {
        return input == null ? "" : input.getText().toString().trim();
    }

    private double number(EditText input) {
        try {
            String raw = value(input).replace(".", "").replace(',', '.');
            return raw.isEmpty() ? 0 : Double.parseDouble(raw);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String trim(double value) {
        if (Math.rint(value) == value) return String.format(ptBr, "%.0f", value);
        return String.format(ptBr, "%.2f", value);
    }

    private String moneyPerKm(double value) {
        return String.format(ptBr, "R$ %.2f/km", value);
    }
}
