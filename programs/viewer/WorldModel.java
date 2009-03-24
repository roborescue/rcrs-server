package viewer;

import java.io.*;
import java.util.*;
import viewer.object.*;
import rescuecore.RescueConstants;
import rescuecore.InputBuffer;

public class WorldModel implements Constants {
	public final HashMap idObjMap = new HashMap();

	public final ArrayList nodeList          = new ArrayList();
	public final ArrayList roadList          = new ArrayList();
	public final ArrayList buildingList      = new ArrayList();
	public final ArrayList movingObjectList  = new ArrayList();
	public final ArrayList ambulanceTeamList = new ArrayList();
	public final ArrayList fireBrigadeList   = new ArrayList();
	public final ArrayList policeForceList   = new ArrayList();
	public final ArrayList civilianList      = new ArrayList();

	private int m_time = 0;
	public int time() { return m_time; }

	/**File used to log some information during the simulation.*/
	//	private PrintWriter _debugFile;//DEBUG

	/**
	 * Constructor of the class.
	 */
	public WorldModel()
	{
		/*
		try {
			_debugFile = new PrintWriter(new FileWriter("DebugInfo.txt"));//DEBUG
		}
		catch (IOException ex)
			{
				System.err.println("Error openning DebugInfo.txt.");
				System.err.println(ex.getMessage());
			}
		*/
	}

	public void add(RealObject obj) {
		idObjMap.put(new Integer(obj.id), obj);
		if      (obj instanceof Node)     nodeList    .add(obj);
		else if (obj instanceof Road)     roadList    .add(obj);
		else if (obj instanceof Building) buildingList.add(obj);
		else if (obj instanceof MovingObject) {
			movingObjectList.add(obj);
			switch (obj.type()) {
			default: break;
			case RescueConstants.TYPE_AMBULANCE_TEAM:  ambulanceTeamList.add(obj); break;
			case RescueConstants.TYPE_FIRE_BRIGADE:    fireBrigadeList.add(obj);   break;
			case RescueConstants.TYPE_POLICE_FORCE:    policeForceList.add(obj);   break;
			case RescueConstants.TYPE_CIVILIAN:        civilianList.add(obj);      break;
			}
		}
	}

	public RealObject get(int id) {
		return (RealObject) idObjMap.get(new Integer(id));
	}

	public void update(InputBuffer in, int time) {
		m_time = time;
		//		System.out.println("Updating world model");
		int type = in.readInt();
		while (type != RescueConstants.TYPE_NULL) {
			int size = in.readInt();
			int id = in.readInt();
			RescueObject obj = get(id);
			if (obj == null) {
				obj = newRescueObject(type, id);
				if (obj instanceof RealObject)
					add((RealObject) obj);
			}
			int propType = in.readInt();
			while (propType != RescueConstants.PROPERTY_NULL) {
				obj.input(propType, getProperty(propType, in));
				propType = in.readInt();
			}
			type = in.readInt();
		}

		if (time == 0) {
			m_initTotalHp = 0;
			for (Iterator it = movingObjectList.iterator();  it.hasNext();  )
				m_initTotalHp += ((Humanoid) it.next()).hp();
		}
		VIEWER.setStatus();

		//If the simulation is finished, we save the stats and close the viewer.
		if(WORLD.time() >= 300)
			{
				try{Thread.currentThread().sleep(5000);}catch(Exception e){}
				WORLD.saveStatInFile(); //To save the stats of the simulation in a file.
				System.exit(0);
			}
	}

