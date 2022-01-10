/*
 * Last change: $Date: 2005/03/16 02:30:30 $
 * $Revision: 1.20 $
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import rescuecore.event.PropertyChangedEvent;
import rescuecore.event.PropertyListener;
import rescuecore.objects.AmbulanceCenter;
import rescuecore.objects.AmbulanceTeam;
import rescuecore.objects.Building;
import rescuecore.objects.Car;
import rescuecore.objects.Civilian;
import rescuecore.objects.FireBrigade;
import rescuecore.objects.FireStation;
import rescuecore.objects.Node;
import rescuecore.objects.PoliceForce;
import rescuecore.objects.PoliceOffice;
import rescuecore.objects.Refuge;
import rescuecore.objects.River;
import rescuecore.objects.RiverNode;
import rescuecore.objects.Road;
import rescuecore.objects.World;

/**
 * This is the base class for all objects in the simulation environment
 */
public abstract class RescueObject implements java.io.Serializable {
  private transient Collection listeners;
  /** The kernel-assigned id */
  protected int id;

  private transient int[] knownProperties;

  private transient HashMap annotations;

  /**
   * Construct a new RescueObject
   **/
  protected RescueObject() {
    listeners = new ArrayList();
    id = 0;
    knownProperties = null;
    annotations = null;
  }

  /**
   * Adds an annotation to this object. This will overwrite any existing
   * annotations with the same key.
   */
  public void addAnnotation(String key, Object annotation) {
    if (annotations == null)
      annotations = new HashMap();
    annotations.put(key, annotation);
  }

  /**
   * Gets an annotation for the given String identifier. If no such annotation
   * exists this will return null.
   *
   * @return The annotation.
   */
  public Object getAnnotation(String key) {
    if (annotations == null) {
      // System.err.println("Could not find annotation named \""+key+"\"");
      return null;
    }
    return annotations.get(key);
  }

  /**
   * Get this objects type
   *
   * @return The type of this object
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
  public abstract int getType();

  /**
   * Get this objects id
   */
  public int getID() {
    return id;
  }

  public void setID(int id) {
    this.id = id;
  }

  public int hashCode() {
    return id;
  }

  public boolean equals(Object o) {
    if (o instanceof RescueObject)
      return this.id == ((RescueObject) o).id;
    return false;
  }

  /**
   * Add a PropertyListener
   *
   * @param l The listener that will be notified of changes to any properties in
   *          this object
   */
  public void addPropertyListener(PropertyListener l) {
    if (listeners == null)
      listeners = new ArrayList();
    synchronized (listeners) {
      listeners.add(l);
    }
  }

  /**
   * Remove a PropertyListener
   *
   * @param l The listener that will no longer be notified of changes to any
   *          properties in this object
   */
  public void removePropertyListener(PropertyListener l) {
    if (listeners == null)
      listeners = new ArrayList();
    synchronized (listeners) {
      listeners.remove(l);
    }
  }

  /**
   * Get a String representation of all this object's properties
   *
   * @return A String representation of all this object's properties
   */
  protected String getPropertiesString() {
    StringBuffer result = new StringBuffer();
    int[] known = getKnownPropertyTypes();
    for (int i = 0; i < known.length; ++i) {
      if (i != 0)
        result.append(", ");
      result.append(getPropertyAsString(known[i]));
    }
    return result.toString();
  }

