/*
 * Last change: $Date: 2004/05/20 23:41:59 $
 * $Revision: 1.9 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore;

/**
   A whole pile of useful constants
 */
public final class RescueConstants {
    private RescueConstants() {}

    public final static int MAX_EXTINGUISH_DISTANCE = 30000;
    public final static int MAX_EXTINGUISH_POWER = 1000;
    public final static int MAX_WATER = 15000;
    public final static int MAX_RESCUE_DISTANCE = 30000;
    public final static int MAX_HP = 10000;
    public final static int MAX_VISION = 10000;

    public final static int SAY_LENGTH = 256;
    public final static int TELL_LENGTH = 256;
    public final static int MAX_PLATOON_MESSAGES = 4;
    public final static int MAX_CENTER_MESSAGES = 2;

    public final static int COMPONENT_TYPE_AGENT = 1;
    public final static int COMPONENT_TYPE_SIMULATOR = 2;
    public final static int COMPONENT_TYPE_VIEWER = 3;

    public final static int BYTE_SIZE = 1;
    public final static int SHORT_SIZE = 2;
    public final static int INT_SIZE = 4;

    public final static int FIERYNESS_NOT_BURNT = 0;
    public final static int FIERYNESS_HEATING = 1;
    public final static int FIERYNESS_BURNING = 2;
    public final static int FIERYNESS_INFERNO = 3;
    public final static int FIERYNESS_WATER_DAMAGE = 4;
    public final static int FIERYNESS_SLIGHTLY_BURNT = 5;
    public final static int FIERYNESS_MODERATELY_BURNT = 6;
    public final static int FIERYNESS_VERY_BURNT = 7;
    public final static int FIERYNESS_BURNT_OUT = 8;
    public final static int NUM_FIERYNESS_LEVELS = 9;

    public final static int TYPE_NULL = 0;
    public final static int TYPE_WORLD = 0x01;
    public final static int TYPE_ROAD = 0x02;
    public final static int TYPE_RIVER = 0x03;
    public final static int TYPE_NODE = 0x04;
    public final static int TYPE_RIVER_NODE = 0x05;
    public final static int TYPE_BUILDING = 0x20;
    public final static int TYPE_REFUGE = 0x21;
    public final static int TYPE_FIRE_STATION = 0x22;
    public final static int TYPE_AMBULANCE_CENTER = 0x23;
    public final static int TYPE_POLICE_OFFICE = 0x24;
    public final static int TYPE_CIVILIAN = 0x40;
    public final static int TYPE_CAR = 0x41;
    public final static int TYPE_FIRE_BRIGADE = 0x42;
    public final static int TYPE_AMBULANCE_TEAM = 0x43;
    public final static int TYPE_POLICE_FORCE = 0x44;

    public final static int AGENT_TYPE_CIVILIAN = 0x01;
    public final static int AGENT_TYPE_FIRE_BRIGADE = 0x02;
    public final static int AGENT_TYPE_FIRE_STATION = 0x04;
    public final static int AGENT_TYPE_AMBULANCE_TEAM = 0x08;
    public final static int AGENT_TYPE_AMBULANCE_CENTER = 0x10;
    public final static int AGENT_TYPE_POLICE_FORCE = 0x20;
    public final static int AGENT_TYPE_POLICE_OFFICE = 0x40;
    public final static int AGENT_TYPE_ANY_MOBILE = AGENT_TYPE_FIRE_BRIGADE | AGENT_TYPE_AMBULANCE_TEAM | AGENT_TYPE_POLICE_FORCE;
    public final static int AGENT_TYPE_ANY_BUILDING = AGENT_TYPE_FIRE_STATION | AGENT_TYPE_AMBULANCE_CENTER | AGENT_TYPE_POLICE_OFFICE;
    public final static int AGENT_TYPE_ANY_AGENT = AGENT_TYPE_ANY_MOBILE | AGENT_TYPE_ANY_BUILDING;
    public final static int AGENT_TYPE_ANY = AGENT_TYPE_ANY_MOBILE | AGENT_TYPE_ANY_BUILDING | AGENT_TYPE_CIVILIAN;

    public final static int PROPERTY_NULL = 0;
    public final static int PROPERTY_MIN = 1;
    public final static int PROPERTY_START_TIME = 1;
    public final static int PROPERTY_LONGITUDE = 2;
    public final static int PROPERTY_LATITUDE = 3;
    public final static int PROPERTY_WIND_FORCE = 4;
    public final static int PROPERTY_WIND_DIRECTION = 5;

    public final static int PROPERTY_HEAD = 6;
    public final static int PROPERTY_TAIL = 7;
    public final static int PROPERTY_LENGTH = 8;

