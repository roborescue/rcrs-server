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

#include "misc.h"
#include "handy.h"
#include "error.h"

namespace MiscSimulator {
  MiscSimulator::MiscSimulator() {}

  MiscSimulator::~MiscSimulator() {}

  int MiscSimulator::init(Config* config, ArgList& args) {
	return Simulator::init(config,args);
  }

  std::string MiscSimulator::getName() const {
    return "Legacy misc simulator";
  }

  void MiscSimulator::update(INT_32 time, const ObjectSet& changed) {
	Simulator::update(time,changed);
	m_collapsed.clear();
	// Build the lists of newly broken buildings and humanoids that have moved
	for (ObjectSet::iterator it = changed.begin();it!=changed.end();++it) {
	  RescueObject* next = m_pool.getObject((*it)->id());
	  Building* b = dynamic_cast<Building*>(next);
	  if (b) {
		if (b->getBrokennessUpdate()==time && m_lastBrokenness[b->id()] < b->getBrokenness()) m_collapsed.insert(b->id());
		m_lastBrokenness[b->id()] = b->getBrokenness();
	  }
	}
  }  

  int MiscSimulator::step(INT_32 time, const AgentCommandList& commands, ObjectSet& changed) {
	for (ObjectSet::iterator it = m_pool.objects().begin();it!=m_pool.objects().end();++it) {
	  RescueObject* next = *it;
	  Humanoid* h = dynamic_cast<Humanoid*>(next);
	  if (h) updateHumanoid(h,time,changed);
	}
	// Apply rescue, clear and repair commands
	applyCommands(commands,time,changed);
	// Write world state
	LOG_DEBUG("Hurt and damaged civilians at time %d",time);
	LOG_DEBUG("ID        | HP    | Damage | Buriedness");
	for (ObjectSet::iterator it = m_pool.objects().begin();it!=m_pool.objects().end();++it) {
	  Humanoid* h = dynamic_cast<Humanoid*>(*it);
	  if (h && h->getHP()>0 && (h->getHP()<10000 || h->getDamage()>0)) {
		LOG_DEBUG("%9d | %5d | %5d  | %3d",h->id(),h->getHP(),h->getDamage(),h->getBuriedness());
	  }
	}
	return 0;
  }

  void MiscSimulator::updateHumanoid(Humanoid* h, INT_32 time, ObjectSet& changed) {
	Building* b = dynamic_cast<Building*>(m_pool.getObject(h->getPosition()));
	if (b) {
	  if (m_collapsed.find(b->id())!=m_collapsed.end()) {
		// This building has collapsed
		int buriedness = findCollapseBuriedness(h,b);
		h->setBuriedness(buriedness,time);
		int damage = findCollapseDamage(h,b,buriedness) + findBuriedDamage(h,b,buriedness);
		h->setDamage(h->getDamage()+damage,time);
		changed.insert(h);
	  }
	  // Did this humanoid enter a broken building?
	  if (h->getPositionUpdate()==time && b->getBrokenness()>0) {
		// Check for getting trapped
		int buriedness = findEntryBuriedness(h,b);
		if (buriedness>0) {
		  h->setBuriedness(buriedness,time);
		  int damage = findBuriedDamage(h,b,buriedness);
		  h->setDamage(h->getDamage()+damage,time);
		  changed.insert(h);
		}
	  }
	  // Check for fire damage
	  int damage = findBurningDamage(h,b);
	  if (damage>0) {
		h->setDamage(h->getDamage()+damage,time);
		changed.insert(h);
	  }
	  // Is this humanoid in a non-burning refuge?
	  if (b->type()==TYPE_REFUGE && b->getFieryness()==0 && h->getDamage()>0) {
		h->setDamage(0,time);
		changed.insert(h);
	  }
	}
	// Apply natural damage increase
	applyNaturalDamage(h,time,changed);
	// Apply damage
	int damage = h->getDamage();
	if (damage>0) {
	  int hp = h->getHP() - damage;
	  if (hp<0) hp=0;
	  h->setHP(hp,time);
	  changed.insert(h);
	}
  }

  int MiscSimulator::findCollapseBuriedness(Humanoid* h, Building* b) {
	// The building has just collapsed on the humanoid
	// We'll use the old misc simulator constants for now
	INT_32 brokenness = b->getBrokenness();
	int result = 0;
	if (brokenness > 0) result = 20;
	if (brokenness > 25) result = 30;
	if (brokenness > 50) result = 60;
	return result;
  }