  /**
   * Get all property types known by this object
   *
   * @return An array containing all the property types known by this object
   * @see RescueConstants#PROPERTY_NULL
   * @see RescueConstants#PROPERTY_START_TIME
   * @see RescueConstants#PROPERTY_LONGITUDE
   * @see RescueConstants#PROPERTY_LATITUDE
   * @see RescueConstants#PROPERTY_WIND_FORCE
   * @see RescueConstants#PROPERTY_WIND_DIRECTION
   * @see RescueConstants#PROPERTY_X
   * @see RescueConstants#PROPERTY_Y
   * @see RescueConstants#PROPERTY_DIRECTION
   * @see RescueConstants#PROPERTY_POSITION
   * @see RescueConstants#PROPERTY_POSITION_HISTORY
   * @see RescueConstants#PROPERTY_POSITION_EXTRA
   * @see RescueConstants#PROPERTY_STAMINA
   * @see RescueConstants#PROPERTY_HP
   * @see RescueConstants#PROPERTY_DAMAGE
   * @see RescueConstants#PROPERTY_BURIEDNESS
   * @see RescueConstants#PROPERTY_FLOORS
   * @see RescueConstants#PROPERTY_BUILDING_ATTRIBUTES
   * @see RescueConstants#PROPERTY_IGNITION
   * @see RescueConstants#PROPERTY_BROKENNESS
   * @see RescueConstants#PROPERTY_FIERYNESS
   * @see RescueConstants#PROPERTY_ENTRANCES
   * @see RescueConstants#PROPERTY_BUILDING_SHAPE_ID
   * @see RescueConstants#PROPERTY_BUILDING_CODE
   * @see RescueConstants#PROPERTY_BUILDING_AREA_GROUND
   * @see RescueConstants#PROPERTY_BUILDING_AREA_TOTAL
   * @see RescueConstants#PROPERTY_BUILDING_APEXES
   * @see RescueConstants#PROPERTY_WATER_QUANTITY
   * @see RescueConstants#PROPERTY_STRETCHED_LENGTH
   * @see RescueConstants#PROPERTY_HEAD
   * @see RescueConstants#PROPERTY_TAIL
   * @see RescueConstants#PROPERTY_LENGTH
   * @see RescueConstants#PROPERTY_ROAD_KIND
   * @see RescueConstants#PROPERTY_CARS_PASS_TO_HEAD
   * @see RescueConstants#PROPERTY_CARS_PASS_TO_TAIL
   * @see RescueConstants#PROPERTY_HUMANS_PASS_TO_HEAD
   * @see RescueConstants#PROPERTY_HUMANS_PASS_TO_TAIL
   * @see RescueConstants#PROPERTY_WIDTH
   * @see RescueConstants#PROPERTY_BLOCK
   * @see RescueConstants#PROPERTY_REPAIR_COST
   * @see RescueConstants#PROPERTY_MEDIAN_STRIP
   * @see RescueConstants#PROPERTY_LINES_TO_HEAD
   * @see RescueConstants#PROPERTY_LINES_TO_TAIL
   * @see RescueConstants#PROPERTY_WIDTH_FOR_WALKERS
   * @see RescueConstants#PROPERTY_EDGES
   * @see RescueConstants#PROPERTY_SIGNAL
   * @see RescueConstants#PROPERTY_SIGNAL_TIMING
   * @see RescueConstants#PROPERTY_SHORTCUT_TO_TURN
   * @see RescueConstants#PROPERTY_POCKET_TO_TURN_ACROSS
   */
  public int[] getKnownPropertyTypes() {
    if (knownProperties == null) {
      Collection c = new ArrayList();
      for (int i = RescueConstants.PROPERTY_MIN; i <= RescueConstants.PROPERTY_MAX; ++i) {
        if (propertyExists(i))
          c.add(Integer.valueOf(i));
      }
      Object[] all = c.toArray();
      knownProperties = new int[all.length];
      for (int i = 0; i < all.length; ++i)
        knownProperties[i] = ((Integer) all[i]).intValue();
    }
    return knownProperties;
  }

