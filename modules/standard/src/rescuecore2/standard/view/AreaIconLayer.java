package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.ImageIcon;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.view.Icons;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders building icons.
 */
public class AreaIconLayer extends StandardEntityViewLayer<Area> {
    private static final int ICON_SIZE = 32;

    private static final ImageIcon FIRE_STATION = Icons.get(BuildingLayer.class,"rescuecore2/standard/view/FireStation-" + ICON_SIZE + "x" + ICON_SIZE + ".png");
    private static final ImageIcon POLICE_OFFICE = Icons.get(BuildingLayer.class,"rescuecore2/standard/view/PoliceOffice-" + ICON_SIZE + "x" + ICON_SIZE + ".png");
    private static final ImageIcon AMBULANCE_CENTRE = Icons.get(BuildingLayer.class,"rescuecore2/standard/view/AmbulanceCentre-" + ICON_SIZE + "x" + ICON_SIZE + ".png");
    private static final ImageIcon REFUGE = Icons.get(BuildingLayer.class,"rescuecore2/standard/view/Refuge-" + ICON_SIZE + "x" + ICON_SIZE + ".png");
    private static final ImageIcon HYDRANT = Icons.get(BuildingLayer.class,"rescuecore2/standard/view/Hydrant-" + ICON_SIZE + "x" + ICON_SIZE + ".png");
    private static final ImageIcon GAS_STATION= Icons.get(BuildingLayer.class,"rescuecore2/standard/view/GasStation-" + ICON_SIZE + "x" + ICON_SIZE + ".png");
    
    /**
       Construct a building icon view layer.
     */
    public AreaIconLayer() {
        super(Area.class);
    }

    @Override
    public String getName() {
        return "Area icons";
    }

    @Override
    public Shape render(Area b, Graphics2D g, ScreenTransform t) {
        ImageIcon icon = null;
        switch (b.getStandardURN()) {
        case REFUGE:
            icon = REFUGE;
            break;
        case GAS_STATION:
            icon = GAS_STATION;
            break;
        case FIRE_STATION:
            icon = FIRE_STATION;
            break;
        case AMBULANCE_CENTRE:
            icon = AMBULANCE_CENTRE;
            break;
        case POLICE_OFFICE:
            icon = POLICE_OFFICE;
            break;
        case HYDRANT:
            icon = HYDRANT;
            break;
        default:
            break;
        }
        if (icon != null) {
            // Draw an icon over the centre of the building
            int x = t.xToScreen(b.getX()) - (icon.getIconWidth() / 2);
            int y = t.yToScreen(b.getY()) - (icon.getIconHeight() / 2);
            icon.paintIcon(null, g, x, y);
        }
        return null;
    }
}
