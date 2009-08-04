package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Edge extends StationaryObject {

	int headID;
	int tailID;
	int Length;

	public Edge(int id) {
		super(id);
	}

	private void setHead(int id){
		headID=id;
	}
	
	private void setTail(int id){
		tailID=id;
	}
	
	private void setLength(int length){
		this.Length=length;
	}
	
	public void input(int property, int[] value) {
	 	switch(property) {
	  		case PROPERTY_HEAD: 
	  			setHead(value[0]);    
	  			break;
	  		case PROPERTY_TAIL:
	  			setTail(value[0]);    
	  			break;
	  		case PROPERTY_LENGTH: 
	  			setLength(value[0]);  
	  			break;
			default: 
				super.input(property, value);      
				break;
	  }
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
