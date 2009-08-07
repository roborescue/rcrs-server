package traffic3.objects.area;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.TrafficNode;
import org.util.xml.element.TagElement;

/**
 *
 */
public class TrafficAreaNode extends TrafficNode {

    /**
     * Constructor.
     * @param wm world manager
     */
    public TrafficAreaNode(WorldManager wm) {
        super(wm);
    }

    /**
     * Constructor.
     * @param wm world manager
     * @param id id
     */
    public TrafficAreaNode(WorldManager wm, String id) {
        super(wm, id);
    }

    /**
     * set properties.
     * @param gmlElement gml element
     * @throws Exception exception
     */
    public void setProperties(TagElement gmlElement) throws WorldManagerException {
        TagElement property = gmlElement.getTagChild("gml:pointProperty");
        String coordinatesText = property.getTagChild("gml:Point").getChildValue("gml:coordinates");
        String[] coordinates = coordinatesText.split(",");
        double x = Double.parseDouble(coordinates[0]);
        double y = Double.parseDouble(coordinates[1]);
        double z = 0;
        if (coordinates.length >= 3) {
            z = Double.parseDouble(coordinates[2]);
        }
        setLocation(x, y, z);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("TrafficAreaNode[");
        sb.append("id:").append(getID()).append(";");
        sb.append("x:").append(getX()).append(";");
        sb.append("y:").append(getY()).append(";");
        sb.append("z:").append(getZ()).append(";");
        sb.append("]");
        return sb.toString();
    }
}
