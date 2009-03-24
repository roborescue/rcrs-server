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

#include "objects.h"
#include "common.h"
#include "error.h"
#include <string.h>

namespace Librescue {
  RescueObject* newRescueObject(TypeId type) {
	RescueObject* result = 0;
  	switch (type) {
	case TYPE_WORLD:
	  result = new World();
	  break;
	case TYPE_CIVILIAN:
	  result = new Civilian();
	  break;
	case TYPE_CAR:
	  result = new Car();
	  break;
	case TYPE_FIRE_BRIGADE:
	  result = new FireBrigade();
	  break;
	case TYPE_AMBULANCE_TEAM:
	  result = new AmbulanceTeam();
	  break;
	case TYPE_POLICE_FORCE:
	  result = new PoliceForce();
	  break;
	case TYPE_BUILDING:
	  result = new Building();
	  break;
	case TYPE_REFUGE:
	  result = new Refuge();
	  break;
	case TYPE_FIRE_STATION:
	  result = new FireStation();
	  break;
	case TYPE_AMBULANCE_CENTER:
	  result = new AmbulanceCenter();
	  break;
	case TYPE_POLICE_OFFICE:
	  result = new PoliceOffice();
	  break;
	case TYPE_ROAD:
	  result = new Road();
	  break;
	case TYPE_RIVER:
	  result = new River();
	  break;
	case TYPE_NODE:
	  result = new Node();
	  break;
	case TYPE_RIVER_NODE:
	  result = new RiverNode();
	  break;
	case TYPE_NULL:
	  result = 0;
	  break;
	}
	return result;
  }

  Property::Property(PropertyId type) {
	m_type = type;
	m_lastUpdate = -1;
  }

  Property::~Property() {
  }

  PropertyId Property::type() const {
	return m_type;
  }

  INT_32 Property::lastUpdate() const {
	return m_lastUpdate;
  }

  void Property::setLastUpdate(INT_32 time) {
	m_lastUpdate = time;
  }

  void Property::merge(const Property* other) {
	if (other->m_lastUpdate >= m_lastUpdate) {
	  if (mergeData(other))
	  //	  LOG_DEBUG("Merged property %d. Old timestamp = %d, new timestamp = %d",m_type,m_lastUpdate,other->m_lastUpdate);
		m_lastUpdate = other->m_lastUpdate;
	}
  }

  IntProperty::IntProperty(PropertyId type) : Property(type), m_value(0) {}
  IntProperty::IntProperty(PropertyId type, INT_32 value) : Property(type), m_value(value) {}

  IntProperty::~IntProperty() {}

  INT_32 IntProperty::getValue() const {
	return m_value;
  }

  void IntProperty::setValue(INT_32 value) {
	m_value = value;
  }

  bool IntProperty::mergeData(const Property* other) {
	const IntProperty* a = dynamic_cast<const IntProperty*>(other);
	if (a) {
	  if (m_lastUpdate >= 0 && m_value==a->m_value) return false;
	  m_value = a->m_value;
	  return true;
	}
	return false;
  }

  ArrayProperty::ArrayProperty(PropertyId type) : Property(type) {}

  ArrayProperty::ArrayProperty(PropertyId type, ValueList& values) : Property(type), m_values(values) {}

  ArrayProperty::~ArrayProperty() {}

  const ValueList ArrayProperty::getValues() const {
	return m_values;
  }

  void ArrayProperty::setValues(const ValueList& values) {
	m_values.clear();
	m_values.reserve(values.size());
	m_values.insert(m_values.begin(),values.begin(),values.end());
  }

  void ArrayProperty::addValue(INT_32 value) {
	m_values.push_back(value);
  }

  void ArrayProperty::clearValues() {
	m_values.clear();
  }

  bool ArrayProperty::mergeData(const Property* other) {
	const ArrayProperty* a = dynamic_cast<const ArrayProperty*>(other);
	if (a) {
	  // Is the current value the same as the new one?
	  if (m_lastUpdate >= 0 && same(m_values,a->getValues())) return false;
	  setValues(a->getValues());
	  return true;
	}
	return false;
  }

  bool ArrayProperty::same(const ValueList& lhs, const ValueList& rhs) {
	if (lhs.size()!=rhs.size()) return false;
	ValueList::const_iterator a = lhs.begin();
	ValueList::const_iterator b = rhs.begin();
	while (a!=lhs.end()) {
	  if (*a != *b) return false;
	  ++a;
	  ++b;
	}
	return true;
  }

  RescueObject::RescueObject() {
	m_id = 0;
  }

  RescueObject::~RescueObject() {
  }

  Id RescueObject::id() const {
	return m_id;
  }

  void RescueObject::setId(Id id) {
	m_id = id;
  }

  Property* RescueObject::getProperty(PropertyId type) {
	return 0;
  }

  const Property* RescueObject::getProperty(PropertyId type) const {
	return 0;
  }


  void RescueObject::merge(const RescueObject* other) {
	for (int i=PROPERTY_MIN;i<=PROPERTY_MAX;++i) {
	  Property* me = getProperty((PropertyId)i);
	  const Property* you = other->getProperty((PropertyId)i);
	  if (me && you) {
		me->merge(you);
	  }
	}
  }

