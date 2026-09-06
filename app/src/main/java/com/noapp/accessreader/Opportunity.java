package com.noapp.accessreader;

import java.util.Locale;

/**
 * Modelo unificado de oportunidade do NÓ.
 * Pode representar corrida, entrega ou rota.
 */
public final class Opportunity {

    public static final String TYPE_RIDE = "CORRIDA";
    public static final String TYPE_DELIVERY = "ENTREGA";
    public static final String TYPE_ROUTE = "ROTA";

    public final String type;
    public final String platform;
    public final String category;
    public final double price;
    public final double pickupKm;
    public final double routeKm;
    public final int minutes;
    public final double totalKm;
    public final double grossPerKm;
    public final double grossPerHour;
    public final String rating;
    public final long timestamp;
    public final String sourcePackage;
    public final String source;

    public Opportunity(
            String type,
            String platform,
            String category,
            double price,
            double pickupKm,
            double routeKm,
            int minutes,
            double totalKm,
            double grossPerKm,
            double grossPerHour,
            String rating,
            long timestamp,
            String sourcePackage,
            String source
    ) {
        this.type = type == null ? TYPE_RIDE : type;
        this.platform = platform == null ? "Aplicativo" : platform;
        this.category = category == null ? "Oportunidade" : category;
        this.price = price;
        this.pickupKm = pickupKm;
        this.routeKm = routeKm;
        this.minutes = minutes;
        this.totalKm = totalKm;
        this.grossPerKm = grossPerKm;
        this.grossPerHour = grossPerHour;
        this.rating = rating == null ? "" : rating;
        this.timestamp = timestamp;
        this.sourcePackage = sourcePackage == null ? "" : sourcePackage;
        this.source = source == null ? "ACCESSIBILITY" : source;
    }

    public static Opportunity fromRide(
            RideOfferParser.RideOffer offer,
            long timestamp,
            String sourcePackage,
            String source
    ) {
        if (offer == null) return null;
        return new Opportunity(
                TYPE_RIDE,
                offer.platform,
                offer.category,
                offer.price,
                offer.pickupKm,
                offer.tripKm,
                offer.tripMinutes,
                offer.totalKm,
                offer.grossPerKm,
                offer.grossPerHour,
                offer.rating,
                timestamp,
                sourcePackage,
                source
        );
    }

    public boolean hasCompleteEfficiencyMetrics() {
        return grossPerKm > 0 && grossPerHour > 0;
    }

    public String stableKey() {
        return String.format(Locale.ROOT,
                "%s|%s|%s|%.2f|%.2f|%.2f|%d",
                type,
                platform,
                category,
                price,
                pickupKm,
                routeKm,
                minutes);
    }
}
