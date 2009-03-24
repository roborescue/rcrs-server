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

 Arash Rahimi
   Wrote original TCP implementation

 Cameron Skinner
   Included TCP in librescue
*/

#include "tcp.h"
#include "error.h"
#include <iostream>
#include <sys/poll.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <cstdio>
#include <fcntl.h>
#include <errno.h>

#define MAX_TCP_SEND 4000

using namespace std;

namespace Librescue {
  TcpConnection::TcpConnection(const char* host, int port) {
	Address address(host,port);
	init(&address);
  }

  TcpConnection::TcpConnection(const Address& target) {
	init(&target);
  }


  TcpConnection::TcpConnection(const Address& from, int socketDescriptor) : m_socketDescriptor(socketDescriptor)
  {
	m_from = from;
	init(NULL);
  }

  TcpConnection::~TcpConnection()
  {
	close();
  }

  void TcpConnection::init(const Address* target) {
	if (target) {
	  m_socketDescriptor = socket(PF_INET,SOCK_STREAM,0);
	  if (m_socketDescriptor==-1) {
		LOG_ERROR("TcpConnection::init: Could not create TCP socket: %s",strerror(errno));
		throw("Could not init TCP connection");
	  }
	  struct linger l = {1, 0};
	  if (setsockopt(m_socketDescriptor, SOL_SOCKET, SO_LINGER, &l, sizeof(linger)) == -1) {
	  	LOG_ERROR("TcpConnection::init: I cannot set SO_LINGER: %s",strerror(errno));
		throw("Could not init TCP connection");
	  }
	  int reuse = 1;
	  if (setsockopt(m_socketDescriptor,SOL_SOCKET,SO_REUSEADDR,&reuse,sizeof(reuse)) == -1) {
		LOG_ERROR("TcpConnection::init: I cannot set SO_REUSEADDR: %s",strerror(errno));
		throw("Could not init TCP connection");
	  }
	  struct timeval timeout = {1,0};
	  if (setsockopt(m_socketDescriptor,SOL_SOCKET,SO_SNDTIMEO,&timeout,sizeof(timeout)) == -1) {
		LOG_ERROR("TcpConnection::init: I cannot set SO_SNDTIMEO: %s",strerror(errno));
		throw("Could not init TCP connection");
	  }
	  sockaddr_in sockStruct;
	  memset(&sockStruct,0,sizeof(sockStruct));
	  target->fill(&sockStruct);
	  if (connect(m_socketDescriptor, (sockaddr *) &sockStruct, sizeof(sockStruct))) {
		LOG_ERROR("TcpConnection::init: Could not connect to server %s: %s",target->toString(),strerror(errno));
		close();
		throw string("Error connecting to server");
	  }
	  m_from = *target;
	}
	sockaddr_in sockStruct;
	int localLength = sizeof(sockStruct);
	getsockname(m_socketDescriptor,(sockaddr*)&sockStruct,(socklen_t*)&localLength);
	Address local(sockStruct);
	m_local = local;
  }

  void TcpConnection::receive(Bytes& result)
  {
	Byte buf[4];
	result.clear();
	if (!receiveData(buf,4)) {
	  close();
	  return;
	}
	// Decode the size big-endian style
	unsigned int length = (buf[0] << 24) + (buf[1]<<16) + (buf[2]<<8) + (buf[3]);

	result.reserve(length);

	Byte *buffer = new Byte[length];
	if (receiveData(buffer,length)) {
	  for (unsigned int i = 0; i < length; i++)
		result.push_back(buffer[i]);
	}
	else {
	  result.clear();
	  close();
	}
	delete[] buffer;

	if (result.size()>0 && result.size()!=length) LOG_WARNING("WARNING: Message is too small: Received %d of %d bytes",result.size(),length);
  }

  bool TcpConnection::receiveData(Byte* buffer, int length) {
	if (m_socketDescriptor==0) return false;
	int total = 0;
	pollfd pollRequest;
	
	pollRequest.fd = m_socketDescriptor;
	pollRequest.events = POLLIN | POLLERR | POLLHUP | POLLNVAL;
	pollRequest.revents = 0;
	
	while (total < length) {
	  int result = poll(&pollRequest, 1, 100);
	  //	  LOG_DEBUG("Poll returned %d, pollRequest.revents=0x%d",result,pollRequest.revents);
	  if (result < 0) {
		LOG_ERROR("Error reading TCP data: %s",strerror(errno));
		return false;
	  }
	  if (pollRequest.revents & POLLERR) {
		LOG_ERROR("Poll error");
		return false;
	  }
	  if (pollRequest.revents & POLLHUP) {
		LOG_ERROR("Poll hangup");
		return false;
	  }
	  if (pollRequest.revents & POLLNVAL) {
		LOG_ERROR("Poll file descriptor not open");
		return false;
	  }
	  if (result > 0) {
		int amount = recv(m_socketDescriptor,buffer+total,length-total,0);
		// We should always read at least one byte unless there is an error - poll told us that there was data to read. A return of zero must mean EOF
		if (amount==0) {
		  LOG_ERROR("Unexpected EOF from %s",m_from.toString());
		  return false;
		}
		if (amount < 0) {
		  LOG_ERROR("Error receiving from %s: %s",m_from.toString(),strerror(errno));
		  return false;
		}
		total += amount;
	  }
	}
	return true;
  }

