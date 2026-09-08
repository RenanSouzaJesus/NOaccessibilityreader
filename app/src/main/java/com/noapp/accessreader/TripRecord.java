package com.noapp.accessreader;

import java.util.Locale;

/** Registro de uma corrida realmente aceita pelo motorista. */
public final class TripRecord {

    public static final String STATUS_ACCEPTED = "ACEITA";
    public static final String STATUS_COMPLETED = "CONCLUÍDA";

    public final String sourceKey;
    public final String platform;
    public final String category;
    public final double price;
    public final double pickupKm;
    public final double routeKm;
    public final int minutes;
    public final double totalKm;
    public final double grossPerKm;
    public final double grossPerHour;
    public final long acceptedAt;
    public final long completedAt;
    public final String status;

    public TripRecord(
            String sourceKey,
            String platform,
            String category,
            double price,
            double pickupKm,
            double routeKm,
            int minutes,
            double totalKm,
            double grossPerKm,
            double grossPerHour,
            long acceptedAt,
            long completedAt,
            String status
    ) {
        this.sourceKey = sourceKey == null ? "" : sourceKey;
        this.platform = platform == null ? "Aplicativo" : platform;
        this.category = category == null ? "Corrida" : category;
        this.price = price;
        this.pickupKm = pickupKm;
        this.routeKm = routeKm;
        this.minutes = minutes;
        this.totalKm = totalKm;
        this.grossPerKm = grossPerKm;
        this.grossPerHour = grossPerHour;
        this.acceptedAt = acceptedAt;
        this.completedAt = completedAt;
        this.status = status == null ? STATUS_ACCEPTED : status;
    }

    public static TripRecord fromOpportunity(Opportunity opportunity, long acceptedAt) {
        if (opportunity == null) return null;
        return new TripRecord(
                opportunity.stableKey(),
                opportunity.platform,
                opportunity.category,
                opportunity.price,
                opportunity.pickupKm,
                opportunity.routeKm,
                opportunity.minutes,
                opportunity.totalKm,
                opportunity.grossPerKm,
                opportunity.grossPerHour,
                acceptedAt,
                0L,
                STATUS_ACCEPTED
        );
    }

    public TripRecord completed(long at) {
        return new TripRecord(
                sourceKey,
                platform,
                category,
                price,
                pickupKm,
                routeKm,
                minutes,
                totalKm,
                grossPerKm,
                grossPerHour,
                acceptedAt,
                at,
                STATUS_COMPLETED
        );
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public String id() {
        return String.format(Locale.ROOT, "%s|%d", sourceKey, acceptedAt);
    }
}
