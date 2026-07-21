package maps.convert.osm2gml.debug;

import java.awt.*;

/**
 * Centralized color constants for the debug visualization subsystem.
 */
@SuppressWarnings("unused")
public class DebugPalette {

    // Background color of the debug canvas.
    public static final Color BACKGROUND = new Color(0xEE, 0xEE, 0xEE);

    // Neutral, lightest: thin backdrop context (e.g. "all edges" layer) that
    // should recede behind every other layer. Distinct from BACKGROUND so it
    // remains visible, and lighter than SLATE so it never competes with it.
    public static final Color CONTEXT_STROKE = new Color(0xBB, 0xC2, 0xC9);

    // Neutral, mid: unchanged / intact / background context objects.
    public static final Color SLATE_STROKE = new Color(0x8A, 0x96, 0xA3);
    public static final Color SLATE_FILL = new Color(0x8A, 0x96, 0xA3, 40);

    // Neutral, darkest: hard constraints / absolute barriers (e.g. impassable).
    public static final Color INK_STROKE = new Color(0x22, 0x22, 0x22);
    public static final Color INK_FILL = new Color(0x22, 0x22, 0x22, 40);

    // Vermillion: negative / removed / invalid / rejected.
    public static final Color CORAL_STROKE = new Color(0xD5, 0x5E, 0x00);
    public static final Color CORAL_FILL = new Color(0xD5, 0x5E, 0x00, 40);

    // Amber (yellow-leaning orange): pending / reference / warning.
    // Kept farther from CORAL on the hue wheel than the previous revision
    // so the two no longer read as "both orange-red".
    public static final Color AMBER_STROKE = new Color(0xE6, 0x9F, 0x00);
    public static final Color AMBER_FILL = new Color(0xE6, 0x9F, 0x00, 55);

    // Bluish green: positive / added / valid / accepted.
    public static final Color MOSS_STROKE = new Color(0x00, 0x9E, 0x73);
    public static final Color MOSS_FILL = new Color(0x00, 0x9E, 0x73, 45);

    // Dark blue: secondary category (e.g. "before" state, alternate branch).
    public static final Color AZURE_STROKE = new Color(0x00, 0x72, 0xB2);
    public static final Color AZURE_FILL = new Color(0x00, 0x72, 0xB2, 40);

    // Sky blue: tertiary category, kept distinct from AZURE by lightness so
    // the two blues can appear in the same view without merging visually.
    public static final Color SKY_STROKE = new Color(0x56, 0xB4, 0xE9);
    public static final Color SKY_FILL = new Color(0x56, 0xB4, 0xE9, 55);

    // Reddish purple: candidate / preview / best-proposal.
    public static final Color VIOLET_STROKE = new Color(0xCC, 0x79, 0xA7);
    public static final Color VIOLET_FILL = new Color(0xCC, 0x79, 0xA7, 45);

    private DebugPalette() {
        // Utility class; no instances.
    }
}
