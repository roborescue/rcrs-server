package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class Edge extends StationaryObject {

	int headID;
	int tailID;
	int Length;

	public Edge(int id) {
		super(id);
	}

	public void setHead(int id){
		headID=id;
	}
	
	public void setTail(int id){
		tailID=id;
	}
	
	public void setLength(int length){
		this.Length=length;
	}

	protected int getTailID() {	
		return tailID;
	}

	protected int getHeadID() {
		return headID;
	}
	
	protected int length() {
		return Length;
	}

}
