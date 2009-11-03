package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Refuge extends Building {
    
	public Refuge(int id) {
		super(id);
	}
	
	public String getType(){
		return "REFUGE";
	}

	public boolean isRefuge() {
		return true;
	}
	

	public boolean isInflameable(){
	    return Building.REFUGE_INFALMEABLE;
	}
	
	public int getFieryness(){
	    if(isInflameable())
	        return super.getFieryness();
	    return 0;
	}

}
