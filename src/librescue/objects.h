/*
 * Copyright (c) 2005, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 Contributors and list of changes:

 Cameron Skinner
   Converted original Robocup Rescue software into librescue
*/

#ifndef RESCUE_OBJECTS_H
#define RESCUE_OBJECTS_H

#include "common.h"
#include "input.h"
#include "output.h"

namespace Librescue {
  RescueObject* newRescueObject(TypeId type);

  class Property {
  private:
	// No copying
	Property(const Property& rhs);
	Property& operator=(const Property& rhs);

  protected:
	PropertyId m_type;

	// The last update time
	INT_32 m_lastUpdate;

	Property(PropertyId type);

	// Return true iff the value of this property was changed
	virtual bool mergeData(const Property* other) = 0;

  public:
	virtual ~Property();

	PropertyId type() const;

	INT_32 lastUpdate() const;
	void setLastUpdate(INT_32 time);

	void merge(const Property* other);
  };

  class IntProperty : public Property {
  private:
	INT_32 m_value;

  public:
	IntProperty(PropertyId type);
	IntProperty(PropertyId type, INT_32 value);
	virtual ~IntProperty();

	INT_32 getValue() const;
	void setValue(INT_32 value);

	virtual bool mergeData(const Property* other);
  };

  class ArrayProperty : public Property {
  private:
	ValueList m_values;

  public:
	ArrayProperty(PropertyId type);
	ArrayProperty(PropertyId type, ValueList& values);
	virtual ~ArrayProperty();

	const ValueList getValues() const;
	void clearValues();
	void addValue(INT_32 value);
	void setValues(const ValueList& v);

	virtual bool mergeData(const Property* other);

	bool same(const ValueList& lhs, const ValueList& rhs);
  };

  class RescueObject {
  private:
	// No copying
	RescueObject(const RescueObject& rhs);
	RescueObject& operator=(const RescueObject& rhs);

	Id m_id;

	//	PropertyId m_properties[];
	//	int m_numProperties;

  public:
	RescueObject();
	virtual ~RescueObject();
	
	virtual TypeId type() const = 0;

	// Merge a different object into this one.
	void merge(const RescueObject* other);

	// Return a new clone of this object
	RescueObject* clone() const;

	Id id() const;
	void setId(Id newId);

	// Return true iff any of this objects properties have been updated at "time" or later
	bool changed(int time) const;

	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;
  };

  class VirtualObject: public RescueObject {
  public:
	VirtualObject();
	virtual ~VirtualObject();
  };

  class World: public VirtualObject {
  private:
	IntProperty m_startTime;
	IntProperty m_longitude;
	IntProperty m_latitude;
	IntProperty m_windForce;
	IntProperty m_windDirection;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	World();
	virtual ~World();

	virtual TypeId type() const;

	INT_32 getStartTime() const;
	INT_32 getLongitude() const;
	INT_32 getLatitude() const;
	INT_32 getWindForce() const;
	INT_32 getWindDirection() const;

	void setStartTime(INT_32 value, INT_32 time);
	void setLongitude(INT_32 value, INT_32 time);
	void setLatitude(INT_32 value, INT_32 time);
	void setWindForce(INT_32 value, INT_32 time);
	void setWindDirection(INT_32 value, INT_32 time);

	INT_32 getStartTimeUpdate() const;
	INT_32 getLongitudeUpdate() const;
	INT_32 getLatitudeUpdate() const;
	INT_32 getWindForceUpdate() const;
	INT_32 getWindDirectionUpdate() const;
  };

  class RealObject: public RescueObject {
  public:
	RealObject();
	virtual ~RealObject();
  };

  class MovingObject: public RealObject {
  private:
	IntProperty m_position;
	IntProperty m_positionExtra;
	IntProperty m_direction;
	ArrayProperty m_positionHistory;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	MovingObject();
	virtual ~MovingObject();

	INT_32 getPosition() const;
	INT_32 getPositionExtra() const;
	INT_32 getDirection() const;
	ValueList getPositionHistory() const;

	void setPosition(INT_32 value, INT_32 time);
	void setPositionExtra(INT_32 value, INT_32 time);
	void setDirection(INT_32 value, INT_32 time);
	void setPositionHistory(ValueList values, INT_32 time);
	void appendPositionHistory(INT_32 value, INT_32 time);
	void clearPositionHistory(INT_32 time);

	INT_32 getPositionUpdate() const;
	INT_32 getPositionExtraUpdate() const;
	INT_32 getDirectionUpdate() const;
	INT_32 getPositionHistoryUpdate() const;
  };

  class Humanoid: public MovingObject {
  private:
	IntProperty m_stamina;
	IntProperty m_hp;
	IntProperty m_damage;
	IntProperty m_buriedness;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	Humanoid();
	virtual ~Humanoid();

	INT_32 getStamina() const;
	INT_32 getHP() const;
	INT_32 getDamage() const;
	INT_32 getBuriedness() const;

	void setStamina(INT_32 value, INT_32 time);
	void setHP(INT_32 value, INT_32 time);
	void setDamage(INT_32 value, INT_32 time);
	void setBuriedness(INT_32 value, INT_32 time);

