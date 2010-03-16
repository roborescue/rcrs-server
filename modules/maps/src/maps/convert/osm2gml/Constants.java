package maps.convert.osm2gml;

import java.awt.Color;

/** Useful OSM to GML constants. */
public final class Constants {
    /** The width of roads in m. */
    public static final float ROAD_WIDTH = 7;

    // CHECKSTYLE:OFF:JavadocVariable
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color MAROON = new Color(128, 0, 0);
    public static final Color LIME = new Color(0, 255, 0);
    public static final Color GREEN = new Color(0, 128, 0);
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color NAVY = new Color(0, 0, 128);
    public static final Color FUSCHIA = new Color(255, 0, 255);
    public static final Color GRAY = new Color(128, 128, 128);
    public static final Color OLIVE = new Color(128, 128, 0);
    public static final Color PURPLE = new Color(128, 0, 128);
    public static final Color SILVER = new Color(192, 192, 192);
    public static final Color TEAL = new Color(0, 128, 128);
    public static final Color YELLOW = new Color(255, 255, 0);
    public static final Color AQUA = new Color(0, 255, 255);
    public static final Color ORANGE = new Color(255, 140, 0);

    public static final Color TRANSPARENT_BLACK = new Color(0, 0, 0, 128);
    public static final Color TRANSPARENT_WHITE = new Color(255, 255, 255, 128);
    public static final Color TRANSPARENT_RED = new Color(255, 0, 0, 128);
    public static final Color TRANSPARENT_MAROON = new Color(128, 0, 0, 128);
    public static final Color TRANSPARENT_LIME = new Color(0, 255, 0, 128);
    public static final Color TRANSPARENT_GREEN = new Color(0, 128, 0, 128);
    public static final Color TRANSPARENT_BLUE = new Color(0, 0, 255, 128);
    public static final Color TRANSPARENT_NAVY = new Color(0, 0, 128, 128);
    public static final Color TRANSPARENT_FUSCHIA = new Color(255, 0, 255, 128);
    public static final Color TRANSPARENT_GRAY = new Color(128, 128, 128, 128);
    public static final Color TRANSPARENT_OLIVE = new Color(128, 128, 0, 128);
    public static final Color TRANSPARENT_PURPLE = new Color(128, 0, 128, 128);
    public static final Color TRANSPARENT_SILVER = new Color(192, 192, 192, 128);
    public static final Color TRANSPARENT_TEAL = new Color(0, 128, 128, 128);
    public static final Color TRANSPARENT_YELLOW = new Color(255, 255, 0, 128);
    public static final Color TRANSPARENT_AQUA = new Color(0, 255, 255, 128);
    public static final Color TRANSPARENT_ORANGE = new Color(255, 140, 0, 128);

    public static final Color[] COLOURS = {RED,
                                           GREEN,
                                           BLUE,
                                           MAROON,
                                           LIME,
                                           NAVY,
                                           OLIVE,
                                           PURPLE,
                                           TEAL,
                                           GRAY,
                                           SILVER,
                                           FUSCHIA,
                                           YELLOW,
                                           AQUA,
                                           ORANGE,
                                           BLACK,
                                           WHITE
    };

    public static final Color[] TRANSPARENT_COLOURS = {TRANSPARENT_RED,
                                                       TRANSPARENT_GREEN,
                                                       TRANSPARENT_BLUE,
                                                       TRANSPARENT_MAROON,
                                                       TRANSPARENT_LIME,
                                                       TRANSPARENT_NAVY,
                                                       TRANSPARENT_OLIVE,
                                                       TRANSPARENT_PURPLE,
                                                       TRANSPARENT_TEAL,
                                                       TRANSPARENT_GRAY,
                                                       TRANSPARENT_SILVER,
                                                       TRANSPARENT_FUSCHIA,
                                                       TRANSPARENT_YELLOW,
                                                       TRANSPARENT_AQUA,
                                                       TRANSPARENT_ORANGE,
                                                       TRANSPARENT_BLACK,
                                                       TRANSPARENT_WHITE
    };
    // CHECKSTYLE:ON:JavadocVariable

    private Constants() {
    }
}
