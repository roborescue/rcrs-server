package traffic3.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Queue;
import java.util.LinkedList;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficArea;
import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.simulator.SimulatorException;

import rescuecore2.standard.entities.Human;

import rescuecore2.log.Logger;

/**
 * A TrafficAgent is a mobile object in the world.
 */
public class TrafficAgent extends TrafficObject {

    /**
     * radius.
     */
    private static final double RADIUS_DEFAULT = 200;
    private static final int D = 2;

    /*
      private final double valueAgentA = 0.0001;
      private final double valueAgentB = 1000.0;
      private final double valueAgentK = 0.000001;
      private final double valueRandomX = Math.random();
      private final double valueRandomY = Math.random();
      
      private final double valueWallA = 0.005; //0.001
      private final double valueWallB = 100.0; //100
      private final double valueWallK = 0.00001;
      private final double valueWallWidth = 0;
    */
    private final double valueAgentA = 0.0001;
    private final double valueAgentB = 1000.0;
    private final double valueAgentK = 0.00001;
    private final double valueRandomX = Math.random();
    private final double valueRandomY = Math.random();
    
    private final double valueWallA = 0.005; //0.001
    private final double valueWallB = 100.0; //100
    private final double valueWallK = 0.00001;
    private final double valueWallWidth = 0;
    
    private double stepDistance = 0;
    // private double stepDistanceMax = 1000;
    
    /**
     * force of going to destination.
     */
    private final double[] destBuf = new double[D];
    
    /**
     * force of avoiding other people.
     */
    private final double[] sumopBuf = new double[D];
    
    /**
     * force of avoiding walls.
     */
    private final double[] sumwBuf = new double[D];
    
    /**
     * Location.
     */
    private final double[] location = new double[D];
    
    /**
     * Velocity.
     */
    private final double[] velocity = new double[D];

    /**
     * Fource.
     */
    private final double[] force = new double[D];
    
    /**
     * Radius.
     */
    private double radius;
    
    /**
     * Type.
     */
    private String type;
    
    /**
     * Colorl.
     */
    private Color renderColor;
    
    /**
     * The destination that this agent wants to go.
     */
    private TrafficAreaNode finalDestination;
    
    /**
     * The destination that this agent wants to go.
     */
    private Queue<TrafficAreaNode> destinationList;
    
    /**
     * now destination.
     */
    private TrafficAreaNode nowDestination;
    
    /**
     * now destination edge.
     */
    private TrafficAreaEdge nowDestinationEdge;
    
    /**
     * limit of velocity.
     */
    private double velocityLimit;
    
    /**
     * Now area that this agent is in.
     * This field can be null, and it means this agent is not in any existed area.
     */
    private TrafficArea nowArea;
    
    /**
     * Whether this agent is now network mode or area mode.
     */
    //private boolean isNetworkMode;
    
    /**
     * Log.
     */
    private double totalDistance = 0;
    
    /**
     * The Last area that agent is in, but except null.
     */
    private TrafficArea lastArea = null;
    
    /**
     *
     */
    private List<Point2D> positionHistory = new ArrayList<Point2D>();
    
    /**
     *
     */
    private boolean savePositionHistory = true;
    
    /**
     * set location count.
     */
    private int setLocationCount = 0;

    private int locationSaveSkipCount = 60;

    private Human human;

    
    /**
     * Constractor.
     * Id, radius, velocityLimit will automatically be set.
     * radius: 200
     * velocityLimit: 0.7+0.1*(Math.random()-0.5);
     * @param worldManager world manager
     */
    public TrafficAgent(WorldManager worldManager, Human human) {
        super(worldManager);
        this.human = human;
        init();
    }
    
    /**
     * Constractor.
     * @param worldManager world manager
     * @param radius radius
     * @param velocityLimit velicity limit
     */
    public TrafficAgent(WorldManager worldManager, double radius, double velocityLimit, Human human) {
        super(worldManager);
        this.human = human;
        init(radius, velocityLimit);
    }
    
