/*
 * Created on 21/05/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package rescuecore.debug;

import javax.swing.JComponent;

import rescuecore.Memory;
import rescuecore.view.Layer;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * @author Jono
 *
 */
public class StringHandler implements Handler {

	private int timeStep = -1;
	private JScrollPane pane;
	private JTextArea notes;
	
	/* (non-Javadoc)
	 * @see rescuecore.debug.Handler#getComponent()
	 */
	public JComponent getComponent() {
		if(pane == null){
			JPanel p = new JPanel(new BorderLayout());
			Border bord = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),"Notes");
			p.setBorder(bord);
			notes = new JTextArea(50,10);
			p.add(notes,BorderLayout.CENTER);
			pane = new JScrollPane(p);
			pane.setPreferredSize(new Dimension(DebugPane.HANDLER_WIDTH,200));
		}
		return pane;
	}

	/* (non-Javadoc)
	 * @see rescuecore.debug.Handler#getLayer()
	 */
	public Layer getLayer() {
		return null;
	}

	/* (non-Javadoc)
	 * @see rescuecore.debug.Handler#handle(java.lang.Object, int)
	 */
	public boolean handle(Object o, int timeStep) {
		if(this.timeStep != timeStep){
			notes.setText("");
			this.timeStep = timeStep;
		}
		if(!(o instanceof String))
			return false;
		notes.append(o.toString());
		notes.append("\n");
		return true;
	}

	/* (non-Javadoc)
	 * @see rescuecore.debug.Handler#setMemory(rescuecore.Memory)
	 */
	public void setMemory(Memory m) {}

}
