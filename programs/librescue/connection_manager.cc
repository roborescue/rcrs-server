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

#include "connection_manager.h"
#include "error.h"
#include "handy.h"
#include <pthread.h>
#include <sys/time.h>
#include <exception>
#include <errno.h>

namespace Librescue {
  ConnectionManager::ConnectionManager() {
	m_udp = 0;
	m_udpWait = 30;
	m_udpSize = 1024;
	// Set up the data mutex
	pthread_mutexattr_t lockAttr;
	pthread_mutexattr_init(&lockAttr);
	pthread_mutexattr_settype(&lockAttr,PTHREAD_MUTEX_FAST_NP);
	if (pthread_mutex_init(&m_dataLock,&lockAttr)) LOG_ERROR("ConnectionManager::ConnectionManager() failed to init mutex: %s",strerror(errno));
	pthread_mutexattr_destroy(&lockAttr);
	// Set up the data condition
	if (pthread_cond_init(&m_dataCondition,NULL)) LOG_ERROR("ConnectionManager::ConnectionManager() failed to init condition: %s",strerror(errno));
	started = false;
  }

  ConnectionManager::~ConnectionManager() {
	stop();
	// Destroy the data mutex
	if (pthread_mutex_destroy(&m_dataLock)) LOG_ERROR("ConnectionManager::ConnectionManager() failed to destroy mutex: %s",strerror(errno));
	// Destroy the data condition
	if (pthread_cond_destroy(&m_dataCondition)) LOG_ERROR("ConnectionManager::ConnectionManager() failed to destroy condition: %s",strerror(errno));
  }

  void ConnectionManager::start() {
	if (started) return;
	// Open a LongUDPConnection for sending and receiving responses
	Address a((int)INADDR_ANY,0);
	m_udp = openLongUDPConnection(a);
	started = true;
  }

  void ConnectionManager::stop() {
	if (!started) return;
	started = false;
	LOG_INFO("Stopping connection manager");
	// Close all listeners
	LOG_INFO("Stopping listeners");
	for (ListenerList::iterator it = m_listeners.begin();it!=m_listeners.end();++it) {
	  //	  LOG_DEBUG("Destroying listener");
	  Listener* next = *it;
	  destroyListener(next);
	  //	  LOG_DEBUG("Done");
	}
	m_listeners.clear();
	m_connectionToListener.clear();
	m_udp = 0;
	// Close any TCP servers
	LOG_INFO("Stopping TCP servers");
	for (TCPServerList::iterator it = m_tcpServers.begin();it!=m_tcpServers.end();++it) {
	  //	  LOG_DEBUG("Closing TCP server");
	  TcpServer* next = *it;
	  next->stop();
	  delete next;
	  //	  LOG_DEBUG("Done");
	}
	m_tcpServers.clear();
	LOG_INFO("Stopping send threads");
	for (SendThreadSet::iterator it = m_allSendThreads.begin();it!=m_allSendThreads.end();++it) {
	  SendThread* next = *it;
	  next->stop();
	  delete next;
	}
	m_allSendThreads.clear();
	LOG_INFO("Connection manager stopped");
  }

  bool ConnectionManager::send(const Bytes& buffer, const Address& address) {
	// Add the message to the send queue
	if (m_sendThreads.find(address)==m_sendThreads.end()) {
	  createSendThread(address);
	}
	SendThread* s = m_sendThreads[address];
	if (s) {
	  s->push(buffer);
	  return true;
	}
	return false;
  }

  bool ConnectionManager::sendAndWait(const Bytes& buffer, const Address& address) {
	// Add the message to the send queue
	if (m_sendThreads.find(address)==m_sendThreads.end()) {
	  createSendThread(address);
	}
	SendThread* s = m_sendThreads[address];
	if (s) {
	  s->pushAndWait(buffer);
	  return true;
	}
	return false;
  }

  void ConnectionManager::createSendThread(const Address& address) {
	bool udp;
	if (m_udpTargets.find(address)!=m_udpTargets.end()) udp = true;
	else if (m_tcpTargets.find(address)!=m_tcpTargets.end()) udp = false;
	else {
	  LOG_WARNING("WARNING: Could not find method for sending to %s",address.toString());
	  return;
	}
	SendThread* s = new SendThread(address,this,udp);
	m_sendThreads[address] = s;
	m_allSendThreads.insert(s);
	s->start();
	//	LOG_DEBUG("Created send thread for sending to %s",address.toString());
  }

