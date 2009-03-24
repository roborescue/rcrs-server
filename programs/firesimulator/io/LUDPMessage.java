package firesimulator.io;

/**
 * @author tn
 *
 */
public class LUDPMessage {
	
	int id;
	int remaining;
	int[][] parts;
	
	public LUDPMessage(int id, int size){
		parts=new int[size][];
		this.id=id;
		remaining=size;
	}
	
	public int getId(){
		return id;
	}
	
	public void store(int[] part,int nth){
		parts[nth]=part;
		remaining--;
	}
	
	public boolean isComplete(){
		return remaining==0;
	}
	
	public int[][] getParts(){
		return parts;
	}

}
