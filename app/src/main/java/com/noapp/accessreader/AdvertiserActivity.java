package com.noapp.accessreader;

import android.app.Activity;
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
import java.util.Date;
import java.util.Locale;

/**
 * Jornada MVP do anunciante para o NÓ Mídia.
 *
 * Nesta primeira versão a solicitação é salva localmente. Ela não reserva veículos
 * nem publica uma campanha de verdade; serve para validar a experiência e os dados
 * necessários antes da conexão com backend/inventário real.
 */
public class AdvertiserActivity extends Activity {

    private static final String PREFS = "no_media";
    private static final int TOTAL_STEPS = 5;

    private final Locale ptBr = new Locale("pt", "BR");

    private TextView stepCounterText;
    private TextView stepTitleText;
    private TextView stepSubtitleText;
    private LinearLayout progressRow;
    private LinearLayout formCard;
    private LinearLayout navigationRow;
    private Button previousButton;
    private Button nextButton;
    private View[] progressSegments = new View[TOTAL_STEPS];
    private LinearLayout[] stepContainers = new LinearLayout[TOTAL_STEPS];

    private EditText companyField;
    private EditText campaignField;
    private RadioGroup objectiveGroup;

    private EditText cityField;
    private EditText regionField;
    private EditText radiusField;

    private RadioGroup vehicleGroup;
    private EditText quantityField;
    private EditText minYearField;
    private EditText maxYearField;
    private EditText vehicleNotesField;

    private RadioGroup formatGroup;
    private EditText startDateField;
    private EditText endDateField;
    private EditText budgetField;
    private RadioGroup activationGroup;

    private TextView summaryText;
    private int currentStep = 0;
    private boolean submitted = false;

    private final String[] titles = {
            "Sua campanha",
            "Onde quer anunciar",
            "Quais veículos precisa",
            "Formato e investimento",
            "Revise a solicitação"
    };

    private final String[] subtitles = {
            "Comece pela empresa, nome da campanha e principal objetivo.",
            "Defina a cidade e a área onde a campanha precisa circular.",
            "Escolha o modal, a quantidade e o perfil de ano dos veículos.",
            "Escolha onde a marca aparece, período, orçamento e ativação.",
            "Confira os dados. O NÓ vai usar isso para buscar inventário compatível."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
        updateStep();
    }

