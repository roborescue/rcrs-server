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

#ifndef ERROR_H
#define ERROR_H

#include <string>
#include <ostream>
#include "common.h"

#define BUFFER_LENGTH 256

#define LOG_DEBUG(...) {char errorBuffer[BUFFER_LENGTH];snprintf(errorBuffer,BUFFER_LENGTH,__VA_ARGS__);Librescue::logDebug(errorBuffer);}
#define LOG_WARNING(...) {char errorBuffer[BUFFER_LENGTH];snprintf(errorBuffer,BUFFER_LENGTH,__VA_ARGS__);Librescue::logWarning(errorBuffer);}
#define LOG_ERROR(...) {char errorBuffer[BUFFER_LENGTH];snprintf(errorBuffer,BUFFER_LENGTH,__VA_ARGS__);Librescue::logError(errorBuffer);}
#define LOG_INFO(...) {char errorBuffer[BUFFER_LENGTH];snprintf(errorBuffer,BUFFER_LENGTH,__VA_ARGS__);Librescue::logInfo(errorBuffer);}

namespace Librescue {
  class InputBuffer;

  enum LogLevel {
	LOG_LEVEL_ERROR = 0,
	LOG_LEVEL_WARNING = 1,
	LOG_LEVEL_INFO = 2,
	LOG_LEVEL_DEBUG = 3
  };

  const int LOG_LEVEL_MIN = 0;
  const int LOG_LEVEL_MAX = 4;
  
  class Error {
  public:
	void log(LogLevel level, std::string string, bool flush = true);
	void setLogLevel(LogLevel level);
	void setLogLevel(LogLevel level, bool use);

	static Error& get();

  private:
	// Singleton
	Error();
	~Error();
	// No copying
	Error(const Error& rhs);
	Error& operator=(const Error& rhs);
	std::ostream& getLog(LogLevel level);

	bool m_logEnabled[LOG_LEVEL_MAX];
  };

  void logError(std::string error);
  void logWarning(std::string warning);
  void logDebug(std::string debug);
  void logInfo(std::string info);
  void dumpBytes(LogLevel level, const char* bytes, int size);
  void dumpBytes(LogLevel level, const Byte* bytes, int size);
  void dumpBytes(LogLevel level, const Bytes& bytes);
  void dumpBytes(LogLevel level, InputBuffer& input);

  void setLogLevel(LogLevel level);
  void setLogLevel(LogLevel level, bool use);
}

#endif
