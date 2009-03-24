package firesimulator.simulator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import firesimulator.kernel.Kernel;
import firesimulator.util.Configuration;
import firesimulator.util.Rnd;
import firesimulator.world.Building;
import firesimulator.world.FireBrigade;
import firesimulator.world.World;


public class Simulator {
	
	private Kernel kernel;
	private World world;
	private WindShift windShift;
	
	public static float GAMMA=0.5f;
	public static float AIR_TO_AIR_COEFFICIENT=0.5f;
	public static float AIR_TO_BUILDING_COEFFICIENT=45f;
	public static float WATER_COEFFICIENT=0.5f;
	public static float ENERGY_LOSS=0.9f;
	public static float WIND_DIRECTION=0.9f;
	public static float WIND_RANDOM=0f;
	public static int   WIND_SPEED=0;
	public static float RADIATION_COEFFICENT=1.0f;
	public static float TIME_STEP_LENGTH=1f; 		
    public static float WEIGHT_GRID = 0.2f;
	
	public Set monitors;
	public static boolean verbose;
	private static Simulator me;

	public Simulator(Kernel kernel,World world){
	    me = this;
	    monitors=new HashSet();
	    verbose = true;
		this.kernel=kernel;
		this.world=world;
		kernel.register(this);
		windShift=null;
	}
	
	public static Simulator getSimulator(){
	    return me;
	}
	
	public void addMonitor(Monitor monitor){
	    monitors.add(monitor);
	}
	
	public void removeMonitor(Monitor monitor){
	    monitors.remove(monitor);
	}
	
	private void informStep(){
	    for(Iterator i = monitors.iterator();i.hasNext();){
	        ((Monitor)i.next()).step(world);
	    }
	}
	
	private void informDone(){
	    for(Iterator i = monitors.iterator();i.hasNext();){
	        ((Monitor)i.next()).done(world);
	    }
	}
	
	private void informReset(){
	    for(Iterator i = monitors.iterator();i.hasNext();){
	        ((Monitor)i.next()).reset(world);
	    }
	}

	public void run(){
		kernel.establishConnection();
		initialize();		
		kernel.signalReadyness();
		goLoop();
	}

	public void goLoop(){
		for(int i=0;i<1000;i++){			
			if(!kernel.waitForNextCycle()) break;
			long t=System.currentTimeMillis();
 			step();
			if(verbose)
			    System.out.println("t="+world.getTime()+" ("+(System.currentTimeMillis()-t)+"ms)");		
			kernel.sendUpdate();						
			kernel.receiveUpdate();
			informStep();
		}
		informDone();
	}
	
	private void step(){	
	    refill();
		executeExtinguishRequests();
		burn();			
		cool();
		updateGrid();		
		exchangeBuilding();
		//FIXED
		cool();
	}
	
	private void cool(){
		for(Iterator i = world.getBuildings().iterator();i.hasNext();){
			Building b = (Building) i.next();
			waterCooling(b);
		}
	}

	private void refill(){
	    for(Iterator i = world.getFirebrigades().iterator();i.hasNext();){
	        FireBrigade fb = ((FireBrigade)i.next());
	        if(fb.refill()){
	            System.out.println("refilling fire brigade "+fb.getID());
	        }
	    }
	}
	
	private void executeExtinguishRequests() {
		for(Iterator i=world.getExtinguishIterator();i.hasNext();){
			ExtinguishRequest er=(ExtinguishRequest)i.next();
			er.execute();
		}
		world.clearExtinguishRequests();
	}

	private void burn() {		
		for(Iterator i=world.getBuildings().iterator();i.hasNext();){
			Building b=(Building)i.next();			
			if(b.getTemperature()>=b.getIgnitionPoint()&&b.fuel>0&&b.isInflameable()){
				float consumed=b.getConsum();				
				if(consumed>b.fuel){				
					consumed=b.fuel;
					b.fuel=0f;
					b.setEnergy(b.getEnergy()+consumed);
					b.setPrevBurned(consumed);
				}else{
				    b.setEnergy(b.getEnergy()+consumed);
				    b.fuel-=consumed;	
				    b.setPrevBurned(consumed);
				}
			}else{
			    b.setPrevBurned(0f);
			}			
		}
	}
	
