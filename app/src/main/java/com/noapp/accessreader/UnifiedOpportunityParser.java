package com.noapp.accessreader;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser unificado para transformar texto acessível/notification em oportunidades.
 * Primeiro reaproveita o parser de corridas e depois tenta formatos de entrega/rota.
 */
public final class UnifiedOpportunityParser {

    private static final Pattern PRICE = Pattern.compile("R\\$\\s*([0-9]+(?:[.,][0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PICKUP = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*km\\s*(?:at[eé]|para)\\s*(?:a|o)?\\s*(?:retirada|coleta|passageiro|embarque)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DELIVERY = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*km\\s*(?:at[eé]|para)\\s*(?:a|o)?\\s*(?:entrega|destino)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_DISTANCE = Pattern.compile("dist[aâ]ncia\\s+total[^0-9]*([0-9]+(?:[.,][0-9]+)?)\\s*km", Pattern.CASE_INSENSITIVE);
    private static final Pattern GENERIC_KM_MIN = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*km[^\\n]*?([0-9]+)\\s*min", Pattern.CASE_INSENSITIVE);
    private static final Pattern MINUTES = Pattern.compile("([0-9]+)\\s*min", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOURS = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*h(?:oras?)?", Pattern.CASE_INSENSITIVE);

    private UnifiedOpportunityParser() {
    }

    public static Opportunity parse(String raw) {
        return parse(raw, "", "ACCESSIBILITY", System.currentTimeMillis());
    }

    public static Opportunity parse(String raw, String sourcePackage, String source, long timestamp) {
        if (raw == null || raw.trim().isEmpty()) return null;

        RideOfferParser.RideOffer ride = RideOfferParser.parse(raw);
        if (ride != null) {
            return Opportunity.fromRide(ride, timestamp, sourcePackage, source);
        }

        String normalized = raw.replace('\u00A0', ' ');
        String platform = detectPlatform(normalized, sourcePackage);
        if (platform == null) return null;

        String type = detectType(platform, normalized);
        if (Opportunity.TYPE_RIDE.equals(type)) return null;

        Double price = firstDouble(PRICE, normalized, 1);
        if (price == null || price <= 0) return null;

        double pickupKm = valueOrZero(firstDouble(PICKUP, normalized, 1));
        double routeKm = valueOrZero(firstDouble(DELIVERY, normalized, 1));
        double totalKm = valueOrZero(firstDouble(TOTAL_DISTANCE, normalized, 1));
        int minutes = parseDurationMinutes(normalized);

        if (totalKm <= 0 && pickupKm > 0 && routeKm > 0) {
            totalKm = pickupKm + routeKm;
        }

        if (totalKm <= 0) {
            Matcher pair = GENERIC_KM_MIN.matcher(normalized);
            if (pair.find()) {
                totalKm = parseNumber(pair.group(1));
                if (minutes <= 0) minutes = parseInt(pair.group(2));
                if (routeKm <= 0) routeKm = totalKm;
            }
        }

        if (totalKm <= 0) return null;

        if (routeKm <= 0) {
            routeKm = Math.max(0, totalKm - pickupKm);
            if (routeKm <= 0) routeKm = totalKm;
        }

        double perKm = price / totalKm;
        double perHour = minutes > 0 ? price / (minutes / 60.0) : 0;
        String rating = classify(perKm);
        String category = detectCategory(platform, normalized);

        return new Opportunity(
                type,
                platform,
                category,
                price,
                pickupKm,
                routeKm,
                minutes,
                totalKm,
                perKm,
                perHour,
                rating,
                timestamp,
                sourcePackage,
                source
        );
    }

    public static String detectPlatform(String text, String packageName) {
        String lower = (text == null ? "" : text).toLowerCase(Locale.ROOT);
        String pkg = (packageName == null ? "" : packageName).toLowerCase(Locale.ROOT);

        if (lower.contains("uber") || pkg.contains("uber")) return "Uber";
        if (lower.contains("99pop") || lower.contains("99 motorista") || lower.contains("99 pop")
                || pkg.contains("taxis99") || pkg.contains("99")) return "99";
        if (lower.contains("ifood") || pkg.contains("ifood")) return "iFood";
        if (lower.contains("mercado livre") || lower.contains("mercadolivre")
                || pkg.contains("mercadolibre") || pkg.contains("mercadolivre")) return "Mercado Livre";
        if (lower.contains("shopee") || pkg.contains("shopee")) return "Shopee";
        if (lower.contains("indrive") || pkg.contains("indriver")) return "inDrive";
        return null;
    }

    private static String detectType(String platform, String text) {
        if ("Uber".equals(platform) || "99".equals(platform) || "inDrive".equals(platform)) {
            return Opportunity.TYPE_RIDE;
        }
        if ("Mercado Livre".equals(platform)
                || text.toLowerCase(Locale.ROOT).contains("rota de entregas")) {
            return Opportunity.TYPE_ROUTE;
        }
        return Opportunity.TYPE_DELIVERY;
    }

    private static String detectCategory(String platform, String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if ("iFood".equals(platform)) {
            if (lower.contains("novo pedido")) return "Pedido";
            return "Entrega";
        }
        if ("Mercado Livre".equals(platform)) return "Rota";
        if ("Shopee".equals(platform)) {
            if (lower.contains("pacote")) return "Pacote";
            return "Entrega";
        }
        return "Oportunidade";
    }

    private static int parseDurationMinutes(String text) {
        Matcher hours = HOURS.matcher(text);
        if (hours.find()) {
            return (int) Math.round(parseNumber(hours.group(1)) * 60.0);
        }

        Matcher minutes = MINUTES.matcher(text);
        if (minutes.find()) return parseInt(minutes.group(1));
        return 0;
    }

    private static String classify(double perKm) {
        if (perKm >= 2.50) return "BOA";
        if (perKm >= 1.70) return "MÉDIA";
        return "RUIM";
    }

    private static Double firstDouble(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) return null;
        return parseNumber(matcher.group(group));
    }

    private static double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }

    private static double parseNumber(String value) {
        return Double.parseDouble(value.replace(',', '.'));
    }

    private static int parseInt(String value) {
        return Integer.parseInt(value);
    }
}
