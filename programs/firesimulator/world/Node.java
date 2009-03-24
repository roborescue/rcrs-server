package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Node extends StationaryObject {

	int[] edgesID;

	public Node(int id) {
		super(id);		
	}
	
	private void setEdges(int[] value){
		edgesID=value;	
	}
	
	public void input(int property, int[] value) {
		switch(property) {
			default: 
				super.input(property, value); 
				break;
			case PROPERTY_EDGES: 
				setEdges(value);  
				break;
	  	}
	}
}