  /**
   * Get whether this object has a particular property or not
   *
   * @param property The property in question
   * @return True iff the given property exists for this object
   * @see RescueConstants#PROPERTY_NULL
   * @see RescueConstants#PROPERTY_START_TIME
   * @see RescueConstants#PROPERTY_LONGITUDE
   * @see RescueConstants#PROPERTY_LATITUDE
   * @see RescueConstants#PROPERTY_WIND_FORCE
   * @see RescueConstants#PROPERTY_WIND_DIRECTION
   * @see RescueConstants#PROPERTY_X
   * @see RescueConstants#PROPERTY_Y
   * @see RescueConstants#PROPERTY_DIRECTION
   * @see RescueConstants#PROPERTY_POSITION
   * @see RescueConstants#PROPERTY_POSITION_HISTORY
   * @see RescueConstants#PROPERTY_POSITION_EXTRA
   * @see RescueConstants#PROPERTY_STAMINA
   * @see RescueConstants#PROPERTY_HP
   * @see RescueConstants#PROPERTY_DAMAGE
   * @see RescueConstants#PROPERTY_BURIEDNESS
   * @see RescueConstants#PROPERTY_FLOORS
   * @see RescueConstants#PROPERTY_BUILDING_ATTRIBUTES
   * @see RescueConstants#PROPERTY_IGNITION
   * @see RescueConstants#PROPERTY_BROKENNESS
   * @see RescueConstants#PROPERTY_FIERYNESS
   * @see RescueConstants#PROPERTY_ENTRANCES
   * @see RescueConstants#PROPERTY_BUILDING_SHAPE_ID
   * @see RescueConstants#PROPERTY_BUILDING_CODE
   * @see RescueConstants#PROPERTY_BUILDING_AREA_GROUND
   * @see RescueConstants#PROPERTY_BUILDING_AREA_TOTAL
   * @see RescueConstants#PROPERTY_BUILDING_APEXES
   * @see RescueConstants#PROPERTY_WATER_QUANTITY
   * @see RescueConstants#PROPERTY_STRETCHED_LENGTH
   * @see RescueConstants#PROPERTY_HEAD
   * @see RescueConstants#PROPERTY_TAIL
   * @see RescueConstants#PROPERTY_LENGTH
   * @see RescueConstants#PROPERTY_ROAD_KIND
   * @see RescueConstants#PROPERTY_CARS_PASS_TO_HEAD
   * @see RescueConstants#PROPERTY_CARS_PASS_TO_TAIL
   * @see RescueConstants#PROPERTY_HUMANS_PASS_TO_HEAD
   * @see RescueConstants#PROPERTY_HUMANS_PASS_TO_TAIL
   * @see RescueConstants#PROPERTY_WIDTH
   * @see RescueConstants#PROPERTY_BLOCK
   * @see RescueConstants#PROPERTY_REPAIR_COST
   * @see RescueConstants#PROPERTY_MEDIAN_STRIP
   * @see RescueConstants#PROPERTY_LINES_TO_HEAD
   * @see RescueConstants#PROPERTY_LINES_TO_TAIL
   * @see RescueConstants#PROPERTY_WIDTH_FOR_WALKERS
   * @see RescueConstants#PROPERTY_EDGES
   * @see RescueConstants#PROPERTY_SIGNAL
   * @see RescueConstants#PROPERTY_SIGNAL_TIMING
   * @see RescueConstants#PROPERTY_SHORTCUT_TO_TURN
   * @see RescueConstants#PROPERTY_POCKET_TO_TURN_ACROSS
   */
  public final boolean propertyExists(int property) {
    return getProperty(property) != null;
    // return false;
  }

  public Property getProperty(int property) /* throws UnknownPropertyException */ {
    // throw new UnknownPropertyException(property);
    return null;
  }

  /*
   * Get a particular property as a String
   *
   * @param property The property we want
   *
   * @return A String representation of the property
   *
   * @throws UnknownPropertyException if the property is unknown
   */
  public String getPropertyAsString(int property) /* throws UnknownPropertyException */ {
    Property p = getProperty(property);
    if (p == null)
      return "<unknown>";
    return p.getStringValue();
  }

  /**
   * Get the last time a property was updated
   *
   * @param property The property we want
   * @return The last time the property was updated
   * @throws UnknownPropertyException if the property is unknown
   */
  public int getLastPropertyUpdate(int property) /* throws UnknownPropertyException */ {
    Property p = getProperty(property);
    if (p == null)
      return RescueConstants.VALUE_UNKNOWN;
    return p.getLastUpdate();
  }

  /**
   * Get the source for last update to a property
   *
   * @param property The property we want
   * @return The source of the last update to the property
   * @throws UnknownPropertyException if the property is unknown
   */
  public Object getLastPropertyUpdateSource(int property) /* throws UnknownPropertyException */ {
    Property p = getProperty(property);
    if (p == null)
      return null;
    return p.getLastUpdateSource();
  }

