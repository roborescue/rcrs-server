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

import java.awt.*;
import java.awt.image.*;

public class RenderTools {
    private final static int HATCH_SIZE = 9;

    private final static Stroke SOLID_STROKE = new BasicStroke(1);
    private final static float[] DOT_PATTERN = {0.1f,1f};
    private final static float[] DASH_PATTERN = {1f,1f};
    private final static float[] DOT_DASH_PATTERN = {0.1f,1f,1f,1f};
    private final static float[][] DASH_PATTERNS = {null,DOT_PATTERN,DASH_PATTERN,DOT_DASH_PATTERN};

    private final static BufferedImage HORIZONTAL = new BufferedImage(HATCH_SIZE,HATCH_SIZE,BufferedImage.TYPE_INT_ARGB);
    private final static BufferedImage VERTICAL = new BufferedImage(HATCH_SIZE,HATCH_SIZE,BufferedImage.TYPE_INT_ARGB);
    private final static BufferedImage DIAGONAL = new BufferedImage(HATCH_SIZE,HATCH_SIZE,BufferedImage.TYPE_INT_ARGB);
    private final static BufferedImage REVERSE_DIAGONAL = new BufferedImage(HATCH_SIZE,HATCH_SIZE,BufferedImage.TYPE_INT_ARGB);
    private final static BufferedImage HATCH = new BufferedImage(HATCH_SIZE,HATCH_SIZE,BufferedImage.TYPE_INT_ARGB);
    private final static BufferedImage CROSS_HATCH = new BufferedImage(HATCH_SIZE,HATCH_SIZE,BufferedImage.TYPE_INT_ARGB);
    private final static Rectangle ANCHOR = new Rectangle(0,0,HATCH_SIZE,HATCH_SIZE);

    private final static int TRANSPARENT_BLACK = new Color(0,0,0,0).getRGB();

    static {
		for (int x=0;x<HATCH_SIZE;++x) {
			for (int y=0;y<HATCH_SIZE;++y) {
				HORIZONTAL.setRGB(x,y,TRANSPARENT_BLACK);
				VERTICAL.setRGB(x,y,TRANSPARENT_BLACK);
				DIAGONAL.setRGB(x,y,TRANSPARENT_BLACK);
				REVERSE_DIAGONAL.setRGB(x,y,TRANSPARENT_BLACK);
				HATCH.setRGB(x,y,TRANSPARENT_BLACK);
				CROSS_HATCH.setRGB(x,y,TRANSPARENT_BLACK);
			}
		}
    }

    private RenderTools() {}

    public static void setLineMode(Graphics g, int mode, Color colour, int width) {
		Stroke stroke;
		if (mode==ViewConstants.LINE_MODE_SOLID) {
			if (width==1) stroke = SOLID_STROKE;
			else stroke = new BasicStroke(width);
		}
		else {
			stroke = new BasicStroke(width,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1f,DASH_PATTERNS[mode],0);
		}
		((Graphics2D)g).setStroke(stroke);
		if (colour!=null)
			g.setColor(colour);
    }

    public static void setLineMode(Graphics g, int mode, Color colour) {
		setLineMode(g,mode,colour,1);
    }

    public static void setLineMode(Graphics g, int mode) {
		setLineMode(g,mode,null,1);
    }

    public static void setLineMode(Graphics g, int mode, int width) {
		setLineMode(g,mode,null,width);
    }

    public static void setFillMode(Graphics g, int mode, Color colour) {
		int c = colour.getRGB();
		for (int x=0;x<HATCH_SIZE;++x) {
			HORIZONTAL.setRGB(x,HATCH_SIZE/2,c);
			VERTICAL.setRGB(HATCH_SIZE/2,x,c);
			DIAGONAL.setRGB(x,x,c);
			REVERSE_DIAGONAL.setRGB(HATCH_SIZE-x-1,x,c);
			HATCH.setRGB(x,HATCH_SIZE/2,c);
			HATCH.setRGB(HATCH_SIZE/2,x,c);
			CROSS_HATCH.setRGB(x,x,c);
			CROSS_HATCH.setRGB(HATCH_SIZE-x-1,x,c);
		}
		switch (mode) {
		case ViewConstants.FILL_MODE_SOLID:
			((Graphics2D)g).setPaint(colour);
			break;
		case ViewConstants.FILL_MODE_HORIZONTAL_LINES:
			((Graphics2D)g).setPaint(new TexturePaint(HORIZONTAL,ANCHOR));
			break;
		case ViewConstants.FILL_MODE_VERTICAL_LINES:
			((Graphics2D)g).setPaint(new TexturePaint(VERTICAL,ANCHOR));
			break;
		case ViewConstants.FILL_MODE_DIAGONAL_LINES:
			((Graphics2D)g).setPaint(new TexturePaint(DIAGONAL,ANCHOR));
			break;
		case ViewConstants.FILL_MODE_REVERSE_DIAGONAL_LINES:
			((Graphics2D)g).setPaint(new TexturePaint(REVERSE_DIAGONAL,ANCHOR));
			break;
		case ViewConstants.FILL_MODE_HATCH:
			((Graphics2D)g).setPaint(new TexturePaint(HATCH,ANCHOR));
			break;
		case ViewConstants.FILL_MODE_CROSS_HATCH:
			((Graphics2D)g).setPaint(new TexturePaint(CROSS_HATCH,ANCHOR));
			break;
		}
    }
}