	private void waterCooling(Building b) {
		double lWATER_COEFFICIENT=(b.getFieryness()>0&&b.getFieryness()<4?WATER_COEFFICIENT:WATER_COEFFICIENT*GAMMA);
		boolean cond = false;
		if(b.getWaterQuantity()>0){
			double dE=(b.getTemperature()-world.INITIAL_TEMP)*b.getCapacity();
			double effect=b.getWaterQuantity()*lWATER_COEFFICIENT;
			int consumed=b.getWaterQuantity();
			if(effect>dE){
				cond = true;
			    double pc=1-((effect-dE)/effect);
				effect*=pc;
				consumed*=pc;
			}
			b.setWaterQuantity(b.getWaterQuantity()-consumed);
			b.setEnergy(b.getEnergy()-effect);					
		}
	}

	private void exchangeBuilding() {	    
		for(Iterator i=world.getBuildings().iterator();i.hasNext();){
			Building b=(Building)i.next();
			double dT=((double)averageTemp(b))-b.getTemperature();
			float coefficient=(AIR_TO_BUILDING_COEFFICIENT*TIME_STEP_LENGTH)/b.getCapacity();
			double T=b.getTemperature()+(dT*coefficient);
			b.setEnergy(T*b.getCapacity());			
		}
		double sumdt=0;
		for(Iterator i=world.getBuildings().iterator();i.hasNext();){
			Building b=(Building)i.next();			
			double radEn=b.getRadiationEnergy();			
			Building[] bs=b.connectedBuilding;
			float[] vs=b.connectedValues;			
			for(int c=0;c<vs.length;c++){			    
			    double oldEnergy=bs[c].getEnergy();
			    double connectionValue=vs[c];			    			   
			    double a=radEn*connectionValue;
			    double sum=oldEnergy+a;
				bs[c].setEnergy(sum);				
			}			
			b.setEnergy(b.getEnergy()-radEn);			
		}		
	}

	private double averageTemp(Building b) {
		double total=0;
		double tempSum=0;
		for(int i=0;i<b.cells.length;i++){
		    double pc=((double)b.cells[i][2])/100d;
			total+=pc;
			tempSum+=getTempAt(b.cells[i][0],b.cells[i][1])*pc;
		}		
		return tempSum/total;
	}

	private void updateGrid() {
		double[][] airtemp=world.getAirTemp();
		double[][] newairtemp = new double[airtemp.length][airtemp[0].length];
		for(int x=0;x<airtemp.length;x++){
			for(int y=0;y<airtemp[0].length;y++){
				newairtemp[x][y]=airtemp[x][y]+((averageTemp(x,y)-airtemp[x][y])*AIR_TO_AIR_COEFFICIENT*TIME_STEP_LENGTH);
				if(!(newairtemp[x][y]>Double.MIN_VALUE&&newairtemp[x][y]<Double.MAX_VALUE)){
				    newairtemp[x][y]=Double.MAX_VALUE*0.75;
				}
                if(newairtemp[x][y] == Double.NEGATIVE_INFINITY || newairtemp[x][y] == Double.NEGATIVE_INFINITY)
                    System.out.println("aha");
			}
		}
		for(int x=0;x<newairtemp.length;x++){
			for(int y=0;y<newairtemp[0].length;y++){
                double deltaT = newairtemp[x][y]-world.INITIAL_TEMP;
                newairtemp[x][y]= world.INITIAL_TEMP+relTemp(deltaT);
				if(!(newairtemp[x][y]>Double.MIN_VALUE&&newairtemp[x][y]<Double.MAX_VALUE)){
				    newairtemp[x][y]=Double.MAX_VALUE*0.75;
				}
                if(newairtemp[x][y] == Double.NEGATIVE_INFINITY || newairtemp[x][y] == Double.NEGATIVE_INFINITY)
                    System.out.println("aha2");
			}
		}
		world.setAirTemp(newairtemp);		
		world.setAirTemp(getWindShift().shift(world.getAirTemp(),this));
	}
    
    private double relTemp(double deltaT){
        
        return deltaT*ENERGY_LOSS*TIME_STEP_LENGTH;
    }
	
