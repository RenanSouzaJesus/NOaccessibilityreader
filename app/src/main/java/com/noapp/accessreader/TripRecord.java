package com.noapp.accessreader;

import android.content.Context;

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

    // Snapshot financeiro do veículo no momento em que a corrida foi aceita.
    public final String vehicleName;
    public final double fuelCostPerKm;
    public final double maintenanceCostPerKm;
    public final double depreciationCostPerKm;
    public final double fixedCostPerKm;
    public final double totalCostPerKm;
    public final double estimatedCost;
    public final double estimatedProfit;
    public final double estimatedMarginPct;

    /** Construtor legado para manter compatibilidade com registros/testes antigos. */
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
        this(sourceKey, platform, category, price, pickupKm, routeKm, minutes, totalKm,
                grossPerKm, grossPerHour, acceptedAt, completedAt, status,
                "", 0, 0, 0, 0, 0, 0, price, 0);
    }

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
            String status,
            String vehicleName,
            double fuelCostPerKm,
            double maintenanceCostPerKm,
            double depreciationCostPerKm,
            double fixedCostPerKm,
            double totalCostPerKm,
            double estimatedCost,
            double estimatedProfit,
            double estimatedMarginPct
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
        this.vehicleName = vehicleName == null ? "" : vehicleName;
        this.fuelCostPerKm = safe(fuelCostPerKm);
        this.maintenanceCostPerKm = safe(maintenanceCostPerKm);
        this.depreciationCostPerKm = safe(depreciationCostPerKm);
        this.fixedCostPerKm = safe(fixedCostPerKm);
        this.totalCostPerKm = safe(totalCostPerKm);
        this.estimatedCost = safe(estimatedCost);
        this.estimatedProfit = estimatedProfit;
        this.estimatedMarginPct = estimatedMarginPct;
    }

    public static TripRecord fromOpportunity(Opportunity opportunity, long acceptedAt) {
        return fromOpportunity(null, opportunity, acceptedAt);
    }

    public static TripRecord fromOpportunity(Context context, Opportunity opportunity, long acceptedAt) {
        if (opportunity == null) return null;

        VehicleProfile profile = context == null
                ? VehicleProfile.empty()
                : VehicleProfileStore.load(context);
        VehicleCostEngine.CostEstimate cost = VehicleCostEngine.estimate(
                profile,
                opportunity.totalKm,
                opportunity.price,
                opportunity.minutes
        );

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
                STATUS_ACCEPTED,
                cost.configured ? cost.vehicleName : "",
                cost.configured ? cost.fuelPerKm : 0,
                cost.configured ? cost.maintenancePerKm : 0,
                cost.configured ? cost.depreciationPerKm : 0,
                cost.configured ? cost.fixedPerKm : 0,
                cost.configured ? cost.totalPerKm : 0,
                cost.configured ? cost.totalCost : 0,
                cost.configured ? cost.profit : opportunity.price,
                cost.configured ? cost.marginPct : 0
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
                STATUS_COMPLETED,
                vehicleName,
                fuelCostPerKm,
                maintenanceCostPerKm,
                depreciationCostPerKm,
                fixedCostPerKm,
                totalCostPerKm,
                estimatedCost,
                estimatedProfit,
                estimatedMarginPct
        );
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean hasCostSnapshot() {
        return totalCostPerKm > 0 || estimatedCost > 0;
    }

    public String id() {
        return String.format(Locale.ROOT, "%s|%d", sourceKey, acceptedAt);
    }

    private static double safe(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) return 0;
        return value;
    }
}
