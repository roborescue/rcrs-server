package firesimulator.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.text.MaskFormatter;

import firesimulator.kernel.viewer.Setter;

/**
 * @author tn
 *
 */
public class ComponentFactory {
	
	public static final int FLOAT=1;
	public static final int INT=2;

	public static ComponentContainer createSlider(int min,int max,float modifier, String name,String property,int type){
		JSlider slider=new JSlider(min,max);
		slider.setMajorTickSpacing((max-min)/4);
		slider.setMinorTickSpacing((max-min)/30);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		JPanel dummy=new JPanel();
		dummy.setBorder(BorderFactory.createTitledBorder(dummy.getBorder(),name));
		dummy.add(slider);
		slider.addChangeListener(new SliderChange(property,type,modifier));
		Setter s=new SliderSetter(property,modifier,type,slider);			
		ComponentContainer cc=new ComponentContainer(dummy,property,s);				
		return cc;
	}
	
	public static ComponentContainer createField(int min, int max, int type,int dec, String name, String unit, String property){
		String vrt="value range: ["+min+","+max+"]";
		JPanel panel=new JPanel(new GridBagLayout());
		GridBagConstraints gbc=new GridBagConstraints();		
		panel.setToolTipText(vrt);
		JLabel lab=new JLabel(name);
		gbc.weightx=2;
		gbc.insets=new Insets(2,5,2,5);
		gbc.anchor=GridBagConstraints.WEST;
		panel.add(lab,gbc);
		String str=""+max;
		int l=str.length();
		str="";
		for(int i=0;i<l;i++)
			str+="#";
		if(type==FLOAT){
			str+=".";
			for(int i=0;i<dec;i++)
				str+="#";			
		}
		str+=" "+unit;
		JFormattedTextField tf=new JFormattedTextField(getMaskFormater(str));	
		tf.addActionListener(new FormatedListener(min,max,type,unit,property));
		tf.setToolTipText("value range: ["+min+","+max+"]");
		gbc.weightx=1;
		gbc.anchor=GridBagConstraints.EAST;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		panel.add(tf,gbc);
		ComponentContainer cc=new ComponentContainer(panel,property,new FormatSetter(tf,property,unit,type,dec,max));
		return cc;
	}

	public static MaskFormatter getMaskFormater(String s) {
		MaskFormatter formatter = null;
	    try {
	        formatter = new MaskFormatter(s);
	    } catch (java.text.ParseException exc) {
	        System.err.println("formatter is bad: " + exc.getMessage());
	        System.exit(-1);
	    }
	    return formatter;
	}

}
