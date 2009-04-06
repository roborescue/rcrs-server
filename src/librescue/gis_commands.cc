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
#include "error.h"

namespace Librescue {
  GISConnect::GISConnect(INT_32 version) : m_version(version) {}

  GISConnect::GISConnect(InputBuffer& in) {
	decode(in);
  }

  GISConnect::~GISConnect() {}

  Header GISConnect::getType() const {
	return KG_CONNECT;
  }

  void GISConnect::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeInt32(m_version);
  }

  void GISConnect::decode(InputBuffer& in) {
	Command::decode(in);
	m_version = in.readInt32();
  }

  Command* GISConnect::clone() const {
	return new GISConnect(m_version);
  }

  INT_32 GISConnect::getVersion() const {
	return m_version;
  }

  GISConnectOK::GISConnectOK(const ObjectSet& objects) {
	//	for (ObjectSet::const_iterator it = objects.begin();it!=objects.end();++it) {
	//	  m_objects.insert((*it)->clone());
	//	}
	//	LOG_DEBUG("GISConnectOK created with %d objects, have %d stored",objects.size(),m_objects.size());
	m_objects.insert(objects.begin(),objects.end());
	m_delete = false;
  }

  GISConnectOK::GISConnectOK(InputBuffer& in) {
	m_delete = false;
	decode(in);
  }
  
  GISConnectOK::~GISConnectOK() {
	deleteObjects();
	// Delete all the objects
	//	for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
	//	  delete *it;
	//	}
  }

  void GISConnectOK::deleteObjects() {
	if (m_delete) {
	  // Delete all the objects
	  for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
		delete *it;
	  }
	  m_delete = false;
	}
	m_objects.clear();
  }

  Header GISConnectOK::getType() const {
	return GK_CONNECT_OK;
  }

  void GISConnectOK::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeObjects(m_objects);
  }

  void GISConnectOK::decode(InputBuffer& in) {
	Command::decode(in);
	// Delete any old objects
	//	for (ObjectSet::iterator it = m_objects.begin();it!=m_objects.end();++it) {
	//	  delete *it;
	//	}
	//	m_objects.clear();
	deleteObjects();
	in.readObjects(0,m_objects);
	m_delete = true;
  }

  Command* GISConnectOK::clone() const {
	if (m_delete) {
	  // Need to clone objects as well
	  ObjectSet clonedObjects;
	  for (ObjectSet::const_iterator it = m_objects.begin();it!=m_objects.end();++it) {
		clonedObjects.insert((*it)->clone());
	  }
	  GISConnectOK* result = new GISConnectOK(clonedObjects);
	  result->m_delete = true;
	  return result;
	}
	return new GISConnectOK(m_objects);
  }

  const ObjectSet& GISConnectOK::getObjects() const {
	return m_objects;
  }

  GISConnectError::GISConnectError(std::string reason) : m_reason(reason) {}

  GISConnectError::GISConnectError(InputBuffer& in) {
	decode(in);
  }

  GISConnectError::~GISConnectError() {}

  Header GISConnectError::getType() const {
	return GK_CONNECT_ERROR;
  }

  void GISConnectError::encode(OutputBuffer& out) const {
	Command::encode(out);
	out.writeString(m_reason);
  }

  void GISConnectError::decode(InputBuffer& in) {
	Command::decode(in);
	m_reason = in.readString();
  }

  Command* GISConnectError::clone() const {
	return new GISConnectError(m_reason);
  }

  const std::string& GISConnectError::getReason() const {
	return m_reason;
  }

  GISAcknowledge::GISAcknowledge() {}

  GISAcknowledge::GISAcknowledge(InputBuffer& in) {
	decode(in);
  }

  GISAcknowledge::~GISAcknowledge() {}

  Header GISAcknowledge::getType() const {
	return KG_ACKNOWLEDGE;
  }

  void GISAcknowledge::encode(OutputBuffer& out) const {
	Command::encode(out);
  }

  void GISAcknowledge::decode(InputBuffer& in) {
	Command::decode(in);
  }

  Command* GISAcknowledge::clone() const {
	return new GISAcknowledge();
  }
};
