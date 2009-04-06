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
   Wrote TCP implementation

 Cameron Skinner
   Included TCP in librescue
*/

#ifndef RESCUE_TCP_H
#define RESCUE_TCP_H

#include <string>
#include <list>
#include <pthread.h>
#include "input.h"
#include "output.h"
#include "connection.h"

namespace Librescue
{
  /*
  typedef struct AcceptInfo {
	struct sockaddr_in from;
	int socket;
  } AcceptInfo;
  */

  class TcpConnection : public Connection
	{
	private:
	  // No copying allowed
	  TcpConnection(const TcpConnection& rhs); 
	  void operator=(const TcpConnection& rhs);

	  void init(const Address* target);
	  bool receiveData(Byte* buffer, int length);
	  mutable char name[256];

	protected:
	  int m_socketDescriptor;
	  Address m_from;
	  Address m_local;

	public:
	  TcpConnection(const char* host, int port);
	  TcpConnection(const Address& target);
	  TcpConnection(const Address& from, int socketDescriptor);
	  virtual ~TcpConnection();

	  virtual bool send(const Bytes& buffer);
	  virtual void receive(Bytes& buffer);
	  virtual bool isDataAvailable(int timeout);
	  virtual void close();
	  virtual bool isOpen();

	  // Get the Address that the last message was received from
	  virtual const Address& addressReceivedFrom() const;

	  virtual const char* toString() const;
	};

  class TcpServer {
  public:
	class TCPConnectionCallback {
	public:
	  TCPConnectionCallback() {}
	  virtual ~TCPConnectionCallback() {}

	  virtual void incomingTCPConnection(TcpConnection* connection, const Address& from) = 0;
	};

  private:
	int m_socket;
	pthread_t m_listenThread;
	//	pthread_mutex_t m_listenLock;
	pthread_mutex_t m_runLock;
	bool running;
	TCPConnectionCallback& m_callback;

	void loop();
	static void* threadFunc(void* server);

	// No copying allowed
	TcpServer(const TcpServer& rhs);
	void operator=(const TcpServer& rhs);

	//  protected:
	//	std::list<AcceptInfo> accepted;

  public:
	TcpServer(int listenPort,TCPConnectionCallback& callback);
	virtual ~TcpServer();

	void start();
	void stop();

	//	bool hasConnections();
	// Gets a newly allocated TcpConnection, or NULL if there are no connections waiting
	//	TcpConnection* getConnection();
  };
}

#endif

