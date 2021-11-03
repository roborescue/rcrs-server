/*
 * Last change: $Date: 2005/03/17 06:07:08 $
 * $Revision: 1.14 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import rescuecore.commands.KASense;
import rescuecore.commands.Update;
import rescuecore.event.MemoryListener;
import rescuecore.event.ObjectAddedEvent;
import rescuecore.event.ObjectChangedEvent;
import rescuecore.event.PropertyChangedEvent;
import rescuecore.event.PropertyListener;
import rescuecore.objects.Building;
import rescuecore.objects.Edge;
import rescuecore.objects.Humanoid;
import rescuecore.objects.MotionlessObject;
import rescuecore.objects.MovingObject;
import rescuecore.objects.Node;
import rescuecore.objects.River;
import rescuecore.objects.RiverNode;
import rescuecore.objects.Road;
import rescuecore.objects.Vertex;

/**
 * This class holds an agents view of the world
 */
public abstract class Memory implements java.io.Serializable {
  private int minX, maxX, width, minY, maxY, height;
  private transient Collection<MemoryListener> listeners;
  private final Object LOCK = Integer.valueOf(0);

  private class InternalPropertyListener implements PropertyListener, java.io.Serializable {
    public void propertyChanged(PropertyChangedEvent event) {
      fireObjectChanged(event.getObject(), event.getProperty(), event.getTimestamp(), event.getSource());
    }
  }

  private final PropertyListener PROPERTY_LISTENER = new InternalPropertyListener();

  /**
   * Construct a new empty memory
   */
  protected Memory() {
    listeners = new HashSet<MemoryListener>();
    width = -1;
  }

  public abstract Memory copy();

  /**
   * Get the minimum x value of the world
   *
   * @return The minimum x value of the world
   */
  public int getMinX() {
    if (width == -1)
      calculateDimensions();
    return minX;
  }

  /**
   * Get the maximum x value of the world
   *
   * @return The maximum x value of the world
   */
  public int getMaxX() {
    if (width == -1)
      calculateDimensions();
    return maxX;
  }

  /**
   * Get the minimum y value of the world
   *
   * @return The minimum y value of the world
   */
  public int getMinY() {
    if (width == -1)
      calculateDimensions();
    return minY;
  }

  /**
   * Get the maximum y value of the world
   *
   * @return The maximum y value of the world
   */
  public int getMaxY() {
    if (width == -1)
      calculateDimensions();
    return maxY;
  }

  /**
   * Get the width of the world
   *
   * @return The width of the world
   */
  public int getWidth() {
    if (width == -1)
      calculateDimensions();
    return width;
  }

  /**
   * Get the height of the world
   *
   * @return The height of the world
   */
  public int getHeight() {
    if (width == -1)
      calculateDimensions();
    return height;
  }

  private void calculateDimensions() {
    Collection<RescueObject> allNodes = getObjectsOfType(RescueConstants.TYPE_NODE);
    if (allNodes.size() == 0) {
      minX = 0;
      maxX = 1;
      minY = 0;
      maxY = 1;
    } else {
      Iterator it = allNodes.iterator();
      Node n = (Node) it.next();
      minX = n.getX();
      minY = n.getY();
      maxX = minX;
      maxY = minY;
      while (it.hasNext()) {
        n = (Node) it.next();
        int x = n.getX();
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        int y = n.getY();
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
      }
    }
    width = maxX - minX;
    height = maxY - minY;
  }

  /**
   * Add a MemoryListener that will be informed of adds and updates
   *
   * @param l The MemoryListener to add
   */
  public void addMemoryListener(MemoryListener l) {
    synchronized (LOCK) {
      if (listeners == null)
        listeners = new HashSet<MemoryListener>();
      listeners.add(l);
    }
  }

  /**
   * Remove a MemoryListener
   *
   * @param l The MemoryListener to remove
   */
  public void removeMemoryListener(MemoryListener l) {
    synchronized (LOCK) {
      if (listeners == null)
        listeners = new HashSet<MemoryListener>();
      listeners.remove(l);
    }
  }

