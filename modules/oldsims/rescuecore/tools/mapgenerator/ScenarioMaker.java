/*
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

package rescuecore.tools.mapgenerator;

import rescuecore.*;
import rescuecore.view.*;
import rescuecore.objects.*;
import rescuecore.tools.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.io.*;
import java.util.Collection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;

public class ScenarioMaker {
	private enum Tool {
		PLACE_ROADS("Place blocked roads"),
		CLEAR_ROADS("Remove blocked roads"),
		PLACE_FIRES("Place fires"),
		CLEAR_FIRES("Remove fires"),
		PLACE_REFUGES("Place refuges"),
		CLEAR_REFUGES("Remove refuges"),
		PLACE_FIRE_BRIGADES("Place fire brigades"),
		CLEAR_FIRE_BRIGADES("Remove fire brigades"),
		PLACE_FIRE_STATIONS("Place fire stations"),
		CLEAR_FIRE_STATIONS("Remove fire stations"),
		PLACE_POLICE_FORCES("Place Police forces"),
		CLEAR_POLICE_FORCES("Remove police forces"),
		PLACE_POLICE_OFFICES("Place police offices"),
		CLEAR_POLICE_OFFICES("Remove police offices"),
		PLACE_AMBULANCE_TEAMS("Place ambulance teams"),
		CLEAR_AMBULANCE_TEAMS("Remove ambulance teams"),
		PLACE_AMBULANCE_CENTRES("Place ambulance centres"),
		CLEAR_AMBULANCE_CENTRES("Remove ambulance centres"),
		PLACE_CIVILIANS("Place civilians"),
		CLEAR_CIVILIANS("Remove civilians"),
		INCREASE_IMPORTANCE("Increase building importance"),
		DECREASE_IMPORTANCE("Decrease building importance"),
		INSPECTOR("Object inspector");

		private String name;

		Tool(String s) {
			name = s;
		}

		public String getName() {
			return name;
		}
	};

	private Road[] allRoads;
	private Node[] allNodes;
	private Building[] allBuildings;
	private Memory memory;
	private Map map;
	private Point pressPoint;
	private Point dragPoint;
	private Layer overlay;
	private Layer roadLayer;
	private Layer buildingLayer;
	private Layer humanoidLayer;
	private Tool tool;
	private JLabel currentTool;
	private ObjectInspector inspector;
	private ObjectSelector selector;
	private int nextID = 1000000;
	private Summary summary;

	public ScenarioMaker(Road[] roads, Node[] nodes, Building[] buildings, final boolean oldGIS) {
		allRoads = roads;
		allNodes = nodes;
		allBuildings = buildings;
		memory = new HashMemory();
		for (int i=0;i<roads.length;++i) {
			if (memory.lookup(roads[i].getID())!=null) System.err.println("WARNING: Duplicate road ID: "+roads[i].getID()+", this is is already used by "+memory.lookup(roads[i].getID()));
			memory.add(roads[i],0,this);
		}
		for (int i=0;i<nodes.length;++i) {
			if (memory.lookup(nodes[i].getID())!=null) System.err.println("WARNING: Duplicate node ID: "+nodes[i].getID()+", this is is already used by "+memory.lookup(nodes[i].getID()));
			memory.add(nodes[i],0,this);
		}
		for (int i=0;i<buildings.length;++i) {
			if (memory.lookup(buildings[i].getID())!=null) System.err.println("WARNING: Duplicate building ID: "+buildings[i].getID()+", this is is already used by "+memory.lookup(buildings[i].getID()));
			memory.add(buildings[i],0,this);
		}
		Arrays.sort(allRoads,new RoadSorter(memory));
		map = new Map(memory);
		roadLayer = Layer.createRoadLayer(memory);
		roadLayer.addRenderer(Road.class,new BigRoadRenderer());
		buildingLayer = Layer.createBuildingLayer(memory);
		buildingLayer.addRenderer(Building.class,new ImportantBuildingRenderer(BuildingRenderer.ordinaryBuildingRenderer()));
		humanoidLayer = Layer.createHumanoidLayer(memory);
		humanoidLayer.addRenderer(Humanoid.class,new HumanoidCountRenderer(HumanoidRenderer.ordinaryHumanoidRenderer()));
		map.addLayer(roadLayer);
		map.addLayer(buildingLayer);
		map.addLayer(humanoidLayer);
		map.addLayer(Layer.createNodeLayer(memory));
		overlay = Layer.createOverlayLayer("Overlay");
		map.addLayer(overlay);
		map.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					handleClick(e);
				}
				public void mousePressed(MouseEvent e) {
					handlePress(e);
				}
				public void mouseReleased(MouseEvent e) {
					handleRelease(e);
				}
			});
		map.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent e) {
					handleDrag(e);
				}
			});
		final Action saveAction = new AbstractAction("Save") {
				public void actionPerformed(ActionEvent e) {
					save(oldGIS);
				}
			};
		final Action randomiseAction = new AbstractAction("Randomise") {
				public void actionPerformed(ActionEvent e) {
					randomise();
				}
			};
		JMenuBar menubar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu tools = new JMenu("Tools");
		JMenu agentTools = new JMenu("Agents");
		JMenu fb = new JMenu("Fire brigades");
		JMenu fs = new JMenu("Fire stations");
		JMenu pf = new JMenu("Police forces");
		JMenu po = new JMenu("Police offices");
		JMenu at = new JMenu("Ambulance teams");
		JMenu ac = new JMenu("Ambulance centres");
		JMenu civ = new JMenu("Civilians");
		file.add(saveAction);
		tools.add(new ToolAction(Tool.PLACE_ROADS));
		tools.add(new ToolAction(Tool.CLEAR_ROADS));
		tools.addSeparator();
		tools.add(new ToolAction(Tool.PLACE_FIRES));
		tools.add(new ToolAction(Tool.CLEAR_FIRES));
		tools.add(new ToolAction(Tool.PLACE_REFUGES));
		tools.add(new ToolAction(Tool.CLEAR_REFUGES));
		tools.addSeparator();
		agentTools.add(fb);
		agentTools.add(fs);
		agentTools.add(pf);
		agentTools.add(po);
		agentTools.add(at);
		agentTools.add(ac);
		agentTools.add(civ);
		fb.add(new ToolAction(Tool.PLACE_FIRE_BRIGADES,"Place"));
		fb.add(new ToolAction(Tool.CLEAR_FIRE_BRIGADES,"Remove"));
		fs.add(new ToolAction(Tool.PLACE_FIRE_STATIONS,"Place"));
		fs.add(new ToolAction(Tool.CLEAR_FIRE_STATIONS,"Remove"));
		pf.add(new ToolAction(Tool.PLACE_POLICE_FORCES,"Place"));
		pf.add(new ToolAction(Tool.CLEAR_POLICE_FORCES,"Remove"));
		po.add(new ToolAction(Tool.PLACE_POLICE_OFFICES,"Place"));
		po.add(new ToolAction(Tool.CLEAR_POLICE_OFFICES,"Remove"));
		at.add(new ToolAction(Tool.PLACE_AMBULANCE_TEAMS,"Place"));
		at.add(new ToolAction(Tool.CLEAR_AMBULANCE_TEAMS,"Remove"));
		ac.add(new ToolAction(Tool.PLACE_AMBULANCE_CENTRES,"Place"));
		ac.add(new ToolAction(Tool.CLEAR_AMBULANCE_CENTRES,"Remove"));
		civ.add(new ToolAction(Tool.PLACE_CIVILIANS,"Place"));
		civ.add(new ToolAction(Tool.CLEAR_CIVILIANS,"Remove"));
		tools.add(agentTools);
		tools.addSeparator();
		tools.add(new ToolAction(Tool.INCREASE_IMPORTANCE));
		tools.add(new ToolAction(Tool.DECREASE_IMPORTANCE));
		tools.addSeparator();
		tools.add(randomiseAction);
		tools.addSeparator();
		tools.add(new ToolAction(Tool.INSPECTOR));
		menubar.add(file);
		menubar.add(tools);
		summary = new Summary();
		JFrame frame = new JFrame("Scenario Maker");
		JPanel top = new JPanel(new BorderLayout());
		top.add(menubar,BorderLayout.CENTER);
		top.add(summary,BorderLayout.EAST);
		JPanel main = new JPanel(new BorderLayout());
		main.add(map,BorderLayout.CENTER);
		JPanel side = new JPanel(new BorderLayout());
		currentTool = new JLabel("Current tool: Object inspector");
		inspector = new ObjectInspector(true);
		selector = new ObjectSelector(map);
		side.add(currentTool,BorderLayout.NORTH);
		side.add(new JScrollPane(inspector),BorderLayout.CENTER);
		main.add(side,BorderLayout.EAST);
		main.add(top,BorderLayout.NORTH);
		frame.addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e) {
			System.exit(0);
		}});
		frame.getContentPane().add(main);
		frame.pack();
		changeTool(Tool.INSPECTOR);
		frame.setVisible(true);
	}

	private int generateID() {
		int result;
		do {
			result = nextID++;
		} while (memory.lookup(result)!=null);
		return result;
	}

	private void changeTool(Tool t) {
		if (tool==t) return;
		tool = t;
		currentTool.setText("Current tool: "+t.getName());
		if (tool==Tool.INSPECTOR) selector.addObjectSelectionListener(inspector);
		else selector.removeObjectSelectionListener(inspector);
	}

	private void randomise() {
		RandomiseDialog r = new RandomiseDialog(null);
		r.pack();
		r.setVisible(true);
		if (r.wasOK()) {
			FireStation[] fireStations = new FireStation[r.getFireStations()];
			PoliceOffice[] policeOffices = new PoliceOffice[r.getPoliceOffices()];
			AmbulanceCenter[] ambulanceCentres = new AmbulanceCenter[r.getAmbulanceCentres()];
			Refuge[] refuges = new Refuge[r.getRefuges()];
			FireBrigade[] fireBrigades = new FireBrigade[r.getFireBrigades()];
			PoliceForce[] policeForces = new PoliceForce[r.getPoliceForces()];
			AmbulanceTeam[] ambulanceTeams = new AmbulanceTeam[r.getAmbulanceTeams()];
			Civilian[] civs = new Civilian[r.getCivs()];

			Building[] ordinary = RandomConfig.placeMotionlessObjects(fireStations,policeOffices,ambulanceCentres,refuges,allBuildings);
			//			RandomConfig.placeMovingObjects(fireBrigades,policeForces,ambulanceTeams,civs,allBuildings,allRoads,allNodes,false,true);
			RandomConfig.placeMovingObjects(fireBrigades,policeForces,ambulanceTeams,civs,allBuildings,new Road[0],allNodes,false,true);
			Building[] fires = RandomConfig.placeNormalFires(r.getFires(),ordinary);

			// Remove all humanoids, convert all buildings back to normal and remove all fieryness
			Collection<RescueObject> all = memory.getAllObjects();
			for (RescueObject next : all) {
				if (next instanceof Building) {
					Building b = (Building)next;
					b.setFieryness(0,0,this);
					convertBuilding(b,RescueConstants.TYPE_BUILDING);
				}
				if (next instanceof Humanoid) {
					memory.remove(next);
				}
			}

			// Add all these objects to the memory
			for (int i=0;i<fireStations.length;++i) memory.add(fireStations[i],0);
			for (int i=0;i<policeOffices.length;++i) memory.add(policeOffices[i],0);
			for (int i=0;i<ambulanceCentres.length;++i) memory.add(ambulanceCentres[i],0);
			for (int i=0;i<refuges.length;++i) memory.add(refuges[i],0);
			// We need to generate IDs for new humanoids
			for (int i=0;i<fireBrigades.length;++i) {
				fireBrigades[i].setID(generateID());
				memory.add(fireBrigades[i],0);
			}
			for (int i=0;i<policeForces.length;++i) {
				policeForces[i].setID(generateID());
				memory.add(policeForces[i],0);
			}
			for (int i=0;i<ambulanceTeams.length;++i) {
				ambulanceTeams[i].setID(generateID());
				memory.add(ambulanceTeams[i],0);
			}
			for (int i=0;i<civs.length;++i) {
				civs[i].setID(generateID());
				memory.add(civs[i],0);
			}
			// And add the fires
			for (int i=0;i<fires.length;++i) fires[i].setFieryness(1,0,null);

			map.setMemory(memory);
			map.repaint();
			summary.update();
		}
	}

	private void handleClick(MouseEvent e) {
		if (dragPoint==null) {
		}
	}

	private void handlePress(MouseEvent e) {
		pressPoint = e.getPoint();
	}

	private void handleRelease(MouseEvent e) {
		if (dragPoint==null) {
			Object[] all = map.getObjectsAtPoint(pressPoint);
			update(all,e);
		}
		else {
			int x1 = Math.min(pressPoint.x,dragPoint.x);
			int y1 = Math.min(pressPoint.y,dragPoint.y);
			int x2 = Math.max(pressPoint.x,dragPoint.x);
			int y2 = Math.max(pressPoint.y,dragPoint.y);
			Rectangle2D box = new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
			Object[] objects = map.getObjectsInArea(box);
			update(objects,e);
			pressPoint = null;
			dragPoint = null;
			overlay.removeAllObjects();
			map.repaint();
		}
	}

	private void handleDrag(MouseEvent e) {
		if (pressPoint!=null) {
			dragPoint = e.getPoint();
			int dx = pressPoint.x - dragPoint.x;
			int dy = pressPoint.y - dragPoint.y;
			if (dx < 0) dx = -dx;
			if (dy < 0) dy = -dy;
			if (dx > 5 || dy > 5) {
				// Draw a rectangle on the view
				Rectangle r = new Rectangle(Math.min(pressPoint.x,dragPoint.x),Math.min(pressPoint.y,dragPoint.y),dx,dy);
				overlay.setObject(r);
				map.repaint();
			}
		}
	}

	private void update(Object[] os, MouseEvent e) {
		boolean change = false;
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			change = leftClick(os);
			break;
		}
		if (change) {
			map.setMemory(memory);
			map.repaint();
			summary.update();
		}
	}

	private boolean leftClick(Object[] os) {
		switch (tool) {
		case PLACE_ROADS:
			return increaseBlock(os);
		case CLEAR_ROADS:
			return decreaseBlock(os);
		case PLACE_FIRES:
			return placeFires(os);
		case CLEAR_FIRES:
			return clearFires(os);
		case PLACE_REFUGES:
			return placeRefuges(os);
		case CLEAR_REFUGES:
			return clearRefuges(os);
		case PLACE_FIRE_BRIGADES:
			return addAgent(os,RescueConstants.TYPE_FIRE_BRIGADE);
		case CLEAR_FIRE_BRIGADES:
			return removeAgent(os,RescueConstants.TYPE_FIRE_BRIGADE);
		case PLACE_POLICE_FORCES:
			return addAgent(os,RescueConstants.TYPE_POLICE_FORCE);
		case CLEAR_POLICE_FORCES:
			return removeAgent(os,RescueConstants.TYPE_POLICE_FORCE);
		case PLACE_AMBULANCE_TEAMS:
			return addAgent(os,RescueConstants.TYPE_AMBULANCE_TEAM);
		case CLEAR_AMBULANCE_TEAMS:
			return removeAgent(os,RescueConstants.TYPE_AMBULANCE_TEAM);
		case PLACE_CIVILIANS:
			return addAgent(os,RescueConstants.TYPE_CIVILIAN);
		case CLEAR_CIVILIANS:
			return removeAgent(os,RescueConstants.TYPE_CIVILIAN);
		case PLACE_FIRE_STATIONS:
			return addCentre(os,RescueConstants.TYPE_FIRE_STATION);
		case CLEAR_FIRE_STATIONS:
			return removeCentre(os,RescueConstants.TYPE_FIRE_STATION);
		case PLACE_POLICE_OFFICES:
			return addCentre(os,RescueConstants.TYPE_POLICE_OFFICE);
		case CLEAR_POLICE_OFFICES:
			return removeCentre(os,RescueConstants.TYPE_POLICE_OFFICE);
		case PLACE_AMBULANCE_CENTRES:
			return addCentre(os,RescueConstants.TYPE_AMBULANCE_CENTER);
		case CLEAR_AMBULANCE_CENTRES:
			return removeCentre(os,RescueConstants.TYPE_AMBULANCE_CENTER);
		case INCREASE_IMPORTANCE:
			return increaseImportance(os);
		case DECREASE_IMPORTANCE:
			return decreaseImportance(os);
		}
		return false;
	}

	/*
	  private boolean rightClick(Object[] os) {
	  switch (tool) {
	  case ROADS:
	  return decreaseBlock(os);
	  case IMPORTANCE:
	  return decreaseImportance(os);
	  }
	  return false;
	  }
	*/

	private boolean increaseBlock(Object[] roads) {
		boolean changed = false;
		for (int i=0;i<roads.length;++i) {
			if (roads[i] instanceof Road) {
				changed = increaseBlock((Road)roads[i]) | changed;
			}
		}
		return changed;
	}

	private boolean decreaseBlock(Object[] roads) {
		boolean changed = false;
		for (int i=0;i<roads.length;++i) {
			if (roads[i] instanceof Road) {
				changed = decreaseBlock((Road)roads[i]) | changed;
			}
		}
		return changed;
	}

	private boolean increaseBlock(Road road) {
		int lanes = road.getLinesToHead();
		int blocked = lanes - freeLanes(road);
		if (blocked==lanes) return false;
		setBlockedLanes(road,blocked+1);
		return true;
	}

	private boolean decreaseBlock(Road road) {
		int lanes = road.getLinesToHead();
		int blocked = lanes - freeLanes(road);
		if (blocked==0) return false;
		setBlockedLanes(road,blocked-1);
		return true;
	}

	private void setBlockedLanes(Road road, int blocked) {
		int width = road.getWidth();
		int laneWidth = width/(road.getLinesToHead()+road.getLinesToTail());
		int blockNeeded = blocked * laneWidth * 2;
		if (blockNeeded > width) {
			System.out.println("Trying to set block to "+blockNeeded+" but width is only "+width);
			blockNeeded = width;
		}
		road.setBlock(blockNeeded,0,this);
		// Check that we have blocked the right number of lanes
		int newBlock = road.getLinesToHead()-freeLanes(road);
		if (newBlock != blocked) System.out.println("We have "+newBlock+" blocked lanes instead of "+blocked+"!");
	}

	private boolean placeFires(Object[] os) {
		boolean changed = false;
		for (int i=0;i<os.length;++i) {
			if (os[i] instanceof Building) changed = placeFire((Building)os[i]) | changed;
		}
		return changed;
	}

	private boolean clearFires(Object[] os) {
		boolean changed = false;
		for (int i=0;i<os.length;++i) {
			if (os[i] instanceof Building) changed = clearFire((Building)os[i]) | changed;
		}
		return changed;
	}

	private boolean placeFire(Building b) {
		if (b.getFieryness()==0) {
			b.setFieryness(1,0,this);
			return true;
		}
		return false;
	}

	private boolean clearFire(Building b) {
		if (b.getFieryness()!=0) {
			b.setFieryness(0,0,this);
			return true;
		}
		return false;
	}

	private boolean placeRefuges(Object[] os) {
		boolean changed = false;
		for (int i=0;i<os.length;++i) {
			if (os[i] instanceof Building) changed = setRefuge((Building)os[i]) | changed;
		}
		return changed;
	}

	private boolean clearRefuges(Object[] os) {
		boolean changed = false;
		for (int i=0;i<os.length;++i) {
			if (os[i] instanceof Building) changed = clearRefuge((Building)os[i]) | changed;
		}
		return changed;
	}

	private boolean increaseImportance(Object[] os) {
		boolean changed = false;
		for (int i=0;i<os.length;++i) {
			if (os[i] instanceof Building) {
				Building b = (Building)os[i];
				b.setImportance(b.getImportance()+1,0,null);
				changed = true;
			}
		}
		return changed;
	}

	private boolean decreaseImportance(Object[] os) {
		boolean changed = false;
		for (int i=0;i<os.length;++i) {
			if (os[i] instanceof Building) {
				Building b = (Building)os[i];
				if (b.getImportance()>1) {
					b.setImportance(b.getImportance()-1,0,null);
					changed = true;
				}
			}
		}
		return changed;
	}

	private boolean setRefuge(Building b) {
		if (b.isRefuge()) return false;
		convertBuilding(b,RescueConstants.TYPE_REFUGE);
		return true;
	}

	private boolean clearRefuge(Building b) {
		if (!b.isRefuge()) return false;
		convertBuilding(b,RescueConstants.TYPE_BUILDING);
		return true;
	}

	private boolean addAgent(Object[] os, int type) {
		// Find the first object that we can place an agent on
		int position = 0;
		Road road = null;
		for (Object next : os) {
			if (next instanceof Building || next instanceof Road || next instanceof Node) {
				position = ((RescueObject)next).getID();
				if (next instanceof Road) road = (Road)next;
			}
		}
		if (position==0) return false;
		// Generate the agent
		Humanoid result;
		switch (type) {
		case RescueConstants.TYPE_FIRE_BRIGADE:
			result = new FireBrigade();
			break;
		case RescueConstants.TYPE_POLICE_FORCE:
			result = new PoliceForce();
			break;
		case RescueConstants.TYPE_AMBULANCE_TEAM:
			result = new AmbulanceTeam();
			break;
		case RescueConstants.TYPE_CIVILIAN:
			result = new Civilian();
			break;
		default:
			return false;
		}
		result.setID(generateID());
		result.setPosition(position,0,this);
		if (road!=null) result.setPositionExtra(road.getLength()/2,0,this);
		memory.add(result,0,this);
		return true;
	}

	private boolean removeAgent(Object[] os, int type) {
		// Remove the first object that is the right type
		for (Object next : os) {
			if (next instanceof RescueObject) {
				RescueObject r = (RescueObject)next;
				if (r.getType()==type) {
					memory.remove(r);
					return true;
				}
			}
		}
		return false;
	}

	private boolean addCentre(Object[] os, int type) {
		// Turn the first ordinary building into the right type
		for (Object next : os) {
			if (next instanceof Building) {
				Building b = (Building)next;
				if (b.isOrdinaryBuilding()) {
					convertBuilding(b,type);
					return true;
				}
			}
		}
		return false;
	}

	private boolean removeCentre(Object[] os, int type) {
		for (Object next : os) {
			if (next instanceof Building) {
				Building b = (Building)next;
				if (b.getType()==type) {
					convertBuilding(b,RescueConstants.TYPE_BUILDING);
					return true;
				}
			}
		}
		return false;
	}

	private void convertBuilding(Building b, int type) {
		if (b.getType()==type) return;
		Building replace;
		switch (type) {
		case RescueConstants.TYPE_BUILDING:
			replace = new Building(b);
			break;
		case RescueConstants.TYPE_REFUGE:
			replace = new Refuge(b);
			break;
		case RescueConstants.TYPE_FIRE_STATION:
			replace = new FireStation(b);
			break;
		case RescueConstants.TYPE_POLICE_OFFICE:
			replace = new PoliceOffice(b);
			break;
		case RescueConstants.TYPE_AMBULANCE_CENTER:
			replace = new AmbulanceCenter(b);
			break;
		default:
			System.err.println("Can't convert to type "+type);
			return;
		}
		replace.setID(b.getID());
		memory.remove(b);
		memory.add(replace,0,this);
	}

	private void save(boolean oldGIS) {
		try {
			// Write out the blocked roads
			PrintWriter out = new PrintWriter(new FileWriter(new File("blockades.lst")));
			for (int i=0;i<allRoads.length;++i) {
				out.println(allRoads[i].getBlock());
			}
			out.flush();
			out.close();
			out = new PrintWriter(new FileWriter(new File("gisini.txt")));
			// Write out all civilians, agents, refuges and fires
			if (oldGIS) {
				writeOldGIS(out,getFireStations(),getPoliceOffices(),getAmbulanceCenters(),getFireBrigades(),getPoliceForces(),getAmbulanceTeams(),getCivilians(),getRefuges(),getFires());
			}
			else {
				MapFiles.writeGISMotionlessObjects(out,getFireStations(),getPoliceOffices(),getAmbulanceCenters(),getRefuges());
				MapFiles.writeGISMovingObjects(out,getFireBrigades(),getPoliceForces(),getAmbulanceTeams(),getCivilians(),memory);
				MapFiles.writeGISFires(out,getFires());
				MapFiles.writeGISImportantBuildings(out,getImportantBuildings());
			}
			out.flush();
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeOldGIS(PrintWriter out, FireStation[] fs, PoliceOffice[] po, AmbulanceCenter[] ac, FireBrigade[] f, PoliceForce[] p, AmbulanceTeam[] a, Civilian[] c, Refuge[] r, Building[] fires) {
		out.println("[MotionLessObject]");
		out.println("FireStationNum="+fs.length);
		out.println("PoliceOfficeNum="+po.length);
		out.println("AmbulanceCenterNum="+ac.length);
		out.println("RefugeNum="+r.length);
		for (int i=0;i<fs.length;++i) out.println("FireStation"+i+"=0,0,"+fs[i].getID()+",0");
		for (int i=0;i<po.length;++i) out.println("PoliceOffice"+i+"=0,0,"+po[i].getID()+",0");
		for (int i=0;i<ac.length;++i) out.println("AmbulanceCenter"+i+"=0,0,"+ac[i].getID()+",0");
		for (int i=0;i<r.length;++i) out.println("Refuge"+i+"=0,0,"+r[i].getID()+",0");
		out.println("[MovingObject]");
		out.println("FireBrigadeNum="+f.length);
		out.println("PoliceForceNum="+p.length);
		out.println("AmbulanceTeamNum="+a.length);
		out.println("CivilianNum="+c.length);
		for (int i=0;i<f.length;++i) out.println("FireBrigade"+i+"=0,0,"+f[i].getPosition()+",0,0,"+f[i].getPositionExtra()+",0");
		for (int i=0;i<p.length;++i) out.println("PoliceForce"+i+"=0,0,"+p[i].getPosition()+",0,0,"+p[i].getPositionExtra()+",0");
		for (int i=0;i<a.length;++i) out.println("AmbulanceTeam"+i+"=0,0,"+a[i].getPosition()+",0,0,"+a[i].getPositionExtra()+",0");
		for (int i=0;i<c.length;++i) out.println("Civilian"+i+"=0,0,"+c[i].getPosition()+",0,0,"+c[i].getPositionExtra()+",0");
		out.println("[Fires]");
		out.println("FirePointNum="+fires.length);
		for (int i=0;i<fires.length;++i) out.println("FirePoint"+i+"=0,0,"+fires[i].getID()+",0");
	}

	private FireStation[] getFireStations() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_FIRE_STATION);
		FireStation[] result = new FireStation[objects.size()];
		objects.toArray(result);
		return result;
	}

	private PoliceOffice[] getPoliceOffices() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_POLICE_OFFICE);
		PoliceOffice[] result = new PoliceOffice[objects.size()];
		objects.toArray(result);
		return result;
	}

	private AmbulanceCenter[] getAmbulanceCenters() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_AMBULANCE_CENTER);
		AmbulanceCenter[] result = new AmbulanceCenter[objects.size()];
		objects.toArray(result);
		return result;
	}

	private Refuge[] getRefuges() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_REFUGE);
		Refuge[] result = new Refuge[objects.size()];
		objects.toArray(result);
		return result;
	}

	private FireBrigade[] getFireBrigades() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_FIRE_BRIGADE);
		FireBrigade[] result = new FireBrigade[objects.size()];
		objects.toArray(result);
		return result;
	}

	private PoliceForce[] getPoliceForces() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_POLICE_FORCE);
		PoliceForce[] result = new PoliceForce[objects.size()];
		objects.toArray(result);
		return result;
	}

	private AmbulanceTeam[] getAmbulanceTeams() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_AMBULANCE_TEAM);
		AmbulanceTeam[] result = new AmbulanceTeam[objects.size()];
		objects.toArray(result);
		return result;
	}

	private Civilian[] getCivilians() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_CIVILIAN);
		Civilian[] result = new Civilian[objects.size()];
		objects.toArray(result);
		return result;
	}

	private Building[] getFires() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_BUILDING,RescueConstants.TYPE_REFUGE,RescueConstants.TYPE_FIRE_STATION,RescueConstants.TYPE_AMBULANCE_CENTER,RescueConstants.TYPE_POLICE_OFFICE);
		Set<Building> fires = new HashSet<Building>();
		for (RescueObject next : objects) {
			Building b = (Building)next;
			if (b.getFieryness()>0) {
				fires.add(b);
			}
		}
		return (Building[])fires.toArray(new Building[0]);
	}

	private Building[] getImportantBuildings() {
		Collection<RescueObject> objects = memory.getObjectsOfType(RescueConstants.TYPE_BUILDING,RescueConstants.TYPE_REFUGE,RescueConstants.TYPE_FIRE_STATION,RescueConstants.TYPE_AMBULANCE_CENTER,RescueConstants.TYPE_POLICE_OFFICE);
		Set<Building> important = new HashSet<Building>();
		for (RescueObject next : objects) {
			Building b = (Building)next;
			if (b.getImportance()>1) {
				important.add(b);
			}
		}
		return (Building[])important.toArray(new Building[0]);
	}

	private static int freeLanes(Road road) {
	    double blockWidth = road.getBlock()/2d;
		double lineWidth = ((double)road.getWidth())/((double)road.getLinesToHead()+road.getLinesToTail());
		double linesBlockedRate = blockWidth / lineWidth;
		return road.getLinesToHead()-(int) Math.floor(linesBlockedRate + 0.5d);
	}

	public static void main(String[] args) {
		boolean oldGIS = false;
		for (int i=0;i<args.length;++i) {
			if (args[i].equalsIgnoreCase("--oldgis")) oldGIS = true;
			else {
				printUsage();
				return;
			}
		}
		try {
			Road[] r = MapFiles.loadRoads("road.bin");
			Node[] n = MapFiles.loadNodes("node.bin");
			Building[] b = MapFiles.loadBuildings("building.bin");
			new ScenarioMaker(r,n,b,oldGIS);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printUsage() {
		System.out.println("Usage: ScenarioMaker [--oldgis]");
	}

	private class Summary extends JLabel {
		public Summary() {
			super("FB: 0, FS: 0, PF: 0, PO: 0, AT: 0, AC: 0, Civ: 0");
		}

		public void update() {
			int fb = memory.getObjectsOfType(RescueConstants.TYPE_FIRE_BRIGADE).size();
			int fs = memory.getObjectsOfType(RescueConstants.TYPE_FIRE_STATION).size();
			int pf = memory.getObjectsOfType(RescueConstants.TYPE_POLICE_FORCE).size();
			int po = memory.getObjectsOfType(RescueConstants.TYPE_POLICE_OFFICE).size();
			int at = memory.getObjectsOfType(RescueConstants.TYPE_AMBULANCE_TEAM).size();
			int ac = memory.getObjectsOfType(RescueConstants.TYPE_AMBULANCE_CENTER).size();
			int civ = memory.getObjectsOfType(RescueConstants.TYPE_CIVILIAN).size();
			setText("FB: "+fb+", FS: "+fs+", PF: "+pf+", PO: "+po+", AT: "+at+", AC: "+ac+", Civ: "+civ);
		}
	}

	private static class BigRoadRenderer implements MapRenderer {
		public boolean canRender(Object o) {
			return o instanceof Road;
		}

		public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) throws CannotFindLocationException {
			Road road = (Road)o;
			Node roadHead = (Node)memory.lookup(road.getHead());
			Node roadTail = (Node)memory.lookup(road.getTail());
			int headX = transform.toScreenX(roadHead.getX());
			int headY = transform.toScreenY(roadHead.getY());
			int tailX = transform.toScreenX(roadTail.getX());
			int tailY = transform.toScreenY(roadTail.getY());
			Shape shape = new java.awt.geom.Line2D.Double(headX,headY,tailX,tailY);
			shape = new BasicStroke(10).createStrokedShape(shape);
			Color c = Color.BLACK;
			int lanes = road.getLinesToHead();
			int free = freeLanes(road);
			c = Color.ORANGE;
			if (free==0) c = Color.BLACK;
			if (free==lanes) c = Color.WHITE;
			RenderTools.setLineMode(g,ViewConstants.LINE_MODE_SOLID,c);
			((Graphics2D)g).draw(shape);
			return shape;
		}
	}

	private static class ImportantBuildingRenderer implements MapRenderer {
		private BuildingRenderer downstream;

		public ImportantBuildingRenderer(BuildingRenderer downstream) {
			this.downstream = downstream;
		}

		public boolean canRender(Object o) {
			return o instanceof Building && downstream.canRender(o);
		}

		public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) throws CannotFindLocationException {
			Shape shape = downstream.render(o,memory,g,transform);
			int importance = ((Building)o).getImportance();
			if (importance > 1) {
				// Draw the importance on top of the shape
				g.setColor(Color.black);
				Rectangle bounds = shape.getBounds();
				FontMetrics metrics = g.getFontMetrics();
				String s = ""+importance;
				int width = metrics.stringWidth(s);
				int centerX = bounds.x + (bounds.width/2);
				g.drawString(s,centerX-(width/2),bounds.y+(bounds.height/2)+metrics.getDescent());
			}
			return shape;
		}
	}

	private static class HumanoidCountRenderer implements MapRenderer {
		private MapRenderer downstream;

		public HumanoidCountRenderer(MapRenderer downstream) {
			this.downstream = downstream;
		}

		public boolean canRender(Object o) {
			return o instanceof Humanoid && downstream.canRender(o);
		}

		public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) throws CannotFindLocationException {
			Shape shape = downstream.render(o,memory,g,transform);
			int position = ((Humanoid)o).getPosition();
			// Find all humanoids at this position
			int count = 0;
			for (RescueObject next : memory.getObjectsOfType(RescueConstants.TYPE_CIVILIAN,RescueConstants.TYPE_AMBULANCE_TEAM,RescueConstants.TYPE_FIRE_BRIGADE,RescueConstants.TYPE_POLICE_FORCE)) {
				Humanoid h = (Humanoid)next;
				if (h.getPosition()==position) ++count;
			}
			if (count > 1) {
				// Draw the count on top of the shape
				g.setColor(Color.black);
				Rectangle bounds = shape.getBounds();
				FontMetrics metrics = g.getFontMetrics();
				String s = ""+count;
				int width = metrics.stringWidth(s);
				int centerX = bounds.x + (bounds.width/2);
				g.drawString(s,centerX-(width/2),bounds.y+(bounds.height/2)+metrics.getDescent());
			}
			return shape;
		}
	}

	private class RoadSorter implements Comparator {
		private Memory m;

		public RoadSorter(Memory m) {
			this.m = m;
		}

		public int compare(Object o1, Object o2) {
			Road r1 = (Road)o1;
			Road r2 = (Road)o2;
			Node h1 = (Node)m.lookup(r1.getHead());
			Node t1 = (Node)m.lookup(r1.getTail());
			Node h2 = (Node)m.lookup(r2.getHead());
			Node t2 = (Node)m.lookup(r2.getTail());
			int x1 = (h1.getX()+t1.getX())/2;
			int y1 = (h1.getY()+t1.getY())/2;
			int x2 = (h2.getX()+t2.getX())/2;
			int y2 = (h2.getY()+t2.getY())/2;
			if (x1 < x2) return -1;
			if (x1 > x2) return 1;
			if (y1 < y2) return -1;
			if (y2 > y1) return 1;
			return 0;
		}
	}

	private class RandomiseDialog extends JDialog {
		private JSpinner civsField;
		private JSpinner fireField;
		private JSpinner policeField;
		private JSpinner ambulanceField;
		private JSpinner fireStationField;
		private JSpinner policeOfficeField;
		private JSpinner ambulanceCenterField;
		private JSpinner refugeField;
		private JSpinner firesField;
		private JButton ok;
		private JButton cancel;
		private boolean wasOK;

		public RandomiseDialog(Frame owner) {
			super(owner,"Randomise config",true);
			civsField = new JSpinner(new SpinnerNumberModel(70,50,80,1));
			fireField = new JSpinner(new SpinnerNumberModel(10,5,15,1));
			policeField = new JSpinner(new SpinnerNumberModel(10,5,15,1));
			ambulanceField = new JSpinner(new SpinnerNumberModel(8,5,10,1));
			fireStationField = new JSpinner(new SpinnerNumberModel(1,1,1,1));
			policeOfficeField = new JSpinner(new SpinnerNumberModel(1,1,1,1));
			ambulanceCenterField = new JSpinner(new SpinnerNumberModel(1,1,11,1));
			refugeField = new JSpinner(new SpinnerNumberModel(4,1,5,1));
			firesField = new JSpinner(new SpinnerNumberModel(4,2,8,1));
			ok = new JButton("OK");
			cancel = new JButton("Cancel");
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			JPanel main = new JPanel(layout);
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.WEST;
			JLabel l = new JLabel("Fire brigades");
			layout.setConstraints(l,c);
			main.add(l);
			++c.gridy;
			l = new JLabel("Police forces");
			layout.setConstraints(l,c);
			main.add(l);
			++c.gridy;
			l = new JLabel("Ambulance teams");
			layout.setConstraints(l,c);
			main.add(l);
			++c.gridy;
			l = new JLabel("Civilians");
			layout.setConstraints(l,c);
			main.add(l);
			c.gridy = 0;
			c.gridx = 2;
			l = new JLabel("Fire stations");
			layout.setConstraints(l,c);
			main.add(l);
			++c.gridy;
			l = new JLabel("Police offices");
			layout.setConstraints(l,c);
			main.add(l);
			++c.gridy;
			l = new JLabel("Ambulance centres");
			layout.setConstraints(l,c);
			main.add(l);
			++c.gridy;
			l = new JLabel("Refuges");
			layout.setConstraints(l,c);
			main.add(l);
			++c.gridy;
			l = new JLabel("Fires");
			layout.setConstraints(l,c);
			main.add(l);
			c.gridy = 0;
			c.gridx = 1;
			layout.setConstraints(fireField,c);
			main.add(fireField);
			++c.gridy;
			layout.setConstraints(policeField,c);
			main.add(policeField);
			++c.gridy;
			layout.setConstraints(ambulanceField,c);
			main.add(ambulanceField);
			++c.gridy;
			layout.setConstraints(civsField,c);
			main.add(civsField);
			c.gridy = 0;
			c.gridx = 3;
			layout.setConstraints(fireStationField,c);
			main.add(fireStationField);
			++c.gridy;
			layout.setConstraints(policeOfficeField,c);
			main.add(policeOfficeField);
			++c.gridy;
			layout.setConstraints(ambulanceCenterField,c);
			main.add(ambulanceCenterField);
			++c.gridy;
			layout.setConstraints(refugeField,c);
			main.add(refugeField);
			++c.gridy;
			layout.setConstraints(firesField,c);
			main.add(firesField);
			++c.gridy;
			c.gridx = 0;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.CENTER;
			layout.setConstraints(ok,c);
			main.add(ok);
			c.gridx = 2;
			layout.setConstraints(cancel,c);
			main.add(cancel);
			setContentPane(main);
			ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						wasOK = true;
						setVisible(false);
					}});
			cancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						wasOK = false;
						setVisible(false);
					}});
		}

		public boolean wasOK() {
			return wasOK;
		}

		public int getFireBrigades() {
			return ((Number)fireField.getValue()).intValue();
		}

		public int getFireStations() {
			return ((Number)fireStationField.getValue()).intValue();
		}

		public int getPoliceForces() {
			return ((Number)policeField.getValue()).intValue();
		}

		public int getPoliceOffices() {
			return ((Number)policeOfficeField.getValue()).intValue();
		}

		public int getAmbulanceTeams() {
			return ((Number)ambulanceField.getValue()).intValue();
		}

		public int getAmbulanceCentres() {
			return ((Number)ambulanceCenterField.getValue()).intValue();
		}

		public int getCivs() {
			return ((Number)civsField.getValue()).intValue();
		}

		public int getRefuges() {
			return ((Number)refugeField.getValue()).intValue();
		}

		public int getFires() {
			return ((Number)firesField.getValue()).intValue();
		}
	}

	private class ToolAction extends AbstractAction {
		private Tool tool;

		public ToolAction(Tool t) {
			this(t,t.getName());
		}

		public ToolAction(Tool t, String name) {
			super(name);
			tool = t;
		}

		public void actionPerformed(ActionEvent e) {
			changeTool(tool);
		}
	}
}
