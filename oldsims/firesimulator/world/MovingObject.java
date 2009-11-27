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
    private World world = null;
    private String currentAction;
    private int currentActionLastChange;
	
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
        if (getLocation() == null)
            return -1;
        if (getLocation() instanceof Edge) {

            Edge e = (Edge) getLocation();
            Node head = (Node) world.getObject(e.getHeadID());
            Node tail = (Node) world.getObject(e.getTailID());
            int x = head.getX() + (tail.getX() - head.getX()) * positionExtra / e.length();
            return x;
        }
        else
            return getLocation().getX();
		
    }

    public int getY() {
        if (getLocation() == null)
            return -1;
        if (getLocation() instanceof Edge) {

            Edge e = (Edge) getLocation();
            Node head = (Node) world.getObject(e.getHeadID());
            Node tail = (Node) world.getObject(e.getTailID());
            int y = head.getY() + (tail.getY() - head.getY()) * positionExtra / e.length();
            return y;
        }
        else
            return getLocation().getY();
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
	
    public void input(String property, int[] value) {
        if ("STAMINA".equals(property)) {
            setStamina(value[0]);
        }
        else if ("HP".equals(property)) {
            setHp(value[0]);	        
        }
        else if ("DAMAGE".equals(property)) {
            setDamage(value[0]);      
        }
        else if ("BURIEDNSS".equals(property)) {
            setBuriedness(value[0]);  
        }
        else if ("POSITION".equals(property)) {
            setPositionId(value[0]);
        }
        else if ("POSITION_EXTRA".equals(property)) {
            setPositionExtra(value[0]);
        }
        else {
            super.input(property, value); 
        }
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
