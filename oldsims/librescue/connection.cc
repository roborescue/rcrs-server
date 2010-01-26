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

#include "connection.h"
#include "error.h"
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>

namespace Librescue {
  Connection::Connection() {
  }

  Connection::~Connection() {
  }

  Address::Address(const char* host, int port) {
	m_port = htons(port);
	if (host) {
	  struct hostent* hostent = gethostbyname(host);
	  struct in_addr* address = (struct in_addr*)hostent->h_addr_list[0];
	  m_address = address->s_addr;
	}
	else {
	  m_address = INADDR_ANY;
	}
	name[0] = 0;
  }

  Address::Address(int host, int port) {
	m_port = port;
	m_address = host;
	name[0] = 0;
  }

  Address::Address(const Address& rhs) {
	m_port = rhs.m_port;
	m_address = rhs.m_address;
	name[0] = 0;
  }

  Address::Address(struct sockaddr_in address) {
	m_port = address.sin_port;
	m_address = address.sin_addr.s_addr;
	name[0] = 0;
  }

  Address::Address() {
	m_port = 0;
	m_address = 0;
	name[0] = 0;
  }

  Address::~Address() {
  }

  const char* Address::toString() const {
	if (name[0]==0) {
	  struct in_addr address;
	  address.s_addr = m_address;
	  snprintf(name,256,"%s:%d",inet_ntoa(address),ntohs(m_port));
	}
	return name;
  }

  bool Address::operator==(const Address& rhs) const {
	return m_port==rhs.m_port && m_address==rhs.m_address;
  }

  bool Address::operator< (const Address& rhs) const {
	if (m_address != rhs.m_address)
	  return m_address < rhs.m_address;
	return m_port < rhs.m_port;
  }

  void Address::fill(struct sockaddr_in* address) const {
	address->sin_family = AF_INET;
	address->sin_port = m_port;
	address->sin_addr.s_addr = m_address;
  }

  u_int16_t Address::port() const {
	return m_port;
  }

  u_int32_t Address::address() const {
	return m_address;
  }
}
