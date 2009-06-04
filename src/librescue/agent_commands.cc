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
 Mohammad Mehdi Saboorian / Channel based communication
*/

#include "command.h"
#include "objects.h"
#include "handy.h"
#include "error.h"

namespace Librescue {
  AgentConnect::AgentConnect(TypeId type, Id tempId, INT_32 version) : m_type(type), m_tempId(tempId), m_version(version) {
  }

  AgentConnect::AgentConnect(InputBuffer& in) {
	decode(in);
  }

  AgentConnect::~AgentConnect() {
  }

  Header AgentConnect::getType() const {
	return AK_CONNECT;
  }

  void AgentConnect::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_version);
	out.writeInt32(m_tempId);
        out.writeInt32(1);
	out.writeInt32(m_type);
  }

  void AgentConnect::decode(InputBuffer& in) {
	Command::decode(in);
	m_version = in.readInt32();
	m_tempId = in.readInt32();
        int count = in.readInt32();
	m_type = (TypeId)in.readInt32();
        for (int i=1; i<count; ++i) {
          LOG_INFO("Ignoring AgentConnect TypeId: %d", in.readInt32());
        }
  }

  Command* AgentConnect::clone() const {
	return new AgentConnect(m_type,m_tempId,m_version);
  }

  TypeId AgentConnect::getAgentType() const {
	return m_type;
  }

  Id AgentConnect::getTempId() const {
	return m_tempId;
  }

  INT_32 AgentConnect::getVersion() const {
	return m_version;
  }

  AgentAcknowledge::AgentAcknowledge(Id id) : m_id(id) {
  }

  AgentAcknowledge::AgentAcknowledge(InputBuffer& in) {
	decode(in);
  }

  AgentAcknowledge::~AgentAcknowledge() {
  }

  Header AgentAcknowledge::getType() const {
	return AK_ACKNOWLEDGE;
  }

  void AgentAcknowledge::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_id);
  }

  void AgentAcknowledge::decode(InputBuffer& in) {
	Command::decode(in);
	m_id = in.readInt32();
  }

  Command* AgentAcknowledge::clone() const {
	return new AgentAcknowledge(m_id);
  }

  Id AgentAcknowledge::getId() const {
	return m_id;
  }

  AgentConnectOK::AgentConnectOK(Id requestId, Id agentId, const ObjectSet& staticObjects) : m_requestId(requestId), m_agentId(agentId) {
	m_objects.insert(staticObjects.begin(),staticObjects.end());
	m_delete = false;
  }

  AgentConnectOK::AgentConnectOK(InputBuffer& in) {
	m_delete = false;	
	decode(in);
  }

  AgentConnectOK::~AgentConnectOK() {
	deleteObjects();
  }

  void AgentConnectOK::deleteObjects() {
	if (m_delete) {
	  // Delete all the objects
	  for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
		delete *it;
	  }
	  m_delete = false;
	}
	m_objects.clear();
  }

  Header AgentConnectOK::getType() const {
	return KA_CONNECT_OK;
  }

  void AgentConnectOK::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_requestId);
	out.writeInt32(m_agentId);
	out.writeObjects(m_objects);
  }

  void AgentConnectOK::decode(InputBuffer& in) {
	Command::decode(in);
	m_requestId = in.readInt32();
	m_agentId = in.readInt32();
	deleteObjects();
	in.readObjects(0,m_objects);
	m_delete = true;
  }

  Command* AgentConnectOK::clone() const {
	if (m_delete) {
	  // Need to clone objects as well
	  ObjectSet clonedObjects;
	  for (ObjectSet::const_iterator it = m_objects.begin();it!=m_objects.end();++it) {
		clonedObjects.insert((*it)->clone());
	  }
	  AgentConnectOK* result = new AgentConnectOK(m_requestId,m_agentId,clonedObjects);
	  result->m_delete = true;
	  return result;
	}
	else return new AgentConnectOK(m_requestId,m_agentId,m_objects);
  }

  Id AgentConnectOK::getRequestId() const {
	return m_requestId;
  }

  Id AgentConnectOK::getAgentId() const {
	return m_agentId;
  }

  const ObjectSet& AgentConnectOK::getObjects() const {
	return m_objects;
  }

  AgentConnectError::AgentConnectError(Id id, std::string reason) : m_id(id), m_reason(reason) {
  }

  AgentConnectError::AgentConnectError(InputBuffer& in) {
	decode(in);
  }

  AgentConnectError::~AgentConnectError() {
  }

  Header AgentConnectError::getType() const {
	return KA_CONNECT_ERROR;
  }

  void AgentConnectError::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_id);
	out.writeString(m_reason);
  }

  void AgentConnectError::decode(InputBuffer& in) {
	Command::decode(in);
	m_id = in.readInt32();
	m_reason = in.readString();
  }

  Command* AgentConnectError::clone() const {
	return new AgentConnectError(m_id,m_reason);
  }

  const std::string& AgentConnectError::getReason() const {
	return m_reason;
  }

  AgentSense::AgentSense(Id id, INT_32 time, const ObjectSet& objects) : m_id(id), m_time(time) {
	//	for (ObjectSet::const_iterator it = objects.begin();it!=objects.end();++it) {
	//	  m_objects.insert((*it)->clone());
	//	}
	m_objects.insert(objects.begin(),objects.end());
	m_delete = false;	
  }

  AgentSense::AgentSense(InputBuffer& in) {
	m_delete = false;	
	decode(in);
  }
  
  AgentSense::~AgentSense() {
	// Delete all the objects
	//	for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
	//	  delete *it;
	//	}
	deleteObjects();
  }

  void AgentSense::deleteObjects() {
	if (m_delete) {
	  // Delete all the objects
	  for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
		delete *it;
	  }
	  m_delete = false;
	}
	m_objects.clear();
  }

  Header AgentSense::getType() const {
	return KA_SENSE;
  }
 
  void AgentSense::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_id);
	out.writeInt32(m_time);
	out.writeObjects(m_objects);
  }
  
  void AgentSense::decode(InputBuffer& in) {
	Command::decode(in);
	m_id = in.readInt32();
	m_time = in.readInt32();
	// Delete any old objects
	deleteObjects();
	in.readObjects(m_time,m_objects);
	m_delete = true;
  }

  Command* AgentSense::clone() const {
	if (m_delete) {
	  // Need to clone objects as well
	  ObjectSet clonedObjects;
	  for (ObjectSet::const_iterator it = m_objects.begin();it!=m_objects.end();++it) {
		clonedObjects.insert((*it)->clone());
	  }
	  AgentSense* result = new AgentSense(m_id,m_time,clonedObjects);
	  result->m_delete = true;
	  return result;
	}
	return new AgentSense(m_id,m_time,m_objects);
  }

  Id AgentSense::getId() const {
	return m_id;
  }

  INT_32 AgentSense::getTime() const {
	return m_time;
  }

  const ObjectSet& AgentSense::getObjects() const {
	return m_objects;
  }

  AgentHear::AgentHear(Header type, Id to, Id from, Byte channel, const Bytes& data) : m_type(type), m_to(to), m_from(from), m_channel(channel), m_data(data) {}

  AgentHear::AgentHear(Header type, InputBuffer& in) : m_type(type) {
	decode(in);
  }

  AgentHear::~AgentHear() {}

  Header AgentHear::getType() const {
	return m_type;
  }

  void AgentHear::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_to);
	out.writeInt32(m_from);
	out.writeInt32((INT_32)m_channel);
	out.writeInt32(m_data.size());
	out.write(m_data);
  }

  void AgentHear::decode(InputBuffer& in) {
	Command::decode(in);
	m_to = in.readInt32();
	m_from = in.readInt32();
	m_channel = (Byte) in.readInt32();
	int length = in.readInt32();
	m_data.clear();
	m_data.reserve(length);
	in.read(length,m_data);
  }

  Command* AgentHear::clone() const {
	return new AgentHear(m_type,m_to,m_from,m_channel,m_data);
  }

  Id AgentHear::getFrom() const {
	return m_from;
  }

  Id AgentHear::getTo() const {
	return m_to;
  }
  
  Byte AgentHear::getChannel() const
  {
  	return m_channel;
  }

  const Bytes& AgentHear::getData() const {
	return m_data;
  }

  AgentCommand::AgentCommand(Id agent) : m_agentId(agent) {
  }

  AgentCommand::AgentCommand(InputBuffer& in) {
	decode(in);
  }

  AgentCommand::~AgentCommand() {}

  void AgentCommand::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_agentId);
  }

  void AgentCommand::decode(InputBuffer& in) {
	Command::decode(in);
	m_agentId = in.readInt32();
  }

  Id AgentCommand::getAgentId() const {
	return m_agentId;
  }

  VoiceCommand::VoiceCommand(Id agent, const Byte* msg, int size, const Byte channel) : AgentCommand(agent) {
  	m_channel=channel;
	m_data.reserve(size);
	for (int i=0;i<size;++i) m_data.push_back(msg[i]);
  }

  VoiceCommand::VoiceCommand(Id agent, const Bytes& msg, const Byte channel) : AgentCommand(agent) {
  	m_channel=channel;
	m_data.reserve(msg.size());
	m_data.insert(m_data.end(),msg.begin(),msg.end());
  }

  VoiceCommand::VoiceCommand(InputBuffer& in) : AgentCommand(0) {
	decode(in);
  }

  VoiceCommand::~VoiceCommand() {
  }

  const Bytes& VoiceCommand::getData() const {
	return m_data;
  }
  
  const Byte VoiceCommand::getChannel() const {
	return m_channel;
  }

  void VoiceCommand::encode(OutputBuffer& out) const {
	AgentCommand::encode(out);
	out.writeInt32((INT_32)m_channel);
	out.writeInt32(m_data.size());
	out.write(m_data);
  }

  void VoiceCommand::decode(InputBuffer& in) {
	AgentCommand::decode(in);
	m_channel = (Byte) in.readInt32();
	INT_32 size = in.readInt32();
	m_data.clear();
	m_data.reserve(size);
	in.read(size,m_data);
  }

  SayCommand::SayCommand(Id agent, const Byte* msg, int size) : VoiceCommand(agent,msg,size, 0) {}

  SayCommand::SayCommand(Id agent, const Bytes& msg) : VoiceCommand(agent,msg, 0) {}

  SayCommand::SayCommand(InputBuffer& in) : VoiceCommand(0,0,0) {
	decode(in);
  }

  SayCommand::~SayCommand() {}

  Header SayCommand::getType() const {
	return AK_TELL;
  }

  Command* SayCommand::clone() const {
	return new SayCommand(m_agentId,m_data);
  }

  TellCommand::TellCommand(Id agent, const Byte* msg, int size, const Byte channel) : VoiceCommand(agent,msg,size,channel) {}

  TellCommand::TellCommand(Id agent, const Bytes& msg, const Byte channel) : VoiceCommand(agent,msg,channel) {}

  TellCommand::TellCommand(InputBuffer& in) : VoiceCommand(0,0,0) {
	decode(in);
  }

  TellCommand::~TellCommand() {
  }

  Header TellCommand::getType() const {
	return AK_TELL;
  }

  Command* TellCommand::clone() const {
	return new TellCommand(m_agentId,m_data);
  }
  
  ChannelCommand::ChannelCommand (Id agent, const Bytes& channels) : AgentCommand (agent)
  {
	  m_channels.reserve (channels.size());
	  m_channels.insert (m_channels.end(), channels.begin(), channels.end());
  }

  ChannelCommand::ChannelCommand(Id agent) : AgentCommand(agent) {
  }

  ChannelCommand::ChannelCommand(InputBuffer& in) : AgentCommand(0) {
	decode(in);
  }

  ChannelCommand::~ChannelCommand() {
  }

  Header ChannelCommand::getType() const {
	return AK_CHANNEL;
  }

  void ChannelCommand::add (Byte channel)
  {
	  m_channels.push_back(channel);
  }

  void ChannelCommand::encode(OutputBuffer& out) const 
  {
	AgentCommand::encode(out);
	out.writeInt32(m_channels.size());
	out.write(m_channels);
  }

  void ChannelCommand::decode(InputBuffer& in) {
	AgentCommand::decode(in);
	m_channels.clear();
	int count = in.readInt32();
	m_channels.reserve(count);
	in.read(count, m_channels);
  }

  Command* ChannelCommand::clone() const {
	return new ChannelCommand(m_agentId,m_channels);
  }

  const Bytes& ChannelCommand::getChannels() const {
	return m_channels;
  }

  MoveCommand::MoveCommand(Id agent, const IdList& path) : AgentCommand(agent) {
	m_path.reserve(path.size());
	m_path.insert(m_path.end(),path.begin(),path.end());
  }

  MoveCommand::MoveCommand(Id agent) : AgentCommand(agent) {
  }

  MoveCommand::MoveCommand(InputBuffer& in) : AgentCommand(0) {
	decode(in);
  }

  MoveCommand::~MoveCommand() {
  }

  Header MoveCommand::getType() const {
	return AK_MOVE;
  }

  void MoveCommand::add(Id next) {
	m_path.push_back(next);
  }

  void MoveCommand::encode(OutputBuffer& out) const {
	AgentCommand::encode(out);
	out.writeInt32(m_path.size());
	for (IdList::const_iterator it = m_path.begin();it != m_path.end();++it) {
	  const Id next = *it;
	  out.writeInt32(next);
	}
  }

  void MoveCommand::decode(InputBuffer& in) {
	AgentCommand::decode(in);
	m_path.clear();
	int count = in.readInt32();
	m_path.reserve(count);
	for (int i=0;i<count;++i) {
	  m_path.push_back((Id)in.readInt32());
	}
  }

  Command* MoveCommand::clone() const {
	return new MoveCommand(m_agentId,m_path);
  }

  const IdList& MoveCommand::getPath() const {
	return m_path;
  }

  ExtinguishCommand::ExtinguishCommand(Id agent) : AgentCommand(agent) {}


  ExtinguishCommand::ExtinguishCommand(Id agent, Id target, INT_32 direction, INT_32 x, INT_32 y, INT_32 amount) : AgentCommand(agent) {
	addNozzle(target,direction,x,y,amount);
  }

  ExtinguishCommand::ExtinguishCommand(InputBuffer& in) : AgentCommand(0) {
	decode(in);
  }

  ExtinguishCommand::~ExtinguishCommand() {}

  void ExtinguishCommand::addNozzle(Id target, INT_32 direction, INT_32 x, INT_32 y, INT_32 amount) {
	Nozzle nozzle(target,direction,x,y,amount);
	m_nozzles.push_back(nozzle);
  }

  Header ExtinguishCommand::getType() const {
	return AK_EXTINGUISH;
  }

  void ExtinguishCommand::encode(OutputBuffer& out) const {
	AgentCommand::encode(out);
	for (Nozzles::const_iterator it = m_nozzles.begin();it!=m_nozzles.end();++it) {
	  const Nozzle next = *it;
	  out.writeInt32(next.target);
	  out.writeInt32(next.direction);
	  out.writeInt32(next.x);
	  out.writeInt32(next.y);
	  out.writeInt32(next.amount);
	}
	out.writeInt32(0);
  }

  void ExtinguishCommand::decode(InputBuffer& in) {
	AgentCommand::decode(in);
	m_nozzles.clear();
	Id next;
	//	LOG_DEBUG("Decoding extinguish from %d",m_agentId);
	do {
	  next = (Id)in.readInt32();
	  if (next) {
		INT_32 direction = in.readInt32();
		INT_32 x = in.readInt32();
		INT_32 y = in.readInt32();
		INT_32 amount = in.readInt32();
		//		LOG_DEBUG("Decoded nozzle: target=%d, water=%d",next,amount);
		addNozzle(next,direction,x,y,amount);
	  }
	} while (next);
  }

  Command* ExtinguishCommand::clone() const {
	ExtinguishCommand* result = new ExtinguishCommand(m_agentId);
	for (Nozzles::const_iterator it = m_nozzles.begin();it!=m_nozzles.end();++it) {
	  const Nozzle next = *it;
	  result->addNozzle(next.target,next.direction,next.x,next.y,next.amount);
	}
	return result;
  }

  const Nozzles& ExtinguishCommand::getNozzles() const {
	return m_nozzles;
  }

  TargetCommand::TargetCommand(Id agent, Id target) : AgentCommand(agent), m_target(target) {}

  TargetCommand::TargetCommand(InputBuffer& in) : AgentCommand(0) {
	decode(in);
  }

  TargetCommand::~TargetCommand() {}

  void TargetCommand::encode(OutputBuffer& out) const {
	AgentCommand::encode(out);
	out.writeInt32(m_target);
  }

  void TargetCommand::decode(InputBuffer& in) {
	AgentCommand::decode(in);
	m_target = (Id)in.readInt32();
  }

  Id TargetCommand::getTarget() const {
	return m_target;
  }

  LoadCommand::LoadCommand(Id agent, Id target) : TargetCommand(agent,target) {}

  LoadCommand::LoadCommand(InputBuffer& in) : TargetCommand(0,0) {
	decode(in);
  }

  LoadCommand::~LoadCommand() {}

  Header LoadCommand::getType() const {
	return AK_LOAD;
  }

  Command* LoadCommand::clone() const {
	return new LoadCommand(m_agentId,m_target);
  }

  //  RescueCommand::RescueCommand(Id agent) : TargetCommand(agent) {}

  RescueCommand::RescueCommand(Id agent, Id target) : TargetCommand(agent,target) {}

  RescueCommand::RescueCommand(InputBuffer& in) : TargetCommand(0,0) {
	decode(in);
  }

  RescueCommand::~RescueCommand() {}

  Header RescueCommand::getType() const {
	return AK_RESCUE;
  }

  Command* RescueCommand::clone() const {
	return new RescueCommand(m_agentId,m_target);
  }

  //  ClearCommand::ClearCommand(Id agent) : TargetCommand(agent) {}

  ClearCommand::ClearCommand(Id agent, Id target) : TargetCommand(agent,target) {}

  ClearCommand::ClearCommand(InputBuffer& in) : TargetCommand(0,0) {
	decode(in);
  }

  ClearCommand::~ClearCommand() {}

  Header ClearCommand::getType() const {
	return AK_CLEAR;
  }

  Command* ClearCommand::clone() const {
	return new ClearCommand(m_agentId,m_target);
  }

  RepairCommand::RepairCommand(Id agent, Id target) : TargetCommand(agent,target) {}

  RepairCommand::RepairCommand(InputBuffer& in) : TargetCommand(0,0) {
	decode(in);
  }

  RepairCommand::~RepairCommand() {}

  Header RepairCommand::getType() const {
	return AK_REPAIR;
  }

  Command* RepairCommand::clone() const {
	return new RepairCommand(m_agentId,m_target);
  }

  UnloadCommand::UnloadCommand(Id agent) : AgentCommand(agent) {}

  UnloadCommand::UnloadCommand(InputBuffer& in) : AgentCommand(0) {
	decode(in);
  }

  UnloadCommand::~UnloadCommand() {}

  Header UnloadCommand::getType() const {
	return AK_UNLOAD;
  }

  Command* UnloadCommand::clone() const {
	return new UnloadCommand(m_agentId);
  }

  RestCommand::RestCommand(Id agent) : AgentCommand(agent) {}

  RestCommand::RestCommand(InputBuffer& in) : AgentCommand(0) {
	decode(in);
  }

  RestCommand::~RestCommand() {}

  Header RestCommand::getType() const {
	return AK_REST;
  }

  Command* RestCommand::clone() const {
	return new RestCommand(m_agentId);
  }
};
