package com.noapp.accessreader;

import org.junit.Test;

import static org.junit.Assert.*;

public class RideComparisonEngineTest {

    @Test
    public void sampleOffersPrefer99ByOverallBalance() {
        RideOfferParser.RideOffer uber = new RideOfferParser.RideOffer(
                "Uber", "UberX", 28.40, 3.1, 6.2, 14,
                9.3, 28.40 / 9.3, 28.40 / (14 / 60.0), "BOA");

        RideOfferParser.RideOffer ninetyNine = new RideOfferParser.RideOffer(
                "99", "99Pop", 31.20, 2.4, 7.0, 16,
                9.4, 31.20 / 9.4, 31.20 / (16 / 60.0), "BOA");

        RideComparisonEngine.ComparisonResult result =
                RideComparisonEngine.compare(uber, ninetyNine);

        assertNotNull(result);
        assertFalse(result.technicalTie);
        assertSame(ninetyNine, result.winner);
        assertTrue(result.scoreAdvantagePct > 0);
    }

    @Test
    public void offerThatWinsBothMetricsIsPreferred() {
        RideOfferParser.RideOffer a = new RideOfferParser.RideOffer(
                "A", "A", 30, 2, 8, 20,
                10, 3.0, 90.0, "BOA");

        RideOfferParser.RideOffer b = new RideOfferParser.RideOffer(
                "B", "B", 20, 2, 8, 20,
                10, 2.0, 60.0, "MÉDIA");

        RideComparisonEngine.ComparisonResult result =
                RideComparisonEngine.compare(a, b);

        assertSame(a, result.winner);
        assertTrue(result.reason.contains("Melhor retorno por km"));
    }

    @Test
    public void veryCloseOffersBecomeTechnicalTie() {
        RideOfferParser.RideOffer a = new RideOfferParser.RideOffer(
                "A", "A", 30, 2, 8, 20,
                10, 3.00, 90.0, "BOA");

        RideOfferParser.RideOffer b = new RideOfferParser.RideOffer(
                "B", "B", 30, 2, 8, 20,
                10, 3.02, 90.5, "BOA");

        RideComparisonEngine.ComparisonResult result =
                RideComparisonEngine.compare(a, b);

        assertTrue(result.technicalTie);
    }
}