  /**
   * Is the value of a particular property known?
   *
   * @param property The property we want
   * @return True if and only if the value of the given property is known
   * @throws UnknownPropertyException if the property does not exist
   */
  public boolean isPropertyValueKnown(int property) /* throws UnknownPropertyException */ {
    Property p = getProperty(property);
    if (p == null)
      return false;
    return p.isValueKnown();
  }

  /**
   * Is the value of a particular property assumed?
   *
   * @param property The property we want
   * @return True if and only if the value of the given property is assumed
   * @throws UnknownPropertyException if the property does not exist
   */
  public boolean isPropertyValueAssumed(int property) /* throws UnknownPropertyException */ {
    Property p = getProperty(property);
    if (p == null)
      return false;
    return p.isValueAssumed();
  }

  /**
   * Update the value of a property. All PropertyListeners will be notified if the
   * value is actually updated.
   *
   * @param property  The property to update
   * @param timestamp The current time step. If this update is more recent than
   *                  the current value of the property then the update will
   *                  proceed
   * @param newValue  The new value of the property
   * @param source    The source of the change
   * @return true if and only if the update was successful. An update will not
   *         occur if the new value is less recent than our current information,
   *         or if the current value is the same as the new value
   * @throws UnknownPropertyException if the property is not recognised
   */
  // public boolean updateProperty(int property, int timestamp, int newValue,
  // Object source) throws UnknownPropertyException {
  // return updateProperty(getProperty(property),timestamp,newValue,source);
  // }

  /**
   * Update the value of a property. All PropertyListeners will be notified if the
   * value is actually updated.
   *
   * @param property  The property to update
   * @param timestamp The current time step. If this update is more recent than
   *                  the current value of the property then the update will
   *                  proceed
   * @param newValue  The new value of the property
   * @param source    The source of the change
   * @return true if and only if the update was successful. An update will not
   *         occur if the new value is less recent than our current information,
   *         or if the current value is the same as the new value
   * @throws UnknownPropertyException if the property is not recognised
   */
  // public boolean updateProperty(int property, int timestamp, int[] newValue,
  // Object source) throws UnknownPropertyException {
  // return updateProperty(getProperty(property),timestamp,newValue,source);
  // }

  /**
   * Update the value of a property. All PropertyListeners will be notified if the
   * value is actually updated.
   *
   * @param property  The property to update
   * @param timestamp The current time step. If this update is more recent than
   *                  the current value of the property then the update will
   *                  proceed
   * @param newValue  The new value of the property
   * @param source    The source of the change
   * @return true if and only if the update was successful. An update will not
   *         occur if the new value is less recent than our current information,
   *         or if the current value is the same as the new value
   * @throws UnknownPropertyException if the property is not recognised
   */
  // public boolean updateProperty(int property, int timestamp, boolean newValue,
  // Object source) throws UnknownPropertyException {
  // return updateProperty(getProperty(property),timestamp,newValue,source);
  // }

  protected boolean setProperty(IntProperty p, int newValue, int timestamp,
      Object source) /* throws UnknownPropertyException */ {
    if (p.setValue(newValue, timestamp, source)) {
      firePropertyChanged(p.getType(), timestamp, source);
      return true;
    }
    return false;
  }

  protected boolean setProperty(ArrayProperty p, int[] newValue, int timestamp,
      Object source) /* throws UnknownPropertyException */ {
    if (p.setValues(newValue, timestamp, source)) {
      firePropertyChanged(p.getType(), timestamp, source);
      return true;
    }
    return false;
  }

  protected boolean updateProperty(IntProperty p, int newValue, int timestamp,
      Object source) /* throws UnknownPropertyException */ {
    if (p.updateValue(newValue, timestamp, source)) {
      firePropertyChanged(p.getType(), timestamp, source);
      return true;
    }
    return false;
  }

