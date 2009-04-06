/*
 * Last change: $Date: 2004/05/04 03:09:38 $
 * $Revision: 1.3 $
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

import java.awt.*;
import rescuecore.view.*;
import rescuecore.objects.*;
import rescuecore.*;

public class OutlineRenderer implements MapRenderer {
    public static final OutlineRenderer RED = new OutlineRenderer(Color.RED);
    public static final OutlineRenderer GREEN = new OutlineRenderer(Color.GREEN);
    public static final OutlineRenderer BLUE = new OutlineRenderer(Color.BLUE);
    public static final OutlineRenderer ORANGE = new OutlineRenderer(Color.ORANGE);
    public static final OutlineRenderer YELLOW = new OutlineRenderer(Color.YELLOW);
    public static final OutlineRenderer WHITE = new OutlineRenderer(Color.WHITE);

	private Color outline;
	private Color fill;

    private OutlineRenderer(Color outline){
		this.outline = outline;
		this.fill = new Color(outline.getRed(),outline.getGreen(),outline.getBlue(),96);
	}

    public boolean canRender(Object o) {
		if (o instanceof RescueObject[] && ((RescueObject[])o).length == 2){
			return true;
		}
		return false;
    }

    public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) {
		RescueObject[] rs = (RescueObject[])o;
		int x1 = 0;
		int y1 = 0;
		int x2 = 0;
		int y2 = 0;
		if(rs[1].isBuilding()){
			Building b = (Building)rs[1];
			Node n = (Node)rs[0];
			x2 = transform.toScreenX(b.getX());
			y2 = transform.toScreenY(b.getY());
			x1 = transform.toScreenX(n.getX());
			y1 = transform.toScreenY(n.getY());

		}
		else if(rs[0].isBuilding()){
			Building b = (Building)rs[0];
			Node n = (Node)rs[1];
			x2 = transform.toScreenX(b.getX());
			y2 = transform.toScreenY(b.getY());
			x1 = transform.toScreenX(n.getX());
			y1 = transform.toScreenY(n.getY());

		}
		else if(rs[1].isRoad() || rs[0].isRoad()){
			Road r;
			if(rs[0].isRoad())
				r = (Road)rs[0];
			else
				r = (Road)rs[1];
			Node head = (Node)(memory.lookup(r.getHead()));
			Node tail = (Node)(memory.lookup(r.getTail()));
			x2 = transform.toScreenX(head.getX());
			y2 = transform.toScreenY(head.getY());
			x1 = transform.toScreenX(tail.getX());
			y1 = transform.toScreenY(tail.getY());
		}
		else if(rs[0].isNode() && rs[1].isNode()){
			Node n1 = (Node)rs[0];
			Node n2 = (Node)rs[1];
			x2 = transform.toScreenX(n1.getX());
			y2 = transform.toScreenY(n1.getY());
			x1 = transform.toScreenX(n2.getX());
			y1 = transform.toScreenY(n2.getY());
		}
		else
			return new Polygon(new int[0],new int[0],0);
		Shape shape = new java.awt.geom.Line2D.Double(x1,y1,x2,y2);
		shape = new BasicStroke(6).createStrokedShape(shape);
		//RenderTools.setLineMode(g,ViewConstants.LINE_MODE_SOLID,outline);
		RenderTools.setFillMode(g,ViewConstants.FILL_MODE_SOLID,fill);
		((Graphics2D)g).fill(shape);
		return shape;
    }
}
