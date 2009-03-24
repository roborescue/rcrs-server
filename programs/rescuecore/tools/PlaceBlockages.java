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
import rescuecore.objects.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class PlaceBlockages {
	private Road[] allRoads;
	private Node[] allNodes;

	private Memory memory;
	private Map map;
	private Point pressPoint;
	private Point dragPoint;
	private Layer overlay;
	private Layer roadLayer;

	public PlaceBlockages(Road[] roads, Node[] nodes) {
		allRoads = roads;
		allNodes = nodes;
		memory = new HashMemory();
		for (int i=0;i<roads.length;++i) {
			memory.add(roads[i],0,this);
		}
		for (int i=0;i<nodes.length;++i) {
			if (memory.lookup(nodes[i].getID())!=null) System.err.println("WARNING: Duplicate node ID: "+nodes[i].getID()+", this is is already used by "+memory.lookup(nodes[i].getID()));
			memory.add(nodes[i],0,this);
		}
		Arrays.sort(allRoads,new RoadSorter(memory));
		map = new Map(memory);
		roadLayer = Layer.createRoadLayer(memory);
		roadLayer.addRenderer(Road.class,new BigRoadRenderer());
		map.addLayer(roadLayer);
		map.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					handleClick(e);
				}
				public void mousePressed(MouseEvent e) {
					handlePress(e);
				}
				public void mouseReleased(MouseEvent e) {
					handleRelease(e);
				}
			});
		map.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent e) {
					handleDrag(e);
				}
			});
		JToolBar toolbar = new JToolBar();
		Action saveAction = new AbstractAction("Save") {
				public void actionPerformed(ActionEvent e) {
					save();
				}
			};
		toolbar.add(saveAction);
		JFrame frame = new JFrame("Road Blockage Placement");
		JPanel top = new JPanel(new BorderLayout());
		top.add(toolbar,BorderLayout.NORTH);
		top.add(map,BorderLayout.CENTER);
		frame.addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e) {
			System.exit(0);
		}});
		frame.getContentPane().add(top);
		frame.pack();
		frame.setVisible(true);
	}

	private void handleClick(MouseEvent e) {
		Object[] all = map.getObjectsAtPoint(e.getPoint());
		update(all);
		map.repaint();
	}

	private void handlePress(MouseEvent e) {
		pressPoint = e.getPoint();
	}

	private void handleRelease(MouseEvent e) {
		if (dragPoint==null) return;
		int x1 = Math.min(pressPoint.x,dragPoint.x);
		int y1 = Math.min(pressPoint.y,dragPoint.y);
		int x2 = Math.max(pressPoint.x,dragPoint.x);
		int y2 = Math.max(pressPoint.y,dragPoint.y);
		Rectangle2D box = new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
		Object[] objects = map.getObjectsInArea(box);
		update(objects);
		pressPoint = null;
		dragPoint = null;
		overlay.removeAllObjects();
		map.repaint();
	}

	private void handleDrag(MouseEvent e) {
		if (pressPoint!=null) {
			dragPoint = e.getPoint();
			int dx = pressPoint.x - dragPoint.x;
			int dy = pressPoint.y - dragPoint.y;
			if (dx < 0) dx = -dx;
			if (dy < 0) dy = -dy;
			if (dx > 5 || dy > 5) {
				// Draw a rectangle on the view
				Rectangle r = new Rectangle(Math.min(pressPoint.x,dragPoint.x),Math.min(pressPoint.y,dragPoint.y),dx,dy);
				overlay.setObject(r);
				map.repaint();
			}
		}
	}

	private void update(Object[] os) {
		for (int i=0;i<os.length;++i) {
			if (os[i] instanceof Road) updateRoad((Road)os[i]);
		}
		map.repaint();
	}

	private void updateRoad(Road road) {
		int lanes = road.getLinesToHead();
		int empty = lanes; //road.getFreeLinesToHead(); FIXME
		int width = road.getWidth();
		--empty;
		if (empty==-1) empty = lanes;
		int blocked = lanes-empty;
		int laneWidth = road.getWidth()/(road.getLinesToHead()+road.getLinesToTail());
		int blockNeeded = blocked * laneWidth * 2;
		if (blockNeeded > road.getWidth()) {
			System.out.println("Trying to set block to "+blockNeeded+" but width is only "+road.getWidth());
			blockNeeded = road.getWidth();
		}
		road.setBlock(blockNeeded,0,this);
		// Check that we have blocked the right number of lanes
		int free = road.getLinesToHead(); //road.getFreeLinesToHead(); FIXME
		if (free != empty) System.out.println("We have "+free+" empty lanes instead of "+empty+"!");
	}

	private void save() {
		try {
			// Write out the blocked roads
			PrintWriter out = new PrintWriter(new FileWriter(new File("blockades.lst")));
			for (int i=0;i<allRoads.length;++i) {
				out.println(allRoads[i].getBlock());
			}
			out.flush();
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class BigRoadRenderer implements MapRenderer {
		public boolean canRender(Object o) { 
			return o instanceof Road;
		}

		public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) throws CannotFindLocationException {
			Road road = (Road)o;
			Node roadHead = (Node)memory.lookup(road.getHead());
			Node roadTail = (Node)memory.lookup(road.getTail());
			int headX = transform.toScreenX(roadHead.getX());
			int headY = transform.toScreenY(roadHead.getY());
			int tailX = transform.toScreenX(roadTail.getX());
			int tailY = transform.toScreenY(roadTail.getY());
			Shape shape = new java.awt.geom.Line2D.Double(headX,headY,tailX,tailY);
			shape = new BasicStroke(10).createStrokedShape(shape);
			Color c = Color.BLACK;
			int lanes = road.getLinesToHead();
			int free = lanes; //road.getFreeLinesToHead(); FIXME
			c = Color.ORANGE;
			if (free==0) c = Color.BLACK;
			if (free==lanes) c = Color.WHITE;
			RenderTools.setLineMode(g,ViewConstants.LINE_MODE_SOLID,c);
			((Graphics2D)g).draw(shape);
			return shape;
		}
	}

	public static void main(String[] args) {
		try {
			Road[] r = MapFiles.loadRoads("road.bin");
			Node[] n = MapFiles.loadNodes("node.bin");
			new PlaceBlockages(r,n);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class RoadSorter implements Comparator {
		private Memory m;

		public RoadSorter(Memory m) {
			this.m = m;
		}

		public int compare(Object o1, Object o2) {
			Road r1 = (Road)o1;
			Road r2 = (Road)o2;
			Node h1 = (Node)m.lookup(r1.getHead());
			Node t1 = (Node)m.lookup(r1.getTail());
			Node h2 = (Node)m.lookup(r2.getHead());
			Node t2 = (Node)m.lookup(r2.getTail());
			int x1 = (h1.getX()+t1.getX())/2;
			int y1 = (h1.getY()+t1.getY())/2;
			int x2 = (h2.getX()+t2.getX())/2;
			int y2 = (h2.getY()+t2.getY())/2;
			if (x1 < x2) return -1;
			if (x1 > x2) return 1;
			if (y1 < y2) return -1;
			if (y2 > y1) return 1;
			return 0;
		}
	}
}
