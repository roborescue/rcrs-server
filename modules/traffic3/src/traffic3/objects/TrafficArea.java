package traffic3.objects;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;
//import rescuecore2.log.Logger;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardWorldModel;

/**
   This class wraps an Area object with some extra information.
 */
public class TrafficArea {
    //    private List<TrafficAreaListener> areaListenerList = new ArrayList<TrafficAreaListener>();
    private Collection<TrafficAgent> agents;

    private List<Line2D> blockingLines;
    private List<Line2D> blockadeLines;
    private List<Line2D> allBlockingLines;

    private Area area;
    private StandardWorldModel world;

    /**
       Construct a TrafficArea.
       @param area The Area to wrap.
       @param world The world model.
    */
    public TrafficArea(final Area area, StandardWorldModel world) {
        this.area = area;
        this.world = world;
        agents = new HashSet<TrafficAgent>();
        blockingLines = null;
        blockadeLines = null;
        allBlockingLines = null;
        area.addEntityListener(new EntityListener() {
                @Override
                public void propertyChanged(Entity e, Property p, Object oldValue, Object newValue) {
                    if (p == area.getBlockadesProperty()) {
                        blockadeLines = null;
                        allBlockingLines = null;
                    }
                    if (p == area.getEdgesProperty()) {
                        blockingLines = null;
                        allBlockingLines = null;
                    }
                }
            });
    }

    /**
       Get the wrapped area.
       @return The wrapped area.
    */
    public Area getArea() {
        return area;
    }

    /**
       Get all lines around this area that block movement.
       @return All area lines that block movement.
    */
    public List<Line2D> getBlockingLines() {
        if (blockingLines == null) {
            blockingLines = new ArrayList<Line2D>();
            for (Edge edge : area.getEdges()) {
                if (!edge.isPassable()) {
                    blockingLines.add(edge.getLine());
                }
            }
        }
        return Collections.unmodifiableList(blockingLines);
    }

    /**
       Get the lines that describe blockades in this area.
       @return All blockade lines.
    */
    public List<Line2D> getBlockadeLines() {
        if (blockadeLines == null) {
            blockadeLines = new ArrayList<Line2D>();
            if (area.isBlockadesDefined()) {
                for (EntityID blockadeID : area.getBlockades()) {
                    Blockade b = (Blockade)world.getEntity(blockadeID);
                    int[] apexes = b.getApexes();
                    for (int i = 0; i < apexes.length - 2; i += 2) {
                        // CHECKSTYLE:OFF:MagicNumber
                        blockadeLines.add(new Line2D(new Point2D(apexes[i], apexes[i + 1]), new Point2D(apexes[i + 2], apexes[i + 3])));
                        // CHECKSTYLE:ON:MagicNumber
                    }
                    // Close the shape
                    blockadeLines.add(new Line2D(new Point2D(apexes[apexes.length - 2], apexes[apexes.length - 1]), new Point2D(apexes[0], apexes[1])));
                }
            }
        }
        return Collections.unmodifiableList(blockadeLines);
    }

    /**
       Get all lines that block movement. This includes impassable edges of the area and all blockade lines.
       @return All movement-blocking lines.
    */
    public List<Line2D> getAllBlockingLines() {
        if (allBlockingLines == null) {
            allBlockingLines = new ArrayList<Line2D>();
            allBlockingLines.addAll(getBlockingLines());
            allBlockingLines.addAll(getBlockadeLines());
        }
        return Collections.unmodifiableList(allBlockingLines);
    }

    /**
       Find out whether this area contains a point (x, y).
       @param x The X coordinate to test.
       @param y The Y coordinate to test.
       @return True if and only if this area contains the specified point.
    */
    public boolean contains(double x, double y) {
        return area.getShape().contains(x, y);
    }

    /**
       Add an agent to this area.
       @param agent The agent to add.
    */
    public void addAgent(TrafficAgent agent) {
        agents.add(agent);
    }

    /**
       Remove an agent from this area.
       @param agent The agent to remove.
    */
    public void removeAgent(TrafficAgent agent) {
        agents.remove(agent);
    }

    /**
       Get all agents in this area.
       @return All agents inside this area.
    */
    public Collection<TrafficAgent> getAgents() {
        return Collections.unmodifiableCollection(agents);
    }

    @Override
    public String toString() {
        return "TrafficArea (" + area + ")";
    }
}
