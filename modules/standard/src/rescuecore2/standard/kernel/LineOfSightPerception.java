package rescuecore2.standard.kernel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import kernel.Perception;
import kernel.AgentProxy;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.config.Config;
import rescuecore2.view.RenderedObject;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.ViewListener;
import rescuecore2.misc.Pair;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.log.Logger;
import rescuecore2.GUIComponent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import rescuecore2.standard.view.StandardWorldModelViewer;
import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.standard.view.BuildingLayer;
import rescuecore2.standard.view.RoadLayer;
import rescuecore2.standard.view.RoadBlockageLayer;
import rescuecore2.standard.view.HumanLayer;

/**
   Line of sight perception.
 */

/*
 * Implementation of Refuge Bed Capacity
 * @author Farshid Faraji
 * May 2020 During Covid-19 :-)))
 * */
public class LineOfSightPerception implements Perception, GUIComponent {
    private static final int DEFAULT_VIEW_DISTANCE = 30000;
    private static final int DEFAULT_HP_PRECISION = 1000;
    private static final int DEFAULT_DAMAGE_PRECISION = 100;
    private static final int DEFAULT_RAY_COUNT = 720;

    private static final String VIEW_DISTANCE_KEY = "perception.los.max-distance";
    private static final String RAY_COUNT_KEY = "perception.los.ray-count";
    private static final String HP_PRECISION_KEY = "perception.los.precision.hp";
    private static final String DAMAGE_PRECISION_KEY = "perception.los.precision.damage";

    private static final IntersectionSorter INTERSECTION_SORTER = new IntersectionSorter();

    private int viewDistance;
    private int hpPrecision;
    private int damagePrecision;
    private int rayCount;

    private StandardWorldModel world;
    private Config config;

    private LOSView view;

    /**
       Create a LineOfSightPerception object.
    */
    public LineOfSightPerception() {
    }

    @Override
    public void initialise(Config newConfig, WorldModel<? extends Entity> model) {
        world = StandardWorldModel.createStandardWorldModel(model);
        this.config = newConfig;
        viewDistance = config.getIntValue(VIEW_DISTANCE_KEY, DEFAULT_VIEW_DISTANCE);
        hpPrecision = config.getIntValue(HP_PRECISION_KEY, DEFAULT_HP_PRECISION);
        damagePrecision = config.getIntValue(DAMAGE_PRECISION_KEY, DEFAULT_DAMAGE_PRECISION);
        rayCount = config.getIntValue(RAY_COUNT_KEY, DEFAULT_RAY_COUNT);
        view = null;
    }

    @Override
    public String toString() {
        return "Line of sight perception";
    }

    @Override
    public JComponent getGUIComponent() {
        if (view == null) {
            view = new LOSView();
            view.refresh();
        }
        return view;
    }

    @Override
    public String getGUIComponentName() {
        return "Line of sight";
    }

    @Override
    public void setTime(int timestep) {
        if (view != null) {
            view.clear();
            view.refresh();
        }
    }

    @Override
    public ChangeSet getVisibleEntities(AgentProxy agent) {
        StandardEntity agentEntity = (StandardEntity)agent.getControlledEntity();
        Logger.debug("Finding visible entities for " + agentEntity);
        ChangeSet result = new ChangeSet();
        // Look for objects within range
        Pair<Integer, Integer> location = agentEntity.getLocation(world);
        if (location != null) {
            Point2D point = new Point2D(location.first(), location.second());
            Collection<StandardEntity> nearby = world.getObjectsInRange(location.first(), location.second(), viewDistance);
            Collection<StandardEntity> visible = findVisible(agentEntity, point, nearby);
            for (StandardEntity next : visible) {
                StandardEntityURN urn = next.getStandardURN();
                switch (urn) {
                case ROAD:
                case HYDRANT:
                    addRoadProperties((Road)next, result);
                    break;
                // the refuge bed and waiting list information is only given to AT
                case REFUGE:
                    if(agentEntity instanceof Human)
                            if (((Human) agentEntity).getPosition(world) == next) {
                                addRefugeProperties((Refuge) next, result);
                            }
                    addBuildingProperties((Building) next, result);
                    break;
                case BUILDING:
                case GAS_STATION:
                case FIRE_STATION:
                case AMBULANCE_CENTRE:
                case POLICE_OFFICE:
                    addBuildingProperties((Building)next, result);
                    break;
                case CIVILIAN:
                case FIRE_BRIGADE:
                case AMBULANCE_TEAM:
                case POLICE_FORCE:
                    // Always send all properties of the agent-controlled object
                    if (next == agentEntity) {
                        addSelfProperties((Human)next, result);
                    }
                    else {
                        addHumanProperties((Human)next, result);
                    }
                    break;
                case BLOCKADE:
                    addBlockadeProperties((Blockade)next, result);
                    break;
                default:
                    // Ignore other types
                    break;
                }
            }

            if(agentEntity instanceof AmbulanceCentre)
            {
                Collection<StandardEntity> refuges = world.getEntitiesOfType(StandardEntityURN.REFUGE);
                for (StandardEntity next : refuges) {
                    addRefugeProperties((Refuge)next, result);
                }
            }

        }
        if (view != null) {
            view.repaint();
        }
        return result;
    }

