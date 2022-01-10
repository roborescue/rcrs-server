package firesimulator.world;

public abstract class StationaryObject extends RealObject {

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
	
	public void setX(int x){
		this.x=x;
	}
	
	public void setY(int y){
		this.y=y;
	}
	
	public boolean isRefuge() {
		return false;
	}
}