	private double averageTemp(int x, int y) {
		double rv =  (neighbourCellAverage(x,y)+buildingAverage(x,y))/(weightSummBuilding(x,y)+weightSummCells(x,y));

        return rv;
	}

	private float buildingAverage(int x, int y) {
		float total=0;
		for(Iterator i=world.gridToBuilding[x][y].iterator();i.hasNext();){
			Object o[]=(Object[])i.next();
			total+=((Building)o[0]).getTemperature()*((Float)o[1]).floatValue();
		}
		return total;
	}
	
	private float weightSummBuilding(int x,int y){
		float total=0;
		for(Iterator i=world.gridToBuilding[x][y].iterator();i.hasNext();){
			Object o[]=(Object[])i.next();
			total+=((Float)o[1]).floatValue();
		}
		return total;
	}

	private double neighbourCellAverage(int x, int y) {
		double total=getTempAt(x+1,y-1);
		total+=getTempAt(x+1,y);
		total+=getTempAt(x+1,y+1);
		total+=getTempAt(x,y-1);
		total+=getTempAt(x,y+1);
		total+=getTempAt(x-1,y-1);
		total+=getTempAt(x-1,y);
		total+=getTempAt(x-1,y+1);		
		return total*WEIGHT_GRID;
	}
	
	private float weightSummCells(int x,int y){
		return 8 * WEIGHT_GRID;
	}
	
	protected double getTempAt(int x,int y){
		if(x<0||y<0||x>=world.getAirTemp().length||y>=world.getAirTemp()[0].length)
			return world.INITIAL_TEMP;
		return world.getAirTemp()[x][y];
	}

	public void setWind(float direction,float speed){
		windShift=new WindShift(direction,(float)speed,world.SAMPLE_SIZE);
	}
	
	public void setWindSpeed(float speed){
		windShift=getWindShift();
		setWind(windShift.getDirection(),speed);
	}
	
	public void setWindDirection(float direction){
		windShift=getWindShift();
		setWind(direction,windShift.speed);
	}
	
	public WindShift getWindShift(){
		if(WIND_RANDOM>0&&windShift!=null){
			float nd=(float) (windShift.direction+windShift.direction*WIND_RANDOM*Rnd.get01());
			float ns=(float) (windShift.speed+windShift.speed*WIND_RANDOM*Rnd.get01());			
			setWind(nd,ns);
		}
		if (windShift==null||windShift.direction!=WIND_DIRECTION||windShift.speed!=WIND_SPEED)
			setWind(WIND_DIRECTION,WIND_SPEED);
		return windShift;
	}

	private void loadVars(){			
		AIR_TO_BUILDING_COEFFICIENT = new Float(Configuration.getValue("air_to_building_flow")).floatValue();
		AIR_TO_AIR_COEFFICIENT =new Float(Configuration.getValue("air_to_air_flow")).floatValue(); 
		ENERGY_LOSS = new Float(Configuration.getValue("energy_loss")).floatValue();		
		WATER_COEFFICIENT= new Float(Configuration.getValue("water_thermal_capacity")).floatValue();
		WIND_SPEED = new Integer(Configuration.getValue("wind_speed")).intValue();
		WIND_DIRECTION = new Float(Configuration.getValue("wind_direction")).floatValue();
		WIND_RANDOM = new Float(Configuration.getValue("wind_random")).floatValue();
		RADIATION_COEFFICENT=new Float(Configuration.getValue("radiation_coefficient")).floatValue();
		ExtinguishRequest.MAX_WATER_PER_CYCLE=new Integer(Configuration.getValue("max_extinguish_power_sum")).intValue();
		ExtinguishRequest.MAX_DISTANCE=new Integer(Configuration.getValue("water_distance")).intValue();
		GAMMA=new Float(Configuration.getValue("gamma")).floatValue();
		Rnd.setSeed(new Long(Configuration.getValue("randomseed")).longValue());		
	}
	
	private void initialize(){		
		try{
			loadVars();
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("invalid configuration, aborting");
			System.exit(-1);			
		}
			
		world.initialize();
	}
	
	public void reset(){
		loadVars();
		world.reset();
		informReset();
	}
}
