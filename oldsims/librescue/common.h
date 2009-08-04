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
 Mohammad Mehdi Saboorian / channel based communication
*/

#ifndef RESCUE_COMMON_H
#define RESCUE_COMMON_H

#include <vector>
#include <list>
#include <set>
#include <queue>

#define INT_32_SIZE 4
#define INT_16_SIZE 2

namespace Librescue {

  typedef int INT_32;
  typedef short INT_16;

  typedef INT_32 Id;
  typedef INT_32 Cursor;
  // For backward compatability
  typedef INT_32 S32;

  class Property;
  class RescueObject;
  class Command;
  class AgentCommand;
  class VoiceCommand;
  
  typedef unsigned char	Byte;
  typedef std::vector<Byte> Bytes;
  //  typedef std::vector<RescueObject*> Objects;
  typedef std::set<RescueObject*> ObjectSet;
  typedef std::vector<Command*> CommandList;
  typedef std::queue<Command*> CommandQueue;
  typedef std::vector<AgentCommand*> AgentCommandList;
  typedef std::vector<VoiceCommand*> VoiceCommandList;
  typedef std::vector<Id> IdList;
  typedef std::set<Id> IdSet;
  typedef std::vector<INT_32> ValueList;

  enum Header {
	HEADER_NULL = 0x00,
	// GIS
	KG_CONNECT = 0x10,
	KG_ACKNOWLEDGE = 0x11,
	GK_CONNECT_OK = 0x12,
	GK_CONNECT_ERROR = 0x13,
	// Simulators
	SK_CONNECT = 0x20,
	SK_ACKNOWLEDGE = 0x21,
	SK_UPDATE = 0x22,
	KS_CONNECT_OK = 0x23,
	KS_CONNECT_ERROR = 0x24,
	// Viewer
	VK_CONNECT = 0x30,
	VK_ACKNOWLEDGE = 0x31,
	KV_CONNECT_OK = 0x32,
	KV_CONNECT_ERROR = 0x33,
	// Agents
	AK_CONNECT = 0x40,
	AK_ACKNOWLEDGE = 0x41,
	KA_CONNECT_OK = 0x42,
	KA_CONNECT_ERROR = 0x43,
	KA_SENSE = 0x44,
	KA_HEAR = 0x45,
	KA_HEAR_SAY = 0x46,
	KA_HEAR_TELL = 0x47,
	// General
	UPDATE = 0x50,
	COMMANDS = 0x51,

	// Agent commands
	AK_REST = 0x80,
	AK_MOVE = 0x81,
	AK_LOAD = 0x82,
	AK_UNLOAD = 0x83,
	AK_SAY = 0x84,
	AK_TELL = 0x85,
	AK_EXTINGUISH = 0x86,
	//	AK_STRETCH = 0x87,
	AK_RESCUE = 0x88,
	AK_CLEAR = 0x89,
	AK_CHANNEL = 0x90,
	AK_REPAIR = 0x91 // changed from 90
  };

  enum TypeId {
	TYPE_NULL = 0,
	TYPE_WORLD = 0x01,
	TYPE_ROAD = 0x02,
	TYPE_RIVER = 0x03,
	TYPE_NODE = 0x04,
	TYPE_RIVER_NODE = 0x05,
	TYPE_BUILDING = 0x20,
	TYPE_REFUGE = 0x21,
	TYPE_FIRE_STATION = 0x22,
	TYPE_AMBULANCE_CENTER = 0x23,
	TYPE_POLICE_OFFICE = 0x24,
	TYPE_CIVILIAN = 0x40,
	TYPE_CAR = 0x41,
	TYPE_FIRE_BRIGADE = 0x42,
	TYPE_AMBULANCE_TEAM = 0x43,
	TYPE_POLICE_FORCE = 0x44
  };

  enum PropertyId {
	PROPERTY_NULL = 0,
	PROPERTY_MIN = 1,
	PROPERTY_START_TIME = 1,
	PROPERTY_LONGITUDE = 2,
	PROPERTY_LATITUDE = 3,
	PROPERTY_WIND_FORCE = 4,
	PROPERTY_WIND_DIRECTION = 5,

	PROPERTY_HEAD = 6,
	PROPERTY_TAIL = 7,
	PROPERTY_LENGTH = 8,