  /**
   * Look up a RescueObject by id
   *
   * @param id The id of the object we want
   * @return The object with the given id, or null if there are no objects with
   *         that id
   */
  public abstract RescueObject lookup(int id);

  /**
   * Get all objects in memory
   *
   * @return An array of all objects in memory
   */
  // public abstract RescueObject[] getAllObjects();

  /**
   * Get all objects in memory as a Collection
   *
   * @return A Collection of all objects in memory
   */
  public abstract Collection<RescueObject> getAllObjects();

  /**
   * Get all objects of particular types
   *
   * @param result The Collection we want to fill with RescueObjects. Implementers
   *               should not remove any items from this Collection.
   * @param type   The types we want
   * @see RescueConstants#TYPE_CIVILIAN
   * @see RescueConstants#TYPE_FIRE_BRIGADE
   * @see RescueConstants#TYPE_AMBULANCE_TEAM
   * @see RescueConstants#TYPE_POLICE_FORCE
   * @see RescueConstants#TYPE_ROAD
   * @see RescueConstants#TYPE_NODE
   * @see RescueConstants#TYPE_RIVER
   * @see RescueConstants#TYPE_RIVER_NODE
   * @see RescueConstants#TYPE_BUILDING
   * @see RescueConstants#TYPE_REFUGE
   * @see RescueConstants#TYPE_FIRE_STATION
   * @see RescueConstants#TYPE_AMBULANCE_CENTER
   * @see RescueConstants#TYPE_POLICE_OFFICE
   * @see RescueConstants#TYPE_WORLD
   * @see RescueConstants#TYPE_CAR
   */
  public abstract void getObjectsOfType(Collection<RescueObject> result, int... types);

  public Collection<RescueObject> getObjectsOfType(int... types) {
    Collection<RescueObject> result = new HashSet<RescueObject>();
    getObjectsOfType(result, types);
    return result;
  }

  /**
   * Get all objects of a particular internal type
   *
   * @param type The type we want
   * @return An array of all objects of the given internal type
   * @see RescueConstants#INTERNAL_TYPE_CIVILIAN
   * @see RescueConstants#INTERNAL_TYPE_FIRE_BRIGADE
   * @see RescueConstants#INTERNAL_TYPE_AMBULANCE_TEAM
   * @see RescueConstants#INTERNAL_TYPE_POLICE_FORCE
   * @see RescueConstants#INTERNAL_TYPE_CAR
   * @see RescueConstants#INTERNAL_TYPE_BUILDING
   * @see RescueConstants#INTERNAL_TYPE_REFUGE
   * @see RescueConstants#INTERNAL_TYPE_FIRE_STATION
   * @see RescueConstants#INTERNAL_TYPE_POLICE_OFFICE
   * @see RescueConstants#INTERNAL_TYPE_AMBULANCE_CENTER
   * @see RescueConstants#INTERNAL_TYPE_ROAD
   * @see RescueConstants#INTERNAL_TYPE_NODE
   * @see RescueConstants#INTERNAL_TYPE_RIVER
   * @see RescueConstants#INTERNAL_TYPE_RIVER_NODE
   * @see RescueConstants#INTERNAL_TYPE_WORLD
   * @see RescueConstants#INTERNAL_TYPE_ANY_BUILDING
   * @see RescueConstants#INTERNAL_TYPE_ANY_HUMANOID
   */
  // public abstract RescueObject[] getObjectsOfInternalType(int type);

  /**
   * Add a new object with a null source
   *
   * @param o         The new object
   * @param timestamp The time that this object is added
   */
  public void add(RescueObject o, int timestamp) {
    add(o, timestamp, null);
  }

  /**
   * Add a new object
   *
   * @param o         The new object
   * @param timestamp The time that this object is added
   * @param source    The source of the change
   */
  public void add(RescueObject o, int timestamp, Object source) {
    fireObjectAdded(o, timestamp, source);
    o.addPropertyListener(PROPERTY_LISTENER);
  }

  /**
   * Remove an object from the memory
   *
   * @param o The object to be removed
   */
  public void remove(RescueObject o) {
    o.removePropertyListener(PROPERTY_LISTENER);
  }

