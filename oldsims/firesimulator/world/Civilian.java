package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Civilian extends MovingObject {


	public Civilian(int id) {
		super(id);
	}
	
	public String getType(){
		return "CIVILIAN";
	}

}
