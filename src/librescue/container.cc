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

#include "simulator.h"
#include "connection_manager.h"
#include "connection.h"
#include "container.h"
#include "error.h"
#include <iostream>
#include <stdlib.h>

namespace Librescue {
  Container::Container(int argc, char** argv) {
	m_host = "localhost";
	m_port = 7000;
	m_nextRequestId = 1;

	m_args = generateArgList(argc,argv);
	m_config.init(m_args);

	// Process the arguments
	ArgList::iterator it = m_args.begin();
	while (it!=m_args.end()) {
	  ArgList::iterator temp = it;
	  const char* next = (*it).c_str();
	  if (strcmp(next,"-h")==0 || strcmp(next,"--host")==0) {
		++it;
		m_host = *it;
		++it;
		m_args.erase(temp,it);
	  }
	  else if (strcmp(next,"-p")==0 || strcmp(next,"--port")==0) {
		++it;
		m_port = atoi((*it).c_str());
		++it;
		m_args.erase(temp,it);
	  }
	  else {
		++it;
	  }
	}

	// Set up the connection manager
	m_kernelAddress = Address(m_host.c_str(),m_port);
	m_connection.sendViaTCP(m_kernelAddress);
	m_connection.start();
  }

  Container::~Container() {	
  }

  void Container::printUsage() {
	std::cerr << "Options" << std::endl;
	std::cerr << "=======" << std::endl;
	std::cerr << "-h\t--host\tThe host to connect to (default localhost)" << std::endl;
	std::cerr << "-p\t--port\tThe port to connect to (default 7000)" << std::endl;
	std::cerr << "=======" << std::endl;
	
  }

  bool Container::addSimulator(Simulator* simulator) {
    if (connectSimulator(simulator, m_nextRequestId++)) {
	  m_simulators.push_back(simulator);
	  simulator->init(&m_config,m_args);
	  return true;
	}
	return false;
  }

  bool Container::addAgent(Agent* agent) {
	if (connectAgent(agent,m_nextRequestId++)) {
	  m_agents.push_back(agent);
	  agent->init(&m_config,m_args);
	  return true;
	}
	return false;
  }

  void Container::run() {
	LOG_DEBUG("Container::run");
	Bytes data;
	CommandList commands;
	Address from;
	// Receive data and send responses until the simulation is finished or we don't receive anything for 10 minutes
	while (m_connection.receive(data,&from,600000)) {
	  InputBuffer input(data);
	  decodeCommands(commands,input);
	  for (CommandList::iterator it = commands.begin();it!=commands.end();++it) {
		Command* next = *it;
		switch (next->getType()) {
		case COMMANDS:
		  handleCommands(dynamic_cast<Commands*>(next));
		  break;
		case UPDATE:
		  handleUpdate(dynamic_cast<Update*>(next));
		  break;
		case KA_SENSE:
		  handleAgentSense(dynamic_cast<AgentSense*>(next));
		  break;
		case KA_HEAR:
		  handleAgentHear(dynamic_cast<AgentHear*>(next));
		  break;
		default:
		  break;
		}
		delete next; // decodeCommands returns new objects
	  }
	  commands.clear();
	}
	// Clean up
	LOG_DEBUG("No more data available");
	m_connection.stop();
	for (Simulators::iterator it = m_simulators.begin();it!=m_simulators.end();++it) {
	  Simulator* sim = *it;
	  sim->cleanup();
	}
	m_simulators.clear();
	for (Agents::iterator it = m_agents.begin();it!=m_agents.end();++it) {
	  Agent* agent = *it;
	  agent->cleanup();
	}
	m_agents.clear();
	LOG_DEBUG("Finished Container::run");
  }

  void Container::handleCommands(Commands* commands) {
	ObjectSet changed;
	OutputBuffer output;
	// Only send changed properties
	TimePropertyFilter filter(commands->getTime());
	output.setPropertyFilter(&filter);
	// All simulators get the same commands
	changed.clear();
	for (Simulators::iterator it = m_simulators.begin();it!=m_simulators.end();++it) {
	  Simulator* sim = *it;
	  if (!sim->step(commands->getTime(),commands->getCommands(),changed)) {
		KernelUpdate update(m_simulatorToId[sim],commands->getTime(),changed);
		output.writeCommand(&update);
		//		dumpBytes(output.buffer());
	  }
	  else {
		LOG_ERROR("Simulator returned non-zero from step(...)");
	  }
	}
	// Send the update
	output.writeInt32(HEADER_NULL);
	m_connection.send(output.buffer(),m_kernelAddress);
  }

  void Container::handleUpdate(Update* update) {
	for (Simulators::iterator it = m_simulators.begin();it!=m_simulators.end();++it) {
	  Simulator* sim = *it;
	  sim->update(update->getTime(),update->getObjects());
	}
  }

  void Container::handleAgentSense(AgentSense* sense) {
	//	LOG_DEBUG("Handling KA_SENSE");
	Agent* agent = m_idToAgent[sense->getId()];
	if (agent) {
	  IdSet changed;
	  for (ObjectSet::iterator it = sense->getObjects().begin();it!=sense->getObjects().end();++it) changed.insert((*it)->id());
	  agent->update(sense->getTime(),sense->getObjects());
	  agent->sense(sense->getTime(),changed);
	}
  }

  void Container::handleAgentHear(AgentHear* hear) {
	Agent* agent = m_idToAgent[hear->getTo()];
	if (agent) {
	  agent->hear(hear->getType(),hear->getFrom(),hear->getData());
	}
  }

