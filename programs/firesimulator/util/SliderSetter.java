package firesimulator.util;

import javax.swing.JSlider;

import firesimulator.kernel.viewer.Setter;

/**
 * @author tn
 *
 */
public class SliderSetter implements Setter {

	String prop;
	float modifier;
	int type;
	JSlider target;

	public SliderSetter(String prop,float modifier,int type, JSlider target){
		this.prop=prop;
		this.modifier=modifier;
		this.type=type;
		this.target=target;
	}

	public void setControll() {
		switch(type){
			case ComponentFactory.FLOAT:
				target.setValue((int)(new Float(Configuration.getValue(prop)).floatValue()*modifier));	
				break;
			default:
				target.setValue((int)(new Integer(Configuration.getValue(prop)).intValue()*modifier));
				break;	
		}
	}

}