  protected boolean updateProperty(ArrayProperty p, int[] newValue, int timestamp,
      Object source) /* throws UnknownPropertyException */ {
    if (p.updateValues(newValue, timestamp, source)) {
      firePropertyChanged(p.getType(), timestamp, source);
      return true;
    }
    return false;
  }

  /*
   * protected boolean updateProperty(Property p, int timestamp, boolean newValue,
   * Object source) throws UnknownPropertyException { if
   * (p.updateValue(newValue,timestamp,source)){
   * firePropertyChanged(p.getPropertyType(),timestamp,source); return true; }
   * return false; }
   */

  public String toString() {
    return Handy.getTypeName(getType()) + " " + id;
  }

  public String toLongString() {
    return Handy.getTypeName(getType()) + " " + id + " [" + getPropertiesString() + "]";
  }

  /**
   * Read from an InputBuffer
   *
   * @param in        An InputBuffer to read data from
   * @param timestamp The timestamp of the update
   * @param source    The source of the change
   */
  public final void read(InputBuffer in, int timestamp, Object source) {
    int prop;
    do {
      prop = in.readInt();
      if (prop != RescueConstants.PROPERTY_NULL) {
        int size = in.readInt();
        Property p = getProperty(prop);
        if (p != null) {
          if (p.read(in, timestamp, source))
            firePropertyChanged(prop, timestamp, source);
        } else {
          System.err.println("Got an unknown property (" + prop + ") from the stream");
          in.skip(size);
        }
      }
    } while (prop != RescueConstants.PROPERTY_NULL);
  }

  /**
   * Update this RescueObject from a different one
   *
   * @param o The object to update from
   */
  public final void merge(RescueObject o) {
    int[] known = getKnownPropertyTypes();
    for (int i = 0; i < known.length; ++i) {
      Property oldP = getProperty(known[i]);
      Property newP = o.getProperty(known[i]);
      if (oldP != null && newP != null) {
        if (oldP.merge(newP))
          firePropertyChanged(oldP.getType(), oldP.getLastUpdate(), oldP.getLastUpdateSource());
      }
    }
  }

  /**
   * Write this RescueObject to an OutputBuffer
   *
   * @param out The OutputBuffer to write to
   */
  public void write(OutputBuffer out) {
    int[] props = getKnownPropertyTypes();
    for (int i = 0; i < props.length; ++i) {
      Property p = getProperty(props[i]);
      if (p != null && p.isValueKnown()) {
        out.writeInt(p.getType());
        int base = out.markBlock();
        p.write(out);
        out.writeBlockSize(base);
      }
    }
    out.writeInt(RescueConstants.PROPERTY_NULL);
  }

  /**
   * Is this object a building?
   *
   * @return true if and only if this object is a building
   */
  public boolean isBuilding() {
    // return (getInternalType() & INTERNAL_TYPE_ANY_BUILDING) != 0;
    switch (getType()) {
      case RescueConstants.TYPE_BUILDING:
      case RescueConstants.TYPE_REFUGE:
      case RescueConstants.TYPE_FIRE_STATION:
      case RescueConstants.TYPE_POLICE_OFFICE:
      case RescueConstants.TYPE_AMBULANCE_CENTER:
        return true;
      default:
        return false;
    }
  }

  public boolean isOrdinaryBuilding() {
    return getType() == RescueConstants.TYPE_BUILDING;
  }

  /**
   * Is this object a refuge?
   *
   * @return true if and only if this object is a refuge
   */
  public boolean isRefuge() {
    return getType() == RescueConstants.TYPE_REFUGE;
  }

  /**
   * Is this object a fire station?
   *
   * @return true if and only if this object is a fire station
   */
  public boolean isFireStation() {
    return getType() == RescueConstants.TYPE_FIRE_STATION;
  }

  /**
   * Is this object a police office?
   *
   * @return true if and only if this object is a police office
   */
  public boolean isPoliceOffice() {
    return getType() == RescueConstants.TYPE_POLICE_OFFICE;
  }

  /**
   * Is this object an ambulance center?
   *
   * @return true if and only if this object is an ambulance center
   */
  public boolean isAmbulanceCenter() {
    return getType() == RescueConstants.TYPE_AMBULANCE_CENTER;
  }