    /**
     * Constractor.
     * @param id id
     * @param worldManager world manager
     * @param radius radius
     * @param velocityLimit velicity limit
     */
    public TrafficAgent(WorldManager worldManager, String id, double radius, double velocityLimit, Human human) {
        super(worldManager, id);
        this.human = human;
        init(radius, velocityLimit);
    }

    public Human getHuman() {
        return human;
    }
    
    /**
     * Whether this agent is simulated as network model or area model.
     * @return is network mode
     */
    /*
      public boolean isNetworkMode() {
      return this.isNetworkMode;
      }
    */
    
    /**
     * initialize by default parameter.
     */
    private void init() {
        final double mean = 0.7;
        final double variable = 0.1;
        final double randomMean = 0.5;
        this.radius = RADIUS_DEFAULT; //[mm]
        this.velocityLimit = mean + variable * (Math.random() - randomMean);
        setColor(Color.green);
    }
    
    /**
     * initialize by specified parameter.
     * @param r radius
     * @param vl velocityLimit
     */
    private void init(double r, double vl) {
        this.radius = r;
        this.velocityLimit = vl;
        setColor(Color.green);
    }
    
    /**
     * Get limit of velocity.
     * @return limit of velocity.
     */
    public double getVLimit() {
        return this.velocityLimit;
    }
    
    /**
     * Set limit of velocity.
     * @param vLimit limit of velocity.
     */
    public void setVLimit(double vLimit) {
        this.velocityLimit = vLimit;
    }
    
    /**
     * Get color.
     * @return color
     */
    public Color getColor() {
        return this.renderColor;
    }
    
    /**
     * Set color.
     * @param c color
     */
    public void setColor(Color c) {
        this.renderColor = c;
    }
    
    /**
     * clear log distance. set totalDistance to 0;
     */
    public void clearLogDistance() {
        this.totalDistance = 0;
    }
    
    /**
     * get log distance.
     * @return distance
     */
    public double getLogDistance() {
        return this.totalDistance;
    }
    
    /*
      public void setLocation(TrafficNetworkPoint now_network_point) {
      this.isNetworkMode = true;
      //if()
      this.nowDestination = null;
      now_network_point_ = now_network_point;
      double x = now_network_point_.getX();
      double y = now_network_point_.getY();
      double z = now_network_point_.getZ();
      double dx = x-locationX;
      double dy = y-locationY;
      double dz = y-locationY;
      this.totalDistance += Math.sqrt(dx*dx+dy*dy+dz*dz);
      locationX = x;
      locationY = y;
      locationZ = z;
      if(this.nowArea!=null)
      this.nowArea.removeAgent(this);
      //alert("newtwork move: "+now_network_point);
      }
    */
    
    /**
     * get Area.
     * @return now area
     */
    public TrafficArea getArea() {
        if (this.nowArea != null) {
            this.lastArea = this.nowArea;
            return this.nowArea;
        }
        return this.lastArea;
    }
    
    /**
     * get position histroy.
     * @return position history
     */
    public Point2D[] getPositionHistory() {
        return positionHistory.toArray(new Point2D[0]);
    }
    
    /**
     * clear position history.
     */
    public void clearPositionHistory() {
        positionHistory.clear();
    }
    
    /**
     * set location.
     * @param x location x
     * @param y location y
     * @param z location z
     */
    public void setLocation(double x, double y, double z) {
        // save position history
        setLocationCount++;
        if (setLocationCount % locationSaveSkipCount == 0 && savePositionHistory) {
            positionHistory.add(new Point2D.Double(x, y));
        }

        // save total distance
        double dx = x - location[0];
        double dy = y - location[1];
        totalDistance += Math.sqrt(dx * dx + dy * dy);


        // update location
        location[0] = x;
        location[1] = y;

        // update rtree
        if(bounds != null) {
            double r = radius;
            getManager().move(bounds, (float)(x - r), (float)(y - r), (float)(x + r), (float)(y + r));
        }

        // update now area
        if (nowArea == null || !nowArea.contains(x, y, z)) {
            if (nowArea != null) {
                nowArea.removeAgent(this);
            }
            TrafficArea area = null;
            if (nowArea != null) {
                for (TrafficArea a : nowArea.getNeighborAreas()) {
                    if (a.contains(x, y, z)) {
                        area = a;
                    }
                }
            }
            if (area == null) {
                area = getManager().findArea(x, y);
            }
            if (area == null) {
                nowArea = null;
                Logger.warn("cannot find area of agents: " + this);
            }
            else {
                nowArea = area;
                nowDestination = null;
                nowArea.addAgent(this);
                /*
                  if (nowArea == null)
                  this.isNetworkMode = false;
                  else
                  this.isNetworkMode = !this.nowArea.isSimulateAsOpenSpace();
                */
            }
        }
        //now_network_point_ = null;
    }
    