  /**
   * Update our memory from a KASense object
   *
   * @param sense The KASense object to update from
   */
  public void update(KASense sense) {
    update(sense.getUpdatedObjects(), sense.getTime(), RescueConstants.SOURCE_SENSE);
  }

  /**
   * Update our memory from an Update object
   *
   * @param sense The Update object to update from
   */
  public void update(Update update) {
    update(update.getUpdatedObjects(), update.getTime(), RescueConstants.SOURCE_UPDATE);
  }

  public void update(RescueObject[] changed, int time, Object source) {
    for (int i = 0; i < changed.length; ++i) {
      RescueObject current = lookup(changed[i].getID());
      if (current == null) {
        RescueObject copy = changed[i].copy();
        add(copy, time, source);
      } else {
        current.merge(changed[i]);
      }
    }
  }

  /**
   * Notify listeners that an object has been added
   *
   * @param o      The added object
   * @param source The source of the change
   */
  private void fireObjectAdded(RescueObject o, int timestep, Object source) {
    ObjectAddedEvent event = new ObjectAddedEvent(o, timestep, source);
    synchronized (LOCK) {
      if (listeners == null)
        listeners = new HashSet<MemoryListener>();
      for (Iterator it = listeners.iterator(); it.hasNext();) {
        ((MemoryListener) it.next()).objectAdded(event);
      }
    }
  }

  /**
   * Notify listeners that an object has been changed
   *
   * @param o      The changed object
   * @param source The source of the change
   */
  private void fireObjectChanged(RescueObject o, int property, int timestep, Object source) {
    ObjectChangedEvent event = new ObjectChangedEvent(o, property, timestep, source);
    synchronized (LOCK) {
      if (listeners == null)
        listeners = new HashSet<MemoryListener>();
      for (Iterator it = listeners.iterator(); it.hasNext();) {
        ((MemoryListener) it.next()).objectChanged(event);
      }
    }
  }

  /**
   * Get the Euclidean distance between two objects
   *
   * @param o1 The first object
   * @param o2 The second object
   * @return The distance between the two objects
   */
  public double getDistance(RescueObject o1, RescueObject o2) throws CannotFindLocationException {
    int x1, x2, y1, y2;
    int[] xy = getXY(o1);
    x1 = xy[0];
    y1 = xy[1];
    xy = getXY(o2);
    x2 = xy[0];
    y2 = xy[1];
    double x = x2 - x1;
    x = x * x;
    double y = y2 - y1;
    y = y * y;
    return Math.sqrt(x + y);
  }

  /**
   * Get the angle in arc-seconds from one object to another
   *
   * @param from The first object
   * @param to   The second object
   * @return The angle from 'from' to 'to', in arc-seconds
   */
  public int getAngle(RescueObject from, RescueObject to) throws CannotFindLocationException {
    int x1, x2, y1, y2;
    int[] xy = getXY(from);
    x1 = xy[0];
    y1 = xy[1];
    xy = getXY(to);
    x2 = xy[0];
    y2 = xy[1];
    int dx = x2 - x1;
    int dy = y2 - y1;
    double theta = Math.atan2(-dx, dy);
    if (theta < 0)
      theta += 2 * Math.PI;
    return (int) (theta * 360 * 60 * 60 / (2.0 * Math.PI));
  }

