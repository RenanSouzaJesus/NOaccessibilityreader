package com.noapp.accessreader;

/**
 * Compara duas ofertas usando as duas métricas que já existem no NÓ:
 * retorno por quilômetro e retorno por hora.
 *
 * O índice é apenas comparativo: média geométrica de R$/km e R$/h.
 * Isso permite dar o mesmo peso relativo às duas dimensões sem transformar
 * o índice em uma nova métrica financeira para o usuário.
 */
public final class RideComparisonEngine {

    private static final double TECHNICAL_TIE_THRESHOLD = 0.03; // 3%

    private RideComparisonEngine() {
    }

    public static ComparisonResult compare(
            RideOfferParser.RideOffer first,
            RideOfferParser.RideOffer second
    ) {
        if (first == null || second == null) return null;

        double firstScore = balanceScore(first);
        double secondScore = balanceScore(second);

        if (firstScore <= 0 || secondScore <= 0) return null;

        double relativeDifference = Math.abs(firstScore - secondScore)
                / Math.max(firstScore, secondScore);

        boolean technicalTie = relativeDifference < TECHNICAL_TIE_THRESHOLD;

        RideOfferParser.RideOffer winner;
        RideOfferParser.RideOffer loser;

        if (firstScore >= secondScore) {
            winner = first;
            loser = second;
        } else {
            winner = second;
            loser = first;
        }

        double scoreAdvantagePct = ((Math.max(firstScore, secondScore)
                / Math.min(firstScore, secondScore)) - 1.0) * 100.0;

        String reason;
        if (technicalTie) {
            reason = "As duas ofertas estão muito próximas no equilíbrio entre R$/km e R$/h.";
        } else if (dominates(winner, loser)) {
            reason = "Melhor retorno por km e melhor retorno por hora.";
        } else {
            reason = "Melhor equilíbrio entre retorno por km e produtividade por hora.";
        }

        return new ComparisonResult(
                first,
                second,
                winner,
                loser,
                technicalTie,
                firstScore,
                secondScore,
                scoreAdvantagePct,
                reason
        );
    }

    private static double balanceScore(RideOfferParser.RideOffer offer) {
        if (offer.grossPerKm <= 0 || offer.grossPerHour <= 0) return 0;
        return Math.sqrt(offer.grossPerKm * offer.grossPerHour);
    }

    private static boolean dominates(
            RideOfferParser.RideOffer winner,
            RideOfferParser.RideOffer loser
    ) {
        return winner.grossPerKm >= loser.grossPerKm
                && winner.grossPerHour >= loser.grossPerHour
                && (winner.grossPerKm > loser.grossPerKm
                || winner.grossPerHour > loser.grossPerHour);
    }

    public static double percentDifference(double value, double reference) {
        if (reference == 0) return 0;
        return ((value / reference) - 1.0) * 100.0;
    }

    public static final class ComparisonResult {
        public final RideOfferParser.RideOffer first;
        public final RideOfferParser.RideOffer second;
        public final RideOfferParser.RideOffer winner;
        public final RideOfferParser.RideOffer loser;
        public final boolean technicalTie;
        public final double firstScore;
        public final double secondScore;
        public final double scoreAdvantagePct;
        public final String reason;

        ComparisonResult(
                RideOfferParser.RideOffer first,
                RideOfferParser.RideOffer second,
                RideOfferParser.RideOffer winner,
                RideOfferParser.RideOffer loser,
                boolean technicalTie,
                double firstScore,
                double secondScore,
                double scoreAdvantagePct,
                String reason
        ) {
            this.first = first;
            this.second = second;
            this.winner = winner;
            this.loser = loser;
            this.technicalTie = technicalTie;
            this.firstScore = firstScore;
            this.secondScore = secondScore;
            this.scoreAdvantagePct = scoreAdvantagePct;
            this.reason = reason;
        }
    }
}
