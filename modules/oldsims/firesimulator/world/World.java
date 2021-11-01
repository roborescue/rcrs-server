package firesimulator.world;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashSet;

import firesimulator.util.Configuration;
import org.apache.log4j.Logger;

/**
 * @author tn
 *
 */
public class World implements WorldConstants {   
    private static final Logger LOG = Logger.getLogger(World.class);
    
    private Hashtable pool;
    private Collection extinguishRequests;
    private int time;
    private Collection updatelist;
    private Collection<Building> buildings;
    private Collection firebrigades;	
    //    private Collection streetNodes;
    //    private Collection roads;
    private int maxX;
    private int maxY;
    private int minX;
    private int minY;
    private double[][] airTemp;
    public ArrayList[][] gridToBuilding;
    public int SAMPLE_SIZE=5000;
    //    public float INITIAL_TEMP=20;
    public float AIR_CAPACITY=0.2f;
    public float AIR_HEIGHT=30;	
    public int CAPACITY;	
    public float maxDist;
    private boolean isInitialized;
    public Collection allWalls;
    private Long hashValue;
    private static World me;
	
    public World(){
        me = this;
        hashValue=null;
        pool=new Hashtable();
        allWalls=new LinkedList();
        extinguishRequests=new LinkedList();
        updatelist=new LinkedList();
        firebrigades=new HashSet();
        buildings=new HashSet<Building>();
        //        streetNodes=new HashSet();
        //        roads=new HashSet();
        maxX=Integer.MIN_VALUE;
        maxY=Integer.MIN_VALUE;
        minX=Integer.MAX_VALUE;
        minY=Integer.MAX_VALUE;
        isInitialized=false;		
    }
	
    public static World getWorld(){
        return me;
    }
	
    public int getMaxX(){
        return maxX;
    }
	
    public Iterator getExtinguishIterator(){
        return extinguishRequests.iterator();
    }
	
    public void addExtinguishRequest(Object request){
        extinguishRequests.add(request);		
    }
	
    public void clearExtinguishRequests(){
        extinguishRequests.clear();
        for(Iterator i=firebrigades.iterator();i.hasNext();){
            FireBrigade fb=(FireBrigade)i.next();
            fb.nextCycle();
        }
    }
	
    public boolean isIntialized(){
        return isInitialized;
    }
	
    public int getMaxY(){
        return maxY;
    }
	
    public int getMinX(){
        return minX;
    }
	
    public int getMinY(){
        return minY;
    }
	
    private void loadVars(){		
        SAMPLE_SIZE=new Integer(Configuration.getValue("resq-fire.cell_size")).intValue();
        Building.concreteBurning=new Float(Configuration.getValue("resq-fire.concrete_burning")).floatValue();
        Building.concreteCapacity=new Float(Configuration.getValue("resq-fire.concrete_capacity")).floatValue();
        Building.concreteEnergie=new Float(Configuration.getValue("resq-fire.concrete_energy")).floatValue();
        Building.concreteIgnition=new Float(Configuration.getValue("resq-fire.concrete_ignition")).floatValue();
        Building.concreteSpeed=new Float(Configuration.getValue("resq-fire.concrete_speed")).floatValue();
        Building.steelBurning=new Float(Configuration.getValue("resq-fire.steel_burning")).floatValue();
        Building.steelCapacity=new Float(Configuration.getValue("resq-fire.steel_capacity")).floatValue();
        Building.steelEnergie=new Float(Configuration.getValue("resq-fire.steel_energy")).floatValue();
        Building.steelIgnition=new Float(Configuration.getValue("resq-fire.steel_ignition")).floatValue();
        Building.steelSpeed=new Float(Configuration.getValue("resq-fire.steel_speed")).floatValue();
        Building.woodBurning=new Float(Configuration.getValue("resq-fire.wooden_burning")).floatValue();
        Building.woodCapacity=new Float(Configuration.getValue("resq-fire.wooden_capacity")).floatValue();
        Building.woodEnergie=new Float(Configuration.getValue("resq-fire.wooden_energy")).floatValue();
        Building.woodIgnition=new Float(Configuration.getValue("resq-fire.wooden_ignition")).floatValue();
        Building.woodSpeed=new Float(Configuration.getValue("resq-fire.wooden_speed")).floatValue();
        Building.FIRE_INFALMEABLE=new Boolean(Configuration.getValue("resq-fire.fire_station_inflammable")).booleanValue();
        Building.AMBULANCE_INFALMEABLE=new Boolean(Configuration.getValue("resq-fire.ambulance_center_inflammable")).booleanValue();
        Building.POLICE_INFALMEABLE=new Boolean(Configuration.getValue("resq-fire.police_office_inflammable")).booleanValue();
        Building.REFUGE_INFALMEABLE=new Boolean(Configuration.getValue("resq-fire.refuge_inflammable")).booleanValue();
        Wall.RAY_RATE=new Float(Configuration.getValue("resq-fire.ray_rate")).floatValue();
        Wall.MAX_SAMPLE_DISTANCE=new Integer(Configuration.getValue("resq-fire.max_ray_distance")).intValue();
        FireBrigade.REFILL_QUANTITY=new Integer(Configuration.getValue("resq-fire.water_refill_rate")).intValue();
        FireBrigade.REFILL_HYDRANT_QUANTITY=new Integer(Configuration.getValue("resq-fire.water_hydrant_refill_rate")).intValue();
        FireBrigade.MAX_WATER_QUANTITY=new Integer(Configuration.getValue("resq-fire.water_capacity")).intValue();
    }
	