    private double min=0;

    /**
     * get x.
     * @return x
     */
    public double getX() {
        return location[0];
    }

    /**
     * get y.
     * @return y
     */
    public double getY() {
        return location[1];
    }
    
    /**
     * get fx.
     * @return fx
     */
    public double getFX() {
        return force[0];
    }
    
    /**
     * get fx.
     * @return fx
     */
    public double getFY() {
        return force[1];
    }
    
    /**
     * get vx.
     * @return vx
     */
    public double getVX() {
        return velocity[0];
    }
    
    /**
     * get vx.
     * @return vx
     */
    public double getVY() {
        return velocity[1];
    }
    
    /**
     * set radius.
     * @param r radius
     */
    public void setRadius(double r) {
        this.radius = r;
    }
    
    /**
     * get radius.
     * @return radius
     */
    public double getRadius() {
        return this.radius;
    }
    
    /**
     * set destination list.
     * @param destination destination
     */
    public void setDestination(TrafficAreaNode... dl) {
        if (dl == null) {
            setDestination(new ArrayList<TrafficAreaNode>());
        }
        else {
            setDestination(Arrays.asList(dl));
        }
    }

    public void setDestination(List<? extends TrafficAreaNode> dl) {
        if (dl == null || dl.isEmpty()) {
            finalDestination = null;
            nowDestination = null;
            destinationList = null;
            return;
        }
        destinationList = new LinkedList<TrafficAreaNode>(dl);
        finalDestination = dl.get(dl.size() - 1);
        nowDestination = null;
        //TrafficArea goal = getManager().findArea(destination.getX(), destination.getY());
        Logger.debug(this + " destination set: " + destinationList);
        Logger.debug(this + " final destination set: " + finalDestination);
        plan();
    }
    
    /**
     * get final destination.
     * @return final destination
     */
    public TrafficAreaNode getFinalDestination() {
        return this.finalDestination;
    }
    
    /**
     * get now destination.
     * @return now destination
     */
    public TrafficAreaNode getNowDestination() {
        return this.nowDestination;
    }

    public Queue<TrafficAreaNode> getDestinationList() {
        return destinationList;
    }
    
    /**
     * plan.
     */
    public void plan() {
        if (this.nowDestination == null) {
            try {
                planDestination();
            }
            catch (WorldManagerException e) {
                Logger.error("Error planning destination", e);
            }
        }
        //if (this.isNetworkMode) {
        //      plan_network();
        //}
        //else {
        planArea();
        //}
    }
    
    /*
      public void plan_network() {
      forceX = 0;
      forceY = 0;
      forceZ = 0;
      throw new RuntimeException("not supported network mode");
      }
    */
    
    /**
     * plan area.
     */
    public void planArea() {
        calcDestinationForce(destBuf);
        calcAgentsForce(sumopBuf);
        calcWallsForce(sumwBuf);
        
        force[0] = destBuf[0] + sumopBuf[0] + sumwBuf[0];
        force[1] = destBuf[1] + sumopBuf[1] + sumwBuf[1];
        //forceZ = destBuf[2] + sumopBuf[2];
        
        if (Double.isNaN(force[0]) || Double.isNaN(force[1])) {
            Logger.warn("plan_area(): force is NaN!");
            force[0] = 0;
            force[1] = 0;
        }
    }
    