    private void addRoadProperties(Road road, ChangeSet result) {
        addAreaProperties(road, result);
        // Only update blockades
        result.addChange(road, road.getBlockadesProperty());
        // Also update each blockade
        if (road.isBlockadesDefined()) {
            for (EntityID id : road.getBlockades()) {
                Blockade blockade = (Blockade)world.getEntity(id);
                if (blockade == null) {
                    Logger.error("Blockade " + id + " is null!");
                    Logger.error(road.getFullDescription());
                }
                else {
                    addBlockadeProperties(blockade, result);
                }
            }
        }
    }

    private void addBuildingProperties(Building building, ChangeSet result) {
        addAreaProperties(building, result);
        // Update TEMPERATURE, FIERYNESS and BROKENNESS
        result.addChange(building, building.getTemperatureProperty());
        result.addChange(building, building.getFierynessProperty());
        result.addChange(building, building.getBrokennessProperty());
        result.addChange(building, building.getCapacityProperty());
    }

    public void addRefugeProperties(Refuge refuge, ChangeSet result) {
        result.addChange(refuge, refuge.getBedCapacityProperty());
        result.addChange(refuge, refuge.getOccupiedBedsProperty());
        result.addChange(refuge, refuge.getWaitingListSizeProperty());
        //TODO send other information e.g civilians info in the refuge
    }


    private void addAreaProperties(Area area, ChangeSet result) {
    }

    private void addFarBuildingProperties(Building building, ChangeSet result) {
        // Update FIERYNESS only
        result.addChange(building, building.getFierynessProperty());
    }

    private void addHumanProperties(Human human, ChangeSet result) {
        // Update POSITION, X, Y, DIRECTION, STAMINA, BURIEDNESS, HP, DAMAGE
        result.addChange(human, human.getPositionProperty());
        result.addChange(human, human.getXProperty());
        result.addChange(human, human.getYProperty());
        result.addChange(human, human.getDirectionProperty());
        result.addChange(human, human.getStaminaProperty());
        result.addChange(human, human.getBuriednessProperty());
        // Round HP and damage
        IntProperty hp = (IntProperty)human.getHPProperty().copy();
        roundProperty(hp, hpPrecision);
        result.addChange(human, hp);
        IntProperty damage = (IntProperty)human.getDamageProperty().copy();
        roundProperty(damage, damagePrecision);
        result.addChange(human, damage);
    }

    private void addSelfProperties(Human human, ChangeSet result) {
        // Update human properties and POSITION_HISTORY
        addHumanProperties(human, result);
        result.addChange(human, human.getPositionHistoryProperty());
        // Un-round hp and damage
        result.addChange(human, human.getHPProperty());
        result.addChange(human, human.getDamageProperty());
        if (human instanceof FireBrigade) {
            FireBrigade fb = (FireBrigade)human;
            result.addChange(fb, fb.getWaterProperty());
        }
    }

    private void addBlockadeProperties(Blockade blockade, ChangeSet result) {
        result.addChange(blockade, blockade.getXProperty());
        result.addChange(blockade, blockade.getYProperty());
        result.addChange(blockade, blockade.getPositionProperty());
        result.addChange(blockade, blockade.getApexesProperty());
        result.addChange(blockade, blockade.getRepairCostProperty());
    }