    public void initialize(){	
        LOG.info("World initialising");
        loadVars();
        allWalls.clear();
        clearExtinguishRequests();
        initializeBuildings();				
        //        initializeRoads();		
        initializeAir();
        igniteGISFires();
        isInitialized=true;		
        LOG.info("World initialised");
    }
	

    /*
    private void initializeRoads() {
        for(Iterator i=streetNodes.iterator();i.hasNext();){
            StreetNode sn=(StreetNode)i.next();
            if(sn.getX()>maxX)maxX=sn.getX();
            if(sn.getX()<minX)minX=sn.getX();
            if(sn.getY()>maxY)maxY=sn.getY();
            if(sn.getY()<minY)minY=sn.getY();
        }
        for(Iterator i=roads.iterator();i.hasNext();((Road)i.next()).initialize(this));
    }
    */

    private void initializeBuildings() {
        for (Building b : buildings) {
            int[] ap=b.getApexes();
            for(int n=0;n<ap.length;n++){
                if(ap[n]>maxX)maxX=ap[n];
                if(ap[n]<minX)minX=ap[n];
                n++;
                if(ap[n]>maxY)maxY=ap[n];
                if(ap[n]<minY)minY=ap[n];
            }
            b.initialize(this);
        }		
        maxDist=(float)Math.sqrt(((maxX-minX)*(maxX-minX))+((maxY-minY)*(maxY-minY)));
        initRayValues();		
    }
	
    private void initRayValues() {		
        long hash=hash();
        boolean loaded=false;
        String fname=Configuration.getValue("resq-fire.rays.dir") + "/" + hash + ".rays";			
        try{
            File f=new File(fname);
            BufferedReader br=new BufferedReader(new FileReader(f));
            float rayDens=Float.parseFloat(br.readLine());
            String nl;
            while(null!=(nl=br.readLine())){
                int x=Integer.parseInt(nl);
                int y=Integer.parseInt(br.readLine());
                int quantity=Integer.parseInt(br.readLine());
                Building[] bl=new Building[quantity];
                float[] wght=new float[quantity];
                for(int c=0;c<quantity;c++){
                    int ox=Integer.parseInt(br.readLine());
                    int oy=Integer.parseInt(br.readLine());
                    bl[c]=(Building)getBuilding(ox,oy);
                    wght[c]=Float.parseFloat(br.readLine());
                }
                Building b=getBuilding(x,y);
                b.connectedBuilding=bl;
                b.connectedValues=wght;
            }
            loaded=true;
            LOG.info("loaded radiation sample file \""+fname+"\"");
        }catch(Exception e){			
            LOG.warn("unable to load radiation sample file \""+fname+"\", sampling:");
            int n=0;
            long t1=System.currentTimeMillis();
            for (Building b : buildings) {
                LOG.info("building "+b.getID()+" ("+(n++)+" of "+buildings.size()+") ");
                b.initWallValues(this);
                long dt=System.currentTimeMillis()-t1;
                dt=dt/n;
                dt=dt*(buildings.size()-n);
                long sec=dt/(1000);
                long min=(sec/60)%60;
                long hour=sec/(60*60);
                sec=sec%60;				
                LOG.info(" time left: ca. "+hour+":"+min+":"+sec);
            }	
        }		
        try{
            if(!loaded){
                File f=new File(fname);
                f.createNewFile();				
                BufferedWriter bw=new BufferedWriter(new FileWriter(f));				
                bw.write(Wall.RAY_RATE+"\n");
                for(Building b : buildings) {
                    bw.write(b.getX()+"\n");
                    bw.write(b.getY()+"\n");
                    bw.write(b.connectedBuilding.length+"\n");
                    for(int c=0;c<b.connectedBuilding.length;c++){
                        bw.write(b.connectedBuilding[c].getX()+"\n");
                        bw.write(b.connectedBuilding[c].getY()+"\n");
                        bw.write(b.connectedValues[c]+"\n");
                    }
                }
                bw.close();
                LOG.info("wrote radiation sample file \""+fname+"\"");
            }			
        }catch(Exception e){
            LOG.error("error while writting radiation sample file \""+fname+"\"", e);
        }
    }


