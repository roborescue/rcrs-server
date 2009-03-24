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
   Converted original Robocup Rescue software into librescue
*/

#ifndef RESCUE_UDP_H
#define RESCUE_UDP_H

#include "connection.h"
#include "common.h"
#include "input.h"
#include "output.h"
#include <string>
#include <netinet/in.h>
#include <time.h>

#define LONGUDP_DEFAULT_PACKET_SIZE 1024
// Default timeout is 60 seconds
#define LONGUDP_DEFAULT_TIMEOUT 60
// Default wait between packets is 1ms
#define LONGUDP_DEFAULT_WAIT 1

namespace Librescue {
  class LongUDPConnection : public Connection {
  private:
	struct Fragment {
	  INT_16 id;
	  INT_16 index;
	  INT_16 total;
	  Byte* data;
	  int size;

	  Fragment() {
		data = 0;
	  }

	  void fill(INT_16 id_, INT_16 index_, INT_16 total_, Byte* buffer_, int size_) {
		id = id_;
		index = index_;
		total = total_;
		data = new Byte[size_];
		memcpy(data,buffer_,size_);
		size = size_;
	  }

	  ~Fragment() {
		if (data) delete[] data;
	  }
	};

	struct PartialPacket {
	  Fragment* fragments;
	  int totalFragments;
	  int received;
	  time_t lastTime;
	  INT_16 id;
	  Address from;
	  int totalSize;

	  PartialPacket(INT_16 id_, int total, Address from_) : from(from_) {
		id = id_;
		totalFragments = total;
		fragments = new Fragment[total];
		received = 0;
		totalSize = 0;
	  }

	  ~PartialPacket() {
		delete[] fragments;
	  }
	};

	typedef std::list<PartialPacket*> PartialPackets;
	PartialPackets partialPackets;
	std::list<Bytes*> finishedPackets;
	std::list<Address> finishedFrom;
	int m_packetSize;
	time_t m_timeout;
	int m_wait;
	int m_socket;
	Address m_target;
	Address m_from;
	int m_nextID;
	mutable char name[1024];

	// Reads packets from the socket and processes them until there is nothing left to read.
	void readUDP(int timeout);

	void init(const Address& localAddress, int packetSize, int wait, time_t timeout);

  public:
	//	LongUDPConnection(Address& localAddress);
	//	LongUDPConnection(Address& localAddress, int packetSize);
	LongUDPConnection(const char* localHost = "localhost", int localPort = 0, int packetSize = LONGUDP_DEFAULT_PACKET_SIZE, int wait = LONGUDP_DEFAULT_WAIT, time_t timeout = LONGUDP_DEFAULT_TIMEOUT);
	LongUDPConnection(const Address& localAddress, int packetSize = LONGUDP_DEFAULT_PACKET_SIZE, int wait = LONGUDP_DEFAULT_WAIT, time_t timeout = LONGUDP_DEFAULT_TIMEOUT);
	virtual ~LongUDPConnection();

	virtual bool send(const Bytes& buffer, const Address& address);
	virtual bool send(const Bytes& buffer);
	virtual void receive(Bytes& buffer);
	virtual bool isDataAvailable(int timeout);
	// Get the Address that the last message was received from
	virtual const Address& addressReceivedFrom() const;

	virtual void close();
	virtual bool isOpen();

	virtual const char* toString() const;

	void setPacketSize(int size);
	void setWait(int wait);
	void setTimeout(int timeout);
	void setTarget(const Address& address);
  };
}

#endif
