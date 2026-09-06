package com.noapp.accessreader;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Observa notificações autorizadas pelo usuário.
 * A notificação funciona como sinal de que há uma oportunidade nova;
 * quando o texto trouxer valor/distância/tempo suficientes, ela também
 * pode virar uma oportunidade completa no inbox do NÓ.
 */
public class NoNotificationListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;
        if (getPackageName().equals(sbn.getPackageName())) return;

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        if (extras == null) return;

        String title = asText(extras.getCharSequence(Notification.EXTRA_TITLE));
        String text = asText(extras.getCharSequence(Notification.EXTRA_TEXT));
        String bigText = asText(extras.getCharSequence(Notification.EXTRA_BIG_TEXT));
        String raw = title + "\n" + text + "\n" + bigText;

        String platform = UnifiedOpportunityParser.detectPlatform(raw, sbn.getPackageName());
        if (platform == null) return;

        getSharedPreferences("no_accessibility", MODE_PRIVATE)
                .edit()
                .putLong("source_notification_time", System.currentTimeMillis())
                .putString("source_notification_platform", platform)
                .putString("source_notification_title", title)
                .putString("source_notification_text", text.isEmpty() ? bigText : text)
                .putString("source_notification_package", sbn.getPackageName())
                .apply();

        Opportunity opportunity = UnifiedOpportunityParser.parse(
                raw,
                sbn.getPackageName(),
                "NOTIFICATION",
                System.currentTimeMillis()
        );

        if (opportunity == null) return;

        boolean isNew = OpportunityStore.upsert(this, opportunity);
        if (isNew) NoNotificationHelper.notifyOpportunity(this, opportunity);
    }

    private String asText(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
