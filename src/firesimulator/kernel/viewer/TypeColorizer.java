package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.AmbulanceCenter;
import firesimulator.world.Building;
import firesimulator.world.FireStation;
import firesimulator.world.PoliceOffice;
import firesimulator.world.Refuge;

/**
 * @author tn
 */
public class TypeColorizer implements BuildingColorizer {

    public static Color BUILDING = Color.LIGHT_GRAY;
    public static Color REFUGE = Color.GREEN;
    public static Color FIRE = Color.RED;
    public static Color POLICE = Color.BLUE;
    public static Color AMBULANCE= Color.YELLOW;
    public static Color UNKNOWN= Color.PINK;
    
    public Color getColor(Building b) {        
        if(b instanceof Refuge)
            return REFUGE;
        if(b instanceof PoliceOffice)
            return POLICE;
        if(b instanceof FireStation)
            return FIRE;
        if(b instanceof AmbulanceCenter)
            return AMBULANCE;
        if(b instanceof Building)
            return BUILDING;
        return UNKNOWN;
    }
    
    public String toString(){
        return "type";
    }

}
