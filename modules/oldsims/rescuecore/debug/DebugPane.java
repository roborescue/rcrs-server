/*
 * Last change: $Date: 2005/03/17 06:07:12 $
 * $Revision: 1.10 $
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
import rescuecore.view.*;
import rescuecore.objects.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DebugPane extends JPanel{

	private DebugLog log;
	private String name;
	private Memory memory;
	protected JTabbedPane tabs;
	private int timeStep;
	private rescuecore.view.Map map;
	private Layer moveHistory;
	private ArrayList<Handler> handlers;
	private JPanel handlerPanel;
	public static final int HANDLER_WIDTH = 200;
	int height = 0;

	public DebugPane(DebugLog log, String name){
		super(new BorderLayout());
		this.log = log;
		this.name = name;
		ObjectInspector inspector = new ObjectInspector();
		memory = new HashMemory();
		setName(name);
		map = new rescuecore.view.Map(memory);
		ObjectSelector selector = new ObjectSelector(map);
		map.addMouseListener(selector);
		selector.addObjectSelectionListener(inspector);
		add(map,BorderLayout.CENTER);
		map.addLayer(Layer.createBuildingLayer(memory));
		map.addLayer(Layer.createRoadLayer(memory));
		map.addLayer(Layer.createNodeLayer(memory));
		map.addLayer(Layer.createHumanoidLayer(memory));
		moveHistory = new Layer("Movement History");
		moveHistory.addRenderer(RescueObject[].class,OutlineRenderer.YELLOW);
		map.addLayer(moveHistory);

		handlerPanel = new JPanel();
		handlerPanel.setLayout(new BoxLayout(handlerPanel,BoxLayout.Y_AXIS));
		JScrollPane sp = new JScrollPane(inspector,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setPreferredSize(new Dimension(HANDLER_WIDTH,200));

		handlerPanel.add(sp);
		add(new JScrollPane(handlerPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),BorderLayout.EAST);

		handlers = new ArrayList<Handler>(10);
	}

	public void registerHandler(Handler h){
		h.setMemory(memory);
		handlers.add(h);
		JComponent comp = h.getComponent();
		if(comp != null){
			handlerPanel.add(comp);
			height += (int)comp.getPreferredSize().getHeight();
			handlerPanel.setPreferredSize(new Dimension(HANDLER_WIDTH+10,height));
		}
		Layer lay = h.getLayer();
		if(lay != null)
			map.addLayer(lay);
	}

	protected void moveToTimeStep(int timeStep){
		memory = new HashMemory();
		for (int i=0;i<=timeStep;++i) {
			Collection<DebugEntry> entries = log.getEntriesForTime(name,i);
			for (DebugEntry next : entries) {
				processEntry(next);
			}
		}
		map.setMemory(memory);
		for (Handler next : handlers) next.setMemory(memory);
		repaint();

		/*
		if(timeStep < this.timeStep)
			for(int i = this.timeStep; i > timeStep; i--)
				log.invertAll(i,memory);
		else if(timeStep > this.timeStep)
			for(int i = this.timeStep+1; i <= timeStep; i++)
				log.applyAll(i,memory);
		else{
			repaint();
			return;
		}
		this.timeStep = timeStep;
		setHistory();
		repaint();
		*/
	}

	private void processEntry(DebugEntry entry) {
		// FIXME: Nasty nasty nasty! This sort of thing should really be in class DebugEntry.
		if (entry instanceof DebugEntry.RescueObjectEntry) {
			RescueObject object = ((DebugEntry.RescueObjectEntry)entry).getObject();
			memory.add(object.copy(),entry.getTimestep());
		}
		else if (entry instanceof DebugEntry.RescueObjectCollectionEntry) {
			Collection<RescueObject> objects = ((DebugEntry.RescueObjectCollectionEntry)entry).getObjects();
			for (RescueObject next : objects) {
				memory.add(next.copy(),entry.getTimestep());
			}
		}
		else if (entry instanceof DebugEntry.IntPropertyUpdateEntry) {
			int property = ((DebugEntry.IntPropertyUpdateEntry)entry).getProperty();
			int value = ((DebugEntry.IntPropertyUpdateEntry)entry).getNewValue();
			int id = ((DebugEntry.IntPropertyUpdateEntry)entry).getObjectID();	
			RescueObject o = memory.lookup(id);
			if (o!=null) {
				Property p = o.getProperty(property);
				if (p instanceof IntProperty) {
					((IntProperty)p).setValue(value,entry.getTimestep(),null);
				}
			}
		}
		else if (entry instanceof DebugEntry.ArrayPropertyUpdateEntry) {
			int property = ((DebugEntry.ArrayPropertyUpdateEntry)entry).getProperty();
			int[] value = ((DebugEntry.ArrayPropertyUpdateEntry)entry).getNewValue();
			int id = ((DebugEntry.ArrayPropertyUpdateEntry)entry).getObjectID();	
			RescueObject o = memory.lookup(id);
			if (o!=null) {
				Property p = o.getProperty(property);
				if (p instanceof ArrayProperty) {
					((ArrayProperty)p).setValues(value,entry.getTimestep(),null);
				}
			}			
		}
		else if (entry instanceof DebugEntry.ObjectDebugEntry) {
			Object obj = ((DebugEntry.ObjectDebugEntry)entry).getObject();
			for(int j = handlers.size()-1; j >= 0; j--){
				Handler h = (Handler)handlers.get(j);
				if(h.handle(obj,entry.getTimestep()))
					break;
			}
		}
	}

	/*
	protected RescueObject getAgent(){
		return memory.lookup(log.getID());
	}
	*/

	/*
	protected void processObjects(){
		ArrayList objects = log.getObjects(timeStep);
		for(int i = 0; i < objects.size(); i++){
			Object obj = objects.get(i);
			for(int j = handlers.size()-1; j >= 0; j--){
				Handler h = (Handler)handlers.get(j);
				if(h.handle(obj,timeStep))
					break;
			}
		}
		doLayout();
	}

	protected void setHistory(){
		if(!getAgent().isHumanoid())
			return;
		int[] hist = ((Humanoid)getAgent()).getPositionHistory();
		moveHistory.removeAllObjects();
		if(hist.length > 0){
			RescueObject[] rs = new RescueObject[hist.length];
			rs[0] = memory.lookup(hist[0]);
			for (int i=1;i<rs.length;++i){
				rs[i] = memory.lookup(hist[i]);
				//if(rs[i-1].isNode() || i==1)
					moveHistory.addObject(new RescueObject[]{rs[i-1],rs[i]});
			}
		}
	}
	*/
}
