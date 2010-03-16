package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.ImageIcon;

import rescuecore2.standard.entities.Building;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders building icons.
 */
public class BuildingIconLayer extends StandardEntityViewLayer<Building> {
    private static final int ICON_SIZE = 32;

    private static final ImageIcon FIRE_STATION = new ImageIcon(BuildingLayer.class.getClassLoader().getResource("rescuecore2/standard/view/FireStation-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon POLICE_OFFICE = new ImageIcon(BuildingLayer.class.getClassLoader().getResource("rescuecore2/standard/view/PoliceOffice-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon AMBULANCE_CENTRE = new ImageIcon(BuildingLayer.class.getClassLoader().getResource("rescuecore2/standard/view/AmbulanceCentre-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon REFUGE = new ImageIcon(BuildingLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Refuge-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));

    /**
       Construct a building icon view layer.
     */
    public BuildingIconLayer() {
        super(Building.class);
    }

    @Override
    public String getName() {
        return "Building icons";
    }

    @Override
    public Shape render(Building b, Graphics2D g, ScreenTransform t) {
        ImageIcon icon = null;
        switch (b.getStandardURN()) {
        case REFUGE:
            icon = REFUGE;
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
