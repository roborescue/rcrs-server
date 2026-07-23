package maps.convert.osm2gml.debug;

import java.awt.*;

/**
 * Centralized color constants for the debug visualization subsystem.
 *
 * <p>This utility class provides a consistent color palette used across
 * various debugging views to indicate the state and categories of spatial objects.
 */
public class DebugPalette {

    /**
     * The background color of the debug canvas.
     */
    public static final Color BACKGROUND = new Color(0xEE, 0xEE, 0xEE);

    /**
     * A neutral slate color used to indicate background context objects.
     */
    public static final Color SLATE_STROKE = new Color(0x8A, 0x96, 0xA3);

    /**
     * A translucent slate color for filling background context objects.
     */
    public static final Color SLATE_FILL = new Color(0x8A, 0x96, 0xA3, 40);

    /**
     * A vermilion color used to indicate negative, removed, invalid, or rejected objects.
     */
    public static final Color CORAL_STROKE = new Color(0xD5, 0x5E, 0x00);

    /**
     * A translucent vermilion color for filling negative, removed, invalid, or rejected objects.
     */
    public static final Color CORAL_FILL = new Color(0xD5, 0x5E, 0x00, 40);

    /**
     * An amber color used to indicate reference or pending objects,
     * neutral with respect to the positive/negative semantics of moss and coral.
     */
    public static final Color AMBER_STROKE = new Color(0xE6, 0x9F, 0x00);

    /**
     * A translucent amber color for filling reference or pending objects.
     */
    public static final Color AMBER_FILL = new Color(0xE6, 0x9F, 0x00, 55);

    /**
     * A bluish-green color used to indicate positive, added, valid, or accepted objects.
     */
    public static final Color MOSS_STROKE = new Color(0x00, 0x9E, 0x73);

    /**
     * A translucent bluish-green color for filling positive, added, valid, or accepted objects.
     */
    public static final Color MOSS_FILL = new Color(0x00, 0x9E, 0x73, 45);

    /**
     * A dark blue color used as a secondary accent to visually distinguish multiple objects;
     * unlike coral and moss, it carries no positive/negative meaning.
     */
    public static final Color AZURE_STROKE = new Color(0x00, 0x72, 0xB2);

    /**
     * A translucent dark blue color for filling secondary accent objects.
     */
    public static final Color AZURE_FILL = new Color(0x00, 0x72, 0xB2, 40);

    /**
     * A sky blue color used as a tertiary accent to visually distinguish multiple
     * objects; unlike coral and moss, it carries no positive/negative meaning.
     */
    public static final Color SKY_STROKE = new Color(0x56, 0xB4, 0xE9);

    /**
     * A translucent sky blue color for filling tertiary accent objects.
     */
    public static final Color SKY_FILL = new Color(0x56, 0xB4, 0xE9, 55);

    /**
     * A reddish-purple color used to indicate candidates, previews, or best-proposal objects.
     */
    public static final Color VIOLET_STROKE = new Color(0xCC, 0x79, 0xA7);

    /**
     * A translucent reddish-purple color for filling candidates, previews, or best-proposal objects.
     */
    public static final Color VIOLET_FILL = new Color(0xCC, 0x79, 0xA7, 45);

    private DebugPalette() {
        // Utility class; no instances.
    }
}
