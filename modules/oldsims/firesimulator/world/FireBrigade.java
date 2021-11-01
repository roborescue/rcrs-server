package firesimulator.world;

import java.io.DataOutputStream;
import java.io.IOException;

import rescuecore.OutputBuffer;

public class FireBrigade extends MovingObject {

	public static int REFILL_QUANTITY;
	public static int REFILL_HYDRANT_QUANTITY;
	public static int MAX_WATER_QUANTITY;
	private int initialWaterQuantity;
	private int waterQuantity;
	private int waterUsed;
	private boolean changed;

	public FireBrigade(int id) {
		super(id);
		initialWaterQuantity = 0;
		waterQuantity = 0;
		waterUsed = 0;
		changed = false;
	}

	public String getType() {
		return "FIRE_BRIGADE";
	}

	public void setInitialWaterQuantity(int quantity) {
		initialWaterQuantity = quantity;
		waterQuantity = quantity;
	}

	public int getWaterQuantity() {
		return waterQuantity;
	}

	public int getWaterUsed() {
		return waterUsed;
	}

	public void setWaterQuantity(int quantity) {
		waterQuantity = quantity;
		changed = true;
	}

	public void addWaterUsed(int quantity) {
		waterUsed += quantity;
	}

	public void nextCycle() {
		waterUsed = 0;
		changed = false;
	}

	public void reset() {
		waterQuantity = initialWaterQuantity;
		waterUsed = 0;
		changed = false;
	}

	public boolean hasChanged() {
		return changed;
	}
	public boolean refillInHydrant() {
		//System.out.println(getID()+" Location is:"+getLocation()+" "+getLocation().getType());
		if(!(getLocation().getType().equals("HYDRANT")))
			return false;

		for (Object next : world.getFirebrigades()) {
			FireBrigade firebrigade = (FireBrigade)next;
			if(firebrigade.getLocationID()==this.getLocationID()&&
					firebrigade.getID()<this.getID())
					return false;
		}
		
		if (getWaterQuantity() + REFILL_HYDRANT_QUANTITY> MAX_WATER_QUANTITY) 
			setWaterQuantity(MAX_WATER_QUANTITY);
		else
			setWaterQuantity(getWaterQuantity() + REFILL_HYDRANT_QUANTITY);
		return true;
	}
	public boolean refillInRefuge() {
		if (!(getLocation().isRefuge()))
			return false;
		
		int fr = ((Refuge) getLocation()).getFieryness();
		if (fr == 3 || fr == 6 || fr == 7)
			return false;
		
		
		if (getWaterQuantity() + REFILL_QUANTITY > MAX_WATER_QUANTITY) {
			setWaterQuantity(MAX_WATER_QUANTITY);
		} else {
			setWaterQuantity(getWaterQuantity() + REFILL_QUANTITY);
		}
		return true;
	}
	public boolean refill() {
//		if (!getCurrentAction().equals("AK_REST")){
//			return false;
//		}unusable because the old simulator don't know the last action
		
		if (getLocation() == null)
			return false;
		
		if(refillInHydrant())
			return true;
		
		if(refillInRefuge())
			return true;
		
		return false;
	}
}