    public final static int PROPERTY_ROAD_KIND = 9;
    public final static int PROPERTY_CARS_PASS_TO_HEAD = 10;
    public final static int PROPERTY_CARS_PASS_TO_TAIL = 11;
    public final static int PROPERTY_HUMANS_PASS_TO_HEAD = 12;
    public final static int PROPERTY_HUMANS_PASS_TO_TAIL = 13;
    public final static int PROPERTY_WIDTH = 14;
    public final static int PROPERTY_BLOCK = 15;
    public final static int PROPERTY_REPAIR_COST = 16;
    public final static int PROPERTY_MEDIAN_STRIP = 17;
    public final static int PROPERTY_LINES_TO_HEAD = 18;
    public final static int PROPERTY_LINES_TO_TAIL = 19;
    public final static int PROPERTY_WIDTH_FOR_WALKERS = 20;
    public final static int PROPERTY_SIGNAL = 21;
    public final static int PROPERTY_SHORTCUT_TO_TURN = 22;
    public final static int PROPERTY_POCKET_TO_TURN_ACROSS = 23;
    public final static int PROPERTY_SIGNAL_TIMING = 24;

    public final static int PROPERTY_X = 25;
    public final static int PROPERTY_Y = 26;
    public final static int PROPERTY_EDGES = 27;

    public final static int PROPERTY_FLOORS = 28;
    public final static int PROPERTY_BUILDING_ATTRIBUTES = 29;
    public final static int PROPERTY_IGNITION = 30;
    public final static int PROPERTY_FIERYNESS = 31;
    public final static int PROPERTY_BROKENNESS = 32;
    public final static int PROPERTY_ENTRANCES = 33;
    public final static int PROPERTY_BUILDING_CODE = 34;
    public final static int PROPERTY_BUILDING_AREA_GROUND = 35;
    public final static int PROPERTY_BUILDING_AREA_TOTAL = 36;
    public final static int PROPERTY_BUILDING_APEXES = 37;

    public final static int PROPERTY_POSITION = 38;
    public final static int PROPERTY_POSITION_EXTRA = 39;
    public final static int PROPERTY_DIRECTION = 40;
    public final static int PROPERTY_POSITION_HISTORY = 41;
    public final static int PROPERTY_STAMINA = 42;
    public final static int PROPERTY_HP = 43;
    public final static int PROPERTY_DAMAGE = 44;
    public final static int PROPERTY_BURIEDNESS = 45;
    public final static int PROPERTY_WATER_QUANTITY = 46;

    public final static int PROPERTY_BUILDING_TEMPERATURE = 47;
    public final static int PROPERTY_BUILDING_IMPORTANCE = 48;

    public final static int PROPERTY_MAX = 48;

    public final static int HEADER_NULL = 0;

    public final static int KG_CONNECT = 0x10;
    public final static int KG_ACKNOWLEDGE = 0x11;
    public final static int GK_CONNECT_OK = 0x12;
    public final static int GK_CONNECT_ERROR = 0x13;

    public final static int SK_CONNECT = 0x20;
    public final static int SK_ACKNOWLEDGE = 0x21;
    public final static int SK_UPDATE = 0x22;
    public final static int KS_CONNECT_OK = 0x23;
    public final static int KS_CONNECT_ERROR = 0x24;

    public final static int VK_CONNECT = 0x30;
    public final static int VK_ACKNOWLEDGE = 0x31;
    public final static int KV_CONNECT_OK = 0x32;
    public final static int KV_CONNECT_ERROR = 0x33;

    public final static int AK_CONNECT = 0x40;
    public final static int AK_ACKNOWLEDGE = 0x41;
    public final static int KA_CONNECT_OK = 0x42;
    public final static int KA_CONNECT_ERROR = 0x43;
    public final static int KA_SENSE = 0x44;
    public final static int KA_HEAR = 0x45;
    public final static int KA_HEAR_SAY = 0x46;
    public final static int KA_HEAR_TELL = 0x47;

    public final static int UPDATE = 0x50;
    public final static int COMMANDS = 0x51;

    public final static int AK_REST = 0x80;
    public final static int AK_MOVE = 0x81;
    public final static int AK_LOAD = 0x82;
    public final static int AK_UNLOAD = 0x83;
    public final static int AK_SAY = 0x84;
    public final static int AK_TELL = 0x85;
    public final static int AK_EXTINGUISH = 0x86;
    //    public final static int AK_STRETCH = 0x87;
    public final static int AK_RESCUE = 0x88;
    public final static int AK_CLEAR = 0x89;
    public final static int AK_CHANNEL = 0x90;
    public final static int AK_REPAIR = 0x91; //changed from 90

    public final static Object SOURCE_SENSE = "KA_SENSE";
    public final static Object SOURCE_INITIAL = "KA_CONNECT_OK";
    public final static Object SOURCE_UNKNOWN = "Unknown";
    public final static Object SOURCE_NONE = "None";
    public final static Object SOURCE_ASSUMED = "Assumed";
    public final static Object SOURCE_UPDATE = "Update";

    public final static int VALUE_UNKNOWN = -2;
    public final static int VALUE_ASSUMED = -1;
}
