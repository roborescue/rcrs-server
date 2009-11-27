package firesimulator.world;

import java.io.DataOutputStream;
import java.io.IOException;

import rescuecore.OutputBuffer;

public class FireBrigade extends MovingObject {

    public static int REFILL_QUANTITY;
    public static int MAX_WATER_QUANTITY; 
    private int initialWaterQuantity;
    private int waterQuantity;
    private int waterUsed;
    private boolean changed;
	
    public FireBrigade(int id) {
        super(id);
        initialWaterQuantity=0;
        waterQuantity=0;
        waterUsed=0;
        changed = false;
    }

    public String getType(){
        return "FIRE_BRIGADE";
    }
	
    public void input(String property, int[] value) {
        if ("WATER_QUANTITY".equals(property)) {
            setInitialWaterQuantity(value[0]);
        }
        else {
            super.input(property, value); 
        }
    }

    public void setInitialWaterQuantity(int quantity) {
        initialWaterQuantity=quantity;
        waterQuantity=quantity;			
    }
	
    public int getWaterQuantity(){
        return waterQuantity;
    }
	
    public int getWaterUsed(){
        return waterUsed;
    }
	
    public void setWaterQuantity(int quantity){
        waterQuantity=quantity;
        changed = true;
    }
	
    public void addWaterUsed(int quantity){
        waterUsed+=quantity;
    }
	
    public void nextCycle(){
        waterUsed=0;
        changed = false;
    }
	
    public void reset(){
        waterQuantity=initialWaterQuantity;
        waterUsed=0;
        changed = false;
    }

    public boolean hasChanged() {
        return changed;
    }

    public boolean refill() {	 	    
        if(!getCurrentAction().equals("AK_REST") || getLocation() == null || !( getLocation().isRefuge())) return false;	
        int fr = ((Refuge)getLocation()).getFieryness();
        if(fr == 3 || fr == 6 || fr == 7) return false;
        if(getWaterQuantity()+REFILL_QUANTITY>MAX_WATER_QUANTITY){			
            setWaterQuantity(MAX_WATER_QUANTITY);				
        }else{
            setWaterQuantity(getWaterQuantity()+REFILL_QUANTITY);
        }
        return true;
    }
	
    public void encode(OutputBuffer dos){
        dos.writeString(getType());
        dos.writeInt(getID());
        dos.writeInt(30); // Size of firebrigade data: 14 bytes of WATER_QUANTITY, 2x4 bytes of string length, 4 bytes of water quantity size, 4 bytes of water quantity.
        dos.writeString("WATER_QUANTITY");
        dos.writeInt(4); // Size of WATER_QUANTITY
        dos.writeInt(getWaterQuantity());
        dos.writeString("");
    }
}
