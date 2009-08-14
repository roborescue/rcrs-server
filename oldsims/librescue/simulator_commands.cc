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

#include "command.h"
#include "objects.h"
#include "error.h"

namespace Librescue {
  SimulatorConnect::SimulatorConnect(Id requestId, INT_32 version) : m_requestId(requestId), m_version(version) {}

  SimulatorConnect::SimulatorConnect(InputBuffer& in) {
	decode(in);
  }

  SimulatorConnect::~SimulatorConnect() {}

  Header SimulatorConnect::getType() const {
	return SK_CONNECT;
  }

  void SimulatorConnect::encode(OutputBuffer& out) const {
	Command::encode(out);
        out.writeInt32(m_requestId);
	out.writeInt32(m_version);
  }

  void SimulatorConnect::decode(InputBuffer& in) {
	Command::decode(in);
        m_requestId = in.readInt32();
	m_version = in.readInt32();
  }

  Command* SimulatorConnect::clone() const {
    return new SimulatorConnect(m_requestId, m_version);
  }

  Id SimulatorConnect::getRequestId() const {
    return m_requestId;
  }

  INT_32 SimulatorConnect::getVersion() const {
	return m_version;
  }

  SimulatorConnectOK::SimulatorConnectOK(Id requestId, Id simulatorId, const ObjectSet& objects) : m_requestId(requestId), m_simulatorId(simulatorId) {
	m_objects.insert(objects.begin(),objects.end());
	m_delete = false;	
  }

  SimulatorConnectOK::SimulatorConnectOK(InputBuffer& in) {
	m_delete = false;	
	decode(in);
  }
  
  SimulatorConnectOK::~SimulatorConnectOK() {
	deleteObjects();
  }