    private double[] calcDestinationForce(double[] dest) {
        double destx = 0;
        double desty = 0;
        if (this.nowDestination != null) {
            double dx = this.nowDestination.getX() - location[0];
            double dy = this.nowDestination.getY() - location[1];
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (this.nowDestination != this.finalDestination && this.nowDestinationEdge != null && dist < this.nowDestinationEdge.length() / 2) {
                double edist = this.nowDestinationEdge.distance(location[0], location[1]);
                TrafficAreaNode n1 = this.nowDestinationEdge.getNodes()[0];
                TrafficAreaNode n2 = this.nowDestinationEdge.getNodes()[1];
                double ndx = n1.getX() - n2.getX();
                double ndy = n1.getY() - n2.getY();
                //double ndz = n1.getZ() - n2.getZ();
                double ndist = Math.sqrt(ndx * ndx + ndy * ndy);
                ndx /= ndist;
                ndy /= ndist;
                //ndz /= ndist;
                double cdx = location[0] - n2.getX();
                double cdy = location[1] - n2.getY();
                double dd = cdx * ndx + cdy * ndy;
                
                dx = dd * ndx - cdx;
                dy = dd * ndy - cdy;
                
                assert !(dx == 0 && dy == 0);
                //dz = dd * ndz - cdz;
                dist = Math.sqrt(dx * dx + dy * dy);
            }
            if (dist == 0) {
                dx = 0;
                dy = 0;
            }
            else {
                dx /= dist;
                dy /= dist;
                //dz /= dist;
            }
            final double ddd = 0.001;
            if (this.nowDestination == this.finalDestination) {
                dx = Math.min(this.velocityLimit, ddd * dist) * dx;
                dy = Math.min(this.velocityLimit, ddd * dist) * dy;
                //dz = Math.min(this.velocityLimit, 0.001 * dist) * dz;
            }
            else {
                dx = this.velocityLimit * dx;
                dy = this.velocityLimit * dy;
                // dz = this.velocityLimit * dz;
            }
            
            //destx = 0.0001*(dx-velocity[0]);
            //desty = 0.0001*(dy-velocity[1]);
            //destz = 0.0001*(dz-velocityZ);
            
            final double sss2 = 0.0002;
            destx = sss2 * (dx - velocity[0]);
            desty = sss2 * (dy - velocity[1]);
            // destz = sss2 * (dz - velocityZ);
            assert (!Double.isNaN(velocity[0]) && !Double.isNaN(velocity[1]));
            assert (!Double.isNaN(destx) && !Double.isNaN(desty));
        }
        else {
            final double sss = 0.0001;
            destx = sss * (-velocity[0]);
            desty = sss * (-velocity[1]);
            // destz = sss * (-velocityZ);
            assert (!Double.isNaN(destx) && !Double.isNaN(desty));
        }
        
        /*
          if (Double.isNaN(destx) || Double.isNaN(desty)) {
          try{throw new Exception("NaN");}catch(Exception e){e.printStackTrace();}
          destx=desty=0;
          }
        */
        dest[0] = destx;
        dest[1] = desty;
        return dest;
    }
    
    
    private double[] calcAgentsForce(double[] sumop) {
        double sumopx = 0;
        double sumopy = 0;
        double sumopz = 0;
        
        if (this.nowArea == null) {
            Arrays.fill(sumop, 0);
            return sumop;
        }
        
        final double cutoffX = 10000.0;
        final double cutoffY = 10000.0;
        final double randomMax = 0.001;
        final double randomMean = 0.5;
        
        float distance = 3000;
        
        List list = new ArrayList<TrafficObject>();
        getManager().listNearObjects((float)location[0], (float)location[1], distance, list);
        TrafficAgent[] agentList = new TrafficAgent[list.size()];
        //System.out.println(agentList.length);
        for (int i=0; i<agentList.length; i++) {
            agentList[i] = (TrafficAgent)list.get(i);
        }
        
        /*
          TrafficAgent[] agentList = new TrafficAgent[100];
          getManager().listNearObjects((float)location[0], (float)location[1], distance, agentList);
        */
        
        //TrafficArea[] areaList = this.nowArea.getNeighborList();
        //for (int j = -1; j < areaList.length; j++) {
        /*
          TrafficAgent[] agentList = null;
          if (j == -1) {
          agentList = this.nowArea.getAgentList();
          }
          else {
          agentList = areaList[j].getAgentList();
          }
        */
        double opdx;
        double opdy;
        for (int i = 0; i < agentList.length; i++) {
            TrafficAgent op = agentList[i];
            if (op ==null || op == this) {
                continue;
            }
            opdx = op.getX() - location[0];
            /*
              if (opdx < -cutoffX || cutoffX < opdx) {
              continue;
              }
            */
            opdy = op.getY() - location[1];
            /*
              if (opdy < -cutoffY || cutoffY < opdy) {
              continue;
              }
            */
            // double opdz = op.getZ() - locationZ;
            double r = this.radius + op.getRadius();
            double opdist2 = opdx * opdx + opdy * opdy;
            
            if (opdist2 == 0) {
                sumopx += randomMax * (this.valueRandomX - randomMean);
                sumopy += randomMax * (this.valueRandomY - randomMean);
                continue;
            }
            double opdist = Math.sqrt(opdist2);
            //org.util.Handy.show(null,null,opdist,this,op);
            double opdxn = opdx / opdist;
            double opdyn = opdy / opdist;
            // double opdzn = opdz / opdist;
            double opp = r - opdist;
            double tmp = -this.valueAgentA * Math.exp(opp / this.valueAgentB);
            if (Double.isInfinite(tmp)) {
                Logger.warn("calculateAgentsForce(): A result of exp is infinite: exp(" + (opp / this.valueAgentB) + ")");
            }
            else {
                sumopx += tmp * opdxn;
                sumopy += tmp * opdyn;
            }
            if (opp > 0) {
                sumopx += -this.valueAgentK * (opp) * opdxn;
                sumopy += -this.valueAgentK * (opp) * opdyn;
                // sumopz += -this.valueAgentK * (opp) * opdzn;
            }
            //}
        }
        
        double d2 = sumopx * sumopx + sumopy * sumopy;
        // double d = Math.sqrt(sumopx * sumopx + sumopy * sumopy + sumopz * sumopz);
        final double lim = 0.0001;
        final double lim2 = 0.00000001;
        if (d2 > lim2) {
            // System.out.println("limit force: " + d2);
            final double limit = 0.001;
            double lpd = limit / Math.sqrt(d2);
            sumopx *= lpd;
            sumopy *= lpd;
            // sumopz *= lim / d;
        }
        assert (!Double.isNaN(sumopx) && !Double.isNaN(sumopy));
        sumop[0] = sumopx;
        sumop[1] = sumopy;
        return sumop;
    }
    
    
    private double[] calcWallsForce(double[] sumw) {
        //for wall
        double sumwx = 0;
        double sumwy = 0;
        double sumwz = 0;
        if (this.nowArea != null) {
            List<Line2D> lineList = this.nowArea.getAllBlockingLines();
            double r = getRadius() + valueWallWidth;
            double dx;
            double dy;
            double dist;
            final double cutoffDistance = 3000;
            for (Line2D line : lineList) {
                Point2D p1 = line.getP1();
                Point2D p2 = line.getP2();
                double p1p2X = p2.getX() - p1.getX();
                double p1p2Y = p2.getY() - p1.getY();
                double p1pX =  location[0] - p1.getX();
                double p1pY =  location[1] - p1.getY();
                double p1p2Dist = Math.sqrt(p1p2X * p1p2X + p1p2Y * p1p2Y);
                if (p1p2Dist == 0) {
                    continue;
                }
                double d = (p1p2X * p1pX + p1p2Y * p1pY) / p1p2Dist;
                if (d < 0) {
                    dist = p1.distance(location[0], location[1]) - r;
                    dx = (location[0] - p1.getX()) / dist / 2;
                    dy = (location[1] - p1.getY()) / dist / 2;
                }
                else if (p1p2Dist < d) {
                    dist = p2.distance(location[0], location[1]) - r;
                    dx = (location[0] - p2.getX()) / dist / 2;
                    dy = (location[1] - p2.getY()) / dist / 2;
                }
                else {
                    double p1p2NX = p1p2X / p1p2Dist;
                    double p1p2NY = p1p2Y / p1p2Dist;
                    dx = -d * p1p2NX + p1pX;
                    dy = -d * p1p2NY + p1pY;
                    dist = Math.sqrt(dx * dx + dy * dy) - r;
                    dx /= dist;
                    dy /= dist;
                    if (Double.isNaN(dist)) {
                        Logger.warn("c: NaN: Math.sqrt(" + (dx * dx + dy * dy) + "): " + dx + "," + dy + ": " + p1p2Dist);
                    }
                }
                if (dist > cutoffDistance) {
                    continue;
                }
                if (dist < 0) {
                    //System.out.println("mark@");
                    sumwx += valueWallK * (dist) * dx;
                    sumwy += valueWallK * (dist) * dy;
                }
                else {
                    double tmp = valueWallA * Math.exp(-(dist) / valueWallB);
                    if (Double.isInfinite(tmp)) {
                        Logger.warn("calculateWallForce(): A result of exp is infinite: exp(" + (-dist / valueWallB) + ")");
                    }
                    else if (Double.isNaN(tmp)) {
                        Logger.warn("calculateWallForce(): A result of exp is NaN: exp(" + (-(dist) / valueWallB) + ")");
                    }
                    else {
                        sumwx += tmp * dx;
                        sumwy += tmp * dy;
                    }
                }
            }
        }
        if (Double.isNaN(sumwx) || Double.isNaN(sumwy)) {
            sumwx = 0;
            sumwy = 0;
        }
        assert (!Double.isNaN(sumwx) && !Double.isNaN(sumwy));
        
        sumw[0] = sumwx;
        sumw[1] = sumwy;
        return sumw;
    }
    
