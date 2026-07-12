package maps.convert.osm2gml.debug;

import java.awt.*;

/**
 * Centralized color constants for the debug visualization subsystem.
 */
@SuppressWarnings("unused")
public class DebugPalette {

    // Background color of the debug canvas.
    public static final Color BACKGROUND = new Color(0xEE, 0xEE, 0xEE);

    // Main color: outlines/fills for existing background objects (buildings, roads).
    public static final Color MAIN_STROKE = new Color(0x9A, 0xA5, 0xB1);
    public static final Color MAIN_FILL = new Color(0x9A, 0xA5, 0xB1, 26);

    // Accent 1: removed objects.
    public static final Color REMOVED_STROKE = new Color(0xD6, 0x45, 0x45);
    public static final Color REMOVED_FILL = new Color(0xD6, 0x45, 0x45, 38);

    // Accent 2: added objects.
    public static final Color CREATED_STROKE = new Color(0x17, 0xA0, 0x56);
    public static final Color CREATED_FILL = new Color(0x17, 0xA0, 0x56, 56);

    // Accent 3: reference/supplementary objects.
    public static final Color REFERENCE_STROKE = new Color(0xC9, 0x7F, 0x1B);
    public static final Color REFERENCE_FILL = new Color(0xC9, 0x7F, 0x1B, 38);

    private DebugPalette() {
        // Utility class; no instances.
    }
}
