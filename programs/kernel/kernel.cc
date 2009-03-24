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
 Mohammad Mehdi Saboorian / Channel based communication
 Yasovardhan Reddy  / CalculateVisibleSet Function

*/

#include "kernel.h"
#include "error.h"
#include "handy.h"
#include "args.h"
#include <sys/time.h>

namespace Rescue {
  Kernel::Kernel() {
	m_perception = 0;
  }

  Kernel::~Kernel() {}

  void Kernel::init(int argc, char** argv) {
	std::string host = "localhost";
	ArgList args = generateArgList(argc,argv);
	m_config.init(args);
	fireCount = 0;
	ambulanceCount = 0;
	policeCount = 0;	

	for (ArgList::iterator it = args.begin();it!=args.end();++it) {
	  const char* next = (*it).c_str();
	  if (strcmp(next,"-g")==0 || strcmp(next,"--gis")==0) {
		++it;
		host = *it;
	  }
	}
	
	INT_32 channelSayCount = m_config.getInt("channel_say_count", 1);
	INT_32 channelRadioCount = m_config.getInt("channel_radio_count", 10);
	//Extension point: better configuration management
	//something like this
	//channel_count : 4 
	//ch00 : 1  <-- channel type (CHANNEL_SAY)
	//ch01 : 2
	//ch02 : 2
	//ch02 : 2
	
	channelCount = channelSayCount + channelRadioCount;
	for (INT_32 i=0; i<channelSayCount; i++)
	{
		ChannelInfo* chInfo = new ChannelInfo();
		chInfo->type = CHANNEL_SAY;
		m_channels[i] = chInfo;
	}	
	
	for (INT_32 j=channelSayCount; j<channelCount; j++)
	{
		ChannelInfo* chInfo = new ChannelInfo();
		chInfo->type = CHANNEL_RADIO;
		m_channels[j] = chInfo;
	}	

	m_gis = Address(host.c_str(),m_config.getInt("gis_port",7001));
	m_connection.listenTCP(m_config.getInt("kernel_port",7000));
	int udpPort = m_config.getInt("kernel_udp_port",0);
	if (udpPort > 0) {
	  m_connection.setUDPWait(m_config.getInt("send_udp_wait",1));
	  m_connection.setUDPSize(m_config.getInt("send_udp_size",1472));
	  m_connection.listenUDP(udpPort);
	}
	m_connection.start();
	m_nextId = 1;

	std::string log = m_config.getString("logname");
	m_log = 0;
	if (log.size()!=0) {
	  m_log = fopen(log.c_str(),"w");
	  if (!m_log) {
		LOG_ERROR("Could not open log file %s",log.c_str());
	  }
	  else {
		const char header[] = "RoboCup-Rescue Prototype Log 02";
		fwrite(header, sizeof(header[0]), strlen(header)+1, m_log);
		// Write the config section
		OutputBuffer out;
		m_config.write(out);
		out.log(m_log);
		fflush(m_log);
	  }
	}
	m_buildingIgnitionTime.clear();
	if (m_perception) delete m_perception;
	m_perception = new OrdinaryAgentPerception(m_config,m_buildingIgnitionTime);
  }

  void Kernel::run() {
	m_kernelState = INITIALISING;
	m_kernelError = NONE;
	int maxTime = m_config.getInt("period",1000);
	memset(&m_connectionState.pendingAgents,0,sizeof(AgentCount));
	preGIS();
	connectToGIS();
	if (m_kernelState != GIS_CONNECTED) {
	  // Failed
	  LOG_ERROR("Failed to connect to GIS");
	}
	else {
	  postGIS();
	  preConnect();
	  waitForConnections();
	  postConnect();
	  m_time = 1;
	  while (m_time <= maxTime) {
		LOG_INFO("Timestep %d/%d",m_time,maxTime);
		step();
		++m_time;
	  }
	  endOfSimulation();
	}
	m_connection.stop();
	if (m_log) {
	  fflush(m_log);
	  fclose(m_log);
	}
  }

  void Kernel::preGIS() {
	m_kernelState = CONNECTING_TO_GIS;
	m_connection.sendViaTCP(m_gis);
  }

  void Kernel::connectToGIS() {
	// Connect to the GIS
	OutputBuffer out;
	GISConnect connect(0);
	out.writeCommand(&connect);
	out.writeInt32(HEADER_NULL);
	if (!m_connection.send(out.buffer(),m_gis)) {
	  m_kernelState = FINISHED;
	  m_kernelError = GIS_SEND_FAILED;
	  return;
	}
	// Wait for a response
	Address from;
	struct timeval end;
	gettimeofday(&end,0);
	addTime(&end,5000);
	while (m_kernelState!=GIS_CONNECTED && m_kernelError==NONE) {
	  Command* c = receiveUntil(&from,&end);
	  if (c) {
		handleCommand(c,from);
		delete c;
	  }
	  else {
		// Timeout
		LOG_ERROR("Timeout connecting to GIS");
		m_kernelState = FINISHED;
		m_kernelError = GIS_TIMEOUT;
	  }
	}
  }

