package com.siit.recipeapp.ui.profile;

import java.util.Locale;

public final class CountryFlag {

    private CountryFlag() {
    }

    // Naziv drzave sa zastavom
    public static String format(String country) {

        if (country == null || country.trim().isEmpty()) {
            return "";
        }

        String name = country.trim();

        for (String code : Locale.getISOCountries()) {

            Locale locale = new Locale.Builder()
                    .setRegion(code)
                    .build();

            if (name.equalsIgnoreCase(
                    locale.getDisplayCountry(Locale.ENGLISH))) {

                int first = 0x1F1E6 + code.charAt(0) - 'A';
                int second = 0x1F1E6 + code.charAt(1) - 'A';

                String flag = new String(Character.toChars(first))
                        + new String(Character.toChars(second));

                return flag + " " + name;
            }
        }

        return name;
    }
}