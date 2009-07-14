package firesimulator.world;

import java.awt.Polygon;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;



import firesimulator.simulator.Simulator;
import firesimulator.util.Geometry;

/**
 * @author tn
 *
 */
public class Building extends StationaryObject {

	public static int WATER_EXTINGUISH_PARAM;
	public static float woodSpeed;
	public static float steelSpeed;
	public static float concreteSpeed;
	public static float WATER_CAPCITY;
	public static boolean POLICE_INFALMEABLE=false;
	public static boolean AMBULANCE_INFALMEABLE=false;
	public static boolean FIRE_INFALMEABLE=false;
	public static boolean REFUGE_INFALMEABLE=false;
	public boolean fierynessChanged;
	private int waterQuantity;
	private int floors=1;
	private int attributes=0;
	private int ignition=0;
	protected int fieryness=0;
	private int brokenness=0;
	public int[][] cells;
	private int[] entrances;
	private int code=0;
	private int buildingAreaGround=0;
	private int buildingAreaTotal=0;
	private int[] apexes;
	private Polygon polygon;	
	public float fuel;
	private float initFuel;
	private float prevBurned;
	public float volume;
	public float capacity;
	private double energy;
	static final int FLOOR_HEIGHT=3;
	public float cooling=0;
	public Collection walls;
	public Hashtable connectedBuildings;
	public Building[] connectedBuilding;
	public float[]	connectedValues;
	public double totalWallArea;
	private int lwater = 0;
	private int lwTime = -1;
    private boolean wasEverWatered = false; 
    public boolean inflameable = true;
	
	public static float woodCapacity=4;
	public static float steelCapacity=4;
	public static float concreteCapacity=4;
	public static float woodIgnition=400;
	public static float steelIgnition=400;
	public static float concreteIgnition=400;
	public static float woodEnergie=1;
	public static float steelEnergie=1;
	public static float concreteEnergie=1;	
	public static float woodBurning=800;
	public static float steelBurning=800;
	public static float concreteBurning=800;
	
	public static final int NORMAL=0;
	public static final int HEATING=1;
	public static final int BURNING=2;
	public static final int COOLING_DOWN=3;
	public static final int EXTINGUISHED=5;
	public static final int BURNED_DOWN=4;
	

	public Building(int id) {
		super(id);
		polygon=null;
		connectedBuildings=new Hashtable(30);
		initFuel = -1;
		prevBurned=0;
	}	
	
	public float getBurningTemp(){
		switch(code){
			case 0:
				return woodBurning;
			case 1:
				return steelBurning;
			default:
				return concreteBurning;
		}
	}
	
	public float getIgnitionPoint(){
		switch(code){
			case 0:
				return woodIgnition;
			case 1:
				return steelIgnition;
			default:
				return concreteIgnition;
		}
	}
	
	
	public void ignite() {
		energy=getCapacity()*getIgnitionPoint()*1.5;
	}

	public int getBuildingAreaGround(){
		return buildingAreaGround;
	}
	
	public int getFloors(){
		return floors;
	}
	
	public void initialize(World world){			
		initWalls(world);
		setFieryness(0);
		setWaterQuantity(0);
		fierynessChanged=false;	
		if(polygon==null){
			polygon=new Polygon();
			for(int n=0;n<apexes.length;n++)
				polygon.addPoint(apexes[n],apexes[++n]);
		}
		volume=(buildingAreaGround*floors*FLOOR_HEIGHT)/10;
		fuel=getInitialFuel();
		setCapacity(volume*getThermoCapacity());
		energy=world.INITIAL_TEMP*getCapacity();
		initFuel=-1;
		prevBurned=0;
		lwTime = -1;
        lwater = 0;	        
        wasEverWatered = false;
	}
	
	public void reset(World w) {
        setFieryness(0);
		setWaterQuantity(0);		
		initialize(w);	
	}
	
	private void initWalls(World world){
	    if(walls != null)
	        return;
		totalWallArea=0;
		walls=new LinkedList();
		int fx=apexes[0];
		int fy=apexes[1];
		int lx=fx;
		int ly=fy;
		for(int n=2;n<apexes.length;n++){
			int tx=apexes[n];
			int ty=apexes[++n];
			Wall w=new Wall(lx,ly,tx,ty,this);
			if(w.validate()){
				walls.add(w);
				totalWallArea+=FLOOR_HEIGHT*1000*w.length;
			}
			else
				System.out.println("warning: ignoring odd wall at building "+getID());
			lx=tx;
			ly=ty;			
		}
		Wall w=new Wall(lx,ly,fx,fy,this);
		walls.add(w);		
		world.allWalls.addAll(walls);
		totalWallArea=totalWallArea/1000000d;
	}
	
	public int hashCode(){
	    return id;
	}
	