  void Kernel::postGIS() {
	// Count how many agents of each type we have and build the list of objects to send to agents
	bool sendCivilians = m_config.getBool("send_civilians_at_start",true);
	int count = 0,x,y;
        mminX=0;mmaxX=0,mminY=0,mmaxY=0;
	for (ObjectSet::iterator it = m_pool.objects().begin();it!=m_pool.objects().end();++it) {
	  RescueObject* next = *it;
          m_pool.locate(next,&x,&y); //Calculating Map Boundaries
          if(x < mminX )
               mminX=x;
          else if(x > mmaxX )
               mmaxX=x;
          if(y <mminY )
              mminY=y;
          else if(y > mmaxY )
              mmaxY=y;
          count++;
	  switch (next->type()) {
	  case TYPE_CIVILIAN:
		m_connectionState.pendingAgents.civ.push_back(dynamic_cast<Civilian*>(next));
		break;
	  case TYPE_FIRE_BRIGADE:
		m_connectionState.pendingAgents.fire.push_back(dynamic_cast<FireBrigade*>(next));
		fireCount++;
		break;
	  case TYPE_FIRE_STATION:
		m_connectionState.pendingAgents.fireStation.push_back(dynamic_cast<FireStation*>(next));
		break;
	  case TYPE_POLICE_FORCE:
		m_connectionState.pendingAgents.police.push_back(dynamic_cast<PoliceForce*>(next));
		policeCount++;
		break;
	  case TYPE_POLICE_OFFICE:
		m_connectionState.pendingAgents.policeOffice.push_back(dynamic_cast<PoliceOffice*>(next));
		break;
	  case TYPE_AMBULANCE_TEAM:
		m_connectionState.pendingAgents.ambulance.push_back(dynamic_cast<AmbulanceTeam*>(next));
		ambulanceCount++;
		break;
	  case TYPE_AMBULANCE_CENTER:
		m_connectionState.pendingAgents.ambulanceCenter.push_back(dynamic_cast<AmbulanceCenter*>(next));
		break;
	  default:
		break;
	  }
	  if (sendCivilians || next->type()!=TYPE_CIVILIAN) m_objectsForAgents.insert(next);
	}
	// Write an update to the log with all initial objects
	if (m_log) {
	  OutputBuffer out;
	  Update dummy(0,m_pool.objects());
	  out.writeCommand(&dummy);
	  out.writeInt32(HEADER_NULL);
	  out.log(m_log);
	  fflush(m_log);
	}
  }

  void Kernel::preConnect() {
	m_kernelState = WAITING_FOR_CONNECTIONS;
  }

  void Kernel::waitForConnections() {
	Address from;
	while (!m_connectionState.connected()) {
	  Command* c = receiveCommand(&from,5000);
	  if (c) {
		handleCommand(c,from);
		delete c;
	  }
	  else {
		LOG_INFO("%d %d %d %d %d %d %d noack=%d",m_connectionState.pendingAgents.civ.size(),m_connectionState.pendingAgents.fire.size(),m_connectionState.pendingAgents.fireStation.size(),m_connectionState.pendingAgents.police.size(),m_connectionState.pendingAgents.policeOffice.size(),m_connectionState.pendingAgents.ambulance.size(),m_connectionState.pendingAgents.ambulanceCenter.size(),m_connectionState.waitingAgents.size());
		LOG_INFO("Waiting for %d simulators and %d viewers to acknowledge",m_connectionState.waitingSimulators.size(),m_connectionState.waitingViewers.size());
	  }
	}
  }

  void Kernel::postConnect() {
  }

  void Kernel::step() {
	startOfStep();
	//	LOG_DEBUG("Sending sense");
	//	struct timeval start, end;
	//	gettimeofday(&start,0);
	sendSense();
	//	gettimeofday(&end,0);
	//	LOG_DEBUG("sendSense: %ldms",timeDiff(&start,&end));
	//	LOG_DEBUG("Waiting for agents");
	//	gettimeofday(&start,0);
	getAgentCommands();
	//	gettimeofday(&end,0);
	//	LOG_DEBUG("getAgentCommands: %ldms",timeDiff(&start,&end));
	//	LOG_DEBUG("Sending commands");
	//	gettimeofday(&start,0);
	sendCommandsToSimulatorsAndViewers();
	//	gettimeofday(&end,0);
	//	LOG_DEBUG("sendCommandsToSimulatorsAndViewers: %ldms",timeDiff(&start,&end));
	//	LOG_DEBUG("Waiting for updates");
	//	gettimeofday(&start,0);
	getSimulatorUpdates();
	//	gettimeofday(&end,0);
	//	LOG_DEBUG("getSimulatorUpdates: %ldms",timeDiff(&start,&end));
	//	LOG_DEBUG("Sending updates");
	//	gettimeofday(&start,0);
	sendUpdatesToSimulatorsAndViewers();
	//	gettimeofday(&end,0);
	//	LOG_DEBUG("sendUpdatesToSimulatorsAndViewers: %ldms",timeDiff(&start,&end));
	//	gettimeofday(&start,0);
	endOfStep();
	//	gettimeofday(&end,0);
	//	LOG_DEBUG("endOfStep: %ldms",timeDiff(&start,&end));
  }

  void Kernel::endOfSimulation() {
	m_kernelState = FINISHED;
  }

  void Kernel::handleCommand(Command* command, Address& from) {
	switch (command->getType()) {
	case GK_CONNECT_OK:
	  handleGISConnectOK(dynamic_cast<GISConnectOK*>(command));
	  break;
	case GK_CONNECT_ERROR:
	  handleGISConnectError(dynamic_cast<GISConnectError*>(command));
	  break;
	case SK_CONNECT:
	  handleSimulatorConnect(dynamic_cast<SimulatorConnect*>(command),from);
	  break;
	case SK_ACKNOWLEDGE:
	  handleSimulatorAcknowledge(dynamic_cast<SimulatorAcknowledge*>(command),from);
	  break;
	case SK_UPDATE:
	  handleKernelUpdate(dynamic_cast<KernelUpdate*>(command),from);
	  break;
	case VK_CONNECT:
	  handleViewerConnect(dynamic_cast<ViewerConnect*>(command),from);
	  break;
	case VK_ACKNOWLEDGE:
	  handleViewerAcknowledge(dynamic_cast<ViewerAcknowledge*>(command),from);
	  break;
	case AK_CONNECT:
	  handleAgentConnect(dynamic_cast<AgentConnect*>(command),from);
	  break;
	case AK_ACKNOWLEDGE:
	  handleAgentAcknowledge(dynamic_cast<AgentAcknowledge*>(command),from);
	  break;
	case AK_MOVE:
	case AK_EXTINGUISH:
	case AK_CLEAR:
	case AK_RESCUE:
	case AK_LOAD:
	case AK_UNLOAD:
	case AK_REST:
	  handleAgentCommand(dynamic_cast<AgentCommand*>(command),from);
	  break;
	case AK_SAY:
	  break; //MMS: ignore say commands  
	case AK_TELL:
	  handleVoiceCommand(dynamic_cast<VoiceCommand*>(command),from);
	  break;
	case AK_CHANNEL:
	  handleChannelCommand (dynamic_cast<ChannelCommand*>(command), from);
	  break;	  	  
	default:
	  break;
	}
  }

