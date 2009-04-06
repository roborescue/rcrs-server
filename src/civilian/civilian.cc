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
*/

#include "civilian.h"
#include "handy.h"
#include "error.h"

using namespace Librescue;

namespace Rescue {
  CivilianAgent::CivilianAgent(Container& container) : Agent(container) {}

  CivilianAgent::~CivilianAgent() {}

  void CivilianAgent::sense(INT_32 time, const IdSet& changed) {
	Agent::sense(time,changed);
	//	LOG_DEBUG("CivilianAgent::sense. %d objects have changed.",sense.getObjects().size());
	// Are we buried or hurt?
	RescueObject* object = me();
	if (!object) {
	  LOG_DEBUG("I don't know who I am!");
	  return;
	}
	//	LOG_DEBUG("Object = %p, type=%d",object,object->type());
	Civilian* civ = dynamic_cast<Civilian*>(object);
	//Humanoid* civ = dynamic_cast<Humanoid*>(object);
	if (!civ) {
	  return;
	  LOG_DEBUG("I'm not a civilian");
	}
	if (civ->getBuriedness()>0) {
	  // Call for help
	  LOG_INFO("I'm trapped");
	  if (randomBoolean(0.1)) {
		LOG_INFO("Calling for help");
		sendSay(stringToBytes("Help!"));
	  }
	}
	if (civ->getDamage()>0) {
	  LOG_INFO("I'm hurt");
	  if (randomBoolean(0.1)) {
		LOG_INFO("Shouting in pain");
		sendSay(stringToBytes("Ouch!"));
	  }
	}
	if (civ->getBuriedness()>0 || civ->getDamage()>0) return; // Do nothing if we're buried or hurt
	// Are we at the refuge?
	RescueObject* location = getLocation();
	if (location && location->type()==TYPE_REFUGE) {
	  LOG_INFO("I'm safe");
	  return;
	}
	// Do we have a path to the refuge?
	if (m_path.empty()) {
	  planPathToRefuge(location);
	}
	else {
	  repairPlan(location);
	}
	// We have a plan. Execute it
	sendMove(m_path);
  }

  AgentType CivilianAgent::getAgentType() const {
	return AGENT_TYPE_CIVILIAN;
	//	return AGENT_TYPE_CIVILIAN | AGENT_TYPE_FIRE_BRIGADE | AGENT_TYPE_FIRE_STATION | AGENT_TYPE_POLICE_FORCE | AGENT_TYPE_POLICE_OFFICE | AGENT_TYPE_AMBULANCE_TEAM | AGENT_TYPE_AMBULANCE_CENTER;
  }

  void CivilianAgent::planPathToRefuge(RescueObject* start) {
	//	LOG_DEBUG("Planning path to refuge from %d",start->id());
	m_path.clear();
	// Do a breadth-first search until we hit a refuge
	std::deque<RescueObject*> open;
	std::map<Id,Id> ancestors;
	std::set<RescueObject*> closed;
	open.push_back(start);
	RescueObject* next;
	ObjectSet neighbours;
	do {
	  next = open.front();
	  open.pop_front();
	  closed.insert(next);
	  neighbours = m_pool.getNeighbours(next);
	  for (ObjectSet::iterator it = neighbours.begin();it!=neighbours.end();++it) {
		RescueObject* neighbour = *it;
		switch (neighbour->type()) {
		case TYPE_ROAD:
		case TYPE_NODE:
		case TYPE_REFUGE:
		  // Work around the buggy traffic simulator by not entering buildings other than refuges
		  //		case TYPE_BUILDING:
		  //		case TYPE_AMBULANCE_CENTER:
		  //		case TYPE_FIRE_STATION:
		  //		case TYPE_POLICE_OFFICE:
		  if (closed.find(neighbour)==closed.end()) {
			open.push_back(neighbour);
			ancestors[neighbour->id()] = next->id();
			//			LOG_DEBUG("Ancestor of %d is %d",neighbour->id(),next->id());
		  }
		  break;
		default:
		  // Don't enter anything else
		  break;
		}
	  }
	} while (next->type()!=TYPE_REFUGE && !open.empty());
	if (next->type()==TYPE_REFUGE) {
	  // Walk back through the ancestor list
	  IdList temp;
	  Id id = next->id();
	  //	  LOG_DEBUG("Reverse path:");
	  do {
		//		LOG_DEBUG("\t%d",id);
		temp.push_back(id);
		id = ancestors[id];
	  } while (id!=start->id());
	  //	  LOG_DEBUG("\t%d",id);
	  temp.push_back(id);
	  m_path.insert(m_path.begin(),temp.rbegin(),temp.rend());
	}
	else {
	  // Planning failed
	  LOG_INFO("Could not find a path to any refuge!");
	}
  }

  void CivilianAgent::repairPlan(RescueObject* location) {
	IdList::iterator it = m_path.begin();
	Id target = location->id();
	//	LOG_INFO("Repairing plan. I'm currently at %d",target);
	//	LOG_INFO("Current path:");
	//	for (IdList::iterator ix = m_path.begin();ix!=m_path.end();++ix) {
	//	  LOG_INFO("\t%d",*ix);
	//	}
	while (it!=m_path.end()) {
	  Id next = *it;
	  if (next==target) break;
	  ++it;
	}
	if (it==m_path.end()) {
	  // Couldn't find our current location on the path. Plan a new one
	  LOG_INFO("I'm lost! Replanning path");
	  planPathToRefuge(location);
	}
	else if (it!=m_path.begin()) {
	  //	  LOG_DEBUG("Repairing path. Old length: %d",m_path.size());
	  m_path.erase(m_path.begin(),it);
	  //	  LOG_DEBUG("New length: %d",m_path.size());	  
	}
  }
}
