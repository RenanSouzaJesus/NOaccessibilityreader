package com.noapp.accessreader;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Persistência leve do inbox de oportunidades do NÓ. */
public final class OpportunityStore {

    private static final String PREFS = "no_opportunity_store";
    private static final String KEY_ITEMS = "items";
    private static final int MAX_ITEMS = 30;

    private OpportunityStore() {
    }

    /**
     * Retorna true quando a oportunidade ainda não existia no inbox.
     * Releituras da mesma tela apenas atualizam o item, evitando spam.
     */
    public static synchronized boolean upsert(Context context, Opportunity opportunity) {
        if (context == null || opportunity == null) return false;

        List<Opportunity> items = readAll(context);
        String key = opportunity.stableKey();
        boolean isNew = true;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).stableKey().equals(key)) {
                items.set(i, opportunity);
                isNew = false;
                break;
            }
        }

        if (isNew) items.add(opportunity);

        Collections.sort(items, (a, b) -> Long.compare(b.timestamp, a.timestamp));
        if (items.size() > MAX_ITEMS) {
            items = new ArrayList<>(items.subList(0, MAX_ITEMS));
        }

        writeAll(context, items);
        return isNew;
    }

    public static synchronized List<Opportunity> listFresh(Context context, long maxAgeMs) {
        long now = System.currentTimeMillis();
        List<Opportunity> all = readAll(context);
        List<Opportunity> fresh = new ArrayList<>();

        for (Opportunity item : all) {
            if (item.timestamp > 0 && now - item.timestamp <= maxAgeMs) {
                fresh.add(item);
            }
        }

        fresh.sort(Comparator.comparingLong((Opportunity item) -> item.timestamp).reversed());
        return fresh;
    }

    public static synchronized void clear(Context context) {
        if (context == null) return;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_ITEMS)
                .apply();
    }

    private static List<Opportunity> readAll(Context context) {
        List<Opportunity> result = new ArrayList<>();
        if (context == null) return result;

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_ITEMS, "[]");

        try {
            JSONArray array = new JSONArray(raw == null ? "[]" : raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null) continue;

                Opportunity item = fromJson(obj);
                if (item != null) result.add(item);
            }
        } catch (Exception ignored) {
        }

        return result;
    }

    private static void writeAll(Context context, List<Opportunity> items) {
        JSONArray array = new JSONArray();
        for (Opportunity item : items) {
            array.put(toJson(item));
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEMS, array.toString())
                .apply();
    }

    private static JSONObject toJson(Opportunity item) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", item.type);
            obj.put("platform", item.platform);
            obj.put("category", item.category);
            obj.put("price", item.price);
            obj.put("pickupKm", item.pickupKm);
            obj.put("routeKm", item.routeKm);
            obj.put("minutes", item.minutes);
            obj.put("totalKm", item.totalKm);
            obj.put("grossPerKm", item.grossPerKm);
            obj.put("grossPerHour", item.grossPerHour);
            obj.put("rating", item.rating);
            obj.put("timestamp", item.timestamp);
            obj.put("sourcePackage", item.sourcePackage);
            obj.put("source", item.source);
        } catch (Exception ignored) {
        }
        return obj;
    }

    private static Opportunity fromJson(JSONObject obj) {
        try {
            return new Opportunity(
                    obj.optString("type", Opportunity.TYPE_RIDE),
                    obj.optString("platform", "Aplicativo"),
                    obj.optString("category", "Oportunidade"),
                    obj.optDouble("price", 0),
                    obj.optDouble("pickupKm", 0),
                    obj.optDouble("routeKm", 0),
                    obj.optInt("minutes", 0),
                    obj.optDouble("totalKm", 0),
                    obj.optDouble("grossPerKm", 0),
                    obj.optDouble("grossPerHour", 0),
                    obj.optString("rating", ""),
                    obj.optLong("timestamp", 0),
                    obj.optString("sourcePackage", ""),
                    obj.optString("source", "ACCESSIBILITY")
            );
        } catch (Exception ignored) {
            return null;
        }
    }
}
