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

import firesimulator.simulator.ExtinguishRequest;
import firesimulator.util.Configuration;

/**
 * @author tn
 *
 */
public class World implements WorldConstants {   
    
	private Hashtable pool;
	private Collection extinguishRequests;
	private int time;
	private Collection updatelist;
	private Collection buildings;
	private Collection firebrigades;	
	private Collection streetNodes;
	private Collection roads;
	private int maxX;
	private int maxY;
	private int minX;
	private int minY;
	private double[][] airTemp;
	public ArrayList[][] gridToBuilding;
	public int SAMPLE_SIZE=5000;
	public float INITIAL_TEMP=20;
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
		firebrigades=new LinkedList();
		buildings=new LinkedList();
		streetNodes=new LinkedList();
		roads=new LinkedList();
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
		SAMPLE_SIZE=new Integer(Configuration.getValue("cell_size")).intValue();
		Building.concreteBurning=new Float(Configuration.getValue("concrete_burning")).floatValue();
		Building.concreteCapacity=new Float(Configuration.getValue("concrete_capacity")).floatValue();
		Building.concreteEnergie=new Float(Configuration.getValue("concrete_energy")).floatValue();
		Building.concreteIgnition=new Float(Configuration.getValue("concrete_ignition")).floatValue();
		Building.concreteSpeed=new Float(Configuration.getValue("concrete_speed")).floatValue();
		Building.steelBurning=new Float(Configuration.getValue("steel_burning")).floatValue();
		Building.steelCapacity=new Float(Configuration.getValue("steel_capacity")).floatValue();
		Building.steelEnergie=new Float(Configuration.getValue("steel_energy")).floatValue();
		Building.steelIgnition=new Float(Configuration.getValue("steel_ignition")).floatValue();
		Building.steelSpeed=new Float(Configuration.getValue("steel_speed")).floatValue();
		Building.woodBurning=new Float(Configuration.getValue("wooden_burning")).floatValue();
		Building.woodCapacity=new Float(Configuration.getValue("wooden_capacity")).floatValue();
		Building.woodEnergie=new Float(Configuration.getValue("wooden_energy")).floatValue();
		Building.woodIgnition=new Float(Configuration.getValue("wooden_ignition")).floatValue();
		Building.woodSpeed=new Float(Configuration.getValue("wooden_speed")).floatValue();
		Building.FIRE_INFALMEABLE=new Boolean(Configuration.getValue("fire_station_inflammable")).booleanValue();
		Building.AMBULANCE_INFALMEABLE=new Boolean(Configuration.getValue("ambulance_center_inflammable")).booleanValue();
		Building.POLICE_INFALMEABLE=new Boolean(Configuration.getValue("police_office_inflammable")).booleanValue();
		Building.REFUGE_INFALMEABLE=new Boolean(Configuration.getValue("refuge_inflammable")).booleanValue();
		Wall.RAY_RATE=new Float(Configuration.getValue("ray_rate")).floatValue();		
		FireBrigade.REFILL_QUANTITY=new Integer(Configuration.getValue("water_refill_rate")).intValue();
		FireBrigade.MAX_WATER_QUANTITY=new Integer(Configuration.getValue("water_capacity")).intValue();
	}
	
	public void initialize(){	
		loadVars();
		allWalls.clear();
		clearExtinguishRequests();
		initializeBuildings();				
		initializeRoads();		
		initializeAir();
		igniteGISFires();
		isInitialized=true;		
	}
	

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

	private void initializeBuildings() {
		for(Iterator i=buildings.iterator();i.hasNext();){						
			Building b=(Building)i.next();
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
		String fname=hash+".rays";			
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
			System.out.println("loaded radiation sample file \""+fname+"\"");
		}catch(Exception e){			
			System.out.println("unable to load radiation sample file \""+fname+"\", sampling:");
			int n=0;
			long t1=System.currentTimeMillis();
			for(Iterator i=buildings.iterator();i.hasNext();){
				Building b=(Building)i.next();
				System.out.print("building "+b.getID()+" ("+(n++)+" of "+buildings.size()+") ");
				b.initWallValues(this);
				long dt=System.currentTimeMillis()-t1;
				dt=dt/n;
				dt=dt*(buildings.size()-n);
				long sec=dt/(1000);
				long min=(sec/60)%60;
				long hour=sec/(60*60);
				sec=sec%60;				
				System.out.println(" time left: ca. "+hour+":"+min+":"+sec);
			}	
		}		
		try{
			if(!loaded){
				File f=new File(fname);
				f.createNewFile();				
				BufferedWriter bw=new BufferedWriter(new FileWriter(f));				
				bw.write(Wall.RAY_RATE+"\n");
				for(Iterator i=buildings.iterator();i.hasNext();){
					Building b=(Building)i.next();
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
				System.out.println("wrote radiation sample file \""+fname+"\"");
			}			
		}catch(Exception e){
			System.out.println("error while writting radiation sample file \""+fname+"\"");
		}
	}


	private Building getBuilding(int x, int y) {
		for(Iterator i=buildings.iterator();i.hasNext();){
			Building b=(Building)i.next();
			if(b.isBuilding(x,y))
				return b;
		}
		System.out.println("parser error");
		throw new NullPointerException();		
	}

	public float getMaxDistance(){
		return maxDist;
	}

	private void initializeAir() {
		int xSampels=1+(maxX-minX)/SAMPLE_SIZE;		
		int ySamples=1+(maxY-minY)/SAMPLE_SIZE;
		System.out.println("grid cell size="+SAMPLE_SIZE+"mm, x*y="+ySamples+"*"+xSampels+"="+xSampels*ySamples);
		airTemp=new double[xSampels][ySamples];
		for(int x=0;x<airTemp.length;x++)
			for(int y=0;y<airTemp[x].length;y++)
				airTemp[x][y]=INITIAL_TEMP;
		CAPACITY=(int)(SAMPLE_SIZE*SAMPLE_SIZE*AIR_HEIGHT*AIR_CAPACITY)/1000000;
		//assign buildings
		gridToBuilding=new ArrayList[xSampels][ySamples];
		for(int x=0;x<gridToBuilding.length;x++)
			for(int y=0;y<gridToBuilding[0].length;y++)
				gridToBuilding[x][y]=new ArrayList();
		for(Iterator i=buildings.iterator();i.hasNext();)						
			((Building)i.next()).findCells(this);					
	}

	public double[][] getAirTemp(){
		return airTemp;
	}
	
	public void setAirTemp(double[][] a){
		airTemp = a;
	}

	public Collection getBuildings(){
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
	}
	
	public void setTime(int time){
		this.time=time;
	}
	
	public void processUpdate(int[] data, int offset, int time){
		//		System.out.println("processing update...");
		//		for(int j=0;j<data.length;j++)System.out.println("data["+j+"]="+data[j]);
		setTime(time);
		for (int i = offset;  data[i] != TYPE_NULL;  i ++){
			int type = data[i++];
			if (type==TYPE_NULL) break;
			int size = data[i++];
			int id   = data[i++];
			//			System.out.println("Update for object "+id+" (type="+type+", size="+size);
			RescueObject obj = getObject(id);
			if (obj == null) {
				obj = createObject(type,id);
			   	if (obj != null){
					putObject(obj);
			   	}else System.out.println("warning: unknown object (type:"+type+")");
			}
			if(obj!=null){
				while (data[i] != PROPERTY_NULL){
					i += setProperty(data, i, obj);
				}				
			}
		}
	}
	
	private int setProperty(int[] data, int index, RescueObject obj) {
		int property = data[index++];
		int size = data[index++]/4;
		//		System.out.println("Property="+property+", size="+size);
		int[] val;
		switch (property) {
		case PROPERTY_WIDTH:
		case PROPERTY_BUILDING_AREA_TOTAL:
		case PROPERTY_START_TIME:
		case PROPERTY_LINES_TO_HEAD:
		case PROPERTY_CARS_PASS_TO_HEAD:
		case PROPERTY_LATITUDE:
		case PROPERTY_DIRECTION:
		case PROPERTY_BLOCK:
		case PROPERTY_STAMINA:
		case PROPERTY_HEAD:
		case PROPERTY_BUILDING_CODE:
		case PROPERTY_SIGNAL:
		case PROPERTY_FIERYNESS:
		case PROPERTY_WIND_DIRECTION:
		case PROPERTY_LENGTH:
		case PROPERTY_FLOORS:
		case PROPERTY_HUMANS_PASS_TO_TAIL:
		case PROPERTY_ROAD_KIND:
		case PROPERTY_REPAIR_COST:
		case PROPERTY_IGNITION:
		case PROPERTY_BUILDING_AREA_GROUND:
		case PROPERTY_BROKENNESS:
		case PROPERTY_WIND_FORCE:
		case PROPERTY_WIDTH_FOR_WALKERS:
		case PROPERTY_MEDIAN_STRIP:
		case PROPERTY_Y:
		case PROPERTY_X:
		case PROPERTY_POSITION:
		case PROPERTY_POSITION_EXTRA:
		case PROPERTY_WATER_QUANTITY:
		case PROPERTY_LONGITUDE:
			//		case PROPERTY_STRETCHED_LENGTH:
		case PROPERTY_HUMANS_PASS_TO_HEAD:
		case PROPERTY_BURIEDNESS:
		case PROPERTY_LINES_TO_TAIL:
		case PROPERTY_HP:
		case PROPERTY_DAMAGE:
		case PROPERTY_CARS_PASS_TO_TAIL:
		case PROPERTY_BUILDING_ATTRIBUTES:
		case PROPERTY_TAIL:
			val = new int[] {data[index]};
			break;
		case PROPERTY_SIGNAL_TIMING:
		case PROPERTY_POSITION_HISTORY:
		case PROPERTY_EDGES:
		case PROPERTY_SHORTCUT_TO_TURN:
		case PROPERTY_ENTRANCES:
		case PROPERTY_BUILDING_APEXES:
		case PROPERTY_POCKET_TO_TURN_ACROSS:
			val = new int[data[index++]];
			for (int i=0;i<val.length;++i) val[i] = data[index++];
			break;
		default:
			val = null;
			break;
		}
		if (val!=null)
			obj.input(property,val);
		return size+2;
	}
	 
	private int[] getListElement(int[] data, int index) {
		int[] result = new int[data[index] / 4]; 
		System.arraycopy(data, index + 1, result, 0, result.length);
		return result;
	}

	private int[] getIDsArray(int[] data, int index) {
		int length;
		for (length = 0;  data[index + length] != 0;  length ++) { }
		int[] result = new int[length];
		System.arraycopy(data, index, result, 0, result.length);
		return result;
	}
	
	private RescueObject createObject(int type,int id){
		RescueObject obj=null;
		switch(type){
		case TYPE_BUILDING:
			obj=new Building(id);
			buildings.add(obj);
			break;
		case TYPE_REFUGE:
			obj=new Refuge(id);
			buildings.add(obj);
			break;
		case TYPE_WORLD:
			obj=new WorldInfo(id);
			break;
		case TYPE_ROAD:
			obj=new Road(id);
			roads.add(obj);
			break;
		case TYPE_NODE:
			obj=new StreetNode(id);
			streetNodes.add(obj);
			break;
		case TYPE_CIVILIAN:
			obj=new Civilian(id);
			break;
		case TYPE_CAR:
			obj=new Civilian(id);
			break;
		case TYPE_AMBULANCE_TEAM:
			obj=new AmbulanceTeam(id);
			break;
		case TYPE_FIRE_BRIGADE:
			obj=new FireBrigade(id);
			firebrigades.add(obj);
			break;
		case TYPE_POLICE_FORCE:
			obj=new PoliceForce(id);
			break;
		case TYPE_AMBULANCE_CENTER:
			obj=new AmbulanceCenter(id);
			buildings.add(obj);
			break;
		case TYPE_FIRE_STATION:
			obj=new FireStation(id);
			buildings.add(obj);
			break;
		case TYPE_POLICE_OFFICE:
			obj=new PoliceOffice(id);
			buildings.add(obj);
			break;				
		}
		// Moving objects need the world to get their position
		if (obj instanceof MovingObject)
			((MovingObject) obj).setWorld(this);
			
		return obj;
	}


	public Collection getRoads() {
		return roads;
	}

	public Collection getNodes() {
		return streetNodes;
	}


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
				airTemp[x][y]=INITIAL_TEMP;
	}

	public void igniteGISFires(){
		//System.out.println("igniting gis fires");
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


	public void processCommands(int[] data) {								
		System.out.println("processing commands...");
		//		for(int j=0;j<data.length;j++)System.out.println("data["+j+"]="+data[j]);
		int c=2; // Skip the header and size
		setTime(data[2]); // Set the time
		int cmd;
		int id;
                int time;
		while((cmd=data[++c])!=TYPE_NULL){
			int size=data[++c]/4;
			switch(cmd){
			case AK_EXTINGUISH:
				System.out.println("EXTINGUISH");
				id=data[++c];
                                time = data[++c];
				System.out.println("fb.id="+id);					
				//					System.out.println("Command length: "+size);
				FireBrigade source=(FireBrigade)getObject(id);
				source.setCurrentAction(AK_EXTINGUISH);
				while(data[c+1]!=0){			
					//					System.out.println("new nozzle:");						
					id=data[++c];
					//					System.out.println("building="+id);
					Building target=(Building)getObject(id);							
					int direction=data[++c];
					//						System.out.println("direction="+direction);	
					int x=data[++c];
					//						System.out.println("nozzle.x="+x);	
					int y=data[++c];
					//						System.out.println("nozzle.y="+y);							
					int quantity=data[++c];
					//					System.out.println("quantity="+quantity);						
					ExtinguishRequest er=new ExtinguishRequest(source,target,quantity);
					extinguishRequests.add(er);
					//						System.out.println("added command");
				}
				c++;
				//					System.out.println("nozzels done");
				break;
			case AK_MOVE:
				//				case AK_STRETCH:
				id =data[c+1];
				//					System.out.println("MOVE (id="+id+")");
				MovingObject obj = (MovingObject)getObject(id);
				obj.setCurrentAction(cmd);
			default:					
				c+=size;
				break;							
			}
		}	
	}

	public void printSummary() {		
		System.out.println("objects total: "+countObjects());
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
