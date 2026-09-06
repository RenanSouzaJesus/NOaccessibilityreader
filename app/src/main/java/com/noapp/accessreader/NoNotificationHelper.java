package com.noapp.accessreader;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.List;
import java.util.Locale;

/** Notificações próprias do NÓ para resumir oportunidades capturadas. */
public final class NoNotificationHelper {

    private static final String CHANNEL_ID = "no_opportunities";
    private static final int NOTIFICATION_ID = 9001;

    private NoNotificationHelper() {
    }

    public static void ensureChannel(Context context) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Oportunidades do NÓ",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Avisos quando o NÓ identifica oportunidades comparáveis.");
        manager.createNotificationChannel(channel);
    }

    public static void notifyOpportunity(Context context, Opportunity latest) {
        if (context == null || latest == null) return;
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ensureChannel(context);

        List<Opportunity> fresh = OpportunityStore.listFresh(context, 15 * 60 * 1000L);
        OpportunityRanker.RankingResult ranking = OpportunityRanker.rank(fresh);

        int rides = 0;
        int deliveries = 0;
        int routes = 0;
        for (Opportunity item : fresh) {
            if (Opportunity.TYPE_RIDE.equals(item.type)) rides++;
            else if (Opportunity.TYPE_DELIVERY.equals(item.type)) deliveries++;
            else if (Opportunity.TYPE_ROUTE.equals(item.type)) routes++;
        }

        String title = "NÓ • Novas oportunidades";
        String countText = String.format(Locale.getDefault(),
                "%d corridas, %d entregas e %d rotas disponíveis",
                rides, deliveries, routes);

        String detail;
        if (ranking != null && ranking.best != null) {
            Opportunity best = ranking.best;
            detail = String.format(new Locale("pt", "BR"),
                    "Melhor agora: %s • R$ %.2f • R$ %.2f/km",
                    best.platform,
                    best.price,
                    best.grossPerKm);
        } else {
            detail = String.format(new Locale("pt", "BR"),
                    "%s • R$ %.2f",
                    latest.platform,
                    latest.price);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_no_app)
                .setContentTitle(title)
                .setContentText(detail)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(countText + "\n" + detail + "\nToque para comparar no NÓ."))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .build();

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(NOTIFICATION_ID, notification);
    }
}
