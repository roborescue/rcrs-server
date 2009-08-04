/*
 * Last change: $Date: 2004/05/20 23:42:00 $
 * $Revision: 1.1 $
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
import java.util.List;
import java.util.ArrayList;

public class AKExtinguish extends AgentCommand {
    private Nozzle[] nozzles;

    public AKExtinguish(int senderID,int time, int targetID, int direction, int x, int y, int water) {
        super(RescueConstants.AK_EXTINGUISH,senderID,time);
        nozzles = new Nozzle[1];
        nozzles[0] = new Nozzle(targetID,direction,x,y,water);
    }

    public AKExtinguish(int senderID, int time, Nozzle[] nozzles) {
        super(RescueConstants.AK_EXTINGUISH,senderID,time);
        this.nozzles = nozzles;
    }

    public AKExtinguish(InputBuffer in) {
        super(RescueConstants.AK_EXTINGUISH,0,0);
        read(in);
    }

    /*
      public AKExtinguish(int senderID, byte[] data) {
      super(AK_EXTINGUISH,senderID);
      readNozzles(data,0);
      }
    */

    public void read(InputBuffer in) {
        super.read(in);
        List allNozzles = new ArrayList();
        int target = 0;
        do {
            target = in.readInt();
            if (target!=0) {
                allNozzles.add(new Nozzle(target,in.readInt(),in.readInt(),in.readInt(),in.readInt()));
            }
        } while (target!=0);
        nozzles = new Nozzle[allNozzles.size()];
        allNozzles.toArray(nozzles);
    }

    public void write(OutputBuffer out) {
        super.write(out);
        for (int i=0;i<nozzles.length;++i) {
            out.writeInt(nozzles[i].getTarget());
            out.writeInt(nozzles[i].getDirection());
            out.writeInt(nozzles[i].getX());
            out.writeInt(nozzles[i].getY());
            out.writeInt(nozzles[i].getWater());
        }
        out.writeInt(0);
    }

    public Nozzle[] getNozzles() {
        return nozzles;
    }
}
