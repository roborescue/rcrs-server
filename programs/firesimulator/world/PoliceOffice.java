package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PoliceOffice extends Building {


	public PoliceOffice(int id) {
		super(id);
	}
	
	public int getType(){
		return TYPE_POLICE_OFFICE;
	}
	

	public boolean isInflameable(){
	    return Building.POLICE_INFALMEABLE;
	}
	
	public int getFieryness(){
	    if(isInflameable())
	        return super.getFieryness();
	    return 0;
	}
}
