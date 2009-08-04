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

public class ProcessHandler {
    private RescueProcess[] processes;
    private ProcessViewer viewer;
    private volatile boolean running;
    private volatile Thread startThread;

    /**
       Generate a new ProcessHandler
       @param processes The processes to start
       @param viewer The viewer used to track the processes
    */
    public ProcessHandler(RescueProcess[] processes, ProcessViewer viewer) {
		this.processes = processes;
		this.viewer = viewer;
    }

    /**
       Start all the processes and wait for them to finish
    */
    public void runAll() {
		//	    System.out.println("Starting processes");
		synchronized(this) {
			running = true;
			startThread = Thread.currentThread();
		}
		for (int i=0;i<processes.length;++i) {
			RescueProcess next = processes[i];
			//		System.out.print("Starting "+next+"...");
			if (viewer!=null) viewer.processStarting(i);
			if (!running || !next.start()) {
				//		    System.out.println("failed");
				break;
			}
			if (viewer!=null) viewer.processStarted(i);
			//		System.out.println("success");
		}
		// Wait for one of them to finish
		boolean finished = false;
		synchronized(this) {
			startThread = null;
			while (running && !finished) {
				//		    System.out.println("Waiting for a process to finish...");
				for (int i=0;i<processes.length;++i) {
					RescueProcess next = processes[i];
					if (!next.isRunning()) {
						//			    System.out.println(next+" has finished");
						if (viewer!=null) viewer.processStopped(i);
						finished = true;
						break;
					}
				}
				try {wait(5000);} catch (InterruptedException e) {}
			}
		}
		stopAllProcesses();
    }

    /**
       Stop all the processes
    */
    public synchronized void stopAll() {
		running = false;
		if (startThread!=null) startThread.interrupt();
		notifyAll();
		while (somethingRunning()) try {wait(1000);} catch (InterruptedException e) {break;}
    }

    /**
       Stop all the processes
    */
    private synchronized void stopAllProcesses() {
		//	    System.out.println("Stopping all processes");
		for (int i=0;i<processes.length;++i) {
			RescueProcess next = processes[i];
			if (next.isRunning()) {
				System.out.print("Stopping process "+(i+1)+" of "+processes.length+"...");
				next.stop();
				if (viewer!=null) viewer.processStopped(i);
				System.out.println("stopped");
			}
		}
    }

	private synchronized boolean somethingRunning() {
		for (int i=0;i<processes.length;++i) {
			RescueProcess next = processes[i];
			if (next.isRunning()) return true;
		}
		return false;
	}
}
