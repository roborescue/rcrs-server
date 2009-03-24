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

#include "input.h"
#include "error.h"
#include "objects.h"

namespace Librescue {
  Overrun::Overrun(std::string why) : m_reason(why) {
  }

  Overrun::~Overrun() throw() {
  }

  std::string Overrun::why() const {
	return m_reason;
  }

  InputBuffer::InputBuffer(const Bytes& data) : m_bytes(data) {
	m_index = 0;
  }

  InputBuffer::~InputBuffer() {
  }

  int InputBuffer::size() const {
	return m_bytes.size();
  }

  Cursor InputBuffer::cursor() const {
	return m_index;
  }

  void InputBuffer::setCursor(Cursor c) {
	m_index = c;
  }

  void InputBuffer::skip(INT_32 size) {
	m_index += size;
	if (unsigned(m_index) > m_bytes.size())
	  throw Overrun();
  }

  INT_32 InputBuffer::readInt32(std::string reason) {
	if ((unsigned)m_index + 4 > m_bytes.size())
	  throw Overrun(reason);
	INT_32 result = m_bytes[m_index]<<24 | m_bytes[m_index+1]<<16 | m_bytes[m_index+2]<<8 | m_bytes[m_index+3];
	m_index+=4;
	return result;
  }

  INT_32 InputBuffer::peekInt32(std::string reason) {
	if ((unsigned)m_index + 4 > m_bytes.size())
	  throw Overrun(reason);
	INT_32 result = m_bytes[m_index]<<24 | m_bytes[m_index+1]<<16 | m_bytes[m_index+2]<<8 | m_bytes[m_index+3];
	return result;
  }

  void InputBuffer::readString(char* result, std::string reason) {
	INT_32 size = readInt32();
	if ((unsigned)m_index+size > m_bytes.size()) throw Overrun(reason);
	for (int i=0;i<size;++i) result[i] = m_bytes[m_index++];
  }

  void InputBuffer::readString(char* result, int size,std::string reason) {
	if ((unsigned)m_index+size > m_bytes.size()) throw Overrun(reason);
	for (int i=0;i<size;++i) result[i] = m_bytes[m_index++];
  }

  std::string InputBuffer::readString(std::string reason) {
	INT_32 size = readInt32();
	if ((unsigned)m_index+size > m_bytes.size()) throw Overrun(reason);
	std::string result;
	for (int i=0;i<size;++i) result += m_bytes[m_index++]; // This is probably inefficient but I don't have a good book on STL handy. I'll fix it later.
	return result;
  }

  void InputBuffer::read(int size, Bytes& buffer,std::string reason) {
	if ((unsigned)m_index+size > m_bytes.size()) throw Overrun(reason);
	for (int i=0;i<size;++i) {
	  buffer.push_back(m_bytes[m_index+i]);
	}
	m_index += size;
  }

  void InputBuffer::read(int size, Byte* buffer,std::string reason) {
	if ((unsigned)m_index+size > m_bytes.size()) throw Overrun(reason);
	for (int i=0;i<size;++i) {
	  buffer[i] = m_bytes[m_index+i];
	}
	m_index += size;
  }

  void InputBuffer::readProperty(Property* p, int time) {
	IntProperty* pInt = dynamic_cast<IntProperty*>(p);
	ArrayProperty* pArray = dynamic_cast<ArrayProperty*>(p);
	p->setLastUpdate(time);
	if (pInt) {
	  pInt->setValue(readInt32("Reading integer property value"));
	}
	else if (pArray) {
	  ValueList list;
	  int count = readInt32("Reading array property size");
	  list.reserve(count);
	  while (--count>=0) {
		list.push_back(readInt32("Reading array property value"));
	  }
	  pArray->setValues(list);
	}
	else {
	  LOG_WARNING("Input: Unknown property type");
	}
  }

  RescueObject* InputBuffer::readObject(int time) {
	TypeId type = (TypeId)readInt32("Reading object type");
	if (type==TYPE_NULL) return 0;
	INT_32 size = readInt32("Reading object size");
	RescueObject* result = newRescueObject(type);
	if (result) {
	  if (size>0) {
		//		result->read(*this,time);
		result->setId(readInt32("Reading object ID"));
		PropertyId next;
		while ((next = (PropertyId)readInt32("Reading property type"))!=PROPERTY_NULL) {
		  int propSize = readInt32("Reading property size");
		  Property* p = result->getProperty(next);
		  if (p) readProperty(p,time);
		  else skip(propSize);
		}
	  }
	}
	else skip(size);
	return result;
  }

  void InputBuffer::readObjects(int time, ObjectSet& result) {
	RescueObject* next = 0;
	do {
	  next = readObject(time);
	  if (next) {
		result.insert(next);
	  }
	} while (next);
  }
}
