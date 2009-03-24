package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AmbulanceCenter extends Building {

	/**
	 * @param id
	 */
	public AmbulanceCenter(int id) {
		super(id);
	}
	
	public int getType(){
		return TYPE_AMBULANCE_CENTER;
	}


	public boolean isInflameable(){
	    return Building.AMBULANCE_INFALMEABLE;
	}
	
	public int getFieryness(){
	    if(isInflameable())
	        return super.getFieryness();
	    return 0;
	}
}
