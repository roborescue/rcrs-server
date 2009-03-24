package firesimulator.kernel.viewer.extinguishManager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import firesimulator.simulator.ExtinguishRequest;

import firesimulator.world.Building;
import firesimulator.world.World;

/**
 * @author tn
 *
 */
public class ExtinguishJob extends JPanel implements MouseListener{
	
	/**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;
    int quantity;
	int start;
	int end;
	Building target;
	World world;
	ExtinguishManager parent;
	
	public ExtinguishJob(int q, int s, int e, Building t,World w,ExtinguishManager parent){
		super();
		quantity=q;
		start=s;
		end=e;
		target=t;
		world=w;
		this.parent=parent;
		addMouseListener(this);
	}
	
	public void execute(){
		if(start<=world.getTime()&&world.getTime()<=end){
			world.addExtinguishRequest(new ExtinguishRequest(null,target,quantity));
		}
	}		
 
	
	public void paint(Graphics gs){
		super.paint(gs);
		Graphics2D g=(Graphics2D)gs;
		//System.out.println("my size :"+getSize());		
		//g.setColor(Color.YELLOW);
		//g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(Color.BLUE);		
		g.drawRoundRect(5,5,getWidth()-10,44-10,10,10);
		g.setColor(Color.BLACK);
		g.drawString("Building "+target.getID(),10f,20f);
		g.drawString("from"+start+" to "+end+" using "+quantity+"l",10f,35f);
	}


	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			parent.modifyJob(this);
		else
			parent.removeJob(this);
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

}