	PROPERTY_ROAD_KIND = 9,
	PROPERTY_CARS_PASS_TO_HEAD = 10,
	PROPERTY_CARS_PASS_TO_TAIL = 11,
	PROPERTY_HUMANS_PASS_TO_HEAD = 12,
	PROPERTY_HUMANS_PASS_TO_TAIL = 13,
	PROPERTY_WIDTH = 14,
	PROPERTY_BLOCK = 15,
	PROPERTY_REPAIR_COST = 16,
	PROPERTY_MEDIAN_STRIP = 17,
	PROPERTY_LINES_TO_HEAD = 18,
	PROPERTY_LINES_TO_TAIL = 19,
	PROPERTY_WIDTH_FOR_WALKERS = 20,
	PROPERTY_SIGNAL = 21,
	PROPERTY_SHORTCUT_TO_TURN = 22,
	PROPERTY_POCKET_TO_TURN_ACROSS = 23,
	PROPERTY_SIGNAL_TIMING = 24,

	PROPERTY_X = 25,
	PROPERTY_Y = 26,
	PROPERTY_EDGES = 27,

	PROPERTY_FLOORS = 28,
	PROPERTY_BUILDING_ATTRIBUTES = 29,
	PROPERTY_IGNITION = 30,
	PROPERTY_FIERYNESS = 31,
	PROPERTY_BROKENNESS = 32,
	PROPERTY_ENTRANCES = 33,
	PROPERTY_BUILDING_CODE = 34,
	PROPERTY_BUILDING_AREA_GROUND = 35,
	PROPERTY_BUILDING_AREA_TOTAL = 36,
	PROPERTY_BUILDING_APEXES = 37,

	PROPERTY_POSITION = 38,
	PROPERTY_POSITION_EXTRA = 39,
	PROPERTY_DIRECTION = 40,
	PROPERTY_POSITION_HISTORY = 41,
	PROPERTY_STAMINA = 42,
	PROPERTY_HP = 43,
	PROPERTY_DAMAGE = 44,
	PROPERTY_BURIEDNESS = 45,
	PROPERTY_WATER_QUANTITY = 46,

	PROPERTY_BUILDING_TEMPERATURE = 47,
	PROPERTY_BUILDING_IMPORTANCE = 48,

	PROPERTY_MAX = 48
  };

  enum Capability {
	CAPABILITY_MOVE = 1,
	CAPABILITY_EXTINGUISH = 2,
	CAPABILITY_HOLD_WATER = 3,
	CAPABILITY_RESCUE = 4,
	CAPABILITY_LOAD = 5,
	CAPABILITY_CLEAR = 6,
	CAPABILITY_SPEAK = 7
  };
  
  enum ChannelType
  {
  	CHANNEL_SAY = 1,
  	CHANNEL_RADIO = 2
  };

  /*
  typedef int AgentType;
  const AgentType AGENT_TYPE_CIVILIAN = 0x01;
  const AgentType AGENT_TYPE_FIRE_BRIGADE = 0x02;
  const AgentType AGENT_TYPE_FIRE_STATION = 0x04;
  const AgentType AGENT_TYPE_AMBULANCE_TEAM = 0x08;
  const AgentType AGENT_TYPE_AMBULANCE_CENTER = 0x10;
  const AgentType AGENT_TYPE_POLICE_FORCE = 0x20;
  const AgentType AGENT_TYPE_POLICE_OFFICE = 0x40;
  */

  const INT_32 FIERYNESS_NONE = 0;
  const INT_32 FIERYNESS_LOW = 1;
  const INT_32 FIERYNESS_MEDIUM = 2;
  const INT_32 FIERYNESS_HIGH = 3;
  const INT_32 FIERYNESS_WATER_DAMAGE = 4;
  const INT_32 FIERYNESS_MINOR_DAMAGE = 5;
  const INT_32 FIERYNESS_MODERATE_DAMAGE = 6;
  const INT_32 FIERYNESS_MAJOR_DAMAGE = 7;
  const INT_32 FIERYNESS_BURNT_OUT = 8;

  typedef std::set<PropertyId> PropertySet;
  const Byte CHANNEL_NONE = 255;
}

#endif