  Connection* ConnectionManager::getConnection(const Address& address) {
	//	LOG_DEBUG("Finding connection for %s",address.toString());
	if (m_udpTargets.find(address)!=m_udpTargets.end()) return m_udp;
	if (m_tcpTargets.find(address)!=m_tcpTargets.end()) {
	  TcpConnection* connection = m_tcpOpen[address];
	  if (!connection) {
		//		LOG_DEBUG("No open TCP connection. Creating new one");
		connection = openTCPConnection(address);
		if (!connection) {
		  // Still no connection
		  LOG_ERROR("Couldn't open TCP connection to %s",address.toString());
		  m_tcpOpen[address] = NULL;
		  return NULL;
		}
		m_tcpOpen[address] = connection;
	  }
	  if (!connection->isOpen()) {
		//		LOG_DEBUG("TCP connection is closed. Creating new one");
		closeTCPConnection(connection);
		connection = openTCPConnection(address);
		if (!connection) {
		  // Still no connection
		  LOG_ERROR("Couldn't open TCP connection to %s",address.toString());
		  m_tcpOpen[address] = NULL;
		  return NULL;
		}
		m_tcpOpen[address] = connection;
	  }
	  return connection;
	}
	return NULL;
  }

  // Receive data and store in a buffer. If "from" is not null then the address the data came from will be stored in "from". This function will block until data is available, or until "timeout" milliseconds have elapsed. If timeout is zero then this function will not block, if timeout is less than zero then it will not timeout (this is the default). Returns true iff data has been received.
  bool ConnectionManager::receive(Bytes& buffer, Address* from, long long timeout) {
	if (!started) {
	  //	  logWarning("WARNING: receive called before connection manager was started!");
	  return false;
	}
	if (!isDataAvailable(timeout)) return false;
	if (pthread_mutex_lock(&m_dataLock)) LOG_ERROR("ConnectionManager::receive() failed to acquire mutex: %s",strerror(errno));
	InputInfo next = m_data.front();
	m_data.pop();
	if (pthread_mutex_unlock(&m_dataLock)) LOG_ERROR("ConnectionManager::receive() failed to release mutex: %s",strerror(errno));
	buffer.clear();
	buffer.insert(buffer.begin(),next.data.begin(),next.data.end());
	if (from) {
	  *from = next.from;
	}
	return true;
  }

  // Find out if any data is available for reading. If timeout is zero then this function will not block, if timeout is less than zero then it will not timeout (this is the default). Returns true iff data has been received.
  bool ConnectionManager::isDataAvailable(long long timeout) {
	if (!started) return false;
	// Check whether anything we are listening to has data available
	if (pthread_mutex_lock(&m_dataLock)) LOG_ERROR("ConnectionManager::isDataAvailable() failed to acquire mutex: %s",strerror(errno));
	bool empty = m_data.empty();
	if (empty) {
	  if (timeout < 0) {
		// Wait for data without timing out
		//		logDebug("Waiting for data with no timeout");
		while (empty) {
		  int result = pthread_cond_wait(&m_dataCondition,&m_dataLock);
		  if (result != 0 && result != ETIMEDOUT) LOG_ERROR("ConnectionManager::isDataAvailable() failed to wait on condition: %s",strerror(result));
		  empty = m_data.empty();
		}
	  }
	  else if (timeout>0) {
		//		LOG_DEBUG("Waiting for data for %lld ms",timeout);
		// Wait for data with the given timeout
		struct timeval now;
		gettimeofday(&now,NULL);
		struct timespec finish;
		finish.tv_sec = now.tv_sec;
		finish.tv_nsec = now.tv_usec*1000L;
		long long change = timeout*(long long)1000000;
		addTime(&finish,0,change);
		while (empty && before(&now,&finish)) {
		  //		  LOG_DEBUG("Waiting for data. Now: %ld:%ld, timeout: %ld:%ld",now.tv_sec,now.tv_usec*1000L,finish.tv_sec,finish.tv_nsec);
		  int result = pthread_cond_timedwait(&m_dataCondition,&m_dataLock,&finish);
		  if (result != 0 && result != ETIMEDOUT) LOG_ERROR("ConnectionManager::isDataAvailable() failed to wait on condition: %s",strerror(result));
		  empty = m_data.empty();
		  if (empty) gettimeofday(&now,NULL);
		}
	  }
	}
	if (pthread_mutex_unlock(&m_dataLock)) LOG_ERROR("ConnectionManager::isDataAvailable() failed to release mutex: %s",strerror(errno));
	return !empty;
  }