  RescueObject* RescueObject::clone() const {
	RescueObject* result = newRescueObject(type());
	result->setId(m_id);
	result->merge(this);
	return result;
  }

  bool RescueObject::changed(int time) const {
	for (int i=PROPERTY_MIN;i<=PROPERTY_MAX;++i) {
	  const Property* next = getProperty((PropertyId)i);
	  if (next && next->lastUpdate()>=time) return true;
	}
	return false;
  }

  VirtualObject::VirtualObject() {
  }

  VirtualObject::~VirtualObject() {
  }

  World::World(): m_startTime(PROPERTY_START_TIME), m_longitude(PROPERTY_LONGITUDE), m_latitude(PROPERTY_LATITUDE), m_windForce(PROPERTY_WIND_FORCE), m_windDirection(PROPERTY_WIND_DIRECTION) {
  }

  World::~World() {
  }

  TypeId World::type() const {
	return TYPE_WORLD;
  }

  Property* World::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_START_TIME:
	  return &m_startTime;
	case PROPERTY_LONGITUDE:
	  return &m_longitude;
	case PROPERTY_LATITUDE:
	  return &m_latitude;
	case PROPERTY_WIND_FORCE:
	  return &m_windForce;
	case PROPERTY_WIND_DIRECTION:
	  return &m_windDirection;
	default:
	  return VirtualObject::getProperty(type);
	}
  }

  const Property* World::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_START_TIME:
	  return &m_startTime;
	case PROPERTY_LONGITUDE:
	  return &m_longitude;
	case PROPERTY_LATITUDE:
	  return &m_latitude;
	case PROPERTY_WIND_FORCE:
	  return &m_windForce;
	case PROPERTY_WIND_DIRECTION:
	  return &m_windDirection;
	default:
	  return VirtualObject::getProperty(type);
	}
  }

  INT_32 World::getStartTime() const {
	return m_startTime.getValue();
  }

  INT_32 World::getLongitude() const {
	return m_longitude.getValue();
  }

  INT_32 World::getLatitude() const {
	return m_latitude.getValue();
  }

  INT_32 World::getWindForce() const {
	return m_windForce.getValue();
  }

  INT_32 World::getWindDirection() const {
	return m_windDirection.getValue();
  }

  void World::setStartTime(INT_32 value, INT_32 time) {
	m_startTime.setValue(value);
	m_startTime.setLastUpdate(time);
  }

  void World::setLongitude(INT_32 value, INT_32 time) {
	m_longitude.setValue(value);
	m_longitude.setLastUpdate(time);
  }

  void World::setLatitude(INT_32 value, INT_32 time) {
	m_latitude.setValue(value);
	m_latitude.setLastUpdate(time);
  }

  void World::setWindForce(INT_32 value, INT_32 time) {
	m_windForce.setValue(value);
	m_windForce.setLastUpdate(time);
  }

  void World::setWindDirection(INT_32 value, INT_32 time) {
	m_windDirection.setValue(value);
	m_windDirection.setLastUpdate(time);
  }

  INT_32 World::getStartTimeUpdate() const {
	return m_startTime.lastUpdate();
  }

  INT_32 World::getLongitudeUpdate() const {
	return m_longitude.lastUpdate();
  }

  INT_32 World::getLatitudeUpdate() const {
	return m_latitude.lastUpdate();
  }

  INT_32 World::getWindForceUpdate() const {
	return m_windForce.lastUpdate();
  }

  INT_32 World::getWindDirectionUpdate() const {
	return m_windDirection.lastUpdate();
  }

  RealObject::RealObject() {
  }

  RealObject::~RealObject() {
  }

  MovingObject::MovingObject(): m_position(PROPERTY_POSITION), m_positionExtra(PROPERTY_POSITION_EXTRA), m_direction(PROPERTY_DIRECTION), m_positionHistory(PROPERTY_POSITION_HISTORY) {
  }

  MovingObject::~MovingObject() {
  }

  Property* MovingObject::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_POSITION:
	  return &m_position;
	case PROPERTY_POSITION_EXTRA:
	  return &m_positionExtra;
	case PROPERTY_DIRECTION:
	  return &m_direction;
	case PROPERTY_POSITION_HISTORY:
	  return &m_positionHistory;
	default:
	  return RealObject::getProperty(type);
	}
  }

  const Property* MovingObject::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_POSITION:
	  return &m_position;
	case PROPERTY_POSITION_EXTRA:
	  return &m_positionExtra;
	case PROPERTY_DIRECTION:
	  return &m_direction;
	case PROPERTY_POSITION_HISTORY:
	  return &m_positionHistory;
	default:
	  return RealObject::getProperty(type);
	}
  }

  INT_32 MovingObject::getPosition() const {
	return m_position.getValue();
  }

  INT_32 MovingObject::getPositionExtra() const {
	return m_positionExtra.getValue();
  }

  INT_32 MovingObject::getDirection() const {
	return m_direction.getValue();
  }

  ValueList MovingObject::getPositionHistory() const {
	return m_positionHistory.getValues();
  }

  void MovingObject::setPosition(INT_32 value, INT_32 time) {
	m_position.setValue(value);
	m_position.setLastUpdate(time);
  }

  void MovingObject::setPositionExtra(INT_32 value, INT_32 time) {
	m_positionExtra.setValue(value);
	m_positionExtra.setLastUpdate(time);
  }

  void MovingObject::setDirection(INT_32 value, INT_32 time) {
	m_direction.setValue(value);
	m_direction.setLastUpdate(time);
  }

  void MovingObject::setPositionHistory(ValueList values, INT_32 time) {
	m_positionHistory.setValues(values);
	m_positionHistory.setLastUpdate(time);
  }

  void MovingObject::appendPositionHistory(INT_32 value, INT_32 time) {
	m_positionHistory.addValue(value);
	m_positionHistory.setLastUpdate(time);
  }

  void MovingObject::clearPositionHistory(INT_32 time) {
	m_positionHistory.clearValues();
	m_positionHistory.setLastUpdate(time);
  }

  INT_32 MovingObject::getPositionUpdate() const {
	return m_position.lastUpdate();
  }

  INT_32 MovingObject::getPositionExtraUpdate() const {
	return m_positionExtra.lastUpdate();
  }

  INT_32 MovingObject::getDirectionUpdate() const {
	return m_direction.lastUpdate();
  }

  INT_32 MovingObject::getPositionHistoryUpdate() const {
	return m_positionHistory.lastUpdate();  
  }

  Humanoid::Humanoid(): m_stamina(PROPERTY_STAMINA), m_hp(PROPERTY_HP), m_damage(PROPERTY_DAMAGE), m_buriedness(PROPERTY_BURIEDNESS) {
  }

  Humanoid::~Humanoid() {
  }

  Property* Humanoid::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_STAMINA:
	  return &m_stamina;
	case PROPERTY_HP:
	  return &m_hp;
	case PROPERTY_DAMAGE:
	  return &m_damage;
	case PROPERTY_BURIEDNESS:
	  return &m_buriedness;
	default:
	  return MovingObject::getProperty(type);
	}
  }

  const Property* Humanoid::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_STAMINA:
	  return &m_stamina;
	case PROPERTY_HP:
	  return &m_hp;
	case PROPERTY_DAMAGE:
	  return &m_damage;
	case PROPERTY_BURIEDNESS:
	  return &m_buriedness;
	default:
	  return MovingObject::getProperty(type);
	}
  }

  INT_32 Humanoid::getStamina() const {
	return m_stamina.getValue();
  }

  INT_32 Humanoid::getHP() const {
	return m_hp.getValue();
  }

  INT_32 Humanoid::getDamage() const {
	return m_damage.getValue();
  }

  INT_32 Humanoid::getBuriedness() const {
	return m_buriedness.getValue();
  }

  void Humanoid::setStamina(INT_32 value, INT_32 time) {
	m_stamina.setValue(value);
	m_stamina.setLastUpdate(time);
  }

  void Humanoid::setHP(INT_32 value, INT_32 time) {
	m_hp.setValue(value);
	m_hp.setLastUpdate(time);
  }

  void Humanoid::setDamage(INT_32 value, INT_32 time) {
	m_damage.setValue(value);
	m_damage.setLastUpdate(time);
  }

  void Humanoid::setBuriedness(INT_32 value, INT_32 time) {
	m_buriedness.setValue(value);
	m_buriedness.setLastUpdate(time);
  }

  INT_32 Humanoid::getStaminaUpdate() const {
	return m_stamina.lastUpdate();
  }

  INT_32 Humanoid::getHPUpdate() const {
	return m_hp.lastUpdate();
  }

  INT_32 Humanoid::getDamageUpdate() const {
	return m_damage.lastUpdate();
  }

  INT_32 Humanoid::getBuriednessUpdate() const {
	return m_buriedness.lastUpdate();
  }

  Civilian::Civilian() {
  }

  Civilian::~Civilian() {
  }

  TypeId Civilian::type() const {
	return TYPE_CIVILIAN;
  }

  Car::Car() {
  }

  Car::~Car() {
  }

  TypeId Car::type() const {
	return TYPE_CAR;
  }

  FireBrigade::FireBrigade(): m_water(PROPERTY_WATER_QUANTITY) /*, m_stretchedLength(PROPERTY_STRETCHED_LENGTH)*/ {
  }

  FireBrigade::~FireBrigade() {
  }

  TypeId FireBrigade::type() const {
	return TYPE_FIRE_BRIGADE;
  }

  Property* FireBrigade::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_WATER_QUANTITY:
	  return &m_water;
	  //	case PROPERTY_STRETCHED_LENGTH:
	  //	  return &m_stretchedLength;
	default:
	  return Humanoid::getProperty(type);
	}
  }

  const Property* FireBrigade::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_WATER_QUANTITY:
	  return &m_water;
	  //	case PROPERTY_STRETCHED_LENGTH:
	  //	  return &m_stretchedLength;
	default:
	  return Humanoid::getProperty(type);
	}
  }

  INT_32 FireBrigade::getWater() const {
	return m_water.getValue();
  }

  //  INT_32 FireBrigade::getStretchedLength() const {
  //	return m_stretchedLength.getValue();
  //  }

  void FireBrigade::setWater(INT_32 value, INT_32 time) {
	m_water.setValue(value);
	m_water.setLastUpdate(time);
  }

  //  void FireBrigade::setStretchedLength(INT_32 value, INT_32 time) {
  //	m_stretchedLength.setValue(value);
  //	m_stretchedLength.setLastUpdate(time);
  //  }

  INT_32 FireBrigade::getWaterUpdate() const {
	return m_water.lastUpdate();
  }

  //  INT_32 FireBrigade::getStretchedLengthUpdate() const {
  //	return m_stretchedLength.lastUpdate();
  //  }

  AmbulanceTeam::AmbulanceTeam() {
  }

  AmbulanceTeam::~AmbulanceTeam() {
  }

  TypeId AmbulanceTeam::type() const {
	return TYPE_AMBULANCE_TEAM;
  }

  PoliceForce::PoliceForce() {
  }

  PoliceForce::~PoliceForce() {
  }

  TypeId PoliceForce::type() const {
	return TYPE_POLICE_FORCE;
  }

  MotionlessObject::MotionlessObject() {
  }

  MotionlessObject::~MotionlessObject() {
  }

  Building::Building(): m_x(PROPERTY_X), m_y(PROPERTY_Y), m_floors(PROPERTY_FLOORS), m_attributes(PROPERTY_BUILDING_ATTRIBUTES), m_ignition(PROPERTY_IGNITION), m_fieryness(PROPERTY_FIERYNESS), m_brokenness(PROPERTY_BROKENNESS), m_entrances(PROPERTY_ENTRANCES), m_buildingCode(PROPERTY_BUILDING_CODE), m_groundArea(PROPERTY_BUILDING_AREA_GROUND), m_totalArea(PROPERTY_BUILDING_AREA_TOTAL), m_apexes(PROPERTY_BUILDING_APEXES), m_temperature(PROPERTY_BUILDING_TEMPERATURE), m_importance(PROPERTY_BUILDING_IMPORTANCE) {
  }

  Building::~Building() {
  }

  TypeId Building::type() const {
	return TYPE_BUILDING;
  }

  Property* Building::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_X:
	  return &m_x;
	case PROPERTY_Y:
	  return &m_y;
	case PROPERTY_FLOORS:
	  return &m_floors;
	case PROPERTY_BUILDING_ATTRIBUTES:
	  return &m_attributes;
	case PROPERTY_IGNITION:
	  return &m_ignition;
	case PROPERTY_FIERYNESS:
	  return &m_fieryness;
	case PROPERTY_BROKENNESS:
	  return &m_brokenness;
	case PROPERTY_ENTRANCES:
	  return &m_entrances;
	case PROPERTY_BUILDING_CODE:
	  return &m_buildingCode;
	case PROPERTY_BUILDING_AREA_GROUND:
	  return &m_groundArea;
	case PROPERTY_BUILDING_AREA_TOTAL:
	  return &m_totalArea;
	case PROPERTY_BUILDING_APEXES:
	  return &m_apexes;
	case PROPERTY_BUILDING_TEMPERATURE:
	  return &m_temperature;
	case PROPERTY_BUILDING_IMPORTANCE:
	  return &m_importance;
	default:
	  return MotionlessObject::getProperty(type);
	}
  }

  const Property* Building::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_X:
	  return &m_x;
	case PROPERTY_Y:
	  return &m_y;
	case PROPERTY_FLOORS:
	  return &m_floors;
	case PROPERTY_BUILDING_ATTRIBUTES:
	  return &m_attributes;
	case PROPERTY_IGNITION:
	  return &m_ignition;
	case PROPERTY_FIERYNESS:
	  return &m_fieryness;
	case PROPERTY_BROKENNESS:
	  return &m_brokenness;
	case PROPERTY_ENTRANCES:
	  return &m_entrances;
	case PROPERTY_BUILDING_CODE:
	  return &m_buildingCode;
	case PROPERTY_BUILDING_AREA_GROUND:
	  return &m_groundArea;
	case PROPERTY_BUILDING_AREA_TOTAL:
	  return &m_totalArea;
	case PROPERTY_BUILDING_APEXES:
	  return &m_apexes;
	case PROPERTY_BUILDING_TEMPERATURE:
	  return &m_temperature;
	case PROPERTY_BUILDING_IMPORTANCE:
	  return &m_importance;
	default:
	  return MotionlessObject::getProperty(type);
	}
  }

  INT_32 Building::getX() const {
	return m_x.getValue();
  }

  INT_32 Building::getY() const {
	return m_y.getValue();
  }

  INT_32 Building::getFloors() const {
	return m_floors.getValue();
  }

  INT_32 Building::getAttributes() const {
	return m_attributes.getValue();
  }

  INT_32 Building::getIgnition() const {
	return m_ignition.getValue();
  }

  INT_32 Building::getFieryness() const {
	return m_fieryness.getValue();
  }

  INT_32 Building::getBrokenness() const {
	return m_brokenness.getValue();
  }

  ValueList Building::getEntrances() const {
	return m_entrances.getValues();
  }

  INT_32 Building::getBuildingCode() const {
	return m_buildingCode.getValue();
  }

  INT_32 Building::getGroundArea() const {
	return m_groundArea.getValue();
  }

  INT_32 Building::getTotalArea() const {
	return m_totalArea.getValue();
  }

  ValueList Building::getApexes() const {
	return m_apexes.getValues();
  }

  INT_32 Building::getTemperature() const {
	return m_temperature.getValue();
  }

  INT_32 Building::getImportance() const {
	return m_importance.getValue();
  }

  void Building::setX(INT_32 value, INT_32 time) {
	m_x.setValue(value);
	m_x.setLastUpdate(time);
  }

  void Building::setY(INT_32 value, INT_32 time) {
	m_y.setValue(value);
	m_y.setLastUpdate(time);
  }

  void Building::setFloors(INT_32 value, INT_32 time) {
	m_floors.setValue(value);
	m_floors.setLastUpdate(time);
  }

  void Building::setAttributes(INT_32 value, INT_32 time) {
	m_attributes.setValue(value);
	m_attributes.setLastUpdate(time);
  }

  void Building::setIgnition(INT_32 value, INT_32 time) {
	m_ignition.setValue(value);
	m_ignition.setLastUpdate(time);
  }

  void Building::setFieryness(INT_32 value, INT_32 time) {
	m_fieryness.setValue(value);
	m_fieryness.setLastUpdate(time);
  }

  void Building::setBrokenness(INT_32 value, INT_32 time) {
	m_brokenness.setValue(value);
	m_brokenness.setLastUpdate(time);
  }

  void Building::setBuildingCode(INT_32 value, INT_32 time) {
	m_buildingCode.setValue(value);
	m_buildingCode.setLastUpdate(time);
  }

  void Building::setGroundArea(INT_32 value, INT_32 time) {
	m_groundArea.setValue(value);
	m_groundArea.setLastUpdate(time);
  }

  void Building::setTotalArea(INT_32 value, INT_32 time) {
	m_totalArea.setValue(value);
	m_totalArea.setLastUpdate(time);
  }

  void Building::setEntrances(ValueList values, INT_32 time) {
	m_entrances.setValues(values);
	m_entrances.setLastUpdate(time);
  }

  void Building::appendEntrance(INT_32 value, INT_32 time) {
	m_entrances.addValue(value);
	m_entrances.setLastUpdate(time);
  }

  void Building::clearEntrances(INT_32 time) {
	m_entrances.clearValues();
	m_entrances.setLastUpdate(time);
  }

  void Building::setApexes(ValueList values, INT_32 time) {
	//	logDebug("Setting building apexes");
	//	for (int i=0;values[i]!=0;++i) {
	//	  snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Apex %d = %d",i,values[i]);
	//	  logDebug(errorBuffer);
	//	}
	m_apexes.setValues(values);
	m_apexes.setLastUpdate(time);
  }

  void Building::appendApex(INT_32 value, INT_32 time) {
	m_apexes.addValue(value);
	m_apexes.setLastUpdate(time);
  }

  void Building::clearApexes(INT_32 time) {
	m_apexes.clearValues();
	m_apexes.setLastUpdate(time);
  }

  void Building::setTemperature(INT_32 value, INT_32 time) {
	m_temperature.setValue(value);
	m_temperature.setLastUpdate(time);
  }

  void Building::setImportance(INT_32 value, INT_32 time) {
	m_importance.setValue(value);
	m_importance.setLastUpdate(time);
  }

  INT_32 Building::getXUpdate() const {
	return m_x.lastUpdate();
  }

  INT_32 Building::getYUpdate() const {
	return m_y.lastUpdate();
  }

  INT_32 Building::getFloorsUpdate() const {
	return m_floors.lastUpdate();
  }

  INT_32 Building::getAttributesUpdate() const {
	return m_attributes.lastUpdate();
  }

  INT_32 Building::getIgnitionUpdate() const {
	return m_ignition.lastUpdate();
  }

  INT_32 Building::getFierynessUpdate() const {
	return m_fieryness.lastUpdate();
  }

  INT_32 Building::getBrokennessUpdate() const {
	return m_brokenness.lastUpdate();
  }

  INT_32 Building::getBuildingCodeUpdate() const {
	return m_buildingCode.lastUpdate();
  }

  INT_32 Building::getGroundAreaUpdate() const {
	return m_groundArea.lastUpdate();
  }

  INT_32 Building::getTotalAreaUpdate() const {
	return m_totalArea.lastUpdate();
  }

  INT_32 Building::getEntrancesUpdate() const {
	return m_entrances.lastUpdate();
  }

  INT_32 Building::getApexesUpdate() const {
	return m_apexes.lastUpdate();
  }

  INT_32 Building::getTemperatureUpdate() const {
	return m_temperature.lastUpdate();
  }

  INT_32 Building::getImportanceUpdate() const {
	return m_importance.lastUpdate();
  }

  Refuge::Refuge() {
  }

  Refuge::~Refuge() {
  }

  TypeId Refuge::type() const {
	return TYPE_REFUGE;
  }

  FireStation::FireStation() {
  }

  FireStation::~FireStation() {
  }

  TypeId FireStation::type() const {
	return TYPE_FIRE_STATION;
  }

  AmbulanceCenter::AmbulanceCenter() {
  }

  AmbulanceCenter::~AmbulanceCenter() {
  }

  TypeId AmbulanceCenter::type() const {
	return TYPE_AMBULANCE_CENTER;
  }

  PoliceOffice::PoliceOffice() {
  }

  PoliceOffice::~PoliceOffice() {
  }

  TypeId PoliceOffice::type() const {
	return TYPE_POLICE_OFFICE;
  }

  Edge::Edge(): m_head(PROPERTY_HEAD), m_tail(PROPERTY_TAIL), m_length(PROPERTY_LENGTH) {
  }

  Edge::~Edge() {
  }

  Property* Edge::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_HEAD:
	  return &m_head;
	case PROPERTY_TAIL:
	  return &m_tail;
	case PROPERTY_LENGTH:
	  return &m_length;
	default:
	  return MotionlessObject::getProperty(type);
	}
  }

  const Property* Edge::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_HEAD:
	  return &m_head;
	case PROPERTY_TAIL:
	  return &m_tail;
	case PROPERTY_LENGTH:
	  return &m_length;
	default:
	  return MotionlessObject::getProperty(type);
	}
  }

  INT_32 Edge::getHead() const {
	return m_head.getValue();
  }

  INT_32 Edge::getTail() const {
	return m_tail.getValue();
  }

  INT_32 Edge::getLength() const {
	return m_length.getValue();
  }

  void Edge::setHead(INT_32 value, INT_32 time) {
	m_head.setValue(value);
	m_head.setLastUpdate(time);
  }

  void Edge::setTail(INT_32 value, INT_32 time) {
	m_tail.setValue(value);
	m_tail.setLastUpdate(time);
  }

  void Edge::setLength(INT_32 value, INT_32 time) {
	m_length.setValue(value);
	m_length.setLastUpdate(time);
  }

  INT_32 Edge::getHeadUpdate() const {
	return m_head.lastUpdate();
  }

  INT_32 Edge::getTailUpdate() const {
	return m_tail.lastUpdate();
  }

  INT_32 Edge::getLengthUpdate() const {
	return m_length.lastUpdate();
  }

  Road::Road(): m_roadKind(PROPERTY_ROAD_KIND), m_carsPassToHead(PROPERTY_CARS_PASS_TO_HEAD), m_carsPassToTail(PROPERTY_CARS_PASS_TO_TAIL), m_humansPassToHead(PROPERTY_HUMANS_PASS_TO_HEAD), m_humansPassToTail(PROPERTY_HUMANS_PASS_TO_TAIL), m_width(PROPERTY_WIDTH), m_block(PROPERTY_BLOCK), m_repairCost(PROPERTY_REPAIR_COST), m_medianStrip(PROPERTY_MEDIAN_STRIP), m_linesToHead(PROPERTY_LINES_TO_HEAD), m_linesToTail(PROPERTY_LINES_TO_TAIL), m_widthForWalkers(PROPERTY_WIDTH_FOR_WALKERS) {
  }

  Road::~Road() {
  }

  TypeId Road::type() const {
	return TYPE_ROAD;
  }

  Property* Road::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_ROAD_KIND:
	  return &m_roadKind;
	case PROPERTY_CARS_PASS_TO_HEAD:
	  return &m_carsPassToHead;
	case PROPERTY_CARS_PASS_TO_TAIL:
	  return &m_carsPassToTail;
	case PROPERTY_HUMANS_PASS_TO_HEAD:
	  return &m_humansPassToHead;
	case PROPERTY_HUMANS_PASS_TO_TAIL:
	  return &m_humansPassToTail;
	case PROPERTY_WIDTH:
	  return &m_width;
	case PROPERTY_BLOCK:
	  return &m_block;
	case PROPERTY_REPAIR_COST:
	  return &m_repairCost;
	case PROPERTY_MEDIAN_STRIP:
	  return &m_medianStrip;
	case PROPERTY_LINES_TO_HEAD:
	  return &m_linesToHead;
	case PROPERTY_LINES_TO_TAIL:
	  return &m_linesToTail;
	case PROPERTY_WIDTH_FOR_WALKERS:
	  return &m_widthForWalkers;
	default:
	  return Edge::getProperty(type);
	}
  }

  const Property* Road::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_ROAD_KIND:
	  return &m_roadKind;
	case PROPERTY_CARS_PASS_TO_HEAD:
	  return &m_carsPassToHead;
	case PROPERTY_CARS_PASS_TO_TAIL:
	  return &m_carsPassToTail;
	case PROPERTY_HUMANS_PASS_TO_HEAD:
	  return &m_humansPassToHead;
	case PROPERTY_HUMANS_PASS_TO_TAIL:
	  return &m_humansPassToTail;
	case PROPERTY_WIDTH:
	  return &m_width;
	case PROPERTY_BLOCK:
	  return &m_block;
	case PROPERTY_REPAIR_COST:
	  return &m_repairCost;
	case PROPERTY_MEDIAN_STRIP:
	  return &m_medianStrip;
	case PROPERTY_LINES_TO_HEAD:
	  return &m_linesToHead;
	case PROPERTY_LINES_TO_TAIL:
	  return &m_linesToTail;
	case PROPERTY_WIDTH_FOR_WALKERS:
	  return &m_widthForWalkers;
	default:
	  return Edge::getProperty(type);
	}
  }

  INT_32 Road::getRoadKind() const {
	return m_roadKind.getValue();
  }
  INT_32 Road::getCarsPassToHead() const {
	return m_carsPassToHead.getValue();
  }
  INT_32 Road::getCarsPassToTail() const {
	return m_carsPassToTail.getValue();
  }
  INT_32 Road::getHumansPassToHead() const {
	return m_humansPassToHead.getValue();
  }
  INT_32 Road::getHumansPassToTail() const {
	return m_humansPassToTail.getValue();
  }
  INT_32 Road::getWidth() const {
	return m_width.getValue();
  }
  INT_32 Road::getBlock() const {
	return m_block.getValue();
  }
  INT_32 Road::getRepairCost() const {
	return m_repairCost.getValue();
  }
  INT_32 Road::getMedianStrip() const {
	return m_medianStrip.getValue();
  }
  INT_32 Road::getLinesToHead() const {
	return m_linesToHead.getValue();
  }
  INT_32 Road::getLinesToTail() const {
	return m_linesToTail.getValue();
  }
  INT_32 Road::getWidthForWalkers() const {
	return m_widthForWalkers.getValue();
  }

  void Road::setRoadKind(INT_32 value, INT_32 time) {
	m_roadKind.setValue(value);
	m_roadKind.setLastUpdate(time);
  }
  void Road::setCarsPassToHead(INT_32 value, INT_32 time) {
	m_carsPassToHead.setValue(value);
	m_carsPassToHead.setLastUpdate(time);
  }
  void Road::setCarsPassToTail(INT_32 value, INT_32 time) {
	m_carsPassToTail.setValue(value);
	m_carsPassToTail.setLastUpdate(time);
  }
  void Road::setHumansPassToHead(INT_32 value, INT_32 time) {
	m_humansPassToHead.setValue(value);
	m_humansPassToHead.setLastUpdate(time);
  }
  void Road::setHumansPassToTail(INT_32 value, INT_32 time) {
	m_humansPassToTail.setValue(value);
	m_humansPassToTail.setLastUpdate(time);
  }
  void Road::setWidth(INT_32 value, INT_32 time) {
	m_width.setValue(value);
	m_width.setLastUpdate(time);
  }
  void Road::setBlock(INT_32 value, INT_32 time) {
	m_block.setValue(value);
	m_block.setLastUpdate(time);
  }
  void Road::setRepairCost(INT_32 value, INT_32 time) {
	m_repairCost.setValue(value);
	m_repairCost.setLastUpdate(time);
  }
  void Road::setMedianStrip(INT_32 value, INT_32 time) {
	m_medianStrip.setValue(value);
	m_medianStrip.setLastUpdate(time);
  }
  void Road::setLinesToHead(INT_32 value, INT_32 time) {
	m_linesToHead.setValue(value);
	m_linesToHead.setLastUpdate(time);
  }
  void Road::setLinesToTail(INT_32 value, INT_32 time) {
	m_linesToTail.setValue(value);
	m_linesToTail.setLastUpdate(time);
  }
  void Road::setWidthForWalkers(INT_32 value, INT_32 time) {
	m_widthForWalkers.setValue(value);
	m_widthForWalkers.setLastUpdate(time);
  }
	
  INT_32 Road::getRoadKindUpdate() const {
	return m_roadKind.lastUpdate();
  }

  INT_32 Road::getCarsPassToHeadUpdate() const {
	return m_carsPassToHead.lastUpdate();
  }
  INT_32 Road::getCarsPassToTailUpdate() const {
	return m_carsPassToTail.lastUpdate();
  }
  INT_32 Road::getHumansPassToHeadUpdate() const {
	return m_humansPassToHead.lastUpdate();
  }
  INT_32 Road::getHumansPassToTailUpdate() const {
	return m_humansPassToTail.lastUpdate();
  }
  INT_32 Road::getWidthUpdate() const {
	return m_width.lastUpdate();
  }
  INT_32 Road::getBlockUpdate() const {
	return m_block.lastUpdate();
  }
  INT_32 Road::getRepairCostUpdate() const {
	return m_repairCost.lastUpdate();
  }
  INT_32 Road::getMedianStripUpdate() const {
	return m_medianStrip.lastUpdate();
  }
  INT_32 Road::getLinesToHeadUpdate() const {
	return m_linesToHead.lastUpdate();
  }
  INT_32 Road::getLinesToTailUpdate() const {
	return m_linesToTail.lastUpdate();
  }
  INT_32 Road::getWidthForWalkersUpdate() const {
	return m_widthForWalkers.lastUpdate();
  }

  River::River() {
  }

  River::~River() {
  }

  TypeId River::type() const {
	return TYPE_RIVER;
  }


  Vertex::Vertex(): m_x(PROPERTY_X), m_y(PROPERTY_Y), m_edges(PROPERTY_EDGES) {
  }

  Vertex::~Vertex() {
  }

  Property* Vertex::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_X:
	  return &m_x;
	case PROPERTY_Y:
	  return &m_y;
	case PROPERTY_EDGES:
	  return &m_edges;
	default:
	  return MotionlessObject::getProperty(type);
	}
  }

  const Property* Vertex::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_X:
	  return &m_x;
	case PROPERTY_Y:
	  return &m_y;
	case PROPERTY_EDGES:
	  return &m_edges;
	default:
	  return MotionlessObject::getProperty(type);
	}
  }

  INT_32 Vertex::getX() const {
	return m_x.getValue();
  }

  INT_32 Vertex::getY() const {
	return m_y.getValue();
  }

  ValueList Vertex::getEdges() const {
	return m_edges.getValues();
  }

  void Vertex::setX(INT_32 value, INT_32 time) {
	m_x.setValue(value);
	m_x.setLastUpdate(time);
  }

  void Vertex::setY(INT_32 value, INT_32 time) {
	m_y.setValue(value);
	m_y.setLastUpdate(time);
  }

  void Vertex::setEdges(ValueList values, INT_32 time) {
	m_edges.setValues(values);
	m_edges.setLastUpdate(time);
  }

  void Vertex::appendEdge(INT_32 value, INT_32 time) {
	m_edges.addValue(value);
	m_edges.setLastUpdate(time);
  }

  void Vertex::clearEdges(INT_32 time) {
	m_edges.clearValues();
	m_edges.setLastUpdate(time);
  }

  INT_32 Vertex::getXUpdate() const {
	return m_x.lastUpdate();
  }

  INT_32 Vertex::getYUpdate() const {
	return m_y.lastUpdate();
  }

  INT_32 Vertex::getEdgesUpdate() const {
	return m_edges.lastUpdate();
  }

  Node::Node(): m_signals(PROPERTY_SIGNAL), m_shortcutToTurn(PROPERTY_SHORTCUT_TO_TURN), m_pocketToTurnAcross(PROPERTY_POCKET_TO_TURN_ACROSS), m_signalTiming(PROPERTY_SIGNAL_TIMING) {
  }

  Node::~Node() {
  }

  TypeId Node::type() const {
	return TYPE_NODE;
  }

  Property* Node::getProperty(PropertyId type) {
	switch (type) {
	case PROPERTY_SIGNAL:
	  return &m_signals;
	case PROPERTY_SHORTCUT_TO_TURN:
	  return &m_shortcutToTurn;
	case PROPERTY_POCKET_TO_TURN_ACROSS:
	  return &m_pocketToTurnAcross;
	case PROPERTY_SIGNAL_TIMING:
	  return &m_signalTiming;
	default:
	  return Vertex::getProperty(type);
	}
  }

  const Property* Node::getProperty(PropertyId type) const{
	switch (type) {
	case PROPERTY_SIGNAL:
	  return &m_signals;
	case PROPERTY_SHORTCUT_TO_TURN:
	  return &m_shortcutToTurn;
	case PROPERTY_POCKET_TO_TURN_ACROSS:
	  return &m_pocketToTurnAcross;
	case PROPERTY_SIGNAL_TIMING:
	  return &m_signalTiming;
	default:
	  return Vertex::getProperty(type);
	}
  }

  INT_32 Node::getSignals() const {
	return m_signals.getValue();
  }
  ValueList Node::getShortcutToTurn() const {
	return m_shortcutToTurn.getValues();
  }
  ValueList Node::getPocketToTurnAcross() const {
	return m_pocketToTurnAcross.getValues();
  }
  ValueList Node::getSignalTiming() const {
	return m_signalTiming.getValues();
  }

  void Node::setSignals(INT_32 value, INT_32 time) {
	m_signals.setValue(value);
	m_signals.setLastUpdate(time);
  }

  void Node::setShortcutToTurn(ValueList values, INT_32 time) {
	m_shortcutToTurn.setValues(values);
	m_shortcutToTurn.setLastUpdate(time);
  }

  void Node::appendShortcutToTurn(INT_32 value, INT_32 time) {
	m_shortcutToTurn.addValue(value);
	m_shortcutToTurn.setLastUpdate(time);
  }

  void Node::clearShortcutToTurn(INT_32 time) {
	m_shortcutToTurn.clearValues();
	m_shortcutToTurn.setLastUpdate(time);
  }

  void Node::setPocketToTurnAcross(ValueList values, INT_32 time) {
	m_pocketToTurnAcross.setValues(values);
	m_pocketToTurnAcross.setLastUpdate(time);
  }

  void Node::appendPocketToTurnAcross(INT_32 value, INT_32 time) {
	m_pocketToTurnAcross.addValue(value);
	m_pocketToTurnAcross.setLastUpdate(time);
  }

  void Node::clearPocketToTurnAcross(INT_32 time) {
	m_pocketToTurnAcross.clearValues();
	m_pocketToTurnAcross.setLastUpdate(time);
  }

  void Node::setSignalTiming(ValueList values, INT_32 time) {
	m_signalTiming.setValues(values);
	m_signalTiming.setLastUpdate(time);
  }

  void Node::appendSignalTiming(INT_32 value, INT_32 time) {
	m_signalTiming.addValue(value);
	m_signalTiming.setLastUpdate(time);
  }

  void Node::clearSignalTiming(INT_32 time) {
	m_signalTiming.clearValues();
	m_signalTiming.setLastUpdate(time);
  }

  INT_32 Node::getSignalsUpdate() const {
	return m_signals.lastUpdate();
  }
  INT_32 Node::getShortcutToTurnUpdate() const {
	return m_shortcutToTurn.lastUpdate();
  }
  INT_32 Node::getPocketToTurnAcrossUpdate() const {
	return m_pocketToTurnAcross.lastUpdate();
  }
  INT_32 Node::getSignalTimingUpdate() const {
	return m_signalTiming.lastUpdate();
  }

  RiverNode::RiverNode() {
  }

  RiverNode::~RiverNode() {
  }

  TypeId RiverNode::type() const {
	return TYPE_RIVER_NODE;
  }
}
