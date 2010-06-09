package traffic3.objects;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.awt.geom.Rectangle2D;

//import rescuecore2.log.Logger;

import rescuecore2.misc.geometry.Line2D;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;

import com.infomatiq.jsi.Rectangle;

/**
   This class wraps an Area object with some extra information.
 */
public class TrafficArea {
    //    private List<TrafficAreaListener> areaListenerList = new ArrayList<TrafficAreaListener>();
    private Collection<TrafficAgent> agents;
    private Collection<TrafficBlockade> blocks;

    private List<Line2D> blockingLines;
    private List<Line2D> blockadeLines;
    private List<Line2D> allBlockingLines;

    private Area area;
    private Rectangle bounds;

    /**
       Construct a TrafficArea.
       @param area The Area to wrap.
    */
    public TrafficArea(final Area area) {
        this.area = area;
        agents = new HashSet<TrafficAgent>();
        blocks = new HashSet<TrafficBlockade>();
        blockingLines = null;
        blockadeLines = null;
        allBlockingLines = null;
        Rectangle2D r = area.getShape().getBounds2D();
        bounds = new Rectangle((float)r.getMinX(), (float)r.getMinY(), (float)r.getMaxX(), (float)r.getMaxY());
        /*
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
        */
    }

    /**
       Get the wrapped area.
       @return The wrapped area.
    */
    public Area getArea() {
        return area;
    }

    /**
       Get the bounding rectangle.
       @return The bounding rectangle.
    */
    public Rectangle getBounds() {
        return bounds;
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
            for (TrafficBlockade block : blocks) {
                blockadeLines.addAll(block.getLines());
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

    /**
       Add a TrafficBlockade.
       @param block The blockade to add.
    */
    public void addBlockade(TrafficBlockade block) {
        blocks.add(block);
        clearBlockadeCache();
    }

    /**
       Remove a TrafficBlockade.
       @param block The blockade to remove.
    */
    public void removeBlockade(TrafficBlockade block) {
        blocks.remove(block);
        clearBlockadeCache();
    }

    /**
       Clear any cached blockade information.
    */
    public void clearBlockadeCache() {
        blockadeLines = null;
        allBlockingLines = null;
    }

    /**
       Get all TrafficBlockades inside this area.
       @return All TrafficBlockades in this area.
    */
    public Collection<TrafficBlockade> getBlockades() {
        return Collections.unmodifiableCollection(blocks);
    }

    @Override
    public String toString() {
        return "TrafficArea (" + area + ")";
    }
}