  /**
   * Is this object a road?
   *
   * @return true if and only if this object is a road
   */
  public boolean isRoad() {
    return getType() == RescueConstants.TYPE_ROAD;
  }

  /**
   * Is this object a node?
   *
   * @return true if and only if this object is a node
   */
  public boolean isNode() {
    return getType() == RescueConstants.TYPE_NODE;
  }

  /**
   * Is this object a humanoid?
   *
   * @return true if and only if this object is a humanoid
   */
  public boolean isHumanoid() {
    // return (getInternalType() & INTERNAL_TYPE_ANY_HUMANOID) != 0;
    switch (getType()) {
      case RescueConstants.TYPE_CIVILIAN:
      case RescueConstants.TYPE_AMBULANCE_TEAM:
      case RescueConstants.TYPE_FIRE_BRIGADE:
      case RescueConstants.TYPE_POLICE_FORCE:
        return true;
      default:
        return false;
    }
  }

  /**
   * Is this object a civilian?
   *
   * @return true if and only if this object is a civilian
   */
  public boolean isCivilian() {
    return getType() == RescueConstants.TYPE_CIVILIAN;
  }

  /**
   * Is this object an ambulance team?
   *
   * @return true if and only if this object is an ambulance
   */
  public boolean isAmbulanceTeam() {
    return getType() == RescueConstants.TYPE_AMBULANCE_TEAM;
  }

  /**
   * Is this object a police force?
   *
   * @return true if and only if this object is a police force
   */
  public boolean isPoliceForce() {
    return getType() == RescueConstants.TYPE_POLICE_FORCE;
  }

  /**
   * Is this object a fire brigade?
   *
   * @return true if and only if this object is a fire brigade
   */
  public boolean isFireBrigade() {
    return getType() == RescueConstants.TYPE_FIRE_BRIGADE;
  }

  protected void firePropertyChanged(int property, int timestep, Object source) {
    if (listeners == null)
      return;
    PropertyChangedEvent event = new PropertyChangedEvent(this, property, timestep, source);
    synchronized (listeners) {
      for (Iterator it = listeners.iterator(); it.hasNext();) {
        ((PropertyListener) it.next()).propertyChanged(event);
      }
    }
  }

  /**
   * Make a deep copy of this RescueObject
   *
   * @return A deep copy of this object
   */
  public final RescueObject copy() {
    RescueObject result = newObject(getType());
    result.merge(this);
    result.setID(id);
    return result;
  }

  /**
   * Construct a new RescueObject
   *
   * @param type The type of the new object
   * @return A newly constructed RescueObject
   */
  public static RescueObject newObject(int type) {
    switch (type) {
      case RescueConstants.TYPE_WORLD:
        return new World();
      case RescueConstants.TYPE_CIVILIAN:
        return new Civilian();
      case RescueConstants.TYPE_CAR:
        return new Car();
      case RescueConstants.TYPE_FIRE_BRIGADE:
        return new FireBrigade();
      case RescueConstants.TYPE_AMBULANCE_TEAM:
        return new AmbulanceTeam();
      case RescueConstants.TYPE_POLICE_FORCE:
        return new PoliceForce();
      case RescueConstants.TYPE_ROAD:
        return new Road();
      case RescueConstants.TYPE_RIVER:
        return new River();
      case RescueConstants.TYPE_NODE:
        return new Node();
      case RescueConstants.TYPE_RIVER_NODE:
        return new RiverNode();
      case RescueConstants.TYPE_BUILDING:
        return new Building();
      case RescueConstants.TYPE_REFUGE:
        return new Refuge();
      case RescueConstants.TYPE_FIRE_STATION:
        return new FireStation();
      case RescueConstants.TYPE_AMBULANCE_CENTER:
        return new AmbulanceCenter();
      case RescueConstants.TYPE_POLICE_OFFICE:
        return new PoliceOffice();
      default:
        System.out.println("WARNING: Unknown object type: " + type);
        return null;
    }
  }
}