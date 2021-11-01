package firesimulator.simulator;

import firesimulator.util.Configuration;
import firesimulator.world.Building;
import firesimulator.world.FireBrigade;

import org.apache.log4j.Logger;

/**
 * @author tn
 *
 */
public class ExtinguishRequest {
    private static final Logger LOG = Logger.getLogger(ExtinguishRequest.class);
		
    public static final int REASON_OK=1;
    public static final int REASON_OK_VIRTUAL=2;
    public static final int REASON_FB_WAS_NULL=-1;
    public static final int REASON_TO_MUCH_WATER=-2;
    public static final int REASON_TANK_EMPTY=-3;
    public static final int REASON_OUT_OF_RANGE=-4;
    public static final int REASON_NEGATIVE_WATER=-5;//Add by Ali Modaresi To fix using kernel bug
    
    public static final String OK="passed all tests";
    public static final String OK_VIRT="is virtual mode";
    public static final String ER_FB_NULL="firebrigade is null";
    public static final String ER_TO_MUCH="firebrigade is exceeding limt";
    public static final String ER_EMPTY="tank is empty";
    public static final String ER_RANGE="target is out of range";
    public static final String ER_NEGATIVE_WATER="negative water request";//Add by Ali Modaresi To fix using kernel bug
    public static final String UNKNOWN="unknown code ";
    
	public static int MAX_WATER_PER_CYCLE;
	public static int MAX_DISTANCE;
	private FireBrigade source;
	private Building target;
	private int quantity;
	private static boolean DEBUG_VERBOSE=true;
	
	public ExtinguishRequest(FireBrigade source, Building target,int quantity){
		this.target=target;
		this.source=source;
		this.quantity=quantity;
	}
	
	public void verbose(String msg){
	    if(DEBUG_VERBOSE)
                LOG.debug(msg);
	}
	
	public int validate(){
	    if(source==null && Configuration.isActive("virtual"))return REASON_OK_VIRTUAL;
		if(source==null)return REASON_FB_WAS_NULL;	
		if(source.getWaterUsed()+quantity>MAX_WATER_PER_CYCLE){						
			return REASON_TO_MUCH_WATER;
		}
		if(source.getWaterQuantity()<quantity){			
			return REASON_TANK_EMPTY;
		}
		if(distance(source,target)>MAX_DISTANCE)
			return REASON_OUT_OF_RANGE;
		
		if(quantity<0){//Added by Ali Modaresi to fix using kernel bug
			LOG.warn("Using kernel bug.... Extinguish with negative water");//Added by Ali Modaresi to fix using kernel bug
			return REASON_NEGATIVE_WATER;//Added by Ali Modaresi to fix using kernel bug
		}
		return REASON_OK;		
	}
	
	public String getReason(int code){
	    switch (code) {
        case REASON_OK:
            return OK;
        case REASON_OK_VIRTUAL:
        		return OK_VIRT;
        case REASON_FB_WAS_NULL:
        		return ER_FB_NULL;
        case REASON_OUT_OF_RANGE:
        		return ER_RANGE;
        	case REASON_TANK_EMPTY:
        		return ER_EMPTY;
        	case REASON_TO_MUCH_WATER:
        		return ER_TO_MUCH;
        	case REASON_NEGATIVE_WATER://Added by Ali Modaresi to fix using kernel bug
        		return ER_NEGATIVE_WATER;//Added by Ali Modaresi to fix using kernel bug
        default:
            return UNKNOWN+code;
        }
	}
	

	private double distance(FireBrigade source2, Building target2) {
		double x=source2.getX()-target2.getX();
		double y=source2.getY()-target2.getY();
		return Math.sqrt((x*x)+(y*y));
	}

	public boolean execute(){
	    verbose(toString());
	    int result;
		if((result=validate())<0){
		    verbose("ERROR reason = "+getReason(result)+"\n");
		    return false;
		}
		if(source!=null){
			source.addWaterUsed(quantity);
			source.setWaterQuantity(source.getWaterQuantity()-quantity);		
		}
		target.setWaterQuantity(target.getWaterQuantity()+quantity);
		verbose("OK reason = "+getReason(result)+"\n");
		return true;
	}

	public FireBrigade getSource() {
		return source;
	}
	
	public String toString(){
	    String fb;
	    try{
	        fb="fb="+source.getID();
	    }catch (Exception e) {
            fb="fb=null";
        }
		return "extinguish request; "+fb+", target="+target.getID()+", quantity="+quantity+" -> ";
	}

}
