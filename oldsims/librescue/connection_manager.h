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

#ifndef CONNECTION_MANAGER_H
#define CONNECTION_MANAGER_H

#include "udp.h"
#include "tcp.h"
#include "connection.h"
#include <set>
#include <map>
#include <queue>
#include <list>

namespace Librescue {
  class ConnectionManager; // Forward declaration

  class Listener {
  protected:
	Connection* m_connection;
	ConnectionManager* m_manager;
	pthread_t m_thread;
	pthread_mutex_t m_lock;

	// Override this if the subclass needs to do something special (e.g. tell the connection manager whether it should send to the from address via TCP or UDP).
	virtual void handleInput(const Address& from, const Bytes& buffer) = 0;

  public:
	Listener(Connection* c, ConnectionManager* manager);
	virtual ~Listener();

	void start();
	void stop();
	void poll();

  private:
	bool m_running;

	Listener(const Listener& l);
	Listener& operator=(const Listener& rhs);
  };

  class UDPListener: public Listener {
  protected:
	virtual void handleInput(const Address& from, const Bytes& buffer);

  public:
	UDPListener(Connection* c, ConnectionManager* manager);
	virtual ~UDPListener();
  };

  class TCPListener: public Listener {
  protected:
	virtual void handleInput(const Address& from, const Bytes& buffer);

  public:
	TCPListener(Connection* c, ConnectionManager* manager);
	virtual ~TCPListener();
  };

  class SendThread {
  private:
	typedef struct SendInfo {
	  const Bytes data;
	  int id;

	  SendInfo(const Bytes& bytes, int ID) : data(bytes),id(ID) {}
	} SendInfo;

	pthread_t m_thread;
	pthread_mutex_t m_lock;
	pthread_cond_t m_condition;
	typedef std::list<SendInfo> SendQueue;
	SendQueue m_queue;
	const Address m_target;
	ConnectionManager* m_manager;
	bool m_udp;
	bool m_running;
	int m_nextID;

	int pushImpl(const Bytes& bytes);
	bool queueContainsID(int id);


  public:
	SendThread(const Address& target, ConnectionManager* manager, bool udp);
	~SendThread();

	void start();
	void stop();
	void run();

	void push(const Bytes& bytes);
	void pushAndWait(const Bytes& bytes);
  };

  typedef struct InputInfo {
	Bytes data;
	Address from;
  } InputInfo;

  typedef struct PollInfo {
	Listener* listener;
	ConnectionManager* manager;
  } PollInfo;

  class ConnectionManager : public TcpServer::TCPConnectionCallback {
	friend class Listener;
	friend class UDPListener;
	friend class TCPListener;
	friend class SendThread;

  private:
	int m_udpWait;
	int m_udpSize;
	//	bool m_listenOnUDPSendPort;
	LongUDPConnection* m_udp; // The connection to use when sending LongUDP
	typedef std::list<Listener*> ListenerList;
	typedef std::map<Connection*, Listener*> ConnectionToListener;
	typedef std::set<Address> AddressSet;
	typedef std::map<Address,TcpConnection*> AddressToTCPConnection;
	typedef std::list<TcpServer*> TCPServerList;
	typedef std::map<Address,SendThread*> AddressToSendThread;
	typedef std::set<SendThread*> SendThreadSet;
	ListenerList m_listeners; // Connections that are listening for incoming data
	ConnectionToListener m_connectionToListener;
	AddressSet m_udpTargets; // Targets that should be sent LongUDP data.
	AddressSet m_tcpTargets; // Targets that should be sent TCP data.
	AddressToTCPConnection m_tcpOpen; // Open TCP connections
	TCPServerList m_tcpServers; // TCP servers that are listening for connections
	AddressToSendThread m_sendThreads; // Map from address to send thread for all known addresses
	SendThreadSet m_allSendThreads;
	bool started; // Whether the manager has been started or not

	// Send a message to a target
	virtual bool sendUDP(const Bytes& buffer, const Address& address);
	virtual bool sendTCP(const Bytes& buffer, const Address& address);

	virtual LongUDPConnection* openLongUDPConnection(const Address& target, int timeout = 60);
	virtual void closeLongUDPConnection(LongUDPConnection*);

	// Open a new TCP connection to a particular address - this will create a new object, so don't forget to call closeTCPConnection later on
	virtual TcpConnection* openTCPConnection(const Address& address);
	// Close an existing TCP connection - this will delete the connection object
	virtual void closeTCPConnection(TcpConnection*);

	Listener* createListener(LongUDPConnection* connection);
	Listener* createListener(TcpConnection* connection);
	void destroyListener(Listener* listener);

	virtual void addData(InputInfo info);

	void createSendThread(const Address& address);

	std::queue<InputInfo> m_data;
	pthread_mutex_t m_dataLock;
	pthread_cond_t m_dataCondition;

	// No copying allowed
	ConnectionManager(const ConnectionManager& rhs);
	ConnectionManager& operator=(const ConnectionManager& rhs);

  public:
	ConnectionManager();
	virtual ~ConnectionManager();

	// Start the connection manager
	virtual void start();
	// Stop the connection manager
	virtual void stop();

	// Send some data to an address. Returns true iff the message was sent successfully.
	virtual bool send(const Bytes& buffer, const Address& address);
	virtual bool sendAndWait(const Bytes& buffer, const Address& address);

	// Receive data and store in a buffer. If "from" is not null then the address the data came from will be stored in "from". This function will block until data is available, or until "timeout" milliseconds have elapsed. If timeout is zero then this function will not block (this is the default), if timeout is less than zero then it will not timeout. Returns true iff data has been received.
	virtual bool receive(Bytes& buffer, Address* from, long long timeout = 0);

	// Find out if any data is available for reading within "timeout" milliseconds. If timeout is zero then this function will not block (this is the default), if timeout is less than zero then it will not timeout. Returns true iff data has been received.
	virtual bool isDataAvailable(long long timeout = 0);

	// TCP
	// Instruct the connection manager to listen for TCP connections on a particular port
	virtual void listenTCP(int port);
	// In the future send all data to the given target via TCP
	virtual void sendViaTCP(const Address& target);
	// Callback function for TCPServers
	virtual void incomingTCPConnection(TcpConnection*, const Address& from);

	// UDP
	virtual void setUDPSize(int size);
	virtual void setUDPWait(int wait);
	// Instruct the connection manager to listen for UDP connections on a particular address and port.
	// Address: The network byte order address to bind to.
	// Port: The network byte order port to bind to (can be zero).
	// Timeout: If a LongUDP packet is not completely received within this number of seconds then consider the packet to be timed out.
	virtual void listenUDP(int port, const char* address = 0, int timeout = 60);
	// In the future send all data to the given target via UDP
	virtual void sendViaUDP(const Address& target);
	// Should we listen to the port we use for sending data?
	//	virtual void setListenOnUDPSendPort(bool b);

	// Get a Connection object for sending to a particular target
	virtual Connection* getConnection(const Address& target);
  };  

  // The main function for a thread controlling a Listener*.
  void* poll(void* info);
  // The main function for a thread controlling a SendThread*.
  void* send(void* info);
}

#endif