  bool TcpConnection::send(const Bytes &output) {
	if (m_socketDescriptor==0) return false;
	unsigned int size = output.size();
	unsigned int totalSent = 0;

	//	LOG_DEBUG("Sending %d bytes",size);

	// Send the size big-endian style
	Byte buf[4];
	buf[0] = (size>>24) & 0xFF;
	buf[1] = (size>>16) & 0xFF;
	buf[2] = (size>>8) & 0xFF;
	buf[3] = size & 0xFF;
	while (totalSent < 4) {
	  int sent = ::send(m_socketDescriptor, buf+totalSent, 4, MSG_NOSIGNAL);
	  if (sent < 0) {
		if (errno==EINTR) continue;
		return false;
	  }
	  totalSent += sent;
	}
	
	totalSent = 0;
	while (totalSent < size) {
	  unsigned int maxSend = size - totalSent;
	  if (maxSend > MAX_TCP_SEND) maxSend = MAX_TCP_SEND;
	  int sent = ::send(m_socketDescriptor, &*output.begin() + totalSent, maxSend, MSG_NOSIGNAL); // This is a bit naughty and depends on Bytes being a std::vector. Someone should fix this.
	  if (sent < 0) {
		if (errno==EINTR) continue;
		return false;
	  }
	  totalSent += sent;
	  //	  LOG_DEBUG("Sent %d of %d bytes",totalSent,size);
	}
	return true;
  }

  bool TcpConnection::isDataAvailable(int timeout)
  {
	if (m_socketDescriptor==0) return false;
	pollfd pollRequest;
	
	pollRequest.fd = m_socketDescriptor;
	pollRequest.events = POLLIN;
	pollRequest.revents = 0;
	
	if (poll(&pollRequest, 1, timeout) > 0) {
	  // Check if there really is data available
	  char c;
	  int amount = recv(m_socketDescriptor,&c,1,MSG_PEEK);
	  if (amount>0) return true;
	  // Nothing really available. Must be EOF.
	  close();
	}
	return false;
  }

  void TcpConnection::close() {
	if (m_socketDescriptor) {
	  //	  LOG_DEBUG("Closing TCP connection to %s",m_from.toString());
	  if (shutdown(m_socketDescriptor,SHUT_RDWR)) {
		if (errno!=ENOTCONN) {
		  LOG_ERROR("Error closing socket: %s",strerror(errno));
		}
	  }
	  if (::close(m_socketDescriptor)) {
		LOG_ERROR("Error closing socket: %s",strerror(errno));
	  }
	  m_socketDescriptor = 0;
	}
  }

  bool TcpConnection::isOpen() {
	return m_socketDescriptor!=0;
  }

  const Address& TcpConnection::addressReceivedFrom() const {
	return m_from;
  }

  const char* TcpConnection::toString() const {
	//	if (name[0]==0) {
	  snprintf(name,256,"TcpConnection: local address=%s, remote address=%s, socket=%d",m_local.toString(),m_from.toString(),m_socketDescriptor);
	  //	}
	return name;
  }