  bool Container::connectSimulator(Simulator* simulator, Id requestId) {
	OutputBuffer out;
	// Send an SK_CONNECT
	SimulatorConnect connect(requestId, 0);
	out.writeCommand(&connect);
	out.writeInt32(HEADER_NULL);
	if (m_connection.send(out.buffer(),m_kernelAddress)) {
	  // Wait for a response
	  Bytes in;
	  Address from;
	  CommandList commands;
	  bool result = false;
	  bool haveResult = false;
	  while (!haveResult) {
		if (m_connection.receive(in,&from,60000)) {
		  InputBuffer buffer(in);
		  decodeCommands(commands,buffer);
		  for (CommandList::iterator it = commands.begin();it != commands.end();++it) {
			Command* next = *it;
			switch (next->getType()) {
			case KS_CONNECT_OK:
			  handleSimulatorConnectOK(simulator,dynamic_cast<SimulatorConnectOK*>(next));
			  result = true;
			  haveResult = true;
			  break;
			case KS_CONNECT_ERROR:
			  handleSimulatorConnectError(dynamic_cast<SimulatorConnectError*>(next));
			  result = false;
			  haveResult = true;
			  break;
			default:
			  // Ignore everything else
			  break;
			}
			delete next;
		  }
		  commands.clear();
		}
		else {
		  LOG_WARNING("Timeout connecting simulator");
		  result = false;
		  haveResult = true;
		}
	  }
	  return result;
	}
	else {
	  LOG_WARNING("WARNING: Error sending connect");
	  return false;
	}
  }

  void Container::handleSimulatorConnectOK(Simulator* sim, SimulatorConnectOK* ok) {
    Id requestId = ok->getRequestId();
    Id simId = ok->getSimulatorId();
    sim->update(0,ok->getObjects());
    m_simulatorToId[sim] = simId;
    // Send an acknowledgement
    LOG_DEBUG("Acknowledging connection for simulator %d (request ID %d)", simId, requestId);
    SimulatorAcknowledge ack(requestId, simId);
    OutputBuffer out;
    out.writeCommand(&ack);
    out.writeInt32(HEADER_NULL);
    if (!m_connection.send(out.buffer(),m_kernelAddress)) {
      LOG_WARNING("WARNING: Error sending simulator acknowledge");
    }
  }

  void Container::handleSimulatorConnectError(SimulatorConnectError* error) {
	LOG_WARNING("Error connecting simulator: %s",error->getReason().c_str());
  }

  bool Container::connectAgent(Agent* agent, Id requestId) {
	OutputBuffer out;
	// Send an AK_CONNECT
	AgentConnect connect(requestId,0,agent->getAgentType());
	out.writeCommand(&connect);
	out.writeInt32(HEADER_NULL);
	if (m_connection.send(out.buffer(),m_kernelAddress)) {
	  LOG_DEBUG("Sent AK_CONNECT");
	  // Wait for a response
	  Bytes in;
	  Address from;
	  CommandList commands;
	  bool result = false;
	  bool haveResult = false;
	  while (!haveResult) {
		if (m_connection.receive(in,&from,60000)) {
		  InputBuffer buffer(in);
		  decodeCommands(commands,buffer);
		  for (CommandList::iterator it = commands.begin();it != commands.end();++it) {
			Command* next = *it;
			// What kind of response was it?
			switch (next->getType()) {
			case KA_CONNECT_OK:
			  LOG_DEBUG("Received KA_CONNECT_OK");
			  handleAgentConnectOK(agent,dynamic_cast<AgentConnectOK*>(next));
			  result = true;
			  haveResult = true;
			  break;
			case KA_CONNECT_ERROR:
			  LOG_DEBUG("Received KA_CONNECT_ERROR");
			  handleAgentConnectError(dynamic_cast<AgentConnectError*>(next));
			  result = false;
			  haveResult = true;
			  break;
			default:
			  // Ignore everything else
			  break;
			}
			delete next;
		  }
		  commands.clear();
		}
		else {
		  LOG_WARNING("Timeout connecting agent");
		  result = false;
		  haveResult = true;
		}
	  }
	  return result;
	}
	else {
	  LOG_WARNING("WARNING: Error sending connect");
	  return false;
	}
  }

  void Container::handleAgentConnectOK(Agent* agent, AgentConnectOK* ok) {
    Id requestId = ok->getRequestId();
    Id agentId = ok->getAgentId();
    agent->connected(*ok);
    m_idToAgent[agentId] = agent;
    // Send an acknowledgement
    AgentAcknowledge ack(requestId, agentId);
    OutputBuffer out;
    out.writeCommand(&ack);
    out.writeInt32(HEADER_NULL);
    LOG_DEBUG("Sending AK_ACKNOWLEDGE for request id %d (agent %d)",requestId, agentId);
    if (!m_connection.send(out.buffer(),m_kernelAddress)) {
      LOG_WARNING("WARNING: Error sending agent acknowledge");
    }
  }

  void Container::handleAgentConnectError(AgentConnectError* error) {
	LOG_WARNING("Error connecting agent: %s",error->getReason().c_str());
  }

  void Container::sendAgentCommand(const AgentCommand* command) {
	OutputBuffer out;
	out.writeCommand(command);
	out.writeInt32(HEADER_NULL);
	if (!m_connection.send(out.buffer(),m_kernelAddress)) {
	  LOG_WARNING("WARNING: Error sending agent command");
	}
  }
}
