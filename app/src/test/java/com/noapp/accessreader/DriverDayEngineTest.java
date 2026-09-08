package com.noapp.accessreader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DriverDayEngineTest {

    @Test
    public void calculatesGoalCostAndEfficiency() {
        DriverDayEngine.DaySummary s = DriverDayEngine.summarize(
                400.0,
                140.0,
                260.0,
                120.0,
                24.0,
                360,
                10,
                18.0,
                300.0,
                9.0
        );

        assertEquals(86.67, s.goalProgressPct, 0.01);
        assertEquals(40.0, s.remainingToGoal, 0.01);
        assertFalse(s.goalReached);
        assertEquals(35.0, s.costPer100Revenue, 0.01);
        assertEquals(2.17, s.profitPerKm, 0.01);
        assertEquals(43.33, s.profitPerHour, 0.01);
        assertEquals(20.0, s.emptyKmPct, 0.01);
        assertFalse(s.hourLimitReached);
    }

    @Test
    public void marksGoalAsReached() {
        DriverDayEngine.DaySummary s = DriverDayEngine.summarize(
                500.0,
                150.0,
                350.0,
                140.0,
                28.0,
                420,
                12,
                21.0,
                300.0,
                9.0
        );

        assertTrue(s.goalReached);
        assertEquals(100.0, s.goalProgressPct, 0.01);
        assertEquals(0.0, s.remainingToGoal, 0.01);
        assertTrue(DriverDayEngine.insight(s, true).contains("Meta líquida atingida"));
    }

    @Test
    public void warnsWhenPersonalHourLimitIsReached() {
        DriverDayEngine.DaySummary s = DriverDayEngine.summarize(
                300.0,
                120.0,
                180.0,
                110.0,
                15.0,
                540,
                9,
                16.0,
                300.0,
                9.0
        );

        assertTrue(s.hourLimitReached);
        assertTrue(DriverDayEngine.insight(s, true).contains("limite pessoal"));
    }

    @Test
    public void prioritizesLongEmptyPickupWarning() {
        DriverDayEngine.DaySummary s = DriverDayEngine.summarize(
                300.0,
                90.0,
                210.0,
                100.0,
                30.0,
                300,
                8,
                10.0,
                400.0,
                9.0
        );

        assertEquals(30.0, s.emptyKmPct, 0.01);
        assertTrue(DriverDayEngine.insight(s, true).contains("deslocamento vazio"));
    }
}
