package firesimulator.kernel.viewer;

import java.awt.Color;

import firesimulator.world.Building;
import firesimulator.world.World;


/**
 * @author tn
 */
public class TempColorizer implements AirColorizer,BuildingColorizer {

	static float[][] keyColors={{1000,1,0,0},
									{300,1,1,0},
									{100,0,1,0},
									{50,0,0,1},
									{20,0,0,0},
									{0,0,0,0}}; 
									
	static float[][] buildingColors={{1000,1,0,0},
										{300,1,1,0},
										{100,0,1,0},
										{50,0,0,1},
										{20,0,0,0.8f},
										{0,0,0,0}}; 									

	public Color getColor(World world, int x, int y) {		
		return interpolator(((float)world.getAirTemp()[x][y]),keyColors);
	}
	
	private Color interpolator(double temp,float[][]keys){
	    if(!(temp<Double.MAX_VALUE&&temp>Double.MIN_VALUE)){
	        return Color.CYAN;
	    }
		float[][] keyColors=keys;
		int pos=0;
		do{
			pos++;
		}while(pos<keyColors.length&&keyColors[pos][0]>temp);
		if(pos>=keyColors.length)
			return Color.BLUE;
		float pc=(float)(temp-keyColors[pos][0])/(keyColors[pos-1][0]-keyColors[pos][0]);
		float red=(keyColors[pos-1][1]-keyColors[pos][1])*pc+keyColors[pos][1];
		float green=(keyColors[pos-1][2]-keyColors[pos][2])*pc+keyColors[pos][2];
		float blue=(keyColors[pos-1][3]-keyColors[pos][3])*pc+keyColors[pos][3];
		if(green>1f||green<0f)
			green=1.0f;
		if(red>1f||red<0f)
			red=1.0f;
		if(blue>1f||blue<0f)
			blue=1.0f;
		Color c=null;
		try{
			c=new Color(red,green,blue);
		}catch (Exception e) {
			System.exit(1);
		}
		return c;
	}
	
	
	public String toString(){
		return "temperature";
	}


	public Color getColor(Building bl) {
	    double temp=bl.getTemperature();
		return interpolator(temp,buildingColors);
	}

}
