package com.noapp.accessreader;

/**
 * Regras simples do painel do motorista. Mantém os cálculos fora da UI para
 * facilitar testes e evitar transformar o dashboard em um bloco de fórmulas.
 */
public final class DriverDayEngine {

    private DriverDayEngine() {}

    public static DaySummary summarize(
            double revenue,
            double cost,
            double profit,
            double totalKm,
            double pickupKm,
            int minutes,
            int completedTrips,
            double maintenanceReserve,
            double dailyGoal,
            double maxHours
    ) {
        double safeRevenue = Math.max(0, revenue);
        double safeCost = Math.max(0, cost);
        double safeProfit = profit;
        double safeKm = Math.max(0, totalKm);
        double safePickup = Math.max(0, pickupKm);
        int safeMinutes = Math.max(0, minutes);
        int safeTrips = Math.max(0, completedTrips);
        double goal = Math.max(0, dailyGoal);
        double hoursLimit = Math.max(0, maxHours);

        double progressPct = goal > 0 ? clamp((safeProfit / goal) * 100.0, 0, 100) : 0;
        double remaining = goal > 0 ? Math.max(0, goal - safeProfit) : 0;
        boolean goalReached = goal > 0 && safeProfit >= goal;
        double costPer100 = safeRevenue > 0 ? (safeCost / safeRevenue) * 100.0 : 0;
        double profitPerKm = safeKm > 0 ? safeProfit / safeKm : 0;
        double profitPerHour = safeMinutes > 0 ? safeProfit / (safeMinutes / 60.0) : 0;
        double emptyKmPct = safeKm > 0 ? (safePickup / safeKm) * 100.0 : 0;
        double tripHours = safeMinutes / 60.0;
        double hourProgressPct = hoursLimit > 0 ? clamp((tripHours / hoursLimit) * 100.0, 0, 100) : 0;
        boolean hourLimitReached = hoursLimit > 0 && tripHours >= hoursLimit;

        return new DaySummary(
                safeRevenue,
                safeCost,
                safeProfit,
                safeKm,
                safePickup,
                safeMinutes,
                safeTrips,
                Math.max(0, maintenanceReserve),
                goal,
                hoursLimit,
                progressPct,
                remaining,
                goalReached,
                costPer100,
                profitPerKm,
                profitPerHour,
                emptyKmPct,
                tripHours,
                hourProgressPct,
                hourLimitReached
        );
    }

    public static String insight(DaySummary s, boolean vehicleConfigured) {
        if (s == null) return "O NÓ precisa de mais dados para formar sua leitura do dia.";
        if (!vehicleConfigured) {
            return "Cadastre seu veículo para transformar faturamento em lucro estimado e enxergar quanto realmente sobra.";
        }
        if (s.completedTrips == 0) {
            return "Quando você concluir viagens, o NÓ vai separar faturamento, custo do carro e lucro estimado.";
        }
        if (s.goalReached) {
            return "Meta líquida atingida. Agora você decide se vale continuar rodando ou preservar seu tempo e o veículo.";
        }
        if (s.hourLimitReached) {
            return "Seu limite pessoal de horas em viagens foi atingido. O NÓ destaca isso para você decidir sem olhar apenas para o faturamento.";
        }
        if (s.emptyKmPct >= 25.0) {
            return String.format(java.util.Locale.ROOT,
                    "Atenção ao deslocamento vazio: %.0f%% dos km foram até o passageiro. Reduzir coleta longa pode melhorar seu resultado.",
                    s.emptyKmPct);
        }
        if (s.costPer100Revenue >= 45.0) {
            return String.format(java.util.Locale.ROOT,
                    "Seu veículo consumiu cerca de R$ %.0f a cada R$ 100 faturados. O foco agora é melhorar a margem das próximas corridas.",
                    s.costPer100Revenue);
        }
        return String.format(java.util.Locale.ROOT,
                "Hoje o lucro estimado está em R$ %.2f por km. Continue priorizando corridas que preservem essa eficiência.",
                s.profitPerKm);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class DaySummary {
        public final double revenue;
        public final double cost;
        public final double profit;
        public final double totalKm;
        public final double pickupKm;
        public final int minutes;
        public final int completedTrips;
        public final double maintenanceReserve;
        public final double dailyGoal;
        public final double maxHours;
        public final double goalProgressPct;
        public final double remainingToGoal;
        public final boolean goalReached;
        public final double costPer100Revenue;
        public final double profitPerKm;
        public final double profitPerHour;
        public final double emptyKmPct;
        public final double tripHours;
        public final double hourProgressPct;
        public final boolean hourLimitReached;

        DaySummary(
                double revenue,
                double cost,
                double profit,
                double totalKm,
                double pickupKm,
                int minutes,
                int completedTrips,
                double maintenanceReserve,
                double dailyGoal,
                double maxHours,
                double goalProgressPct,
                double remainingToGoal,
                boolean goalReached,
                double costPer100Revenue,
                double profitPerKm,
                double profitPerHour,
                double emptyKmPct,
                double tripHours,
                double hourProgressPct,
                boolean hourLimitReached
        ) {
            this.revenue = revenue;
            this.cost = cost;
            this.profit = profit;
            this.totalKm = totalKm;
            this.pickupKm = pickupKm;
            this.minutes = minutes;
            this.completedTrips = completedTrips;
            this.maintenanceReserve = maintenanceReserve;
            this.dailyGoal = dailyGoal;
            this.maxHours = maxHours;
            this.goalProgressPct = goalProgressPct;
            this.remainingToGoal = remainingToGoal;
            this.goalReached = goalReached;
            this.costPer100Revenue = costPer100Revenue;
            this.profitPerKm = profitPerKm;
            this.profitPerHour = profitPerHour;
            this.emptyKmPct = emptyKmPct;
            this.tripHours = tripHours;
            this.hourProgressPct = hourProgressPct;
            this.hourLimitReached = hourLimitReached;
        }
    }
}
