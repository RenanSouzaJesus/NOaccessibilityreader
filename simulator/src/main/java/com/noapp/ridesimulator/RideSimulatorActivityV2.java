package com.noapp.ridesimulator;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Evolui o simulador para testar o fluxo oferta -> aceita -> concluída.
 * As telas de aceite contêm sinais que o NÓ consegue reconhecer via acessibilidade.
 */
public class RideSimulatorActivityV2 extends RideSimulatorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wireCurrentScreen();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        if (view != null) view.post(this::wireCurrentScreen);
    }

    private void wireCurrentScreen() {
        View root = findViewById(android.R.id.content);
        if (!(root instanceof ViewGroup)) return;
        wireButtons((ViewGroup) root);
    }

    private void wireButtons(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                String label = button.getText() == null ? "" : button.getText().toString();
                if ("Aceitar".equals(label)) {
                    button.setOnClickListener(v -> showAccepted("Uber"));
                } else if ("Aceitar corrida".equals(label)) {
                    button.setOnClickListener(v -> showAccepted("99"));
                }
            }
            if (child instanceof ViewGroup) wireButtons((ViewGroup) child);
        }
    }

    private void showAccepted(String platform) {
        boolean uber = "Uber".equals(platform);
        int accent = uber ? Color.BLACK : Color.rgb(255, 214, 0);
        int fg = uber ? Color.WHITE : Color.rgb(25, 25, 25);
        int bg = uber ? Color.rgb(242, 244, 246) : Color.rgb(246, 246, 246);

        getWindow().setStatusBarColor(accent);
        getWindow().setNavigationBarColor(Color.rgb(20, 20, 20));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(24), dp(20), dp(28));
        page.setBackgroundColor(bg);

        TextView brand = text(uber ? "Uber Driver" : "99 Motorista", 24, fg, true);
        brand.setPadding(dp(14), dp(12), dp(14), dp(12));
        brand.setBackground(shape(accent, dp(14)));
        page.addView(brand);

        TextView category = text(uber ? "UberX" : "99Pop", 18, Color.rgb(30, 30, 30), true);
        category.setPadding(0, dp(24), 0, 0);
        page.addView(category);

        TextView accepted = text("Corrida aceita", 30, Color.rgb(25, 25, 25), true);
        accepted.setPadding(0, dp(8), 0, 0);
        accepted.setContentDescription("Corrida aceita");
        page.addView(accepted);

        TextView state = text("A caminho do passageiro", 18, Color.rgb(70, 70, 70), false);
        state.setPadding(0, dp(8), 0, 0);
        state.setContentDescription("A caminho do passageiro");
        page.addView(state);

        TextView nav = text("Navegar até o passageiro", 15, Color.rgb(90, 90, 90), false);
        nav.setPadding(0, dp(8), 0, dp(18));
        page.addView(nav);

        Button finish = button("Finalizar viagem", accent, fg);
        finish.setOnClickListener(v -> showCompleted(platform));
        page.addView(finish);

        Button back = outlineButton("Voltar ao simulador");
        back.setOnClickListener(v -> recreate());
        page.addView(back);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);
        scroll.addView(page);
        super.setContentView(scroll);
    }

    private void showCompleted(String platform) {
        boolean uber = "Uber".equals(platform);
        int accent = uber ? Color.BLACK : Color.rgb(255, 214, 0);
        int fg = uber ? Color.WHITE : Color.rgb(25, 25, 25);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(22), dp(42), dp(22), dp(30));
        page.setBackgroundColor(Color.rgb(247, 247, 247));

        TextView brand = text(uber ? "Uber Driver • UberX" : "99 Motorista • 99Pop", 17, Color.rgb(55, 55, 55), true);
        page.addView(brand);

        TextView done = text("Viagem concluída", 30, Color.rgb(24, 24, 24), true);
        done.setPadding(0, dp(22), 0, 0);
        done.setContentDescription("Viagem concluída");
        page.addView(done);

        TextView gains = text("Ganhos da viagem registrados", 16, Color.rgb(85, 85, 85), false);
        gains.setPadding(0, dp(8), 0, dp(22));
        gains.setContentDescription("Ganhos da viagem");
        page.addView(gains);

        Button back = button("Voltar ao simulador", accent, fg);
        back.setOnClickListener(v -> recreate());
        page.addView(back);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(247, 247, 247));
        scroll.addView(page);
        super.setContentView(scroll);
    }

    private Button button(String label, int bg, int fg) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(fg);
        b.setBackground(shape(bg, dp(14)));
        b.setStateListAnimator(null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        lp.setMargins(0, dp(10), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    private Button outlineButton(String label) {
        Button b = button(label, Color.TRANSPARENT, Color.rgb(40, 40, 40));
        GradientDrawable d = shape(Color.TRANSPARENT, dp(14));
        d.setStroke(dp(1), Color.rgb(190, 190, 190));
        b.setBackground(d);
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setIncludeFontPadding(false);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return tv;
    }

    private GradientDrawable shape(int color, float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