    private void planDestination() throws WorldManagerException {
        if (this.finalDestination == null) {
            this.nowDestination = null;
            return;
        }
        TrafficAreaNode current = destinationList.peek();
        if (current == null) {
            this.nowDestination = finalDestination;
            return;
        }
        TrafficArea start = getManager().findArea(getX(), getY());
        TrafficArea goal = getManager().findArea(current.getX(), current.getY());
        
        // this block should be changed!
        if (start == null || goal == null) {
            Logger.error("start = " + start + ", goal = " + goal);
            this.nowDestination = this.finalDestination;
            return;
        }
        
        if (start.equals(goal)) {
            // Pop the destination queue and recurse
            destinationList.poll();
            planDestination();
            return;
        }
        
        Map<TrafficArea, Double> traceAreaMap = new HashMap<TrafficArea, Double>();
        Map<TrafficArea, TrafficArea> traceTransfer = new HashMap<TrafficArea, TrafficArea>();
        List<TrafficArea> buf = new ArrayList<TrafficArea>();
        traceAreaMap.put(start, 0.0);
        buf.add(start);
        
        Logger.debug("Tracing to goal");
        Logger.debug("Current position: " + start);
        Logger.debug("Goal: " + goal);

        for (int i = 0; traceTransfer.get(goal) == null; i++) {
            TrafficArea[] tmp = buf.toArray(new TrafficArea[0]);
            buf.clear();
            //            Logger.debug("i = " + i);
            for (TrafficArea target : tmp) {
                double distance = traceAreaMap.get(target);
                Logger.debug("Next target: " + target);
                Logger.debug("Distance: " + distance);
                List<TrafficArea> neighbors = new ArrayList<TrafficArea>();
                for (TrafficArea t : target.getNeighborAreas()) {
                    neighbors.add(t);
                }
                Logger.debug("Neighbours: " + neighbors);
                
                for (TrafficArea n : neighbors) {
                    double newDistance = distance + target.getDistance(n);
                    Logger.debug("Neighbour " + n);
                    Logger.debug("Distance: " + newDistance);
                    Logger.debug("Existing parent: " + traceTransfer.get(n));
                    Logger.debug("Existing distance: " + traceAreaMap.get(n));
                    if (traceAreaMap.get(n) == null || newDistance < traceAreaMap.get(n)) {
                        traceAreaMap.put(n, newDistance);
                        traceTransfer.put(n, target);
                        buf.add(n);
                        Logger.debug("Added " + n);
                    }
                }
            }
            if (i > 1000) {
                StringBuffer sb = new StringBuffer();
                sb.append("cannot trace to goal(step>1000).\n");
                sb.append("start:").append(start).append(";");
                sb.append("goal:").append(goal).append(";");
                Logger.debug(sb.toString());
                setDestination((TrafficAreaNode)null);
                return ;
                //throw new RuntimeException("cannot trace to goal.(step>10000)\n" + sb.toString());
            }
        }
        buf.clear();
        TrafficArea last = goal;
        buf.add(last);
        while (last != start) {
            last = traceTransfer.get(last);
            buf.add(last);
        }
        StringBuffer sblog = new StringBuffer();
        for (int i = 0; i < buf.size(); i++) {
            sblog.append(buf.get(i).getID());
            sblog.append(", ");
        }
        Logger.debug("Path traced: " + sblog.toString());

        TrafficArea tnn = buf.get(buf.size() - 2);
        TrafficAreaEdge[] edgeList = tnn.getConnector(buf.get(buf.size() - 1));
        TrafficAreaEdge selected = edgeList[0];
        double min = selected.distance(location[0], location[1]);
        for (int i = 1; i < edgeList.length; i++) {
            double distance = edgeList[i].distance(location[0], location[1]);
            if (min > distance) {
                min = distance;
                selected = edgeList[i];
            }
        }
        this.nowDestinationEdge = selected;
        TrafficAreaNode[] cons = selected.getNodes();
        double x = (cons[0].getX() + cons[1].getX()) / 2;
        double y = (cons[0].getY() + cons[1].getY()) / 2;
        TrafficAreaNode tan = getManager().createAreaNode(x, y, 0);
        this.nowDestination = tan;
        Logger.debug("nowDestination is now " + tan);
    }
    
