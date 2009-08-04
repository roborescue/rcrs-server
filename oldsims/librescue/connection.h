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

#ifndef RESCUE_CONNECTION_H
#define RESCUE_CONNECTION_H

#include "common.h"
#include <string>
#include <netinet/in.h>
#include <vector>

namespace Librescue {
  class Address {
  private:
	u_int16_t m_port;
	u_int32_t m_address;
	mutable char name[256];

  public:
	Address(const char* hostname, int port);
	// The host and port are in network byte order.
	Address(int host, int port);
	Address(const Address& rhs);
	Address(struct sockaddr_in address);
	Address();
	virtual ~Address();

	u_int16_t port() const;
	u_int32_t address() const;

	void fill(struct sockaddr_in* address) const;
	const char* toString() const;

	bool operator== (const Address& rhs) const;
	bool operator< (const Address& rhs) const;
  };

  class Connection {
  private:
	// No copying allowed
	Connection(const Connection& rhs);
	Connection& operator=(const Connection& rhs);

  protected:
	Connection();

  public:
	virtual ~Connection();

	virtual const char* toString() const = 0;

	virtual bool send(const Bytes& buffer) = 0;
	virtual void receive(Bytes& buffer) = 0;
	virtual bool isDataAvailable(int timeout) = 0;

	virtual void close() = 0;
	virtual bool isOpen() = 0;

	// Get the Address that the last message was received from
	virtual const Address& addressReceivedFrom() const = 0;
  };
}

#endif