    private Building getBuilding(int x, int y) {
        for (Building b : buildings) {
            if(b.isBuilding(x,y))
                return b;
        }
        LOG.error("parser error");
        throw new NullPointerException();		
    }

    public float getMaxDistance(){
        return maxDist;
    }

    private void initializeAir() {
        LOG.info("World width: " + (maxX - minX) + "mm");
        LOG.info("World height: " + (maxY - minY) + "mm");
        int xSamples=1 + (maxX - minX) / SAMPLE_SIZE;		
        int ySamples=1 + (maxY - minY) / SAMPLE_SIZE;
        LOG.info("grid cell size=" + SAMPLE_SIZE + "mm, x*y=" + xSamples + "*" + ySamples + " = " + (xSamples * ySamples));
        airTemp=new double[xSamples][ySamples];
        for(int x=0;x<airTemp.length;x++)
            for(int y=0;y<airTemp[x].length;y++)
                airTemp[x][y]=0;
        CAPACITY=(int)(SAMPLE_SIZE*SAMPLE_SIZE*AIR_HEIGHT*AIR_CAPACITY)/1000000;
        //assign buildings
        gridToBuilding=new ArrayList[xSamples][ySamples];
        for(int x=0;x<gridToBuilding.length;x++)
            for(int y=0;y<gridToBuilding[0].length;y++)
                gridToBuilding[x][y]=new ArrayList();
        for(Building b : buildings) {
            b.findCells(this);
        }
    }

    public double[][] getAirTemp(){
        return airTemp;
    }
	
    public void setAirTemp(double[][] a){
        airTemp = a;
    }

    public void setAirCellTemp(int x, int y, double temp) {
        airTemp[x][y] = temp;
    }

    public double getAirCellTemp(int x, int y) {
        return airTemp[x][y];
    }

    public Collection<Building> getBuildings(){
        return buildings;
    }
	
    public void addUpdate(RescueObject obj){
        updatelist.add(obj);
    }
	
    public void clearUpdates(){
        updatelist.clear();
    }
	
    public Collection getUpdates(){
        return updatelist;
    }
	
    public int countObjects(){
        return pool.size();
    }
	
    public int getTime(){
        return time;
    }
	
    public RescueObject getObject(int ID){
        return (RescueObject)pool.get(new Integer(ID));
    }
	
    public void putObject(RescueObject obj){
        pool.put(new Integer(obj.getID()),obj);
        if (obj instanceof FireBrigade) {
            firebrigades.add(obj);
        }
        if (obj instanceof Building) {
            buildings.add((Building)obj);
        }
        //        if (obj instanceof StreetNode) {
        //            streetNodes.add(obj);
        //        }
        //        if (obj instanceof Road) {
        //            roads.add(obj);
        //        }
        // Moving objects need the world to get their position
        if (obj instanceof MovingObject) {
            ((MovingObject) obj).setWorld(this);
        }
    }
	
    public void setTime(int time){
        this.time=time;
    }
	
