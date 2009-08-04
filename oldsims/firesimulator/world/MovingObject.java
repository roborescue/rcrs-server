package firesimulator.world;

/**
 * @author tn
 *
 */
public class MovingObject extends RealObject {

	private int stamina=0;
	private int hp=0;
	private int damage=0;
	private int buriedness=0;
	private int positionId=0;
	private int positionExtra=0;
	private RescueObject position;
	private World world = null;
    private int currentAction;
    private int currentActionLastChange;
	
	public MovingObject(int id) {
		super(id);
		currentAction = AK_REST;
		currentActionLastChange = 0;
	}
	
	public void setWorld(World w) {
		world = w; 
	}


	private void  setPositionId(int id){
		positionId=id;
		if (world != null) {
			position = world.getObject(positionId);
		}
	}
	
	public int getPositionId(){
		return positionId;
	}
	
	private void setPositionExtra(int pos){
		positionExtra=pos;
	}
	
	public int getPositionExtra(){
		return positionExtra;
	}
	
	private void setPosition(){
		
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
	
	private void setStamina(int stamina){
		this.stamina=stamina;
	}
	
	private void setHp(int hp){
		this.hp=hp;
	}
	
	private void setDamage(int damage){
		this.damage=damage;
	}
	
	private void setBuriedness(int buriedness){
		this.buriedness=buriedness;
	}
	
	public void input(int property, int[] value) {
		switch(property) {	  		
	  		case PROPERTY_STAMINA:
				setStamina(value[0]);
				break;
	  		case PROPERTY_HP:         
	  			setHp(value[0]);	        
	  			break;
	  		case PROPERTY_DAMAGE:     
	  			setDamage(value[0]);      
	  			break;
	  		case PROPERTY_BURIEDNESS: 
	  			setBuriedness(value[0]);  
	  			break;
	  		case PROPERTY_POSITION:
	  			setPositionId(value[0]);
	  			break;
			case PROPERTY_POSITION_EXTRA:
				setPositionExtra(value[0]);
				break;		
			default: 
				super.input(property, value); 
				break;
	  }
	}

    public int getCurrentAction() {
        if(world.getTime()>currentActionLastChange)
            return AK_REST;
        return currentAction;
    }

    public void setCurrentAction(int action) {
        currentActionLastChange = world.getTime();
        currentAction = action;
    }

}