  void Kernel::handleSimulatorConnect(SimulatorConnect* command, Address& from) {
	LOG_INFO("Simulator connected from %s",from.toString());
	Command* reply;
	OutputBuffer out;
	SimulatorInfo info;
	info.id = 0;
	if (m_kernelError==NONE) {
	  switch (m_kernelState) {
	  case INITIALISING:
	  case CONNECTING_TO_GIS:
		reply = new SimulatorConnectError("Kernel still waiting for GIS");
		break;	  
	  case WAITING_FOR_CONNECTIONS:
		// Send a connect OK message
		info.address = from;
		info.id = generateId();
		reply = new SimulatorConnectOK(info.id,m_pool.objects());
		break;
	  case FINISHED:
		reply = new SimulatorConnectError("Simulation has finished");
		break;
	  default:
		reply = new SimulatorConnectError("Simulation has started");
		break;
	  }
	}
	else reply = new SimulatorConnectError("Kernel has an error");
	out.writeCommand(reply);
	out.writeInt32(HEADER_NULL);
	if (m_connection.send(out.buffer(),from) && reply->getType()==KS_CONNECT_OK) {
	  // Add this simulator to the list of waiting simulators
	  m_connectionState.waitingSimulators.push_back(info);
	  LOG_INFO("Waiting for acknowledge from simulator %d at %s",info.id,from.toString());
	}
	else {
	  LOG_ERROR("Couldn't send reply to simulator");
	}
	delete reply;
  }

  void Kernel::handleSimulatorAcknowledge(SimulatorAcknowledge* command, Address& from) {
	Id id = command->getId();
	// Look up the waiting simulator list
	for (SimulatorInfos::iterator it = m_connectionState.waitingSimulators.begin();it!=m_connectionState.waitingSimulators.end();++it) {
	  SimulatorInfo next = *it;
	  if (next.id==id && next.address == from) {
		// This is the guy
		m_connectionState.waitingSimulators.erase(it);
		Simulator sim;
		sim.id = next.id;
		sim.address = next.address;
		m_simulators.push_back(sim);
		LOG_INFO("Simulator %d at %s has acknowledged. Now waiting for %d simulator(s)",id,from.toString(),m_connectionState.waitingSimulators.size());
		return;
	  }
	}
	LOG_WARNING("WARNING: Received an unexpected acknowledge from a simulator at %s claiming to be id %d",from.toString(),id);
  }

  void Kernel::handleViewerConnect(ViewerConnect* command, Address& from) {
	LOG_INFO("Viewer connected from %s",from.toString());
	Command* reply;
	if (m_kernelError==NONE) {
	  switch (m_kernelState) {
	  case INITIALISING:
	  case CONNECTING_TO_GIS:
		reply = new ViewerConnectError("Kernel still waiting for GIS");
		break;	  
	  case WAITING_FOR_CONNECTIONS:
		// Send a connect OK message
		reply = new ViewerConnectOK(m_pool.objects());
		break;
	  case FINISHED:
		reply = new ViewerConnectError("Simulation has finished");
		break;
	  default:
		reply = new ViewerConnectError("Simulation has started");
		break;
	  }
	}
	else reply = new ViewerConnectError("Kernel has an error");
	OutputBuffer out;
	out.writeCommand(reply);
	out.writeInt32(HEADER_NULL);
	if (m_connection.send(out.buffer(),from) && reply->getType()==KV_CONNECT_OK) {
	  // Add this viewer to the list of waiting viewers
	  ViewerInfo info;
	  info.address = from;
	  m_connectionState.waitingViewers.push_back(info);
	}
	else {
	  LOG_ERROR("Couldn't send reply to viewer");
	}
	delete reply;
  }

  void Kernel::handleViewerAcknowledge(ViewerAcknowledge* command, Address& from) {
	// Look up the waiting viewer list
	for (ViewerInfos::iterator it = m_connectionState.waitingViewers.begin();it!=m_connectionState.waitingViewers.end();++it) {
	  ViewerInfo next = *it;
	  if (next.address == from) {
		// This is the guy
		m_connectionState.waitingViewers.erase(it);
		Viewer v;
		v.address = next.address;
		m_viewers.push_back(v);
		LOG_INFO("Viewer at %s has acknowledged. Now waiting for %d viewer(s)",from.toString(),m_connectionState.waitingViewers.size());
		return;
	  }
	}
	LOG_WARNING("WARNING: Received an unexpected acknowledge from a viewer at %s",from.toString());
  }

  void Kernel::handleKernelUpdate(KernelUpdate* command, Address& from) {
	if (m_kernelState!=WAITING_FOR_UPDATES) {
	  LOG_WARNING("Received an unexpected SK_UPDATE from %s",from.toString());
	  return;
	}
	// Merge the changed objects into the object pool (as long as the time is right)
	if (command->getTime()==m_time) {
	  const ObjectSet& changed = command->getObjects();
	  m_pool.update(changed);
	  // Check for newly ignited buildings
	  for (ObjectSet::const_iterator it = changed.begin();it!=changed.end();++it) {
		const Building* b = dynamic_cast<const Building*>(*it);
		if (b && b->getFieryness()!=0 && m_buildingIgnitionTime[b->id()]==0) {
		  m_buildingIgnitionTime[b->id()] = m_time;
		}
	  }
	}
	else LOG_WARNING("WARNING: Received an update for time %d. It is now time %d",command->getTime(),m_time);
  }

