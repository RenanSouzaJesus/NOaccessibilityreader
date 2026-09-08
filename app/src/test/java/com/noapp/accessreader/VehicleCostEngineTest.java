package com.noapp.accessreader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VehicleCostEngineTest {

    @Test
    public void calculatesCostPerKmAndTripProfit() {
        VehicleProfile profile = new VehicleProfile(
                "HB20 1.0",
                "Gasolina",
                6.00,
                10.0,
                0.15,
                0.20,
                900.0,
                3000.0,
                0L
        );

        VehicleCostEngine.CostEstimate result = VehicleCostEngine.estimate(
                profile,
                9.3,
                28.40,
                14
        );

        assertTrue(result.configured);
        assertEquals(0.60, result.fuelPerKm, 0.0001);
        assertEquals(0.30, result.fixedPerKm, 0.0001);
        assertEquals(1.25, result.totalPerKm, 0.0001);
        assertEquals(11.625, result.totalCost, 0.0001);
        assertEquals(16.775, result.profit, 0.0001);
        assertEquals(59.0669, result.marginPct, 0.001);
        assertEquals(40.9331, result.costPer100Revenue, 0.001);
        assertEquals(1.80376, result.profitPerKm, 0.001);
        assertEquals(71.8928, result.profitPerHour, 0.001);
    }

    @Test
    public void withoutVehicleProfileKeepsRevenueButHasNoEstimatedCost() {
        VehicleCostEngine.CostEstimate result = VehicleCostEngine.estimate(
                VehicleProfile.empty(),
                10.0,
                30.0,
                20
        );

        assertFalse(result.configured);
        assertEquals(0.0, result.totalCost, 0.0001);
        assertEquals(30.0, result.profit, 0.0001);
    }
}
