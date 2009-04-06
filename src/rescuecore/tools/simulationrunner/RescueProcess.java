/*
 * Last change: $Date: 2004/05/04 03:09:39 $
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

package rescuecore.tools.simulationrunner;

import java.io.*;

public class RescueProcess {
    private String name, commandline, done, logPrefix;
    private boolean useOut;
    private Process process;
    private LogReader out, error;

    /** Create a new RescueProcess
		@param name The name of this process
		@param commandline The command line used to start the process
		@param started When this string is encountered in the standard output stream then this process is deemed to have initialised successfully. If this parameter is null, then the process is assumed to initialise instantaneously.
    */
    public RescueProcess(String name, String commandline, String started, String logPrefix) {
		this.name = name;
		this.commandline = commandline;
		this.done = started;
		this.logPrefix = logPrefix;
		useOut = true;
		if (done.startsWith("stderr:")) {
			done = done.substring(7);
			useOut = false;
		}
    }

    public String toString() {
		return name+": "+commandline+" -> "+done;
    }

    /**
       Get the name of this process
       @return The name of this process
	*/
    public String getName() {
		return name;
    }

    /**
       Get the command line used to start this process
       @return The command line used to start this process
	*/
    public String getCommandLine() {
		return commandline;
    }

    /**
       Get the string that delimits successful initialisation of this process
       @return The string that delimits successful initialisation
    */
    public String getStartedFlag() {
		return done;
    }

    /**
       Start this process and wait for the started flag to appear in the output. Standard output and standard error are automatically redirected to files called <name>.log and <name>.error, where <name> is the value returned by the @{link #getName()} method.
       @return true iff the process starts successfully
    */
    public synchronized boolean start() {
		try {
			System.out.println("Executing "+commandline);
			process = Runtime.getRuntime().exec(commandline);
			InputStream output = process.getInputStream();
			InputStream errors = process.getErrorStream();
			// Create log files
			out = new LogReader(output,logPrefix==null?null:new BufferedOutputStream(new FileOutputStream(new File(logPrefix+name+".log"))));
			error = new LogReader(errors,logPrefix==null?null:new BufferedOutputStream(new FileOutputStream(new File(logPrefix+name+".error"))));
		}
		catch (IOException e) {
			System.err.println("Could not start process: "+commandline);
			e.printStackTrace();
			if (process!=null) process.destroy();
			return false;
		}
		// Start sucking the output into log files
		if (useOut) {
			out.start(done,this);
			error.start();
		}
		else {
			out.start();
			error.start(done,this);
		}
		return true;
    }

    /**
       Add a new destination for the standard output stream from this process. Output data will be multiplexed to all destinations.
       @param stream An OutputStream to write standard output to
	*/
    public void addOutputDestination(OutputStream stream) {
		out.addOutputStream(stream);
    }

    /**
       Add a new destination for the standard error stream from this process. Output data will be multiplexed to all destinations.
       @param stream An OutputStream to write standard error to
	*/
    public void addErrorDestination(OutputStream stream) {
		error.addOutputStream(stream);
    }

    /**
       Find out whether this process is running or not
       @return true iff the process is running
    */
    public synchronized boolean isRunning() {
		if (process==null) return false;
		try {process.exitValue();}
		catch (IllegalThreadStateException e) {return true;}
		return false;
    }

    /**
       Stop the process
	*/
    public synchronized void stop() {
		if (process==null) return;
		out.kill();
		error.kill();
		process.destroy();
		process = null;
    }
}

