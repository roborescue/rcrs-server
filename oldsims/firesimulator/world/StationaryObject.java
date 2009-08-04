package firesimulator.world;

public class StationaryObject extends RealObject {

	private int x;
	private int y;

	public StationaryObject(int id) {
		super(id);
	}
	
	public boolean isStationary(){
		return true;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	private void setX(int x){
		this.x=x;
	}
	
	private void setY(int y){
		this.y=y;
	}
	
	public void input(int property, int[] value) {
		switch(property) {
			case PROPERTY_X:
				setX(value[0]);		
				break;
			case PROPERTY_Y:
				setY(value[0]);
				break;
			default: 				
				super.input(property, value);
				break;
		}
	}

	
	public boolean isRefuge() {
		return false;
	}
}
