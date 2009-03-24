/*
 * Last change: $Date: 2004/08/09 23:56:57 $
 * $Revision: 1.7 $
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
import rescuecore.Memory;

public class ConvexHullRenderer implements MapRenderer {
    public static final ConvexHullRenderer RED = new ConvexHullRenderer(Color.RED);
    public static final ConvexHullRenderer GREEN = new ConvexHullRenderer(Color.GREEN);
    public static final ConvexHullRenderer BLUE = new ConvexHullRenderer(Color.BLUE);
    public static final ConvexHullRenderer ORANGE = new ConvexHullRenderer(Color.ORANGE);
    public static final ConvexHullRenderer YELLOW = new ConvexHullRenderer(Color.YELLOW);
    public static final ConvexHullRenderer WHITE = new ConvexHullRenderer(Color.WHITE);

	private Color outline;
	private Color fill;

    private ConvexHullRenderer(Color outline){
		this.outline = outline;
		this.fill = new Color(outline.getRed(),outline.getGreen(),outline.getBlue(),96);
	}

    public boolean canRender(Object o) {
		return o instanceof ConvexHull;
    }

    public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) {
		ConvexHull hull = (ConvexHull)o;
		// Draw the points
		int[] hullXs = hull.getXs();
		int[] hullYs = hull.getYs();
		int[] xs = new int[hullXs.length];
		int[] ys = new int[hullYs.length];
		System.arraycopy(hullXs,0,xs,0,xs.length);
		System.arraycopy(hullYs,0,ys,0,ys.length);
		int num = hull.countPoints();
		g.setColor(outline);
		for (int i=0;i<num;++i) {
			g.drawRect(transform.toScreenX(xs[i])-1,transform.toScreenY(ys[i])-1,3,3);
		}
		// Draw segments
		/*int lastX = transform.toScreenX(xs[0]);
		int lastY = transform.toScreenY(ys[0]);
		int firstX = lastX;
		int firstY = lastY;
		for (int i=1;i<num;++i) {
			int x = transform.toScreenX(xs[i]);
			int y = transform.toScreenY(ys[i]);
			g.drawLine(lastX,lastY,x,y);
			lastX = x;
			lastY = y;
		}
		g.drawLine(lastX,lastY,firstX,firstY);*/
		for (int i=0;i<num;++i) {
			xs[i] = transform.toScreenX(xs[i]);
			ys[i] = transform.toScreenY(ys[i]);
		}
		Polygon p = new Polygon(xs,ys,xs.length);
		g.setColor(fill);
		g.fillPolygon(p);
		g.setColor(outline);
		g.drawPolygon(p);
		return p;
    }
}
