package com.noapp.accessreader;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RideOfferParser {

    private static final Pattern PRICE = Pattern.compile("R\\$\\s*([0-9]+(?:[.,][0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PICKUP_KM = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*km\\s*(?:at[eé]|para)\\s*(?:o\\s*)?passageiro", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRIP_KM_MIN = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*km[^\\n]*?([0-9]+)\\s*min", Pattern.CASE_INSENSITIVE);
    private static final Pattern DESC_PICKUP_KM = Pattern.compile("dist[aâ]ncia\\s+at[eé]\\s+o\\s+passageiro[^0-9]*([0-9]+(?:[.,][0-9]+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DESC_TRIP = Pattern.compile("viagem[^0-9]*([0-9]+(?:[.,][0-9]+)?)\\s*quil[oô]metros[^0-9]*([0-9]+)\\s*minutos", Pattern.CASE_INSENSITIVE);

    private RideOfferParser() {
    }

    public static RideOffer parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;

        String normalized = raw.replace('\u00A0', ' ');

        Double price = firstDouble(PRICE, normalized, 1);
        Double pickupKm = firstDouble(PICKUP_KM, normalized, 1);
        Double tripKm = null;
        Integer tripMinutes = null;

        Matcher tripMatcher = TRIP_KM_MIN.matcher(normalized);
        while (tripMatcher.find()) {
            double candidateKm = parseNumber(tripMatcher.group(1));
            int candidateMin = parseInt(tripMatcher.group(2));
            String match = tripMatcher.group(0).toLowerCase(Locale.ROOT);

            // Evita usar a linha de coleta como distância da viagem.
            if (match.contains("passageiro")) continue;

            tripKm = candidateKm;
            tripMinutes = candidateMin;
            break;
        }

        if (pickupKm == null) {
            pickupKm = firstDouble(DESC_PICKUP_KM, normalized, 1);
        }

        if (tripKm == null || tripMinutes == null) {
            Matcher descTripMatcher = DESC_TRIP.matcher(normalized);
            if (descTripMatcher.find()) {
                tripKm = parseNumber(descTripMatcher.group(1));
                tripMinutes = parseInt(descTripMatcher.group(2));
            }
        }

        if (price == null || pickupKm == null || tripKm == null || tripMinutes == null) {
            return null;
        }

        if (price <= 0 || pickupKm < 0 || tripKm <= 0 || tripMinutes <= 0) {
            return null;
        }

        String platform = detectPlatform(normalized);
        String category = detectCategory(normalized);

        double totalKm = pickupKm + tripKm;
        double grossPerKm = totalKm > 0 ? price / totalKm : 0;
        double grossPerHour = tripMinutes > 0 ? price / (tripMinutes / 60.0) : 0;
        String rating = classify(grossPerKm);

        return new RideOffer(platform, category, price, pickupKm, tripKm, tripMinutes,
                totalKm, grossPerKm, grossPerHour, rating);
    }

    private static String detectPlatform(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("uber")) return "Uber";
        if (lower.contains("99pop") || lower.contains("99 pop") || lower.contains("simulação - 99") || lower.contains("simulacao - 99")) return "99";
        if (lower.contains("indrive")) return "inDrive";
        return "App de mobilidade";
    }

    private static String detectCategory(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("uberx")) return "UberX";
        if (lower.contains("comfort")) return "Comfort";
        if (lower.contains("black")) return "Black";
        if (lower.contains("99pop") || lower.contains("99 pop")) return "99Pop";
        if (lower.contains("99moto") || lower.contains("99 moto")) return "99Moto";
        return "Corrida";
    }

    private static String classify(double grossPerKm) {
        if (grossPerKm >= 2.50) return "BOA";
        if (grossPerKm >= 1.70) return "MÉDIA";
        return "RUIM";
    }

    private static Double firstDouble(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) return null;
        return parseNumber(matcher.group(group));
    }

    private static double parseNumber(String value) {
        return Double.parseDouble(value.replace(',', '.'));
    }

    private static int parseInt(String value) {
        return Integer.parseInt(value);
    }

    public static final class RideOffer {
        public final String platform;
        public final String category;
        public final double price;
        public final double pickupKm;
        public final double tripKm;
        public final int tripMinutes;
        public final double totalKm;
        public final double grossPerKm;
        public final double grossPerHour;
        public final String rating;

        RideOffer(String platform, String category, double price, double pickupKm,
                  double tripKm, int tripMinutes, double totalKm,
                  double grossPerKm, double grossPerHour, String rating) {
            this.platform = platform;
            this.category = category;
            this.price = price;
            this.pickupKm = pickupKm;
            this.tripKm = tripKm;
            this.tripMinutes = tripMinutes;
            this.totalKm = totalKm;
            this.grossPerKm = grossPerKm;
            this.grossPerHour = grossPerHour;
            this.rating = rating;
        }
    }
}
