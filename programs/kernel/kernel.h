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

 Cameron Skinner / November 2005: Complete rewrite of kernel code.
 Mohammad Mehdi Saboorian / Channel based communicatio
*/

#ifndef RESCUE_KERNEL_H
#define RESCUE_KERNEL_H

#include "objectpool.h"
#include "connection_manager.h"
#include "input.h"
#include "output.h"
#include "config.h"
#include "common.h"
#include "command.h"
#include "connection.h"
#include "objects.h"
#include "perception.h"
#include <stdio.h>

using namespace Librescue;

namespace Rescue {
  enum KernelState {
	INITIALISING,
	CONNECTING_TO_GIS,
	GIS_CONNECTED,
	WAITING_FOR_CONNECTIONS,
	SENDING_SENSE,
	WAITING_FOR_AGENT_COMMANDS,
	SENDING_COMMANDS,
	WAITING_FOR_UPDATES,
	SENDING_UPDATES,
	WRITING_LOG,
	FINISHED
  };

  enum KernelError {
	NONE,
	GIS_TIMEOUT,
	GIS_ERROR,
	GIS_SEND_FAILED
  };

  class Kernel {
  private:
	// No copying
	Kernel(const Kernel& rhs);
	Kernel& operator=(const Kernel& rhs);

	typedef struct {
	  Address address;
	  Id tempId;
	  Id id;
	} AgentInfo;

	typedef struct {
	  Address address;
	  Id id;
	} SimulatorInfo;

	typedef struct {
	  Address address;
	} ViewerInfo;

	typedef struct {
	  std::vector<Civilian*> civ;
	  std::vector<FireBrigade*> fire;
	  std::vector<PoliceForce*> police;
	  std::vector<AmbulanceTeam*> ambulance;
	  std::vector<FireStation*> fireStation;
	  std::vector<PoliceOffice*> policeOffice;
	  std::vector<AmbulanceCenter*> ambulanceCenter;

	  bool empty();
	  RescueObject* getAgent(AgentType type);
	} AgentCount;

	typedef std::vector<AgentInfo> AgentInfos;
	typedef std::vector<SimulatorInfo> SimulatorInfos;
	typedef std::vector<ViewerInfo> ViewerInfos;

	typedef struct {
	  AgentCount pendingAgents; // Agents that have not connected yet
	  AgentInfos waitingAgents; // Agents that have connected but not acknowledged yet
	  SimulatorInfos waitingSimulators;
	  ViewerInfos waitingViewers;

	  bool connected() {
		return pendingAgents.empty() && waitingAgents.empty() && waitingSimulators.empty() && waitingViewers.empty();
	  }
	} ConnectionState;

	typedef struct {
	  RescueObject* object;
	  Address address;
	} Agent;

	typedef struct {
	  Id id;
	  Address address;
	} Simulator;

	typedef struct {
	  Address address;
	} Viewer;
	
	typedef INT_32 ChannelId;
	typedef INT_32 ChannelType;
	typedef std::map<Id, INT_32> ChannelRegistration;
	typedef std::map<Id, Bytes> ChannelRequestMap;
	typedef std::map<Id, INT_32> MessageCountMap;

	typedef struct
	{
		ChannelRegistration regs;
		VoiceCommandList commands;
		ChannelType type;
	} ChannelInfo;
	

	typedef std::vector<Agent> Agents;
	typedef std::vector<Simulator> Simulators;
	typedef std::vector<Viewer> Viewers;
	
	typedef std::map<Id,AgentCommand*> AgentCommandMap;
	typedef std::map<ChannelId, ChannelInfo*> ChannelMap;
	
	FILE* m_log;
	ObjectPool m_pool;
	ConnectionManager m_connection;
	Config m_config;
	Address m_gis;
	Id m_nextId;
	Agents m_agents;
	Simulators m_simulators;
	Viewers m_viewers;
	AgentCommandList m_agentCommands;
	VoiceCommandList m_voiceCommands;
	CommandQueue m_pendingCommands;
	ChannelMap m_channels;
	ChannelRequestMap m_channelRequests;
	MessageCountMap toldCount;

	KernelState m_kernelState;
	KernelError m_kernelError;
	ObjectSet m_objectsForAgents;
	ConnectionState m_connectionState;
	INT_32 m_time;
	INT_32 fireCount;
	INT_32 ambulanceCount;
	INT_32 policeCount;
	INT_32 channelCount;

	AgentPerception* m_perception;

	std::map<Id,int> m_buildingIgnitionTime;

	void preGIS();
	void connectToGIS();
	void postGIS();
	void preConnect();
	void waitForConnections();
	void postConnect();
	void step();
	void endOfSimulation();

	// Wait for a command within some timeout
	Command* receiveCommand(Address* from, long long timeout);
	// Wait for a command or a particular time
	Command* receiveUntil(Address* from, struct timeval* end);

	void handleCommand(Command* command, Address& from);
	void handleSimulatorConnect(SimulatorConnect* command, Address& from);
	void handleSimulatorAcknowledge(SimulatorAcknowledge* command, Address& from);
	void handleViewerConnect(ViewerConnect* command, Address& from);
	void handleViewerAcknowledge(ViewerAcknowledge* command, Address& from);
	void handleKernelUpdate(KernelUpdate* command, Address& from);
	void handleAgentConnect(AgentConnect* command, Address& from);
	void handleAgentAcknowledge(AgentAcknowledge* command, Address& from);
	void handleAgentCommand(AgentCommand* command, Address& from);
	void handleVoiceCommand(VoiceCommand* command, Address& from);
	void handleGISConnectOK(GISConnectOK* command);
	void handleGISConnectError(GISConnectError* command);
	void handleChannelCommand (ChannelCommand* command, Address& from);

	void startOfStep();
	void sendSense();
	void getAgentCommands();
	void sendCommandsToSimulatorsAndViewers();
	void getSimulatorUpdates();
	void sendUpdatesToSimulatorsAndViewers();
	void endOfStep();

	void calculateVisibleSet(const RescueObject* agent, ObjectSet& result, AgentPropertyFilter& filter);
	bool canHear(const Agent* agent, const VoiceCommand* command);
	bool isAlive(const RescueObject* o);
	void sendHear(const Agent* agent, const VoiceCommand* command);
	ChannelId getGlobalChannelId (const RescueObject* agent, ChannelId localChannelId);

	Id generateId();

  public:
	Kernel();
	~Kernel();

        int mmaxX,mmaxY,mminX,mminY; //Map Boundaries
	void init(int argc, char** argv);
	void run();
  };
}

#endif
