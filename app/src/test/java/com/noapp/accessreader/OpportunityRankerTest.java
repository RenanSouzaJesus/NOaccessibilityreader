package com.noapp.accessreader;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class OpportunityRankerTest {

    @Test
    public void defaultSimulatorOffersPrefer99() {
        Opportunity uber = new Opportunity(
                Opportunity.TYPE_RIDE,
                "Uber",
                "UberX",
                28.40,
                3.1,
                6.2,
                14,
                9.3,
                28.40 / 9.3,
                28.40 / (14.0 / 60.0),
                "BOA",
                1000L,
                "test",
                "TEST"
        );

        Opportunity ninetyNine = new Opportunity(
                Opportunity.TYPE_RIDE,
                "99",
                "99Pop",
                31.20,
                2.4,
                7.0,
                16,
                9.4,
                31.20 / 9.4,
                31.20 / (16.0 / 60.0),
                "BOA",
                1000L,
                "test",
                "TEST"
        );

        OpportunityRanker.RankingResult result = OpportunityRanker.rank(
                Arrays.asList(uber, ninetyNine)
        );

        assertNotNull(result);
        assertEquals("99", result.best.platform);
        assertTrue(result.completeMetrics);
        assertTrue(result.advantagePct > 0);
    }

    @Test
    public void incompleteDeliveryDoesNotBeatCompleteOffersByArbitraryUnits() {
        Opportunity complete = new Opportunity(
                Opportunity.TYPE_RIDE,
                "99",
                "99Pop",
                30,
                2,
                8,
                20,
                10,
                3,
                90,
                "BOA",
                1000L,
                "test",
                "TEST"
        );

        Opportunity incomplete = new Opportunity(
                Opportunity.TYPE_DELIVERY,
                "iFood",
                "Pedido",
                50,
                1,
                4,
                0,
                5,
                10,
                0,
                "BOA",
                1000L,
                "test",
                "TEST"
        );

        OpportunityRanker.RankingResult result = OpportunityRanker.rank(
                Arrays.asList(incomplete, complete)
        );

        assertNotNull(result);
        assertEquals("99", result.best.platform);
        assertTrue(result.completeMetrics);
    }
}
