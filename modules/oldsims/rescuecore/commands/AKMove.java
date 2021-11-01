/*
 * Last change: $Date: 2004/07/11 22:26:28 $
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
import rescuecore.Handy;

public class AKMove extends AgentCommand {
    private int[] path;

    public AKMove(int senderID, int time, int[] path) {
        super(RescueConstants.AK_MOVE,senderID, time);
        this.path = new int[path.length];
        System.arraycopy(path,0,this.path,0,path.length);
    }

    public AKMove(InputBuffer in) {
        super(RescueConstants.AK_MOVE,0,0);
        read(in);
    }

    public void read(InputBuffer in) {
        super.read(in);
        path = new int[in.readInt()];
        //		System.out.println("Reading AK_MOVE of length "+path.length);
        for (int i=0;i<path.length;++i) {
            path[i] = in.readInt();
            //			System.out.println(path[i]);
        }
    }

    public void write(OutputBuffer out) {
        super.write(out);
        out.writeInt(path.length);
        for (int i=0;i<path.length;++i) out.writeInt(path[i]);
    }

    public int[] getPath() {
        return path;
    }

    public String toString() {
        return "AK_MOVE ("+Handy.arrayAsString(path)+")";
    }
}
