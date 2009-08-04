/*
 * Created by brenner on 29.03.2005 
 *
 */
package viewer;

import java.awt.Color;

public class ViewerColors {

    private static final Color defaultColor =   Color.CYAN;

    public static final Color UNDAMAGED = Color.GRAY;
    public static final Color BURNING_SMALL_DAMAGE = new Color(176, 176,  56);
    public static final Color BURNING_MEDIUM_DAMAGE = new Color(204, 122,  50);
    public static final Color BURNING_HEAVY_DAMAGE = new Color(160,  52,  52);
    public static final Color ONLY_WATER_DAMAGE = new Color(50, 120, 130);
    public static final Color EXTINGUISHED_SMALL_DAMAGE = new Color(100, 140, 210);
    public static final Color EXTINGUISHED_MEDIUM_DAMAGE = new Color(100, 70, 190);
    public static final Color EXTINGUISHED_HEAVY_DAMAGE = new Color(80, 60, 140);
    public static final Color COMPLETELY_DESTROYED = Color.DARK_GRAY;
    
    public static final Color[] FIRE_COLORS = {
        UNDAMAGED,
        BURNING_SMALL_DAMAGE,
        BURNING_MEDIUM_DAMAGE,
        BURNING_HEAVY_DAMAGE,
        ONLY_WATER_DAMAGE,
        EXTINGUISHED_SMALL_DAMAGE,
        EXTINGUISHED_MEDIUM_DAMAGE,
        EXTINGUISHED_HEAVY_DAMAGE,
        COMPLETELY_DESTROYED
        };

    public static Color getBuildingColorForViewer(int fieryness) {
        if (fieryness < 0 || fieryness >= FIRE_COLORS.length)
            return defaultColor;        
        return FIRE_COLORS[fieryness];
    }

}