  void SimulatorConnectOK::deleteObjects() {
	if (m_delete) {
	  // Delete all the objects
	  for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
		delete *it;
	  }
	  m_delete = false;
	}
	m_objects.clear();
  }

  Header SimulatorConnectOK::getType() const {
	return KS_CONNECT_OK;
  }

  void SimulatorConnectOK::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_requestId);
	out.writeInt32(m_simulatorId);
	out.writeObjects(m_objects);
  }

  void SimulatorConnectOK::decode(InputBuffer& in) {
	Command::decode(in);
	m_requestId = in.readInt32();
	m_simulatorId = in.readInt32();
	// Delete any old objects
	deleteObjects();
	in.readObjects(0,m_objects);
	m_delete = true;
        LOG_DEBUG("Decoded simulator connect OK. RequestId = %d, simulatorId = %d", m_requestId, m_simulatorId);
  }

  Command* SimulatorConnectOK::clone() const {
	if (m_delete) {
	  // Need to clone objects as well
	  ObjectSet clonedObjects;
	  for (ObjectSet::const_iterator it = m_objects.begin();it!=m_objects.end();++it) {
		clonedObjects.insert((*it)->clone());
	  }
	  SimulatorConnectOK* result = new SimulatorConnectOK(m_requestId, m_simulatorId, clonedObjects);
	  result->m_delete = true;
	  return result;
	}
	return new SimulatorConnectOK(m_requestId, m_simulatorId, m_objects);
  }

  const ObjectSet& SimulatorConnectOK::getObjects() const {
	return m_objects;
  }

  Id SimulatorConnectOK::getRequestId() const {
	return m_requestId;
  }

  Id SimulatorConnectOK::getSimulatorId() const {
	return m_simulatorId;
  }

  SimulatorConnectError::SimulatorConnectError(Id requestId, std::string reason) : m_requestId(requestId), m_reason(reason) {}

  SimulatorConnectError::SimulatorConnectError(InputBuffer& in) {
	decode(in);
  }

  SimulatorConnectError::~SimulatorConnectError() {}

  Header SimulatorConnectError::getType() const {
	return KS_CONNECT_ERROR;
  }

  void SimulatorConnectError::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_requestId);
	out.writeString(m_reason);
  }

  void SimulatorConnectError::decode(InputBuffer& in) {
	Command::decode(in);
        m_requestId = in.readInt32();
	m_reason = in.readString();
  }

  Command* SimulatorConnectError::clone() const {
    return new SimulatorConnectError(m_requestId, m_reason);
  }

  Id SimulatorConnectError::getRequestId() const {
	return m_requestId;
  }

  const std::string& SimulatorConnectError::getReason() const {
	return m_reason;
  }

  SimulatorAcknowledge::SimulatorAcknowledge(Id requestId, Id simulatorId) : m_requestId(requestId), m_simulatorId(simulatorId) {}

  SimulatorAcknowledge::SimulatorAcknowledge(InputBuffer& in) {
	decode(in);
  }

  SimulatorAcknowledge::~SimulatorAcknowledge() {}

  Header SimulatorAcknowledge::getType() const {
	return SK_ACKNOWLEDGE;
  }

  void SimulatorAcknowledge::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_requestId);
	out.writeInt32(m_simulatorId);
  }

  void SimulatorAcknowledge::decode(InputBuffer& in) {
	Command::decode(in);
        m_requestId = in.readInt32();
	m_simulatorId = in.readInt32();
  }

  Command* SimulatorAcknowledge::clone() const {
    return new SimulatorAcknowledge(m_requestId, m_simulatorId);
  }

  Id SimulatorAcknowledge::getRequestId() const {
	return m_requestId;
  }

  Id SimulatorAcknowledge::getSimulatorId() const {
	return m_requestId;
  }

  Commands::Commands(INT_32 id, INT_32 time, const AgentCommandList& commands) : m_id(id), m_time(time) {
	m_commands.reserve(commands.size());
	m_commands.insert(m_commands.end(),commands.begin(),commands.end());
	m_delete = false;	
  }
  
  Commands::Commands(InputBuffer& in) {
	m_delete = false;	
	decode(in);
  }

  Commands::~Commands() {
	deleteObjects();
  }

  void Commands::deleteObjects() {
	if (m_delete) {
	  // Delete all the objects
	  for (AgentCommandList::iterator it = m_commands.begin();it!=m_commands.end();++it) {
		delete *it;
	  }
	  m_delete = false;
	}
	m_commands.clear();
  }

  Header Commands::getType() const {
	return COMMANDS;
  }
  
  void Commands::encode(OutputBuffer& out) const {
	Command::encode(out);
        out.writeInt32(m_id);
	out.writeInt32(m_time);
	// Write all commands of each type
        out.writeInt32(m_commands.size());
	for (AgentCommandList::const_iterator it = m_commands.begin();it!=m_commands.end();++it) {
	  const AgentCommand* next = *it;
	  out.writeInt32(next->getType());
	  Cursor base = out.writeInt32(0);
	  next->encode(out);
	  out.writeSize(base);
	}
  }

  void Commands::decode(InputBuffer& in) {
	Command::decode(in);
        m_id = in.readInt32();
	m_time = in.readInt32();
	// Delete any old objects
	deleteObjects();
        INT_32 count = in.readInt32();
        //        LOG_DEBUG("Reading %d commands", count);
        for (INT_32 i = 0; i < count; ++i) {
          Header header = (Header)in.readInt32();
          //          LOG_DEBUG("Command %d is %d", i, header);
	  int size = in.readInt32();
	  Command* next = decodeCommand(header,size,in);
	  if (next) {
		AgentCommand* a = dynamic_cast<AgentCommand*>(next);
		if (a) m_commands.push_back(a);
		else delete next;
	  }
	}
	m_delete = true;
  }

  Command* Commands::clone() const {
	if (m_delete) {
	  // Need to clone objects as well
	  AgentCommandList clonedObjects;
	  for (AgentCommandList::const_iterator it = m_commands.begin();it!=m_commands.end();++it) {
		clonedObjects.push_back(dynamic_cast<AgentCommand*>((*it)->clone()));
	  }
	  Commands* result = new Commands(m_id,m_time,clonedObjects);
	  result->m_delete = true;
	  return result;
	}
	return new Commands(m_id,m_time,m_commands);
  }

  INT_32 Commands::getID() const {
	return m_id;
  }

  INT_32 Commands::getTime() const {
	return m_time;
  }

  const AgentCommandList& Commands::getCommands() const {
	return m_commands;
  }

  Update::Update(INT_32 id, INT_32 time, const ObjectSet& objects) : m_id(id), m_time(time) {
	m_objects.insert(objects.begin(),objects.end());
	m_delete = false;	
  }

  Update::Update(InputBuffer& in) {
	m_delete = false;	
	decode(in);
  }

  Update::~Update() {
	deleteObjects();
	// Delete all the objects
  }

  void Update::deleteObjects() {
	if (m_delete) {
	  // Delete all the objects
	  for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
		delete *it;
	  }
	  m_delete = false;
	}
	m_objects.clear();
  }

  Header Update::getType() const {
	return UPDATE;
  }

  void Update::encode(OutputBuffer& out) const {
	Command::encode(out);
	//	LOG_DEBUG("Writing update for time %d",m_time);
        out.writeInt32(m_id);
	out.writeInt32(m_time);
	out.writeObjects(m_objects);
  }

  void Update::decode(InputBuffer& in) {
	Command::decode(in);
	// Delete any old objects
	deleteObjects();
        m_id = in.readInt32();
	m_time = in.readInt32();
	in.readObjects(m_time,m_objects);
	m_delete = true;
  }

  Command* Update::clone() const {
	if (m_delete) {
	  // Need to clone objects as well
	  ObjectSet clonedObjects;
	  for (ObjectSet::const_iterator it = m_objects.begin();it!=m_objects.end();++it) {
		clonedObjects.insert((*it)->clone());
	  }
	  Update* result = new Update(m_id,m_time,clonedObjects);
	  result->m_delete = true;
	  return result;
	}
	return new Update(m_id,m_time,m_objects);
  }

  INT_32 Update::getID() const {
	return m_id;
  }
  
  INT_32 Update::getTime() const {
	return m_time;
  }
  
  const ObjectSet& Update::getObjects() const {
	return m_objects;
  }

  KernelUpdate::KernelUpdate(Id id, INT_32 time, const ObjectSet& objects) : m_id(id), m_time(time) {
	m_objects.insert(objects.begin(),objects.end());
	m_delete = false;	
  }

  KernelUpdate::KernelUpdate(InputBuffer& in) {
	m_delete = false;	
	decode(in);
  }

  KernelUpdate::~KernelUpdate() {
	deleteObjects();
	// Delete all the objects
  }

  void KernelUpdate::deleteObjects() {
	if (m_delete) {
	  // Delete all the objects
	  for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
		delete *it;
	  }
	  m_delete = false;
	}
	m_objects.clear();
  }

  Header KernelUpdate::getType() const {
	return SK_UPDATE;
  }

  void KernelUpdate::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_id);
	out.writeInt32(m_time);
	out.writeObjects(m_objects);
  }

  void KernelUpdate::decode(InputBuffer& in) {
	Command::decode(in);
	// Delete any old objects
	deleteObjects();
	m_id = in.readInt32();
	m_time = in.readInt32();
	in.readObjects(m_time,m_objects);
	m_delete = true;
  }

  Command* KernelUpdate::clone() const {
	if (m_delete) {
	  // Need to clone objects as well
	  ObjectSet clonedObjects;
	  for (ObjectSet::const_iterator it = m_objects.begin();it!=m_objects.end();++it) {
		clonedObjects.insert((*it)->clone());
	  }
	  KernelUpdate* result = new KernelUpdate(m_id,m_time,clonedObjects);
	  result->m_delete = true;
	  return result;
	}
	return new KernelUpdate(m_id,m_time,m_objects);
  }

  INT_32 KernelUpdate::getTime() const {
	return m_time;
  }

  Id KernelUpdate::getId() const {
	return m_id;
  }

  const ObjectSet& KernelUpdate::getObjects() const {
	return m_objects;
  }
};