  /**
   * Get the X and Y coordinates of an object
   *
   * @param o The object of interest
   * @return An array of two ints. The first element is the X coordinate, the
   *         second is Y. The return value may be null if the location of the
   *         object is unknown
   */
  public int[] getXY(RescueObject o) throws CannotFindLocationException {
    int x = 0, y = 0;
    switch (o.getType()) {
      case RescueConstants.TYPE_CIVILIAN:
      case RescueConstants.TYPE_FIRE_BRIGADE:
      case RescueConstants.TYPE_AMBULANCE_TEAM:
      case RescueConstants.TYPE_POLICE_FORCE:
      case RescueConstants.TYPE_CAR:
        RescueObject location = lookup(((Humanoid) o).getPosition());
        if (location == null) {
          throw new CannotFindLocationException();
        }
        switch (location.getType()) {
          case RescueConstants.TYPE_BUILDING:
          case RescueConstants.TYPE_POLICE_OFFICE:
          case RescueConstants.TYPE_FIRE_STATION:
          case RescueConstants.TYPE_AMBULANCE_CENTER:
          case RescueConstants.TYPE_REFUGE:
            x = ((Building) location).getX();
            y = ((Building) location).getY();
            break;
          case RescueConstants.TYPE_NODE:
            x = ((Node) location).getX();
            y = ((Node) location).getY();
            break;
          case RescueConstants.TYPE_RIVER_NODE:
            x = ((RiverNode) location).getX();
            y = ((RiverNode) location).getY();
            break;
          case RescueConstants.TYPE_ROAD:
            Node roadHead = (Node) lookup(((Road) location).getHead());
            Node roadTail = (Node) lookup(((Road) location).getTail());
            int roadLength = ((Road) location).getLength();
            double d = ((double) ((Humanoid) o).getPositionExtra()) / ((double) roadLength);
            int xDelta = roadTail.getX() - roadHead.getX();
            int yDelta = roadTail.getY() - roadHead.getY();
            x = (int) (roadHead.getX() + d * xDelta);
            y = (int) (roadHead.getY() + d * yDelta);
            break;
          case RescueConstants.TYPE_AMBULANCE_TEAM:
            return getXY(location);
          default:
            System.err
                .println("Can't find location for a humanoid that lives in " + Handy.getTypeName(location.getType()));
            throw new CannotFindLocationException(
                o + " is located at " + location + " but I don't know how to handle that");
        }
        break;
      case RescueConstants.TYPE_NODE:
        x = ((Node) o).getX();
        y = ((Node) o).getY();
        break;
      case RescueConstants.TYPE_RIVER_NODE:
        x = ((RiverNode) o).getX();
        y = ((RiverNode) o).getY();
        break;
      case RescueConstants.TYPE_ROAD:
        // Halfway between head and tail?
        Node head = (Node) lookup(((Road) o).getHead());
        Node tail = (Node) lookup(((Road) o).getTail());
        if (head == null) {
          System.err.println("Can't find head of this road: " + o.toLongString());
        }
        if (tail == null) {
          System.err.println("Can't find tail of this road: " + o.toLongString());
        }
        x = (head.getX() + tail.getX()) / 2;
        y = (head.getY() + tail.getY()) / 2;
        break;
      case RescueConstants.TYPE_RIVER:
        RiverNode head_ = (RiverNode) lookup(((River) o).getHead());
        RiverNode tail_ = (RiverNode) lookup(((River) o).getTail());
        x = (head_.getX() + tail_.getX()) / 2;
        y = (head_.getY() + tail_.getY()) / 2;
        break;
      case RescueConstants.TYPE_BUILDING:
      case RescueConstants.TYPE_REFUGE:
      case RescueConstants.TYPE_FIRE_STATION:
      case RescueConstants.TYPE_AMBULANCE_CENTER:
      case RescueConstants.TYPE_POLICE_OFFICE:
        x = ((Building) o).getX();
        y = ((Building) o).getY();
        break;
      case RescueConstants.TYPE_WORLD:
        throw new CannotFindLocationException("Tried to find the position of a TYPE_WORLD object");
    }
    return new int[] { x, y };
  }

  /**
   * Get the RescueObject the represents the position of a MovingObject. This
   * might be another MovingObject, for example the position of a civilian in an
   * ambulance will be the ambulance
   *
   * @param object The object in question
   * @return The position of the object, or null if the position cannot be found
   */
  public RescueObject getPosition(MovingObject o) {
    RescueObject result = lookup(o.getPosition());
    return result;
  }

