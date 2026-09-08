package com.noapp.accessreader;

/** Perfil de custos usado para estimar quanto o veículo consome por km. */
public final class VehicleProfile {

    public final String name;
    public final String fuelType;
    public final double fuelPrice;
    public final double consumptionKmPerLiter;
    public final double maintenancePerKm;
    public final double depreciationPerKm;
    public final double fixedMonthlyCost;
    public final double expectedMonthlyKm;
    public final long updatedAt;

    public VehicleProfile(
            String name,
            String fuelType,
            double fuelPrice,
            double consumptionKmPerLiter,
            double maintenancePerKm,
            double depreciationPerKm,
            double fixedMonthlyCost,
            double expectedMonthlyKm,
            long updatedAt
    ) {
        this.name = name == null ? "" : name.trim();
        this.fuelType = fuelType == null ? "" : fuelType.trim();
        this.fuelPrice = nonNegative(fuelPrice);
        this.consumptionKmPerLiter = nonNegative(consumptionKmPerLiter);
        this.maintenancePerKm = nonNegative(maintenancePerKm);
        this.depreciationPerKm = nonNegative(depreciationPerKm);
        this.fixedMonthlyCost = nonNegative(fixedMonthlyCost);
        this.expectedMonthlyKm = nonNegative(expectedMonthlyKm);
        this.updatedAt = updatedAt;
    }

    public boolean isConfigured() {
        return fuelPrice > 0 && consumptionKmPerLiter > 0;
    }

    public String displayName() {
        return name.isEmpty() ? "Meu veículo" : name;
    }

    public static VehicleProfile empty() {
        return new VehicleProfile("", "Gasolina", 0, 0, 0, 0, 0, 0, 0);
    }

    private static double nonNegative(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) return 0;
        return value;
    }
}
