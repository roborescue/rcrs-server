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

#include "handy.h"
#include "error.h"
#include <sys/time.h>
#include <stdlib.h>
#include <iostream>

namespace Librescue {
  bool before(struct timeval* first, struct timespec* last) {
	return first->tv_sec < last->tv_sec || (first->tv_sec==last->tv_sec && first->tv_usec*1000L < last->tv_nsec);
  }

  bool before(struct timeval* first, struct timeval* last) {
	return first->tv_sec < last->tv_sec || (first->tv_sec==last->tv_sec && first->tv_usec < last->tv_usec);
  }

  // Get the number of milliseconds between two times. This will return last-first.
  long int timeDiff(struct timeval* first, struct timeval* last) {
	long long result = (last->tv_sec - first->tv_sec)*1000ll;
	result += (last->tv_usec - first->tv_usec)/1000ll;
	return result;
  }

  void addTime(struct timeval* time, long long mseconds) {
	addTime(time,0,mseconds*1000ll);
  }

  void addTime(struct timeval* time, time_t seconds, long long useconds) {
	time->tv_sec += seconds;
	time->tv_usec += useconds;
	while (time->tv_usec >= 1000000L) {
	  ++time->tv_sec;
	  time->tv_usec -= 1000000L;
	}
  }

  void addTime(struct timespec* time, time_t seconds, long long nseconds) {
	time->tv_sec += seconds;
	long long nsec = time->tv_nsec + nseconds;
	//	LOG_DEBUG("Adding %lld ns",nseconds);
	while (nsec >= 1000000000ll) {
	  ++time->tv_sec;
	  nsec -= 1000000000ll;
	}
	time->tv_nsec = nsec;
  }

  int min(int a, int b) {
	return a<b?a:b;
  }

  long min(long a, long b) {
	return a<b?a:b;
  }

  long long min(long long a, long long b) {
	return a<b?a:b;
  }

  int max(int a, int b) {
	return a>b?a:b;
  }

  long max(long a, long b) {
	return a>b?a:b;
  }

  long long max(long long a, long long b) {
	return a>b?a:b;
  }

  void initRandom(int seed) {
	if (seed < 0) srandom(clock());
	else srandom((unsigned)seed);
  }

  int randomInt() {
	return random();
  }

  int randomInt(int min, int max) {
	return (int)(randomDouble(min,max));
  }

  double randomDouble() {
	double d = random();
	return d/RAND_MAX;
  }

  double randomDouble(double min, double max) {
	double d = randomDouble();
	return min + (d * (max-min));
  }

  bool randomBoolean(double probability) {
	return randomDouble() < probability;
  }

  Bytes stringToBytes(std::string s) {
	Bytes result;
	const char* data = s.c_str();
	for (int i=0;data[i]!=0;++i) result.push_back((unsigned char)data[i]);
	return result;
  }

  int round(int value, int base) {
	int result = value/base;
	result *= base;
	int remainder = value - result;
	if (remainder >= base/2) result += base;
	return result;
  }

