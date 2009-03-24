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

public abstract class Log {
	public final static String HEADER_VERSION_0 = "RoboCup-Rescue Prototype Log 00\0";
	public final static String HEADER_VERSION_1 = "RoboCup-Rescue Prototype Log 01\0";
	public final static String HEADER_VERSION_2 = "RoboCup-Rescue Prototype Log 02\0";
	public final static int HEADER_LENGTH = HEADER_VERSION_0.length();

	public abstract int getMaxTimestep();
	public abstract Memory getMemory(int timestep);
	public abstract Update getUpdate(int timestep);
	public abstract Commands getCommands(int timestep);
	public abstract Map<String,String> getConfigValues();

	public static Log generateLog(String filename) throws IOException, InvalidLogException {
		File file = new File(filename);
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		byte[] data = new byte[(int)file.length()];
		in.readFully(data);
		InputBuffer input = new InputBuffer(data);
		// Check the header
		byte[] preamble = new byte[HEADER_LENGTH];
		input.readBytes(preamble);
		String preambleString = new String(preamble);
		if (HEADER_VERSION_0.equals(preambleString)) {
			throw new InvalidLogException("Log version 0 is no longer supported");
		}
		else if (HEADER_VERSION_1.equals(preambleString)) {
			throw new InvalidLogException("Log version 1 is no longer supported");
		}
		else if (HEADER_VERSION_2.equals(preambleString)) {
			return new LogVersion2(input);
		}
		else {
			throw new InvalidLogException("Unknown log version: "+preambleString);
		}
	}
}
