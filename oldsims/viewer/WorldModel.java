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
        if (time == 0) {
            // This is really a connect OK
            int count = in.readInt();
            System.out.println("Updating world model: " + count + " objects to read");
            for (int i = 0; i < count; ++i) {
                String type = in.readString();
                int id = in.readInt();
                int size = in.readInt();
                RescueObject obj = get(id);
                if (obj == null) {
                    obj = newRescueObject(type, id);
                    if (obj instanceof RealObject)
                        add((RealObject) obj);
                }
                if (obj == null) {
                    in.skip(size);
                }
                else {
                    String propType = in.readString();
                    while (!"".equals(propType)) {
                        obj.input(propType, getProperty(propType, in));
                        propType = in.readString();
                    }
                }
            }
        }
        else {
            int count = in.readInt();
            System.out.println("Updating world model: " + count + " objects to read");
            for (int i = 0; i < count; ++i) {
                int id = in.readInt();
                int propCount = in.readInt();
                RescueObject obj = get(id);
                for (int j = 0; j < propCount; ++j) {
                    String propType = in.readString();
                    int[] data = getProperty(propType, in);
                    if (obj != null) {
                        obj.input(propType, data);
                    }
                }
            }
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

    private RescueObject newRescueObject(String type, int id) {
        if ("WORLD".equals(type)) {
            return new World(id);
        }
        if ("RIVER".equals(type)) {
            return new River(id);
        }
        if ("RIVER_NODE".equals(type)) {
            return new RiverNode(id);
        }
        if ("ROAD".equals(type)) {
            return new Road(id);
        }
        if ("NODE".equals(type)) {
            return new Node(id);
        }
        if ("BUILDING".equals(type)) {
            return new Building(id);
        }
        if ("REFUGE".equals(type)) {
            return new Refuge(id);
        }
        if ("FIRE_STATION".equals(type)) {
            return new FireStation(id);
        }
        if ("AMBULANCE_CENTRE".equals(type)) {
            return new AmbulanceCenter(id);
        }
        if ("POLICE_OFFICE".equals(type)) {
            return new PoliceOffice(id);
        }
        if ("CIVILIAN".equals(type)) {
            return new Civilian(id);
        }
        if ("FIRE_BRIGADE".equals(type)) {
            return new FireBrigade(id);
        }
        if ("AMBULANCE_TEAM".equals(type)) {
            return new AmbulanceTeam(id);
        }
        if ("POLICE_FORCE".equals(type)) {
            return new PoliceForce(id);
        }
        if ("CAR".equals(type)) {
            return new Car(id);
        }
        Util.myassert(false, "illegal object type" + type);
        return null;
    }

    private int[] getProperty(String type, InputBuffer in) {
        int size = in.readInt();
        int[] val = null;
        if ("EDGES".equals(type)
            || "SIGNAL_TIMING".equals(type)
            || "SHORTCUT_TO_TURN".equals(type)
            || "POCKET_TO_TURN_ACROSS".equals(type)
            || "POSITION_HISTORY".equals(type)
            || "ENTRANCES".equals(type)
            || "BUILDING_APEXES".equals(type)) {
            val = new int[in.readInt()];
            for (int i=0;i<val.length;++i) val[i] = in.readInt();
        }
        else {
            val = new int[] {in.readInt()};
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
        int count = dis.readInt();
        for (int i = 0; i < count; ++i) {
            int id = dis.readInt();
            int propCount = dis.readInt();
            RescueObject obj = get(id);
            int pos = 0, posEx = 0, route[] = new int[0];
            MovingObject mv = null;
            if (obj instanceof MovingObject) {
                mv = (MovingObject) obj;
                pos   = mv.motionlessPosition().id;
                posEx = mv.positionExtra();
            }
            for (int j = 0; j < propCount; ++j) {
                String propType = dis.readString();
                int[] val = getProperty(propType, dis);
                if ("POSITION".equals(propType)) {
                    pos = val[0];
                }
                if ("POSITION_EXTRA".equals(propType)) {
                    posEx = val[0];
                }
                if ("POSITION_HISTORY".equals(propType)) {
                    route = val;
                }
            }
            if (obj instanceof MovingObject)
                mv.prepareForAnimation(pos, posEx, route);
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
        int id = dis.readInt();
        int time = dis.readInt();
        int count = dis.readInt();
        for (int i = 0; i < count; ++i) {
            String command = dis.readString();
            int size = dis.readInt();
            if (size != 0) {
                int senderId = dis.readInt();
                int commandTime = dis.readInt();
                byte[] content = new byte[size-RescueConstants.INT_SIZE-RescueConstants.INT_SIZE];
                dis.readBytes(content);
                RealObject sender = get(senderId);
                //				if (sender instanceof Civilian) _debugFile.print(rescuecore.Handy.getCommandTypeName(command)+" ("+senderId+")");
                if ("AK_MOVE".equals(command)
                    || "AK_RESCUE".equals(command)
                    || "AK_LOAD".equals(command)
                    || "AK_UNLOAD".equals(command)
                    || "AK_EXTINGUISH".equals(command)
                    || "AK_CLEAR".equals(command)) {
                    sender.setAction(command, content);
                } 
                if ("AK_TELL".equals(command)
                    || "AK_SAY".equals(command)) {
                    sender.setCommunication(command, content);
                }
            }
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
