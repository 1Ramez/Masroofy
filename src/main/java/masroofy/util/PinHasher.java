package masroofy.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Utility for hashing and verifying PINs stored in the local SQLite database.
 */
public final class PinHasher {

    private PinHasher() {
    }

    public static String sha256Hex(String raw) {
        if (raw == null)
            raw = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // Should never happen on a standard JRE.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String rawPin, String storedPin) {
        if (storedPin == null)
            return false;
        String raw = rawPin == null ? "" : rawPin;
        String stored = storedPin.trim();
        if (stored.length() == 64 && stored.matches("[0-9a-fA-F]{64}")) {
            return sha256Hex(raw).equalsIgnoreCase(stored);
        }
        return raw.equals(stored);
    }
}

