package firesimulator.util;

import java.awt.GridBagConstraints;

import javax.swing.JComponent;

import firesimulator.kernel.viewer.Setter;

/**
 * @author tn
 *
 */
public class ComponentContainer {
		
	JComponent comp;
	String prop;
	Setter setter;
		
	public ComponentContainer(JComponent comp,Object prop, Setter setter){
		this.comp=comp;
		this.prop=prop.toString();
		this.setter=setter;
	}
		
	public JComponent getComponent(){
		return comp;
	}
	
	public String getProperty(){
		return prop;
	}
	
	public Setter getSetter(){
		return setter;
	}
	
	public GridBagConstraints getConstrains(){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth=GridBagConstraints.REMAINDER;
		gbc.gridheight=1;
		return gbc;
	}

}