	INT_32 getStaminaUpdate() const;
	INT_32 getHPUpdate() const;
	INT_32 getDamageUpdate() const;
	INT_32 getBuriednessUpdate() const;
  };

  class Civilian: public Humanoid {
  public:
	Civilian();
	virtual ~Civilian();

	virtual TypeId type() const;
  };

  class Car: public Humanoid {
  public:
	Car();
	virtual ~Car();

	virtual TypeId type() const;
  };

  class FireBrigade: public Humanoid {
  private:
	IntProperty m_water;
	//	IntProperty m_stretchedLength;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	FireBrigade();
	virtual ~FireBrigade();

	virtual TypeId type() const;

	INT_32 getWater() const;
	//	INT_32 getStretchedLength() const;

	void setWater(INT_32 value, INT_32 time);
	//	void setStretchedLength(INT_32 value, INT_32 time);

	INT_32 getWaterUpdate() const;
	//	INT_32 getStretchedLengthUpdate() const;
  };

  class AmbulanceTeam: public Humanoid {
  public:
	AmbulanceTeam();
	virtual ~AmbulanceTeam();

	virtual TypeId type() const;
  };

  class PoliceForce: public Humanoid {
  public:
	PoliceForce();
	virtual ~PoliceForce();

	virtual TypeId type() const;
  };

  class MotionlessObject: public RealObject {
  public:
	MotionlessObject();
	virtual ~MotionlessObject();
  };

  class Building: public MotionlessObject {
  private:
	IntProperty m_x;
	IntProperty m_y;
	IntProperty m_floors;
	IntProperty m_attributes;
	IntProperty m_ignition;
	IntProperty m_fieryness;
	IntProperty m_brokenness;
	ArrayProperty m_entrances;
	IntProperty m_buildingCode;
	IntProperty m_groundArea;
	IntProperty m_totalArea;
	ArrayProperty m_apexes;
	IntProperty m_temperature;
	IntProperty m_importance;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	Building();
	virtual ~Building();

	virtual TypeId type() const;

	INT_32 getX() const;
	INT_32 getY() const;
	INT_32 getFloors() const;
	INT_32 getAttributes() const;
	INT_32 getIgnition() const;
	INT_32 getFieryness() const;
	INT_32 getBrokenness() const;
	ValueList getEntrances() const;
	INT_32 getBuildingCode() const;
	INT_32 getGroundArea() const;
	INT_32 getTotalArea() const;
	ValueList getApexes() const;
	INT_32 getTemperature() const;
	INT_32 getImportance() const;

	void setX(INT_32 value, INT_32 time);
	void setY(INT_32 value, INT_32 time);
	void setFloors(INT_32 value, INT_32 time);
	void setAttributes(INT_32 value, INT_32 time);
	void setIgnition(INT_32 value, INT_32 time);
	void setFieryness(INT_32 value, INT_32 time);
	void setBrokenness(INT_32 value, INT_32 time);
	void setBuildingCode(INT_32 value, INT_32 time);
	void setGroundArea(INT_32 value, INT_32 time);
	void setTotalArea(INT_32 value, INT_32 time);
	void setEntrances(ValueList values, INT_32 time);
	void appendEntrance(INT_32 value, INT_32 time);
	void clearEntrances(INT_32 time);
	void setApexes(ValueList values, INT_32 time);
	void appendApex(INT_32 value, INT_32 time);
	void clearApexes(INT_32 time);
	void setTemperature(INT_32 value, INT_32 time);
	void setImportance(INT_32 value, INT_32 time);

	INT_32 getXUpdate() const;
	INT_32 getYUpdate() const;
	INT_32 getFloorsUpdate() const;
	INT_32 getAttributesUpdate() const;
	INT_32 getIgnitionUpdate() const;
	INT_32 getFierynessUpdate() const;
	INT_32 getBrokennessUpdate() const;
	INT_32 getBuildingCodeUpdate() const;
	INT_32 getGroundAreaUpdate() const;
	INT_32 getTotalAreaUpdate() const;
	INT_32 getEntrancesUpdate() const;
	INT_32 getApexesUpdate() const;
	INT_32 getTemperatureUpdate() const;
	INT_32 getImportanceUpdate() const;
  };

  class Refuge: public Building {
  public:
	Refuge();
	virtual ~Refuge();

	virtual TypeId type() const;
  };

  class FireStation: public Building {
  public:
	FireStation();
	virtual ~FireStation();

	virtual TypeId type() const;
  };

  class AmbulanceCenter: public Building {
  public:
	AmbulanceCenter();
	virtual ~AmbulanceCenter();

	virtual TypeId type() const;
  };

  class PoliceOffice: public Building {
  public:
	PoliceOffice();
	virtual ~PoliceOffice();

	virtual TypeId type() const;
  };

  class Edge: public MotionlessObject {
  private:
	IntProperty m_head;
	IntProperty m_tail;
	IntProperty m_length;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	Edge();
	virtual ~Edge();

	INT_32 getHead() const;
	INT_32 getTail() const;
	INT_32 getLength() const;

