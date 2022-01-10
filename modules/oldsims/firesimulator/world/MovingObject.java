package firesimulator.world;

/**
 * @author tn
 *
 */
public abstract class MovingObject extends RealObject {

    private int stamina=0;
    private int hp=0;
    private int damage=0;
    private int buriedness=0;
    private int positionId=0;
    private int positionExtra=0;
    private RescueObject position;
    protected World world = null; 
    private String currentAction;
    private int currentActionLastChange;
    private int x;
    private int y;
	
    public MovingObject(int id) {
        super(id);
        currentAction = "AK_REST";
        currentActionLastChange = 0;
    }
	
    public void setWorld(World w) {
        world = w; 
    }


    public void setPositionId(int id){
        positionId=id;
        if (world != null) {
            position = world.getObject(positionId);
        }
    }
	
    public int getPositionId(){
        return positionId;
    }
	
    public void setPositionExtra(int pos){
        positionExtra=pos;
    }
	
    public int getPositionExtra(){
        return positionExtra;
    }
	
    public void setPosition(){
		
    }

    public StationaryObject getLocation(){
        if (position instanceof MovingObject)
            return ((MovingObject) position).getLocation();
        else if (position instanceof StationaryObject)
            return (StationaryObject) position;
        else 
            return null;
    }
	
    public int getLocationID(){
        if (position instanceof MovingObject)
            return ((MovingObject) position).getLocationID();
        else if (position instanceof StationaryObject)
            return position.id;
        else 
            return -1;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }
	
    public void setY(int y) {
        this.y = y;
    }
	
    public void setStamina(int stamina){
        this.stamina=stamina;
    }
	
    public void setHp(int hp){
        this.hp=hp;
    }
	
    public void setDamage(int damage){
        this.damage=damage;
    }
	
    public void setBuriedness(int buriedness){
        this.buriedness=buriedness;
    }
	
    public String getCurrentAction() {
        if(world.getTime()>currentActionLastChange)
            return "AK_REST";
        return currentAction;
    }

    public void setCurrentAction(String action) {
        currentActionLastChange = world.getTime();
        currentAction = action;
    }

}