  void Kernel::handleAgentConnect(AgentConnect* command, Address& from) {
	LOG_INFO("Agent connected from %s",from.toString());
	Command* reply;
	RescueObject* agentObject;
	AgentInfo info;
	if (m_kernelError==NONE) {
	  switch (m_kernelState) {
	  case INITIALISING:
	  case CONNECTING_TO_GIS:
		reply = new AgentConnectError(command->getTempId(),"Kernel waiting for GIS");
		break;
	  case WAITING_FOR_CONNECTIONS:
		// What kind of agent can we assign?
		agentObject = m_connectionState.pendingAgents.getAgent(command->getAgentType());
		if (agentObject==0) {
		  // No free agents
		  reply = new AgentConnectError(command->getTempId(),"No more agents");
		}
		else {
		  Id agentId = agentObject->id();
		  reply = new AgentConnectOK(command->getTempId(),agentId,m_objectsForAgents,*agentObject);
		  info.address = from;
		  info.tempId = command->getTempId();
		  info.id = agentId;
		}
		break;
	  case FINISHED:
		reply = new AgentConnectError(command->getTempId(),"Simulation has finished");
		break;
	  default:
		reply = new AgentConnectError(command->getTempId(),"Simulation has started");
		break;
	  }
	}
	else reply = new AgentConnectError(command->getTempId(),"Kernel has an error");
	// Send the reply
	OutputBuffer out;
	out.writeCommand(reply);
	out.writeInt32(HEADER_NULL);
	if (m_connection.send(out.buffer(),from) && reply->getType()==KA_CONNECT_OK) {
	  // Add this agent to the list of waiting agents
	  m_connectionState.waitingAgents.push_back(info);	
	}
	delete reply;
  }

  void Kernel::handleAgentAcknowledge(AgentAcknowledge* command, Address& from) {
	// Look up the waiting agent list
	for (AgentInfos::iterator it = m_connectionState.waitingAgents.begin();it!=m_connectionState.waitingAgents.end();++it) {
	  const AgentInfo next = *it;
	  if (next.address == from && next.id == command->getId()) {
		// This is the guy
		m_connectionState.waitingAgents.erase(it);
		Agent agent;
		agent.object = m_pool.getObject(next.id);
		agent.address = from;
		m_agents.push_back(agent);
		LOG_INFO("Agent %d at %s has acknowledged.",next.id,from.toString());
		return;
	  }
	}
	LOG_WARNING("WARNING: Received an unexpected acknowledge from an agent at %s claiming to be id %d",from.toString(),command->getId());
  }

  void Kernel::handleAgentCommand(AgentCommand* command, Address& from) {
	if (m_kernelState!=FINISHED) {
	  if (m_kernelState!=WAITING_FOR_AGENT_COMMANDS) LOG_INFO("Received agent command out of time. Current kernel state: %d: Agent id %d (%s) sent command %d",m_kernelState,command->getAgentId(),from.toString(),command->getType());
	  if (m_time > m_config.getInt("steps_agents_frozen",3)) {
		// Check that the agent is still alive
		RescueObject* o = m_pool.getObject(command->getAgentId());
		if (!o) {
		  LOG_INFO("Rejected command from unknown agent %d",command->getAgentId());
		  return;
		}
		if (!isAlive(o)) {
		  LOG_INFO("Rejected command from dead agent %d",command->getAgentId());
		  return;
		}
		// Check for valid command
		ExtinguishCommand* ex = dynamic_cast<ExtinguishCommand*>(command);
		if (ex) {
		  const Nozzles& nozzles = ex->getNozzles();
		  int maxNozzles = m_config.getInt("max_nozzles",-1);
		  int nozzlePower = m_config.getInt("max_extinguish_power",1000);
		  int maxSum = m_config.getInt("max_extinguish_power_sum",1000);
		  int numNozzles = nozzles.size();
		  if (maxNozzles != -1 && numNozzles>maxNozzles) {
			LOG_INFO("Extinguish rejected from agent %d: Too many nozzles (%d / %d)",command->getAgentId(),numNozzles,maxNozzles);
			return;
		  }
		  else {
			int sum = 0;
			for (Nozzles::const_iterator it = nozzles.begin();it!=nozzles.end();++it) {
			  Nozzle next = *it;
			  int distance = m_pool.range(next.target,o);
			  if (distance > m_config.getInt("max_extinguish_distance",30000)) {
				LOG_INFO("Extinguish rejected from agent %d trying to extinguish target %d: Distance too far (%d / %d)",command->getAgentId(),next.target,distance,m_config.getInt("max_extinguish_distance",30000));
				return;
			  }
			  else if (nozzlePower!=-1 && next.amount > nozzlePower) {
				LOG_INFO("Extinguish rejected from agent %d: Nozzle power too high (%d / %d)",command->getAgentId(),next.amount,nozzlePower);
				return;
			  }
			  else {
				sum += next.amount;
			  }
			}
			if (maxSum != -1 && sum > maxSum) {
			  LOG_INFO("Extinguish rejected from agent %d: Total nozzle power too high (%d / %d)",command->getAgentId(),sum,maxSum);
			  return;
			}
		  }
		}
		m_agentCommands.push_back(dynamic_cast<AgentCommand*>(command->clone()));
	  }
	  else {
		LOG_INFO("Rejected command from agent %d: Agents frozen until time %d",command->getAgentId(),m_config.getInt("steps_agents_frozen",3));
	  }
	}
	else LOG_INFO("Received unexpected agent command from %s",from.toString());
  }