    /*
    public void processChangeSet(InputBuffer data, int time){
        setTime(time);
        int count = data.readInt();
        for (int i = 0; i < count; ++i) {
            int id = data.readInt();
            int propCount = data.readInt();
            RescueObject obj = getObject(id);
            if (obj == null) {
                LOG.warn("Unknown object (id:"+id+")");
            }
            for (int j = 0; j < propCount; ++j) {
                String propUrn = data.readString();
                setProperty(data, obj, propUrn);
            }
        }
    }
	
    private void setProperty(InputBuffer data, RescueObject obj, String urn) {
        int size = data.readInt();
        int[] val;
        if (urn.equals("SIGNAL_TIMING")
            || urn.equals("POSITION_HISTORY")
            || urn.equals("EDGES")
            || urn.equals("SHORTCUT_TO_TURN")
            || urn.equals("ENTRANCES")
            || urn.equals("BUILDING_APEXES")
            || urn.equals("POCKET_TO_TURN_ACROSS")) {
            val = new int[data.readInt()];
            for (int i = 0; i < val.length; ++i) {
                val[i] = data.readInt();
            }
        }
        else {
            val = new int[] {data.readInt()};
        }
        if (obj != null) {
            obj.input(urn, val);
        }
    }
	 
    private RescueObject createObject(String urn, int id){
        RescueObject obj=null;
        if (urn.equals("BUILDING")) {
            obj=new Building(id);
            buildings.add((Building)obj);
        }
        else if (urn.equals("REFUGE")) {
            obj=new Refuge(id);
            buildings.add((Building)obj);
        }
        else if (urn.equals("WORLD")) {
            obj=new WorldInfo(id);
        }
        //        else if (urn.equals("ROAD")) {
        //            obj=new Road(id);
        //            roads.add(obj);
        //        }
        //        else if (urn.equals("NODE")) {
        //            obj=new StreetNode(id);
        //            streetNodes.add(obj);
        //        }
        else if (urn.equals("CIVILIAN")) {
            obj=new Civilian(id);
        }
        else if (urn.equals("CAR")) {
            obj=new Civilian(id);
        }
        else if (urn.equals("AMBULANCE_TEAM")) {
            obj=new AmbulanceTeam(id);
        }
        else if (urn.equals("FIRE_BRIGADE")) {
            obj=new FireBrigade(id);
            firebrigades.add(obj);
        }
        else if (urn.equals("POLICE_FORCE")) {
            obj=new PoliceForce(id);
        }
        else if (urn.equals("AMBULANCE_CENTRE")) {
            obj=new AmbulanceCenter(id);
            buildings.add((Building)obj);
        }
        else if (urn.equals("FIRE_STATION")) {
            obj=new FireStation(id);
            buildings.add((Building)obj);
        }
        else if (urn.equals("POLICE_OFFICE")) {
            obj=new PoliceOffice(id);
            buildings.add((Building)obj);
        }
        // Moving objects need the world to get their position
        if (obj instanceof MovingObject)
            ((MovingObject) obj).setWorld(this);
        return obj;
    }
    */


    //    public Collection getRoads() {
    //        return roads;
    //    }

    //    public Collection getNodes() {
    //        return streetNodes;
    //    }


    public void reset() {
        loadVars();
        setTime(0);				
        resetAir();
        for(Iterator i=buildings.iterator();i.hasNext();((Building)i.next()).reset(this));
        for(Iterator i=firebrigades.iterator();i.hasNext();((FireBrigade)i.next()).reset());
        igniteGISFires();
    }
	
	
    private void resetAir() {
        for(int x=0;x<airTemp.length;x++)
            for(int y=0;y<airTemp[x].length;y++)
                airTemp[x][y]=0;
    }

    public void igniteGISFires(){
        for(Iterator it=getBuildings().iterator();it.hasNext();){
            Building b=(Building)it.next();
            if(b.getIgnition()!=0){
                b.ignite();
                addUpdate(b);
            }
        }
    }
	


    public Collection getFirebrigades() {
        return firebrigades;
    }

    public void setFirebrigades(Collection collection) {
        firebrigades = collection;
    }

    /*
    public void processCommands(InputBuffer data) {								
        LOG.info("processing commands...");
        data.readInt(); // Skip the size
        data.readInt(); // Skip the simulator ID
        int time = data.readInt();
        setTime(time);
        int count = data.readInt();
        for (int i = 0; i < count; ++i) {
            String cmd = data.readString();
            int size = data.readInt();
            if ("AK_EXTINGUISH".equals(cmd)) {
                LOG.debug("EXTINGUISH");
                int agentID = data.readInt();
                data.readInt(); // Skip the command time
                LOG.debug("fb.id="+agentID);					
                FireBrigade source=(FireBrigade)getObject(agentID);
                source.setCurrentAction(cmd);
                int targetID = data.readInt();
                Building target=(Building)getObject(targetID);
                int quantity=data.readInt();
                ExtinguishRequest er=new ExtinguishRequest(source,target,quantity);
                extinguishRequests.add(er);
            }
            else if ("AK_MOVE".equals(cmd)) {
                int agentID = data.readInt();
                LOG.debug("MOVE (id="+agentID+")");
                MovingObject obj = (MovingObject)getObject(agentID);
                obj.setCurrentAction(cmd);
                data.skip(size - 4);
            }
            else {
                LOG.debug("Ignoring " + cmd);
                LOG.debug("Skipping " + size + " bytes");
                data.skip(size);
            }
        }	
    }
    */

    public void printSummary() {		
        LOG.debug("objects total: "+countObjects());
    }
	
    public long hash(){
        if(hashValue==null){
            long sum=0;
            for(Iterator i=buildings.iterator();i.hasNext();){
                Building b=(Building) i.next();				
                int[] ap=b.getApexes();				
                for(int c=0;c<ap.length;c++){
                    if(Long.MAX_VALUE-sum<=ap[c]){
                        sum=0;
                    }
                    sum+=ap[c];	
                }					
            }			
            hashValue=new Long(sum);
        }			
        return hashValue.longValue();
    }

}
