package com.noapp.accessreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Mantém as áreas Oportunidades, Viagens, Campanhas e Ajustes sempre separadas. */
final class PremiumNavUpgrade {

    private PremiumNavUpgrade() {}

    static void install(Activity activity, String active) {
        View root = activity.findViewById(android.R.id.content);
        if (!(root instanceof ViewGroup)) return;

        TextView opportunities = findText((ViewGroup) root, "Oportunidades");
        TextView campaigns = findText((ViewGroup) root, "Campanhas");
        TextView settings = findText((ViewGroup) root, "Ajustes");
        TextView trips = findText((ViewGroup) root, "Viagens");

        ViewGroup nav = null;
        if (opportunities != null && opportunities.getParent() instanceof ViewGroup) {
            nav = (ViewGroup) opportunities.getParent();
        } else if (campaigns != null && campaigns.getParent() instanceof ViewGroup) {
            nav = (ViewGroup) campaigns.getParent();
        }

        if (nav != null && trips == null) {
            TextView item = PremiumUi.text(activity, "Viagens", 10,
                    "Viagens".equals(active) ? PremiumUi.NAVY : PremiumUi.MUTED, true);
            item.setGravity(Gravity.CENTER);
            item.setBackground(PremiumUi.shape(
                    "Viagens".equals(active) ? PremiumUi.CYAN_SOFT : Color.TRANSPARENT,
                    PremiumUi.dp(activity, 14), Color.TRANSPARENT, 0));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            int index = opportunities == null ? 0 : nav.indexOfChild(opportunities) + 1;
            nav.addView(item, Math.max(0, index), lp);
            trips = item;
        }

        if (opportunities != null) opportunities.setOnClickListener(v -> open(activity, OpportunitiesTripsActivity.class));
        if (trips != null) trips.setOnClickListener(v -> open(activity, TripsActivityV2.class));
        if (campaigns != null) campaigns.setOnClickListener(v -> open(activity, CampaignsTripsActivity.class));
        if (settings != null) settings.setOnClickListener(v -> showSettings(activity));

        fixCaptureStatus(activity, (ViewGroup) root);
    }

    private static void fixCaptureStatus(Activity activity, ViewGroup root) {
        boolean access = isTripAccessibilityEnabled(activity);
        boolean notifications = isNotificationListenerEnabled(activity);
        replaceText(root, "Tela desativada", access ? "Tela ativa" : "Tela desativada");

        TextView configure = findText(root, "CONFIGURAR");
        if (configure != null && access && notifications) {
            configure.setText("PRONTO");
            configure.setTextColor(PremiumUi.GREEN);
            configure.setBackground(PremiumUi.shape(Color.rgb(232, 250, 242),
                    PremiumUi.dp(activity, 999), Color.rgb(232, 250, 242), 0));
        }
    }

    private static void replaceText(ViewGroup group, String needle, String replacement) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                String value = tv.getText() == null ? "" : tv.getText().toString();
                if (value.contains(needle)) tv.setText(value.replace(needle, replacement));
            }
            if (child instanceof ViewGroup) replaceText((ViewGroup) child, needle, replacement);
        }
    }

    private static TextView findText(ViewGroup group, String exact) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView && exact.equals(((TextView) child).getText().toString())) {
                return (TextView) child;
            }
            if (child instanceof ViewGroup) {
                TextView found = findText((ViewGroup) child, exact);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void open(Activity activity, Class<?> target) {
        if (target.isInstance(activity)) return;
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }

    private static void showSettings(Activity activity) {
        String[] options = {
                "Acessibilidade — " + (isTripAccessibilityEnabled(activity) ? "Ativa" : "Desativada"),
                "Alertas dos aplicativos — " + (isNotificationListenerEnabled(activity) ? "Ativos" : "Desativados")
        };
        new AlertDialog.Builder(activity)
                .setTitle("Conexões do NÓ")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) activity.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    else activity.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                })
                .setNegativeButton("Fechar", null)
                .show();
    }

    private static boolean isTripAccessibilityEnabled(Activity activity) {
        String expected = new ComponentName(activity, TripAwareAccessibilityService.class).flattenToString();
        String enabled = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return containsComponent(enabled, expected);
    }

    private static boolean isNotificationListenerEnabled(Activity activity) {
        String expected = new ComponentName(activity, NoNotificationListenerService.class).flattenToString();
        String enabled = Settings.Secure.getString(activity.getContentResolver(), "enabled_notification_listeners");
        return containsComponent(enabled, expected);
    }

    private static boolean containsComponent(String enabled, String expected) {
        if (TextUtils.isEmpty(enabled) || TextUtils.isEmpty(expected)) return false;
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);
        while (splitter.hasNext()) if (expected.equalsIgnoreCase(splitter.next())) return true;
        return false;
    }
}
