/*
 * Last change: $Date: 2004/05/04 03:09:38 $
 * $Revision: 1.2 $
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

package rescuecore.event;

import rescuecore.*;

/**
   This class will filter which events get passed to a MemoryListener
 */
public class FilterMemoryListener implements MemoryListener {
	private int mode;
	private Class clazz;
	private int type;
	private MemoryListener listener;

	public final static int MODE_CLASS = 0;
	public final static int MODE_TYPE = 1;

	private FilterMemoryListener(int mode, Class clazz, int type, MemoryListener l) {
		this.mode = mode;
		this.clazz = clazz;
		this.type = type;
		this.listener = l;
	}

	/**
	   Create a new FilterMemoryListener that will only pass on notifications that concern RescueObjects of a particular class
	   @param clazz The Class we are interested in
	   @param l The MemoryListener to pass qualifying updates down to
	   @return A MemoryListener that will filter out all notifications that do not concern RescueObjects of the given class
	*/
	public static MemoryListener createClassFilter(Class clazz, MemoryListener l) {
		return new FilterMemoryListener(MODE_CLASS,clazz,0,l);
	}

	/**
	   Create a new FilterMemoryListener that will only pass on notifications that concern RescueObjects of a particular type
	   @param type The type we are interested in
	   @param l The MemoryListener to pass qualifying updates down to
	   @return A MemoryListener that will filter out all notifications that do not concern RescueObjects of the given type
	   @see RescueConstants#TYPE_CIVILIAN
	   @see RescueConstants#TYPE_FIRE_BRIGADE
	   @see RescueConstants#TYPE_AMBULANCE_TEAM
	   @see RescueConstants#TYPE_POLICE_FORCE
	   @see RescueConstants#TYPE_ROAD
	   @see RescueConstants#TYPE_NODE
	   @see RescueConstants#TYPE_RIVER
	   @see RescueConstants#TYPE_RIVER_NODE
	   @see RescueConstants#TYPE_BUILDING
	   @see RescueConstants#TYPE_REFUGE
	   @see RescueConstants#TYPE_FIRE_STATION
	   @see RescueConstants#TYPE_AMBULANCE_CENTER
	   @see RescueConstants#TYPE_POLICE_OFFICE
	   @see RescueConstants#TYPE_WORLD
	   @see RescueConstants#TYPE_CAR
	*/
	public static MemoryListener createTypeFilter(int type, MemoryListener l) {
		return new FilterMemoryListener(MODE_TYPE,null,type,l);
	}

	public void objectAdded(ObjectAddedEvent event) {
		if (filter(event.getObject())) listener.objectAdded(event);
	}

	public void objectChanged(ObjectChangedEvent event) {
		if (filter(event.getObject())) listener.objectChanged(event);
	}

	private boolean filter(RescueObject o) {
		switch(mode) {
		case MODE_CLASS:
			return (clazz.isInstance(o));
		case MODE_TYPE:
			return (o.getType()==type);
		}
		return false;
	}
}
