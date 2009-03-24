/*
 * Last change: $Date: 2004/06/10 01:17:51 $
 * $Revision: 1.11 $
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

package rescuecore.debug;

import rescuecore.*;
import rescuecore.objects.*;
import rescuecore.view.*;
import rescuecore.commands.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class UpdateHandler implements Handler {
	private DefaultListModel messages;
	private JScrollPane messagePane;

	public UpdateHandler(){
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createTitledBorder("Updates"));
		messages = new DefaultListModel();
		JList messageList = new JList(messages);
		messagePanel.add(messageList,BorderLayout.CENTER);
		messagePane = new JScrollPane(messagePanel);
		messagePane.setPreferredSize(new Dimension(DebugPane.HANDLER_WIDTH,80));
	}

	public void setMemory(Memory m){
	}


	public JComponent getComponent(){
		return messagePane;
	}

	public Layer getLayer(){
		return null;
	}

	public boolean handle(Object o, int timeStep){
		int time;
		RescueObject[] objects;
		if (o instanceof KASense) {
			time = ((KASense)o).getTime();
			objects = ((KASense)o).getUpdatedObjects();
		}
		else if (o instanceof Update) {
			time = ((Update)o).getTime();
			objects = ((Update)o).getUpdatedObjects();
		}
		else return false;
		messages.clear();
		for (RescueObject next : objects) {
			messages.addElement(next.toString());
			int[] known = next.getKnownPropertyTypes();
			for (int prop : known) {
				if (next.getLastPropertyUpdate(prop)==time) {
					messages.addElement("  "+next.getProperty(prop));
				}
			}
		}
		return true;
	}
}
