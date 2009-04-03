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

#ifndef HANDY_H
#define HANDY_H

#include "common.h"
#include <sys/time.h>
#include <string>

namespace Librescue {
  bool before(struct timeval* first, struct timespec* last);
  bool before(struct timeval* first, struct timeval* last);

  // Get the number of milliseconds between two times. This will return last-first.
  long int timeDiff(struct timeval* first, struct timeval* last);

  void addTime(struct timeval* time, long long mseconds);
  void addTime(struct timeval* time, time_t seconds, long long useconds);
  void addTime(struct timespec* time, time_t seconds, long long nseconds);

  int min(int a, int b);
  long min(long a, long b);
  long long min(long long a, long long b);

  int max(int a, int b);
  long max(long a, long b);
  long long max(long long a, long long b);

  void initRandom(int seed);
  int randomInt();
  int randomInt(int min, int max);
  double randomDouble();
  double randomDouble(double min, double max);
  bool randomBoolean(double probability);

  Bytes stringToBytes(std::string s);

  int round(int value, int base);

  std::string propertyName(PropertyId property);
  std::string typeName(TypeId type);
  std::string headerName(Header header);
  std::string agentType(AgentType type);

  class StopWatch {
  public:
	StopWatch();
	StopWatch(std::string msg);
	~StopWatch();

  private:
	std::string m_msg;
	clock_t m_start;
  };
}

#endif
