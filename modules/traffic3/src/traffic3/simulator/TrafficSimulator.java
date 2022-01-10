package traffic3.simulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.GaussianGenerator;

import rescuecore2.GUIComponent;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.gui.ShapeDebugFrame;
import rescuecore2.misc.gui.ShapeDebugFrame.Line2DShapeInfo;
import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKClearArea;
import rescuecore2.standard.messages.AKExtinguish;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.WorldModelListener;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.properties.IntProperty;

import traffic3.manager.TrafficManager;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficArea;
import traffic3.objects.TrafficBlockade;

/**
 * The Area model traffic simulator.
 */
public class TrafficSimulator extends StandardSimulator implements GUIComponent {
  private static final double STEP_TIME_MS = 100;
  private static final double REAL_TIME_S = 60;
  private static final int MICROSTEPS = (int) ((1000.0 / STEP_TIME_MS) * REAL_TIME_S);

  private static final int RESCUE_AGENT_RADIUS = 500;
  private static final int CIVILIAN_RADIUS = 200;
  private static final double RESCUE_AGENT_VELOCITY_MEAN = 0.7;
  private static final double RESCUE_AGENT_VELOCITY_SD = 0.1;
  private static final double CIVILIAN_VELOCITY_MEAN = 0.2;
  private static final double CIVILIAN_VELOCITY_SD = 0.002;

  private TrafficSimulatorGUI gui;

  private TrafficManager manager;

  /**
   * Construct a new TrafficSimulator.
   */
  public TrafficSimulator() {
    manager = new TrafficManager();
    gui = new TrafficSimulatorGUI(manager);
  }

  @Override
  public JComponent getGUIComponent() {
    return gui;
  }

  @Override
  public String getGUIComponentName() {
    return "Traffic simulator";
  }

  @Override
  protected void postConnect() {
    TrafficConstants.init(config);
    manager.clear();
    for (StandardEntity next : model) {
      if (next instanceof Area) {
        convertAreaToTrafficArea((Area) next);
      }
    }
    NumberGenerator<Double> agentVelocityGenerator = new GaussianGenerator(RESCUE_AGENT_VELOCITY_MEAN,
        RESCUE_AGENT_VELOCITY_SD, config.getRandom());
    NumberGenerator<Double> civilianVelocityGenerator = new GaussianGenerator(CIVILIAN_VELOCITY_MEAN,
        CIVILIAN_VELOCITY_SD, config.getRandom());
    for (StandardEntity next : model) {
      if (next instanceof Human) {
        convertHuman((Human) next, agentVelocityGenerator, civilianVelocityGenerator);
      }
      if (next instanceof Blockade) {
        convertBlockade((Blockade) next);
      }
    }
    model.addWorldModelListener(new WorldModelListener<StandardEntity>() {
      @Override
      public void entityAdded(WorldModel<? extends StandardEntity> model, StandardEntity e) {
        if (e instanceof Blockade) {
          convertBlockade((Blockade) e);
        }
      }

      @Override
      public void entityRemoved(WorldModel<? extends StandardEntity> model, StandardEntity e) {
        if (e instanceof Blockade) {
          Blockade b = (Blockade) e;
          TrafficBlockade block = manager.getTrafficBlockade(b);
          block.getArea().removeBlockade(block);
          manager.remove(block);
        }
      }
    });
    gui.initialise();
    manager.cacheInformation(model);
  }

