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

import java.awt.Point;
import java.awt.event.*;
import java.util.*;

public class ObjectSelector implements MouseListener {
	private Collection<ObjectSelectionListener> listeners;
	private MousePressInfo mouseInfo;
	private Map map;

	public ObjectSelector() {
		listeners = new ArrayList<ObjectSelectionListener>();
		mouseInfo = new MousePressInfo();
	}

	public ObjectSelector(Map map) {
		this();
		setMap(map);
	}

	public void setMap(Map newMap) {
		if (map!=null) map.removeMouseListener(this);
		if (newMap!=null && newMap!=map) newMap.addMouseListener(this);
		map = newMap;
	}

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

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		/*
		if (e.getClickCount()>=2) {
			Point p = e.getPoint();
			Object[] objects = map.toArray();
			// Choose which object to view
			JPopupMenu objectMenu = new JPopupMenu("Choose an object");
			for (int i=0;i<objects.length;++i) {
				if (objects[i] instanceof RescueObject) {
					final RescueObject ro = (RescueObject)objects[i];
					final AbstractAction action = new AbstractAction(ro.toString()) {
							public void actionPerformed(ActionEvent e) {
								JFrame frame = new JFrame();
								ObjectInspector content = new ObjectInspector();
								content.showObject(ro);
								frame.setContentPane(content);
								frame.pack();
								frame.setVisible(true);
							}
						};
					objectMenu.add(action);
				}
			}
			objectMenu.pack();
			objectMenu.show(map,p.x,p.y);
		}
		*/
	}

	public void mousePressed(MouseEvent e) {
		if (map==null) return;
		Point p = e.getPoint();
		// Is it close to the last mouse press?
		if (mouseInfo.closeTo(p) && mouseInfo.objects!=null && mouseInfo.objects.length>0) {
			fireObjectSelected(mouseInfo.advancePointer());
		}
		else {
			Object[] objects = map.getObjectsAtPoint(p);
			mouseInfo.newPoint(p,objects);
			if (objects.length>0) fireObjectSelected(objects[0]);
			else fireObjectSelected(null);
		}
	}

	private void fireObjectSelected(Object o) {
		ObjectSelectionEvent e = new ObjectSelectionEvent(map,o);
		ObjectSelectionListener[] l;
		synchronized(listeners) {
			l = (ObjectSelectionListener[])listeners.toArray(new ObjectSelectionListener[0]);
		}
		for (int i=0;i<l.length;++i) {
			l[i].objectSelected(e);
		}
	}

	private class MousePressInfo {
		Point p;
		Object[] objects;
		int index;

		public boolean closeTo(Point point) {
			if (p==null || point==null) return false;
			if (Math.abs(p.x-point.x)<2 && Math.abs(p.y-point.y)<2) return true;
			return false;
		}

		public Object advancePointer() {
			++index;
			if (index>=objects.length) index = 0;
			System.out.println("Selected object "+index+" of "+objects.length);
			return objects[index];
		}

		public Object newPoint(Point p, Object[] objects) {
			this.p = p;
			this.objects = objects;
			index = 0;
			return objects.length>0?objects[0]:null;
		}
	}
}
