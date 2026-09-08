package com.noapp.accessreader;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Persistência local das corridas aceitas/concluídas. */
public final class TripStore {

    private static final String PREFS = "no_trip_store";
    private static final String KEY_ITEMS = "items";
    private static final int MAX_ITEMS = 500;
    private static final long DUPLICATE_WINDOW_MS = 10 * 60 * 1000L;

    private TripStore() {}

    public static synchronized boolean accept(Context context, Opportunity opportunity, long acceptedAt) {
        if (context == null || opportunity == null || !Opportunity.TYPE_RIDE.equals(opportunity.type)) return false;

        List<TripRecord> items = readAll(context);
        String sourceKey = opportunity.stableKey();

        for (TripRecord item : items) {
            if (sourceKey.equals(item.sourceKey)
                    && Math.abs(acceptedAt - item.acceptedAt) <= DUPLICATE_WINDOW_MS) {
                return false;
            }
        }

        // Congela o custo vigente do veículo nesta viagem. Mudanças futuras no
        // perfil não alteram o histórico financeiro já registrado.
        TripRecord record = TripRecord.fromOpportunity(context, opportunity, acceptedAt);
        if (record == null) return false;
        items.add(record);
        trimAndWrite(context, items);
        return true;
    }

    public static synchronized boolean completeLatest(Context context, String platform, long completedAt) {
        if (context == null) return false;
        List<TripRecord> items = readAll(context);
        TripRecord target = null;
        int targetIndex = -1;

        for (int i = 0; i < items.size(); i++) {
            TripRecord item = items.get(i);
            if (item.isCompleted()) continue;
            if (platform != null && !platform.isEmpty() && !platform.equalsIgnoreCase(item.platform)) continue;
            if (target == null || item.acceptedAt > target.acceptedAt) {
                target = item;
                targetIndex = i;
            }
        }

        if (target == null || targetIndex < 0) return false;
        items.set(targetIndex, target.completed(completedAt));
        trimAndWrite(context, items);
        return true;
    }

    public static synchronized List<TripRecord> listAll(Context context) {
        List<TripRecord> items = readAll(context);
        items.sort(Comparator.comparingLong((TripRecord item) -> item.acceptedAt).reversed());
        return items;
    }

    public static synchronized List<TripRecord> listSince(Context context, long since) {
        List<TripRecord> result = new ArrayList<>();
        for (TripRecord item : readAll(context)) {
            long time = item.completedAt > 0 ? item.completedAt : item.acceptedAt;
            if (time >= since) result.add(item);
        }
        result.sort(Comparator.comparingLong((TripRecord item) -> item.acceptedAt).reversed());
        return result;
    }

    public static synchronized TripRecord latestOpen(Context context) {
        TripRecord latest = null;
        for (TripRecord item : readAll(context)) {
            if (item.isCompleted()) continue;
            if (latest == null || item.acceptedAt > latest.acceptedAt) latest = item;
        }
        return latest;
    }

    public static synchronized void clear(Context context) {
        if (context == null) return;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_ITEMS)
                .apply();
    }

    private static void trimAndWrite(Context context, List<TripRecord> items) {
        items.sort(Comparator.comparingLong((TripRecord item) -> item.acceptedAt).reversed());
        if (items.size() > MAX_ITEMS) items = new ArrayList<>(items.subList(0, MAX_ITEMS));
        writeAll(context, items);
    }

    private static List<TripRecord> readAll(Context context) {
        List<TripRecord> result = new ArrayList<>();
        if (context == null) return result;

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_ITEMS, "[]");

        try {
            JSONArray array = new JSONArray(raw == null ? "[]" : raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null) continue;
                result.add(new TripRecord(
                        obj.optString("sourceKey", ""),
                        obj.optString("platform", "Aplicativo"),
                        obj.optString("category", "Corrida"),
                        obj.optDouble("price", 0),
                        obj.optDouble("pickupKm", 0),
                        obj.optDouble("routeKm", 0),
                        obj.optInt("minutes", 0),
                        obj.optDouble("totalKm", 0),
                        obj.optDouble("grossPerKm", 0),
                        obj.optDouble("grossPerHour", 0),
                        obj.optLong("acceptedAt", 0),
                        obj.optLong("completedAt", 0),
                        obj.optString("status", TripRecord.STATUS_ACCEPTED),
                        obj.optString("vehicleName", ""),
                        obj.optDouble("fuelCostPerKm", 0),
                        obj.optDouble("maintenanceCostPerKm", 0),
                        obj.optDouble("depreciationCostPerKm", 0),
                        obj.optDouble("fixedCostPerKm", 0),
                        obj.optDouble("totalCostPerKm", 0),
                        obj.optDouble("estimatedCost", 0),
                        obj.has("estimatedProfit") ? obj.optDouble("estimatedProfit", 0) : obj.optDouble("price", 0),
                        obj.optDouble("estimatedMarginPct", 0)
                ));
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private static void writeAll(Context context, List<TripRecord> items) {
        JSONArray array = new JSONArray();
        for (TripRecord item : items) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("sourceKey", item.sourceKey);
                obj.put("platform", item.platform);
                obj.put("category", item.category);
                obj.put("price", item.price);
                obj.put("pickupKm", item.pickupKm);
                obj.put("routeKm", item.routeKm);
                obj.put("minutes", item.minutes);
                obj.put("totalKm", item.totalKm);
                obj.put("grossPerKm", item.grossPerKm);
                obj.put("grossPerHour", item.grossPerHour);
                obj.put("acceptedAt", item.acceptedAt);
                obj.put("completedAt", item.completedAt);
                obj.put("status", item.status);
                obj.put("vehicleName", item.vehicleName);
                obj.put("fuelCostPerKm", item.fuelCostPerKm);
                obj.put("maintenanceCostPerKm", item.maintenanceCostPerKm);
                obj.put("depreciationCostPerKm", item.depreciationCostPerKm);
                obj.put("fixedCostPerKm", item.fixedCostPerKm);
                obj.put("totalCostPerKm", item.totalCostPerKm);
                obj.put("estimatedCost", item.estimatedCost);
                obj.put("estimatedProfit", item.estimatedProfit);
                obj.put("estimatedMarginPct", item.estimatedMarginPct);
                array.put(obj);
            } catch (Exception ignored) {
            }
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEMS, array.toString())
                .apply();
    }
}