  // Instruct the connection manager to listen for TCP connections on a particular port
  void ConnectionManager::listenTCP(int port) {
	try {
	  TcpServer* server = new TcpServer(port,*this);
	  m_tcpServers.push_back(server);
	  server->start();
	}
	catch (std::string& ex) {
	  LOG_WARNING("WARNING: Error creating TcpServer: %s",ex.c_str());
	}
	catch (std::bad_alloc& ex) {
	  LOG_ERROR("std::bad_alloc when constructing TcpServer");
	}
  }

  void ConnectionManager::sendViaTCP(const Address& target) {
	m_tcpTargets.insert(target);
  }

  void ConnectionManager::setUDPSize(int size) {
	if (m_udp) logWarning("WARNING: Setting UDP size when connection manager has already started!");
	m_udpSize = size;
  }

  void ConnectionManager::setUDPWait(int wait) {
	if (m_udp) logWarning("WARNING: Setting UDP wait when connection manager has already started!");
	m_udpWait = wait;
  }

  // Instruct the connection manager to listen for UDP connections on a particular address and port
  void ConnectionManager::listenUDP(int port, const char* address, int timeout) {
	Address a(address,port);
	openLongUDPConnection(a,timeout);
	LOG_INFO("Listening for UDP connections on %s",a.toString());
  }

  void ConnectionManager::sendViaUDP(const Address& target) {
	m_udpTargets.insert(target);
  }

  bool ConnectionManager::sendUDP(const Bytes& buffer, const Address& target) {
	if (m_udp) {
	  return m_udp->send(buffer,target);
	}
	logWarning("WARNING: Attempted to send UDP without starting connection manager!");
	return false;
  }

  bool ConnectionManager::sendTCP(const Bytes& buffer, const Address& target) {
	// Find the connection
	TcpConnection* connection = dynamic_cast<TcpConnection*>(getConnection(target));
	// Now send the data
	if (!connection) {
	  LOG_WARNING("WARNING: Could not find TCP connection to %s",target.toString());
	  return false;
	}
	if (!connection->send(buffer)) {
	  // Connection failed. Close it.
	  LOG_WARNING("WARNING: Send via TCP to %s failed: %s",target.toString(),strerror(errno));
	  closeTCPConnection(connection);
	  m_tcpOpen[target] = NULL;
	  return false;
	}
	return true;
  }

  LongUDPConnection* ConnectionManager::openLongUDPConnection(const Address& address, int timeout) {
	LongUDPConnection* result;
	try {
	  result = new LongUDPConnection(address,m_udpSize,m_udpWait,(time_t)timeout);
	}
	catch (std::bad_alloc& ex) {
	  logError("std::bad_alloc when constructing LongUDPConnection");
	  return NULL;
	}
	Listener* listener = createListener(result);
	if (listener) {
	  m_listeners.push_back(listener);
	  m_connectionToListener[result] = listener;
	  listener->start();
	  return result;
	}
	else {
	  result->close();
	  delete result;
	  return NULL;
	}
  }

  void ConnectionManager::closeLongUDPConnection(LongUDPConnection* c) {
	Listener* l = m_connectionToListener[c];
	l->stop();
	// The listener will delete c later on
  }

  TcpConnection* ConnectionManager::openTCPConnection(const Address& target) {
	TcpConnection* result;
	try {
	  result = new TcpConnection(target);
	}
	catch (std::string& ex) {
	  return NULL;
	}
	catch (std::bad_alloc& ex) {
	  logError("std::bad_alloc when constructing TcpConnection");
	  return NULL;
	}
	Listener* listener = createListener(result);
	if (listener) {
	  m_listeners.push_back(listener);
	  m_connectionToListener[result] = listener;
	  listener->start();
	  return result;
	}
	else {
	  result->close();
	  delete result;
	  return NULL;
	}
  }

