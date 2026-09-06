package com.noapp.ridesimulator;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

public class RideSimulatorActivity extends Activity {

    private static final int UBER_BLACK = Color.rgb(0, 0, 0);
    private static final int UBER_WHITE = Color.rgb(255, 255, 255);
    private static final int UBER_MAP = Color.rgb(232, 237, 240);
    private static final int UBER_MUTED = Color.rgb(95, 99, 104);
    private static final int UBER_BLUE = Color.rgb(39, 110, 241);

    private static final int NINE_YELLOW = Color.rgb(255, 214, 0);
    private static final int NINE_ORANGE = Color.rgb(255, 145, 0);
    private static final int NINE_BLACK = Color.rgb(28, 28, 28);
    private static final int NINE_WHITE = Color.rgb(255, 255, 255);
    private static final int NINE_BG = Color.rgb(244, 244, 244);
    private static final int NINE_MUTED = Color.rgb(101, 101, 101);
    private static final int NINE_GREEN = Color.rgb(24, 151, 101);

    private static final String PUSH_CHANNEL = "ride_simulator_push";
    private static final int REQUEST_PUSH = 77;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensurePushChannel();
        showHome();
    }

    private void showHome() {
        getWindow().setStatusBarColor(Color.rgb(15, 15, 15));
        getWindow().setNavigationBarColor(Color.rgb(15, 15, 15));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        root.setBackgroundColor(Color.rgb(15, 15, 15));

        root.addView(text("Simulador de Corridas", 30, Color.WHITE, true));

        TextView subtitle = text(
                "Escolha uma plataforma. A tela da Uber imita a Uber; a tela da 99 imita a 99. O overlay continua com a identidade visual do NÓ.",
                15,
                Color.rgb(185, 185, 185),
                false
        );
        subtitle.setPadding(0, dp(8), 0, dp(16));
        root.addView(subtitle);

        root.addView(homeButton("Uber • UberX", UBER_WHITE, UBER_BLACK,
                v -> showUberOffer("28,40", "3,1", "6,2", "14")));

        root.addView(homeButton("99 • 99Pop", NINE_YELLOW, NINE_BLACK,
                v -> show99Offer("31,20", "2,4", "7,0", "16")));

        TextView pushTitle = text("Teste de notificações", 17, Color.WHITE, true);
        pushTitle.setPadding(0, dp(24), 0, dp(4));
        root.addView(pushTitle);

        TextView pushInfo = text(
                "Use estes botões para simular o aviso que chega antes da tela da oportunidade. Depois abra a oferta acima para o NÓ capturar os dados completos.",
                13,
                Color.rgb(165, 165, 165),
                false
        );
        pushInfo.setPadding(0, 0, 0, dp(4));
        root.addView(pushInfo);

        root.addView(outlineButton("Simular push da Uber", Color.WHITE,
                v -> postMockNotification(true)));
        root.addView(outlineButton("Simular push da 99", NINE_YELLOW,
                v -> postMockNotification(false)));

        TextView info = text(
                "Mantenha a acessibilidade do NÓ ativa. Ao abrir uma oferta, o HUD do NÓ deve aparecer por cima e permanecer até você tocar em OK.",
                14,
                Color.rgb(165, 165, 165),
                false
        );
        info.setPadding(0, dp(24), 0, 0);
        root.addView(info);

        setContentView(wrap(root, Color.rgb(15, 15, 15)));
    }

    private void postMockNotification(boolean uber) {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_PUSH);
            return;
        }

        ensurePushChannel();
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        String title = uber ? "Uber Driver" : "99 Motorista";
        String body = uber
                ? "Nova corrida disponível! Toque para ver os detalhes."
                : "Nova corrida disponível! Toque para ver os detalhes.";

        Notification notification = new Notification.Builder(this, PUSH_CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .build();

        manager.notify(uber ? 1101 : 1102, notification);
    }

    private void ensurePushChannel() {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        NotificationChannel channel = new NotificationChannel(
                PUSH_CHANNEL,
                "Simulação de oportunidades",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notificações fictícias da Uber e 99 para testes do NÓ.");
        manager.createNotificationChannel(channel);
    }

    private void showUberOffer(String price, String pickupKm, String tripKm, String tripMin) {
        getWindow().setStatusBarColor(UBER_BLACK);
        getWindow().setNavigationBarColor(UBER_BLACK);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(UBER_MAP);

        FrameLayout map = new FrameLayout(this);
        map.setBackgroundColor(UBER_MAP);
        map.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(250)));

        TextView mapTitle = text("Centro", 20, Color.rgb(90, 100, 108), true);
        FrameLayout.LayoutParams mt = new FrameLayout.LayoutParams(-2, -2);
        mt.leftMargin = dp(22);
        mt.topMargin = dp(44);
        map.addView(mapTitle, mt);

        TextView street1 = text("Av. Brasil", 13, Color.rgb(128, 136, 142), false);
        FrameLayout.LayoutParams s1 = new FrameLayout.LayoutParams(-2, -2);
        s1.leftMargin = dp(40);
        s1.topMargin = dp(112);
        map.addView(street1, s1);

        TextView street2 = text("Rua das Flores", 13, Color.rgb(128, 136, 142), false);
        FrameLayout.LayoutParams s2 = new FrameLayout.LayoutParams(-2, -2);
        s2.rightMargin = dp(34);
        s2.topMargin = dp(170);
        s2.gravity = Gravity.END;
        map.addView(street2, s2);

        TextView driverDot = text("●", 26, UBER_BLUE, true);
        FrameLayout.LayoutParams d = new FrameLayout.LayoutParams(-2, -2);
        d.leftMargin = dp(155);
        d.topMargin = dp(105);
        map.addView(driverDot, d);

        TextView pickupDot = text("●", 20, UBER_BLACK, true);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(-2, -2);
        p.rightMargin = dp(72);
        p.bottomMargin = dp(34);
        p.gravity = Gravity.END | Gravity.BOTTOM;
        map.addView(pickupDot, p);

        screen.addView(map);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(22), dp(16), dp(22), dp(24));
        sheet.setBackground(roundRect(UBER_WHITE, dp(24), Color.TRANSPARENT, 0));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = text("UberX", 15, UBER_WHITE, true);
        badge.setPadding(dp(12), dp(7), dp(12), dp(7));
        badge.setBackground(roundRect(UBER_BLACK, dp(8), Color.TRANSPARENT, 0));
        top.addView(badge);
        top.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));

        TextView close = text("×", 34, UBER_BLACK, false);
        close.setGravity(Gravity.CENTER);
        close.setBackground(roundRect(Color.rgb(245, 245, 245), dp(12), Color.TRANSPARENT, 0));
        close.setOnClickListener(v -> showHome());
        top.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        sheet.addView(top);

        TextView priceView = text("R$ " + price, 43, UBER_BLACK, true);
        priceView.setPadding(0, dp(8), 0, 0);
        sheet.addView(priceView);

        TextView rating = text("★ 4,85", 18, UBER_BLACK, false);
        rating.setContentDescription("Avaliação do passageiro 4,85");
        rating.setPadding(0, dp(8), 0, dp(14));
        sheet.addView(rating);

        sheet.addView(divider(Color.rgb(232, 232, 232)));

        TextView pickup = text(
                pickupKm + " km até o passageiro\nRua das Flores, 123 - Centro",
                17,
                UBER_BLACK,
                false
        );
        pickup.setContentDescription("Distância até o passageiro: " + pickupKm + " quilômetros");
        pickup.setLineSpacing(dp(4), 1f);
        pickup.setPadding(0, dp(16), 0, dp(12));
        sheet.addView(pickup);

        TextView trip = text(
                tripKm + " km • " + tripMin + " min de viagem\nDestino: Shopping Sul",
                17,
                UBER_BLACK,
                false
        );
        trip.setContentDescription("Viagem: " + tripKm + " quilômetros e " + tripMin + " minutos");
        trip.setLineSpacing(dp(4), 1f);
        trip.setPadding(0, 0, 0, dp(18));
        sheet.addView(trip);

        Button accept = button("Aceitar", UBER_BLACK, UBER_WHITE, v -> { });
        accept.setContentDescription("Botão simulado de aceitar corrida");
        sheet.addView(accept);

        sheet.addView(outlineButton("Editar cenário de teste", UBER_BLACK,
                v -> showEditor(true, price, pickupKm, tripKm, tripMin)));

        TextView lab = text("Simulação local • nenhuma corrida real será aceita", 12, UBER_MUTED, false);
        lab.setGravity(Gravity.CENTER);
        lab.setPadding(0, dp(12), 0, 0);
        sheet.addView(lab);

        screen.addView(sheet);
        setContentView(wrap(screen, UBER_MAP));
    }

    private void show99Offer(String price, String pickupKm, String tripKm, String tripMin) {
        getWindow().setStatusBarColor(NINE_YELLOW);
        getWindow().setNavigationBarColor(NINE_BLACK);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(NINE_BG);

        FrameLayout map = new FrameLayout(this);
        map.setBackgroundColor(Color.rgb(233, 233, 233));
        map.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(225)));

        TextView topBrand = text("99", 32, NINE_BLACK, true);
        FrameLayout.LayoutParams brandLp = new FrameLayout.LayoutParams(-2, -2);
        brandLp.leftMargin = dp(18);
        brandLp.topMargin = dp(22);
        map.addView(topBrand, brandLp);

        TextView route = text("●  ━━━━━━━  ●", 24, NINE_ORANGE, true);
        FrameLayout.LayoutParams routeLp = new FrameLayout.LayoutParams(-2, -2);
        routeLp.gravity = Gravity.CENTER;
        map.addView(route, routeLp);

        TextView area = text("Centro • região da oferta", 14, NINE_MUTED, false);
        FrameLayout.LayoutParams areaLp = new FrameLayout.LayoutParams(-2, -2);
        areaLp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        areaLp.bottomMargin = dp(28);
        map.addView(area, areaLp);

        screen.addView(map);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(20), dp(16), dp(20), dp(24));
        sheet.setBackground(roundRect(NINE_WHITE, dp(22), Color.TRANSPARENT, 0));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        header.addView(text("99Pop", 23, NINE_BLACK, true));
        header.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));

        TextView timer = text("15 s", 14, NINE_BLACK, true);
        timer.setPadding(dp(12), dp(7), dp(12), dp(7));
        timer.setBackground(roundRect(NINE_YELLOW, dp(999), Color.TRANSPARENT, 0));
        header.addView(timer);
        sheet.addView(header);

        TextView priceView = text("R$ " + price, 40, NINE_BLACK, true);
        priceView.setPadding(0, dp(10), 0, dp(2));
        sheet.addView(priceView);

        TextView earning = text("Valor estimado que você recebe", 13, NINE_MUTED, false);
        earning.setPadding(0, 0, 0, dp(10));
        sheet.addView(earning);

        TextView rating = text("★ 4,78  •  890+ viagens", 16, NINE_BLACK, false);
        rating.setContentDescription("Avaliação do passageiro 4,78");
        rating.setPadding(0, 0, 0, dp(14));
        sheet.addView(rating);

        sheet.addView(divider(Color.rgb(235, 235, 235)));

        TextView pickup = text(
                pickupKm + " km até o passageiro\nAv. Brasil, 456 - Centro",
                17,
                NINE_BLACK,
                false
        );
        pickup.setContentDescription("Distância até o passageiro: " + pickupKm + " quilômetros");
        pickup.setLineSpacing(dp(4), 1f);
        pickup.setPadding(0, dp(15), 0, dp(12));
        sheet.addView(pickup);

        TextView trip = text(
                tripKm + " km • " + tripMin + " min de viagem\nDestino: Mercado Municipal",
                17,
                NINE_BLACK,
                false
        );
        trip.setContentDescription("Viagem: " + tripKm + " quilômetros e " + tripMin + " minutos");
        trip.setLineSpacing(dp(4), 1f);
        trip.setPadding(0, 0, 0, dp(14));
        sheet.addView(trip);

        TextView protectedValue = text("✓ Valor da corrida visível antes do aceite", 13, NINE_GREEN, true);
        protectedValue.setPadding(0, 0, 0, dp(14));
        sheet.addView(protectedValue);

        Button accept = button("Aceitar corrida", NINE_YELLOW, NINE_BLACK, v -> { });
        accept.setContentDescription("Botão simulado de aceitar corrida");
        sheet.addView(accept);

        sheet.addView(outlineButton("Recusar", NINE_BLACK, v -> showHome()));
        sheet.addView(outlineButton("Editar cenário de teste", NINE_ORANGE,
                v -> showEditor(false, price, pickupKm, tripKm, tripMin)));

        TextView lab = text("Simulação local • nenhuma corrida real será aceita", 12, NINE_MUTED, false);
        lab.setGravity(Gravity.CENTER);
        lab.setPadding(0, dp(12), 0, 0);
        sheet.addView(lab);

        screen.addView(sheet);
        setContentView(wrap(screen, NINE_BG));
    }

    private void showEditor(boolean uber, String price, String pickupKm, String tripKm, String tripMin) {
        int bg = uber ? UBER_WHITE : NINE_BG;
        int fg = uber ? UBER_BLACK : NINE_BLACK;
        int accent = uber ? UBER_BLACK : NINE_YELLOW;
        int accentText = uber ? UBER_WHITE : NINE_BLACK;

        getWindow().setStatusBarColor(uber ? UBER_BLACK : NINE_YELLOW);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        root.setBackgroundColor(bg);

        root.addView(text(uber ? "Editar UberX" : "Editar 99Pop", 28, fg, true));
        TextView helper = text("Altere os valores para gerar uma nova oferta e testar o parser/overlay.", 14, Color.rgb(105, 105, 105), false);
        helper.setPadding(0, dp(6), 0, dp(14));
        root.addView(helper);

        EditText priceInput = input("Valor em R$", price, fg);
        EditText pickupInput = input("Km até o passageiro", pickupKm, fg);
        EditText tripInput = input("Km da viagem", tripKm, fg);
        EditText minInput = input("Minutos da viagem", tripMin, fg);
        root.addView(priceInput);
        root.addView(pickupInput);
        root.addView(tripInput);
        root.addView(minInput);

        root.addView(button("Atualizar oferta", accent, accentText, v -> {
            String p = valueOr(priceInput, price);
            String pk = valueOr(pickupInput, pickupKm);
            String tk = valueOr(tripInput, tripKm);
            String tm = valueOr(minInput, tripMin);
            if (uber) showUberOffer(p, pk, tk, tm);
            else show99Offer(p, pk, tk, tm);
        }));

        root.addView(outlineButton("Voltar sem alterar", fg, v -> {
            if (uber) showUberOffer(price, pickupKm, tripKm, tripMin);
            else show99Offer(price, pickupKm, tripKm, tripMin);
        }));

        setContentView(wrap(root, bg));
    }

    private Button homeButton(String label, int bg, int fg, View.OnClickListener listener) {
        Button button = button(label, bg, fg, listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(64));
        lp.setMargins(0, dp(12), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private Button button(String label, int bg, int fg, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(fg);
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        button.setBackground(roundRect(bg, dp(12), Color.TRANSPARENT, 0));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        lp.setMargins(0, dp(8), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private Button outlineButton(String label, int fg, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(fg);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        button.setBackground(roundRect(Color.TRANSPARENT, dp(12), Color.rgb(210, 210, 210), dp(1)));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(50));
        lp.setMargins(0, dp(8), 0, 0);
        button.setLayoutParams(lp);
        return button;
    }

    private EditText input(String hint, String value, int fg) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(value);
        input.setTextColor(fg);
        input.setHintTextColor(Color.rgb(135, 135, 135));
        input.setTextSize(17);
        input.setSingleLine(true);
        input.setPadding(dp(14), 0, dp(14), 0);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setBackground(roundRect(Color.rgb(245, 245, 245), dp(10), Color.rgb(220, 220, 220), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        lp.setMargins(0, dp(10), 0, 0);
        input.setLayoutParams(lp);
        return input;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(color);
        tv.setTextSize(size);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tv.setIncludeFontPadding(false);
        return tv;
    }

    private View divider(int color) {
        View view = new View(this);
        view.setBackgroundColor(color);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1)));
        return view;
    }

    private ScrollView wrap(View root, int bg) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);
        scroll.setFillViewport(true);
        scroll.addView(root);
        return scroll;
    }

    private GradientDrawable roundRect(int fill, float radius, int stroke, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) drawable.setStroke(strokeWidth, stroke);
        return drawable;
    }

    private String valueOr(EditText input, String fallback) {
        String s = input.getText().toString().trim();
        return s.isEmpty() ? fallback : s;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
