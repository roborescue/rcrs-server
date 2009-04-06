package firesimulator.util;

import javax.swing.JFormattedTextField;

import firesimulator.kernel.viewer.Setter;

/**
 * @author tn
 *
 */
public class FormatSetter implements Setter {

	JFormattedTextField client;	
	String property;
	String unit;
	int type;
	int dec;
	int max;
	
	public FormatSetter(JFormattedTextField client,String property,String unit,int type,int dec,int max){
		this.client=client;		
		this.property=property;
		this.unit=unit;
		this.type=type;
		this.dec=dec;
		this.max=max;		
	}

	public void setControll() {
		try{
			String left=new String();
			String right=new String();			
			String value=Configuration.getValue(property);
//            if(property.equalsIgnoreCase(""))
			if(type==ComponentFactory.FLOAT){			
				int dot=value.indexOf(".");			
				if(dot>0){
					right=value.substring(dot+1);
					left=value.substring(0,dot);				
					if(right.length()>dec)
						right=right.substring(0,dec);				
				}else{
					right="";				
				}				
				int missing=dec-right.length();
				for(int c=0;c<missing;c++)
					right+="0";									
			}				
			if(type==ComponentFactory.INT){
				left=value;			
			}
			int l=new Integer(max).toString().length();
			if(left.length()<l){
				int missing=l-left.length();
				for(int c=0;c<missing;c++)
					left="0"+left;
			}
			if(type==ComponentFactory.FLOAT)
				left+=".";
			value=left+right+" "+unit;			
			client.setText(value);
		}catch(Exception e){
			System.out.println("setter error at property "+property+" (type="+type+")");
		}
	}

}
