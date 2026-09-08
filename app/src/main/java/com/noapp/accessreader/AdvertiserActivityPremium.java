package com.noapp.accessreader;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Fluxo premium e simplificado para criação de campanhas do NÓ Mídia. */
public class AdvertiserActivityPremium extends Activity {

    private static final String PREFS = "no_media";
    private static final int TOTAL_STEPS = 5;
    private final Locale ptBr = new Locale("pt", "BR");

    private LinearLayout page;
    private LinearLayout form;
    private TextView stepText;
    private TextView titleText;
    private TextView subtitleText;
    private Button nextButton;
    private TextView backButton;
    private View[] progress = new View[TOTAL_STEPS];
    private LinearLayout[] steps = new LinearLayout[TOTAL_STEPS];
    private int current = 0;

    private EditText company;
    private EditText campaign;
    private RadioGroup objective;
    private EditText city;
    private EditText region;
    private EditText radius;
    private RadioGroup vehicle;
    private EditText quantity;
    private EditText minYear;
    private EditText maxYear;
    private EditText vehicleNotes;
    private RadioGroup format;
    private EditText startDate;
    private EditText endDate;
    private EditText budget;
    private RadioGroup activation;
    private TextView review;

    private final String[] titles = {
            "Vamos começar pela campanha",
            "Onde sua marca precisa circular?",
            "Que veículos você procura?",
            "Como será a mídia?",
            "Confira antes de enviar"
    };