  int MiscSimulator::findEntryBuriedness(Humanoid* h, Building* b) {
	if (!m_config->getBool("entering_broken_buildings_causes_damage",false)) return 0;
	// Entering a building might cause damage
	if (h->type()==TYPE_POLICE_FORCE) return 0; // Unless you're a policeman
	double chance = b->getBrokenness()/100.0;
	int max = m_config->getInt("entering_broken_building_max_buriedness",30);
	int buriedness = Librescue::randomBoolean(chance)?Librescue::randomInt(1,max):0;
	LOG_DEBUG("Humanoid %d entered building with brokenness %d, got %d buriedness",h->id(),b->getBrokenness(),buriedness);
	return buriedness;
  }

  int MiscSimulator::findCollapseDamage(Humanoid* h, Building* b, int extraBuriedness) {
	// Constants taken from old simulator:
	// 0.4 chance of 3 damage
	// 0.5 chance of 15 damage
	// 0.1 chance of 100 damage
	double d = Librescue::randomDouble();
	if (d < 0.4) return 3;
	if (d < 0.9) return 15;
	return 100;
  }

  /*
  int MiscSimulator::findEntryDamage(Humanoid* h, Building* b, int extraBuriedness) {
	if (!m_config->getBool("entering_broken_buildings_causes_damage",false)) return 0;
	double chance = b->getBrokenness()/100.0;
	return Librescue::binomial(chance,extra);
  }
  */

  int MiscSimulator::findBuriedDamage(Humanoid* h, Building* b, int extraBuriedness) {
	// Constants taken from old simulator for wooden buildings:
	// 0.5 chance of 2 damage
	// 0.3 chance of 10 damage
	double d = Librescue::randomDouble();
	if (d < 0.5) return 2;
	if (d < 0.8) return 10;
	return 0;
  }

  int MiscSimulator::findBurningDamage(Humanoid* h, Building* b) {
	double d;
	switch (b->getFieryness()) {
	case FIERYNESS_LOW:
	case FIERYNESS_MEDIUM:
	case FIERYNESS_HIGH:
	  // Constants taken from old simulator:
	  // 0.4 chance of 5 damage
	  // 0.2 chance of 20 damage
	  // 0.1 chance of 100 damage
	  d = Librescue::randomDouble();
	  if (d < 0.4) return 5;
	  if (d < 0.6) return 20;
	  if (d < 0.7) return 100;
	  return 0;
	default:
	  return 0;
	}
  }

  void MiscSimulator::applyNaturalDamage(Humanoid* h, INT_32 time, ObjectSet& changed) {
	int damage = h->getDamage();
	if (damage>0) {
	  int newDamage = (int)(damage * damage * 0.00025); // Constant taken from old simulator
	  if (newDamage < 1) newDamage = 1;
	  h->setDamage(damage+newDamage,time);
	  changed.insert(h);
	}
  }

  void MiscSimulator::applyCommands(const AgentCommandList& commands, INT_32 time, ObjectSet& changed) {
	for (AgentCommandList::const_iterator it = commands.begin();it!=commands.end();++it) {
	  const AgentCommand* next = *it;
	  switch (next->getType()) {
	  case AK_CLEAR:
		handleClear(dynamic_cast<const ClearCommand*>(next),time,changed);
		break;
	  case AK_RESCUE:
		handleRescue(dynamic_cast<const RescueCommand*>(next),time,changed);
		break;
	  case AK_REPAIR:
		handleRepair(dynamic_cast<const RepairCommand*>(next),time,changed);
		break;
	  default:
		// Ignore
		break;
	  }
	}
  }

  void MiscSimulator::handleClear(const ClearCommand* command, INT_32 time, ObjectSet& changed) {
	RescueObject* r = m_pool.getObject(command->getAgentId());
	if (!r) {
	  LOG_ERROR("Received a clear command from an unknown agent: %d",command->getAgentId());
	  return;
	}
	PoliceForce* police = dynamic_cast<PoliceForce*>(r);
	if (!police) {
	  LOG_INFO("Received a clear command from agent %d who is of type %d",command->getAgentId(),r->type());
	  return;
	}
	if (police->getBuriedness()>0) {
	  LOG_INFO("Received a clear command from buried police agent %d (buriedness=%d)",command->getAgentId(),police->getBuriedness());
	  return;
	}
	r = m_pool.getObject(command->getTarget());
	if (!r) {
	  LOG_INFO("Received a clear command from agent %d for a non-existant road: %d",command->getAgentId(),command->getTarget());
	  return;
	}
	Road* road = dynamic_cast<Road*>(r);
	if (!road) {
	  LOG_INFO("Received a clear command from agent %d for a non-road object: %d is of type %d",command->getAgentId(),command->getTarget(),r->type());
	  return;
	}
	if (road->getBlock()==0) {
	  LOG_INFO("Received a clear command from agent %d to clear non-blocked road %d",command->getAgentId(),road->id());
	  return;
	}
	if (police->getPosition()!=road->id() && police->getPosition()!=road->getHead() && police->getPosition()!=road->getTail()) {
	  LOG_INFO("Received a clear command from agent %d to clear road %d (%d - %d), but the agent is at location %d",command->getAgentId(),road->id(),road->getHead(),road->getTail(),police->getPosition());
	  return;
	}
	int length = road->getLength();
	int oldBlock = road->getBlock();
	int oldCost = road->getRepairCost();
	int totalAreaBlocked = oldBlock*length;
	int rate = m_config->getInt("road_clear_rate",20000000);
	int newAreaBlocked = totalAreaBlocked - rate;
	if (newAreaBlocked < 0) newAreaBlocked = 0;
	int newBlock = newAreaBlocked / length;
	int newCost = (int)((newBlock * length + rate - 1)/rate);
	road->setBlock(newBlock,time);
	road->setRepairCost(newCost,time);
	changed.insert(road);
	LOG_DEBUG("Road %d block: was %d now %d, repair cost: was %d now %d",road->id(),oldBlock,newBlock,oldCost,newCost);
  }

