package com.kuchumov.springApp.utilites.transliterator;

import java.util.HashMap;
import java.util.Map;

public class Transliterator {

    private final static Map<String, String> CONFORMITY = new HashMap<>();
    public static String transliterate(String str) {
        checkExistOfConformity();

        StringBuilder stringBuilder = new StringBuilder();
        String transliteratedStr;

        for (int i = 0; i < str.length(); i++) {
            String key = str.substring(i, i + 1);
            if (CONFORMITY.containsKey(key.toUpperCase())) {
                if (CONFORMITY.containsKey(key)) {
                    stringBuilder.append(CONFORMITY.get(key));
                } else {
                    stringBuilder.append(CONFORMITY.get(key.toUpperCase()).toLowerCase());
                }
            } else if ((int) key.charAt(0) > 127) {
                stringBuilder.append("?");
            } else {
                stringBuilder.append(key);
            }
        }
        transliteratedStr = stringBuilder.toString();
        return transliteratedStr;
    }

    private static void checkExistOfConformity() {
        if (CONFORMITY.isEmpty()) {
            CONFORMITY.put("А", "A");
            CONFORMITY.put("Б", "B");
            CONFORMITY.put("В", "V");
            CONFORMITY.put("Г", "G");
            CONFORMITY.put("Д", "D");
            CONFORMITY.put("Е", "YE");
            CONFORMITY.put("Ё", "YO");
            CONFORMITY.put("Ж", "G");
            CONFORMITY.put("З", "Z");
            CONFORMITY.put("И", "I");
            CONFORMITY.put("Й", "Y");
            CONFORMITY.put("К", "K");
            CONFORMITY.put("Л", "L");
            CONFORMITY.put("М", "M");
            CONFORMITY.put("Н", "N");
            CONFORMITY.put("О", "O");
            CONFORMITY.put("П", "P");
            CONFORMITY.put("Р", "R");
            CONFORMITY.put("С", "S");
            CONFORMITY.put("Т", "T");
            CONFORMITY.put("У", "U");
            CONFORMITY.put("Ф", "F");
            CONFORMITY.put("Х", "H");
            CONFORMITY.put("Ц", "C");
            CONFORMITY.put("Ч", "CH");
            CONFORMITY.put("Ш", "SH");
            CONFORMITY.put("Щ", "SH");
            CONFORMITY.put("Ъ", "");
            CONFORMITY.put("Ы", "Y");
            CONFORMITY.put("Ь", "");
            CONFORMITY.put("Э", "YE");
            CONFORMITY.put("Ю", "YU");
            CONFORMITY.put("Я", "YA");
        }
    }
}
