/*
 * Last change: $Date: 2005/02/18 03:34:34 $
 * $Revision: 1.5 $
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

import rescuecore.*;
import rescuecore.objects.*;
import java.awt.*;
import java.awt.geom.*;

public class RoadRenderer implements MapRenderer {
    private final static RoadRenderer ORDINARY = new RoadRenderer();

    public static RoadRenderer ordinaryRoadRenderer() {
		return ORDINARY;
    }

    public static RoadRenderer outlinedRoadRenderer(int mode, Color colour) {
		return new OutlinedRoadRenderer(mode,colour);
    }

    protected RoadRenderer() {}

    public boolean canRender(Object o) {return (o instanceof Road);}

    public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) {
		Road road = (Road)o;
		Node roadHead = (Node)memory.lookup(road.getHead());
		Node roadTail = (Node)memory.lookup(road.getTail());
		int headX = transform.toScreenX(roadHead.getX());
		int headY = transform.toScreenY(roadHead.getY());
		int tailX = transform.toScreenX(roadTail.getX());
		int tailY = transform.toScreenY(roadTail.getY());
		int blockedLines = 0; //road.getBlockedLines(); FIXME
		int total = road.getLinesToHead()+road.getLinesToTail();
		int free = total-blockedLines;
		boolean isBlocked = road.getBlock()>0;
		RenderTools.setLineMode(g,ViewConstants.LINE_MODE_SOLID,Color.black,(road.getLinesToHead()+road.getLinesToTail())*3);
		g.drawLine(headX,headY,tailX,tailY);
		if (isBlocked) {
			Color blockColour = Color.white;
			if (blockedLines>0) blockColour = Color.gray;
			if (free==0) blockColour = Color.red;
			RenderTools.setLineMode(g,ViewConstants.LINE_MODE_SOLID,blockColour,2);
			// Draw a cross in the middle of the road
			int centerX = (headX+tailX)/2;
			int centerY = (headY+tailY)/2;
			g.drawLine(centerX-3,centerY-3,centerX+3,centerY+3);
			g.drawLine(centerX-3,centerY+3,centerX+3,centerY-3);
		}
		Shape shape = new java.awt.geom.Line2D.Double(headX,headY,tailX,tailY);
		shape = new BasicStroke((road.getLinesToTail()+road.getLinesToHead())*3).createStrokedShape(shape);
		return shape;
    }

    private static class OutlinedRoadRenderer extends RoadRenderer {
		private int mode;
		private Color colour;

		public OutlinedRoadRenderer(int mode, Color colour) {
			this.mode = mode;
			this.colour = colour;
		}

		public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) {
			Road road = (Road)o;
			Node roadHead = (Node)memory.lookup(road.getHead());
			Node roadTail = (Node)memory.lookup(road.getTail());
			int headX = transform.toScreenX(roadHead.getX());
			int headY = transform.toScreenY(roadHead.getY());
			int tailX = transform.toScreenX(roadTail.getX());
			int tailY = transform.toScreenY(roadTail.getY());
			int blocked = 0; //road.getBlockedLines(); FIXME
			int total = road.getLinesToHead()+road.getLinesToTail();
			int free = total-blocked;
			Shape shape = new java.awt.geom.Line2D.Double(headX,headY,tailX,tailY);
			shape = new BasicStroke(road.getLinesToTail()+road.getLinesToHead()).createStrokedShape(shape);
			RenderTools.setLineMode(g,mode,colour);
			((Graphics2D)g).draw(shape);
			return shape;
		}
    }
}
