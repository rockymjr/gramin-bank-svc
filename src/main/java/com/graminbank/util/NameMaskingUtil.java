package com.graminbank.util;

public class NameMaskingUtil {

    public static String maskName(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return "******* *******";
        }

        String maskedFirst = maskString(firstName);
        String maskedLast = maskString(lastName);

        return maskedFirst + " " + maskedLast;
    }

    private static String maskString(String str) {
        if (str == null || str.isEmpty()) {
            return "*****";
        }

        // Show first letter, mask rest
        char firstLetter = str.charAt(0);
        int maskLength = str.length() - 1;

        return firstLetter + "*".repeat(maskLength);
    }
}
