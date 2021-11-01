package firesimulator.world;

import org.apache.log4j.Logger;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class RescueObject implements WorldConstants {
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
