package com.siit.recipeapp.domain.service;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

// Lookup service for Open Food Facts open-source database
public final class OpenFoodFactsLookupService {
    public static final class Result {
        public final String name;
        public final String brand;
        public final Double packageQuantity;
        public final String unitId;
        public Result(String name, String brand, Double quantity, String unitId) {
            this.name = name; this.brand = brand; this.packageQuantity = quantity; this.unitId = unitId;
        }
    }

    public Result lookup(String barcode) { return request(barcode); }

    private Result request(String barcode) {
        HttpURLConnection connection = null;
        try {
            String encoded = URLEncoder.encode(barcode, StandardCharsets.UTF_8);
            URL url = new URL("https://world.openfoodfacts.org/api/v3/product/" + encoded
                    + "?fields=product_name,brands,quantity");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setRequestProperty("User-Agent", "RecipeApp/1.0 (student project)");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            try (InputStream stream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
                JSONObject root = new JSONObject(body.toString());
                JSONObject product = root.optJSONObject("product");
                if (product == null || root.optInt("status", 1) == 0) return null;
                String name = clean(product.optString("product_name", null));
                String brand = clean(product.optString("brands", null));
                PackageSize size = parseSize(product.optString("quantity", null));
                if (name == null && brand == null && size == null) return null;
                return new Result(name, brand, size == null ? null : size.quantity,
                        size == null ? null : size.unitId);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String clean(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) return null;
        return value.trim();
    }

    private static final class PackageSize {
        final double quantity; final String unitId;
        PackageSize(double quantity, String unitId) { this.quantity = quantity; this.unitId = unitId; }
    }

    private static PackageSize parseSize(String raw) {
        if (raw == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?i)(\\d+(?:[.,]\\d+)?)\\s*(kg|g|mg|l|ml|cl|pcs?|pieces?)")
                .matcher(raw.trim());
        if (!matcher.find()) return null;
        try {
            double value = Double.parseDouble(matcher.group(1).replace(',', '.'));
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            if (unit.equals("mg")) { value /= 1000.0; unit = "g"; }
            if (unit.equals("cl")) { value *= 10.0; unit = "ml"; }
            if (unit.equals("pc") || unit.equals("pcs") || unit.startsWith("piece")) unit = "piece";
            boolean supported = unit.equals("g") || unit.equals("kg") || unit.equals("l")
                    || unit.equals("ml") || unit.equals("piece");
            return value > 0 && supported ? new PackageSize(value, unit) : null;
        } catch (NumberFormatException ignored) { return null; }
    }
}
