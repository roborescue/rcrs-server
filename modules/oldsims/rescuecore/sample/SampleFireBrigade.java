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
   This is a sample implementation of a fire brigade. This agent will attempt to extinguish the closest fire it knows about. If it doesn't know anything then it moves randomly.
*/
public class SampleFireBrigade extends PlatoonAgent {
    private final static byte CHANNEL = 1;

    private boolean enableDebug;

    /**
       A list of known targets
    */
    private List targets;

    /**
       Construct a new SampleFireBrigade
    */
    public SampleFireBrigade() {
        super(RescueConstants.TYPE_FIRE_BRIGADE);
        targets = new ArrayList();
        enableDebug = false;
    }

    public SampleFireBrigade(String[] args) {
        this();
        for (String next : args) {
            if (next.equalsIgnoreCase("debug")) {
                enableDebug = true;
            }
        }
    }

    /**
       Get a reference to the FireBrigade controlled by this agent
       @return the FireBrigade controlled by this agent
    */
    private FireBrigade me() {
        return (FireBrigade)memory.lookup(id);
    }

    public void initialise(RescueObject[] knowledge) {
        if (enableDebug) {
            try {
                enableDebug("debug.log");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.initialise(knowledge);
        // Add a memory listener so that we get informed about changes to the buildings
        memory.addMemoryListener(new MemoryListener() {
                public void objectAdded(ObjectAddedEvent event) {
                    RescueObject o = event.getObject();
                    if (o.isBuilding()) {
                        if (((Building)o).isOnFire()) targets.add(o); // Add to target list if it is on fire
                    }
                }
                public void objectChanged(ObjectChangedEvent event) {
                    RescueObject o = event.getObject();
                    if (o.isBuilding() && event.getProperty()==RescueConstants.PROPERTY_FIERYNESS) { // We only care about the fieryness of the building - we can ignore all other updates
                        if (((Building)o).isOnFire()) targets.add(o); // Add to target list if it is on fire
                        else targets.remove(o); // Otherwise remove it from the target list
                    }
                }
            });
        //		log("Initialised"); // Log a debugging message
    }

    public void sense() {
        // Is this the first timestep?
        if (timeStep==1) {
            // Listen on the right channel
            appendCommand(new AKChannel(id,timeStep,CHANNEL));
        }

        //		System.out.println("Fire brigade "+id+" water quantity: "+me().getWaterQuantity());
        //		log("Sense"); // Log a debugging message
        // Am I at a refuge?
        if (getLocation().isRefuge() && me().getWaterQuantity()<RescueConstants.MAX_WATER) {
            //			log("Filling at "+getLocation());
            return;
        }
        // Am I out of water?
        if (me().getWaterQuantity()==0) {
            // Find the closest refuge
            moveToClosestRefuge();
            return;
        }
        // Sort the targets by distance from me
        //		log("Sorting targets");
        SampleSearch.sortByDistance(targets,me(),memory);
        // Find a valid target
        for (Iterator it = targets.iterator();it.hasNext();) {
            // Try to plan a path to the next best target
            Building next = (Building)it.next();
            if (!next.isOnFire()) {
                // If the next target is not on fire then remove it from our target list
                it.remove();
                continue;
            }
            try {
                if (memory.getDistance(me(),next) < RescueConstants.MAX_EXTINGUISH_DISTANCE) {
                    //					System.out.println("Extinguishing "+next);
                    extinguish(next);
                    tell(("Extinguishing "+next.getID()).getBytes(),CHANNEL);
                    return;
                }
            }
            catch (CannotFindLocationException e) {
                System.err.println("PANIC! I don't know where I am!");
                System.err.println(e);
            }
            Node entrance = (Node)memory.lookup(next.getEntrances()[0]); // Drive to the first entrance of the building
            //			log("Trying to plan path to "+next+" (entrance "+entrance+")");
            int[] path = SampleSearch.breadthFirstSearch(getLocation(),entrance,memory); // Find a path from my current location to the target
            if (path!=null) {
                // We've found a path. Hooray!
                // Send a move command and we're finished
                //				log("Moving to "+next);
                move(path);
                return;
            }
            //			log("No path");
        }
        // We couldn't find a good target. Pick a random road and try moving there instead
        //		log("No good targets - picking a Road at random");
        Collection<RescueObject> allRoads = memory.getObjectsOfType(RescueConstants.TYPE_ROAD); // Find all roads
        Road[] roads = new Road[allRoads.size()];
        allRoads.toArray(roads);
        Road target = (Road)roads[(int)(Math.random()*roads.length)]; // Pick one at random
        //		log("Random target: "+target+". Planning path");
        // Plan a path
        int[] path = SampleSearch.breadthFirstSearch(getLocation(),target,memory);
        if (path!=null) {
            //			log("Moving to "+target);
            move(path); // Move if the path is valid
        }
        else {
            // If we couldn't move randomly then just give up
            //			log("I give up");
        }
    }

    protected void hear(int from, byte[] msg, byte channel) {
        //		System.out.println("Received message from "+from+": "+String.valueOf(msg));
    }

    private void moveToClosestRefuge() {
        //		log("Moving to closest refuge");
        Collection<RescueObject> allRefuges = memory.getObjectsOfType(RescueConstants.TYPE_REFUGE);
        Refuge[] refuges = new Refuge[allRefuges.size()];
        allRefuges.toArray(refuges);
        SampleSearch.sortByDistance(refuges,me(),memory);
        for (int i=0;i<refuges.length;++i) {
            Refuge next = refuges[i];
            //			log("Trying to plan path to "+next);
            int[] path = SampleSearch.breadthFirstSearch(getLocation(),next,memory); // Find a path from my current location to the target
            if (path!=null) {
                // We've found a path. Hooray!
                // Send a move command and we're finished
                //				log("Moving to "+next);
                move(path);
                return;
            }
            //			log("No path");
        }
        //		log("Couldn't move to refuge");
    }
}
