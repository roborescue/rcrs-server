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

#ifndef LIBRESCUE_CONTAINER_H
#define LIBRESCUE_CONTAINER_H

#include <vector>
#include <map>
#include "simulator.h"
#include "agent.h"
#include "connection_manager.h"
#include "args.h"

namespace Librescue {
  typedef std::vector<Simulator*> Simulators;
  typedef std::map<Simulator*,Id> SimulatorToId;

  typedef std::vector<Agent*> Agents;
  typedef std::map<Id,Agent*> IdToAgent;

  class Container {
  private:
	Simulators m_simulators;
	SimulatorToId m_simulatorToId;

	Agents m_agents;
	IdToAgent m_idToAgent;

	Id m_nextRequestId;

	ConnectionManager m_connection;
	Config m_config;

	std::string m_host;
	int m_port;
	Address m_kernelAddress;

	ArgList m_args;

	bool connectSimulator(Simulator* simulator, Id requestId);
	void handleCommands(Commands* commands);
	void handleUpdate(Update* update);
	void handleSimulatorConnectOK(Simulator* sim, SimulatorConnectOK* ok);
	void handleSimulatorConnectError(SimulatorConnectError* ok);

	bool connectAgent(Agent* agent, Id requestId);
	void handleAgentSense(AgentSense* sense);
	void handleAgentHear(AgentHear* hear);
	void handleAgentConnectOK(Agent* agent, AgentConnectOK* ok);
	void handleAgentConnectError(AgentConnectError* ok);

	void printUsage();

  public:
	Container(int argc, char** argv);
	~Container();

	// Returns true iff the connection to the kernel worked
	bool addSimulator(Simulator* simulator);
	bool addAgent(Agent* agent);

	// Run all the agents and simulators until the kernel disappears
	void run();

 	void sendAgentCommand(const AgentCommand* command);
  };
}

#endif