  /**
   * Get the MotionlessObject that represents a given object's position in the
   * world
   *
   * @param o The object in question
   * @return The MotionlessObject that represents a given object's position in the
   *         world, or null if the position cannot be found
   */
  public MotionlessObject getMotionlessPosition(RescueObject o) {
    switch (o.getType()) {
      case RescueConstants.TYPE_CIVILIAN:
      case RescueConstants.TYPE_FIRE_BRIGADE:
      case RescueConstants.TYPE_POLICE_FORCE:
      case RescueConstants.TYPE_AMBULANCE_TEAM:
      case RescueConstants.TYPE_CAR:
        RescueObject position = lookup(((Humanoid) o).getPosition());
        if (position == null)
          return null;
        return getMotionlessPosition(position);
      case RescueConstants.TYPE_BUILDING:
      case RescueConstants.TYPE_REFUGE:
      case RescueConstants.TYPE_FIRE_STATION:
      case RescueConstants.TYPE_POLICE_OFFICE:
      case RescueConstants.TYPE_AMBULANCE_CENTER:
      case RescueConstants.TYPE_ROAD:
      case RescueConstants.TYPE_NODE:
      case RescueConstants.TYPE_RIVER_NODE:
      case RescueConstants.TYPE_RIVER:
        return (MotionlessObject) o;
      default:
        return null;
    }
  }

