/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.2 $
 *
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

package rescuecore.view;

import java.awt.*;
import javax.swing.*;
import java.text.NumberFormat;
import rescuecore.Memory;

public class Display extends JPanel {
    private Map map;
    private ObjectInspector inspector;
    private JLabel timestep;
    private JLabel score;

    private final static NumberFormat FORMAT = NumberFormat.getInstance();

    static {
		FORMAT.setMaximumFractionDigits(4);
		FORMAT.setMinimumFractionDigits(0);
		FORMAT.setMaximumIntegerDigits(100);
		FORMAT.setMinimumIntegerDigits(1);
    }

    public static Display showDisplay(Memory m) {
		return showDisplay(Map.defaultMap(m));
    }

    public static Display showDisplay(Map map) {
		final JFrame frame = new JFrame();
		Display result = new Display(map);
		frame.setContentPane(result);
		frame.pack();
		SwingUtilities.invokeLater(new Runnable() {public void run() {
			frame.setVisible(true);
		}});
		return result;
    }

    public Display(Memory m) {
		this(Map.defaultMap(m));
    }

    public Display(Map map) {
		super(new BorderLayout());
		this.map = map;
		inspector = new ObjectInspector();
		timestep = new JLabel("300");
		score = new JLabel("100.0000");
		JPanel top = new JPanel(new FlowLayout());
		top.add(new JLabel("Time: "));
		top.add(timestep);
		top.add(new JLabel("Score: "));
		top.add(score);
		add(top,BorderLayout.NORTH);
		add(inspector,BorderLayout.EAST);
		add(map,BorderLayout.CENTER);
		ObjectSelector selector = new ObjectSelector();
		map.addMouseListener(selector);
		selector.addObjectSelectionListener(inspector);
    }

	public Map getMap() {
		return map;
	}

    public void setTimestep(int t) {
		timestep.setText(""+t);
    }

    public void setScore(double d) {
		score.setText(FORMAT.format(d));
    }
}