	void setHead(INT_32 value, INT_32 time);
	void setTail(INT_32 value, INT_32 time);
	void setLength(INT_32 value, INT_32 time);

	INT_32 getHeadUpdate() const;
	INT_32 getTailUpdate() const;
	INT_32 getLengthUpdate() const;
  };

  class Road: public Edge {
  private:
	IntProperty m_roadKind;
	IntProperty m_carsPassToHead;
	IntProperty m_carsPassToTail;
	IntProperty m_humansPassToHead;
	IntProperty m_humansPassToTail;
	IntProperty m_width;
	IntProperty m_block;
	IntProperty m_repairCost;
	IntProperty m_medianStrip;
	IntProperty m_linesToHead;
	IntProperty m_linesToTail;
	IntProperty m_widthForWalkers;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	Road();
	virtual ~Road();

	virtual TypeId type() const;

	INT_32 getRoadKind() const;
	INT_32 getCarsPassToHead() const;
	INT_32 getCarsPassToTail() const;
	INT_32 getHumansPassToHead() const;
	INT_32 getHumansPassToTail() const;
	INT_32 getWidth() const;
	INT_32 getBlock() const;
	INT_32 getRepairCost() const;
	INT_32 getMedianStrip() const;
	INT_32 getLinesToHead() const;
	INT_32 getLinesToTail() const;
	INT_32 getWidthForWalkers() const;

	void setRoadKind(INT_32 value, INT_32 time);
	void setCarsPassToHead(INT_32 value, INT_32 time);
	void setCarsPassToTail(INT_32 value, INT_32 time);
	void setHumansPassToHead(INT_32 value, INT_32 time);
	void setHumansPassToTail(INT_32 value, INT_32 time);
	void setWidth(INT_32 value, INT_32 time);
	void setBlock(INT_32 value, INT_32 time);
	void setRepairCost(INT_32 value, INT_32 time);
	void setMedianStrip(INT_32 value, INT_32 time);
	void setLinesToHead(INT_32 value, INT_32 time);
	void setLinesToTail(INT_32 value, INT_32 time);
	void setWidthForWalkers(INT_32 value, INT_32 time);
	
	INT_32 getRoadKindUpdate() const;
	INT_32 getCarsPassToHeadUpdate() const;
	INT_32 getCarsPassToTailUpdate() const;
	INT_32 getHumansPassToHeadUpdate() const;
	INT_32 getHumansPassToTailUpdate() const;
	INT_32 getWidthUpdate() const;
	INT_32 getBlockUpdate() const;
	INT_32 getRepairCostUpdate() const;
	INT_32 getMedianStripUpdate() const;
	INT_32 getLinesToHeadUpdate() const;
	INT_32 getLinesToTailUpdate() const;
	INT_32 getWidthForWalkersUpdate() const;
  };

  class River: public Edge {
  public:
	River();
	virtual ~River();

	virtual TypeId type() const;
  };

  class Vertex: public MotionlessObject {
  private:
	IntProperty m_x;
	IntProperty m_y;
	ArrayProperty m_edges;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	Vertex();
	virtual ~Vertex();

	INT_32 getX() const;
	INT_32 getY() const;
	ValueList getEdges() const;

	void setX(INT_32 value, INT_32 time);
	void setY(INT_32 value, INT_32 time);
	void setEdges(ValueList values, INT_32 time);
	void appendEdge(INT_32 value, INT_32 time);
	void clearEdges(INT_32 time);

	INT_32 getXUpdate() const;
	INT_32 getYUpdate() const;
	INT_32 getEdgesUpdate() const;
  };

  class Node: public Vertex {
  private:
	IntProperty m_signals;
	ArrayProperty m_shortcutToTurn;
	ArrayProperty m_pocketToTurnAcross;
	ArrayProperty m_signalTiming;

  protected:
	virtual Property* getProperty(PropertyId type);
	virtual const Property* getProperty(PropertyId type) const;

  public:
	Node();
	virtual ~Node();

	virtual TypeId type() const;

	INT_32 getSignals() const;
	ValueList getShortcutToTurn() const;
	ValueList getPocketToTurnAcross() const;
	ValueList getSignalTiming() const;

	void setSignals(INT_32 value, INT_32 time);
	void setShortcutToTurn(ValueList values, INT_32 time);
	void appendShortcutToTurn(INT_32 value, INT_32 time);
	void clearShortcutToTurn(INT_32 time);
	void setPocketToTurnAcross(ValueList values, INT_32 time);
	void appendPocketToTurnAcross(INT_32 value, INT_32 time);
	void clearPocketToTurnAcross(INT_32 time);
	void setSignalTiming(ValueList values, INT_32 time);
	void appendSignalTiming(INT_32 value, INT_32 time);
	void clearSignalTiming(INT_32 time);

	INT_32 getSignalsUpdate() const;
	INT_32 getShortcutToTurnUpdate() const;
	INT_32 getPocketToTurnAcrossUpdate() const;
	INT_32 getSignalTimingUpdate() const;
  };

  class RiverNode: public Vertex {
  public:
	RiverNode();
	virtual ~RiverNode();

	virtual TypeId type() const;
  };
}
#endif