  void Kernel::handleVoiceCommand(VoiceCommand* command, Address& from) {
	if (m_kernelState!=FINISHED) {
	  if (m_kernelState!=WAITING_FOR_AGENT_COMMANDS) LOG_INFO("Received agent command out of time. Current kernel state: %d: Agent id %d (%s) sent command %d",m_kernelState,command->getAgentId(),from.toString(),command->getType());
	  if (m_time > m_config.getInt("steps_agents_freezed",3)) {
		RescueObject* o = m_pool.getObject(command->getAgentId());
		if (!o) {
		  LOG_INFO("Rejected voice command from unknown agent %d",command->getAgentId());
		  return;
		}
		if (!isAlive(o)) {
		  LOG_INFO("Rejected voice command from dead agent %d",command->getAgentId());
		  return;
		}
		if ((int)command->getData().size() > m_config.getInt("say_max_bytes",256)) {
		  LOG_INFO("Agent %d can not send a message of length %d",command->getAgentId(),command->getData().size());
		  return;
		}
		if (m_config.getBool("send_voice_synchronously",true)) {
		  // Send the command with all the sense messages
		  //		  LOG_DEBUG("Queuing message from %d",command->getAgentId());
		  m_voiceCommands.push_back(dynamic_cast<VoiceCommand*>(command->clone()));
		}
		if (toldCount[command->getAgentId()] <= 0)
		{
			LOG_INFO("Maximum message count reached for agent %d", command->getAgentId());
		}
		else 
		{
		  toldCount[command->getAgentId()]--;
		  
		//Extension point: any other filter goes here
		//Distance filter
		//Channel bandwidth filter
		//Geographical filter
		 		  
		  // Send the command now
		  for (Agents::iterator it = m_agents.begin();it!=m_agents.end();++it) {
			Agent next = *it;
			sendHear(&next,command);
		  }
		}
	  }
	  else {
		LOG_INFO("Rejected voice command from agent %d: Agents frozen until time %d",command->getAgentId(),m_config.getInt("steps_agents_frozen",3));
	  }
	}
	else LOG_INFO("Received unexpected agent command from %s",from.toString());
  }
  
  void Kernel::handleChannelCommand (ChannelCommand* command, Address& from)
  {	
	  if (m_kernelState!=FINISHED) 
	  {
		  if (m_kernelState!=WAITING_FOR_AGENT_COMMANDS) LOG_INFO("Received agent command out of time. Current kernel state: %d: Agent id %d (%s) sent command %d",m_kernelState,command->getAgentId(),from.toString(),command->getType());
		  RescueObject* o = m_pool.getObject(command->getAgentId());
		  if (!o) 
		  {
			  LOG_INFO("Rejected channel command from unknown agent %d",command->getAgentId());
			  return;
		  }
		  if (!isAlive(o)) 
		  {
			  LOG_INFO("Rejected channel command from dead agent %d",command->getAgentId());
			  return;
		  }
		  if ((int)command->getChannels().size() == 0) 
		  {
			  LOG_INFO("Agent %d sent an empty channel list.",command->getAgentId());
			  return;
		  }
		  else 
		  {
			  INT_32 maxPlatoonMessageCount = m_config.getInt("max_platoon_message_count", 4);
			  INT_32 maxAmbulanceCenterMessageCount = 2 * ambulanceCount;
			  INT_32 maxFireStationMessageCount = 2 * fireCount;
			  INT_32 maxPoliceOfficeMessageCount = 2 * policeCount;

			  INT_32 maxMessageCount;
			  TypeId type = o->type();
			  Bytes refinedChannels;

			  if (type == TYPE_FIRE_BRIGADE || type == TYPE_AMBULANCE_TEAM || type == TYPE_POLICE_FORCE)
				  maxMessageCount = maxPlatoonMessageCount;
			  else if (type == TYPE_AMBULANCE_CENTER)
				  maxMessageCount = maxAmbulanceCenterMessageCount;
			  else if (type == TYPE_FIRE_STATION)
				  maxMessageCount = maxFireStationMessageCount;
			  else if (type == TYPE_POLICE_OFFICE)
				  maxMessageCount = maxPoliceOfficeMessageCount;

			  //Check constraints
			  for (Bytes::const_iterator it = command->getChannels().begin(); it != command->getChannels().end(); ++it)
			  {
				  Byte ch = *it;

				  if (refinedChannels.size() == maxMessageCount)
					  break;

				  if (ch < channelCount)
					if (ch != 0)
						refinedChannels.push_back(ch);
				  else
					  LOG_INFO("Unavailable channel %d (Agent: %d)", (int)ch, command->getAgentId());
			  }

		      LOG_DEBUG("Received registration request for %d channels from %d", refinedChannels.size(), command->getAgentId());
			  int tempIndex = 0;
			  while (refinedChannels.size() < maxMessageCount)
			  {
				  refinedChannels.push_back(refinedChannels.at(tempIndex));
				  tempIndex++;
			  }
			  m_channelRequests[o->id()] = refinedChannels;
			  LOG_DEBUG("Registered %d channels for %d", refinedChannels.size(), command->getAgentId());
		  }
	  }
	  else LOG_INFO("Received unexpected agent command from %s",from.toString());
  }

  void Kernel::handleGISConnectOK(GISConnectOK* command) {
	if (m_kernelState!=CONNECTING_TO_GIS) {
	  LOG_INFO("Received unexpected GISConnectOK");
	  return;
	}
	LOG_INFO("Connected to GIS. %d objects received",command->getObjects().size());
	m_pool.update(command->getObjects());
	// Send an acknowledge
	OutputBuffer out;
	GISAcknowledge reply;
	out.writeCommand(&reply);
	out.writeInt32(HEADER_NULL);
	m_connection.send(out.buffer(),m_gis);
	m_kernelState = GIS_CONNECTED;
  }

  void Kernel::handleGISConnectError(GISConnectError* command) {
	if (m_kernelState!=CONNECTING_TO_GIS) {
	  LOG_INFO("Received unexpected GISConnectError");
	  return;
	}
	LOG_WARNING("Error connecting to GIS: %s",command->getReason().c_str());
	m_kernelState = FINISHED;
	m_kernelError = GIS_ERROR;
  }