  void ConnectionManager::closeTCPConnection(TcpConnection* c) {
	Listener* l = m_connectionToListener[c];
	l->stop();
	// The listener will delete c later on
  }

  void ConnectionManager::incomingTCPConnection(TcpConnection* c, const Address& from) {
	Listener* l = createListener(c);
	if (l) {
	  m_listeners.push_back(l);
	  m_connectionToListener[c] = l;
	  sendViaTCP(from);
	  m_tcpOpen[from] = c;
	  l->start();
	}
	else {
	  LOG_WARNING("Couldn't create listener for incoming TCP connection");
	  c->close();
	  delete c;
	}
  }

  void ConnectionManager::addData(InputInfo info) {
	if (pthread_mutex_lock(&m_dataLock)) LOG_ERROR("ConnectionManager::addData() failed to acquire mutex: %s",strerror(errno));
	m_data.push(info);
	// Tell everyone that data is available
	if (pthread_cond_broadcast(&m_dataCondition)) LOG_ERROR("ConnectionManager::addData() failed to broadcast condition: %s",strerror(errno));
	if (pthread_mutex_unlock(&m_dataLock)) LOG_ERROR("ConnectionManager::addData() failed to release mutex: %s",strerror(errno));
  }

  Listener* ConnectionManager::createListener(LongUDPConnection* connection) {
	UDPListener* listener;
	try {
	  listener = new UDPListener(connection,this);
	}
	catch (std::bad_alloc& ex) {
	  logError("std::bad_alloc when constructing UDPListener");
	  return NULL;
	}
	return listener;
  }

  Listener* ConnectionManager::createListener(TcpConnection* connection) {
	TCPListener* listener;
	try {
	  listener = new TCPListener(connection,this);
	}
	catch (std::bad_alloc& ex) {
	  logError("std::bad_alloc when constructing TCPListener");
	  return NULL;
	}
	return listener;
  }

  void ConnectionManager::destroyListener(Listener* listener) {
	listener->stop();
	delete listener;
  }

  Listener::Listener(Connection* c, ConnectionManager* m) {
	m_connection = c;
	m_manager = m;
	m_running = false;
	// Initialise the mutex
	if (pthread_mutex_init(&m_lock,0)) LOG_ERROR("Couldn't init listener mutex: %s",strerror(errno));
  }

  Listener::~Listener() {
	stop();
	// Destroy the mutex
	if (pthread_mutex_destroy(&m_lock)) LOG_ERROR("Couldn't destroy listener mutex: %s",strerror(errno));
  }

