/*
 * Last change: $Date: 2005/06/14 21:55:52 $
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

package rescuecore.commands;

import rescuecore.InputBuffer;
import rescuecore.OutputBuffer;
import rescuecore.RescueConstants;
import rescuecore.RescueObject;

public class KSConnectOK extends Command {
	private int id;
	private RescueObject[] knowledge;

	public KSConnectOK(int id, RescueObject[] knowledge) {
		super(RescueConstants.KS_CONNECT_OK);
		this.knowledge = knowledge;
		this.id = id;
	}

	public KSConnectOK(InputBuffer in) {
		super(RescueConstants.KS_CONNECT_OK);
		read(in);
	}

	public void read(InputBuffer in) {
		id = in.readInt();
		knowledge = in.readObjects(0,null);
	}

	public void write(OutputBuffer out) {
		out.writeInt(id);
		out.writeObjects(knowledge);
	}

	public int getSimulatorID() {
		return id;
	}

	public RescueObject[] getKnowledge() {
		return knowledge;
	}
}
