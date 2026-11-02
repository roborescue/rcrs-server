package rcrs.server.launcher;

import java.util.Locale;

enum RunMode {
    PRECOMPUTE,
    COMPRUN;

    static RunMode parse(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "precompute" -> PRECOMPUTE;
            case "comprun" -> COMPRUN;
            default -> throw new IllegalArgumentException("Unknown mode: " + value);
        };
    }
}
