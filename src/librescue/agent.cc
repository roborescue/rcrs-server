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

#include "agent.h"
#include "common.h"
#include "objectpool.h"
#include "objects.h"
#include "command.h"
#include "container.h"
#include "error.h"
#include <stdlib.h>
#include <string.h>

namespace Librescue {
  Agent::Agent(Container& container) : m_container(container) {}

  Agent::~Agent() {}

  int Agent::init(Config* config, ArgList& args) {
	m_config = config;
	return 0;
  }

  void Agent::cleanup() {}

  int Agent::connected(const AgentConnectOK& connect) {
	m_pool.update(connect.getObjects());
	RescueObject* self = connect.getSelf();
	m_pool.addObject(self->clone());
	m_id = connect.getId();
	LOG_DEBUG("Connected. My id is %d, type is %d",m_id,self->type());
	return 0;
  }

  void Agent::update(INT_32 time, const ObjectSet& changed) {
	m_pool.update(changed);
	m_time = time;
  }

  void Agent::sense(INT_32 time, const IdSet& changed) {
  }

  void Agent::hear(Header type, Id from, const Bytes& data) {
  }

  Id Agent::getId() const {
	return m_id;
  }

  void Agent::sendMove(const IdList& path) const {
	MoveCommand move(m_id,path);
	sendAgentCommand(&move);
  }

  void Agent::sendRest() const {
	RestCommand rest(m_id);
	sendAgentCommand(&rest);
  }

  void Agent::sendLoad(Id target) const {
	LoadCommand load(m_id,target);
	sendAgentCommand(&load);
  }

  void Agent::sendUnload() const {
	UnloadCommand unload(m_id);
	sendAgentCommand(&unload);
  }

  void Agent::sendRescue(Id target) const {
	RescueCommand rescue(m_id,target);
	sendAgentCommand(&rescue);
  }

  void Agent::sendClear(Id target) const {
	ClearCommand clear(m_id,target);
	sendAgentCommand(&clear);
  }

  void Agent::sendExtinguish(Id target, int amount) const {
	int x,y;
	if (m_pool.locate(m_id,&x,&y)) {
	  ExtinguishCommand ex(m_id,target,0,x,y,amount);
	  sendAgentCommand(&ex);
	}
  }

  void Agent::sendSay(const Bytes& data) const {
	SayCommand say(m_id,data);
	sendAgentCommand(&say);
  }

  void Agent::sendTell(const Bytes& data, Byte channel) const {
	TellCommand tell(m_id,data, channel);
	sendAgentCommand(&tell);
  }

  void Agent::sendAgentCommand(const AgentCommand* command) const {
	m_container.sendAgentCommand(command);
  }

  RescueObject* Agent::me() {
	return m_pool.getObject(m_id);
  }

  RescueObject* Agent::getLocation() {
	MovingObject* moving = dynamic_cast<MovingObject*>(me());
	if (moving)	return m_pool.getObject(moving->getPosition());
	else return me();
  }
}
