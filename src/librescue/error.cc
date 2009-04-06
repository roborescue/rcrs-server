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

#include "error.h"
#include "input.h"
#include <iostream>
#include <stdexcept>

namespace Librescue {
  Error::Error() {
	m_logEnabled[LOG_LEVEL_ERROR] = true;
	m_logEnabled[LOG_LEVEL_WARNING] = true;
	m_logEnabled[LOG_LEVEL_INFO] = true;
	m_logEnabled[LOG_LEVEL_DEBUG] = true;
  }

  Error::~Error() {
  }

  Error& Error::get() {
	static Error instance;
	return instance;
  }

  std::ostream& Error::getLog(LogLevel level) {
	switch (level) {
	case LOG_LEVEL_ERROR:
	  return std::cerr;
	case LOG_LEVEL_WARNING:
	  return std::cerr;
	case LOG_LEVEL_INFO:
	  return std::cout;
	case LOG_LEVEL_DEBUG:
	  return std::cout;
	}
	// Can't actually reach here
	throw std::invalid_argument("Reached end of getLog(LogLevel). This shouldn't happen!");
  }

  void Error::log(LogLevel level, std::string string, bool flush) {
	if (m_logEnabled[level]) {
	  std::ostream& stream = getLog(level);
	  stream << string << std::endl;
	  if (flush) stream.flush();
	}
  }

  void Error::setLogLevel(LogLevel level) {
	for (int i = LOG_LEVEL_MIN;i<LOG_LEVEL_MAX;++i) {
	  m_logEnabled[i] = i<=level;
	}
  }

  void Error::setLogLevel(LogLevel level, bool use) {
	m_logEnabled[level] = use;
  }

  void logError(std::string error) {
	Error::get().log(LOG_LEVEL_ERROR,error);
  }

  void logWarning(std::string warning) {
	Error::get().log(LOG_LEVEL_WARNING,warning);
  }

  void logDebug(std::string debug) {
	Error::get().log(LOG_LEVEL_DEBUG,debug);
  }

  void logInfo(std::string info) {
	Error::get().log(LOG_LEVEL_INFO,info);
  }

  void setLogLevel(LogLevel level) {
	Error::get().setLogLevel(level);
  }

  void setLogLevel(LogLevel level, bool use) {
	Error::get().setLogLevel(level,use);
  }

  void dumpBytes(LogLevel level, const char* bytes, int size) {
	char msg[128];
	for (int i=0;i<size;i+=4) {
	  snprintf(msg,128,"%d\t%02x\t%02x\t%02x\t%02x\t%d",i,bytes[i],bytes[i+1],bytes[i+2],bytes[i+3],bytes[i]<<24|bytes[i+1]<<16|bytes[i+2]<<8|bytes[i+3]);
	  Error::get().log(level,msg,i<size-4);
	}
  }

  void dumpBytes(LogLevel level, const Byte* bytes, int size) {
	char msg[128];
	for (int i=0;i<size;i+=4) {
	  snprintf(msg,128,"%d\t%02x\t%02x\t%02x\t%02x\t%d",i,bytes[i],bytes[i+1],bytes[i+2],bytes[i+3],bytes[i]<<24|bytes[i+1]<<16|bytes[i+2]<<8|bytes[i+3]);
	  Error::get().log(level,msg,i<size-4);
	}
  }

  void dumpBytes(LogLevel level, const Bytes& bytes) {
	char msg[128];
	int size = bytes.size();
	for (int i=0;i<size;i+=4) {
	  snprintf(msg,128,"%d\t%02x\t%02x\t%02x\t%02x\t%d",i,bytes[i],bytes[i+1],bytes[i+2],bytes[i+3],bytes[i]<<24|bytes[i+1]<<16|bytes[i+2]<<8|bytes[i+3]);
	  Error::get().log(level,msg,i<size-4);
	}
  }

  void dumpBytes(LogLevel level, InputBuffer& input) {
	Cursor start = input.cursor();
	Cursor current = start;
	char msg[128];
	while (current<(Cursor)input.size()) {
	  INT_32 next = input.readInt32();
	  snprintf(msg,128,"%d\t%02x\t%02x\t%02x\t%02x\t%d",current,(next>>24)&0xFF,(next>>16)&0xFF,(next>>8)&0xFF,next&0xFF,next);
	  current = input.cursor();
	  Error::get().log(level,msg,current<(Cursor)input.size());
	}
	input.setCursor(start);
  }
}