    private void buildScreen() {
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
        back.setOnClickListener(v -> handleBack());
        toolbar.addView(back);

        toolbar.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1f));

        TextView badge = text("NÓ MÍDIA", 10, getColor(R.color.no_orange), true);
        badge.setLetterSpacing(0.08f);
        badge.setPadding(dp(10), dp(6), dp(10), dp(6));
        badge.setBackgroundResource(R.drawable.bg_no_chip);
        toolbar.addView(badge);

        LinearLayout brandRow = new LinearLayout(this);
        brandRow.setOrientation(LinearLayout.HORIZONTAL);
        brandRow.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(-1, -2);
        brandLp.setMargins(0, dp(12), 0, 0);
        page.addView(brandRow, brandLp);

        TextView logo = text("NÓ", 34, getColor(R.color.no_white), true);
        logo.setIncludeFontPadding(false);
        brandRow.addView(logo);
        TextView dot = text(".", 34, getColor(R.color.no_cyan), true);
        dot.setIncludeFontPadding(false);
        brandRow.addView(dot);

        TextView heading = text("Crie sua campanha", 25, getColor(R.color.no_white), true);
        heading.setPadding(0, dp(6), 0, 0);
        page.addView(heading);

        TextView intro = text(
                "Solicite o inventário de mídia certo por região, veículo e formato. Nesta versão, a solicitação fica salva no aparelho para validação do fluxo.",
                13,
                getColor(R.color.no_text_secondary),
                false
        );
        intro.setLineSpacing(dp(1), 1f);
        intro.setPadding(0, dp(5), 0, 0);
        page.addView(intro);

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

        stepCounterText = text("ETAPA 1 DE 5", 10, getColor(R.color.no_cyan), true);
        stepCounterText.setLetterSpacing(0.1f);
        LinearLayout.LayoutParams counterLp = new LinearLayout.LayoutParams(-1, -2);
        counterLp.setMargins(0, dp(18), 0, 0);
        page.addView(stepCounterText, counterLp);

        progressRow = new LinearLayout(this);
        progressRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(-1, dp(4));
        progressLp.setMargins(0, dp(8), 0, 0);
        page.addView(progressRow, progressLp);
        for (int i = 0; i < TOTAL_STEPS; i++) {
            View segment = new View(this);
            progressSegments[i] = segment;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            if (i > 0) lp.setMargins(dp(4), 0, 0, 0);
            progressRow.addView(segment, lp);
        }

        stepTitleText = text(titles[0], 20, getColor(R.color.no_white), true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, dp(16), 0, 0);
        page.addView(stepTitleText, titleLp);

        stepSubtitleText = text(subtitles[0], 12, getColor(R.color.no_text_secondary), false);
        stepSubtitleText.setPadding(0, dp(4), 0, 0);
        page.addView(stepSubtitleText);

        formCard = new LinearLayout(this);
        formCard.setOrientation(LinearLayout.VERTICAL);
        formCard.setPadding(dp(14), dp(14), dp(14), dp(14));
        formCard.setBackgroundResource(R.drawable.bg_no_card);
        LinearLayout.LayoutParams formLp = new LinearLayout.LayoutParams(-1, -2);
        formLp.setMargins(0, dp(14), 0, 0);
        page.addView(formCard, formLp);

        buildStepOne();
        buildStepTwo();
        buildStepThree();
        buildStepFour();
        buildStepFive();

        navigationRow = new LinearLayout(this);
        navigationRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams navLp = new LinearLayout.LayoutParams(-1, -2);
        navLp.setMargins(0, dp(14), 0, 0);
        page.addView(navigationRow, navLp);

        previousButton = secondaryButton("Voltar");
        previousButton.setOnClickListener(v -> previousStep());
        LinearLayout.LayoutParams prevLp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        prevLp.setMargins(0, 0, dp(5), 0);
        navigationRow.addView(previousButton, prevLp);

        nextButton = primaryButton("Continuar");
        nextButton.setOnClickListener(v -> nextStep());
        LinearLayout.LayoutParams nextLp = new LinearLayout.LayoutParams(0, dp(48), 1.35f);
        nextLp.setMargins(dp(5), 0, 0, 0);
        navigationRow.addView(nextButton, nextLp);

        TextView disclaimer = text(
                "A solicitação não garante disponibilidade nem reserva veículos. O match, cobertura e preço final dependem do inventário elegível e das regras da campanha.",
                11,
                getColor(R.color.no_text_muted),
                false
        );
        disclaimer.setPadding(0, dp(14), 0, 0);
        page.addView(disclaimer);

        setContentView(scroll);
    }

    private void buildStepOne() {
        LinearLayout step = stepContainer();
        stepContainers[0] = step;
        formCard.addView(step);

        addSectionLabel(step, "1  •  IDENTIFICAÇÃO");
        companyField = field("Nome da empresa", InputType.TYPE_CLASS_TEXT);
        addField(step, "Empresa / marca", companyField);

        campaignField = field("Ex.: Lançamento Cotia - Outubro", InputType.TYPE_CLASS_TEXT);
        addField(step, "Nome da campanha", campaignField);

        addChoiceLabel(step, "Objetivo principal");
        objectiveGroup = radioGroup(
                "Reconhecimento de marca",
                "Promoção / oferta",
                "Inauguração / lançamento",
                "Tráfego / QR Code"
        );
        step.addView(objectiveGroup);
    }

    private void buildStepTwo() {
        LinearLayout step = stepContainer();
        stepContainers[1] = step;
        formCard.addView(step);

        addSectionLabel(step, "2  •  REGIÃO");
        cityField = field("Ex.: Cotia - SP", InputType.TYPE_CLASS_TEXT);
        addField(step, "Cidade principal", cityField);

        regionField = field("Ex.: Centro, Granja Viana ou eixo Raposo Tavares", InputType.TYPE_CLASS_TEXT);
        addField(step, "Bairros, região ou rota desejada", regionField);

        radiusField = field("Ex.: 15", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        addField(step, "Raio aproximado (km)", radiusField);
    }

    private void buildStepThree() {
        LinearLayout step = stepContainer();
        stepContainers[2] = step;
        formCard.addView(step);

        addSectionLabel(step, "3  •  INVENTÁRIO DE VEÍCULOS");
        addChoiceLabel(step, "Tipo de veículo");
        vehicleGroup = radioGroup(
                "Carro de passeio",
                "Moto / Motoboy",
                "Van / Utilitário",
                "Caminhão"
        );
        step.addView(vehicleGroup);

        quantityField = field("Ex.: 20", InputType.TYPE_CLASS_NUMBER);
        addField(step, "Quantidade desejada", quantityField);

        LinearLayout years = new LinearLayout(this);
        years.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams yearsLp = new LinearLayout.LayoutParams(-1, -2);
        yearsLp.setMargins(0, dp(12), 0, 0);
        step.addView(years, yearsLp);

        LinearLayout minBox = new LinearLayout(this);
        minBox.setOrientation(LinearLayout.VERTICAL);
        minYearField = field("Ex.: 2020", InputType.TYPE_CLASS_NUMBER);
        minBox.addView(fieldLabel("Ano mínimo"));
        LinearLayout.LayoutParams minFieldLp = new LinearLayout.LayoutParams(-1, dp(48));
        minFieldLp.setMargins(0, dp(6), 0, 0);
        minBox.addView(minYearField, minFieldLp);
        LinearLayout.LayoutParams minBoxLp = new LinearLayout.LayoutParams(0, -2, 1f);
        minBoxLp.setMargins(0, 0, dp(5), 0);
        years.addView(minBox, minBoxLp);

        LinearLayout maxBox = new LinearLayout(this);
        maxBox.setOrientation(LinearLayout.VERTICAL);
        maxYearField = field("Opcional", InputType.TYPE_CLASS_NUMBER);
        maxBox.addView(fieldLabel("Ano máximo"));
        LinearLayout.LayoutParams maxFieldLp = new LinearLayout.LayoutParams(-1, dp(48));
        maxFieldLp.setMargins(0, dp(6), 0, 0);
        maxBox.addView(maxYearField, maxFieldLp);
        LinearLayout.LayoutParams maxBoxLp = new LinearLayout.LayoutParams(0, -2, 1f);
        maxBoxLp.setMargins(dp(5), 0, 0, 0);
        years.addView(maxBox, maxBoxLp);

        vehicleNotesField = multilineField("Ex.: preferência por hatch/sedan, cor neutra, baú fechado...");
        addField(step, "Marca, modelo ou perfil adicional (opcional)", vehicleNotesField);
    }

    private void buildStepFour() {
        LinearLayout step = stepContainer();
        stepContainers[3] = step;
        formCard.addView(step);

        addSectionLabel(step, "4  •  FORMATO DA CAMPANHA");
        addChoiceLabel(step, "Onde a publicidade aparece");
        formatGroup = radioGroup(
                "Vidro traseiro",
                "Lateral do veículo",
                "Envelopamento",
                "Baú",
                "Bag / Caixa",
                "Capacete",
                "Uniforme",
                "Somente online no app"
        );
        step.addView(formatGroup);

        startDateField = field("DD/MM/AAAA", InputType.TYPE_CLASS_DATETIME);
        addField(step, "Início", startDateField);

        endDateField = field("DD/MM/AAAA", InputType.TYPE_CLASS_DATETIME);
        addField(step, "Fim", endDateField);

        budgetField = field("Ex.: 20000", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        addField(step, "Orçamento estimado (R$)", budgetField);

        addChoiceLabel(step, "Ativação / mensuração");
        activationGroup = radioGroup(
                "Sem ativação",
                "QR Code",
                "NFC",
                "Link / cupom"
        );
        step.addView(activationGroup);
    }

    private void buildStepFive() {
        LinearLayout step = stepContainer();
        stepContainers[4] = step;
        formCard.addView(step);

        addSectionLabel(step, "5  •  RESUMO");
        summaryText = text("", 13, getColor(R.color.no_white), false);
        summaryText.setLineSpacing(dp(3), 1f);
        summaryText.setPadding(dp(12), dp(12), dp(12), dp(12));
        summaryText.setBackgroundResource(R.drawable.bg_no_metric);
        step.addView(summaryText, new LinearLayout.LayoutParams(-1, -2));

        TextView note = text(
                "Ao solicitar, o NÓ registra o briefing. A próxima etapa do produto será consultar veículos elegíveis e retornar cobertura, disponibilidade e proposta.",
                11,
                getColor(R.color.no_text_secondary),
                false
        );
        note.setPadding(0, dp(12), 0, 0);
        step.addView(note);
    }

    private LinearLayout stepContainer() {
        LinearLayout step = new LinearLayout(this);
        step.setOrientation(LinearLayout.VERTICAL);
        return step;
    }

    private void addSectionLabel(LinearLayout parent, String value) {
        TextView label = text(value, 10, getColor(R.color.no_cyan), true);
        label.setLetterSpacing(0.08f);
        parent.addView(label);
    }

    private void addChoiceLabel(LinearLayout parent, String value) {
        TextView label = fieldLabel(value);
        label.setPadding(0, dp(12), 0, dp(4));
        parent.addView(label);
    }

    private void addField(LinearLayout parent, String label, EditText editText) {
        TextView labelView = fieldLabel(label);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(-1, -2);
        labelLp.setMargins(0, dp(12), 0, 0);
        parent.addView(labelView, labelLp);

        LinearLayout.LayoutParams fieldLp = new LinearLayout.LayoutParams(-1, editText.getMinLines() > 1 ? dp(88) : dp(48));
        fieldLp.setMargins(0, dp(6), 0, 0);
        parent.addView(editText, fieldLp);
    }

    private TextView fieldLabel(String value) {
        return text(value, 12, getColor(R.color.no_text_secondary), true);
    }

    private EditText field(String hint, int inputType) {
        EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setSingleLine(true);
        input.setTextColor(getColor(R.color.no_white));
        input.setHintTextColor(getColor(R.color.no_text_muted));
        input.setTextSize(14);
        input.setHint(hint);
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setBackgroundResource(R.drawable.bg_no_metric);
        input.setSelectAllOnFocus(false);
        return input;
    }

    private EditText multilineField(String hint) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setSingleLine(false);
        input.setMinLines(3);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setTextColor(getColor(R.color.no_white));
        input.setHintTextColor(getColor(R.color.no_text_muted));
        input.setTextSize(14);
        input.setHint(hint);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackgroundResource(R.drawable.bg_no_metric);
        return input;
    }

    private RadioGroup radioGroup(String... options) {
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        for (String option : options) {
            RadioButton radio = new RadioButton(this);
            radio.setText(option);
            radio.setTextColor(getColor(R.color.no_white));
            radio.setTextSize(13);
            radio.setPadding(dp(2), dp(5), 0, dp(5));
            radio.setButtonTintList(ColorStateList.valueOf(getColor(R.color.no_cyan)));
            group.addView(radio, new RadioGroup.LayoutParams(-1, -2));
        }
        return group;
    }

    private Button primaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextAllCaps(false);
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
        button.setTextAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(getColor(R.color.no_white));
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackgroundResource(R.drawable.bg_no_button_secondary);
        button.setStateListAnimator(null);
        return button;
    }

    private void nextStep() {
        if (!validateStep(currentStep)) return;

        if (currentStep < TOTAL_STEPS - 1) {
            currentStep++;
            updateStep();
            return;
        }

        submitRequest();
    }

    private void previousStep() {
        if (currentStep <= 0) {
            finish();
            return;
        }
        currentStep--;
        updateStep();
    }

    private boolean validateStep(int step) {
        if (step == 0) {
            if (isBlank(companyField) || isBlank(campaignField) || selectedText(objectiveGroup).isEmpty()) {
                return validationError("Preencha empresa, campanha e objetivo.");
            }
        } else if (step == 1) {
            if (isBlank(cityField) || parsePositive(radiusField) <= 0) {
                return validationError("Informe a cidade e um raio maior que zero.");
            }
        } else if (step == 2) {
            if (selectedText(vehicleGroup).isEmpty() || parseInteger(quantityField) <= 0) {
                return validationError("Escolha o veículo e informe a quantidade desejada.");
            }
            int minYear = parseInteger(minYearField);
            int maxYear = isBlank(maxYearField) ? 0 : parseInteger(maxYearField);
            if (minYear < 1980 || minYear > 2100) {
                return validationError("Informe um ano mínimo válido.");
            }
            if (maxYear > 0 && (maxYear < minYear || maxYear > 2100)) {
                return validationError("O ano máximo precisa ser igual ou maior que o ano mínimo.");
            }
        } else if (step == 3) {
            if (selectedText(formatGroup).isEmpty()) {
                return validationError("Escolha um formato de publicidade.");
            }
            if (!validDate(startDateField) || !validDate(endDateField)) {
                return validationError("Use datas no formato DD/MM/AAAA.");
            }
            if (parsePositive(budgetField) <= 0) {
                return validationError("Informe um orçamento maior que zero.");
            }
            if (selectedText(activationGroup).isEmpty()) {
                return validationError("Escolha uma opção de ativação / mensuração.");
            }
        }
        return true;
    }

    private boolean validationError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void updateStep() {
        if (submitted) return;

        for (int i = 0; i < TOTAL_STEPS; i++) {
            stepContainers[i].setVisibility(i == currentStep ? View.VISIBLE : View.GONE);
            progressSegments[i].setBackgroundColor(i <= currentStep
                    ? getColor(R.color.no_cyan)
                    : getColor(R.color.no_card_soft));
        }

        stepCounterText.setText("ETAPA " + (currentStep + 1) + " DE " + TOTAL_STEPS);
        stepTitleText.setText(titles[currentStep]);
        stepSubtitleText.setText(subtitles[currentStep]);
        previousButton.setText(currentStep == 0 ? "Central" : "Voltar");
        nextButton.setText(currentStep == TOTAL_STEPS - 1 ? "Solicitar campanha" : "Continuar");

        if (currentStep == TOTAL_STEPS - 1) {
            summaryText.setText(buildSummary());
        }
    }

    private String buildSummary() {
        String yearRange = safe(minYearField);
        if (!isBlank(maxYearField)) yearRange += " a " + safe(maxYearField);
        else yearRange += " ou superior";

        String region = safe(cityField);
        if (!isBlank(regionField)) region += " • " + safe(regionField);
        region += " • raio " + safe(radiusField) + " km";

        return "EMPRESA\n" + safe(companyField)
                + "\n\nCAMPANHA\n" + safe(campaignField)
                + "\n" + selectedText(objectiveGroup)
                + "\n\nREGIÃO\n" + region
                + "\n\nVEÍCULOS\n" + safe(quantityField) + " × " + selectedText(vehicleGroup)
                + "\nAno: " + yearRange
                + (isBlank(vehicleNotesField) ? "" : "\nPerfil: " + safe(vehicleNotesField))
                + "\n\nFORMATO\n" + selectedText(formatGroup)
                + "\n\nPERÍODO\n" + safe(startDateField) + " → " + safe(endDateField)
                + "\n\nORÇAMENTO\nR$ " + normalizeMoneyInput(safe(budgetField))
                + "\n\nATIVAÇÃO\n" + selectedText(activationGroup);
    }

    private void submitRequest() {
        String requestId = "NO-" + new SimpleDateFormat("yyMMdd-HHmmss", Locale.ROOT).format(new Date());
        String summary = buildSummary();

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit()
                .putString("last_request_id", requestId)
                .putString("last_request_status", "SOLICITAÇÃO RECEBIDA")
                .putLong("last_request_time", System.currentTimeMillis())
                .putString("last_request_company", safe(companyField))
                .putString("last_request_campaign", safe(campaignField))
                .putString("last_request_summary", summary)
                .apply();

        submitted = true;
        stepCounterText.setText("SOLICITAÇÃO ENVIADA");
        stepTitleText.setText("Recebemos seu briefing");
        stepSubtitleText.setText("O próximo estágio do produto vai cruzar esta solicitação com o inventário de veículos elegíveis.");
        progressRow.setVisibility(View.GONE);
        navigationRow.setVisibility(View.GONE);

        formCard.removeAllViews();

        TextView status = text("✓  SOLICITAÇÃO RECEBIDA", 11, getColor(R.color.no_cyan_soft), true);
        status.setLetterSpacing(0.06f);
        status.setPadding(dp(10), dp(7), dp(10), dp(7));
        status.setBackgroundResource(R.drawable.bg_no_status_active);
        formCard.addView(status, new LinearLayout.LayoutParams(-2, -2));

        TextView id = text(requestId, 22, getColor(R.color.no_white), true);
        id.setPadding(0, dp(14), 0, 0);
        formCard.addView(id);

        TextView explanation = text(
                "Nenhum veículo foi reservado ainda. Esta versão registra o pedido e valida a jornada do anunciante.",
                12,
                getColor(R.color.no_text_secondary),
                false
        );
        explanation.setPadding(0, dp(5), 0, dp(12));
        formCard.addView(explanation);

        TextView savedSummary = text(summary, 12, getColor(R.color.no_white), false);
        savedSummary.setLineSpacing(dp(2), 1f);
        savedSummary.setPadding(dp(12), dp(12), dp(12), dp(12));
        savedSummary.setBackgroundResource(R.drawable.bg_no_metric);
        formCard.addView(savedSummary, new LinearLayout.LayoutParams(-1, -2));

        Button central = primaryButton("Voltar para a Central");
        central.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams centralLp = new LinearLayout.LayoutParams(-1, dp(48));
        centralLp.setMargins(0, dp(14), 0, 0);
        formCard.addView(central, centralLp);
    }

    private String selectedText(RadioGroup group) {
        if (group == null || group.getCheckedRadioButtonId() == -1) return "";
        RadioButton radio = group.findViewById(group.getCheckedRadioButtonId());
        return radio == null ? "" : radio.getText().toString();
    }

    private boolean isBlank(EditText input) {
        return input == null || TextUtils.isEmpty(input.getText().toString().trim());
    }

    private String safe(EditText input) {
        return input == null ? "" : input.getText().toString().trim();
    }

    private int parseInteger(EditText input) {
        try {
            return Integer.parseInt(safe(input));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private double parsePositive(EditText input) {
        try {
            return Double.parseDouble(safe(input).replace(',', '.'));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private boolean validDate(EditText input) {
        String value = safe(input);
        if (!value.matches("\\d{2}/\\d{2}/\\d{4}")) return false;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", ptBr);
        format.setLenient(false);
        try {
            format.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private String normalizeMoneyInput(String value) {
        try {
            double parsed = Double.parseDouble(value.replace(',', '.'));
            return String.format(ptBr, "%.2f", parsed);
        } catch (Exception ignored) {
            return value;
        }
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

    private void handleBack() {
        if (submitted || currentStep == 0) finish();
        else previousStep();
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
