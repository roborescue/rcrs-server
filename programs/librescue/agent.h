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

#ifndef RESCUE_AGENT_H
#define RESCUE_AGENT_H

#include "common.h"
#include "command.h"
#include "args.h"
#include "objectpool.h"
#include <stdio.h>

namespace Librescue {
  class Container;
  class Config;

  class Agent {
  private:
	Container& m_container;
	
  protected:
	Agent(Container& container);

	Config* m_config;
	ObjectPool m_pool;
	Id m_id;
	INT_32 m_time;

	void sendMove(const IdList& path) const;
	void sendRest() const;
	void sendLoad(Id target) const;
	void sendUnload() const;
	void sendRescue(Id target) const;
	void sendClear(Id target) const;
	void sendExtinguish(Id target, int amount) const;
	void sendSay(const Bytes& data) const;
	void sendTell(const Bytes& data, Byte channel) const;
	void sendAgentCommand(const AgentCommand* command) const;

 	RescueObject* me();
	RescueObject* getLocation();

  public:
	virtual ~Agent();

	// Initialise the agent. Return zero on success.
	virtual int init(Config* config, ArgList& args);
	// Do any cleanup required at the end of the simulation
	virtual void cleanup();

	virtual int connected(const AgentConnectOK& connect);

	virtual void sense(INT_32 time, const IdSet& changed);
	virtual void hear(Header type, Id from, const Bytes& bytes);

	virtual AgentType getAgentType() const = 0;

	void update(INT_32 time, const ObjectSet& changed);

	Id getId() const;
  };
}

#endif
