/*
 * Last change: $Date: 2004/05/04 03:09:39 $
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

package rescuecore.tools.simulationrunner;

import java.io.*;
import java.net.*;

public class SimulationClient implements SimulationConstants {
	private ProcessHandler processHandler;
	private InputHandler input;

	public static void main(String[] args) {
		int port = DEFAULT_CLIENT_PORT;
		for (int i=0;i<args.length;++i) {
			if (args[i].equalsIgnoreCase("--help")) {
				printHelp();
				return;
			}
			else if (args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("--port")) {
				port = Integer.parseInt(args[++i]);
			}
		}
		try {new SimulationClient(port);}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printHelp() {
		System.out.println("Usage: SimulationClient [-p | --port <port number>]");
		System.out.println("-p\t--port\tListen on <port number>. Default "+DEFAULT_CLIENT_PORT);
	}

	public SimulationClient(int port) throws IOException {
		input = new InputHandler(port);
		new Thread(input).start();
	}

	private void kill() {
		input.kill();
	}

	private synchronized boolean runProcesses(RescueProcess[] processes) {
		if (processHandler!=null) return false;
		processHandler = new ProcessHandler(processes,null);
		new Thread() {public void run() {processHandler.runAll();stopProcesses();}}.start();
		return true;
	}

	private synchronized void stopProcesses() {
		if (processHandler==null) return;
		processHandler.stopAll();
		processHandler = null;
	}

	private class InputHandler implements Runnable {
		private volatile boolean alive;
		private volatile boolean running;
		private ServerSocket server;

		public InputHandler(int port) throws IOException {
			server = new ServerSocket(port);
			server.setSoTimeout(1000);
			alive = true;
			running = true;
		}

		public synchronized void kill() {
			running = false;
			while (alive) try {wait(1000);} catch (InterruptedException e) {break;}
		}

		public void run() {
			try {
				while (running) {
					// Listen for input
					Socket s = server.accept();
					handle(s);
					s.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			try {
				server.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			synchronized(this) {
				alive = false;
				notifyAll();
			}
		}

		private void handle(Socket s) throws IOException {
			DataInputStream in = null;
			DataOutputStream out = null;
			try {
				out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
				in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
				int command;
				do {
					command = in.readInt();
					switch (command) {
					case COMMAND_STOP:
						stopProcesses();
						out.writeInt(COMMAND_SUCCESSFUL);
						break;
					case COMMAND_START:
						if (handleStart(in)) out.writeInt(COMMAND_SUCCESSFUL);
						else out.writeInt(COMMAND_FAILED);
						break;
					case COMMAND_FILE:
						if (handleFile(in,out)) out.writeInt(COMMAND_SUCCESSFUL);
						else out.writeInt(COMMAND_FAILED);
						break;
					case COMMAND_END:
						out.writeInt(COMMAND_SUCCESSFUL);
						break;
					default:
						System.err.println("Unrecognised command: "+command);
						out.writeInt(UNKNOWN_COMMAND);
					}
					out.flush();
				}
				while (command != COMMAND_END);
			}
			catch (IOException e) {
				out.writeInt(COMMAND_FAILED);
				throw e;
			}
			finally {
				if (in!=null) in.close();
				if (out!=null) {
					try {
						out.flush();
						out.close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private boolean handleStart(DataInputStream in) throws IOException {
			int num = in.readInt();
			RescueProcess[] processes = new RescueProcess[num];
			for (int i=0;i<num;++i) {
				processes[i] = new RescueProcess(in.readUTF(),in.readUTF(),in.readUTF(),null);
			}
			return runProcesses(processes);
		}

		private boolean handleFile(DataInputStream in, DataOutputStream out) throws IOException {
			String name = in.readUTF();
			long size = in.readLong();
			OutputStream file = null;
			try {
				try {
					file = new BufferedOutputStream(new FileOutputStream(name));
				}
				catch (IOException e) {
					return false;
				}
				byte[] buffer = new byte[1024];
				long count = 0;
				while (count < size) {
					int num = in.read(buffer);
					if (num > 0) try {
						file.write(buffer,0,num);
						count += num;
					}
					catch (IOException e) {
						return false;
					}
				}
			}
			finally {
				if (file!=null) try {
					file.flush();
					file.close();
				}
				catch (IOException e) {
				}
			}
			return true;
		}
	}
}
