/*
 *
 */
package firesimulator.util;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFormattedTextField;

/**
 *
 */
public class FormatedListener implements ActionListener {

	int min;
	int max;
	int type;
	String unit;
	String property;
	
	
	public FormatedListener(int min, int max, int type,String unit, String property){
		super();
		this.min=min;
		this.max=max;
		this.type=type;
		this.unit=unit;
		this.property=property;
	}

	public void actionPerformed(ActionEvent arg0) {
		JFormattedTextField source=(JFormattedTextField)arg0.getSource();
		String in=source.getText();		
		int w=in.indexOf(" "+unit);
		in=in.substring(0,w);
		try{
			switch(type){
				case ComponentFactory.FLOAT:
					float fl=Float.parseFloat(in);
					if(fl>max||fl<min)
						throw new Exception("");
					Configuration.setProperty(property,Float.toString(fl),true);
					break;
				case ComponentFactory.INT:
					int it=Integer.parseInt(in);
					if(it>max||it<min)
						throw new Exception("");
					Configuration.setProperty(property,Integer.toString(it),true);
					break;
			}
			if(type==ComponentFactory.FLOAT){
				
			}
		}catch(Exception e){
			Toolkit.getDefaultToolkit().beep();			
		}
	}

}