  void Kernel::startOfStep() {
	m_perception->newTimestep(m_time);
  }

  void Kernel::sendSense() {
	m_kernelState = SENDING_SENSE;
	// Calculate the visible set for each agent
	ObjectSet visible;
	OutputBuffer out;
	//	MultiFilter filter;
	AgentPropertyFilter filter;
	 
	for (ChannelMap::iterator it = m_channels.begin();it!=m_channels.end();++it) 
	{
	  VoiceCommandList comm = it->second->commands;
	  ChannelRegistration regs = it->second->regs;
	  for (ChannelRegistration::iterator itt = it->second->regs.begin(); itt != it->second->regs.end(); ++itt)
	  {
		itt->second = 0;
	  }
	  for (VoiceCommandList::iterator itt = comm.begin();itt!=comm.end();++itt) 
	  {
		delete *itt;
	  }
		comm.clear();
	}

	//Update from channel requests
	for (ChannelRequestMap::iterator it = m_channelRequests.begin(); it != m_channelRequests.end(); ++it)
	{
		Id agentId = it->first;
		Bytes chs = it->second;

		for (Bytes::iterator itt = chs.begin(); itt != chs.end(); ++itt)
		{
			int lastRef = 0;
			ChannelInfo* ch = m_channels[*itt];
			ChannelRegistration::const_iterator regItr;
		
			regItr = ch->regs.find(agentId);
			if (regItr != ch->regs.end())
				lastRef = regItr->second;
		  ch->regs[agentId] = lastRef+1;
		  }
	}
	
	  INT_32 maxPlatoonMessageCount = m_config.getInt("max_platoon_message_count", 4);
	  INT_32 maxAmbulanceCenterMessageCount = 2 * ambulanceCount;
	  INT_32 maxFireStationMessageCount = 2 * fireCount;
	  INT_32 maxPoliceOfficeMessageCount = 2 * policeCount;
	
	  INT_32 maxMessageCount;
	  TypeId type;

         m_pool.preProcessData(mmaxX,mmaxY,mminX,mminY,m_perception->m_range,(m_perception->m_speed)*m_time);
	for (Agents::iterator it = m_agents.begin();it!=m_agents.end();++it) {
	  Agent next = *it;

	  // Send any SAY or TELL messages that need sending *before* the next sense
	  if (m_config.getBool("send_voice_synchronously",true)) {
		//		LOG_DEBUG("Sending %d messages synchronously",m_voiceCommands.size());
		for (VoiceCommandList::iterator ix = m_voiceCommands.begin();ix!=m_voiceCommands.end();++ix) {
		  VoiceCommand* nextVoice = *ix;
		  //		  LOG_DEBUG("Trying to send from %d to %d",nextVoice->getAgentId(),next.object->id());
		  sendHear(&next,nextVoice);
		}
	  }
	  type = next.object->type();
	  if (type == TYPE_FIRE_BRIGADE || type == TYPE_AMBULANCE_TEAM || type == TYPE_POLICE_FORCE)
		  maxMessageCount = maxPlatoonMessageCount;
	  else if (type == TYPE_AMBULANCE_CENTER)
		  maxMessageCount = maxAmbulanceCenterMessageCount;
	  else if (type == TYPE_FIRE_STATION)
		  maxMessageCount = maxFireStationMessageCount;
	  else if (type == TYPE_POLICE_OFFICE)
		  maxMessageCount = maxPoliceOfficeMessageCount;

	  toldCount[next.object->id()] = maxMessageCount;
	  visible.clear();
	  filter.clear();
	  //	  struct timeval start, end;
	  //	  gettimeofday(&start,0);
	  calculateVisibleSet(next.object,visible,filter);
	  //	  gettimeofday(&end,0);
	  //	  LOG_DEBUG("calculateVisibleSet: %ldms",timeDiff(&start,&end));
	  // Send a KA_SENSE
	  //	  LOG_DEBUG("Sending sense to agent %d",next.object->id());
	  AgentSense sense(next.object->id(),m_time,visible);
	  out.clear();
	  out.setPropertyFilter(&filter);
	  out.writeCommand(&sense);
	  out.writeInt32(HEADER_NULL);
	  m_connection.send(out.buffer(),next.address);
	  out.clearPropertyFilter();
	}
	// Delete all the old voice commands
	for (VoiceCommandList::iterator it = m_voiceCommands.begin();it!=m_voiceCommands.end();++it) {
	  delete *it;
	}
        
        m_pool.postProcessData(m_perception->m_range,(m_perception->m_speed)*m_time);
	m_voiceCommands.clear();
  }

  void Kernel::getAgentCommands() {
	m_kernelState = WAITING_FOR_AGENT_COMMANDS;
	// Read commands until the end of the timestep
	struct timeval end;
	gettimeofday(&end,0);
	addTime(&end,m_config.getInt("step",1000));
	Address from;
	Command* c;
	do {
	  c = receiveUntil(&from,&end);
	  if (c) {
		handleCommand(c,from);
		delete c;
	  }
	} while (c);
  }

  void Kernel::sendCommandsToSimulatorsAndViewers() {
	m_kernelState = SENDING_COMMANDS;
	OutputBuffer out;
	Commands commands(m_time,m_agentCommands);
	out.writeCommand(&commands);
	out.writeInt32(HEADER_NULL);
	for (Simulators::iterator it = m_simulators.begin();it!=m_simulators.end();++it) {
	  Simulator next = *it;
	  if (!m_connection.send(out.buffer(),next.address)) {
		LOG_WARNING("WARNING: Error sending commands to simulator %d (%s)",next.id,next.address.toString());
	  }
	}
	for (Viewers::iterator it = m_viewers.begin();it!=m_viewers.end();++it) {
	  Viewer next = *it;
	  if (!m_connection.send(out.buffer(),next.address)) {
		LOG_WARNING("WARNING: Error sending commands to viewer %s",next.address.toString());
	  }
	}
	// Log all the agent commands
	out.log(m_log);
	fflush(m_log);	
	//Extension point: Log voice commands
	// Delete all the old commands
	for (AgentCommandList::iterator it = m_agentCommands.begin();it!=m_agentCommands.end();++it) {
	  delete *it;
	}
	m_agentCommands.clear();
  }

