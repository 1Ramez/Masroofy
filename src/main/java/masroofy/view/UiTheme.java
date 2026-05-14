package masroofy.view;

/**
 * Centralized UI palette for a consistent "sleek" theme across scenes.
 */
public final class UiTheme {

    private static String mode = "dark";

    public static String BG = "#0B0F17";
    public static String SURFACE = "#121826";
    public static String SURFACE_2 = "#0F1522";
    public static String BORDER = "#243043";

    public static String TEXT = "#E6EDF3";
    public static String TEXT_MUTED = "#9AA4B2";
    public static String TEXT_DIM = "#6B7280";

    public static String ACCENT = "#C9A84C";
    public static String SUCCESS = "#3FB950";
    public static String DANGER = "#F85149";

    private UiTheme() {
    }

    public static String getMode() {
        return mode;
    }

    public static boolean isLight() {
        return "light".equalsIgnoreCase(mode);
    }

    public static void applyDark() {
        mode = "dark";
        BG = "#0B0F17";
        SURFACE = "#121826";
        SURFACE_2 = "#0F1522";
        BORDER = "#243043";

        TEXT = "#E6EDF3";
        TEXT_MUTED = "#9AA4B2";
        TEXT_DIM = "#6B7280";

        ACCENT = "#C9A84C";
        SUCCESS = "#3FB950";
        DANGER = "#F85149";
    }

    public static void applyLight() {
        mode = "light";
        BG = "#F7FAFC";
        SURFACE = "#FFFFFF";
        SURFACE_2 = "#F1F5F9";
        BORDER = "#CBD5E1";

        TEXT = "#0F172A";
        TEXT_MUTED = "#334155";
        TEXT_DIM = "#64748B";

        ACCENT = "#B08A1A";
        SUCCESS = "#15803D";
        DANGER = "#B91C1C";
    }
}