  std::string propertyName(PropertyId property) {
	switch (property) {
	case PROPERTY_NULL:
	  return "NULL";
	case PROPERTY_START_TIME:
	  return "START TIME";
	case PROPERTY_LONGITUDE:
	  return "LONGITUDE";
	case PROPERTY_LATITUDE:
	  return "LATITUDE";
	case PROPERTY_WIND_FORCE:
	  return "WIND_FORCE";
	case PROPERTY_WIND_DIRECTION:
	  return "WIND_DIRECTION";
	case PROPERTY_HEAD:
	  return "HEAD";
	case PROPERTY_TAIL:
	  return "TAIL";
	case PROPERTY_LENGTH:
	  return "LENGTH";
	case PROPERTY_ROAD_KIND:
	  return "ROAD_KIND";
	case PROPERTY_CARS_PASS_TO_HEAD:
	  return "CARS_PASS_TO_HEAD";
	case PROPERTY_CARS_PASS_TO_TAIL:
	  return "CARS_PASS_TO_TAIL";
	case PROPERTY_HUMANS_PASS_TO_HEAD:
	  return "HUMANS_PASS_TO_HEAD";
	case PROPERTY_HUMANS_PASS_TO_TAIL:
	  return "HUMANS_PASS_TO_TAIL";
	case PROPERTY_WIDTH:
	  return "WIDTH";
	case PROPERTY_BLOCK:
	  return "BLOCK";
	case PROPERTY_REPAIR_COST:
	  return "REPAIR_COST";
	case PROPERTY_MEDIAN_STRIP:
	  return "MEDIAN_STRIP";
	case PROPERTY_LINES_TO_HEAD:
	  return "LINES_TO_HEAD";
	case PROPERTY_LINES_TO_TAIL:
	  return "LINES_TO_TAIL";
	case PROPERTY_WIDTH_FOR_WALKERS:
	  return "WIDTH_FOR_WALKERS";
	case PROPERTY_SIGNAL:
	  return "SIGNAL";
	case PROPERTY_SHORTCUT_TO_TURN:
	  return "SHORTCUT_TO_TURN";
	case PROPERTY_POCKET_TO_TURN_ACROSS:
	  return "POCKET_TO_TURN_ACROSS";
	case PROPERTY_SIGNAL_TIMING:
	  return "SIGNAL_TIMING";
	case PROPERTY_X:
	  return "X";
	case PROPERTY_Y:
	  return "Y";
	case PROPERTY_EDGES:
	  return "EDGES";
	case PROPERTY_FLOORS:
	  return "FLOORS";
	case PROPERTY_BUILDING_ATTRIBUTES:
	  return "BUILDING_ATTRIBUTES";
	case PROPERTY_IGNITION:
	  return "IGNITION";
	case PROPERTY_FIERYNESS:
	  return "FIERYNESS";
	case PROPERTY_BROKENNESS:
	  return "BROKENNESS";
	case PROPERTY_ENTRANCES:
	  return "ENTRANCES";
	case PROPERTY_BUILDING_CODE:
	  return "BUILDING_CODE";
	case PROPERTY_BUILDING_AREA_GROUND:
	  return "BUILDING_AREA_GROUND";
	case PROPERTY_BUILDING_AREA_TOTAL:
	  return "BUILDING_AREA_TOTAL";
	case PROPERTY_BUILDING_APEXES:
	  return "BUILDING_APEXES";
	case PROPERTY_POSITION:
	  return "POSITION";
	case PROPERTY_POSITION_EXTRA:
	  return "POSITION_EXTRA";
	case PROPERTY_DIRECTION:
	  return "DIRECTION";
	case PROPERTY_POSITION_HISTORY:
	  return "POSITION_HISTORY";
	case PROPERTY_STAMINA:
	  return "STAMINA";
	case PROPERTY_HP:
	  return "HP";
	case PROPERTY_DAMAGE:
	  return "DAMAGE";
	case PROPERTY_BURIEDNESS:
	  return "BURIEDNESS";
	case PROPERTY_WATER_QUANTITY:
	  return "WATER_QUANTITY";
	case PROPERTY_BUILDING_TEMPERATURE:
	  return "BUILDING_TEMPERATURE";
	case PROPERTY_BUILDING_IMPORTANCE:
	  return "BUILDING_IMPORTANCE";
	}
	LOG_ERROR("Unrecognised property: %d",property);
	return "Unrecognised property";
  }

  std::string typeName(TypeId type) {
	switch (type) {
	case TYPE_NULL:
	  return "NULL";
	case TYPE_WORLD:
	  return "WORLD";
	case TYPE_ROAD:
	  return "ROAD";
	case TYPE_RIVER:
	  return "RIVER";
	case TYPE_NODE:
	  return "NODE";
	case TYPE_RIVER_NODE:
	  return "RIVER_NODE";
	case TYPE_BUILDING:
	  return "BUILDING";
	case TYPE_REFUGE:
	  return "REFUGE";
	case TYPE_FIRE_STATION:
	  return "FIRE_STATION";
	case TYPE_AMBULANCE_CENTER:
	  return "AMBULANCE_CENTER";
	case TYPE_POLICE_OFFICE:
	  return "POLICE_OFFICE";
	case TYPE_CIVILIAN:
	  return "CIVILIAN";
	case TYPE_CAR:
	  return "CAR";
	case TYPE_FIRE_BRIGADE:
	  return "FIRE_BRIGADE";
	case TYPE_AMBULANCE_TEAM:
	  return "AMBULANCE_TEAM";
	case TYPE_POLICE_FORCE:
	  return "POLICE_FORCE";
	}
	LOG_ERROR("Unrecognised type: %d",type);
	return "Unrecognised type";
  }

