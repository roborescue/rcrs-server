package firesimulator.simulator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import firesimulator.util.Configuration;
import firesimulator.util.Rnd;
import firesimulator.world.Building;
import firesimulator.world.FireBrigade;
import firesimulator.world.World;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.uncommons.maths.random.GaussianGenerator;

public class Simulator {
    private static final Log LOG = LogFactory.getLog(Simulator.class);
	
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
    public static float AIR_CELL_HEAT_CAPACITY = 1f;
	
    public Set monitors;
    public static boolean verbose;
    private static Simulator me;

    private EnergyHistory energyHistory;

    public Simulator(World world){
        me = this;
        monitors=new HashSet();
        verbose = true;
        //        this.kernel=kernel;
        this.world=world;
        //        kernel.register(this);
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
	
    public void step(int timestep){	
        energyHistory = new EnergyHistory(world, timestep);
        refill();
        executeExtinguishRequests();
        burn();			
        cool();
        updateGrid();		
        exchangeBuilding();
        //FIXED
        cool();
        energyHistory.registerFinalEnergy(world);
        energyHistory.logSummary();
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
                LOG.debug("refilling fire brigade "+fb.getID());
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
        for (Building b : world.getBuildings()) {
            if (b.getTemperature() >= b.getIgnitionPoint() && b.fuel > 0 && b.isInflameable()) {
                float consumed = b.getConsum();
                if(consumed > b.fuel) {
                    consumed = b.fuel;
                }
                double oldFuel = b.fuel;
                double oldEnergy = b.getEnergy();
                double oldTemp = b.getTemperature();
                b.setEnergy(b.getEnergy() + consumed);
                energyHistory.registerBurn(b, consumed);
                b.fuel -= consumed;	
                b.setPrevBurned(consumed);
                /*
                  LOG.debug("Building " + b.getID() + " burned " + consumed + " fuel.");
                  LOG.debug("Old fuel: " + oldFuel + ", old energy: " + oldEnergy + ", old temperature: " + oldTemp);
                  LOG.debug("New fuel: " + b.fuel + ", new energy: " + b.getEnergy() + ", new temperature: " + b.getTemperature());
                */
            }
            else {
                b.setPrevBurned(0f);
            }			
        }
    }
	
    private void waterCooling(Building b) {
        double lWATER_COEFFICIENT=(b.getFieryness()>0&&b.getFieryness()<4?WATER_COEFFICIENT:WATER_COEFFICIENT*GAMMA);
        boolean cond = false;
        if(b.getWaterQuantity()>0){
            double oldEnergy = b.getEnergy();
            double oldTemp = b.getTemperature();
            double oldWater = b.getWaterQuantity();
            double dE=b.getTemperature() * b.getCapacity();
            if (dE <= 0) {
                //                LOG.debug("Building already at or below ambient temperature");
                return;
            }
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
            energyHistory.registerCool(b, effect);
            LOG.debug("Building " + b.getID() + " water cooling");
            LOG.debug("Old energy: " + oldEnergy + ", old temperature: " + oldTemp + ", old water: " + oldWater);
            LOG.debug("Consumed " + consumed + " water: effect = " + effect);
            LOG.debug("New energy: " + b.getEnergy() + ", new temperature: " + b.getTemperature() + ", new water: " + b.getWaterQuantity());
        }
    }

    private void exchangeBuilding() {	    
        for(Iterator i=world.getBuildings().iterator();i.hasNext();){
            Building b=(Building)i.next();
            exchangeWithAir(b);
        }
        double sumdt=0;
        Map<Building, Double> radiation = new HashMap<Building, Double>();
        for(Iterator i=world.getBuildings().iterator();i.hasNext();){
            Building b=(Building)i.next();			
            double radEn=b.getRadiationEnergy();
            radiation.put(b, radEn);
        }
        for(Iterator i=world.getBuildings().iterator();i.hasNext();){
            Building b=(Building)i.next();			
            double radEn=radiation.get(b);
            Building[] bs=b.connectedBuilding;
            float[] vs=b.connectedValues;
            /*
              LOG.debug("Building " + b.getID() + " radiating energy");
              LOG.debug("Total energy: " + b.getEnergy());
              LOG.debug("Radiated energy: " + radEn);
              LOG.debug("Old temperature: " + b.getTemperature());
            */
            for(int c=0;c<vs.length;c++){			    
                double oldEnergy=bs[c].getEnergy();
                double connectionValue=vs[c];			    			   
                double a=radEn*connectionValue;
                double sum=oldEnergy+a;
                bs[c].setEnergy(sum);
                energyHistory.registerRadiationGain(bs[c], a);
                /*
                  LOG.debug("Building " + bs[c].getID() + " connection value: " + connectionValue);
                  LOG.debug("Building " + bs[c].getID() + " received " + a);
                */
            }
            b.setEnergy(b.getEnergy()-radEn);			
            energyHistory.registerRadiationLoss(b, -radEn);
            /*
              LOG.debug("New temperature: " + b.getTemperature());
            */
        }		
    }

    private void exchangeWithAir(Building b) {
        // Give/take heat to/from air cells
        double oldTemperature = b.getTemperature();
        double oldEnergy = b.getEnergy();
        double energyDelta = 0;

        /*
        if (b.getID() == 16204 || b.getID() == 23545) {
            LOG.debug("Building " + b.getID() + " heat exchange");
            LOG.debug("Building energy: " + oldEnergy);
            LOG.debug("Building temperature: " + oldTemperature);
            LOG.debug("AIR_TO_BUILDING_COEFFICIENT: " + AIR_TO_BUILDING_COEFFICIENT);
            LOG.debug("AIR_CELL_HEAT_CAPACITY: " + AIR_CELL_HEAT_CAPACITY);
            LOG.debug("TIME_STEP_LENGTH: " + TIME_STEP_LENGTH);
            LOG.debug("CELL_SIZE: " + world.SAMPLE_SIZE);
        }
        */

        for (int[] nextCell : b.cells) {
            int cellX = nextCell[0];
            int cellY = nextCell[1];
            double cellCover = nextCell[2] / 100.0;
            double cellTemp = world.getAirCellTemp(cellX, cellY);
            double dT = cellTemp - b.getTemperature();
            double energyTransferToBuilding = dT * AIR_TO_BUILDING_COEFFICIENT * TIME_STEP_LENGTH * cellCover * world.SAMPLE_SIZE;
            energyDelta += energyTransferToBuilding;
            double newCellTemp = cellTemp - energyTransferToBuilding / (AIR_CELL_HEAT_CAPACITY * world.SAMPLE_SIZE);
            world.setAirCellTemp(cellX, cellY, newCellTemp);

            /*
            if (b.getID() == 16204 || b.getID() == 23545) {
                LOG.debug("Cell " + cellX + ", " + cellY);
                LOG.debug("Area covered: " + cellCover);
                LOG.debug("Cell temperature: " + cellTemp);
                LOG.debug("dT: " + dT);
                LOG.debug("Energy transfer to building: " + energyTransferToBuilding);
                LOG.debug("New cell temperature: " + newCellTemp);
            }
            */
        }
        b.setEnergy(oldEnergy + energyDelta);
        energyHistory.registerAir(b, energyDelta);

        /*
        if (b.getID() == 16204 || b.getID() == 23545) {
            LOG.debug("New energy: " + b.getEnergy());
            LOG.debug("New temperature: " + b.getTemperature());
        }
        */
    }

    /*
      private double averageTemp(Building b) {
      double total=0;
      double tempSum=0;
      //        LOG.debug("Finding average cell temperature for building " + b.getID());
      for(int i=0;i<b.cells.length;i++){
      double pc=((double)b.cells[i][2])/100d;
      total+=pc;
      tempSum+=getTempAt(b.cells[i][0],b.cells[i][1])*pc;
      }		
      return tempSum/total;
      }
    */

    private void updateGrid() {
        LOG.debug("Updating air grid");
        double[][] airtemp=world.getAirTemp();
        double[][] newairtemp = new double[airtemp.length][airtemp[0].length];
        for(int x=0;x<airtemp.length;x++){
            for(int y=0;y<airtemp[0].length;y++){
                double dt = (averageTemp(x,y)-airtemp[x][y]);
                double change = (dt * AIR_TO_AIR_COEFFICIENT * TIME_STEP_LENGTH);
                newairtemp[x][y] = relTemp(airtemp[x][y] + change);
                //                if (newairtemp[x][y] > 0.000001 || airtemp[x][y] > 0.000001) {
                //                    LOG.debug("Cell " + x + ", " + y + " old temperature: " + airtemp[x][y] + ", dt: " + dt + ", change: " + change + ", new temp: " + newairtemp[x][y]);
                //                }
                if(!(newairtemp[x][y]>-Double.MAX_VALUE&&newairtemp[x][y]<Double.MAX_VALUE)){
                    LOG.warn("Value is not sensible: " + newairtemp[x][y]);
                    newairtemp[x][y]=Double.MAX_VALUE*0.75;
                }
                if(newairtemp[x][y] == Double.NEGATIVE_INFINITY || newairtemp[x][y] == Double.POSITIVE_INFINITY) {
                    LOG.warn("aha");
                }
            }
        }
        world.setAirTemp(newairtemp);		
        world.setAirTemp(getWindShift().shift(world.getAirTemp(),this));
    }
    
    private double relTemp(double deltaT){
        return Math.max(0, deltaT*ENERGY_LOSS*TIME_STEP_LENGTH);
    }
	
    private double averageTemp(int x, int y) {
        //        double rv = (neighbourCellAverage(x,y)+buildingAverage(x,y))/(weightSummBuilding(x,y)+weightSummCells(x,y));
        double rv = neighbourCellAverage(x, y) / weightSummCells(x, y);
        return rv;
    }

    /*
      private float buildingAverage(int x, int y) {
      float total=0;
      for(Iterator i=world.gridToBuilding[x][y].iterator();i.hasNext();){
      Object o[]=(Object[])i.next();
      total+=((Building)o[0]).getTemperature()*((Float)o[1]).floatValue();
      }
      if (Double.isNaN(total)) {
      LOG.warn("buildingAverage(" + x + ", " + y + ") returned NaN");
      total = 0;
      for(Iterator i=world.gridToBuilding[x][y].iterator();i.hasNext();){
      Object o[]=(Object[])i.next();
      Building b = (Building)o[0];
      float f = ((Float)o[1]).floatValue();
      LOG.debug("Building " + b.getID() + " temperature: " + b.getTemperature());
      LOG.debug("Building " + b.getID() + " coverage   : " + f);
      total+=((Building)o[0]).getTemperature()*((Float)o[1]).floatValue();
      LOG.debug("New total: " + total);
      }
      LOG.debug("Done");
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
    */

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
            return 0;
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
        AIR_TO_BUILDING_COEFFICIENT = new Float(Configuration.getValue("resq-fire.air_to_building_flow")).floatValue();
        AIR_TO_AIR_COEFFICIENT =new Float(Configuration.getValue("resq-fire.air_to_air_flow")).floatValue(); 
        ENERGY_LOSS = new Float(Configuration.getValue("resq-fire.energy_loss")).floatValue();		
        WATER_COEFFICIENT= new Float(Configuration.getValue("resq-fire.water_thermal_capacity")).floatValue();
        WIND_SPEED = new Integer(Configuration.getValue("resq-fire.wind_speed")).intValue();
        WIND_DIRECTION = new Float(Configuration.getValue("resq-fire.wind_direction")).floatValue();
        WIND_RANDOM = new Float(Configuration.getValue("resq-fire.wind_random")).floatValue();
        RADIATION_COEFFICENT=new Float(Configuration.getValue("resq-fire.radiation_coefficient")).floatValue();
        AIR_CELL_HEAT_CAPACITY=new Float(Configuration.getValue("resq-fire.air_cell_heat_capacity")).floatValue();
        ExtinguishRequest.MAX_WATER_PER_CYCLE=new Integer(Configuration.getValue("resq-fire.max_extinguish_power_sum")).intValue();
        ExtinguishRequest.MAX_DISTANCE=new Integer(Configuration.getValue("resq-fire.water_distance")).intValue();
        GAMMA=new Float(Configuration.getValue("resq-fire.gamma")).floatValue();
        Rnd.setSeed(new Long(Configuration.getValue("resq-fire.randomseed")).longValue());
        java.util.Random random = new java.util.Random(new Long(Configuration.getValue("resq-fire.randomseed")).longValue());
        Building.burnRate = new GaussianGenerator(new Double(Configuration.getValue("resq-fire.burn-rate-average")).doubleValue(),
                                                  new Double(Configuration.getValue("resq-fire.burn-rate-variance")).doubleValue(),
                                                  random);
    }
	
    public void initialize(){		
        try{
            loadVars();
        }catch (Exception e) {
            LOG.fatal("invalid configuration, aborting", e);
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
