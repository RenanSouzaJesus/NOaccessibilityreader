package com.noapp.accessreader;

import java.util.List;

/**
 * Escolhe a melhor oportunidade sem inventar dados ausentes.
 * Se houver ofertas com R$/km e R$/h, só elas disputam o ranking principal.
 * Se nenhuma tiver duração, usa apenas R$/km e sinaliza menor confiança.
 */
public final class OpportunityRanker {

    private OpportunityRanker() {
    }

    public static RankingResult rank(List<Opportunity> opportunities) {
        if (opportunities == null || opportunities.isEmpty()) return null;

        boolean hasComplete = false;
        for (Opportunity item : opportunities) {
            if (isValid(item) && item.hasCompleteEfficiencyMetrics()) {
                hasComplete = true;
                break;
            }
        }

        Opportunity best = null;
        double bestScore = -1;

        for (Opportunity item : opportunities) {
            if (!isValid(item)) continue;
            if (hasComplete && !item.hasCompleteEfficiencyMetrics()) continue;

            double score = hasComplete
                    ? Math.sqrt(item.grossPerKm * item.grossPerHour)
                    : item.grossPerKm;

            if (score > bestScore) {
                bestScore = score;
                best = item;
            }
        }

        if (best == null) return null;

        double secondScore = -1;
        Opportunity second = null;
        for (Opportunity item : opportunities) {
            if (item == best || !isValid(item)) continue;
            if (hasComplete && !item.hasCompleteEfficiencyMetrics()) continue;

            double score = hasComplete
                    ? Math.sqrt(item.grossPerKm * item.grossPerHour)
                    : item.grossPerKm;

            if (score > secondScore) {
                secondScore = score;
                second = item;
            }
        }

        double advantagePct = 0;
        if (second != null && secondScore > 0) {
            advantagePct = ((bestScore / secondScore) - 1.0) * 100.0;
        }

        String reason = hasComplete
                ? "Melhor equilíbrio entre retorno por km e retorno por hora."
                : "Melhor retorno por km entre as oportunidades com dados disponíveis.";

        return new RankingResult(best, second, hasComplete, bestScore, advantagePct, reason);
    }

    private static boolean isValid(Opportunity item) {
        return item != null && item.price > 0 && item.totalKm > 0 && item.grossPerKm > 0;
    }

    public static final class RankingResult {
        public final Opportunity best;
        public final Opportunity second;
        public final boolean completeMetrics;
        public final double score;
        public final double advantagePct;
        public final String reason;

        RankingResult(
                Opportunity best,
                Opportunity second,
                boolean completeMetrics,
                double score,
                double advantagePct,
                String reason
        ) {
            this.best = best;
            this.second = second;
            this.completeMetrics = completeMetrics;
            this.score = score;
            this.advantagePct = advantagePct;
            this.reason = reason;
        }
    }
}
