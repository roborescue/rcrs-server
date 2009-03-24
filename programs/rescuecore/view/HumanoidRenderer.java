/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.4 $
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

public abstract class HumanoidRenderer implements MapRenderer {
    private final static HumanoidRenderer ORDINARY = new OrdinaryHumanoidRenderer();

    private final static int RADIUS = 5;
    private final static int DIAMETER = RADIUS*2+1;

    public static HumanoidRenderer ordinaryHumanoidRenderer() {
		return ORDINARY;
    }

    public static HumanoidRenderer outlinedHumanoidRenderer(int outlineMode, Color outlineColour) {
		return new OutlinedHumanoidRenderer(outlineMode,outlineColour);
    }

    public static HumanoidRenderer coveredHumanoidRenderer(Color colour) {
		return new CoveredHumanoidRenderer(colour);
    }

    public boolean canRender(Object o) {return (o instanceof Humanoid);}

    private static class OrdinaryHumanoidRenderer extends HumanoidRenderer {
		public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) throws CannotFindLocationException {
			Humanoid h = (Humanoid)o;
			int[] xy = memory.getXY(h);
			if (xy==null) return null;
			int x = transform.toScreenX(xy[0]);
			int y = transform.toScreenY(xy[1]);
			Color colour = ViewConstants.CIVILIAN_COLOUR;
			if (h.getType()==RescueConstants.TYPE_FIRE_BRIGADE) colour = ViewConstants.FIRE_BRIGADE_COLOUR;
			if (h.getType()==RescueConstants.TYPE_AMBULANCE_TEAM) colour = ViewConstants.AMBULANCE_TEAM_COLOUR;
			if (h.getType()==RescueConstants.TYPE_POLICE_FORCE) colour = ViewConstants.POLICE_FORCE_COLOUR;
			if (h.getType()==RescueConstants.TYPE_CAR) colour = ViewConstants.CAR_COLOUR;
			RenderTools.setFillMode(g,ViewConstants.FILL_MODE_SOLID,colour);
			g.fillOval(x-RADIUS,y-RADIUS,DIAMETER,DIAMETER);
			RenderTools.setLineMode(g,ViewConstants.LINE_MODE_SOLID,Color.black);
			g.drawOval(x-RADIUS,y-RADIUS,DIAMETER,DIAMETER);
			return new Ellipse2D.Double(x-RADIUS,y-RADIUS,DIAMETER,DIAMETER);
		}
    }

    private static class OutlinedHumanoidRenderer extends HumanoidRenderer {
		private int mode;
		private Color colour;

		OutlinedHumanoidRenderer(int mode, Color colour) {
			this.mode = mode;
			this.colour = colour;
		}

		public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) throws CannotFindLocationException {
			Humanoid h = (Humanoid)o;
			int[] xy = memory.getXY(h);
			if (xy==null) return null;
			int x = transform.toScreenX(xy[0]);
			int y = transform.toScreenY(xy[1]);
			RenderTools.setLineMode(g,mode,colour);
			g.drawOval(x-RADIUS,y-RADIUS,DIAMETER,DIAMETER);
			return new Ellipse2D.Double(x-RADIUS,y-RADIUS,DIAMETER,DIAMETER);
		}
    }

    private static class CoveredHumanoidRenderer extends HumanoidRenderer {
		private Color colour;

		CoveredHumanoidRenderer(Color colour) {
			this.colour = colour;
		}

		public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) throws CannotFindLocationException {
			Humanoid h = (Humanoid)o;
			int[] xy = memory.getXY(h);
			if (xy==null) return null;
			int x = transform.toScreenX(xy[0]);
			int y = transform.toScreenY(xy[1]);
			RenderTools.setFillMode(g,ViewConstants.FILL_MODE_SOLID,colour);
			g.fillOval(x-RADIUS,y-RADIUS,DIAMETER,DIAMETER);
			return new Ellipse2D.Double(x-RADIUS,y-RADIUS,DIAMETER,DIAMETER);
		}
    }
}
