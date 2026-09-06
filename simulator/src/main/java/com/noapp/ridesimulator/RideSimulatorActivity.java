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

    private static final int IFOOD_RED = Color.rgb(234, 29, 44);
    private static final int ML_YELLOW = Color.rgb(255, 220, 0);
    private static final int SHOPEE_ORANGE = Color.rgb(238, 77, 45);

    private static final String PUSH_CHANNEL = "ride_simulator_push";
    private static final int REQUEST_PUSH = 77;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensurePushChannel();
        showHome();
    }

    private void showHome() {
        int dark = Color.rgb(15, 15, 15);
        getWindow().setStatusBarColor(dark);
        getWindow().setNavigationBarColor(dark);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        root.setBackgroundColor(dark);

        root.addView(text("Simulador de Oportunidades", 30, Color.WHITE, true));

        TextView subtitle = text(
                "Telas locais para testar como o NÓ reúne corridas, entregas e rotas sem depender dos aplicativos reais.",
                15,
                Color.rgb(185, 185, 185),
                false
        );
        subtitle.setPadding(0, dp(8), 0, dp(14));
        root.addView(subtitle);

        root.addView(homeButton("Uber • UberX", UBER_WHITE, UBER_BLACK,
                v -> showUberOffer("28,40", "3,1", "6,2", "14")));

        root.addView(homeButton("99 • 99Pop", NINE_YELLOW, NINE_BLACK,
                v -> show99Offer("31,20", "2,4", "7,0", "16")));

        root.addView(homeButton("iFood • Pedido", IFOOD_RED, Color.WHITE,
                v -> showIfoodOffer()));

        root.addView(homeButton("Mercado Livre • Rota", ML_YELLOW, Color.rgb(35, 35, 35),
                v -> showMercadoLivreRoute()));

        root.addView(homeButton("Shopee • Pacote", SHOPEE_ORANGE, Color.WHITE,
                v -> showShopeeOffer()));

        TextView pushTitle = text("Teste de notificações", 17, Color.WHITE, true);
        pushTitle.setPadding(0, dp(24), 0, dp(4));
        root.addView(pushTitle);

        TextView pushInfo = text(
                "Primeiro simule um push. Depois abra a tela correspondente acima. Assim você testa alerta + leitura completa + central do NÓ.",
                13,
                Color.rgb(165, 165, 165),
                false
        );
        pushInfo.setPadding(0, 0, 0, dp(4));
        root.addView(pushInfo);

        root.addView(outlineButton("Push Uber", Color.WHITE, v -> postMockNotification("Uber")));
        root.addView(outlineButton("Push 99", NINE_YELLOW, v -> postMockNotification("99")));
        root.addView(outlineButton("Push iFood", IFOOD_RED, v -> postMockNotification("iFood")));
        root.addView(outlineButton("Push Mercado Livre", ML_YELLOW, v -> postMockNotification("Mercado Livre")));
        root.addView(outlineButton("Push Shopee", SHOPEE_ORANGE, v -> postMockNotification("Shopee")));

        TextView info = text(
                "Mantenha a acessibilidade do NÓ ativa. O HUD deve permanecer até você tocar em OK. As oportunidades ficam salvas por alguns minutos na Central.",
                14,
                Color.rgb(165, 165, 165),
                false
        );
        info.setPadding(0, dp(22), 0, 0);
        root.addView(info);

        setContentView(wrap(root, dark));
    }

    private void postMockNotification(String platform) {
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

        String title;
        String body;
        int id;

        switch (platform) {
            case "Uber":
                title = "Uber Driver";
                body = "Nova corrida disponível! Toque para ver os detalhes.";
                id = 1101;
                break;
            case "99":
                title = "99 Motorista";
                body = "Nova corrida disponível! Toque para ver os detalhes.";
                id = 1102;
                break;
            case "iFood":
                title = "iFood Entregador";
                body = "Novo pedido disponível! Toque para ver os detalhes.";
                id = 1103;
                break;
            case "Mercado Livre":
                title = "Mercado Livre";
                body = "Nova rota disponível! Toque para ver os detalhes.";
                id = 1104;
                break;
            default:
                title = "Shopee Entregas";
                body = "Novo pacote disponível! Toque para ver os detalhes.";
                id = 1105;
                break;
        }

        Notification notification = new Notification.Builder(this, PUSH_CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .build();

        manager.notify(id, notification);
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
        channel.setDescription("Notificações fictícias para testes do NÓ.");
        manager.createNotificationChannel(channel);
    }

    private void showUberOffer(String price, String pickupKm, String tripKm, String tripMin) {
        getWindow().setStatusBarColor(UBER_BLACK);
        getWindow().setNavigationBarColor(UBER_BLACK);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(UBER_MAP);

        FrameLayout map = fakeMap("Centro", UBER_BLUE, UBER_MAP);
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
        sheet.addView(backToHomeText(UBER_MUTED));

        screen.addView(sheet);
        setContentView(wrap(screen, UBER_MAP));
    }

    private void show99Offer(String price, String pickupKm, String tripKm, String tripMin) {
        getWindow().setStatusBarColor(NINE_YELLOW);
        getWindow().setNavigationBarColor(NINE_BLACK);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(NINE_BG);
        screen.addView(fakeMap("99 • Centro", NINE_ORANGE, Color.rgb(233, 233, 233)));

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

        sheet.addView(button("Aceitar corrida", NINE_YELLOW, NINE_BLACK, v -> { }));
        sheet.addView(outlineButton("Recusar", NINE_BLACK, v -> showHome()));
        sheet.addView(outlineButton("Editar cenário de teste", NINE_ORANGE,
                v -> showEditor(false, price, pickupKm, tripKm, tripMin)));
        sheet.addView(backToHomeText(NINE_MUTED));

        screen.addView(sheet);
        setContentView(wrap(screen, NINE_BG));
    }

    private void showIfoodOffer() {
        getWindow().setStatusBarColor(IFOOD_RED);
        getWindow().setNavigationBarColor(Color.rgb(30, 30, 30));

        LinearLayout root = brandedOpportunityRoot(Color.rgb(248, 248, 248));
        root.addView(brandedHeader("iFood Entregador", IFOOD_RED, Color.WHITE));
        root.addView(fakeMap("Retirada → Entrega", IFOOD_RED, Color.rgb(235, 235, 235)));

        LinearLayout card = whiteCard();
        card.addView(text("Novo pedido", 18, IFOOD_RED, true));
        card.addView(bigPrice("R$ 12,90", Color.rgb(30, 30, 30)));
        card.addView(infoLine("1,4 km até a retirada"));
        card.addView(infoLine("McDonald's • Av. Rebouças, 3970"));
        card.addView(infoLine("3,2 km até a entrega"));
        card.addView(infoLine("Cliente • Rua Cardoso de Almeida, 840"));
        card.addView(button("Aceitar pedido", IFOOD_RED, Color.WHITE, v -> { }));
        card.addView(outlineButton("Voltar", Color.rgb(45, 45, 45), v -> showHome()));
        root.addView(card);

        setContentView(wrap(root, Color.rgb(248, 248, 248)));
    }

    private void showMercadoLivreRoute() {
        getWindow().setStatusBarColor(ML_YELLOW);
        getWindow().setNavigationBarColor(Color.rgb(35, 35, 35));

        LinearLayout root = brandedOpportunityRoot(Color.rgb(247, 247, 247));
        root.addView(brandedHeader("Mercado Livre", ML_YELLOW, Color.rgb(35, 35, 35)));

        LinearLayout card = whiteCard();
        card.addView(text("Nova rota de entregas", 18, Color.rgb(50, 50, 50), true));
        card.addView(bigPrice("R$ 285,00", Color.rgb(25, 25, 25)));
        card.addView(infoLine("82 entregas"));
        card.addView(infoLine("Coleta: Barueri - SP"));
        card.addView(infoLine("Entrega: Cotia e região - SP"));
        card.addView(infoLine("Estimativa: 6h"));
        card.addView(infoLine("Distância total: 98 km"));
        card.addView(infoLine("Tipo: Pacotes e envelopes"));
        card.addView(button("Aceitar rota", Color.rgb(35, 35, 35), Color.WHITE, v -> { }));
        card.addView(outlineButton("Voltar", Color.rgb(45, 45, 45), v -> showHome()));
        root.addView(card);

        setContentView(wrap(root, Color.rgb(247, 247, 247)));
    }

    private void showShopeeOffer() {
        getWindow().setStatusBarColor(SHOPEE_ORANGE);
        getWindow().setNavigationBarColor(Color.rgb(35, 35, 35));

        LinearLayout root = brandedOpportunityRoot(Color.rgb(248, 248, 248));
        root.addView(brandedHeader("Shopee Entregas", SHOPEE_ORANGE, Color.WHITE));
        root.addView(fakeMap("Coleta → Cliente", SHOPEE_ORANGE, Color.rgb(237, 237, 237)));

        LinearLayout card = whiteCard();
        card.addView(text("Novo pacote", 18, SHOPEE_ORANGE, true));
        card.addView(bigPrice("R$ 9,50", Color.rgb(30, 30, 30)));
        card.addView(infoLine("1,1 km até a coleta"));
        card.addView(infoLine("Centro de Distribuição Shopee • Osasco - SP"));
        card.addView(infoLine("4,8 km até a entrega"));
        card.addView(infoLine("Cliente • Rua das Palmeiras, 320"));
        card.addView(button("Aceitar pacote", SHOPEE_ORANGE, Color.WHITE, v -> { }));
        card.addView(outlineButton("Voltar", Color.rgb(45, 45, 45), v -> showHome()));
        root.addView(card);

        setContentView(wrap(root, Color.rgb(248, 248, 248)));
    }

    private FrameLayout fakeMap(String label, int accent, int bg) {
        FrameLayout map = new FrameLayout(this);
        map.setBackgroundColor(bg);
        map.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(215)));

        TextView title = text(label, 19, Color.rgb(80, 88, 95), true);
        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(-2, -2);
        titleLp.leftMargin = dp(22);
        titleLp.topMargin = dp(34);
        map.addView(title, titleLp);

        TextView street1 = text("Av. Brasil", 13, Color.rgb(128, 136, 142), false);
        FrameLayout.LayoutParams s1 = new FrameLayout.LayoutParams(-2, -2);
        s1.leftMargin = dp(42);
        s1.topMargin = dp(105);
        map.addView(street1, s1);

        TextView route = text("●  ━━━━━━━  ●", 24, accent, true);
        FrameLayout.LayoutParams routeLp = new FrameLayout.LayoutParams(-2, -2);
        routeLp.gravity = Gravity.CENTER;
        map.addView(route, routeLp);

        return map;
    }

    private LinearLayout brandedOpportunityRoot(int background) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);
        return root;
    }

    private View brandedHeader(String title, int bg, int fg) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(16), dp(18), dp(16));
        header.setBackgroundColor(bg);

        header.addView(text(title, 22, fg, true));
        header.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(1), 1f));
        TextView close = text("×", 30, fg, false);
        close.setOnClickListener(v -> showHome());
        header.addView(close);
        return header;
    }

    private LinearLayout whiteCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(18), dp(20), dp(24));
        card.setBackground(roundRect(Color.WHITE, dp(20), Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(12), dp(12), dp(12), dp(20));
        card.setLayoutParams(lp);
        return card;
    }

    private TextView bigPrice(String value, int color) {
        TextView price = text(value, 38, color, true);
        price.setPadding(0, dp(8), 0, dp(10));
        return price;
    }

    private TextView infoLine(String value) {
        TextView line = text(value, 16, Color.rgb(45, 45, 45), false);
        line.setPadding(0, dp(6), 0, dp(6));
        return line;
    }

    private TextView backToHomeText(int color) {
        TextView lab = text("Simulação local • nenhuma oportunidade real será aceita", 12, color, false);
        lab.setGravity(Gravity.CENTER);
        lab.setPadding(0, dp(12), 0, 0);
        return lab;
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(60));
        lp.setMargins(0, dp(10), 0, 0);
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
        button.setBackground(roundRect(Color.TRANSPARENT, dp(12), Color.rgb(120, 120, 120), dp(1)));
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
