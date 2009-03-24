/*
 * Last change: $Date: 2004/07/11 22:26:28 $
 * $Revision: 1.6 $
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.Rectangle2D;
import rescuecore.*;
import rescuecore.objects.*;

/**
   This class represents a view of the world. It is possible to draw outlines of objects in the world, convex hulls around groups of objects and to register custom renderers.
 */
public class Map extends JComponent {
	private Memory memory;
	private Dimension mapSize;
	private int minX, maxX, minY, maxY, xRange, yRange;
	private java.util.List layers;
	//	private Collection listeners;
	//	private MousePressInfo mouseInfo;
	private JPopupMenu menu;

	public static Map defaultMap(Memory m) {
		Map result = new Map(m);
		result.addLayer(Layer.createRoadLayer(m));
		result.addLayer(Layer.createNodeLayer(m));
		result.addLayer(Layer.createBuildingLayer(m));
		result.addLayer(Layer.createHumanoidLayer(m));
		return result;
	}

	public Map(Memory m) {
		layers = new ArrayList();
		setDoubleBuffered(true);
		setMemory(m);
		//		listeners = new HashSet();
		//		mouseInfo = new MousePressInfo();
		menu = new JPopupMenu("Layers");
		addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					Point p = e.getPoint();
					if (e.getButton()==MouseEvent.BUTTON3) {
						menu.pack();
						menu.show(Map.this,p.x,p.y);
					}
					//					else handleMousePress(p);
				}
				//				public void mouseClicked(MouseEvent e) {
				//					handleMouseClick(e);
				//				}
			});
	}

	/*
	public void addObjectSelectionListener(ObjectSelectionListener l) {
		synchronized(listeners) {
			listeners.add(l);
		}
	}

	public void removeObjectSelectionListener(ObjectSelectionListener l) {
		synchronized(listeners) {
			listeners.remove(l);
		}
	}
	*/

	public void addLayer(final Layer l) {
		layers.add(l);
		final AbstractAction action = new AbstractAction(l.isEnabled()?"Hide "+l.getName():"Show "+l.getName()) {
				public void actionPerformed(ActionEvent e) {
					l.setEnabled(!l.isEnabled());
					putValue(Action.SMALL_ICON,l.isEnabled()?javax.swing.plaf.metal.MetalIconFactory.getMenuItemCheckIcon():null);
					putValue(Action.NAME,l.isEnabled()?"Hide "+l.getName():"Show "+l.getName());
					repaint();
				}
			};
		menu.add(action);
		repaint();
	}

	public void removeLayer(Layer l) {
		int index = layers.indexOf(l);
		layers.remove(l);
		menu.remove(index);
		repaint();
	}

	public void setMemory(Memory m) {
		//		System.out.println("Setting memory");
		memory = m;
		minX = 0;
		minY = 0;
		maxX = 1;
		maxY = 1;
		boolean first = true;
		int x = 0;
		int y = 0;
		Collection<RescueObject> all = memory.getAllObjects();
		for (RescueObject next : all) {
			switch (next.getType()) {
			case RescueConstants.TYPE_REFUGE:
			case RescueConstants.TYPE_FIRE_STATION:
			case RescueConstants.TYPE_AMBULANCE_CENTER:
			case RescueConstants.TYPE_POLICE_OFFICE:
			case RescueConstants.TYPE_BUILDING:
				x = ((Building)next).getX();
				y = ((Building)next).getY();
				break;
			case RescueConstants.TYPE_NODE:
				x = ((Node)next).getX();
				y = ((Node)next).getY();
				break;
			case RescueConstants.TYPE_RIVER_NODE:
				x = ((RiverNode)next).getX();
				y = ((RiverNode)next).getY();
				break;
			default:
				continue;
			}
			if (first) {
				minX = maxX = x;
				minY = maxY = y;
				first = false;
			}
			else {
				minX = Math.min(minX,x);
				maxX = Math.max(maxX,x);
				minY = Math.min(minY,y);
				maxY = Math.max(maxY,y);
			}
		}
		maxX += 6000;
		maxY += 6000;
		minX -= 6000;
		minY -= 6000;
		xRange = maxX-minX;
		yRange = maxY-minY;
		//		mapSize = new Dimension(xRange,yRange);
		// Map size should be between 200 and 600
		mapSize = new Dimension(Math.max(200,Math.min(xRange,600)),Math.max(200,Math.min(yRange,600)));
		for (Iterator it = layers.iterator();it.hasNext();) {
			Layer next = (Layer)it.next();
			//			System.out.println("Updating layer "+next.getName());
			next.memoryChanged(m);
		}
	}

	public Dimension getPreferredSize() {
		return mapSize;
	}

	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		int width = getSize().width;
		int height = getSize().height;
		RenderTools.setFillMode(graphics,ViewConstants.FILL_MODE_SOLID,ViewConstants.BACKGROUND_COLOUR);
		graphics.fillRect(0,0,width,height);
		Graphics2D g = (Graphics2D)graphics;
		ScreenTransform transform = new ScreenTransform(minX,minY,xRange,yRange,width,height);
		for (Iterator it = layers.iterator();it.hasNext();) {
			Layer next = (Layer)it.next();
			if (next.isEnabled())
				next.paint(g,width,height,transform,memory);
		}
	}

	public Object[] getObjectsAtPoint(Point p) {
		Collection all = new ArrayList();
		Layer[] allLayers = (Layer[])layers.toArray(new Layer[0]);
		for (int i=allLayers.length-1;i>=0;--i) {
			Layer next = allLayers[i];
			if (next.isEnabled()) {
				Object[] o = next.getObjectsAtPoint(p);
				for (int j=0;j<o.length;++j) all.add(o[j]);
			}
		}
		return all.toArray();
	}

	public Object[] getObjectsInArea(Rectangle2D area) {
		Collection all = new ArrayList();
		Layer[] allLayers = (Layer[])layers.toArray(new Layer[0]);
		for (int i=allLayers.length-1;i>=0;--i) {
			Layer next = allLayers[i];
			if (next.isEnabled()) {
				Object[] o = next.getObjectsInArea(area);
				for (int j=0;j<o.length;++j) all.add(o[j]);
			}
		}
		return all.toArray();		
	}
}