  @Override
  protected void processCommands(KSCommands c, ChangeSet changes) {
    long start = System.currentTimeMillis();
    Logger.info("Timestep " + c.getTime());

    // Clear all destinations and position history
    for (TrafficAgent agent : manager.getAgents()) {
      agent.clearPath();
      agent.clearPositionHistory();
      agent.setMobile(true);
    }
    for (Command next : c.getCommands()) {
      if (next instanceof AKMove) {
        handleMove((AKMove) next);
      }
      if (next instanceof AKLoad) {
        handleLoad((AKLoad) next, changes);
      }
      if (next instanceof AKUnload) {
        handleUnload((AKUnload) next, changes);
      }
      if (next instanceof AKRescue) {
        handleRescue((AKRescue) next, changes);
      }
      if (next instanceof AKClear) {
        handleClear((AKClear) next, changes);
      }
      if (next instanceof AKClearArea) {
        handleClear((AKClearArea) next, changes);
      }
      if (next instanceof AKExtinguish) {
        handleExtinguish((AKExtinguish) next, changes);
      }
    }
    /**
     * Any agents that are dead or in ambulances are immobile, Civilians that are
     * injured are immobile, Agents that are buried are immobile, Civilians in
     * refuges are immobile
     */
    for (StandardEntity next : model) {
      if (next instanceof Human) {
        Human h = (Human) next;
        if (h.isHPDefined() && h.getHP() <= 0) {
          Logger.debug("Agent " + h + " is dead");
          manager.getTrafficAgent(h).setMobile(false);
        }
        if (h.isPositionDefined() && (model.getEntity(h.getPosition()) instanceof AmbulanceTeam)) {
          Logger.debug("Agent " + h + " is in an ambulance");
          manager.getTrafficAgent(h).setMobile(false);
        }
        if (h.isBuriednessDefined() && h.getBuriedness() > 0) {
          Logger.debug("Agent " + h + " is buried");
          manager.getTrafficAgent(h).setMobile(false);
        }
        if (h instanceof Civilian && h.isDamageDefined() && h.getDamage() > 0) {
          Logger.debug("Agent " + h + " is injured");
          manager.getTrafficAgent(h).setMobile(false);
        }
        if (h instanceof Civilian && h.isPositionDefined() && (model.getEntity(h.getPosition()) instanceof Refuge)) {
          Logger.debug("Agent " + h + " is in a refuge");
          manager.getTrafficAgent(h).setMobile(false);
        }
      }
    }
    timestep();
    for (TrafficAgent agent : manager.getAgents()) {
      // Update position and positionHistory for agents that were not
      // loaded or unloaded
      Human human = agent.getHuman();
      if (!agent.isMobile()) {
        human.undefinePositionHistory();
        human.setTravelDistance(0);
        changes.addChange(human, human.getPositionHistoryProperty());
        changes.addChange(human, human.getTravelDistanceProperty());
        continue;
      }
      Point2D[] history = agent.getPositionHistory().toArray(new Point2D[0]);
      int[] historyArray = new int[history.length * 2];
      for (int i = 0; i < history.length; ++i) {
        historyArray[i * 2] = (int) history[i].getX();
        historyArray[(i * 2) + 1] = (int) history[i].getY();
      }
      double x = agent.getX();
      double y = agent.getY();
      TrafficArea location = agent.getArea();
      if (location != null) {
        human.setPosition(location.getArea().getID());
        changes.addChange(human, human.getPositionProperty());
      }
      human.setX((int) x);
      human.setY((int) y);
      human.setPositionHistory(historyArray);
      human.setTravelDistance((int) agent.getTravelDistance());
      changes.addChange(human, human.getXProperty());
      changes.addChange(human, human.getYProperty());
      changes.addChange(human, human.getPositionHistoryProperty());
      changes.addChange(human, human.getTravelDistanceProperty());
    }
    long end = System.currentTimeMillis();
    Logger.info("Timestep " + c.getTime() + " took " + (end - start) + " ms");
  }

  @Override
  protected void handleUpdate(KSUpdate u) {
    clearCache(u);
    super.handleUpdate(u);
  }

