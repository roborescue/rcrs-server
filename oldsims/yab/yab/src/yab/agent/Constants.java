// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

public interface Constants {
    static final int
        TIME_INITIALIZING_AGENT = Integer.MIN_VALUE, // [cycle]
        TIME_STARTING_ACTION = 3, // [cycle]
        UTTERANCE_LIMIT = 4, // [message/cycle]
        HEARING_LIMIT_OF_HUMANOID = 4; // [message/cycle]

    /** @see "<code>period</code> of the RUN/config.txt" */
    static final int SIMULATING_TIME = 300; // [cycle]

    /** @see "<code>max_extinguish_length</code> of the kernel/parameters.hxx"
     */
    static final int EXTINGUISHABLE_DISTANCE = 30 * 1000; // [mm]

    /** @see "<code>max_extinguish_power</code> of the kernel/parameters.hxx"
     */
    static final int EXTINGUISHABLE_QUANTITY = 1000; // [0.001 m^3]

	static final int CHANNEL_SAY = 0;	

}
