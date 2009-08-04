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

#include "command.h"
#include "objects.h"

namespace Librescue {
  ViewerConnect::ViewerConnect(Id requestId, INT_32 version) : m_requestId(requestId), m_version(version) {}

  ViewerConnect::ViewerConnect(InputBuffer& in) {
	decode(in);
  }

  ViewerConnect::~ViewerConnect() {}

  Header ViewerConnect::getType() const {
	return VK_CONNECT;
  }

  void ViewerConnect::encode(OutputBuffer& out) const {
	Command::encode(out);
        out.writeInt32(m_requestId);
	out.writeInt32(m_version);
  }

  void ViewerConnect::decode(InputBuffer& in) {
	Command::decode(in);
        m_requestId = in.readInt32();
	m_version = in.readInt32();
  }

  Command* ViewerConnect::clone() const {
    return new ViewerConnect(m_requestId, m_version);
  }

  Id ViewerConnect::getRequestId() const {
	return m_requestId;
  }

  INT_32 ViewerConnect::getVersion() const {
	return m_version;
  }

  ViewerConnectOK::ViewerConnectOK(Id requestId, Id viewerId, const ObjectSet& objects) : m_requestId(requestId), m_viewerId(viewerId) {
	m_objects.insert(objects.begin(),objects.end());
	m_delete = false;	
  }

  ViewerConnectOK::ViewerConnectOK(InputBuffer& in) {
	m_delete = false;	
	decode(in);
  }
  
  ViewerConnectOK::~ViewerConnectOK() {
	deleteObjects();
	// Delete all the objects
  }

  void ViewerConnectOK::deleteObjects() {
	if (m_delete) {
	  // Delete all the objects
	  for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
		delete *it;
	  }
	  m_delete = false;
	}
	m_objects.clear();
  }

  Header ViewerConnectOK::getType() const {
	return KV_CONNECT_OK;
  }

  void ViewerConnectOK::encode(OutputBuffer& out) const {
	Command::encode(out);
        out.writeInt32(m_requestId);
        out.writeInt32(m_viewerId);
	out.writeObjects(m_objects);
  }

  void ViewerConnectOK::decode(InputBuffer& in) {
	Command::decode(in);
        m_requestId = in.readInt32();
        m_viewerId = in.readInt32();
	// Delete any old objects
	deleteObjects();
	in.readObjects(0,m_objects);
	m_delete = true;
  }

  Command* ViewerConnectOK::clone() const {
	if (m_delete) {
	  // Need to clone objects as well
	  ObjectSet clonedObjects;
	  for (ObjectSet::const_iterator it = m_objects.begin();it!=m_objects.end();++it) {
		clonedObjects.insert((*it)->clone());
	  }
	  ViewerConnectOK* result = new ViewerConnectOK(m_requestId, m_viewerId, clonedObjects);
	  result->m_delete = true;
	  return result;
	}
	return new ViewerConnectOK(m_requestId, m_viewerId, m_objects);
  }

  Id ViewerConnectOK::getRequestId() const {
	return m_requestId;
  }

  Id ViewerConnectOK::getViewerId() const {
	return m_viewerId;
  }

  const ObjectSet& ViewerConnectOK::getObjects() const {
	return m_objects;
  }

  ViewerConnectError::ViewerConnectError(Id requestId, std::string reason) : m_requestId(requestId), m_reason(reason) {}

  ViewerConnectError::ViewerConnectError(InputBuffer& in) {
	decode(in);
  }

  ViewerConnectError::~ViewerConnectError() {}

  Header ViewerConnectError::getType() const {
	return KV_CONNECT_ERROR;
  }

  void ViewerConnectError::encode(OutputBuffer& out) const {
	Command::encode(out);
        out.writeInt32(m_requestId);
	out.writeString(m_reason);
  }

  void ViewerConnectError::decode(InputBuffer& in) {
	Command::decode(in);
        m_requestId = in.readInt32();
	m_reason = in.readString();
  }

  Command* ViewerConnectError::clone() const {
    return new ViewerConnectError(m_requestId, m_reason);
  }

  Id ViewerConnectError::getRequestId() const {
	return m_requestId;
  }

  const std::string& ViewerConnectError::getReason() const {
	return m_reason;
  }

  ViewerAcknowledge::ViewerAcknowledge(Id requestId, Id viewerId) : m_requestId(requestId), m_viewerId(viewerId) {}

  ViewerAcknowledge::ViewerAcknowledge(InputBuffer& in) {
	decode(in);
  }

  ViewerAcknowledge::~ViewerAcknowledge() {}

  Header ViewerAcknowledge::getType() const {
	return VK_ACKNOWLEDGE;
  }

  void ViewerAcknowledge::encode(OutputBuffer& out) const {
	Command::encode(out);
        out.writeInt32(m_requestId);
        out.writeInt32(m_viewerId);
  }

  void ViewerAcknowledge::decode(InputBuffer& in) {
	Command::decode(in);
        m_requestId = in.readInt32();
        m_viewerId = in.readInt32();
  }

  Command* ViewerAcknowledge::clone() const {
    return new ViewerAcknowledge(m_requestId, m_viewerId);
  }

  Id ViewerAcknowledge::getRequestId() const {
	return m_requestId;
  }

  Id ViewerAcknowledge::getViewerId() const {
	return m_viewerId;
  }
};