  private void clearCache(KSUpdate u) {
    for (EntityID id : u.getChangeSet().getChangedEntities()) {
      StandardEntity entity = model.getEntity(id);
      switch (StandardEntityURN.fromInt(u.getChangeSet().getEntityURN(id))) {
        case BLOCKADE:
          IntProperty blockadeCost = (IntProperty) u.getChangeSet().getChangedProperty(id,
              StandardPropertyURN.REPAIR_COST.getURNId());
          EntityRefProperty position = ((EntityRefProperty) u.getChangeSet().getChangedProperty(id,
              StandardPropertyURN.POSITION.getURNId()));
          if (entity == null || blockadeCost == null
              || ((Blockade) entity).getRepairCost() != blockadeCost.getValue()) {
            if (position != null)
              clearAreaCache(position.getValue());
            else if (entity != null) {
              clearAreaCache(((Blockade) entity).getPosition());

            }
          }
          break;
        case ROAD:
        case HYDRANT:
          if (entity == null)
            continue;
          EntityRefListProperty blockades = (EntityRefListProperty) u.getChangeSet().getChangedProperty(id,
              StandardPropertyURN.BLOCKADES.getURNId());
          if ((!((Road) entity).isBlockadesDefined())
              || !blockades.getValue().containsAll(((Road) entity).getBlockades())
              || !((Road) entity).getBlockades().containsAll(blockades.getValue()))
            clearAreaCache(id);
          break;
        case BUILDING:
        case AMBULANCE_CENTRE:
        case FIRE_STATION:
        case GAS_STATION:
        case POLICE_OFFICE:
        case REFUGE:
        case AMBULANCE_TEAM:
        case POLICE_FORCE:
        case CIVILIAN:
        case FIRE_BRIGADE:
        case WORLD:
        default:
          break;
      }
    }
  }

  private void clearAreaCache(EntityID entityArea) {
    manager.getTrafficArea((Area) model.getEntity(entityArea)).clearBlockadeCache();
  }

  private void convertAreaToTrafficArea(Area area) {
    manager.register(new TrafficArea(area));
  }

  private void convertBlockade(Blockade b) {
    Logger.debug("Converting blockade: " + b.getFullDescription());
    Area a = (Area) model.getEntity(b.getPosition());
    Logger.debug("Area: " + a);
    TrafficArea area = manager.getTrafficArea(a);
    Logger.debug("Traffic area: " + area);
    TrafficBlockade block = new TrafficBlockade(b, area);
    manager.register(block);
    area.addBlockade(block);
  }

  private void convertHuman(Human h, NumberGenerator<Double> agentVelocityGenerator,
      NumberGenerator<Double> civilianVelocityGenerator) {
    double radius = 0;
    double velocityLimit = 0;
    if (h instanceof FireBrigade || h instanceof PoliceForce || h instanceof AmbulanceTeam) {
      radius = RESCUE_AGENT_RADIUS;
      velocityLimit = agentVelocityGenerator.nextValue();
    } else if (h instanceof Civilian) {
      radius = CIVILIAN_RADIUS;
      velocityLimit = civilianVelocityGenerator.nextValue();
    } else {
      throw new IllegalArgumentException("Unrecognised agent type: " + h + " (" + h.getClass().getName() + ")");
    }
    TrafficAgent agent = new TrafficAgent(h, manager, radius, velocityLimit);
    agent.setLocation(h.getX(), h.getY());
    manager.register(agent);
  }

  private void handleMove(AKMove move) {
    Human human = (Human) model.getEntity(move.getAgentID());
    TrafficAgent agent = manager.getTrafficAgent(human);
    EntityID current = human.getPosition();
    if (current == null) {
      Logger.warn("Rejecting move: Agent position is not defined");
      return;
    }
    Entity currentEntity = model.getEntity(human.getPosition());
    if (!(currentEntity instanceof Area)) {
      Logger.warn("Rejecting move: Agent position is not an area: " + currentEntity);
      return;
    }
    Area currentArea = (Area) currentEntity;
    List<EntityID> list = move.getPath();
    List<PathElement> steps = new ArrayList<PathElement>();
    Edge lastEdge = null;
    /**
     * Check that all elements refer to Area instances and build the list of target
     * points Target points between areas are the midpoint of the shared edge
     */
    for (Iterator<EntityID> it = list.iterator(); it.hasNext();) {
      EntityID next = it.next();
      if (next.equals(current)) {
        continue;
      }
      Entity e = model.getEntity(next);
      if (!(e instanceof Area)) {
        Logger.warn("Rejecting move: Entity ID " + next + " is not an area: " + e);
        return;
      }

      Edge edge = currentArea.getEdgeTo(next);
      if (edge == null) {
        Logger.warn("Rejecting move: Entity ID " + next + " is not adjacent to " + currentArea);
        return;
      }
      Area nextArea = (Area) e;

      steps.addAll(getPathElements2(human, currentArea, lastEdge, nextArea, edge));

      current = next;
      currentArea = nextArea;
      lastEdge = edge;
    }
    int targetX = move.getDestinationX();
    int targetY = move.getDestinationY();
    if (targetX == -1 && targetY == -1) {
      targetX = currentArea.getX();
      targetY = currentArea.getY();
    } else if (list.isEmpty()) {
      Logger.warn("Rejecting move: Path is empty");
      return;
    }
    steps.add(new PathElement(current, null, new Point2D(targetX, targetY)));
    agent.setPath(steps);
  }