    private void roundProperty(IntProperty p, int precision) {
        if (precision != 1 && p.isDefined()) {
            p.setValue(round(p.getValue(), precision));
        }
    }

    private int round(int value, int precision) {
        int remainder = value % precision;
        value -= remainder;
        if (remainder >= precision / 2) {
            value += precision;
        }
        return value;
    }

    private Collection<StandardEntity> findVisible(StandardEntity agentEntity, Point2D location, Collection<StandardEntity> nearby) {
        Logger.debug("Finding visible entities from " + location);
        Logger.debug(nearby.size() + " nearby entities");
        Collection<LineInfo> lines = getAllLines(nearby);
        // Cast rays
        // CHECKSTYLE:OFF:MagicNumber
        double dAngle = Math.PI * 2 / rayCount;
        // CHECKSTYLE:ON:MagicNumber
        Collection<StandardEntity> result = new HashSet<StandardEntity>();
        for (int i = 0; i < rayCount; ++i) {
            double angle = i * dAngle;
            Vector2D vector = new Vector2D(Math.sin(angle), Math.cos(angle)).scale(viewDistance);
            Ray ray = new Ray(new Line2D(location, vector), lines);
            for (LineInfo hit : ray.getLinesHit()) {
                StandardEntity e = hit.getEntity();
                result.add(e);
            }
            if (view != null) {
                view.addRay(agentEntity, ray);
            }
        }
        // Now look for humans
        for (StandardEntity next : nearby) {
            if (next instanceof Human) {
                Human h = (Human)next;
                if (canSee(agentEntity, location, h, lines)) {
                    result.add(h);
                }
            }
        }
        // Add self
        result.add(agentEntity);
        Logger.debug(agentEntity + " can see " + result);
        return result;
    }

    private boolean canSee(StandardEntity agent, Point2D location, Human h, Collection<LineInfo> lines) {
        if (h.isXDefined() && h.isYDefined()) {
            int x = h.getX();
            int y = h.getY();
            Point2D humanLocation = new Point2D(x, y);
            Ray ray = new Ray(new Line2D(location, humanLocation), lines);
            if (ray.getVisibleLength() >= 1) {
                if (view != null) {
                    view.addRay(agent, ray);
                }
                return true;
            }
        }
        else if (h.isPositionDefined()) {
            if (h.getPosition().equals(agent.getID())) {
                return true;
            }
            Entity e = world.getEntity(h.getPosition());
            if (e instanceof AmbulanceTeam) {
                return canSee(agent, location, (Human)e, lines);
            }
        }
        return false;
    }

    private Collection<LineInfo> getAllLines(Collection<StandardEntity> entities) {
        Collection<LineInfo> result = new LinkedList<LineInfo>();
        for (StandardEntity next : entities) {
            if (next instanceof Building) {
                for (Edge edge : ((Building)next).getEdges()) {
                    Line2D line = edge.getLine();
                    result.add(new LineInfo(line, next, !edge.isPassable()));
                }
            }
            if (next instanceof Road) {
                for (Edge edge : ((Road)next).getEdges()) {
                    Line2D line = edge.getLine();
                    result.add(new LineInfo(line, next, false));
                }
            }
            else if (next instanceof Blockade) {
                int[] apexes = ((Blockade)next).getApexes();
                List<Point2D> points = GeometryTools2D.vertexArrayToPoints(apexes);
                List<Line2D> lines = GeometryTools2D.pointsToLines(points, true);
                for (Line2D line : lines) {
                    result.add(new LineInfo(line, next, false));
                }
            }
            else {
                continue;
            }
        }
        return result;
    }

    private static class Ray {
        /** The ray itself. */
        private Line2D ray;
        /** The visible length of the ray. */
        private double length;
        /** List of lines hit in order. */
        private List<LineInfo> hit;

