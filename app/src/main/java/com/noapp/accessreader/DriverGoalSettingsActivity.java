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

/** Meta simples: quanto o motorista quer que sobre e qual limite de horas deseja observar. */
public class DriverGoalSettingsActivity extends Activity {

    private final Locale ptBr = new Locale("pt", "BR");
    private EditText goalInput;
    private EditText hoursInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumUi.lightSystemBars(this);
        build();
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
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
        toolbar.addView(PremiumUi.chip(this, "MINHA META", PremiumUi.NAVY, PremiumUi.CYAN_SOFT));
        page.addView(toolbar);

        TextView title = PremiumUi.text(this, "Quanto precisa sobrar hoje?", 25, PremiumUi.TEXT, true);
        title.setPadding(0, PremiumUi.dp(this, 18), 0, 0);
        page.addView(title);

        TextView sub = PremiumUi.text(this,
                "O NÓ acompanha uma meta de lucro estimado, não só faturamento. Assim você mede o dia pelo que tende a ficar no bolso.",
                12, PremiumUi.MUTED, false);
        sub.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        sub.setPadding(0, PremiumUi.dp(this, 6), 0, PremiumUi.dp(this, 16));
        page.addView(sub);

        LinearLayout card = PremiumUi.card(this);
        card.addView(PremiumUi.text(this, "Meta do dia", 17, PremiumUi.TEXT, true));

        goalInput = numberField("Ex.: 300,00");
        addField(card, "Lucro estimado que você quer atingir (R$)", goalInput);

        hoursInput = numberField("Ex.: 9");
        addField(card, "Limite pessoal de horas em viagens", hoursInput);

        TextView help = PremiumUi.text(this,
                "O limite de horas usa apenas o tempo das viagens que o NÓ conseguiu registrar. Tempo parado e online fora de corrida ainda não entra nesta conta.",
                10, PremiumUi.MUTED, false);
        help.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        help.setPadding(0, PremiumUi.dp(this, 12), 0, 0);
        card.addView(help);
        page.addView(card);

        LinearLayout example = PremiumUi.card(this);
        LinearLayout.LayoutParams exampleLp = new LinearLayout.LayoutParams(-1, -2);
        exampleLp.setMargins(0, PremiumUi.dp(this, 12), 0, 0);
        page.addView(example, exampleLp);
        example.addView(PremiumUi.text(this, "Exemplo de uso", 15, PremiumUi.TEXT, true));
        TextView exampleText = PremiumUi.text(this,
                "Se sua meta é R$ 300 de lucro estimado, o NÓ mostra quanto já sobrou, quanto falta e se a meta foi atingida — sem confundir isso com o faturamento bruto.",
                11, PremiumUi.MUTED, false);
        exampleText.setLineSpacing(PremiumUi.dp(this, 2), 1f);
        exampleText.setPadding(0, PremiumUi.dp(this, 6), 0, 0);
        example.addView(exampleText);

        Button save = PremiumUi.primaryButton(this, "Salvar minha meta");
        save.setOnClickListener(v -> save());
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50));
        saveLp.setMargins(0, PremiumUi.dp(this, 16), 0, 0);
        page.addView(save, saveLp);

        Button fill = PremiumUi.secondaryButton(this, "Usar exemplo: R$ 300 em até 9h");
        fill.setOnClickListener(v -> {
            goalInput.setText("300,00");
            hoursInput.setText("9");
        });
        LinearLayout.LayoutParams fillLp = new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 48));
        fillLp.setMargins(0, PremiumUi.dp(this, 9), 0, 0);
        page.addView(fill, fillLp);

        setContentView(scroll);
        load();
    }

    private void load() {
        DriverGoalStore.DriverGoal goal = DriverGoalStore.load(this);
        if (goal.dailyNetGoal > 0) goalInput.setText(trim(goal.dailyNetGoal));
        if (goal.maxHours > 0) hoursInput.setText(trim(goal.maxHours));
    }

    private void save() {
        double goal = number(goalInput);
        double hours = number(hoursInput);
        if (goal <= 0) {
            Toast.makeText(this, "Informe uma meta maior que zero.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (hours < 0 || hours > 24) {
            Toast.makeText(this, "Informe um limite de horas entre 0 e 24.", Toast.LENGTH_SHORT).show();
            return;
        }
        DriverGoalStore.save(this, new DriverGoalStore.DriverGoal(goal, hours));
        Toast.makeText(this, "Meta salva", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void addField(LinearLayout parent, String label, EditText field) {
        TextView l = PremiumUi.text(this, label, 11, PremiumUi.MUTED, true);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(-1, -2);
        labelLp.setMargins(0, PremiumUi.dp(this, 14), 0, PremiumUi.dp(this, 6));
        parent.addView(l, labelLp);
        parent.addView(field, new LinearLayout.LayoutParams(-1, PremiumUi.dp(this, 50)));
    }

    private EditText numberField(String hint) {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setHint(hint);
        field.setTextSize(14);
        field.setTextColor(PremiumUi.TEXT);
        field.setHintTextColor(Color.rgb(152, 162, 179));
        field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        field.setPadding(PremiumUi.dp(this, 14), 0, PremiumUi.dp(this, 14), 0);
        field.setBackground(PremiumUi.shape(PremiumUi.SURFACE, PremiumUi.dp(this, 15), PremiumUi.BORDER, PremiumUi.dp(this, 1)));
        return field;
    }

    private double number(EditText input) {
        try {
            String raw = input.getText().toString().trim().replace(".", "").replace(',', '.');
            return raw.isEmpty() ? 0 : Double.parseDouble(raw);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String trim(double value) {
        if (Math.rint(value) == value) return String.format(ptBr, "%.0f", value);
        return String.format(ptBr, "%.2f", value);
    }
}
