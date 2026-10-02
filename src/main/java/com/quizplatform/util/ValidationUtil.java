package com.quizplatform.util;

import java.util.regex.Pattern;

/**
 * Utility class for basic input validation.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private ValidationUtil() {
    }

    /**
     * Checks if a string is null or whitespace.
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Alias for isBlank.
     */
    public static boolean isNullOrBlank(String str) {
        return isBlank(str);
    }


    /**
     * Validates whether an email string adheres to a standard email format.
     */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
