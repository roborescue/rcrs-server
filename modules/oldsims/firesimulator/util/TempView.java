package firesimulator.util;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * @author tn
 *
 */
public class TempView extends JFrame{

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    static float[][] keyColors={{1000,1,0,0},
												{300,1,1,0},
												{100,0,1,0},
												{50,0,0,1},
												{20,0,0,0.8f},
												{0,0,0,0}}; 

	public static void main(String[] args) {
		TempView gt=new TempView();
		gt.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		gt.setVisible(true);
	}
	
	public void update(Graphics g){
			g.setColor(Color.WHITE);
			g.fillRect(0,0,getWidth(),getHeight());
			for(int x=0;x<=200;x++){
				Color c=interpolator(x*5,keyColors);
				g.setColor(c);
				g.drawLine(50,250-x,100,250-x);
			}
			g.setColor(Color.BLACK);
			for(int x=0;x<=200;x+=40){
				g.drawLine(105,250-x,110,250-x);
				g.drawString(""+x*5+"�C",115,255-x);
			}
		}
		
	private Color interpolator(float temp,float[][]keys){
			float[][] keyColors=keys;
			int pos=0;
			do{
				pos++;
			}while(keyColors[pos][0]>temp&&pos<keyColors.length);
			if(pos>=keyColors.length)
				return Color.BLUE;
			float pc=(temp-keyColors[pos][0])/(keyColors[pos-1][0]-keyColors[pos][0]);
			float red=(keyColors[pos-1][1]-keyColors[pos][1])*pc+keyColors[pos][1];
			float green=(keyColors[pos-1][2]-keyColors[pos][2])*pc+keyColors[pos][2];
			float blue=(keyColors[pos-1][3]-keyColors[pos][3])*pc+keyColors[pos][3];
			return new Color(red,green,blue);
		}
		
}
