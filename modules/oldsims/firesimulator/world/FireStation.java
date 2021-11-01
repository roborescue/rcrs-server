package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FireStation extends Building {

	public FireStation(int id) {
		super(id);
	}
	
	public String getType(){
		return "FIRE_STATION";
	}
	
	public boolean isInflameable(){
	    return Building.FIRE_INFALMEABLE;
	}
	
	public int getFieryness(){
	    if(isInflameable())
	        return super.getFieryness();
	    return 0;
	}
	
}
