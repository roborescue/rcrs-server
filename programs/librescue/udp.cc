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

#include "udp.h"
#include "error.h"
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/poll.h>
#include <time.h>
#include <string>
#include <arpa/inet.h>
#include <errno.h>

#define MAGIC 0x0008

namespace Librescue {
  LongUDPConnection::LongUDPConnection(const char* localHost, int localPort, int packetSize, int wait, time_t timeout) {
	Address address(localHost,localPort);
	init(address,packetSize,wait,timeout);
  }

  LongUDPConnection::LongUDPConnection(const Address& address, int packetSize, int wait, time_t timeout) {
	init(address,packetSize,wait,timeout);
  }

  void LongUDPConnection::init(const Address& address, int packetSize, int wait, time_t timeout) {
	m_packetSize = packetSize;
	m_wait = wait;
	m_timeout = timeout;
	m_nextID = 0;
	m_target = address;
	m_socket=socket(PF_INET,SOCK_DGRAM,0);
	if (address.port()>0) {
	  // Bind the socket so we can receive packets
	  struct sockaddr_in local;
	  address.fill(&local);
	  if (bind(m_socket,(struct sockaddr*)&local,sizeof(local))) {
		shutdown(m_socket,SHUT_RDWR);
		m_socket = 0;
		throw std::string("Bind error creating UDP socket");
	  }
	}
  }

  LongUDPConnection::~LongUDPConnection() {
	close();
  }

  void LongUDPConnection::setTarget(const Address& address) {
	m_target = address;
  }

  const Address& LongUDPConnection::addressReceivedFrom() const {
	return m_from;
  }

  bool LongUDPConnection::send(const Bytes& out, const Address& address) {
	Address old = m_target;
	m_target = address;
	bool result = send(out);
	m_target = old;
	return result;
  }

  bool LongUDPConnection::send(const Bytes& out) {
	struct sockaddr_in to;
	m_target.fill(&to);
	// Break the message up into fragments
	//	LOG_DEBUG("Sending %d bytes",out.size());
	//	logDebug(errorBuffer);
	//	LOG_DEBUG("Fragmenting message: %d bytes, max packet size: %d",out.size(),m_packetSize);
	//	logDebug(msg);
	int start = 0;
	int remaining = out.size();
	int sequenceNumber = 0;
	int total = (remaining/(m_packetSize-8))+1;
	if (remaining%(m_packetSize-8)==0) --total;
	char* buffer = new char[m_packetSize];
	int id = m_nextID++;
	//	dumpBytes(out);
	int sum = 0;
	//	LOG_DEBUG("Sending message to %s - %d bytes",m_target.toString(),remaining);
	//	logDebug(errorBuffer);
	while (remaining>0) {
	  // Magic number
	  buffer[0] = 0;
	  buffer[1] = 0x08;
	  // ID
	  buffer[2] = (id>>8)&0xFF;
	  buffer[3] = id&0xFF;
	  // Sequence number
	  buffer[4] = (sequenceNumber>>8)&0xFF;
	  buffer[5] = sequenceNumber&0xFF;
	  // Total number
	  buffer[6] = (total>>8)&0xFF;
	  buffer[7] = total&0xFF;
	  // Now the data
	  int length = remaining>m_packetSize-8?m_packetSize-8:remaining;
	  memcpy(&buffer[8],&out[start],length); // This is naughty! It depends on Bytes being a std::vector
	  start += length;
	  remaining -= length;
	  int toSend = length+8;
	  while (toSend > 0) {
		int result = sendto(m_socket,buffer,toSend,0,(const sockaddr*)&to,sizeof(to));
		if (result < 0) {
		  // Error
		  if (errno==EINTR) continue;
		  LOG_ERROR("Error sending packet: %s (sendto returned %d)",strerror(errno),result);
		  return false;
		}
		toSend -= result;
		// Let's put in a delay to stop the output buffer on the network interface overflowing
		if (remaining > 0 && m_wait>0) usleep(m_wait*1000);
	  }
	  sum += length;
	  ++sequenceNumber;
	}
	delete[] buffer;
	return true;
  }

