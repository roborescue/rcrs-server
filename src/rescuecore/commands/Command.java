/*
 * Last change: $Date: 2005/06/14 21:55:52 $
 * $Revision: 1.3 $
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

package rescuecore.commands;

import rescuecore.RescueConstants;
import rescuecore.Handy;
import rescuecore.InputBuffer;
import rescuecore.OutputBuffer;

/**
   This class encapsulates a command.
   @see RescueConstants#COMMAND_MOVE
   @see RescueConstants#COMMAND_EXTINGUISH
   @see RescueConstants#COMMAND_CLEAR
   @see RescueConstants#COMMAND_LOAD
   @see RescueConstants#COMMAND_UNLOAD
   @see RescueConstants#COMMAND_RESCUE
 */
public abstract class Command implements java.io.Serializable {
    protected int type;

    /**
       Construct a new Command from a byte array
       @param type The type of this command
       @see RescueConstants#COMMAND_MOVE
       @see RescueConstants#COMMAND_EXTINGUISH
       @see RescueConstants#COMMAND_CLEAR
       @see RescueConstants#COMMAND_LOAD
       @see RescueConstants#COMMAND_UNLOAD
       @see RescueConstants#COMMAND_RESCUE
    */
    protected Command(int type) {
        this.type = type;
    }

    /**
       Get the type of this command
       @return This command's type
       @see RescueConstants#COMMAND_MOVE
       @see RescueConstants#COMMAND_EXTINGUISH
       @see RescueConstants#COMMAND_CLEAR
       @see RescueConstants#COMMAND_LOAD
       @see RescueConstants#COMMAND_UNLOAD
       @see RescueConstants#COMMAND_RESCUE
    */
    public final int getType() {
        return type;
    }

    /**
       Write this command to an OutputBuffer
       @param The OutputBuffer to write to
    */
    public abstract void write(OutputBuffer out);

    /**
       Read command data from an InputBuffer
       @param The InputBuffer to read from
    */
    public abstract void read(InputBuffer in);

    public String toString() {
        return Handy.getCommandTypeName(type);
    }

    /**
       Create an EXTINGUISH command that will use the maximum extinguish power and ignore the direction,x and y parameters
       @param agentID The id of the agent sending the extinguish command
       @param time The time of the command.
       @param targetID The id of the burning building
       @return A filled-in EXTINGUISH command
    */
    public static Command EXTINGUISH(int agentID, int time, int targetID) {
        return EXTINGUISH(agentID,time,targetID,0,0,0,RescueConstants.MAX_EXTINGUISH_POWER);
    }

    /**
       Create an EXTINGUISH command that will use the maximum extinguish power
       @param agentID The id of the agent sending the extinguish command
       @param time The time of the command.
       @param targetID The id of the burning building
       @param direction The direction from the nozzle to the building
       @param x The x coordinate of the nozzle
       @param y The y coordinate of the nozzle
       @return A filled-in EXTINGUISH command
    */
    public static Command EXTINGUISH(int agentID, int time, int targetID, int direction, int x, int y) {
        return EXTINGUISH(agentID,time,targetID,direction,x,y,RescueConstants.MAX_EXTINGUISH_POWER);
    }

    /**
       Create an EXTINGUISH command
       @param agentID The id of the agent sending the extinguish command
       @param time The time of the command.
       @param targetID The id of the burning building
       @param direction The direction from the nozzle to the building
       @param x The x coordinate of the nozzle
       @param y The y coordinate of the nozzle
       @param water The amount of water to extinguish with, in 1/1000 m^3/min
       @return A filled-in EXTINGUISH command
    */
    public static Command EXTINGUISH(int agentID, int time, int targetID, int direction, int x, int y, int water) {
        return new AKExtinguish(agentID, time, targetID, direction, x, y, water);
    }

    /**
       Create a CLEAR command
       @param id The id of the agent sending the clear command
       @param time The time of the command.
       @param target The id of the road to be cleared
       @return A filled-in CLEAR command
    */
    public static Command CLEAR(int id, int time, int target) {
        return new AKClear(id,time,target);
    }

    /**
       Create a RESCUE command
       @param id The id of the agent sending the rescue command
       @param time The time of the command.
       @param target The id of the civilian to be rescued
       @return A filled-in RESCUE command
    */
    public static Command RESCUE(int id, int time, int target) {
        return new AKRescue(id,time,target);
    }

    /**
       Create a LOAD command
       @param id The id of the agent sending the load command
       @param time The time of the command.
       @param target The id of the civilian to be loaded
       @return A filled-in LOAD command
    */
    public static Command LOAD(int id, int time, int target) {
        return new AKLoad(id,time,target);
    }

    /**
       Create an UNLOAD command
       @param id The id of the agent sending the unload command
       @param time The time of the command.
       @return A filled-in UNLOAD command
    */
    public static Command UNLOAD(int id, int time) {
        return new AKUnload(id,time);
    }

    /**
       Create a MOVE command
       @param id The id of the agent sending the move command
       @param time The time of the command.
       @param path A list of ids (nodes, roads, rivers, rivernodes, buildings) the describe the path
       @return A filled-in MOVE command
    */
    public static Command MOVE(int id, int time, int[] path) {
        return new AKMove(id,time,path);
    }

    /**
       Create a SAY command
       @param id The id of the agent saying something
       @param time The time of the command.
       @param msg The message
       @return A filled-in SAY command
    */
    public static Command SAY(int id, int time, byte[] msg, int length) {
        return new AKSay(id,time,msg,length);
    }

    /**
       Create an TELL command
       @param id The id of the agent saying something
       @param time The time of the command.
       @param msg The message
       @param channel The channel to use
       @return A filled-in TELL command
    */
    public static Command TELL(int id, int time, byte[] msg, int length, byte channel) {
        return new AKTell(id,time,msg,length,channel);
    }
}