  private Collection<? extends PathElement> getPathElements(Human human, Area lastArea, Edge lastEdge, Area nextArea,
      Edge nextEdge) {
    if (human.getID().getValue() == 204623396) {
      System.out.println(
          "lastArea=" + lastArea + " lastEdge=" + lastEdge + " nextArea=" + nextArea + " nextEdge=" + nextEdge);
    }
    ArrayList<PathElement> steps = new ArrayList<PathElement>();
    Point2D edgePoint = getBestPoint(nextEdge, nextArea);
    Point2D centrePoint = new Point2D(lastArea.getX(), lastArea.getY());
    if (lastEdge == null) {
      Point2D entracePoint = getEntranceOfArea(nextEdge, lastArea);
      if (entracePoint != null) {
        steps.add(new PathElement(lastArea.getID(), null, entracePoint, centrePoint));
        steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, entracePoint));
      } else
        steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint));

    } else {
      Point2D startEntracePoint = getEntranceOfArea(lastEdge, lastArea);
      if (startEntracePoint != null)
        steps.add(new PathElement(lastArea.getID(), null, startEntracePoint));
      Point2D entracePoint = getEntranceOfArea(nextEdge, lastArea);
      if (entracePoint != null) {
        steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), entracePoint, centrePoint));
        steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, entracePoint));
      } else {
        steps.add(new PathElement(lastArea.getID(), nextEdge.getLine(), edgePoint, centrePoint));
      }
    }

    return steps;
  }

  private boolean haveImpassibleEdge(Area dest) {
    for (Edge edge : dest.getEdges()) {
      if (!edge.isPassable())
        return true;
    }
    return false;
  }

  private Point2D getEntranceOfArea(Edge inComingEdge, Area dest) {

    Point2D edgeMid = getBestPoint(inComingEdge, dest);

    Line2D wallLine = inComingEdge.getLine();

    int distance = 500;
    while (distance > 0) {
      Vector2D offset = wallLine.getDirection().getNormal().normalised().scale(distance);
      Point2D destXY = edgeMid.plus(offset);
      if (dest.getShape().contains(destXY.getX(), destXY.getY())) {
        return destXY;
      }
      offset = wallLine.getDirection().getNormal().normalised().scale(-distance);
      destXY = edgeMid.plus(offset);
      if (dest.getShape().contains(destXY.getX(), destXY.getY())) {
        return destXY;
      }
      distance -= 100;
    }
    return null;

  }

  private Collection<? extends PathElement> getPathElements2(Human human, Area lastArea, Edge lastEdge, Area nextArea,
      Edge nextEdge) {
    Collection<? extends PathElement> originalPaths = getPathElements(human, lastArea, lastEdge, nextArea, nextEdge);
    if (isOriginalPathOk(originalPaths))
      return originalPaths;
    Point2D start;
    if (lastEdge == null)
      start = new Point2D(human.getX(), human.getY());
    else
      start = getBestPoint(lastEdge, lastArea);
    Point2D startpoint;
    if (lastEdge == null)
      startpoint = start;
    else
      startpoint = getMidPoint(lastEdge.getStart(), lastEdge.getEnd());
    Point2D edgePoint = getBestPoint(nextEdge, nextArea);
    Point2D centrePoint = new Point2D(lastArea.getX(), lastArea.getY());

    List<ShapeDebugFrame.ShapeInfo> resultGraph = new ArrayList<ShapeDebugFrame.ShapeInfo>();

    resultGraph.add(
        new Line2DShapeInfo(new Line2D(startpoint, centrePoint), "path start to center", Color.black, false, true));
    resultGraph.add(new Line2DShapeInfo(new Line2D(centrePoint, getMidPoint(nextEdge.getStart(), nextEdge.getEnd())),
        "path center to end", Color.white, false, true));

    TrafficArea trafficArea = manager.getTrafficArea(lastArea);
    int[][] graph = manager.getTrafficArea(lastArea).getGraph();
    List<Line2D> oLines = manager.getTrafficArea(lastArea).getOpenLines();
    List<Line2D> graphline = new ArrayList<>();
    resultGraph.add(new Line2DShapeInfo(oLines, "openLines", Color.green, false, false));
    for (int i = 0; i < graph.length; i++) {
      for (int j = 0; j < graph.length; j++) {
        if (graph[i][j] > 10000)
          continue;
        Line2D line = new Line2D(getMidPoint(oLines.get(i).getOrigin(), oLines.get(i).getEndPoint()),
            getMidPoint(oLines.get(j).getOrigin(), oLines.get(j).getEndPoint()));
        graphline.add(line);
      }
    }

    int src = trafficArea.getNearestLineIndex(start);
    int end = trafficArea.getNearestLineIndex(edgePoint);

    if (src != end && src != -1 && end != -1) {
      Dijkstra dijkstra = new Dijkstra(graph.length);
      try {
        dijkstra.Run(graph, src);
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        if (dijkstra.getWeight(end) < 1000) {
          ArrayList<Integer> path = dijkstra.getpathArray(end);
          if (path.size() > 2) {
            List<Point2D> points = new ArrayList<Point2D>();
            for (Integer integer : path) {
              Point2D point = getMidPoint(oLines.get(integer).getOrigin(), oLines.get(integer).getEndPoint());
              points.add(point);
            }

            ArrayList<PathElement> result = new ArrayList<PathElement>();
            result.add(new PathElement(nextArea.getID(), nextEdge.getLine(), start));

            for (Point2D point : points)
              result.add(new PathElement(nextArea.getID(), nextEdge.getLine(), point));

            result.add(new PathElement(nextArea.getID(), nextEdge.getLine(), edgePoint));

            return result;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return originalPaths;
  }

  private boolean isOriginalPathOk(Collection<? extends PathElement> originalPaths) {
    if (originalPaths.isEmpty()) {
      Logger.warn("originalPaths is null");
      return true;
    }

    TrafficArea lastArea = null;
    ArrayList<PathElement> SameAreaElements = new ArrayList<>();
    for (PathElement pathElement : originalPaths) {

      TrafficArea area = manager.getTrafficArea((Area) model.getEntity(pathElement.getAreaID()));
      for (TrafficBlockade block : area.getBlockades()) {
        if (block.getBlockade().getShape().contains(pathElement.getGoal().getX(), pathElement.getGoal().getY()))
          return false;
      }
      double minDistance = getMinDistance(area.getAllBlockingLines(), pathElement.getGoal());

      if (minDistance < TrafficSimulator.RESCUE_AGENT_RADIUS / 2)
        return false;
      if (lastArea == null || lastArea == area) {
        SameAreaElements.add(pathElement);
      } else {
        if (!checkElements(lastArea, SameAreaElements))
          return false;
        SameAreaElements.clear();
      }
      lastArea = area;
    }
    if (!checkElements(lastArea, SameAreaElements))
      return false;
    return true;
  }

  private boolean checkElements(TrafficArea lastArea, List<PathElement> sameAreaElements) {
    if (sameAreaElements.size() <= 1)
      return true;

    for (int i = 1; i < sameAreaElements.size(); i++) {
      Line2D line2D = new Line2D(sameAreaElements.get(i - 1).getGoal(), sameAreaElements.get(i).getGoal());
      for (Line2D block : lastArea.getAllBlockingLines()) {
        if (GeometryTools2D.getSegmentIntersectionPoint(line2D, block) != null)
          return false;
      }
    }
    return true;
  }

  static Point2D getMidPoint(Point2D p1, Point2D p2) {
    return new Point2D((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
  }

  private Point2D getTransivit(Point2D base, Point2D p1) {
    return new Point2D((base.getX() - (p1.getX() - base.getX())), (base.getY() - (p1.getY() - base.getY())));

  }

  private Point2D getBestPoint(Edge edge, Area dest) {
    return getMidPoint(edge.getStart(), edge.getEnd());
  }

  private double getMinDistance(List<Line2D> blockingLines, Point2D point) {
    double min = Integer.MAX_VALUE;
    for (Line2D block : blockingLines) {
      Point2D tempPoint = GeometryTools2D.getClosestPointOnSegment(block, point);
      double tempDistance = GeometryTools2D.getDistance(point, tempPoint);
      if (tempDistance < min)
        min = tempDistance;
    }
    return min;

  }

  // Return the loaded civilian (if any)
  private Civilian handleLoad(AKLoad load, ChangeSet changes) {
    EntityID agentID = load.getAgentID();
    EntityID targetID = load.getTarget();
    Entity agent = model.getEntity(agentID);
    Entity target = model.getEntity(targetID);
    if (agent == null) {
      Logger.warn("Rejecting load command from agent " + agentID + ": agent does not exist");
      return null;
    }
    if (!(agent instanceof AmbulanceTeam)) {
      Logger.warn("Rejecting load command from agent " + agentID + ": agent type is " + agent.getURN());
      return null;
    }
    if (target == null) {
      Logger.warn("Rejecting load command from agent " + agentID + ": target does not exist " + targetID);
      return null;
    }
    if (!(target instanceof Civilian)) {
      Logger.warn(
          "Rejecting load command from agent " + agentID + ": target " + targetID + " is of type " + target.getURN());
      return null;
    }
    AmbulanceTeam at = (AmbulanceTeam) agent;
    Civilian h = (Civilian) target;
    if (at.isHPDefined() && at.getHP() <= 0) {
      Logger.warn("Rejecting load command from agent " + agentID + ": agent is dead");
      return null;
    }
    if (at.isBuriednessDefined() && at.getBuriedness() > 0) {
      Logger.warn("Rejecting load command from agent " + agentID + ": agent is buried");
      return null;
    }
    if (h.isBuriednessDefined() && h.getBuriedness() > 0) {
      Logger.warn("Rejecting load command from agent " + agentID + ": target " + targetID + " is buried");
      return null;
    }
    if (!h.isPositionDefined() || !at.isPositionDefined() || !h.getPosition().equals(at.getPosition())) {
      Logger.warn("Rejecting load command from agent " + agentID + ": target is non-adjacent " + targetID);
      return null;
    }
    if (h.getID().equals(at.getID())) {
      Logger.warn("Rejecting load command from agent " + agentID + ": tried to load self");
      return null;
    }
    // Is there something already loaded?
    for (Entity e : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
      Civilian c = (Civilian) e;
      if (c.isPositionDefined() && agentID.equals(c.getPosition())) {
        Logger.warn(
            "Rejecting load command from agent " + agentID + ": agent already has civilian " + c.getID() + " loaded");
        return null;
      }
    }
    // All checks passed: do the load
    h.setPosition(agentID);
    h.undefineX();
    h.undefineY();
    changes.addChange(h, h.getPositionProperty());
    changes.addChange(h, h.getXProperty());
    changes.addChange(h, h.getYProperty());
    manager.getTrafficAgent(at).setMobile(false);
    manager.getTrafficAgent(h).setMobile(false);
    Logger.debug(at + " loaded " + h);
    return h;
  }

  // Return the unloaded civilian (if any)
  private Civilian handleUnload(AKUnload unload, ChangeSet changes) {
    EntityID agentID = unload.getAgentID();
    Entity agent = model.getEntity(agentID);
    if (agent == null) {
      Logger.warn("Rejecting unload command from agent " + agentID + ": agent does not exist");
      return null;
    }
    if (!(agent instanceof AmbulanceTeam)) {
      Logger.warn("Rejecting unload command from agent " + agentID + ": agent type is " + agent.getURN());
      return null;
    }
    AmbulanceTeam at = (AmbulanceTeam) agent;
    if (!at.isPositionDefined() || !at.isXDefined() || !at.isYDefined()) {
      Logger.warn("Rejecting unload command from agent " + agentID + ": could not locate agent");
      return null;
    }
    if (at.isHPDefined() && at.getHP() <= 0) {
      Logger.warn("Rejecting unload command from agent " + agentID + ": agent is dead");
      return null;
    }
    if (at.isBuriednessDefined() && at.getBuriedness() > 0) {
      Logger.warn("Rejecting unload command from agent " + agentID + ": agent is buried");
      return null;
    }
    // Is there something loaded?
    Civilian target = null;
    Logger.debug("Looking for civilian carried by " + agentID);
    for (Entity e : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
      Civilian c = (Civilian) e;
      Logger.debug(c + " is at " + c.getPosition());
      if (c.isPositionDefined() && agentID.equals(c.getPosition())) {
        target = c;
        Logger.debug("Found civilian " + c);
        break;
      }
    }
    if (target == null) {
      Logger.warn("Rejecting unload command from agent " + agentID + ": agent is not carrying any civilians");
      return null;
    }
    // All checks passed
    target.setPosition(at.getPosition());
    target.setX(at.getX());
    target.setY(at.getY());
    changes.addChange(target, target.getPositionProperty());
    changes.addChange(target, target.getXProperty());
    changes.addChange(target, target.getYProperty());
    for (TrafficAgent trafficAgent : manager.getAgents()) {
      if (trafficAgent.getHuman() == target) {
        trafficAgent.setLocation(at.getX(), at.getY());
        trafficAgent.clearPath();
      }
    }
    manager.getTrafficAgent(at).setMobile(false);
    manager.getTrafficAgent(target).setMobile(false);
    Logger.debug(at + " unloaded " + target);
    return target;
  }

  private void handleClear(AKClear clear, ChangeSet changes) {
    // Agents clearing roads are not mobile
    EntityID agentID = clear.getAgentID();
    Entity agent = model.getEntity(agentID);
    if (agent instanceof Human) {
      manager.getTrafficAgent((Human) agent).setMobile(false);
      Logger.debug(agent + " is clearing");
    }
  }

  private void handleClear(AKClearArea clear, ChangeSet changes) {
    EntityID agentID = clear.getAgentID();
    Entity agent = model.getEntity(agentID);
    if (agent instanceof Human) {
      manager.getTrafficAgent((Human) agent).setMobile(false);
      Logger.debug(agent + " is clearing");
    }
  }

  private void handleRescue(AKRescue rescue, ChangeSet changes) {
    // Agents rescuing civilians are not mobile
    EntityID agentID = rescue.getAgentID();
    Entity agent = model.getEntity(agentID);
    if (agent instanceof Human) {
      manager.getTrafficAgent((Human) agent).setMobile(false);
      Logger.debug(agent + " is rescuing");
    }
  }

  private void handleExtinguish(AKExtinguish ex, ChangeSet changes) {
    // Agents extinguishing fires are not mobile
    EntityID agentID = ex.getAgentID();
    Entity agent = model.getEntity(agentID);
    if (agent instanceof Human) {
      manager.getTrafficAgent((Human) agent).setMobile(false);
      Logger.debug(agent + " is extinguishing");
    }
  }

  private void timestep() {
    long start = System.currentTimeMillis();
    for (TrafficAgent agent : manager.getAgents()) {
      agent.beginTimestep();
    }
    long pre = System.currentTimeMillis();
    Logger.debug("Running " + MICROSTEPS + " microsteps");
    for (int i = 0; i < MICROSTEPS; i++) {
      microstep();
    }

    long post = System.currentTimeMillis();
    for (TrafficAgent agent : manager.getAgents()) {
      agent.endTimestep();
    }
    long end = System.currentTimeMillis();
    if (manager.getAgents().size() != 0) {
      Logger.debug("Pre-timestep took " + (pre - start) + " ms (average " + ((pre - start) / manager.getAgents().size())
          + "ms per agent)");
      Logger.debug("Microsteps took: " + (post - pre) + "ms (average " + ((post - pre) / MICROSTEPS) + "ms)");
      Logger.debug("Post-timestep took " + (end - post) + " ms (average " + ((end - post) / manager.getAgents().size())
          + "ms per agent)");
    }
    Logger.debug("Total time: " + (end - start) + "ms");
  }

  private void microstep() {
    for (TrafficAgent agent : manager.getAgents()) {
      agent.step(STEP_TIME_MS);
    }
    gui.refresh();
  }
}