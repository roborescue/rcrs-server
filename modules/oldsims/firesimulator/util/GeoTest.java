package firesimulator.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * @author tn
 *
 */
public class GeoTest extends JFrame implements MouseListener{

    private static final long serialVersionUID = 1L;
    float p[];
	float[] cross;
	int current=0;
	Point c;
	Point d;

	public static void main(String[] args) {
		GeoTest gt=new GeoTest();
		gt.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		gt.setVisible(true);
	}
	
	public GeoTest(){
		super("geotest");
		p=new float[]{50f,50f,300f,300f,100f,100f,200f,200f};
		addMouseListener(this);
		setSize(500,500);
		cross=null;
	}
	
	public void update(Graphics g){
		g.setColor(Color.BLACK);
		g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(Color.RED);
		g.drawLine((int)p[0],(int)p[1],(int)p[2],(int)p[3]);
		g.setColor(Color.GREEN);
		g.drawLine((int)p[4],(int)p[5],(int)p[6],(int)p[7]);		
		if(d!=null&&c!=null){
			g.setColor(Color.YELLOW);
			g.drawLine(c.x,c.y,d.x,d.y);
		}
		if(cross!=null){
			g.setColor(Color.CYAN);
			g.drawLine((int)cross[0]-10,(int)cross[1]-10,(int)cross[0]+10,(int)cross[1]+10);
			g.drawLine((int)cross[0]-10,(int)cross[1]+10,(int)cross[0]+10,(int)cross[1]-10);
		}
		if(c!=null){
			g.setColor(Color.YELLOW);
			g.drawOval(c.x-5,c.y-5,10,10);
		}
	}

	public void mouseClicked(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1){		
			if(current>7)current=0;
			p[current]=(float)e.getX();
			p[++current]=(float)e.getY();
			if(e.isControlDown()) p[current-1]=200f;
			if(e.isShiftDown()) p[current-1]=400f;
			current++;
		}
		findPoint();
		d=Geometry.getRndPoint(c,100d);
		findIntersect();
		update(getGraphics());
	}

	private void findPoint() {
		Point a=new Point((int)p[0],(int)p[1]);
		Point b=new Point((int)p[2],(int)p[3]);
		c=Geometry.getRndPoint(a,b);
	}

	private void findIntersect() {
		cross=Geometry.intersect(p);
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		
	}
	
	
	
	
	
}
