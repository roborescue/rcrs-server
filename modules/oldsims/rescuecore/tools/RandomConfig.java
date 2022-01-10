/*
 * Last change: $Date: 2004/08/10 20:46:17 $
 * $Revision: 1.7 $
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

package rescuecore.tools;

import java.io.*;
import java.util.*;
import rescuecore.objects.*;
import rescuecore.*;

/**
   This class will take a city data file and produce a random gisini.txt file for use on that city.
   @author Cameron Skinner
 */
public class RandomConfig {
	private Node[] allNodes;
	private Road[] allRoads;
	private Building[] allBuildings;

    public static void main(String[] args) {
		new RandomConfig(args);
    }

    RandomConfig(String[] args) {
		Limits fireBrigades = new Limits(10,15,"fireBrigades","fire brigades");
		Limits policeForces = new Limits(10,15,"policeForces","police forces");
		Limits ambulanceTeams = new Limits(5,8,"ambulanceTeams","ambulance teams");
		Limits fireStations = new Limits(1,1,"fireStations","fire stations");
		Limits policeStations = new Limits(1,1,"policeStations","police stations");
		Limits ambulanceCenters = new Limits(1,1,"ambulanceCenters","ambulance centers");
		Limits civilians = new Limits(70,90,"civilians","civilians");
		Limits refuges = new Limits(1,5,"refuges","refuges");
		Limits fires = new Limits(2,8,"fires","fires");
		Limits fireRadius = new Limits(0,20000,"fireradius","fire radius");
		Limits[] allLimits = new Limits[] {fireBrigades,policeForces,ambulanceTeams,fireStations,policeStations,ambulanceCenters,civilians,refuges,fires,fireRadius};
		boolean allowTrappedAgents = false;
		boolean bigFires = false;
		boolean trappedCivilians = true;
		
		String dir = "";
		
		for (int i=0;i<args.length;++i) {
			if (args[i].startsWith("-min-")) {
				for (int j=0;j<allLimits.length;++j) {
					if (args[i].equalsIgnoreCase("-min-"+allLimits[j].prefix)) allLimits[j].min = Integer.parseInt(args[++i]);
				}
			}
			else if (args[i].startsWith("-max-")) {
				for (int j=0;j<allLimits.length;++j) {
					if (args[i].equalsIgnoreCase("-max-"+allLimits[j].prefix)) allLimits[j].max = Integer.parseInt(args[++i]);
				}
			}
			else if (args[i].startsWith("-no-")) {
				for (int j=0;j<allLimits.length;++j) {
					if (args[i].equalsIgnoreCase("-no-"+allLimits[j].prefix)) {
						allLimits[j].min = 0;
						allLimits[j].max = 0;
					}
				}
			}
			else if (args[i].startsWith("-set-")) {
				int num = Integer.parseInt(args[i+1]);
				for (int j=0;j<allLimits.length;++j) {
					if (args[i].equalsIgnoreCase("-set-"+allLimits[j].prefix)) {
						allLimits[j].min = num;
						allLimits[j].max = num;
					}
				}
				++i;
			}
			else if (args[i].equalsIgnoreCase("-t") || args[i].equalsIgnoreCase("--allow-trapped-agents")) {
				allowTrappedAgents = true;
			}
			else if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("--allow-untrapped-civilians")) {
				trappedCivilians = false;
			}
			else if (args[i].equalsIgnoreCase("-b") || args[i].equalsIgnoreCase("--big-fires")) {
				bigFires = true;
			}
			
			else if (args[i].equalsIgnoreCase("-d") || args[i].equalsIgnoreCase("--dir")) {
				dir = args[i+1];
			}
			
			else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help")) {
				System.out.println("Usage: RandomConfig [options]");
				System.out.println("This program will read from \"road.bin\", \"node.bin\" and \"building.bin\" and produce a randomised \"gisini.txt\"");
				System.out.println("Options");
				System.out.println("=======");
				for (int j=0;j<allLimits.length;++j) {
					System.out.println("-min-"+allLimits[j].prefix+"\tSet the minimum number of "+allLimits[j].name+" (currently "+allLimits[j].min+")");
					System.out.println("-max-"+allLimits[j].prefix+"\tSet the maximum number of "+allLimits[j].name+" (currently "+allLimits[j].max+")");
					System.out.println("-no-"+allLimits[j].prefix+"\tSet the minimum and maximum of "+allLimits[j].name+" to zero");
				}
				System.out.println("-t\t--allow-trapped-agents\tAllow rescue agents (fire brigades, police forces and ambulance teams) to be placed inside buildings (default OFF)");
				System.out.println("-c\t--allow-untrapped-civilians\tAllow civilians to be placed outside buildings (default OFF)");
				System.out.println("-b\t--big-fires\tAllow big fires");
				System.out.println("-d\t--dir\tSet output directory (use full path)");
				System.out.println("-h\t--help\tPrint this message");
				return;
			}
		}
		try {
			
			File parentDir = new File(dir);
			File gisini = new File(parentDir, "gisini.txt");
			
			// Open the output

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(gisini)));
			
			// Build the city data
			allNodes = MapFiles.loadNodes(parentDir);
			allRoads = MapFiles.loadRoads(parentDir);
			allBuildings = MapFiles.loadBuildings(parentDir);

			Memory memory = new HashMemory();
			for (int i=0;i<allNodes.length;++i) memory.add(allNodes[i],0);
			for (int i=0;i<allRoads.length;++i) memory.add(allRoads[i],0);
			for (int i=0;i<allBuildings.length;++i) memory.add(allBuildings[i],0);
			// Place items
			int numFireBrigades = fireBrigades.getNumber();
			int numPoliceForces = policeForces.getNumber();
			int numAmbulanceTeams = ambulanceTeams.getNumber();
			int numFireStations = fireStations.getNumber();
			int numPoliceStations = policeStations.getNumber();
			int numAmbulanceCenters = ambulanceCenters.getNumber();
			int numRefuges = refuges.getNumber();
			int numCivilians = civilians.getNumber();
			int numFires = fires.getNumber();

			FireStation[] fireStationBuildings = new FireStation[numFireStations];
			PoliceOffice[] policeOfficeBuildings = new PoliceOffice[numPoliceStations];
			AmbulanceCenter[] ambulanceCenterBuildings = new AmbulanceCenter[numAmbulanceCenters];
			Refuge[] refugeBuildings = new Refuge[numRefuges];
			Building[] normalBuildings = placeMotionlessObjects(fireStationBuildings,policeOfficeBuildings,ambulanceCenterBuildings,refugeBuildings,allBuildings);
			MapFiles.writeGISMotionlessObjects(out,fireStationBuildings,policeOfficeBuildings,ambulanceCenterBuildings,refugeBuildings);


			FireBrigade[] fireBrigadeObjects = new FireBrigade[numFireBrigades];
			PoliceForce[] policeForceObjects = new PoliceForce[numPoliceForces];
			AmbulanceTeam[] ambulanceTeamObjects = new AmbulanceTeam[numAmbulanceTeams];
			Civilian[] civilianObjects = new Civilian[numCivilians];
			//			placeMovingObjects(fireBrigadeObjects,policeForceObjects,ambulanceTeamObjects,civilianObjects,allBuildings,allRoads,allNodes,allowTrappedAgents,trappedCivilians);
			placeMovingObjects(fireBrigadeObjects,policeForceObjects,ambulanceTeamObjects,civilianObjects,allBuildings,new Road[0],allNodes,allowTrappedAgents,trappedCivilians);
			MapFiles.writeGISMovingObjects(out,fireBrigadeObjects,policeForceObjects,ambulanceTeamObjects,civilianObjects,memory);

			Building[] fireBuildings;
			if (bigFires) fireBuildings = placeBigFires(numFires,normalBuildings,fireRadius);
			else fireBuildings = placeNormalFires(numFires,normalBuildings);
			MapFiles.writeGISFires(out,fireBuildings);

			out.flush();
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }

	/*
	  Place all motionless objects (fire stations, police offices, ambulance teams and refuges)
	  @param fire An array to be filled with fire stations
	  @param police An array to be filled with police offices
	  @param ambulance An array to be filled with ambulance centres
	  @param refuge An array to be filled with refuges
	  @param allBuildings All buildings in the map
	  @return All ordinary buildings
	*/
    public static Building[] placeMotionlessObjects(FireStation[] fire, PoliceOffice[] police, AmbulanceCenter[] ambulance, Refuge[] refuge, Building[] allBuildings) {
		List remaining = new ArrayList();
		for (int i=0;i<allBuildings.length;++i) remaining.add(allBuildings[i]);
		Collections.shuffle(remaining);
		System.out.println("Placing "+ambulance.length+" ambulance centers");
		Iterator it = remaining.iterator();
		for (int i=0;i<ambulance.length;++i) {
			Building location = (Building)it.next();
			it.remove();
			AmbulanceCenter a = new AmbulanceCenter(location);
			a.setID(location.getID());
			ambulance[i] = a;
			//			writeFixedObjectData(out,TYPE_AMBULANCE_CENTER,i,location);
		}
		System.out.println("Placing "+fire.length+" fire stations");
		for (int i=0;i<fire.length;++i) {
			Building location = (Building)it.next();
			it.remove();
			FireStation a = new FireStation(location);
			a.setID(location.getID());
			fire[i] = a;
			//			writeFixedObjectData(out,TYPE_FIRE_STATION,i,location);
			//			System.out.print(".");
		}
		System.out.println("Placing "+police.length+" police stations");
		for (int i=0;i<police.length;++i) {
			Building location = (Building)it.next();
			it.remove();
			PoliceOffice a = new PoliceOffice(location);
			a.setID(location.getID());
			police[i] = a;
			//			writeFixedObjectData(out,TYPE_POLICE_OFFICE,i,location);
			//			System.out.print(".");
		}
		System.out.println("Placing "+refuge.length+" refuges");
		for (int i=0;i<refuge.length;++i) {
			Building location = (Building)it.next();
			it.remove();
			Refuge a = new Refuge(location);
			a.setID(location.getID());
			refuge[i] = a;
			//			writeFixedObjectData(out,TYPE_REFUGE,i,location);
			//			System.out.print(".");
		}
		//		System.out.println();
		return (Building[])remaining.toArray(new Building[0]);
    }

    public static void placeMovingObjects(FireBrigade[] fire, PoliceForce[] police, AmbulanceTeam[] ambulance, Civilian[] civ, Building[] b, Road[] r, Node[] n, boolean allowTrappedAgents, boolean trappedCivilians) {
		RescueObject[] allLocations = new RescueObject[b.length+r.length+n.length];
		System.arraycopy(b,0,allLocations,0,b.length);
		System.arraycopy(r,0,allLocations,b.length,r.length);
		System.arraycopy(n,0,allLocations,b.length+r.length,n.length);
		RescueObject[] outsideLocations = new RescueObject[r.length+n.length];
		System.arraycopy(r,0,outsideLocations,0,r.length);
		System.arraycopy(n,0,outsideLocations,r.length,n.length);
		System.out.println("Placing "+civ.length+" civilians");
		for (int i=0;i<civ.length;++i) {
			civ[i] = new Civilian();
			civ[i].setPosition(randomLocation(trappedCivilians?b:allLocations).getID(),0,null);
			//			writeMovingObjectData(out,TYPE_CIVILIAN,i,randomLocation(trappedCivilians?b:allLocations));
			//			System.out.print(".");
		}
		//		System.outln.println();
		System.out.println("Placing "+ambulance.length+" ambulance teams");
		for (int i=0;i<ambulance.length;++i) {
			ambulance[i] = new AmbulanceTeam();
			ambulance[i].setPosition(randomLocation(allowTrappedAgents?allLocations:outsideLocations).getID(),0,null);
			//			writeMovingObjectData(out,TYPE_AMBULANCE_TEAM,i,randomLocation(allowTrappedAgents?allLocations:outsideLocations));
			//			System.out.print(".");
		}
		//		System.out.println();
		System.out.println("Placing "+fire.length+" fire brigades");
		for (int i=0;i<fire.length;++i) {
			fire[i] = new FireBrigade();
			fire[i].setPosition(randomLocation(allowTrappedAgents?allLocations:outsideLocations).getID(),0,null);
			//			writeMovingObjectData(out,TYPE_FIRE_BRIGADE,i,randomLocation(allowTrappedAgents?allLocations:outsideLocations));
			//			System.out.print(".");
		}
		//		System.out.println();
		System.out.println("Placing "+police.length+" police forces");
		for (int i=0;i<police.length;++i) {
			police[i] = new PoliceForce();
			police[i].setPosition(randomLocation(allowTrappedAgents?allLocations:outsideLocations).getID(),0,null);
			//			writeMovingObjectData(out,TYPE_POLICE_FORCE,i,randomLocation(allowTrappedAgents?allLocations:outsideLocations));
			//			System.out.print(".");
		}
		//		System.out.println();
    }

    public static Building[] placeNormalFires(int num, Building[] b) {
		List remaining = new ArrayList();
		for (int i=0;i<b.length;++i) remaining.add(b[i]);
		Collections.shuffle(remaining);
		Building[] result = new Building[num];
		System.out.println("Placing "+num+" fires");
		Iterator it = remaining.iterator();
		for (int i=0;i<num;++i) result[i] = (Building)it.next();
		return result;
    }

    public static Building[] placeBigFires(int num, Building[] b, Limits radius) {
		List remaining = new ArrayList();
		for (int i=0;i<b.length;++i) remaining.add(b[i]);
		Collections.shuffle(remaining);
		Collection fires = new HashSet();
		System.out.print("Placing "+num+" big fires");
		Iterator it = remaining.iterator();
		for (int i=0;i<num;++i) {
			Building center = (Building)it.next();
			fires.add(center);
			long r = radius.getNumber();
			long distanceSquared = r*r;
			// Check for close buildings
			for (int j=0;j<b.length;++j) {
				long dx = center.getX()-b[j].getX();
				long dy = center.getY()-b[j].getY();
				long distance = (dx*dx) + (dy*dy);
				if (distance <= distanceSquared) fires.add(b[j]);
			}
		}
		return (Building[])fires.toArray(new Building[0]);
    }

    public static RescueObject randomLocation(RescueObject[] possible) {
		return possible[(int)(Math.random()*possible.length)];
    }

	//	private static Node findNode(int id) {
	//		for (int i=0;i<allNodes.length;++i) if (allNodes[i].getID()==id) return allNodes[i];
	//		return null;
	//	}

    private class Limits {
		int min;
		int max;
		String prefix;
		String name;

		Limits(int min, int max, String prefix, String name) {
			this.min = min;
			this.max = max;
			this.prefix = prefix;
			this.name = name;
		}

		int getNumber() {
			if (min==max) return min;
			int range = max-min;
			return (int)(Math.random()*range)+min;
		}
    }
}
