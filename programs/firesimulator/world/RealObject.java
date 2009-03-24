package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class RealObject extends RescueObject{


	public RealObject(int id) {
		super(id);
	}

	abstract public int getX();
	
	abstract public int getY();

}
