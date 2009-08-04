package firesimulator.world;

import java.io.DataOutputStream;
import java.io.IOException;

public class FireBrigade extends MovingObject {

	public static int REFILL_QUANTITY;
	public static int MAX_WATER_QUANTITY; 
	private int initialWaterQuantity;
	private int waterQuantity;
	private int waterUsed;
	
	public FireBrigade(int id) {
		super(id);
		initialWaterQuantity=0;
		waterQuantity=0;
		waterUsed=0;
	}

	public int getType(){
		return TYPE_FIRE_BRIGADE;
	}
	
	public void input(int property, int[] value) {
			switch(property) {	  		
				case PROPERTY_WATER_QUANTITY:
					setInitialWaterQuantity(value[0]);
					break;
				default: 
					super.input(property, value); 
					break;
		  }
	}

	private void setInitialWaterQuantity(int quantity) {
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
	}
	
	public void addWaterUsed(int quantity){
		waterUsed+=quantity;
	}
	
	public void nextCycle(){
		waterUsed=0;
	}
	
	public void reset(){
		waterQuantity=initialWaterQuantity;
		waterUsed=0;
	}

	public boolean refill() {	 	    
	    if(getCurrentAction() != AK_REST || getLocation() == null || !( getLocation().isRefuge())) return false;	
	    int fr = ((Refuge)getLocation()).getFieryness();
	    if(fr == 3 || fr == 6 || fr == 7) return false;
	    if(getWaterQuantity()+REFILL_QUANTITY>MAX_WATER_QUANTITY){			
	        setWaterQuantity(MAX_WATER_QUANTITY);				
	    }else{
	        setWaterQuantity(getWaterQuantity()+REFILL_QUANTITY);
	    }
	    return true;
	}
	
	public void encode(DataOutputStream dos){
		try {
			dos.writeInt(getType());
			dos.writeInt(getID());
			dos.writeInt(16); // Size of firebrigade data
			dos.writeInt(PROPERTY_WATER_QUANTITY);
			dos.writeInt(4); // Size of WATER_QUANTITY
			dos.writeInt(getWaterQuantity());
			dos.writeInt(PROPERTY_NULL);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
