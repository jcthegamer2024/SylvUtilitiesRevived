package org.sylvarion.sylvUtilities.escalator;

import java.util.Locale;

public enum EscalatorDirection {
    UP,
    DOWN;

    public static EscalatorDirection parse(final String input) {
        if (input == null) {
            return null;
        }
        try {
            return EscalatorDirection.valueOf(input.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }
}

