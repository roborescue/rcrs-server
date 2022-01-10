/*
 * Last change: $Date: 2004/07/11 22:26:28 $
 * $Revision: 1.6 $
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

package rescuecore.sample;

import rescuecore.*;
import rescuecore.objects.*;
import rescuecore.commands.AKChannel;

/**
   This is a sample implementation of a center agent. All this agent does is pass on messages.
*/
public class SampleCenter extends CenterAgent {
	private byte agentChannel;

    /**
       Construct a new SampleCenter
    */
    public SampleCenter() {
        super(RescueConstants.TYPE_FIRE_STATION, RescueConstants.TYPE_AMBULANCE_CENTER, RescueConstants.TYPE_POLICE_OFFICE);
        agentChannel = 0;
    }

    /**
       Get a reference the the Building controlled by this agent
       @return the Building controlled by this agent
    */
    private Building me() {
		return (Building)memory.lookup(id);
    }

    public void initialise(RescueObject[] knowledge) {
		super.initialise(knowledge);
		switch (type) {
		case RescueConstants.TYPE_FIRE_STATION:
			agentChannel = 1;
			break;
		case RescueConstants.TYPE_POLICE_OFFICE:
			agentChannel = 2;
			break;
		case RescueConstants.TYPE_AMBULANCE_CENTER:
			agentChannel = 3;
			break;
		}
		//	log("Initialised"); // Log a debugging message
    }

    public void sense() {
		// Is this the first timestep?
		if (timeStep==1) {
			// Listen on the right channels
			byte[] channels = new byte[2];
			// All centers listen on channel 4
			channels[0] = 4;
			channels[1] = agentChannel;
			appendCommand(new AKChannel(id,timeStep,channels));
		}
    }

    protected void hear(int from, byte[] msg, byte channel) {
		//		System.out.println("Received message from "+from+" on channel "+channel+" : "+String.valueOf(msg));
		// Pass the message through to the other channel
		if (channel==4) tell(msg,agentChannel);
		if (channel==agentChannel) tell(msg,(byte)4);
	}

	/**
	   Find out if an id represents one of my platoon agents
	   @param id The id to test
	   @return true if the RescueObject specified by the id is one of my platoon agents, false otherwise
	*/
	private boolean isPlatoon(int id) {
		int agentType = memory.lookup(id).getType(); // What is the type of the agent we heard the message from?
		switch (type) { // What is my type?
		case RescueConstants.TYPE_FIRE_STATION:
			// I'm a fire station, so my platoon agents are fire brigades
			return agentType==RescueConstants.TYPE_FIRE_BRIGADE;
		case RescueConstants.TYPE_POLICE_OFFICE:
			// I'm a police office, so my platoon agents are police forces
			return agentType==RescueConstants.TYPE_POLICE_FORCE;
		case RescueConstants.TYPE_AMBULANCE_CENTER:
			// I'm an ambulance center, so my platoon agents are ambulance teams
			return agentType==RescueConstants.TYPE_AMBULANCE_TEAM;
		}
		throw new RuntimeException("Weird type for "+this+": "+Handy.getTypeName(type));
	}

	/**
	   Find out if an id represents one of the centers
	   @param id The id to test
	   @return true if the RescueObject specified by the id is one of the centers
	*/
	private boolean isCenter(int id) {
		RescueObject target = memory.lookup(id);
		if (target.isFireStation() || target.isAmbulanceCenter() || target.isPoliceOffice()) return true; // Centers are fire stations, police offices and ambulance centers
		return false;
	}
}
