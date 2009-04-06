/*
 * Copyright (c) 2006, The Black Sheep, Department of Computer Science, The University of Auckland
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
*/

#include "perception.h"
#include "error.h"
#include "handy.h"
#include "args.h"
#include <sys/time.h>

namespace Rescue {
  AgentPerception::AgentPerception() {}

  AgentPerception::~AgentPerception() {}

  void AgentPerception::newTimestep(int time) {}

  OrdinaryAgentPerception::OrdinaryAgentPerception(Config& config, std::map<Id,int>& ignition) : m_ignitionTimes(ignition) {
	m_range = config.getInt("vision",30000);
	m_speed = config.getInt("fire_cognition_spreading_speed",10000);
  }

  OrdinaryAgentPerception::~OrdinaryAgentPerception() {
  }

  //  void OrdinaryAgentPerception::newTimestep(int time) {
  //  }

  void OrdinaryAgentPerception::getVisibleProperties(const RescueObject* agent, const RescueObject* object, int time, const ObjectPool& allObjects, PropertySet& result) {
    //	Id agentId = agent->id();
//	int x;
//	int y;
//	if (allObjects.locate(agent,&x,&y)) {
	
          // Is this object within the visible radius of the agent?
	  int range = allObjects.range(agent,object);
	  if (range <= m_range) {
		// Update new observation times
		//		m_observations[agentId][object->id()] = time;
		// Add all visible properties
		switch (object->type()) {
		case TYPE_ROAD:
		  result.insert(PROPERTY_BLOCK);
		  result.insert(PROPERTY_REPAIR_COST);
		  break;
		case TYPE_NODE:
		  break;
		case TYPE_REFUGE:
		case TYPE_FIRE_STATION:
		case TYPE_AMBULANCE_CENTER:
		case TYPE_POLICE_OFFICE:
		case TYPE_BUILDING:
		  result.insert(PROPERTY_FIERYNESS);
		  result.insert(PROPERTY_BROKENNESS);
		  result.insert(PROPERTY_BUILDING_TEMPERATURE);
		  break;
		case TYPE_FIRE_BRIGADE:
		  result.insert(PROPERTY_WATER_QUANTITY);
		case TYPE_POLICE_FORCE:
		case TYPE_AMBULANCE_TEAM:
		case TYPE_CIVILIAN:
		  result.insert(PROPERTY_POSITION);
		  result.insert(PROPERTY_POSITION_EXTRA);
		  result.insert(PROPERTY_POSITION_HISTORY);
		  result.insert(PROPERTY_STAMINA);
		  result.insert(PROPERTY_HP);
		  result.insert(PROPERTY_DAMAGE);
		  result.insert(PROPERTY_BURIEDNESS);
		  break;
		case TYPE_WORLD:
		case TYPE_RIVER:
		case TYPE_RIVER_NODE:
		case TYPE_CAR:
		case TYPE_NULL:
		  break;
		}
	  }
      //  }
}
  void OrdinaryAgentPerception::getFarBuildings(const RescueObject* agent, const RescueObject* object, int time, const ObjectPool& allObjects, PropertySet& result) {

	        int range = allObjects.range(agent,object);
		const Building* b = dynamic_cast<const Building*>(object);
		if (b) {
		  // Is this a visible far building?
		  int ignitionTime = m_ignitionTimes[b->id()];
		  if (ignitionTime>0) {
			int farRange = (time-ignitionTime)*m_speed;
			if (range <= farRange) {
			  result.insert(PROPERTY_FIERYNESS);
			}
		  }
		}
	  
  }

  /*
  int OrdinaryAgentPerception::getLastObservationTime(Id agent, Id object) const {
	return m_lastObservations[agent][object];
  }
  ObservationTimePropertyFilter::ObservationTimePropertyFilter(const OrdinaryAgentPerception* perception, Id agent) : m_perception(perception), m_agentId(agent) {}

  ObservationTimePropertyFilter::~ObservationTimePropertyFilter() {}

  bool ObservationTimePropertyFilter::allowed(const RescueObject* object, const Property* property) const {
	// We should write a property if it was updated after the last observation
	int lastObserved = m_perception->getLastObservationTime(m_agentId,object->id());
	int lastUpdate = property->lastUpdate();
	return lastUpdate>=lastObserved;
  }
  */

  AgentPropertyFilter::AgentPropertyFilter() {
  }

  AgentPropertyFilter::~AgentPropertyFilter() {
  }

  bool AgentPropertyFilter::allowed(const RescueObject* object, const Property* property) const {
	//	LOG_DEBUG("Can we write object %d property %s?",object->id(),propertyName(property->type()).c_str());
	if (m_visibleProperties.find(object->id())==m_visibleProperties.end()) {
	  //	  LOG_DEBUG("No. Object is not visible.");
	  return false;
	}
	PropertySet visible = (*m_visibleProperties.find(object->id())).second;
	if (visible.find(property->type())!=visible.end()) {
	  //	  LOG_DEBUG("Yes.");
	  return true;
	}
	//	LOG_DEBUG("No. Object is visible but this property is not.");
	return false;

	/*
	if (m_farBuildings.find(object->id())!=m_farBuildings.end()) return property->type()==PROPERTY_FIERYNESS;
	switch (property->type()) {
	case PROPERTY_ROAD_KIND:
	case PROPERTY_CARS_PASS_TO_HEAD:
	case PROPERTY_CARS_PASS_TO_TAIL:
	case PROPERTY_HUMANS_PASS_TO_HEAD:
	case PROPERTY_HUMANS_PASS_TO_TAIL:
	case PROPERTY_MEDIAN_STRIP:
	case PROPERTY_WIDTH_FOR_WALKERS:
	case PROPERTY_SIGNAL:
	case PROPERTY_SHORTCUT_TO_TURN:
	case PROPERTY_POCKET_TO_TURN_ACROSS:
	case PROPERTY_SIGNAL_TIMING:
	case PROPERTY_STAMINA:
	  return false;
	default:
	  break;
	}
	return true;
	*/
  }

  /*
  void AgentPropertyFilter::clearFarBuildings() {
	m_farBuildings.clear();
  }

  void AgentPropertyFilter::addFarBuilding(Id id) {
	m_farBuildings.insert(id);
  }
  */

  void AgentPropertyFilter::clear() {
	m_visibleProperties.clear();
  }

  void AgentPropertyFilter::add(Id objectId, PropertyId property) {
	m_visibleProperties[objectId].insert(property);
  }

  void AgentPropertyFilter::rewrite(const RescueObject* object, const IntProperty* property, INT_32* value) const {
	switch (property->type()) {
	case PROPERTY_DAMAGE:
	  *value = round(*value,10);
	  break;
	case PROPERTY_HP:
	  *value = round(*value,1000);
	  break;
	case PROPERTY_BUILDING_TEMPERATURE:
	  *value = round(*value,50);
	  break;
	default:
	  break;
	}
  }
}