	public void initWallValues(World world){			
		int totalHits=0;
		int totalRays=0;
		int selfHits=0;
		int strange=0;
		for(Iterator w=walls.iterator();w.hasNext();){
			Wall wall=(Wall)w.next();			
			wall.findHits(world);
			totalHits+=wall.hits;
			selfHits+=wall.selfHits;
			totalRays+=wall.rays;
			strange=wall.strange;
		}		
		int c=0;
		connectedBuilding=new Building[connectedBuildings.size()];
		connectedValues=new float[connectedBuildings.size()];
        float base = totalRays;
		for(Enumeration e=connectedBuildings.keys();e.hasMoreElements();c++){
			Building b=(Building)e.nextElement();
			Integer value=(Integer)connectedBuildings.get(b);
			connectedBuilding[c]=b;
			connectedValues[c]=value.floatValue()/base;
		}
		System.out.print("{"+(((float)totalHits)*100/((float)totalRays))+","+totalRays+","+totalHits+","+selfHits+","+strange+"}");
	}
	
	public float getInitialFuel(){
	    if(initFuel<0){
	        initFuel = (float)(getFuelDensity()*buildingAreaGround*floors*FLOOR_HEIGHT)/10;
	    }
		return initFuel;
	}
	
	private float getFuelDensity(){
		switch(code){
			case 0:
				return woodEnergie;
			case 1:
				return steelEnergie;
			default:
				return concreteEnergie;
		}
	}
	
	private float getThermoCapacity(){
		switch(code){
			case 0:
				return woodCapacity;
			case 1:
				return steelCapacity;
			default:
				return concreteCapacity;
		}
	}

	public Polygon getPolygon(){
		return polygon;
	}
	
	public int getType(){
		return TYPE_BUILDING;
	}
	
	private void setAttributes(int atrb){
		this.attributes=atrb;
	}
	
	public int getCode(){
		return code;
	}
	
	public void setIgnition(int ignition){
		this.ignition=ignition;
	}
	
	public int getIgnition(){
		return ignition;
	}
	
	public void setFieryness(int fieryness){		
		this.fieryness=fieryness;
	}
	
	public float getFuel(){
	    return fuel;
	}
	
	public int getFieryness(){ 
		if(!isInflameable())
		    return 0;
		if(getTemperature()>=getIgnitionPoint()){
			if(fuel>=getInitialFuel()*0.66)
				return 1;   // burning, slightly damaged
			if(fuel>=getInitialFuel()*0.33)
				return 2;   // burning, more damaged
			if(fuel>0)
			    return 3;    // burning, severly damaged 
	    }
		if(fuel==getInitialFuel())
            if (wasEverWatered)
                return 4;   // not burnt, but watered-damaged
            else    
                return 0;   // not burnt, no water damage
		if(fuel>=getInitialFuel()*0.66)
			return 5;        // extinguished, slightly damaged
		if(fuel>=getInitialFuel()*0.33)
			return 6;        // extinguished, more damaged
		if(fuel>0)
			return 7;        // extinguished, severely damaged
		return 8;           // completely burnt down
	}
	
	private void setBrokenness(int brk){
		this.brokenness=brk;
	}
	
	private void setEntrances(int[] ent){
		this.entrances=ent;
	}
	
	private void setCode(int code){
		this.code=code;
	}
	
	private void setBuildingAreaGround(int area){
		this.buildingAreaGround=area;
	}

	private void setBuildingAreaTotal(int area){
		this.buildingAreaTotal=area;
	}
	
	private void setApexes(int[] apx){
		this.apexes=apx;
	}
	
	public int[] getApexes(){
		return apexes;
	}
		
	private void setFloors(int floors){
		this.floors=floors;
	}

	public void input(int property, int[] value) {
		switch(property) {
			case PROPERTY_FLOORS:
				setFloors(value[0]);
				break;
			case PROPERTY_BUILDING_ATTRIBUTES:
				setAttributes(value[0]);
				break;
			case PROPERTY_IGNITION:
				//				System.out.println("Building "+id+" ignition: "+value[0]);
				setIgnition(value[0]);
				break;
			case PROPERTY_FIERYNESS:
				setFieryness(value[0]);
				break;
			case PROPERTY_BROKENNESS:
				setBrokenness(value[0]);
				break;
			case PROPERTY_ENTRANCES:
				setEntrances(value);
				break;
			case PROPERTY_BUILDING_CODE:
				setCode(value[0]);
				break;
			case PROPERTY_BUILDING_AREA_GROUND:
				setBuildingAreaGround(value[0]);
				break;
			case PROPERTY_BUILDING_AREA_TOTAL:
				setBuildingAreaTotal(value[0]);
				break;
			case PROPERTY_BUILDING_APEXES:
				setApexes(value);
				break;
			default: 				
				super.input(property, value);
				break;
		}
	  }
	  