        public Ray(Line2D ray, Collection<LineInfo> otherLines) {
            this.ray = ray;
            List<Pair<LineInfo, Double>> intersections = new ArrayList<Pair<LineInfo, Double>>();
            // Find intersections with other lines
            for (LineInfo other : otherLines) {
                double d1 = ray.getIntersection(other.getLine());
                double d2 = other.getLine().getIntersection(ray);
                if (d2 >= 0 && d2 <= 1 && d1 > 0 && d1 <= 1) {
                    intersections.add(new Pair<LineInfo, Double>(other, d1));
                }
            }
            Collections.sort(intersections, INTERSECTION_SORTER);
            hit = new ArrayList<LineInfo>();
            length = 1;
            for (Pair<LineInfo, Double> next : intersections) {
                LineInfo l = next.first();
                hit.add(l);
                if (l.isBlocking()) {
                    length = next.second();
                    break;
                }
            }
        }

        public Line2D getRay() {
            return ray;
        }

        public double getVisibleLength() {
            return length;
        }

        public List<LineInfo> getLinesHit() {
            return Collections.unmodifiableList(hit);
        }
    }

    private static class LineInfo {
        private Line2D line;
        private StandardEntity entity;
        private boolean blocking;

        public LineInfo(Line2D line, StandardEntity entity, boolean blocking) {
            this.line = line;
            this.entity = entity;
            this.blocking = blocking;
        }

        public Line2D getLine() {
            return line;
        }

        public StandardEntity getEntity() {
            return entity;
        }

        public boolean isBlocking() {
            return blocking;
        }
    }

    private static class IntersectionSorter implements Comparator<Pair<LineInfo, Double>>, java.io.Serializable {
        @Override
        public int compare(Pair<LineInfo, Double> a, Pair<LineInfo, Double> b) {
            double d1 = a.second();
            double d2 = b.second();
            if (d1 < d2) {
                return -1;
            }
            if (d1 > d2) {
                return 1;
            }
            return 0;
        }
    }

    private class LOSView extends JPanel {
        private transient StandardWorldModelViewer viewer;
        private transient Collection<Ray> rays;
        private transient Map<StandardEntity, Collection<Ray>> sources;
        private transient StandardEntity selected;

        public LOSView() {
            super(new BorderLayout());
            viewer = new StandardWorldModelViewer();
            viewer.removeAllLayers();
            viewer.addLayer(new BuildingLayer());
            viewer.addLayer(new RoadLayer());
            viewer.addLayer(new RoadBlockageLayer());
            viewer.addLayer(new HumanLayer());
            viewer.addLayer(new RayLayer());
            rays = new ArrayList<Ray>();
            sources = new LazyMap<StandardEntity, Collection<Ray>>() {
                @Override
                public Collection<Ray> createValue() {
                    return new HashSet<Ray>();
                }
            };
            selected = null;
            viewer.addViewListener(new ViewListener() {
                    @Override
                    public void objectsClicked(ViewComponent v, List<RenderedObject> objects) {
                        selected = null;
                        for (RenderedObject o : objects) {
                            if (o.getObject() instanceof Human) {
                                selected = (StandardEntity)o.getObject();
                                viewer.repaint();
                            }
                        }
                    }

                    @Override
                    public void objectsRollover(ViewComponent v, List<RenderedObject> objects) {
                    }
                });
            add(viewer, BorderLayout.CENTER);
        }

        public void clear() {
            synchronized (rays) {
                rays.clear();
                sources.clear();
            }
        }

        public void addRay(StandardEntity source, Ray ray) {
            synchronized (rays) {
                rays.add(ray);
                sources.get(source).add(ray);
            }
        }

        public void refresh() {
            viewer.view(world);
        }

        private class RayLayer extends StandardViewLayer {
            @Override
            public Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
                Collection<Ray> toDraw = new HashSet<Ray>();
                synchronized (rays) {
                    if (selected == null) {
                        toDraw.addAll(rays);
                    }
                    else {
                        toDraw.addAll(sources.get(selected));
                    }
                }
                g.setColor(Color.CYAN);
                for (Ray next : toDraw) {
                    Line2D line = next.getRay();
                    Point2D origin = line.getOrigin();
                    Point2D end = line.getPoint(next.getVisibleLength());
                    int x1 = transform.xToScreen(origin.getX());
                    int y1 = transform.yToScreen(origin.getY());
                    int x2 = transform.xToScreen(end.getX());
                    int y2 = transform.yToScreen(end.getY());
                    g.drawLine(x1, y1, x2, y2);
                }
                return new ArrayList<RenderedObject>();
            }

            @Override
            public String getName() {
                return "Line of sight rays";
            }
        }
    }
}