  TcpServer::TcpServer(int listenPort, TCPConnectionCallback& callback) : m_callback(callback) {
	// Initialise the listening socket
	m_socket = socket(PF_INET,SOCK_STREAM,0);
	if (m_socket==-1) {
	  LOG_ERROR("Could not create TCP server socket: %s",strerror(errno));
	  throw string("Could not open TCP server socket");
	}
	struct linger l = {1, 0};
	if (setsockopt(m_socket, SOL_SOCKET, SO_LINGER, &l, sizeof(linger)) == -1) {
	  LOG_ERROR("HEY! I cannot set SO_LINGER: %s",strerror(errno));
	  throw("Could not open TCP server socket");
	}
	int reuse = 1;
	if (setsockopt(m_socket,SOL_SOCKET,SO_REUSEADDR,&reuse,sizeof(reuse)) == -1) {
	  LOG_ERROR("HEY! I cannot set SO_REUSEADDR: %s",strerror(errno));
	  throw("Could not open TCP server socket");
	}
	sockaddr_in sockStruct;
	memset(&sockStruct,0,sizeof(sockStruct));
	sockStruct.sin_family = AF_INET;
	sockStruct.sin_addr.s_addr = htonl(INADDR_ANY);
	sockStruct.sin_port = htons(listenPort);
	if (::bind(m_socket, (sockaddr *) &sockStruct, sizeof(sockStruct))) {
	  logError("Couldn't bind on requested port");
	  throw string("Error binding on desired port");
	}
	// Make the socket non-blocking
	fcntl(m_socket,F_SETFL,O_NONBLOCK);
	// Init the mutexes
	//	pthread_mutex_init(&m_listenLock,NULL);
	pthread_mutex_init(&m_runLock,NULL);

	// Say what's going on
	int localLength = sizeof(sockStruct);
	getsockname(m_socket,(sockaddr*)&sockStruct,(socklen_t*)&localLength);
	Address local(sockStruct);
	LOG_INFO("Listening for TCP connections on %s",local.toString());
  }

  TcpServer::~TcpServer() {
	stop();
	// Close the socket
	if (shutdown(m_socket,SHUT_RDWR)) logError("Error closing socket");
	// Destroy the mutexes
	//	pthread_mutex_destroy(&m_listenLock);
	pthread_mutex_destroy(&m_runLock);
	// Destroy the thread if it still exists
	if (m_listenThread) pthread_cancel(m_listenThread);
  }

  void TcpServer::start() {
	pthread_mutex_lock(&m_runLock);
	running = true;
	pthread_mutex_unlock(&m_runLock);
	if (pthread_create(&m_listenThread,NULL,&TcpServer::threadFunc,(void*)this)) {
	  logError("Couldn't create TCP server thread");
	  throw ("Could not start TCP server");
	}
  }

  void TcpServer::stop() {
	pthread_mutex_lock(&m_runLock);
	running = false;
	pthread_mutex_unlock(&m_runLock);
	if (m_listenThread) {
	  pthread_join(m_listenThread,NULL);
	  m_listenThread = 0;
	}
  }

  void* TcpServer::threadFunc(void* args) {
	TcpServer* server = (TcpServer*)args;
	server->loop();
	return server;
  }

  void TcpServer::loop() {
	//	logDebug("TCP server running");
	// Start listening
	if (listen(m_socket,100)) {
	  LOG_ERROR("TCP server couldn't listen: %s",strerror(errno));
	  throw string("Couldn't start listening");
	}
	while (true) {
	  pthread_mutex_lock(&m_runLock);
	  bool finish = !running;
	  pthread_mutex_unlock(&m_runLock);
	  if (finish) return;
	  // Are there any connections pending?
	  pollfd pollRequest;	
	  pollRequest.fd = m_socket;
	  pollRequest.events = POLLIN;
	  pollRequest.revents = 0;
	  //	  logDebug("TCP server checking for incoming connection");
	  if (poll(&pollRequest,1,100)>0) {
		//		AcceptInfo info;
		struct sockaddr_in from;
		socklen_t size = sizeof(struct sockaddr_in);
		int socket = accept(m_socket,(struct sockaddr*)&from,&size);
		if (socket<0) logError("Error accepting connection");
		else {
		  Address fromAddress(from);
		  TcpConnection* result = new TcpConnection(fromAddress,socket);
		  //		  snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Incoming TCP connection from %s",fromAddress.toString());
		  //		  logDebug(errorBuffer);
		  m_callback.incomingTCPConnection(result,fromAddress);
		  //		  info.socket = socket;
		  //		  pthread_mutex_lock(&m_listenLock);
		  //		  accepted.push_back(info);
		  //		  pthread_mutex_unlock(&m_listenLock);
		}
	  }
	}
	//	logDebug("TCP server finished");
  }

  /*
  bool TcpServer::hasConnections() {
	pthread_mutex_lock(&m_listenLock);
	bool result = !accepted.empty();
	pthread_mutex_unlock(&m_listenLock);
	return result;
  }

  TcpConnection* TcpServer::getConnection() {
	TcpConnection* result = NULL;
	pthread_mutex_lock(&m_listenLock);
	if (!accepted.empty()) {
	  AcceptInfo socket = accepted.front();
	  accepted.pop_front();
	  Address from(socket.from);
	  try {
		result = new TcpConnection(from,socket.socket);
	  }
	  catch (std::bad_alloc& ex) {
		logError("std::bad_alloc in TcpServer::getConnection()");
	  }
	}
	pthread_mutex_unlock(&m_listenLock);
	return result;
  }
  */
}
