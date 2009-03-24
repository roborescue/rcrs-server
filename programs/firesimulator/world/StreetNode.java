package firesimulator.world;


public class StreetNode extends Node {

	public StreetNode(int id) {
		super(id);
	}
	
	public void input(int property, int[] value) {
		super.input(property,value);
	}	
	
	public int getType(){
		return TYPE_NODE;
	}

}
