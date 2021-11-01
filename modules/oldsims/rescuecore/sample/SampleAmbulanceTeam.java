/*
 * Last change: $Date: 2004/05/04 03:09:38 $
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
import rescuecore.event.*;
import rescuecore.commands.AKChannel;
import java.util.*;

/**
   This is a sample implementation of an ambulance team. This agent will attempt to rescue the closest humanoid it knows about. If it doesn't know anything then it moves randomly.
*/
public class SampleAmbulanceTeam extends PlatoonAgent {
	private final static byte CHANNEL = 3;

    /**
       A list of known targets
    */
    private List<Humanoid> targets;

    /**
       Construct a new SampleAmbulanceTeam
    */
    public SampleAmbulanceTeam() {
		super(RescueConstants.TYPE_AMBULANCE_TEAM); // We need to specify that we can only be an ambulance team
		targets = new ArrayList<Humanoid>();
    }

    /**
       Get a reference the the AmbulanceTeam controlled by this agent
       @return the AmbulanceTeam controlled by this agent
    */
    private AmbulanceTeam me() {
		return (AmbulanceTeam)memory.lookup(id);
    }

    public void initialise(RescueObject[] knowledge) {
		// Add a memory listener so that we get informed about changes to humanoids
		super.initialise(knowledge);
		memory.addMemoryListener(new MemoryListener() {
				public void objectAdded(ObjectAddedEvent event) {
					RescueObject o = event.getObject();
					if (o.isHumanoid()) {
						Humanoid h = (Humanoid)o;
						// If the object is a buried humanoid, and we know the position of the humanoid, then add it to our target list
						if ((h.isBuried() || h.isDamaged()) && memory.lookup(h.getPosition())!=null) {
							if (!targets.contains(h)) {
								targets.add(h);
								//								System.out.println(SampleAmbulanceTeam.this+" adding new target "+h);
							}
						}
					}
				}
				public void objectChanged(ObjectChangedEvent event) {
					RescueObject o = event.getObject();
					if (o.isHumanoid()) {
						Humanoid h = (Humanoid)o;
						// If the object is a buried humanoid, and we know the position of the humanoid, then add it to our target list
						if ((h.isBuried() || h.isDamaged()) && memory.lookup(h.getPosition())!=null) {
							if (!targets.contains(h)) {
								targets.add(h);
								//								System.out.println(SampleAmbulanceTeam.this+" adding changed target "+h);
							}
						}
						else targets.remove(h); // Otherwise remove it
					}
				}
			});
    }

    public void sense() {
		// Is this the first timestep?
		if (timeStep==1) {
			// Listen on the right channel
			appendCommand(new AKChannel(id,timeStep,CHANNEL));
		}
		SampleSearch.sortByDistance(targets,me(),memory);
		for (Humanoid next : targets) {
			if (next==me()) continue; // Ignore me
			// Am I transporting someone to a refuge?
			if (next.getPosition()==id) {
				// Am I at a refuge?
				if (getLocation() instanceof Refuge) {
					unload();
					tell("Unloading".getBytes(),CHANNEL);
					//					System.out.println(this+" unloading");
					return;
				}
				else {
					// Plan a path to a refuge
					List<RescueObject> refuges = new ArrayList<RescueObject>();
					memory.getObjectsOfType(refuges,RescueConstants.TYPE_REFUGE);
					SampleSearch.sortByDistance(refuges,me(),memory);
					int[] path = SampleSearch.breadthFirstSearch(getLocation(),refuges.iterator().next(),memory);
					move(path);
					//					System.out.println(this+" heading for refuge");
					return;
				}
			}
			// Am I at the same location as this target (and not at a refuge)?
			if (!(getLocation() instanceof Refuge) && next.getPosition()==getPosition()) {
				//				System.out.println(this+" at same position as "+next);
				//				System.out.println(this+" buriedness = "+next.getBuriedness()+", damage="+next.getDamage());
				if (next.isBuried()) {
					// Yes! Dig him out
					//					System.out.println(this+" rescueing "+next);
					rescue(next);
					tell(("Rescueing "+next.getID()).getBytes(),CHANNEL);
					return;
				}
				else if (next.isDamaged()) {
					// Load him
					System.out.println(this+" loading "+next);
					load(next);
					tell(("Loading "+next.getID()).getBytes(),CHANNEL);
					return;
				}
			}
		}
		// Try to plan a path to the next best target
		for (Iterator<Humanoid> it = targets.iterator();it.hasNext();) {
			Humanoid next = it.next();
			RescueObject targetLocation = memory.lookup(next.getPosition());
			if (!next.isBuried() || targetLocation==null) {
				// If the target is not buried or we don't know its location then remove it from our target list
				it.remove();
				continue;
			}
			int[] path = SampleSearch.breadthFirstSearch(getLocation(),targetLocation,memory); // Find a path from my current location to the target's location
			if (path!=null) {
				// We've found a path. Hooray!
				// Send a move command and we're finished
				//				System.out.println(this+" moving to "+next);
				move(path);
				return;
			}
		}
		// We couldn't find a good target. Pick a random road and try moving there instead
		Collection<RescueObject> allRoads = memory.getObjectsOfType(RescueConstants.TYPE_ROAD); // Find all roads
		Road[] roads = new Road[allRoads.size()];
		allRoads.toArray(roads);
		Road target = (Road)roads[(int)(Math.random()*roads.length)]; // Pick one at random
		// Plan a path
		int[] path = SampleSearch.breadthFirstSearch(getLocation(),target,memory);
		if (path!=null) {
			//			System.out.println(this+" moving randomly");
			move(path); // Move if the path is valid
		}
		else {
			// If we couldn't move randomly then just give up
			//			System.out.println(this+" giving up");
		}
    }

    protected void hear(int from, byte[] msg, byte channel) {
		//		System.out.println("Received message from "+from+": "+String.valueOf(msg));
    }
}
