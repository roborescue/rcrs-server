/*
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore.tools;

import rescuecore.*;
import rescuecore.view.*;
import rescuecore.log.*;
import rescuecore.objects.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.Collection;

public class LogViewer extends JPanel {
	private ObjectInspector inspector;
	private Map map;
	private Log log;
	private int time;
	
	public static void main(String[] args) {
		if (args.length==0) {
			System.err.println("Usage: LogViewer <filename>");
			return;
		}
		try {
			Log log = Log.generateLog(args[0]);
			JFrame frame = new JFrame("Log viewer");
			frame.setContentPane(new LogViewer(log));
			frame.addWindowListener(new WindowAdapter(){
					public void windowClosing(WindowEvent e){System.exit(0);}
				});
			frame.pack();
			Toolkit t = Toolkit.getDefaultToolkit();
			Dimension d = t.getScreenSize();
			//			frame.setSize(200,200);
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LogViewer(Log log) {
		super(new BorderLayout());
		this.log = log;
		time = 0;
		map = Map.defaultMap(log.getMemory(0));
		final JSlider slider = new JSlider(0,log.getMaxTimestep());
		add(map,BorderLayout.CENTER);
		JPanel timePanel = new JPanel(new BorderLayout());
		Border b = BorderFactory.createLineBorder(Color.BLACK);
		b = BorderFactory.createTitledBorder(b,"Time Step");
		timePanel.setBorder(b);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(10);
		JButton left = new JButton(" < ");
		JButton right = new JButton(" > ");
		left.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){slider.setValue(slider.getValue()-1);}
			});
		right.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){slider.setValue(slider.getValue()+1);}
			});
		slider.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e){if (!slider.getValueIsAdjusting()) setTimeStep(slider.getValue());}
			});
		timePanel.add(slider,BorderLayout.CENTER);
		timePanel.add(left,BorderLayout.WEST);
		timePanel.add(right,BorderLayout.EAST);
		add(timePanel,BorderLayout.SOUTH);
		inspector = new ObjectInspector();
		add(inspector,BorderLayout.EAST);
		ObjectSelector selector = new ObjectSelector(map);
		selector.addObjectSelectionListener(inspector);
		slider.setValue(0);
	}

	private void setTimeStep(int t) {
		if (t < 0 || t > log.getMaxTimestep()) return;
		time = t;
		Memory m = log.getMemory(time);
		map.setMemory(m);
		map.repaint();
		Collection<RescueObject> all = m.getAllObjects();
		for (RescueObject next : all) {
			if (next instanceof Road) {
				Road r = (Road)next;
				if (r.getBlock() > r.getWidth()) System.out.println("Overblocked");
				if (r.getLinesToHead() != r.getLinesToTail()) System.out.println("Asymmetric road");
			}
		}
	}

	private void moveTime(int delta) {
		setTimeStep(time+delta);
	}
}
