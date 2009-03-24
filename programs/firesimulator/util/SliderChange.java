package firesimulator.util;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author tn
 *
 */
public class SliderChange implements ChangeListener {
	
	String prop;	
	int type;
	float modifier;
	
	public SliderChange(String prop,int type,float modifier){
		this.prop=prop;
		this.type=type;
		this.modifier=modifier;
	}
	
	public void stateChanged(ChangeEvent e) {			
		int val=((JSlider)e.getSource()).getValue();
		switch(type){
			case ComponentFactory.FLOAT:
				Configuration.setProperty(prop,Float.toString(val/modifier),true);
				break;	
			default:
				Configuration.setProperty(prop,Integer.toString((int)(val/modifier)),true);
				break;
		}											
	}
}