  void Kernel::getSimulatorUpdates() {
	m_kernelState = WAITING_FOR_UPDATES;
	// Wait until we have received updates from all simulators
	std::set<Id> waiting;
	for (Simulators::iterator it = m_simulators.begin();it!=m_simulators.end();++it) {
	  waiting.insert((*it).id);
	}
	Address from;
	while (!waiting.empty()) {
	  Command* c = receiveCommand(&from,1000);
	  if (c) {
		if (c->getType()==SK_UPDATE) {
		  KernelUpdate* update = dynamic_cast<KernelUpdate*>(c);
		  if (update) {
			if (waiting.find(update->getId())==waiting.end()) {
			  LOG_WARNING("I'm not waiting for an update from simulator %d",update->getId());
			}
			else {
			  waiting.erase(update->getId());
			}
		  }
		}
		handleCommand(c,from);
		delete c;
	  }
	  LOG_INFO("Still waiting for %d simulators",waiting.size());
	}
  }

  void Kernel::sendUpdatesToSimulatorsAndViewers() {
	m_kernelState = SENDING_UPDATES;
	OutputBuffer out;
	Update update(m_time,m_pool.objects());
	out.writeCommand(&update);
	out.writeInt32(HEADER_NULL);
	for (Simulators::iterator it = m_simulators.begin();it!=m_simulators.end();++it) {
	  Simulator next = *it;
	  if (!m_connection.send(out.buffer(),next.address)) {
		LOG_WARNING("WARNING: Could not send update to simulator %d (%s)",next.id,next.address.toString());
	  }
	}
	for (Viewers::iterator it = m_viewers.begin();it!=m_viewers.end();++it) {
	  Viewer next = *it;
	  if (!m_connection.send(out.buffer(),next.address)) {
		LOG_WARNING("WARNING: Could not send update to viewer %s",next.address.toString());
	  }
	}
	// Log the world update
	out.log(m_log);
	fflush(m_log);
  }

  void Kernel::endOfStep() {
  }

  void Kernel::calculateVisibleSet(const RescueObject* agent, ObjectSet& result, AgentPropertyFilter& filter) {

	  int i,j,x,y,ax,ay,bx,by;	
          PropertySet visible;
  

	  //for (ObjectSet::iterator it = m_pool.objects().begin();it!=m_pool.objects().end();++it) {
	 if(m_pool.locate(agent,&x,&y))
	 {
           //Calculating Objects in Vision Range
             ax=(x/m_perception->m_range)-1;
	     ay=(y/m_perception->m_range)-1;
	     for(i=ax;i<ax+3;i++)
	       for(j=ay;j<ay+3;j++)
		  if(i>=0 && i <= m_pool.xr -1 && j >=0 && j <= m_pool.yr -1 ){
		     for (ObjectSet::iterator it = m_pool.preData[i][j].begin();it!=m_pool.preData[i][j].end();++it) {
			  RescueObject* next = *it;
			  visible.clear();
			  m_perception->getVisibleProperties(agent,next,m_time,m_pool,visible);

			  if (!visible.empty()) {
		             for (PropertySet::iterator it = visible.begin();it!=visible.end();++it) {
				  filter.add(next->id(),*it);
				  }
			      result.insert(next);
			  }
                       }
		      }
             int range=(m_perception->m_speed)*(m_time);
	      bx=(x/range)-1;
	      by=(y/range)-1;
	      for(i=bx;i<bx+3;i++)
	         for(j=by;j<by+3;j++)
	            if(i>=0 && i <= m_pool.bxr -1 && j >=0 && j <= m_pool.byr -1 ){
		       for(ObjectSet::iterator it = m_pool.preBData[i][j].begin();it!=m_pool.preBData[i][j].end();++it) {
		            RescueObject* next = *it;
			    visible.clear();
//	    int range=m_pool.range(agent,next);
//	       if(range <= m_perception->m_range){
        		    m_perception->getFarBuildings(agent,next,m_time,m_pool,visible);
			    if (!visible.empty()) {
                              for (PropertySet::iterator it = visible.begin();it!=visible.end();++it) {
			     	  filter.add(next->id(),*it);
			       }
			       result.insert(next);
			     }
			}
           	     }
                             
         }
         else{

          LOG_WARNING("OrdinaryAgentPerception: Couldn't locate agent %d",agent->id());
          }

	//	m_perception->calculatePerception(agent->id(),m_pool,m_time,result);
	/*
	// Find all objects with the visible radius of the agent
	int x;
	int y;
	if (m_pool.locate(agent,&x,&y)) {
	  int range = m_config.getInt("vision",10000);
	  int spreadingSpeed = m_config.getInt("fire_cognition_spreading_speed",10000);
	  m_pool.getObjectsInRange(x,y,range,result);
	  // Find far buildings
	  if (agent->type()!=TYPE_CIVILIAN || !m_config.getBool("civilian_sense_hack",false)) {
		if (m_time > m_config.getInt("steps_far_fire_invisible",0)) {
		  bool fireOnly = m_config.getBool("send_far_fire_changed_only",true);
		  if (fireOnly) m_agentFilter.clearFarBuildings();
		  for (ObjectSet::iterator it = m_pool.objects().begin();it!=m_pool.objects().end();++it) {
			RescueObject* next = *it;
			Building* b = dynamic_cast<Building*>(next);
			if (b && b->getFieryness()!=0) {
			  int ignitionTime = m_buildingIgnitionTime[b->id()];
			  if (ignitionTime<0) continue;
			  int visibleRange = spreadingSpeed * (m_time-ignitionTime);
			  int range = m_pool.range(agent,b);
			  if (range <= visibleRange) {
				result.insert(b);
				if (fireOnly) m_agentFilter.addFarBuilding(b->id());
			  }
			}
		  }
		}
	  }
	  if (m_config.getBool("round_hp_and_damage",false)) m_agentFilter.setSelf(agent->id());
	}
	else {
	  LOG_WARNING("Couldn't locate agent %d",agent->id());
	}
	*/
  }

