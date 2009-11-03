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
	
	private void setX(int x){
		this.x=x;
	}
	
	private void setY(int y){
		this.y=y;
	}
	
	public void input(String property, int[] value) {
            if ("X".equals(property)) {
                setX(value[0]);		
            }
            else if ("Y".equals(property)) {
                setY(value[0]);
            }
            else {
                super.input(property, value);
            }
	}

	
	public boolean isRefuge() {
		return false;
	}
}