  void Listener::start() {
	if (pthread_mutex_lock(&m_lock)) LOG_ERROR("Listener::start() failed to acquire mutex: %s",strerror(errno));
	bool alreadyRunning = m_running;
	if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("Listener::start() failed to release mutex: %s",strerror(errno));
	if (alreadyRunning) {
	  LOG_WARNING("WARNING: Listener already started");
	  return;
	}
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr,PTHREAD_CREATE_JOINABLE);
	pthread_attr_setschedpolicy(&attr,SCHED_OTHER);
	pthread_attr_setinheritsched(&attr,PTHREAD_EXPLICIT_SCHED);
	pthread_attr_setscope(&attr,PTHREAD_SCOPE_SYSTEM);
	// Start the listen thread
	m_running = true;
	if (pthread_create(&m_thread,&attr,&Librescue::poll,this)) {
	  LOG_ERROR("Could not create listen thread: %s",strerror(errno));
	  m_thread = 0;
	  stop();
	}
	pthread_attr_destroy(&attr);
  }

  void Listener::stop() {
	if (pthread_mutex_lock(&m_lock)) LOG_ERROR("Listener::stop() failed to acquire mutex: %s",strerror(errno));
	bool temp = m_running;
	m_running = false;
	if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("Listener::stop() failed to release mutex: %s",strerror(errno));
	if (temp) {
	  // Close the connection
	  if (pthread_mutex_lock(&m_lock)) LOG_ERROR("Listener::stop() failed to acquire mutex: %s",strerror(errno));
	  m_connection->close();
	  if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("Listener::stop() failed to release mutex: %s",strerror(errno));
	  // Stop the thread
	  if (m_thread) if (pthread_join(m_thread,0)) LOG_ERROR("Listener::stop() failed to join thread: %s",strerror(errno));
	  delete m_connection;
	}
  }

  void Listener::poll() {
	bool finished = false;
	while (!finished) {
	  // See if data is available
	  if (pthread_mutex_lock(&m_lock)) LOG_ERROR("Listener::poll() failed to acquire mutex: %s",strerror(errno));
	  bool available = m_connection->isDataAvailable(100);
	  if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("Listener::poll() failed to release mutex: %s",strerror(errno));
	  if (available) {
		Bytes input;
		InputInfo info;
		if (pthread_mutex_lock(&m_lock)) LOG_ERROR("Listener::poll() failed to acquire mutex: %s",strerror(errno));
		m_connection->receive(input);
		info.data = input;
		info.from = m_connection->addressReceivedFrom();
		if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("Listener::poll() failed to release mutex: %s",strerror(errno));
		// Add the data to the queue
		handleInput(info.from,info.data);
		m_manager->addData(info);
	  }
	  if (pthread_mutex_lock(&m_lock)) LOG_ERROR("Listener::poll() failed to acquire mutex: %s",strerror(errno));
	  finished = !m_running || !m_connection->isOpen();
	  if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("Listener::poll() failed to release mutex: %s",strerror(errno));
	  pthread_yield();
	}
  }

  UDPListener::UDPListener(Connection* c, ConnectionManager* m) : Listener(c,m) {
  }

  UDPListener::~UDPListener() {
  }

  void UDPListener::handleInput(const Address& from, const Bytes& buffer) {
	m_manager->sendViaUDP(from);
  }

  TCPListener::TCPListener(Connection* c, ConnectionManager* m) : Listener(c,m) {
  }

  TCPListener::~TCPListener() {
  }

  void TCPListener::handleInput(const Address& from, const Bytes& buffer) {
	m_manager->sendViaTCP(from);
	m_manager->m_tcpOpen[from] = (TcpConnection*)m_connection;
  }

  SendThread::SendThread(const Address& target, ConnectionManager* manager, bool udp) : m_target(target), m_manager(manager), m_udp(udp) {
	m_nextID = 0;
	m_running = false;
	// Initialise the mutex
	if (pthread_mutex_init(&m_lock,0)) LOG_ERROR("Couldn't init listener mutex: %s",strerror(errno));
	// Initalise the condition variable
	if (pthread_cond_init(&m_condition,NULL)) LOG_ERROR("SendThread::SendThread() failed to init condition: %s",strerror(errno));
  }

  SendThread::~SendThread() {
	stop();
	// Destroy the mutex
	if (pthread_mutex_destroy(&m_lock)) LOG_ERROR("Couldn't destroy listener mutex: %s",strerror(errno));
	// Destroy the condition variable
	if (pthread_cond_destroy(&m_condition)) LOG_ERROR("SendThread::SendThread() failed to destroy condition: %s",strerror(errno));
  }

  void SendThread::start() {
	if (pthread_mutex_lock(&m_lock)) LOG_ERROR("SendThread::start() failed to acquire mutex: %s",strerror(errno));
	bool alreadyRunning = m_running;
	if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("SendThread::start() failed to release mutex: %s",strerror(errno));
	if (alreadyRunning) {
	  LOG_WARNING("WARNING: SendThread already started");
	  return;
	}
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr,PTHREAD_CREATE_JOINABLE);
	pthread_attr_setschedpolicy(&attr,SCHED_OTHER);
	pthread_attr_setinheritsched(&attr,PTHREAD_EXPLICIT_SCHED);
	pthread_attr_setscope(&attr,PTHREAD_SCOPE_SYSTEM);
	// Start the listen thread
	m_running = true;
	if (pthread_create(&m_thread,&attr,&Librescue::send,this)) {
	  LOG_ERROR("Could not create send thread: %s",strerror(errno));
	  m_thread = 0;
	  stop();
	}
	pthread_attr_destroy(&attr);
  }

  void SendThread::stop() {
	if (pthread_mutex_lock(&m_lock)) LOG_ERROR("SendThread::stop() failed to acquire mutex: %s",strerror(errno));
	bool temp = m_running;
	m_running = false;
	if (pthread_cond_broadcast(&m_condition)) LOG_ERROR("SendThread::stop() failed to broadcast condition: %s",strerror(errno));
	if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("SendThread::stop() failed to release mutex: %s",strerror(errno));
	if (temp) {
	  // Stop the thread
	  if (m_thread) if (pthread_join(m_thread,0)) LOG_ERROR("SendThread::stop() failed to join thread: %s",strerror(errno));
	}
  }

  void SendThread::run() {
	bool finished = false;
	while (!finished) {
	  // See if there is anything to send
	  if (pthread_mutex_lock(&m_lock)) LOG_ERROR("SendThread::run() failed to acquire mutex: %s",strerror(errno));
	  bool available = !m_queue.empty();
	  if (available) {
		SendInfo next = m_queue.front();
		m_queue.pop_front();
		if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("SendThread::run() failed to release mutex: %s",strerror(errno));
		// Send the data
		//		LOG_DEBUG("Thread %d sending message %d (%d bytes) to %s",m_thread,next.id,next.data.size(),m_target.toString());
		if (m_udp) m_manager->sendUDP(next.data,m_target);
		else m_manager->sendTCP(next.data,m_target);
		//		LOG_DEBUG("Thread %d sent message %d (%d bytes) to %s",m_thread,next.id,next.data.size(),m_target.toString());
		// Notify the send
		if (pthread_mutex_lock(&m_lock)) LOG_ERROR("SendThread::poll() failed to acquire mutex: %s",strerror(errno));
		if (pthread_cond_broadcast(&m_condition)) LOG_ERROR("SendThread::run() failed to broadcast condition: %s",strerror(errno));
	  }
	  else {
		int result = pthread_cond_wait(&m_condition,&m_lock);
		if (result != 0 && result != ETIMEDOUT) LOG_ERROR("SendThread::run() failed to wait on condition: %s",strerror(result));
	  }
	  finished = !m_running;
	  if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("SendThread::run() failed to release mutex: %s",strerror(errno));
	  pthread_yield();
	}
  }

  void SendThread::push(const Bytes& bytes) {
	pushImpl(bytes);
  }

  void SendThread::pushAndWait(const Bytes& bytes) {
	int id = pushImpl(bytes);
	// Wait for the message to get sent
	if (pthread_mutex_lock(&m_lock)) LOG_ERROR("SendThread::pushAndWait() failed to acquire mutex: %s",strerror(errno));
	while (queueContainsID(id)) {
	  int result = pthread_cond_wait(&m_condition,&m_lock);
	  if (result != 0 && result != ETIMEDOUT) LOG_ERROR("SendThread::pushAndWait() failed to wait on condition: %s",strerror(result));
	}
	if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("SendThread::pushAndWait() failed to release mutex: %s",strerror(errno));
  }

  int SendThread::pushImpl(const Bytes& bytes) {
	if (pthread_mutex_lock(&m_lock)) LOG_ERROR("SendThread::push() failed to acquire mutex: %s",strerror(errno));
	SendInfo info(bytes,m_nextID++);
	m_queue.push_back(info);
	if (pthread_cond_broadcast(&m_condition)) LOG_ERROR("SendThread::push() failed to broadcast condition: %s",strerror(errno));
	if (pthread_mutex_unlock(&m_lock)) LOG_ERROR("SendThread::push() failed to release mutex: %s",strerror(errno));
	return info.id;
  }

  bool SendThread::queueContainsID(int id) {
	// We assume that the mutex is already locked
	for (SendQueue::iterator it = m_queue.begin();it!=m_queue.end();++it) {
	  if ((*it).id == id) return true;
	}
	return false;
  }

  void* poll(void* arg) {
	Listener* listener = (Listener*)arg;
	listener->poll();
	pthread_exit(0);
  }

  void* send(void* arg) {
	SendThread* thread = (SendThread*)arg;
	thread->run();
	pthread_exit(0);
  }
}
