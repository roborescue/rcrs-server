/*
 * Last change: $Date: 2004/06/10 01:17:51 $
 * $Revision: 1.2 $
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

/*
 * Created on 20/05/2004
 *
 */
package rescuecore.view;

import rescuecore.*;
import java.awt.*;
/**
 * @author Jono
 *
 */

public class TextRenderer implements MapRenderer {
	private Font f = new Font("Times",Font.BOLD,12);
	
	public void setFont(Font f){
		this.f = f;
	}
	
	public boolean canRender(Object o) {return (o instanceof Text);}

	public Shape render(Object o, Memory memory, Graphics g, ScreenTransform transform) {
		FontMetrics fm = g.getFontMetrics(f);
		Text t = (Text)o;
		String s = t.getString();
		int x = transform.toScreenX(t.getX());
		int y = transform.toScreenY(t.getY());
		int width = fm.stringWidth(s);
		int height = fm.getHeight();
		x -= width/2;
		y += height/2;
		g.setColor(Color.WHITE);
		g.drawString(s,x,y);
		return new Rectangle(x,y,width,height);
	}
}