  /**
   * Find the closest Node to a RescueObject. If the given object is a Node then
   * it is returned. If it is a road, then the head node is returned. If it is a
   * building, then the entrance node closest to the center of the building is
   * returned. If it is a mobile agent (civilian, fire brigade etc) then the
   * closest node to it's location is returned
   *
   * @param o The object we want the closest node to
   * @return The Node object that is closest to the given RescueObject
   */
  public Node getClosestNode(RescueObject o) {
    switch (o.getType()) {
      case RescueConstants.TYPE_NODE:
        return (Node) o;
      case RescueConstants.TYPE_ROAD:
        return (Node) lookup(((Road) o).getHead());
      case RescueConstants.TYPE_BUILDING:
      case RescueConstants.TYPE_REFUGE:
      case RescueConstants.TYPE_FIRE_STATION:
      case RescueConstants.TYPE_AMBULANCE_CENTER:
      case RescueConstants.TYPE_POLICE_OFFICE:
        int[] entrances = ((Building) o).getEntrances();
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < entrances.length; ++i) {
          try {
            double d = getDistance(o, lookup(entrances[i]));
            if (d < bestDistance) {
              best = i;
              bestDistance = d;
            }
          } catch (CannotFindLocationException e) {
          }
        }
        if (best == -1)
          return null;
        return (Node) lookup(entrances[best]);
      case RescueConstants.TYPE_CIVILIAN:
      case RescueConstants.TYPE_FIRE_BRIGADE:
      case RescueConstants.TYPE_AMBULANCE_TEAM:
      case RescueConstants.TYPE_POLICE_FORCE:
      case RescueConstants.TYPE_CAR:
        RescueObject location = lookup(((Humanoid) o).getPosition());
        if (location == null)
          return null;
        switch (location.getType()) {
          case RescueConstants.TYPE_NODE:
            return (Node) location;
          case RescueConstants.TYPE_ROAD:
            int length = ((Road) location).getLength();
            int pos = ((Humanoid) o).getPositionExtra();
            if (pos < length / 2)
              return (Node) lookup(((Road) location).getHead());
            else
              return (Node) lookup(((Road) location).getTail());
          default:
            return getClosestNode(location);
        }
    }
    return null;
  }

  /**
   * Get the node id (either head or tail of the given road) that is closest to
   * the position given
   *
   * @param road          The road that we want the head or tail of
   * @param positionExtra The agent's position along the road
   * @return Either the head or tail node of the given road, whichever is closest
   */
  public Node getClosestNode(Road road, int positionExtra) {
    if (positionExtra < (road.getLength() / 2))
      return (Node) lookup(road.getHead());
    return (Node) lookup(road.getTail());
  }

  /**
   * Find the neighbours of a RescueObject. This method delegates to @{link
   * #findNodeNeighbours(Node)}, @{link #findRoadNeighbours(Road)} or @{link
   * #findBuildingNeighbours(Building)} for Nodes, Roads and Buildings
   * respectively
   *
   * @param o The RescueObject we want the neighbours for
   * @return An array of all neighbours of the given object
   */
  public RescueObject[] findNeighbours(RescueObject o) {
    switch (o.getType()) {
      case RescueConstants.TYPE_NODE:
        return findNodeNeighbours((Node) o);
      case RescueConstants.TYPE_ROAD:
        return findRoadNeighbours((Road) o);
      case RescueConstants.TYPE_BUILDING:
      case RescueConstants.TYPE_REFUGE:
      case RescueConstants.TYPE_FIRE_STATION:
      case RescueConstants.TYPE_POLICE_OFFICE:
      case RescueConstants.TYPE_AMBULANCE_CENTER:
        return findBuildingNeighbours((Building) o);
      default:
        System.out.println("Cannot find neighbours of a " + Handy.getTypeName(o.getType()));
        return null;
    }
  }

  /**
   * Find the neighbours of a Node. This method returns all RescueObjects
   * identified by this Nodes 'edges' property
   *
   * @param node The Node we want the neighbours for
   * @return An array of all neighbours of the given Node
   */
  public RescueObject[] findNodeNeighbours(Node node) {
    int[] edges = node.getEdges();
    RescueObject[] all = new RescueObject[edges.length];
    int count = 0;
    for (int i = 0; i < edges.length; ++i) {
      all[i] = lookup(edges[i]);
    }
    return all;
  }

  /**
   * Find the neighbours of a Road. This method returns the head and tail nodes
   * connected to this Road
   *
   * @param road The Road we want the neighbours for
   * @return An array containing the head and tail nodes, in that order
   */
  public RescueObject[] findRoadNeighbours(Road road) {
    RescueObject head = lookup(road.getHead());
    RescueObject tail = lookup(road.getTail());
    return new RescueObject[] { head, tail };
  }

  public Node getHead(Road r) {
    return (Node) lookup(r.getHead());
  }

  public Node getTail(Road r) {
    return (Node) lookup(r.getTail());
  }

  public Road getRoadBetween(Node n1, Node n2) {
    RescueObject[] neighbours = findNodeNeighbours(n1);
    for (int i = 0; i < neighbours.length; ++i) {
      if (neighbours[i] instanceof Road) {
        Road r = (Road) neighbours[i];
        if ((r.getHead() == n1.getID() && r.getTail() == n2.getID())
            || (r.getHead() == n2.getID() && r.getTail() == n1.getID()))
          return r;
      }
    }
    return null;
  }

  public boolean neighbours(RescueObject o1, RescueObject o2) {
    int id = o2.getID();
    if (o1 instanceof Building) {
      int[] entrances = ((Building) o1).getEntrances();
      for (int i = 0; i < entrances.length; ++i)
        if (entrances[i] == id)
          return true;
    }
    if (o1 instanceof Edge) {
      Edge e = (Edge) o1;
      if (e.getHead() == id || e.getTail() == id)
        return true;
    }
    if (o1 instanceof Vertex) {
      return isEdge(id, (Vertex) o1);
    }
    return false;
  }

  public boolean isEdge(int id, Vertex v) {
    int[] edges = v.getEdges();
    for (int i = 0; i < edges.length; ++i)
      if (edges[i] == id)
        return true;
    return false;
  }

  public boolean isEdge(RescueObject o, Node node) {
    return isEdge(o.getID(), node);
  }

  public boolean isEntrance(int id, Building b) {
    int[] entrances = b.getEntrances();
    for (int i = 0; i < entrances.length; ++i)
      if (entrances[i] == id)
        return true;
    return false;
  }

  public boolean isEntrance(RescueObject o, Building b) {
    return isEntrance(o.getID(), b);
  }

  /**
   * Find the neighbours of a Building. This method returns all Nodes identified
   * by this Buildings 'entrances' property
   *
   * @param b The Building we want the neighbours for
   * @return An array of all Nodes identified by the 'entrances' property
   */
  public RescueObject[] findBuildingNeighbours(Building b) {
    int[] entrances = b.getEntrances();
    RescueObject[] all = new RescueObject[entrances.length];
    for (int i = 0; i < entrances.length; ++i) {
      all[i] = lookup(entrances[i]);
    }
    return all;
  }
}