    private final String[] subtitles = {
            "Poucos dados agora. Você pode refinar a campanha depois.",
            "Escolha a cidade e a área principal de circulação.",
            "Defina o tipo, quantidade e faixa de ano dos veículos.",
            "Escolha formato, período, orçamento e mensuração.",
            "Revise o briefing. O NÓ ainda não reserva veículos nesta etapa."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumUi.lightSystemBars(this);
        build();
        updateStep();
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setBackgroundColor(PremiumUi.BG);

        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(PremiumUi.dp(this, 16), PremiumUi.dp(this, 14), PremiumUi.dp(this, 16), PremiumUi.dp(this, 24));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        backButton = PremiumUi.text(this, "‹  Voltar", 13, PremiumUi.NAVY, true);
        backButton.setPadding(0, PremiumUi.dp(this, 8), PremiumUi.dp(this, 8), PremiumUi.dp(this, 8));
        backButton.setOnClickListener(v -> goBack());
        toolbar.addView(backButton);
        toolbar.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));
        toolbar.addView(PremiumUi.chip(this, "NÓ MÍDIA", PremiumUi.ORANGE, Color.rgb(255, 246, 232)));
        page.addView(toolbar);

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(-1, -2);
        brandLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        page.addView(brand, brandLp);
        brand.addView(PremiumUi.text(this, "NÓ", 28, PremiumUi.NAVY, true));
        brand.addView(PremiumUi.text(this, ".", 28, PremiumUi.CYAN, true));

        stepText = PremiumUi.text(this, "ETAPA 1 DE 5", 10, PremiumUi.MUTED, true);
        stepText.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams stepLp = new LinearLayout.LayoutParams(-1, -2);
        stepLp.setMargins(0, PremiumUi.dp(this, 14), 0, 0);
        page.addView(stepText, stepLp);

        LinearLayout progressRow = new LinearLayout(this);
        progressRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 4));
        progressLp.setMargins(0, PremiumUi.dp(this, 8), 0, 0);
        page.addView(progressRow, progressLp);
        for (int i = 0; i < TOTAL_STEPS; i++) {
            progress[i] = new View(this);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -1, 1f);
            if (i > 0) p.setMargins(PremiumUi.dp(this, 4), 0, 0, 0);
            progressRow.addView(progress[i], p);
        }

        titleText = PremiumUi.text(this, titles[0], 23, PremiumUi.TEXT, true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, PremiumUi.dp(this, 18), 0, 0);
        page.addView(titleText, titleLp);

        subtitleText = PremiumUi.text(this, subtitles[0], 12, PremiumUi.MUTED, false);
        subtitleText.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.setMargins(0, PremiumUi.dp(this, 5), 0, 0);
        page.addView(subtitleText, subLp);

        form = PremiumUi.card(this);
        LinearLayout.LayoutParams formLp = new LinearLayout.LayoutParams(-1, -2);
        formLp.setMargins(0, PremiumUi.dp(this, 16), 0, 0);
        page.addView(form, formLp);

        buildStep1();
        buildStep2();
        buildStep3();
        buildStep4();
        buildStep5();

        nextButton = PremiumUi.primaryButton(this, "Continuar");
        nextButton.setOnClickListener(v -> next());
        LinearLayout.LayoutParams nextLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50));
        nextLp.setMargins(0, PremiumUi.dp(this, 16), 0, 0);
        page.addView(nextButton, nextLp);

        TextView footer = PremiumUi.text(this,
                "Você poderá revisar a proposta antes de qualquer ativação de campanha.",
                11, PremiumUi.MUTED, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(PremiumUi.dp(this, 8), PremiumUi.dp(this, 13), PremiumUi.dp(this, 8), 0);
        page.addView(footer);

        setContentView(scroll);
    }

    private void buildStep1() {
        LinearLayout s = step();
        steps[0] = s;
        form.addView(s);
        company = field("Ex.: Minha empresa", InputType.TYPE_CLASS_TEXT);
        addField(s, "Empresa ou marca", company);
        campaign = field("Ex.: Campanha Verão Cotia", InputType.TYPE_CLASS_TEXT);
        addField(s, "Nome da campanha", campaign);
        addLabel(s, "Objetivo principal");
        objective = choices("Reconhecimento de marca", "Promoção / oferta", "Inauguração / lançamento", "Tráfego / QR Code");
        s.addView(objective);
    }

    private void buildStep2() {
        LinearLayout s = step();
        steps[1] = s;
        form.addView(s);
        city = field("Ex.: Cotia - SP", InputType.TYPE_CLASS_TEXT);
        addField(s, "Cidade principal", city);
        region = field("Ex.: Granja Viana, Centro, Raposo Tavares", InputType.TYPE_CLASS_TEXT);
        addField(s, "Bairros, região ou rota", region);
        radius = field("Ex.: 15", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        addField(s, "Raio aproximado (km)", radius);
    }

    private void buildStep3() {
        LinearLayout s = step();
        steps[2] = s;
        form.addView(s);
        addLabel(s, "Tipo de veículo");
        vehicle = choices("Carro de passeio", "Moto / Motoboy", "Van / Utilitário", "Caminhão");
        s.addView(vehicle);
        quantity = field("Ex.: 20", InputType.TYPE_CLASS_NUMBER);
        addField(s, "Quantidade desejada", quantity);

        LinearLayout years = new LinearLayout(this);
        years.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams ylp = new LinearLayout.LayoutParams(-1, -2);
        ylp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        s.addView(years, ylp);

        LinearLayout left = miniField("Ano mínimo", minYear = field("2020", InputType.TYPE_CLASS_NUMBER));
        LinearLayout right = miniField("Ano máximo", maxYear = field("Opcional", InputType.TYPE_CLASS_NUMBER));
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(0, -2, 1f);
        l.setMargins(0, 0, PremiumUi.dp(this, 5), 0);
        LinearLayout.LayoutParams r = new LinearLayout.LayoutParams(0, -2, 1f);
        r.setMargins(PremiumUi.dp(this, 5), 0, 0, 0);
        years.addView(left, l);
        years.addView(right, r);

        vehicleNotes = multilineField("Ex.: preferência por hatch/sedan, cor neutra, baú fechado...");
        addField(s, "Observação opcional", vehicleNotes);
    }

    private void buildStep4() {
        LinearLayout s = step();
        steps[3] = s;
        form.addView(s);
        addLabel(s, "Formato da publicidade");
        format = choices("Vidro traseiro", "Lateral do veículo", "Envelopamento", "Baú", "Bag / Caixa", "Capacete", "Uniforme", "Somente online no app");
        s.addView(format);

        startDate = dateField();
        addField(s, "Início", startDate);
        endDate = dateField();
        addField(s, "Fim", endDate);
        budget = field("Ex.: 3000", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        addField(s, "Orçamento estimado (R$)", budget);

        addLabel(s, "Mensuração");
        activation = choices("Sem ativação", "QR Code", "NFC", "Link / cupom");
        s.addView(activation);
    }

    private void buildStep5() {
        LinearLayout s = step();
        steps[4] = s;
        form.addView(s);
        TextView hint = PremiumUi.text(this, "RESUMO DA SOLICITAÇÃO", 10, PremiumUi.MUTED, true);
        hint.setLetterSpacing(0.08f);
        s.addView(hint);
        review = PremiumUi.text(this, "", 13, PremiumUi.TEXT, false);
        review.setLineSpacing(PremiumUi.dp(this, 4), 1f);
        review.setPadding(PremiumUi.dp(this, 14), PremiumUi.dp(this, 14), PremiumUi.dp(this, 14), PremiumUi.dp(this, 14));
        review.setBackground(PremiumUi.shape(PremiumUi.SOFT, PremiumUi.dp(this, 16), PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, -2);
        rlp.setMargins(0, PremiumUi.dp(this, 10), 0, 0);
        s.addView(review, rlp);
    }

    private LinearLayout step() {
        LinearLayout s = new LinearLayout(this);
        s.setOrientation(LinearLayout.VERTICAL);
        return s;
    }

    private void addLabel(LinearLayout parent, String label) {
        TextView tv = PremiumUi.text(this, label, 12, PremiumUi.MUTED, true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, PremiumUi.dp(this, 14), 0, PremiumUi.dp(this, 6));
        parent.addView(tv, lp);
    }

    private void addField(LinearLayout parent, String label, EditText field) {
        addLabel(parent, label);
        parent.addView(field, new LinearLayout.LayoutParams(-1, field.getMinLines() > 1 ? PremiumUi.dp(this, 88) : PremiumUi.dp(this, 50)));
    }

    private LinearLayout miniField(String label, EditText input) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(PremiumUi.text(this, label, 11, PremiumUi.MUTED, true));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50));
        lp.setMargins(0, PremiumUi.dp(this, 6), 0, 0);
        box.addView(input, lp);
        return box;
    }

    private EditText field(String hint, int inputType) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setHint(hint);
        input.setTextSize(14);
        input.setTextColor(PremiumUi.TEXT);
        input.setHintTextColor(Color.rgb(152, 162, 179));
        input.setPadding(PremiumUi.dp(this, 14), 0, PremiumUi.dp(this, 14), 0);
        input.setBackground(PremiumUi.shape(PremiumUi.SURFACE, PremiumUi.dp(this, 15), PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        return input;
    }

    private EditText multilineField(String hint) {
        EditText input = field(hint, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setSingleLine(false);
        input.setMinLines(3);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setPadding(PremiumUi.dp(this, 14), PremiumUi.dp(this, 12), PremiumUi.dp(this, 14), PremiumUi.dp(this, 12));
        return input;
    }

    private EditText dateField() {
        EditText input = field("Selecione a data", InputType.TYPE_CLASS_DATETIME);
        input.setFocusable(false);
        input.setClickable(true);
        input.setCursorVisible(false);
        input.setOnClickListener(v -> openDatePicker(input));
        return input;
    }

    private void openDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        Date parsed = parseDate(target.getText().toString());
        if (parsed != null) c.setTime(parsed);
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> target.setText(String.format(ptBr, "%02d/%02d/%04d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private RadioGroup choices(String... values) {
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        for (String value : values) {
            RadioButton rb = new RadioButton(this);
            rb.setText(value);
            rb.setTextColor(PremiumUi.TEXT);
            rb.setTextSize(13);
            rb.setButtonTintList(ColorStateList.valueOf(PremiumUi.CYAN));
            rb.setPadding(PremiumUi.dp(this, 2), PremiumUi.dp(this, 7), 0, PremiumUi.dp(this, 7));
            group.addView(rb, new RadioGroup.LayoutParams(-1, -2));
        }
        return group;
    }

    private void next() {
        if (!validate(current)) return;
        if (current < TOTAL_STEPS - 1) {
            current++;
            updateStep();
        } else {
            submit();
        }
    }

    private void goBack() {
        if (current == 0) finish();
        else {
            current--;
            updateStep();
        }
    }

    private void updateStep() {
        for (int i = 0; i < TOTAL_STEPS; i++) {
            steps[i].setVisibility(i == current ? View.VISIBLE : View.GONE);
            progress[i].setBackground(PremiumUi.shape(i <= current ? PremiumUi.CYAN : PremiumUi.BORDER, PremiumUi.dp(this, 999), Color.TRANSPARENT, 0));
        }
        stepText.setText("ETAPA " + (current + 1) + " DE " + TOTAL_STEPS);
        titleText.setText(titles[current]);
        subtitleText.setText(subtitles[current]);
        nextButton.setText(current == TOTAL_STEPS - 1 ? "Enviar solicitação" : "Continuar");
        backButton.setText(current == 0 ? "‹  Central" : "‹  Voltar");
        if (current == TOTAL_STEPS - 1) review.setText(buildSummary());
    }

    private boolean validate(int step) {
        if (step == 0 && (blank(company) || blank(campaign) || selected(objective).isEmpty()))
            return error("Preencha empresa, campanha e objetivo.");
        if (step == 1 && (blank(city) || positive(radius) <= 0))
            return error("Informe a cidade e um raio maior que zero.");
        if (step == 2) {
            if (selected(vehicle).isEmpty() || integer(quantity) <= 0) return error("Escolha o veículo e a quantidade.");
            int min = integer(minYear);
            int max = blank(maxYear) ? 0 : integer(maxYear);
            if (min < 1980 || min > 2100) return error("Informe um ano mínimo válido.");
            if (max > 0 && (max < min || max > 2100)) return error("Confira o ano máximo.");
        }
        if (step == 3) {
            if (selected(format).isEmpty()) return error("Escolha o formato da publicidade.");
            Date start = parseDate(safe(startDate));
            Date end = parseDate(safe(endDate));
            if (start == null || end == null) return error("Selecione as duas datas.");
            if (end.before(start)) return error("A data final precisa ser depois da data inicial.");
            if (positive(budget) <= 0) return error("Informe um orçamento maior que zero.");
            if (selected(activation).isEmpty()) return error("Escolha uma opção de mensuração.");
        }
        return true;
    }

    private boolean error(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    private String buildSummary() {
        String years = safe(minYear) + (blank(maxYear) ? " ou superior" : " a " + safe(maxYear));
        String where = safe(city);
        if (!blank(region)) where += " • " + safe(region);
        where += " • raio " + safe(radius) + " km";
        return "EMPRESA\n" + safe(company)
                + "\n\nCAMPANHA\n" + safe(campaign) + "\n" + selected(objective)
                + "\n\nREGIÃO\n" + where
                + "\n\nVEÍCULOS\n" + safe(quantity) + " × " + selected(vehicle) + "\nAno: " + years
                + (blank(vehicleNotes) ? "" : "\nPerfil: " + safe(vehicleNotes))
                + "\n\nFORMATO\n" + selected(format)
                + "\n\nPERÍODO\n" + safe(startDate) + " → " + safe(endDate)
                + "\n\nORÇAMENTO\nR$ " + normalizeMoney(safe(budget))
                + "\n\nATIVAÇÃO\n" + selected(activation);
    }

    private void submit() {
        String id = "NO-" + new SimpleDateFormat("yyMMdd-HHmmss", Locale.ROOT).format(new Date());
        String summary = buildSummary();
        SharedPreferences.Editor e = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        e.putString("last_request_id", id)
                .putString("last_request_status", "SOLICITAÇÃO RECEBIDA")
                .putLong("last_request_time", System.currentTimeMillis())
                .putString("last_request_company", safe(company))
                .putString("last_request_campaign", safe(campaign))
                .putString("last_request_summary", summary)
                .apply();
        Toast.makeText(this, "Campanha salva", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, CampaignAnalysisPremiumActivity.class));
        finish();
    }

    private String selected(RadioGroup group) {
        if (group == null || group.getCheckedRadioButtonId() == -1) return "";
        RadioButton rb = group.findViewById(group.getCheckedRadioButtonId());
        return rb == null ? "" : rb.getText().toString();
    }

    private boolean blank(EditText e) {
        return e == null || TextUtils.isEmpty(e.getText().toString().trim());
    }

    private String safe(EditText e) {
        return e == null ? "" : e.getText().toString().trim();
    }

    private int integer(EditText e) {
        try { return Integer.parseInt(safe(e)); } catch (Exception ignored) { return 0; }
    }

    private double positive(EditText e) {
        try { return Double.parseDouble(safe(e).replace(',', '.')); } catch (Exception ignored) { return 0; }
    }

    private Date parseDate(String value) {
        if (TextUtils.isEmpty(value) || !value.matches("\\d{2}/\\d{2}/\\d{4}")) return null;
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", ptBr);
        f.setLenient(false);
        try { return f.parse(value); } catch (ParseException ignored) { return null; }
    }

    private String normalizeMoney(String value) {
        try { return String.format(ptBr, "%.2f", Double.parseDouble(value.replace(',', '.'))); }
        catch (Exception ignored) { return value; }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
