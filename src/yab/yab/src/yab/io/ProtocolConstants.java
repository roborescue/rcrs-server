// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

public interface ProtocolConstants {
    /** @see "<code>port</code> of the RUN/config.txt" */
    static final int KERNEL_LISTENING_PORT = 7000;

    /** @see "<code>send_udp_size</code> of the RUN/config.txt" */
    static final int UDP_PACKET_SIZE = 1472; //[byte]

    /** @see "<code>System::agentConnect()</code> of the kernel/System.cxx" */
    static final String REASON_OF_KA_CONNECT_ERROR = "No more agents\u0000";     

static final int
        HEADER_NULL      = 0x00,
        AK_CONNECT       = 0x40,
        AK_ACKNOWLEDGE   = 0x41,
        KA_CONNECT_OK    = 0x42,
        KA_CONNECT_ERROR = 0x43,
        KA_SENSE         = 0x44,
        KA_HEAR          = 0x45,
        KA_HEAR_SAY 	 = 0x46,
        KA_HEAR_TELL	 = 0x47,
        AK_REST          = 0x80,
        AK_MOVE          = 0x81,
        AK_LOAD          = 0x82,
        AK_UNLOAD        = 0x83,
//        AK_SAY           = 0x84,
        AK_TELL          = 0x85,
        AK_EXTINGUISH    = 0x86,
		//AK_STRETCH       = 0x87;
        AK_RESCUE        = 0x88,
        AK_CLEAR         = 0x89,
        AK_CHANNEL	 = 0x90;

    static final int
        TYPE_NULL             = 0,
        TYPE_WORLD            = 0x01,
        TYPE_ROAD             = 0x02,
        TYPE_NODE             = 0x04,
        TYPE_RIVER            = 0x03,
        TYPE_RIVER_NODE       = 0x05,
        TYPE_BUILDING         = 0x20,
        TYPE_REFUGE           = 0x21,
        TYPE_FIRE_STATION     = 0x22,
        TYPE_AMBULANCE_CENTER = 0x23,
        TYPE_POLICE_OFFICE    = 0x24,
        TYPE_CIVILIAN         = 0x40,
        TYPE_CAR              = 0x41,
        TYPE_FIRE_BRIGADE     = 0X42,
        TYPE_AMBULANCE_TEAM   = 0X43,
        TYPE_POLICE_FORCE     = 0X44,
        TYPE_FIRE_COMPANY     = TYPE_FIRE_BRIGADE;

    static final int
        PROPERTY_NULL                  =  0,
        PROPERTY_START_TIME            =  1,
        PROPERTY_LONGITUDE             =  2,
        PROPERTY_LATITUDE              =  3,
        PROPERTY_WIND_FORCE            =  4,
        PROPERTY_WIND_DIRECTION        =  5,
        PROPERTY_HEAD                  =  6,
        PROPERTY_TAIL                  =  7,
        PROPERTY_LENGTH                =  8,
        PROPERTY_ROAD_KIND             =  9,
        PROPERTY_CARS_PASS_TO_HEAD     =  10,
        PROPERTY_CARS_PASS_TO_TAIL     =  11,
        PROPERTY_HUMANS_PASS_TO_HEAD   =  12,
        PROPERTY_HUMANS_PASS_TO_TAIL   =  13,
        PROPERTY_WIDTH                 =  14,
        PROPERTY_BLOCK                 =  15,
        PROPERTY_REPAIR_COST           =  16,
        PROPERTY_MEDIAN_STRIP          =  17,
        PROPERTY_LINES_TO_HEAD         =  18,
        PROPERTY_LINES_TO_TAIL         =  19,
        PROPERTY_WIDTH_FOR_WALKERS     =  20,
        PROPERTY_SIGNAL                =  21,
        PROPERTY_SHORTCUT_TO_TURN      =  22,
        PROPERTY_POCKET_TO_TURN_ACROSS =  23,
        PROPERTY_SIGNAL_TIMING         =  24,
        PROPERTY_X                     =  25,
        PROPERTY_Y                     =  26,
        PROPERTY_EDGES                 =  27,
        PROPERTY_FLOORS                =  28,
        PROPERTY_BUILDING_ATTRIBUTES   =  29,
        PROPERTY_IGNITION              =  30,
        PROPERTY_FIERYNESS             =  31,
        PROPERTY_BROKENNESS            =  32,
        PROPERTY_ENTRANCES             =  33,
        PROPERTY_BUILDING_CODE         =  34,
        PROPERTY_BUILDING_AREA_GROUND  =  35,
        PROPERTY_BUILDING_AREA_TOTAL   =  36,
        PROPERTY_BUILDING_APEXES       =  37,
        PROPERTY_POSITION              =  38,
        PROPERTY_POSITION_EXTRA        =  39,
        PROPERTY_DIRECTION             =  40,
        PROPERTY_POSITION_HISTORY      =  41,
        PROPERTY_STAMINA               =  42,
        PROPERTY_HP                    =  43,
        PROPERTY_DAMAGE                =  44,
        PROPERTY_BURIEDNESS            =  45,
        PROPERTY_WATER_QUANTITY        =  46,
        PROPERTY_HEAT                  =  47;

        //PROPERTY_STRETCHED_LENGTH      =  26;


    static final int
        AGENT_TYPE_CIVILIAN         =  0X01,
        AGENT_TYPE_FIRE_BRIGADE     =  0X02,
        AGENT_TYPE_FIRE_STATION     =  0X04,
        AGENT_TYPE_AMBULANCE_TEAM   =  0X08,
        AGENT_TYPE_AMBULANCE_CENTER =  0X10,
        AGENT_TYPE_POLICE_FORCE     =  0X20,
        AGENT_TYPE_POLICE_OFFICE    =  0X40;
}
