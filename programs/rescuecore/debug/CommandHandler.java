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

public class CommandHandler implements Handler {

	private Layer commandLayer;
	private DefaultListModel messages;
	private JScrollPane messagePane;

	private int timeStep = -1;

	private Memory memory;

	public CommandHandler(){
		JPanel messagePanel = new JPanel(new BorderLayout());
		Border bord = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),"Commands");
		messagePanel.setBorder(bord);
		messages = new DefaultListModel();
		JList messageList = new JList(messages);
		messagePanel.add(messageList,BorderLayout.CENTER);
		messagePane = new JScrollPane(messagePanel);
		messagePane.setPreferredSize(new Dimension(DebugPane.HANDLER_WIDTH,80));
	}

	public void setMemory(Memory m){
		memory = m;
	}


	public JComponent getComponent(){
		return messagePane;
	}

	public Layer getLayer(){
		if(commandLayer == null){
			commandLayer = new Layer("Move Commands");
			commandLayer.addRenderer(RescueObject[].class,OutlineRenderer.ORANGE);
			commandLayer.addRenderer(Humanoid.class,HumanoidRenderer.outlinedHumanoidRenderer(ViewConstants.FILL_MODE_SOLID, new Color(255,255,255,96)));
			commandLayer.addRenderer(ExtinguishCommand.class, new ExtinguishCommandRenderer());

		}
		return commandLayer;
	}

	public boolean handle(Object o, int timeStep){
		if(this.timeStep != timeStep){
			commandLayer.removeAllObjects();
			messages.clear();
			this.timeStep = timeStep;
		}
		if(!(o instanceof Command))
			return false;
		Command c = (Command)o;
		if(c == null)
			return true;
		int id;
		switch(c.getType()){
		case RescueConstants.AK_MOVE:
			int[] path = ((AKMove)c).getPath();
			RescueObject[] rs = new RescueObject[path.length];
			rs[0] = memory.lookup(path[0]);
			RescueObject last = null;
			for (int i=0;i<rs.length;++i){
				rs[i] = memory.lookup(path[i]);
				if(rs[i] == null)
					messages.addElement("Bad ID in path. Index "+i+". ID "+path[i]);
				else if(rs[i].isNode()){
					if(last != null)
						commandLayer.addObject(new RescueObject[]{last,rs[i]});
					last = rs[i];
				}
			}
			break;
		case RescueConstants.AK_SAY:
			messages.addElement("Say of "+((AKSay)c).getMessage().length+" bytes.");
			break;
		case RescueConstants.AK_TELL:
			messages.addElement("Tell of "+((AKTell)c).getMessage().length+" bytes.");
			break;
		case RescueConstants.AK_EXTINGUISH:
			AKExtinguish ex = (AKExtinguish)c;
			id = ex.getSender();
			Nozzle[] nozzles = ex.getNozzles();
			for (int i=0;i<nozzles.length;++i) {
				int targetID = nozzles[i].getTarget();
				messages.addElement("Extinguishing building "+targetID);
				try {
					commandLayer.addObject(new ExtinguishCommand((Building)memory.lookup(targetID),(Humanoid)memory.lookup(id),memory));
				}
				catch (CannotFindLocationException e) {
					e.printStackTrace();
				}
			}
			break;
		case RescueConstants.AK_CLEAR:
			id = ((AKClear)c).getSender();
			commandLayer.addObject(memory.lookup(id));
			messages.addElement("Clearing road.");
			break;
		case RescueConstants.AK_RESCUE:
			id = ((AKRescue)c).getSender();
			commandLayer.addObject(memory.lookup(id));
			messages.addElement("Rescuing.");
			break;
		case RescueConstants.AK_LOAD:
			id = ((AKLoad)c).getSender();
			commandLayer.addObject(memory.lookup(id));
			messages.addElement("Loading.");
			break;
		case RescueConstants.AK_UNLOAD:
			id = ((AKUnload)c).getSender();
			commandLayer.addObject(memory.lookup(id));
			messages.addElement("Unloading.");
			break;
		}
		return true;
	}
}
