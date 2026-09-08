package com.noapp.accessreader;

import android.content.Context;
import android.content.SharedPreferences;

/** Persistência local do perfil de custo do veículo. */
public final class VehicleProfileStore {

    private static final String PREFS = "no_vehicle_profile";

    private VehicleProfileStore() {}

    public static VehicleProfile load(Context context) {
        if (context == null) return VehicleProfile.empty();
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new VehicleProfile(
                p.getString("name", ""),
                p.getString("fuel_type", "Gasolina"),
                p.getFloat("fuel_price", 0f),
                p.getFloat("consumption_km_l", 0f),
                p.getFloat("maintenance_per_km", 0f),
                p.getFloat("depreciation_per_km", 0f),
                p.getFloat("fixed_monthly_cost", 0f),
                p.getFloat("expected_monthly_km", 0f),
                p.getLong("updated_at", 0L)
        );
    }

    public static void save(Context context, VehicleProfile profile) {
        if (context == null || profile == null) return;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString("name", profile.name)
                .putString("fuel_type", profile.fuelType)
                .putFloat("fuel_price", (float) profile.fuelPrice)
                .putFloat("consumption_km_l", (float) profile.consumptionKmPerLiter)
                .putFloat("maintenance_per_km", (float) profile.maintenancePerKm)
                .putFloat("depreciation_per_km", (float) profile.depreciationPerKm)
                .putFloat("fixed_monthly_cost", (float) profile.fixedMonthlyCost)
                .putFloat("expected_monthly_km", (float) profile.expectedMonthlyKm)
                .putLong("updated_at", System.currentTimeMillis())
                .apply();
    }

    public static boolean isConfigured(Context context) {
        return load(context).isConfigured();
    }
}