	public void encode(DataOutputStream dos){
		try {
			dos.writeInt(getType());
			dos.writeInt(getID());
			dos.writeInt(16); // Size of building data
			dos.writeInt(PROPERTY_FIERYNESS);
			dos.writeInt(4); // Size of FIERYNESS
			dos.writeInt(getFieryness());
			dos.writeInt(PROPERTY_NULL);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void findCells(World w) {
		LinkedList tmp=new LinkedList();
		for(int x=0;x<w.getAirTemp().length;x++)
			for(int y=0;y<w.getAirTemp()[0].length;y++){
				int xv=x*w.SAMPLE_SIZE+w.getMinX();
				int yv=y*w.SAMPLE_SIZE+w.getMinY();
				if(Geometry.boundingTest(polygon,xv,yv,w.SAMPLE_SIZE,w.SAMPLE_SIZE)){
					int pc=Geometry.percent((float)xv,(float)yv,(float)w.SAMPLE_SIZE,(float)w.SAMPLE_SIZE,polygon);
					if(pc>0){
						tmp.add(new Integer(x));
						tmp.add(new Integer(y));
						tmp.add(new Integer(pc));
						Object[] o=new Object[]{this,new Float(pc)};
						w.gridToBuilding[x][y].add(o);
					}
				}
			}
		if(tmp.size()>0){
			cells=new int[tmp.size()/3][3];
			Iterator i=tmp.iterator();
			for(int c=0;c<cells.length;c++){
				cells[c][0]=((Integer)i.next()).intValue();
				cells[c][1]=((Integer)i.next()).intValue();
				cells[c][2]=((Integer)i.next()).intValue();
			}
		}else System.out.println("WARNING: "+getID()+" has no cell");
	}



	public double getTemperature() {
	    double rv=energy/getCapacity();
	    if(rv==Double.NaN||rv==Double.POSITIVE_INFINITY||rv==Double.NEGATIVE_INFINITY)
	        rv=Double.MAX_VALUE*0.75;
		return rv;
	}

	public String codeToString() {
		switch(code){
			case 0:
				return "wooden";
			case 1:
				return "steelframe";
			default:
				return "concret";
		}
	}

	public int getLastWater(){
	    return lwater;
	}
	
	public boolean getLastWatered(){
	    return lwTime == World.getWorld().getTime();
	}
	
    public boolean wasEverWatered() {
        return wasEverWatered;
    }
    
	public int getWaterQuantity() {
		return waterQuantity;
	}

	public void setWaterQuantity(int i) {
	    if(i > waterQuantity){
	        lwTime = World.getWorld().getTime();
	        lwater = i - waterQuantity;	        
            wasEverWatered = true;
	    }
		waterQuantity = i;
	}
	

	public float getCapacity() {
		return capacity;
	}

	public void setCapacity(float f) {
		capacity = f;
	}

	public float getEnergieDensity() {
		switch(code){
			case 0:
				return woodEnergie;
			case 1:
				return steelEnergie;
			default:
				return concreteEnergie;
		}
	}

	public float getReationSpeed() {
		switch(code){
			case 0:
				return woodSpeed;
			case 1:
				return steelSpeed;
			default:
				return concreteSpeed;
		}		
	}
	
	public String toString(){
		String rv="building "+getID()+"\n";
		for(Iterator i=walls.iterator();i.hasNext();rv+=i.next()+"\n");
		return rv;
	}

	public double getRadiationEnergy() {
		double t=getTemperature()+273;
		double radEn=(t*0.01)*(t*0.01)*(t*Simulator.RADIATION_COEFFICENT)*(t*Simulator.RADIATION_COEFFICENT)*totalWallArea;
		if(radEn==Double.NaN||radEn==Double.POSITIVE_INFINITY||radEn==Double.NEGATIVE_INFINITY)
		    radEn=Double.MAX_VALUE*0.75;
		return radEn;
	}

	public boolean isBuilding(int x,int y){
		return getX()==x&&getY()==y;
	}
	
    public double getEnergy() {
        if(energy==Double.NaN||energy==Double.POSITIVE_INFINITY||energy==Double.NEGATIVE_INFINITY)
            energy=Double.MAX_VALUE*0.75d;
        return energy;
    }
    public void setEnergy(double energy) {        
        if(energy==Double.NaN||energy==Double.POSITIVE_INFINITY||energy==Double.NEGATIVE_INFINITY){
            energy=Double.MAX_VALUE*0.75d;            
        }
        this.energy = energy;        		    
    }

    public float getConsum() {
        if(fuel==0){
            return 0;
        }
        float tf = (float) (getTemperature()/1000f);
        float lf = getFuel()/getInitialFuel();
        float f = tf*lf*0.2f;
        if(f<0.005f)
            f=0.005f;
        return getInitialFuel()*f;
    }
    
    public float getPrevBurned() {
        return prevBurned;
    }
    
    public void setPrevBurned(float prevBurned) {
        this.prevBurned = prevBurned;
    }
    
    public void setInflameable(boolean inflameable){
        this.inflameable=inflameable;
    }
    
    public boolean isInflameable(){
        return inflameable;
    }
}
