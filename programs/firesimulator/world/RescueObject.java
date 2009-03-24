package firesimulator.world;

import java.io.DataOutputStream;


/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public  class RescueObject implements WorldConstants {

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
		if(o==null)return false;
		return(id==o.hashCode());
	}
		
	public void input(int property, int[] value){
		//System.out.println("unkown property: "+property);
	}
	
	public int getType(){
			return TYPE_NULL;
	}
		
	public int getID(){
		return id;
	}
	
	public void encode(DataOutputStream dos){
	}
	
}