  void LongUDPConnection::receive(Bytes& in) {
	// Pull a message out if one exists
	in.clear();
	if (m_socket==0 || finishedPackets.empty()) {
	  return;
	}
	else {
	  Bytes* first = finishedPackets.front();
	  m_from = finishedFrom.front();
	  finishedPackets.pop_front();
	  finishedFrom.pop_front();
	  in.insert(in.begin(),first->begin(),first->end());
	  delete first;
	}
  }

  bool LongUDPConnection::isDataAvailable(int timeout) {
	if (m_socket==0) return false;
	if (!finishedPackets.empty()) return true;
	readUDP(timeout);
	return !finishedPackets.empty();
  }

  void LongUDPConnection::close() {
	if (m_socket) {
	  shutdown(m_socket,SHUT_RDWR);
	  m_socket = 0;
	}
  }

  bool LongUDPConnection::isOpen() {
	return m_socket!=0;
  }

  void LongUDPConnection::readUDP(int timeout) {
	// Read until there is nothing else to read
	//	LOG_DEBUG("LongUDPConnection::readUDP(%d)",timeout);
	//	snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Read buffer size: %d",m_packetSize+8);
	//	logDebug(errorBuffer);
	Byte data[m_packetSize+8]; // FIXME: Should use ioctl SIOCINQ to find out how big the next packet is (or recvfrom with MSG_TRUNC | MSG_PEEK)
	struct sockaddr_in from;
	socklen_t s = sizeof(struct sockaddr_in);
	pollfd pollRequest;	
	pollRequest.fd = m_socket;
	pollRequest.events = POLLIN;
	pollRequest.revents = 0;
	//	logDebug("Polling");
	while (poll(&pollRequest,1,timeout)>0) {
	  //	  int available = recvfrom(m_socket,&data[0],m_packetSize+8,MSG_TRUNC | MSG_PEEK,0,0);
	  //	  snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"%d bytes available",available);
	  //	  logDebug(errorBuffer);
	  int size = recvfrom(m_socket,&data[0],m_packetSize+8,0,(struct sockaddr*)&from,&s);
	  //	  char msg[256];
	  //	  LOG_DEBUG("Received %d bytes",size);
	  //	  logDebug(errorBuffer);
	  if (size<0) break;
	  if (size<8) {
		LOG_DEBUG("Undersized packet received: length=%d. Throwing it back.",size);
		continue; // If we didn't receive a full LongUDP packet header then it must be a bad packet - we can't wait for more data, because UDP packets either arrive complete or not at all.
	  }
	  INT_16 magic = data[0]<<8 | data[1];
	  INT_16 id = data[2]<<8 | data[3];
	  INT_16 segment = data[4]<<8 | data[5];
	  INT_16 total = data[6]<<8 | data[7];
	  if (magic != MAGIC) {
		LOG_DEBUG("Bad magic number");
		continue; // Bad magic number. Discard and continue.
	  }
	  Address fromAddress(from.sin_addr.s_addr, from.sin_port);
	  // Do we already have a PartialPacket with this id and address?
	  PartialPacket* packet = 0;
	  PartialPackets::iterator it;
	  //	  snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Received %d bytes of id %d from %s",size,id,fromAddress.toString());
	  //	  logDebug(errorBuffer);
	  //	  logDebug("Looking for existing PartialPacket. Packets currently in progress:");
	  //	  for (it = partialPackets.begin();it!=partialPackets.end();++it) {
	  //		PartialPacket* next = *it;
	  //		snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Packet %d from %s",next->id,next->from.toString());
	  //		logDebug(errorBuffer);
	  //	  }
	  //	  snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"&partialPackets = %p",&partialPackets);
	  //	  logDebug(errorBuffer);
	  for (it = partialPackets.begin();it!=partialPackets.end();++it) {
		PartialPacket* next = *it;
		if (next->id == id && next->from==fromAddress) {
		  //		  logDebug("Found partial packet");
		  packet = next;
		  break;
		}
		// Has this packet been sitting around too long?
		time_t now = time(0);
		if (next->lastTime+m_timeout < now) {
		  // Too long. Kill it.
		  LOG_DEBUG("LongUDP timeout: packet %d from %s",next->id,next->from.toString());
		  it = partialPackets.erase(it);
		  --it; // Compensate for the ++it that will happend when we finish the loop
		  delete next;
		}
	  }
	  //	  logDebug("Finished checking for existing packet");
	  if (!packet) { // Create  the PartialPacket if it doesn't already exist
		//		snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Creating new packet: id %d from %s",id,fromAddress.toString());
		//		logDebug(errorBuffer);
		packet = new PartialPacket(id,total,fromAddress);
		partialPackets.push_front(packet);
		it = partialPackets.begin();
	  }
	  //	  logDebug("Checking for existing data");
	  // OK, we now have a PartialPacket. Add this fragment. "it" should point to the index of the packet in partialPackets.
	  if (!packet->fragments[segment].data) { // Did we already have some data for this fragment?
		if (total > 10) printf("%d of %d\n",segment,total);
		//		snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Filling fragment: segment %d of %d, %d bytes",segment,total,size-8);
		//		logDebug(errorBuffer);
		//		dumpBytes((const char*)&data[8],size-8);
		packet->fragments[segment].fill(id,segment,total,&data[8],size-8);
		packet->totalSize += size;
		//		logDebug("Updating received count");
		++packet->received;
		//		logDebug("Timestamp");
		time(&packet->lastTime);
		//		logDebug("Checking if packet is finished");
		// Is this packet now finished?
		if (packet->received == packet->totalFragments) {
		  // Yes. Copy the data into finishedPackets
		  Bytes* complete = new Bytes();
		  complete->reserve(packet->totalSize);
		  for (int i=0;i<packet->totalFragments;++i) {
			Byte* next = packet->fragments[i].data;
			int nextSize = packet->fragments[i].size;
			complete->insert(complete->end(),&next[0],&next[nextSize]);
			//			snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Inserted %d bytes from fragment %d. Complete size=%d, capacity=%d",nextSize,i,complete->size(),complete->capacity());
			//			logDebug(errorBuffer);
			//			dumpBytes(next,nextSize);
			//			dumpBytes(*complete);
			//			for (int j=0;j<packet->fragments[i].size;++j) {
			//			  complete->push_back(packet->fragments[i].data[j]);
			//			}
		  }
		  //		  snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Finished packet %d from %s",packet->id,packet->from.toString());
		  //		  logDebug(errorBuffer);
		  finishedPackets.push_back(complete);
		  finishedFrom.push_back(fromAddress);
		  //		  logDebug("Erasing partial packet");
		  partialPackets.erase(it);
		  delete packet;
		  //		  logDebug"All done");
		}
	  }
	  else {
		LOG_WARNING("WARNING: Already have data for fragment %d/%d of message %d from %s!",segment,packet->totalFragments,packet->id,packet->from.toString());
	  }
	}
	//	LOG_DEBUG("Finished reading UDP");
  }

  void LongUDPConnection::setWait(int wait) {
	m_wait = wait;
  }

  // FIXME: Make this thread-safe
  void LongUDPConnection::setPacketSize(int size) {
	m_packetSize = size;
  }

  void LongUDPConnection::setTimeout(int timeout) {
	m_timeout = timeout;
  }

  const char* LongUDPConnection::toString() const {
	//	if (name[0]==0) {
	  snprintf(name,1024,"LongUDPConnection: target=%s, packetSize=%d, timeout=%d, wait=%d, input queue size=%zd",m_target.toString(),m_packetSize,(int)m_timeout,m_wait,finishedPackets.size());
	  //	}
	return name;
  }
}