  std::string headerName(Header header) {
	switch (header) {
	case HEADER_NULL:
	  return "HEADER_NULL";
	case KG_CONNECT:
	  return "KG_CONNECT";
	case KG_ACKNOWLEDGE:
	  return "KG_ACKNOWLEDGE";
	case GK_CONNECT_OK:
	  return "GK_CONNECT_OK";
	case GK_CONNECT_ERROR:
	  return "GK_CONNECT_ERROR";
	case SK_CONNECT:
	  return "SK_CONNECT";
	case SK_ACKNOWLEDGE:
	  return "SK_ACKNOWLEDGE";
	case SK_UPDATE:
	  return "SK_UPDATE";
	case KS_CONNECT_OK:
	  return "KS_CONNECT_OK";
	case KS_CONNECT_ERROR:
	  return "KS_CONNECT_ERROR";
	case VK_CONNECT:
	  return "VK_CONNECT";
	case VK_ACKNOWLEDGE:
	  return "VK_ACKNOWLEDGE";
	case KV_CONNECT_OK:
	  return "KV_CONNECT_OK";
	case KV_CONNECT_ERROR:
	  return "KV_CONNECT_ERROR";
	case AK_CONNECT:
	  return "AK_CONNECT";
	case AK_ACKNOWLEDGE:
	  return "AK_ACKNOWLEDGE";
	case KA_CONNECT_OK:
	  return "KA_CONNECT_OK";
	case KA_CONNECT_ERROR:
	  return "KA_CONNECT_ERROR";
	case KA_SENSE:
	  return "KA_SENSE";
	case KA_HEAR:
	  return "KA_HEAR";
	case KA_HEAR_SAY:
	  return "KA_HEAR_SAY";
	case KA_HEAR_TELL:
	  return "KA_HEAR_TELL";
	case UPDATE:
	  return "UPDATE";
	case COMMANDS:
	  return "COMMANDS";
	case AK_REST:
	  return "AK_REST";
	case AK_MOVE:
	  return "AK_MOVE";
	case AK_LOAD:
	  return "AK_LOAD";
	case AK_UNLOAD:
	  return "AK_UNLOAD";
	case AK_SAY:
	  return "AK_SAY";
	case AK_TELL:
	  return "AK_TELL";
	case AK_EXTINGUISH:
	  return "AK_EXTINGUISH";
	case AK_RESCUE:
	  return "AK_RESCUE";
	case AK_CLEAR:
	  return "AK_CLEAR";
	case AK_REPAIR:
	  return "AK_REPAIR";
	case AK_CHANNEL:
	  return "AK_CHANNEL";
	}
	LOG_ERROR("Unrecognised header: %d",header);
	return "Unrecognised header";
  }

  /*
  std::string agentType(AgentType type) {
	std::string result;
	if (type & AGENT_TYPE_CIVILIAN) {
	  result.append("Civilian");
	}
	if (type & AGENT_TYPE_FIRE_BRIGADE) {
	  if (!result.empty()) result.append(", ");
	  result.append("Fire brigade");
	}
	if (type & AGENT_TYPE_POLICE_FORCE) {
	  if (!result.empty()) result.append(", ");
	  result.append("Police force");
	}
	if (type & AGENT_TYPE_AMBULANCE_TEAM) {
	  if (!result.empty()) result.append(", ");
	  result.append("Ambulance team");
	}
	if (type & AGENT_TYPE_FIRE_STATION) {
	  if (!result.empty()) result.append(", ");
	  result.append("Fire station");
	}
	if (type & AGENT_TYPE_POLICE_OFFICE) {
	  if (!result.empty()) result.append(", ");
	  result.append("Police office");
	}
	if (type & AGENT_TYPE_AMBULANCE_CENTER) {
	  if (!result.empty()) result.append(", ");
	  result.append("Ambulance center");
	}
	if (result.empty()) return "No agent type";
	return result;
  }
  */

  StopWatch::StopWatch() : m_start(clock()) {
  }

  StopWatch::StopWatch(std::string msg) : m_msg(msg), m_start(clock()) {
  }

  StopWatch::~StopWatch() {
	clock_t total = clock()-m_start;
	std::cout << (m_msg.empty()?"Activity":m_msg.c_str()) << " took " << total << " tics ("<< ((double)total)/CLOCKS_PER_SEC << "s)" << std::endl;
  }
}
