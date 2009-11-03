package firesimulator.io;

/**
 * @author tn
 *
 */
public class LUDPMessage {
	
	int id;
	int remaining;
	byte[][] parts;
	
	public LUDPMessage(int id, int size){
		parts=new byte[size][];
		this.id=id;
		remaining=size;
	}
	
	public int getId(){
		return id;
	}
	
	public void store(byte[] part,int nth){
		parts[nth]=part;
		remaining--;
	}
	
	public boolean isComplete(){
		return remaining==0;
	}
	
	public byte[][] getParts(){
		return parts;
	}

}
