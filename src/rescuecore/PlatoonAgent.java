/*
 * Last change: $Date: 2004/05/20 23:41:59 $
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

package rescuecore;

import rescuecore.objects.*;
import rescuecore.commands.Command;

/**
   An abstract superclass for platoon agents (i.e fire brigade, police force, ambulance team).
 */
public abstract class PlatoonAgent extends Agent {
    protected PlatoonAgent(int... types) {
		super(types);
    }

    private Humanoid me() {
		return (Humanoid)memory.lookup(id);
    }

    /**
       Get this agent's current position as a RescueObject
       @return The RescueObject representing this agent's current position
	*/
    protected RescueObject getLocation() {
		return memory.lookup(me().getPosition());
    }

    /**
       Get this agent's current position
       @return This agent's current position
	*/
    protected int getPosition() {
		return me().getPosition();
    }

     /**
        Get this agent's current position
        @return This agent's current position
 	*/
	protected int getPositionExtra() {
		return me().getPositionExtra();
    }

    /**
       Append a move command
       @param ids The list of road/node/building ids that we want to move through
	*/
    protected void move(int[] ids) {
		if (ids==null || ids.length==0) return;
		appendCommand(Command.MOVE(id,timeStep,ids));
    }

    /**
       Append an extinguish command
       @param target The building to extinguish
	*/
    protected void extinguish(Building target) {
		try {
			int[] xy = memory.getXY(me());
			appendCommand(Command.EXTINGUISH(id,timeStep,target.getID()));
		}
		catch (CannotFindLocationException e) {
			System.err.println(e);
		}
    }

    /**
       Append an extinguish command
       @param target The building to extinguish
       @param power The amount of water to use
	*/
    protected void extinguish(Building target, int power) {
		try {
			int[] xy = memory.getXY(me());
			appendCommand(Command.EXTINGUISH(id,timeStep,target.getID(),memory.getAngle(me(),target),xy[0],xy[1],power));
		}
		catch (CannotFindLocationException e) {
			System.err.println(e);
		}
    }

    /**
       Append a clear command
       @param target The road to clear
	*/
    protected void clear(Road target) {
		appendCommand(Command.CLEAR(id,timeStep,target.getID()));
    }

    /**
       Append a rescue command
       @param target The humanoid to rescue
	*/
    protected void rescue(Humanoid target) {
		appendCommand(Command.RESCUE(id,timeStep,target.getID()));
    }

    /**
       Append a load command
       @param target The humanoid to load
	*/
    protected void load(Humanoid target) {
		appendCommand(Command.LOAD(id,timeStep,target.getID()));
    }

    /**
       Append an unload command
	*/
    protected void unload() {
		appendCommand(Command.UNLOAD(id,timeStep));
    }
}
