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

package rescuecore.tools.simulationrunner;

import java.io.*;
import java.util.*;

public class LogReader implements Runnable {
    private boolean running;
    private boolean alive;
    private InputStream in;
    private java.util.List out;
    private final Object waitLock = new Object();
    private String waitingFor;
    private StringBuffer waitBuffer;

    /**
       Generate a new LogReader that reads from the given InputStream and writes to the given OutputStream
       @param in The InputStream to read
       @param log An OutputStream to write to
    */
    public LogReader(InputStream in, OutputStream log) {
		this.in = in;
		out = new ArrayList();
		if (log!=null)
			out.add(log);
    }

    /**
       Generate a new LogReader that reads from the given InputStream
       @param in The InputStream to read
    */
    public LogReader(InputStream in) {
		this.in = in;
		out = new ArrayList();
    }

    /**
       Add a new destination for the data
       @param stream The new destination for data.
    */
    public void addOutputStream(OutputStream stream) {
		out.add(stream);
    }

    /**
       Shut down this LogReader
    */
    public synchronized void kill() {
		running = false;
		while (alive) {
			try {wait(1000);} catch (InterruptedException e) {break;}
		}
    }

    /**
       Start this LogReader
    */
    public void start() {
		start(null,null);
    }

    /**
       Start this LogReader and wait for a particular String to appear in the output. This method will block until that message appears
       @param waitFor The message to wait for
       @param process The RescueProcess that controls this LogReader
    */
    public void start(String waitFor, RescueProcess process) {
		synchronized(waitLock) {
			new Thread(this).start();
			waitingFor = waitFor;
			if (waitFor==null || waitFor.equals("")) return;
			waitBuffer = new StringBuffer();
			while (waitingFor!=null) {
				if (!process.isRunning()) break;
				try {waitLock.wait(1000);} catch (InterruptedException e) {break;}
			}
		}
    }

    public void run() {
		running = true;
		alive = true;
		byte[] data = new byte[1024];
		try {
			while (running) {
				// Be nice to other processes
				Thread.yield();
				// Read some data
				// But only read available data to avoid blocking
				int available = Math.min(data.length,in.available());
				if (available==0) {
					try {Thread.sleep(1000);} catch (InterruptedException e) {}
					continue;
				}
				//		System.out.println(available+" bytes available");
				int count = in.read(data,0,available);
				// And write it out
				if (count > 0) {
					//		    System.out.println("Read: "+new String(data));
					checkWait(data);
					for (Iterator it = out.iterator();it.hasNext();) {
						OutputStream next = (OutputStream)it.next();
						next.write(data,0,count);
						next.flush();
					}
				}
			}
		}
		catch (IOException e) {
			//	    e.printStackTrace();
		}
		for (Iterator it = out.iterator();it.hasNext();) {
			OutputStream next = (OutputStream)it.next();
			try {
				next.flush();
				next.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		synchronized(this) {
			alive = false;
			notifyAll();
		}
    }

    private void checkWait(byte[] data) {
		synchronized(waitLock) {
			if (waitingFor==null || waitBuffer==null) return;
			waitBuffer.append(new String(data));
			String s = waitBuffer.toString();
			//	    System.out.println("Checking "+s+" for "+waitingFor);
			if (s.indexOf(waitingFor)!=-1) {
				waitingFor = null;
				waitBuffer = null;
				waitLock.notifyAll();
			}
		}
    }
}
