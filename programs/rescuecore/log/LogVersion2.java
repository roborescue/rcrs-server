/*
 * Last change: $Date: 2005/06/14 21:55:52 $
 * $Revision: 1.9 $
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

package rescuecore.log;

import java.io.*;
import java.util.*;
import rescuecore.*;
import rescuecore.commands.*;

public class LogVersion2 extends Log {
    private Map<Integer,Update> updates;
	private Map<Integer,Commands> commands;
	private int maxTimestep;
	private Map<String,String> config;

    private Memory memory;
    private int lastTime;

    public LogVersion2(InputBuffer in) throws IOException {
		updates = new HashMap<Integer,Update>();
		commands = new HashMap<Integer,Commands>();
		maxTimestep = 0;
		config = new HashMap<String,String>();
		// Load the preamble
		int size = in.readInt();
		int configEntries = in.readInt();
		for (int i=0;i<configEntries;++i) {
			String key = in.readString();
			String value = in.readString();
			config.put(key,value);
		}
		// Read all the commands and updates, using the old formats
		Command c = null;
		while (in.available()>0) {
			Command[] newCommands = readCommands(in);
			for (Command next : newCommands) {
				if (next instanceof Update) {
					Update u = (Update)next;
					updates.put(u.getTime(),u);
					maxTimestep = Math.max(maxTimestep,u.getTime());
				}
				else if (next instanceof Commands) {
					Commands o = (Commands)next;
					commands.put(o.getTime(),o);
					maxTimestep = Math.max(maxTimestep,o.getTime());
				}
				else {
					System.err.println("Unrecognised command in log: "+next);
				}
			}
		}
		getMemory(0);
    }

    public int getMaxTimestep() {
		return maxTimestep;
    }

	public Update getUpdate(int timestep) {
		return updates.get(timestep);
	}

	public Commands getCommands(int timestep) {
		return commands.get(timestep);
	}

	public Map<String,String> getConfigValues() {
		return new HashMap(config);
	}


    public Memory getMemory(int timestep) {
		if (memory==null || lastTime > timestep) {
			memory = new HashMemory();
			lastTime = -1;
		}
		try {
			// Apply all updates from the last time until the required time
			for (int currentTime = lastTime+1;currentTime<=timestep && currentTime<maxTimestep;++currentTime) {
				Update update = getUpdate(currentTime);
				if (update!=null) memory.update(update);
			}
			lastTime = timestep;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return memory;
    }

	private Command[] readCommands(InputBuffer in) {
		int totalSize = in.readInt();
		return in.readCommands();
	}
}