	private RescueObject newRescueObject(int type, int id) {
		RescueObject obj;
		switch (type) {
		case RescueConstants.TYPE_WORLD:            obj = new World(id);           break;
		case RescueConstants.TYPE_RIVER:            obj = new River(id);           break;
		case RescueConstants.TYPE_RIVER_NODE:       obj = new RiverNode(id);       break;
		case RescueConstants.TYPE_ROAD:             obj = new Road(id);            break;
		case RescueConstants.TYPE_NODE:             obj = new Node(id);            break;
		case RescueConstants.TYPE_BUILDING:         obj = new Building(id);        break;
		case RescueConstants.TYPE_AMBULANCE_CENTER: obj = new AmbulanceCenter(id); break;
		case RescueConstants.TYPE_FIRE_STATION:     obj = new FireStation(id);     break;
		case RescueConstants.TYPE_POLICE_OFFICE:    obj = new PoliceOffice(id);    break;
		case RescueConstants.TYPE_REFUGE:           obj = new Refuge(id);          break;
		case RescueConstants.TYPE_CIVILIAN:         obj = new Civilian(id);        break;
		case RescueConstants.TYPE_AMBULANCE_TEAM:   obj = new AmbulanceTeam(id);   break;
		case RescueConstants.TYPE_FIRE_BRIGADE:     obj = new FireBrigade(id);     break;
		case RescueConstants.TYPE_POLICE_FORCE:     obj = new PoliceForce(id);     break;
		case RescueConstants.TYPE_CAR:              obj = new Car(id);             break;
		default: Util.myassert(false, "illeagle object type" + type); throw new Error();
		}
		return obj;
	}

	private int[] getProperty(int type, InputBuffer in) {
		int size = in.readInt();
		int[] val = null;
		switch (type) {
		case RescueConstants.PROPERTY_START_TIME:
		case RescueConstants.PROPERTY_LONGITUDE:
		case RescueConstants.PROPERTY_LATITUDE:
		case RescueConstants.PROPERTY_WIND_FORCE:
		case RescueConstants.PROPERTY_WIND_DIRECTION:
		case RescueConstants.PROPERTY_X:
		case RescueConstants.PROPERTY_Y:
		case RescueConstants.PROPERTY_DIRECTION:
		case RescueConstants.PROPERTY_POSITION:
		case RescueConstants.PROPERTY_POSITION_EXTRA:
		case RescueConstants.PROPERTY_STAMINA:
		case RescueConstants.PROPERTY_HP:
		case RescueConstants.PROPERTY_DAMAGE:
		case RescueConstants.PROPERTY_BURIEDNESS:
		case RescueConstants.PROPERTY_FLOORS:
		case RescueConstants.PROPERTY_BUILDING_ATTRIBUTES:
		case RescueConstants.PROPERTY_IGNITION:
		case RescueConstants.PROPERTY_BROKENNESS:
		case RescueConstants.PROPERTY_FIERYNESS:
			//		case RescueConstants.PROPERTY_BUILDING_SHAPE_ID:
		case RescueConstants.PROPERTY_BUILDING_CODE:
		case RescueConstants.PROPERTY_BUILDING_AREA_GROUND:
		case RescueConstants.PROPERTY_BUILDING_AREA_TOTAL:
		case RescueConstants.PROPERTY_WATER_QUANTITY:
			//		case RescueConstants.PROPERTY_STRETCHED_LENGTH:
		case RescueConstants.PROPERTY_HEAD:
		case RescueConstants.PROPERTY_TAIL:
		case RescueConstants.PROPERTY_LENGTH:
		case RescueConstants.PROPERTY_ROAD_KIND:
		case RescueConstants.PROPERTY_CARS_PASS_TO_HEAD:
		case RescueConstants.PROPERTY_CARS_PASS_TO_TAIL:
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_HEAD:
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_TAIL:
		case RescueConstants.PROPERTY_WIDTH:
		case RescueConstants.PROPERTY_BLOCK:
		case RescueConstants.PROPERTY_REPAIR_COST:
		case RescueConstants.PROPERTY_MEDIAN_STRIP:
		case RescueConstants.PROPERTY_LINES_TO_HEAD:
		case RescueConstants.PROPERTY_LINES_TO_TAIL:
		case RescueConstants.PROPERTY_WIDTH_FOR_WALKERS:
		case RescueConstants.PROPERTY_SIGNAL:
		case RescueConstants.PROPERTY_BUILDING_TEMPERATURE:
		case RescueConstants.PROPERTY_BUILDING_IMPORTANCE:
			val = new int[] {in.readInt()};
			break;
		case RescueConstants.PROPERTY_EDGES:
		case RescueConstants.PROPERTY_SIGNAL_TIMING:
		case RescueConstants.PROPERTY_SHORTCUT_TO_TURN:
		case RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS:
		case RescueConstants.PROPERTY_POSITION_HISTORY:
		case RescueConstants.PROPERTY_ENTRANCES:
		case RescueConstants.PROPERTY_BUILDING_APEXES:
			val = new int[in.readInt()];
			for (int i=0;i<val.length;++i) val[i] = in.readInt();
			break;
		default:
			System.err.println("Unrecognised property: "+type);
			in.skip(size);
			break;
		}
		return val;
	}