  bool Kernel::canHear(const Agent* agent, const VoiceCommand* command) 
  {
	RescueObject* speaker = m_pool.getObject(command->getAgentId());
	TypeId hearer = agent->object->type();
    ChannelInfo* ch;
	ChannelRegistration::iterator regItr;
	ch = m_channels[command->getChannel()];
		
	if (agent->object->id() == command->getAgentId())
		return true; //Own message (doesn't count)

	//MMS: Allows all SAY communications
	if (ch->type == CHANNEL_SAY &&  m_pool.range(speaker,agent->object) > m_config.getInt("voice",30000))
		return true;
	
	regItr = ch->regs.find(agent->object->id());

	if (regItr != ch->regs.end())
		if (regItr->second > 0)
		{
			//Extension point: any receving filter goes here
			//sender filter
			//distance filter
			switch (ch->type)
			{
				//MMS: commented to allow all SAY communications
				//case CHANNEL_SAY:
				//	if (m_pool.range(speaker,agent->object) > m_config.getInt("voice",30000))
				//		return false;
				//	break;
				case CHANNEL_RADIO:
					break;
				default:
					return false;
			}
			regItr->second--;
			return true;
		}
	return false;
  }

  void Kernel::sendHear(const Agent* agent, const VoiceCommand* command) {
	if (canHear(agent,command)) {
	  LOG_DEBUG("Sending hear message from %d to %d on channel %d",command->getAgentId(),agent->object->id(), command->getChannel());
	  AgentHear hear(KA_HEAR,agent->object->id(),command->getAgentId(), command->getChannel(),command->getData());
	  OutputBuffer out;
	  out.writeCommand(&hear);
//	  if (m_config.getBool("additional_hearing",false)) {
//		Header header = command->getType()==AK_SAY?KA_HEAR_SAY:KA_HEAR_TELL;
//		AgentHear extraHear(header,agent->object->id(),command->getAgentId(),command->getData());
//		out.writeCommand(&extraHear);
//	  }
	  out.writeInt32(HEADER_NULL);
	  m_connection.send(out.buffer(),agent->address);
	}
  }
  
  Id Kernel::generateId() {
	return m_nextId++;
  }

  Command* Kernel::receiveCommand(Address* from, long long timeout) {
	if (m_pendingCommands.empty()) {
	  Bytes in;
	  if (m_connection.receive(in,from,timeout)) {
		CommandList all;
		InputBuffer buffer(in);
		try {
		  decodeCommands(all,buffer);
		}
		catch (Overrun& e) {
		  LOG_WARNING("Overrun detected while decoding command from %s: %s",from->toString(),e.why().c_str());
		  LOG_WARNING("The error was encountered at index %d",buffer.cursor());
		  buffer.setCursor(0);
		  dumpBytes(Librescue::LOG_LEVEL_WARNING,buffer);
		}
		for (CommandList::iterator it = all.begin();it!=all.end();++it) m_pendingCommands.push(*it);
	  }
	}
	if (m_pendingCommands.empty()) return 0;
	Command* result = m_pendingCommands.front();
	m_pendingCommands.pop();
	return result;
  }

  Command* Kernel::receiveUntil(Address* from, struct timeval* end) {
	struct timeval now;
	gettimeofday(&now,0);
	while (before(&now,end)) {
	  Command* c = receiveCommand(from,timeDiff(&now,end));
	  if (c) return c;
	  gettimeofday(&now,0);
	}
	return 0;
  }

  bool Kernel::isAlive(const RescueObject* o) {
	const Humanoid* h = dynamic_cast<const Humanoid*>(o);
	const Building* b = dynamic_cast<const Building*>(o);
	if (h) {
	  return h->getHP()>0;
	}
	else if (b) {
	  return b->getBrokenness()<100 && b->getFieryness()!=FIERYNESS_BURNT_OUT;
	}
	else {
	  return false;
	}
  }

  bool Kernel::AgentCount::empty() {
	return civ.empty() && fire.empty() && police.empty() && ambulance.empty() && fireStation.empty() && policeOffice.empty() && ambulanceCenter.empty();
  }

  RescueObject* Kernel::AgentCount::getAgent(AgentType type) {
	RescueObject* result = 0;
	if ((type & AGENT_TYPE_CIVILIAN) && !civ.empty()) {
	  result = civ.back();
	  civ.pop_back();
	}
	else if ((type & AGENT_TYPE_FIRE_BRIGADE) && !fire.empty()) {
	  result = fire.back();
	  fire.pop_back();
	}
	else if ((type & AGENT_TYPE_POLICE_FORCE) && !police.empty()) {
	  result = police.back();
	  police.pop_back();
	}
	else if ((type & AGENT_TYPE_AMBULANCE_TEAM) && !ambulance.empty()) {
	  result = ambulance.back();
	  ambulance.pop_back();
	}
	else if ((type & AGENT_TYPE_FIRE_STATION) && !fireStation.empty()) {
	  result = fireStation.back();
	  fireStation.pop_back();
	}
	else if ((type & AGENT_TYPE_POLICE_OFFICE) && !policeOffice.empty()) {
	  result = policeOffice.back();
	  policeOffice.pop_back();
	}
	else if ((type & AGENT_TYPE_AMBULANCE_CENTER) && !ambulanceCenter.empty()) {
	  result = ambulanceCenter.back();
	  ambulanceCenter.pop_back();
	}
	return result;
  }
}
