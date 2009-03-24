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

import rescuecore.*;
import rescuecore.view.*;
import javax.swing.*;

public interface Handler{

	/**
	 * Called once by the DebugPane when the Handler is registered. This gets
	 * a JComponent to add to the Debugger that this Handler may write to.
	 * @return A JComponent to add, or null if no JComponent is used by this Handler.
	 **/
	public JComponent getComponent();
	/**
	 * Gets a Layer for this Handler to draw to.
	 * @return A Layer to draw on, or null if no Layer is needed by this Handler.
	 **/
	public Layer getLayer();
	/**
	 * Handles the debugging of a transient object.
	 * @return Whether or not this Handler has dealt with the Object.
	 **/
	public boolean handle(Object o, int timeStep);
	/**
	 * Sets up the Handler on a given memory.
	 **/
	public void setMemory(Memory m);

}
