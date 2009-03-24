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

/*
*/

#ifndef MISC_SIMULATOR_H
#define MISC_SIMULATOR_H

#include "config.h"
#include "objectpool.h"
#include "objects.h"
#include "simulator.h"
#include "command.h"
#include "args.h"
#include <set>
#include <map>

using namespace Librescue;

namespace MiscSimulator {
  class MiscSimulator : public Simulator {
  private:
	// No copying allowed
	MiscSimulator(const MiscSimulator& source);
	MiscSimulator& operator= (const MiscSimulator& rhs);

	typedef std::set<Humanoid*> HumanoidSet;
	//	typedef std::map<Id,HumanoidSet> BuildingOccupants;
	typedef std::map<Id,INT_32> BrokennessMap;

	IdSet m_collapsed; // Buildings that have collapsed this timestep
	BrokennessMap m_lastBrokenness; // The most recent brokenness for each building

	// Update a humanoids buriedness, damage, hp
	void updateHumanoid(Humanoid* h, INT_32 time, ObjectSet& changed);

	// How much extra buriedness does this humanoid get given that the building has just become more broken?
	int findCollapseBuriedness(Humanoid* h, Building* b);
	// How much extra buriedness does this humanoid get given that it has just entered a damaged building?
	int findEntryBuriedness(Humanoid* h, Building* b);

	// How much damage does a humanoid take given that the building has just become more broken?
	int findCollapseDamage(Humanoid* h, Building* b, int extraBuriedness);
	// How much damage does a humanoid take given that it just got trapped by entering a broken building?
	//	int findEntryDamage(Humanoid* h, Building* b, int extraBuriedness);
	// How much extra damage does a humanoid take given that it has become more buried?
	int findBuriedDamage(Humanoid* h, Building* b, int extraBuriedness);
	// How much extra damage does a humanoid take given that it is in a burning building?
	int findBurningDamage(Humanoid* h, Building* b);

	void applyNaturalDamage(Humanoid* h, INT_32 time, ObjectSet& changed);

	void applyCommands(const AgentCommandList& commands, INT_32 time, ObjectSet& changed);

	void handleClear(const ClearCommand* command, INT_32 time, ObjectSet& changed);
	void handleRescue(const RescueCommand* command, INT_32 time, ObjectSet& changed);
	void handleRepair(const RepairCommand* command, INT_32 time, ObjectSet& changed);

  public:
	MiscSimulator();
	virtual ~MiscSimulator();

	virtual int init(Config* config, ArgList& args);
	virtual void update(INT_32 time, const ObjectSet& changed);
	virtual int step(INT_32 time, const AgentCommandList& commands, ObjectSet& changed);

  };
}
#endif
