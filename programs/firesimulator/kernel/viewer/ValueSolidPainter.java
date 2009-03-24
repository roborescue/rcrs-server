package firesimulator.kernel.viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import firesimulator.world.World;

public class ValueSolidPainter extends SolidPainter {

    public void paint(Graphics2D g, World w, AirColorizer col) {        
		double[][]air=w.getAirTemp();				
		int oX=w.getMinX();
		int oY=w.getMinY();		
		g.setColor(Color.GREEN);
		Font font=g.getFont();		
		font=font.deriveFont(2000f);
		g.setFont(font);
		for(int x=0;x<air.length;x++)
			for(int y=0;y<air[x].length;y++){
			    g.drawString(""+air[x][y], oX+x*w.SAMPLE_SIZE, oY+y*w.SAMPLE_SIZE);				
			}		
	}
    
    public String toString(){
        return "value";
    }
    
}