  void MiscSimulator::handleRescue(const RescueCommand* command, INT_32 time, ObjectSet& changed) {
	RescueObject* r = m_pool.getObject(command->getAgentId());
	if (!r) {
	  LOG_ERROR("Received a rescue command from an unknown agent: %d",command->getAgentId());
	  return;
	}
	AmbulanceTeam* agent = dynamic_cast<AmbulanceTeam*>(r);
	if (!agent) {
	  LOG_INFO("Received a rescue command from agent %d who is of type %d",command->getAgentId(),r->type());
	  return;
	}
	if (agent->getBuriedness()>0) {
	  LOG_INFO("Received a rescue command from buried ambulance agent %d (buriedness=%d)",command->getAgentId(),agent->getBuriedness());
	  return;
	}
	r = m_pool.getObject(command->getTarget());
	if (!r) {
	  LOG_INFO("Received a rescue command from agent %d for a non-existant target: %d",command->getAgentId(),command->getTarget());
	  return;
	}
	Humanoid* target = dynamic_cast<Humanoid*>(r);
	if (!target) {
	  LOG_INFO("Received a rescue command from agent %d for a non-humanoid object: %d is of type %d",command->getAgentId(),command->getTarget(),r->type());
	  return;
	}
	if (target->getBuriedness()==0) {
	  LOG_INFO("Received a rescue command from agent %d to rescue non-buried humanoid %d",command->getAgentId(),target->id());
	  return;
	}
	if (agent->getPosition()!=target->getPosition()) {
	  LOG_INFO("Received a rescue command from agent %d to rescue humanoid %d, but they are at different locations: %d and %d",command->getAgentId(),target->id(),agent->getPosition(),target->getPosition());
	}
	target->setBuriedness(target->getBuriedness()-1,time);
	changed.insert(target);
  }

  void MiscSimulator::handleRepair(const RepairCommand* command, INT_32 time, ObjectSet& changed) {
	if (!m_config->getBool("allow_repair")) return;
	RescueObject* r = m_pool.getObject(command->getAgentId());
	if (!r) {
	  LOG_ERROR("Received a repair command from an unknown agent: %d",command->getAgentId());
	  return;
	}
	PoliceForce* agent = dynamic_cast<PoliceForce*>(r);
	if (!agent) {
	  LOG_INFO("Received a repair command from agent %d who is of type %d",command->getAgentId(),r->type());
	  return;
	}
	if (agent->getBuriedness()>0) {
	  LOG_INFO("Received a repair command from buried police agent %d (buriedness=%d)",command->getAgentId(),agent->getBuriedness());
	  return;
	}
	r = m_pool.getObject(command->getTarget());
	if (!r) {
	  LOG_INFO("Received a repair command from agent %d for a non-existant target: %d",command->getAgentId(),command->getTarget());
	  return;
	}
	Building* target = dynamic_cast<Building*>(r);
	if (!target) {
	  LOG_INFO("Received a repair command from agent %d for a non-building object: %d is of type %d",command->getAgentId(),command->getTarget(),r->type());
	  return;
	}
	if (target->getBrokenness()==0) {
	  LOG_INFO("Received a repair command from agent %d to repair non-broken building %d",command->getAgentId(),target->id());
	  return;
	}
	if (agent->getPosition()!=target->id()) {
	  LOG_INFO("Received a repair command from agent %d to repair building %d, but agent is at location %d",command->getAgentId(),target->id(),agent->getPosition());
	}
	int rate = m_config->getInt("building_repair_rate",5);
	target->setBrokenness(Librescue::max(0,target->getBrokenness()-rate),time);
	changed.insert(target);
  }
}
