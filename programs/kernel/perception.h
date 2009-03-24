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

#ifndef RESCUE_PERCEPTION_H
#define RESCUE_PERCEPTION_H

#include "objectpool.h"
#include "config.h"
#include "common.h"
#include "objects.h"
#include "property_filter.h"
#include <stdio.h>
#include <map>

using namespace Librescue;

namespace Rescue {
  class AgentPerception {
  private:
	// No copying
	AgentPerception(const AgentPerception& rhs);
	AgentPerception& operator=(const AgentPerception& rhs);
	
  public:
	AgentPerception();
	int m_range; // The range of vision
	int m_speed; // The speed of far building vision
	virtual ~AgentPerception();

	// Find out which properties of an object are visible by a particular agent.
	// Agent: The agent that is observing the world.
	// Object: The object to test for visibility.
	// Time: The current timestep.
	// AllObjects: All objects in the world.
	// Result: A PropertySet that should be filled with the properties of Object that are visible by Agent.
	virtual void getVisibleProperties(const RescueObject* agent, const RescueObject* object, int time, const ObjectPool& allObjects, PropertySet& result) = 0;
	virtual void getFarBuildings(const RescueObject* agent, const RescueObject* object, int time, const ObjectPool& allObjects, PropertySet& result) = 0;

	// Notification that a new timestep has started.
	virtual void newTimestep(int time);
  };

  class OrdinaryAgentPerception : public AgentPerception {
  private:
	// No copying
	OrdinaryAgentPerception(const OrdinaryAgentPerception& rhs);
	OrdinaryAgentPerception& operator=(const OrdinaryAgentPerception& rhs);


	std::map<Id,int>& m_ignitionTimes;

  public:
	OrdinaryAgentPerception(Config& config, std::map<Id,int>& ignitionTimes);
	virtual ~OrdinaryAgentPerception();

	virtual void getVisibleProperties(const RescueObject* agent, const RescueObject* object, int time, const ObjectPool& allObjects, PropertySet& result);
	virtual void getFarBuildings(const RescueObject* agent, const RescueObject* object, int time, const ObjectPool& allObjects, PropertySet& result) ;
	//	virtual void newTimestep(int time);
  };

  // This class will round the PROPERTY_HP and PROPERTY_DAMAGE properties for all humanoids.
  class AgentPropertyFilter : public PropertyFilter {
  private:
	std::map<Id,PropertySet> m_visibleProperties;

  public:
	AgentPropertyFilter();
	virtual ~AgentPropertyFilter();

	bool allowed(const RescueObject* object, const Property* property) const;

	void clear();
	void add(Id objectId, PropertyId property);

	virtual void rewrite(const RescueObject* object, const IntProperty* prop, INT_32* value) const;
  };
}

#endif
