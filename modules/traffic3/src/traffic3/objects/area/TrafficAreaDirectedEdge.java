package traffic3.objects.area;

import java.util.ArrayList;
import java.util.List;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficNode;

import org.util.xml.element.TagElement;

/**
 *
 */
public class TrafficAreaDirectedEdge extends TrafficObject {

    private TrafficAreaEdge edge;
    private boolean positive;

    // cache
    private TrafficAreaNode[] getNodesCACHE = null;

    /**
     * Constructor.
     * @param wm world manager
     */
    public TrafficAreaDirectedEdge(WorldManager wm) {
        super(wm);
    }

    /**
     * Constructor.
     * @param wm world manager
     * @param id id
     */
    public TrafficAreaDirectedEdge(WorldManager wm, String id) {
        super(wm, id);
    }

    public TrafficAreaDirectedEdge(WorldManager wm, String id, TrafficAreaEdge e, boolean p) {
        super(wm, id);
        setEdge(e, p);
    }

    public void checkObject() {
        
    }

    public boolean getDirection() {
        return positive;
    }
    public void setDirection(boolean p) {
        positive = p;
        createCache();
    }
    public TrafficAreaEdge getEdge() {
        return edge;
    }

    public void setEdge(TrafficAreaEdge e, boolean p) {
        edge = e;
        edge.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    fireChanged();
                }
            });
        positive = p;
        createCache();
    }

    public TrafficAreaNode getFirstNode() {
        TrafficAreaNode[] nodes = edge.getNodes();
        if (positive) {
            return nodes[0];
        } else {
            return nodes[nodes.length - 1];
        }
    }

    public TrafficAreaNode getLastNode() {
        TrafficAreaNode[] nodes = edge.getNodes();
        if (positive) {
            return nodes[nodes.length - 1];
        } else {
            return nodes[0];
        }
    }

    public TrafficArea[] getAreas() {
        return edge.getAreas();
    }
    public Line2D[] getLines() {
        return edge.getLines();
    }

    protected void createCache() {
        TrafficAreaNode[] nodes = edge.getNodes();
        if (positive) {
            getNodesCACHE = nodes;
        }
        else {
            TrafficAreaNode[] newNodes = new TrafficAreaNode[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                newNodes[i] = nodes[nodes.length - 1 - i];
            }
            getNodesCACHE = newNodes;
        }
    }

    public TrafficAreaNode[] getNodes() {
        if (getNodesCACHE == null) {
            createCache();
        }
        return getNodesCACHE;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("TrafficAreaDirectedEdge[");
        sb.append("id:").append(getID()).append(";");
        sb.append("edgeID:").append(edge.getID()).append(";");
        sb.append("areas:{");
        if (edge == null || edge.getAreas() == null) {
            sb.append("null");
        }
        else {
            for (TrafficArea area : edge.getAreas()) {
                if (area == null) {
                    sb.append("null,");;
                }
                else {
                    sb.append(area.getID()).append(",");
                }
            }
        }
        sb.append("};");
        sb.append("nodes:{");
        if (getNodesCACHE == null) {
            sb.append("null");
        }
        else {
            for (int i = 0; i < getNodesCACHE.length; i++) {
                sb.append(getNodesCACHE[i].getID()).append(",");
            }
        }

        sb.append("};");
        sb.append("]");
        return sb.toString();
    }

    /*
    public String toLongString() {
        StringBuffer sb = new StringBuffer();
        return sb.toString();
    }
    */
}
