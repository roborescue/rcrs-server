package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class Node extends StationaryObject {

    int[] edgesID;

    public Node(int id) {
        super(id);		
    }
	
    public void setEdges(int[] value){
        edgesID=value;	
    }
}