	private int m_minX = Integer.MAX_VALUE;
	private int m_minY = Integer.MAX_VALUE;
	private int m_maxX = Integer.MIN_VALUE;
	private int m_maxY = Integer.MIN_VALUE;
	public int minX() { return m_minX; }
	public int maxX() { return m_maxX; }
	public int minY() { return m_minY; }
	public int maxY() { return m_maxY; }

	public void setWorldRange() {
		Iterator it = idObjMap.values().iterator();
		//		System.out.println(idObjMap.values().size()+" objects");
		int roads = 0;
		int nodes = 0;
		int buildings = 0;
		int civilians = 0;
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof Building) ++buildings;
			if (obj instanceof Road) ++roads;
			if (obj instanceof Node) ++nodes;
			if (obj instanceof Civilian) ++civilians;
			if (!(obj instanceof PointObject))
				continue;
			PointObject po = (PointObject) obj;
			if (m_minX > po.x()) m_minX = po.x();
			if (m_maxX < po.x()) m_maxX = po.x();
			if (m_minY > po.y()) m_minY = po.y();
			if (m_maxY < po.y()) m_maxY = po.y();
		}
		//		System.out.println(roads+" roads");
		//		System.out.println(nodes+" nodes");
		//		System.out.println(buildings+" buildings");
		//		System.out.println(civilians+" civilians");
	}

	private IO io() { return Main.io(); }

	public void extractNextPositionPropertys() {
		if (!io().hasUpdateData(m_time + 1))
			return;
		InputBuffer dis = io().updateData(m_time + 1);
		int type = dis.readInt();
		while (type != RescueConstants.TYPE_NULL) {
			int size = dis.readInt();
			int id = dis.readInt();
			RescueObject obj = get(id);
			int pos = 0, posEx = 0, route[] = new int[0];
			MovingObject mv = null;
			if (obj instanceof MovingObject) {
				mv = (MovingObject) obj;
				pos   = mv.motionlessPosition().id;
				posEx = mv.positionExtra();
			}
			int propType = dis.readInt();
			while (propType != RescueConstants.PROPERTY_NULL) {
				int[] val = getProperty(propType, dis);
				switch (propType) {
				default: break;
				case RescueConstants.PROPERTY_POSITION:         pos   = val[0]; break;
				case RescueConstants.PROPERTY_POSITION_EXTRA:   posEx = val[0]; break;
				case RescueConstants.PROPERTY_POSITION_HISTORY: route = val;    break;
				}
				propType = dis.readInt();
			}
			if (obj instanceof MovingObject)
				mv.prepareForAnimation(pos, posEx, route);
			type = dis.readInt();
		}
	}

	public void progress() { playback(m_time + 1); }

	public void playback(int time) {
		for (int i = (time > m_time) ? m_time : 0;
			 i <= time  &&  io().hasUpdateData(i);
			 i ++) {
			//			System.out.println("Updating timestep "+i);
			update(io().updateData(i), i);
		}
	}

	public void parseCommands() {
		InputBuffer dis = io().commandsData(m_time + 1);
		//DEBUG
		//		_debugFile.println("");
		//		_debugFile.println("************************");
		//		_debugFile.println("Time: " + m_time);
		//		_debugFile.println("************************");
		//END DEBUG
		//		rescuecore.Handy.printBytes(dis);
		int time = dis.readInt();
		int command = dis.readInt();
		while (command != RescueConstants.HEADER_NULL) {
			int size = dis.readInt();
			if (size != 0) {
				int senderId = dis.readInt();
				byte[] content = new byte[size-RescueConstants.INT_SIZE];
				dis.readBytes(content);
				RealObject sender = get(senderId);
				//				if (sender instanceof Civilian) _debugFile.print(rescuecore.Handy.getCommandTypeName(command)+" ("+senderId+")");
				switch (command) {
				case RescueConstants.AK_MOVE:
				case RescueConstants.AK_RESCUE:
				case RescueConstants.AK_LOAD:
				case RescueConstants.AK_UNLOAD:
				case RescueConstants.AK_EXTINGUISH:
				case RescueConstants.AK_CLEAR:
					sender.setAction(command, content);
					break;
				case RescueConstants.AK_TELL:
				case RescueConstants.AK_SAY:
					sender.setCommunication(command, content);
					break;
				default:
					break;
				}
			}
			command = dis.readInt();
		}
	}

	// -------------------------------------------------------------------- score
	private int m_initTotalHp;
	private int _numDeads = 0;
	private int _numLiving = 0;
	private double _totalHp = 0;
	private double _totalNonburnedBldgArea = 0;
	private double _totalBldgArea = 0;

	public double score() {
        double totalHp = 0;
        int numLiving = 0;
        for (Iterator it = movingObjectList.iterator(); it.hasNext();) {
			Humanoid h = (Humanoid)it.next();
            int hp = h.hp();
			if (m_time>=300 && (h.buriedness()>0 || h.damage()>0)) hp = 0;
            totalHp += hp;
            if (hp > 0)
                numLiving++;
        }

        double totalBldgArea = 0;
        double totalNonburnedBldgArea = 0;
        for (Iterator it = buildingList.iterator(); it.hasNext();) {
            Building b = (Building) it.next();
            int area = b.buildingAreaTotal();
            totalBldgArea += area;
            double factor = 1.0;
            switch (b.fieryness()) {
			case 0:
				break;
			case 1 :   
			case 4 :
			case 5 :
				factor = 0.666666;
				break;
			case 2 :                    
			case 6 :
				factor = 0.333333;
				break;
			default :
				factor = 0;
				break;
            }
            totalNonburnedBldgArea += (factor * area);
        }

        return (numLiving + totalHp / m_initTotalHp)
			* Math.sqrt(totalNonburnedBldgArea / totalBldgArea);
    }

	/**
     * This method save the stat of the simulation at the end of it. Stats are saved
     * in a file to be used automatically and analyse. The format of a line in
     * the file is: score;dead;totalNumberOfAgent;RemaningHPOfAllAgents;HPAtInitial;AreaNotBurnt;AreaAtInitial;
     * safe;heating;onFire;Severe;Saved;PatBurnt;HalfBurnt;FullBurnt;Building0%;25%;50%;75%;100%;
     * Road0%;25%;50%;75%;100%;
     */
    public void saveStatInFile()
    {
        PrintWriter printWriter = null;
        try
			{
				printWriter = new PrintWriter(new FileWriter("SimulationsStats.txt", true), true);

				String stats = new String();
				stats = stats.concat("" + "" + Main.m_simulationID + ";");
				stats = stats.concat("" + "" + score() + ";");
				stats = stats.concat("" + "" + _numDeads + ";");
				stats = stats.concat("" + "" + _numLiving + ";");
				stats = stats.concat("" + "" + _totalHp + ";");
				stats = stats.concat("" + "" + m_initTotalHp + ";");
				stats = stats.concat("" + "" + _totalNonburnedBldgArea + ";");
				stats = stats.concat("" + "" + _totalBldgArea);

				printWriter.println(stats);

			}
        catch(java.io.IOException e)
			{
				System.out.println("IO Exception: SimulationsStats.txt");
				System.out.println(e.getMessage());
			}
        finally
			{
				if (printWriter != null)
					{
						printWriter.close();
					}
			}

        //Close the debug file
		//        _debugFile.close();

    }//END saveStatInFile
}