    /**
     * step.
     * @param dt dt
     */
    public void step(double dt) {
        /*
          if (this.isNetworkMode) {
          stepDistance += this.velocityLimit * dt;
          velocity[0] = 0;
          velocity[1] = 0;
          velocityZ = 0;
          forceX = 0;
          forceY = 0;
          forceZ = 0;
          }
          else {
        */
        double x = location[0] + dt * velocity[0];
        double y = location[1] + dt * velocity[1];
        //double z = locationZ + dt * velocityZ;
        velocity[0] += dt * force[0];
        velocity[1] += dt * force[1];
        double v = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);
        if (v > this.velocityLimit) {
            //System.err.println("velocity exceeded velocityLimit");
            v /= this.velocityLimit;
            velocity[0] /= v;
            velocity[1] /= v;
            //velocityZ /= v;
        }
        setLocation(x, y, 0);
        /*
          }
        */
    }
    
    /**
     * set type.
     * @param type type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * get type.
     * @return type
     */
    public String getType() {
        return this.type;
    }
    
    /**
     * is checked.
     * @return is checked
     */
    public boolean isChecked() { return checked; }
    
    /**
     * check validation.
     * @throws Exception failed to check validation
     */
    public void checkObject() throws WorldManagerException { checked = true; }
    
    traffic3.manager.RTreeRectangle bounds = new traffic3.manager.RTreeRectangle(this, 0, 0, 0, 0);
    public traffic3.manager.RTreeRectangle getBounds() {
        return bounds;
    }
    
    
    /**
     * to string.
     * @return string
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("TrafficAgent[");
        sb.append("id:").append(getID()).append(";");
        sb.append("x:").append((int)getX()).append(";");
        sb.append("y:").append((int)getY()).append(";");
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * to long string.
     * @return long string
     */
    public String toLongString() {
        StringBuffer sb = new StringBuffer("TrafficAgent[");
        sb.append("id:").append(getID()).append(";");
        sb.append("type:").append(type).append(";");
        sb.append("x:").append(location[0]).append(";");
        sb.append("y:").append(location[1]).append(";");
        sb.append("now-area:").append(this.nowArea).append(";");
        //sb.append("is network mode[").append(this.isNetworkMode).append("]");
        sb.append("now-destination:").append(this.nowDestination).append(";");
        sb.append("final-destination:").append(this.finalDestination).append(";");
        //sb.append("now area:").append().append(";");
        sb.append("]");
        return sb.toString();
    }
}
