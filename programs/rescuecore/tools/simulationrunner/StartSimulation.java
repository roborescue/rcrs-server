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

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

public class StartSimulation {
    private final static String DEFAULT_CONFIG_FILE = "simulation.config";
    private final static String DEFAULT_LOG_PREFIX = "";

    private final static String[] FILTER_NAMES = {"Kernel"};
    private final static Class[] FILTER_CLASSES = {KernelProcessFilter.class};

    private final static Map filters;
	private ProcessHandler handler;

	private final Thread HALT = new Thread() {public void run() {handler.stopAll();}};

    static {
		filters = new HashMap();
		for (int i=0;i<FILTER_NAMES.length;++i)
			filters.put(FILTER_NAMES[i],FILTER_CLASSES[i]);
    }

    public static void main(String[] args) {
		new StartSimulation(args);
    }

    private StartSimulation(String[] args) {
		String config = DEFAULT_CONFIG_FILE;
		String logPrefix = DEFAULT_LOG_PREFIX;
		boolean gui = true;
		for (int i=0;i<args.length;++i) {
			if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("--config")) {
				config = args[++i];
			}
			else if (args[i].equalsIgnoreCase("-l") || args[i].equalsIgnoreCase("--log-prefix")) {
				logPrefix = args[++i];
			}
			else if (args[i].equalsIgnoreCase("-g") || args[i].equalsIgnoreCase("--nogui")) {
				gui = false;
			}
			else {
				printUsage();
				return;
			}
		}
		try {
			//	    System.out.println("Reading config file: "+config);
			ConfigFile configFile = new ConfigFile(config,logPrefix);
			//	    System.out.println("Starting "+configFile.processes.size()+" processes");
			//	    int i=1;
			//	    for (Iterator it = configFile.processes.iterator();it.hasNext();++i) {
			//		RescueProcess next = (RescueProcess)it.next();
			//		System.out.println(i+":\t"+next.name+" ("+next.commandline+")");
			//	    }
			// Start all the processes
			ProcessViewer viewer = new ProcessViewer((RescueProcess[])configFile.getProcesses().toArray(new RescueProcess[0]));
			handler = new ProcessHandler((RescueProcess[])configFile.getProcesses().toArray(new RescueProcess[0]),viewer);
			if (gui) {
				JFrame frame = new JFrame("Robocup Rescue Simulation");
				frame.setContentPane(viewer);
				frame.pack();
				frame.setVisible(true);
				frame.addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e) {
					SwingUtilities.invokeLater(HALT);
				}});
			}
			Runtime.getRuntime().addShutdownHook(HALT);
			handler.runAll();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
    }

    private void printUsage() {
		System.out.println("Usage: StartSimulation [(-c | --config) <config file>] [(-l | --log-prefix) <log prefix>] [(-g | --nogui)]");
		System.out.println("-c\t--config\tUse <config file> to configure the simulator. Default \"simulation.config\"");
		System.out.println("-l\t--log-prefix\tPrepend all log files with <log prefix>. Default is no prefix");
		System.out.println("-g\t--nogui\tDon't show the gui");
    }

    private abstract class ProcessFilter implements Runnable {
		private boolean running, alive;
		private BufferedReader reader;

		public ProcessFilter() {

		}

		public void kill() {
			running = false;
			synchronized(this) {
				while(alive) try {wait(1000);} catch (InterruptedException e) {break;}
			}
		}

		public void run() {
			running = alive = true;
			while (running) {
				try {
					String line = reader.readLine();
				}
				catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
			synchronized(this) {
				alive = false;
				notifyAll();
			}
		}

		public JComponent getViewComponent() {
			return null;
		}
    }

    private class KernelProcessFilter extends ProcessFilter {
    }
}
