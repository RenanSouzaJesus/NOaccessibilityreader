package com.noapp.accessreader;

/** Cálculos financeiros do veículo, separados da UI para facilitar testes. */
public final class VehicleCostEngine {

    private VehicleCostEngine() {}

    public static CostEstimate estimate(
            VehicleProfile profile,
            double totalKm,
            double revenue,
            int minutes
    ) {
        VehicleProfile p = profile == null ? VehicleProfile.empty() : profile;
        double km = Math.max(0, totalKm);
        double gross = Math.max(0, revenue);

        double fuelPerKm = p.consumptionKmPerLiter > 0
                ? p.fuelPrice / p.consumptionKmPerLiter
                : 0;
        double fixedPerKm = p.expectedMonthlyKm > 0
                ? p.fixedMonthlyCost / p.expectedMonthlyKm
                : 0;
        double totalPerKm = fuelPerKm
                + p.maintenancePerKm
                + p.depreciationPerKm
                + fixedPerKm;

        double fuelCost = fuelPerKm * km;
        double maintenanceCost = p.maintenancePerKm * km;
        double depreciationCost = p.depreciationPerKm * km;
        double fixedCost = fixedPerKm * km;
        double totalCost = totalPerKm * km;
        double profit = gross - totalCost;
        double marginPct = gross > 0 ? (profit / gross) * 100.0 : 0;
        double costPer100Revenue = gross > 0 ? (totalCost / gross) * 100.0 : 0;
        double revenuePerCost = totalCost > 0 ? gross / totalCost : 0;
        double profitPerKm = km > 0 ? profit / km : 0;
        double profitPerHour = minutes > 0 ? profit / (minutes / 60.0) : 0;

        return new CostEstimate(
                p.displayName(),
                p.isConfigured(),
                fuelPerKm,
                p.maintenancePerKm,
                p.depreciationPerKm,
                fixedPerKm,
                totalPerKm,
                fuelCost,
                maintenanceCost,
                depreciationCost,
                fixedCost,
                totalCost,
                profit,
                marginPct,
                costPer100Revenue,
                revenuePerCost,
                profitPerKm,
                profitPerHour
        );
    }

    public static final class CostEstimate {
        public final String vehicleName;
        public final boolean configured;
        public final double fuelPerKm;
        public final double maintenancePerKm;
        public final double depreciationPerKm;
        public final double fixedPerKm;
        public final double totalPerKm;
        public final double fuelCost;
        public final double maintenanceCost;
        public final double depreciationCost;
        public final double fixedCost;
        public final double totalCost;
        public final double profit;
        public final double marginPct;
        public final double costPer100Revenue;
        public final double revenuePerCost;
        public final double profitPerKm;
        public final double profitPerHour;

        CostEstimate(
                String vehicleName,
                boolean configured,
                double fuelPerKm,
                double maintenancePerKm,
                double depreciationPerKm,
                double fixedPerKm,
                double totalPerKm,
                double fuelCost,
                double maintenanceCost,
                double depreciationCost,
                double fixedCost,
                double totalCost,
                double profit,
                double marginPct,
                double costPer100Revenue,
                double revenuePerCost,
                double profitPerKm,
                double profitPerHour
        ) {
            this.vehicleName = vehicleName;
            this.configured = configured;
            this.fuelPerKm = fuelPerKm;
            this.maintenancePerKm = maintenancePerKm;
            this.depreciationPerKm = depreciationPerKm;
            this.fixedPerKm = fixedPerKm;
            this.totalPerKm = totalPerKm;
            this.fuelCost = fuelCost;
            this.maintenanceCost = maintenanceCost;
            this.depreciationCost = depreciationCost;
            this.fixedCost = fixedCost;
            this.totalCost = totalCost;
            this.profit = profit;
            this.marginPct = marginPct;
            this.costPer100Revenue = costPer100Revenue;
            this.revenuePerCost = revenuePerCost;
            this.profitPerKm = profitPerKm;
            this.profitPerHour = profitPerHour;
        }
    }
}
