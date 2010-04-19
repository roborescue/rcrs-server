package firesimulator.world;

import rescuecore.OutputBuffer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class RescueObject implements WorldConstants {
    private static final Log LOG = LogFactory.getLog(RescueObject.class);

    int id;
	
    public RescueObject(int id){
        this.id=id;
    }
	
    public boolean isStationary(){
        return false;
    }
		
    public int hashCode(){
        return id;
    }
	
    public boolean equals( Object o ) {
        if (!(o instanceof RescueObject)) {
            return false;
        }
        return this.id == ((RescueObject)o).id;
    }
		
    public abstract String getType();
		
    public int getID(){
        return id;
    }
}
