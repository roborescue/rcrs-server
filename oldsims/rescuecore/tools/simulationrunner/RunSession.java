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

// FIXME: Clients need to make a callback to let the server know when they are finished

import java.io.*;
import java.net.*;
import java.util.*;

public class RunSession implements SimulationConstants {
	private final static String DEFAULT_SESSION_FILE = "session.xml";
	private final static String DEFAULT_TEAM_DIRECTORY = "teams";
	private final static String DEFAULT_CLIENTS_FILE = "clients.txt";

	public static void main(String[] args) {
		String sessionFile = DEFAULT_SESSION_FILE;
		String teamDir = DEFAULT_TEAM_DIRECTORY;
		String clientsFile = DEFAULT_CLIENTS_FILE;
		for (int i=0;i<args.length;++i) {
			if (args[i].equalsIgnoreCase("-s") || args[i].equalsIgnoreCase("--session")) {
				sessionFile = args[++i];
			}
			else if (args[i].equalsIgnoreCase("-t") || args[i].equalsIgnoreCase("--teams")) {
				teamDir = args[++i];
			}
			else if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("--clients")) {
				clientsFile = args[++i];
			}
			else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help")) {
				printHelp();
				return;
			}
			else {
				System.err.println("Unrecognised option: "+args[i]);
				printHelp();
				return;
			}
		}
		try {
			// Load the client info
			ClientInfo[] clients = loadClients(clientsFile);
			if (clients==null) return;
			// Load the teams
			TeamInfo[] teams = loadTeams(teamDir);
			if (teams==null) return;
			// Load the session
			Session session = new Session(sessionFile);

			// Say what's going on
			System.out.println("Clients");
			System.out.println("*******");
			for (int i=0;i<clients.length;++i) {
				System.out.println(clients[i]);
			}
			System.out.println();
			System.out.println("Teams");
			System.out.println("*****");
			for (int i=0;i<teams.length;++i) {
				System.out.println("Team "+(i+1));
				System.out.println("\tCommands");
				System.out.println("\t========");
				for (int j=0;j<teams[i].commands.length;++j) {
					System.out.println("\t"+teams[i].commands[j]);
				}
				System.out.println();
				System.out.println("\tFiles");
				System.out.println("\t========");
				for (int j=0;j<teams[i].files.length;++j) {
					System.out.println("\t"+teams[i].files[j]);
				}
				System.out.println();
			}
			System.out.println("Session");
			System.out.println("*******");
			System.out.println("Name: "+session.getName());
			System.out.println("Config: "+session.getConfigFile());
			System.out.println("Gis: "+session.getGisFile());
			System.out.println("Roads: "+session.getRoadFile());
			System.out.println("Nodes: "+session.getNodeFile());
			System.out.println("Buildings: "+session.getBuildingFile());
			System.out.println("Galpolydata: "+session.getGalFile());
			System.out.println("Shindopolydata: "+session.getShindoFile());
			RescueProcess[] allProcesses = session.getProcesses();
			for (int i=0;i<allProcesses.length;++i) {
				System.out.println("Process "+i+": "+allProcesses[i]);
			}
			// Run everything
			run(clients,teams,session);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void run(ClientInfo[] clients, TeamInfo[] teams, Session session) throws IOException {
		sendSimulatorData(clients,session);
		for (int i=0;i<teams.length;++i) {
			run(clients,teams[i],session);
		}
	}
	
	private static boolean sendSimulatorData(ClientInfo[] clients, Session session) throws IOException {
		// Send all simulator data to simulation clients
 		for (int i=0;i<clients.length;++i) {
			if (clients[i].isSimulation) {
				// Send all simulator data to simulation clients
				Socket s = null;
				DataOutputStream out = null;
				DataInputStream in = null;
				try {
					s = new Socket(clients[i].address,clients[i].port);
					out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
					in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
					if (!sendFile("config.txt",session.getConfigFile(),out,in)) return false;
					if (!sendFile("road.bin",session.getRoadFile(),out,in)) return false;
					if (!sendFile("node.bin",session.getNodeFile(),out,in)) return false;
					if (!sendFile("building.bin",session.getBuildingFile(),out,in)) return false;
					if (!sendFile("galpolydata.dat",session.getGalFile(),out,in)) return false;
					if (!sendFile("shindopolydata.dat",session.getShindoFile(),out,in)) return false;
					if (!sendFile("gisini.txt",session.getGisFile(),out,in)) return false;
				}
				finally {
					if (in!=null) try {in.close();} catch (IOException e) {e.printStackTrace();}
					if (out!=null) try {out.writeInt(COMMAND_END);out.flush();out.close();} catch (IOException e) {e.printStackTrace();}
					if (s!=null) try {s.close();} catch (IOException e) {e.printStackTrace();}
				}
			}
		}
		return true;
	}

	private static boolean run(ClientInfo[] clients, TeamInfo team, Session session) throws IOException {
		try {
			int numClients = 0;
			for (int i=0;i<clients.length;++i) {
				if (!clients[i].isSimulation) {
					++numClients;
					// Send all agent data to agent clients
					Socket s = null;
					DataOutputStream out = null;
					DataInputStream in = null;
					try {
						s = new Socket(clients[i].address,clients[i].port);
						out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
						in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
						for (int j=0;j<team.files.length;++j) {
							File file = new File(team.files[j]);
							if (!sendFile(file.getName(),file,out,in)) return false;
						}
					}
					finally {
						if (in!=null) try {in.close();} catch (IOException e) {e.printStackTrace();}
						if (out!=null) try {out.writeInt(COMMAND_END);out.flush();out.close();} catch (IOException e) {e.printStackTrace();}
						if (s!=null) try {s.close();} catch (IOException e) {e.printStackTrace();}
					}
				}
			}
			// Now allocate processes to clients
			int processesPerClient = (int)Math.ceil(((double)team.commands.length)/(double)numClients);
			String[][] processes = new String[numClients][processesPerClient];
			int nextClient = 0;
			int nextIndex = 0;
			for (int i=0;i<team.commands.length;++i) {
				processes[nextClient++][nextIndex] = team.commands[i];
				if (nextClient >= numClients) {
					nextClient = 0;
					++nextIndex;
				}
			}
			// Send the jobs to the clients
			nextClient = 0;
			for (int i=0;i<clients.length;++i) {
				if (clients[i].isSimulation) continue;
				sendProcesses(clients[i],processes[nextClient++]);
			}
			// Wait for a client to return
		}
		finally {
			// Stop all the clients
		}
		return true;
	}

	private static boolean sendFile(String name, String filename, DataOutputStream out, DataInputStream in) throws IOException {
		return sendFile(name,new File(filename),out,in);
	}

	private static boolean sendFile(String name, File file, DataOutputStream out, DataInputStream in) throws IOException {
		long size = file.length();
		DataInputStream data = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		return sendFile(name,data,size,out,in);
	}

	private static boolean sendFile(String name, DataInputStream file, long size, DataOutputStream out, DataInputStream in) throws IOException {
		out.writeInt(COMMAND_FILE);
		out.writeUTF(name);
		out.writeLong(size);
		byte[] buffer = new byte[1024];
		long sent = 0;
		while (sent < size) {
			int n = file.read(buffer);
			if (n > 0) {
				out.write(buffer,0,n);
				sent += n;
			}
		}
		out.flush();
		return in.readInt()==COMMAND_SUCCESSFUL;
	}

	private static boolean sendProcesses(ClientInfo client, String[] processes) throws IOException {
		return true;
	}

	private static void printHelp() {
		System.err.println("Usage: RunSession [-s | --session <session file>] [-t | --teams <teams directory>] [-c | --clients <clients file>] [-h | --help]");
		System.err.println("-s\t--session\tLoad the session from <session file>. Default \""+DEFAULT_SESSION_FILE+"\"");
		System.err.println("-t\t--teams\tLoad the teams from <teams directory>. Each subdirectory in <teams directory> should contain a file called \"commandlines.txt\" that contains all command lines used to launch the agents, as well as the binaries for those agents and any supporting files. Default \""+DEFAULT_TEAM_DIRECTORY+"\"");
		System.err.println("-c\t--clients\tGet the list of clients from <clients file>. Default \""+DEFAULT_CLIENTS_FILE+"\"");
		System.err.println("-h\t--help\tPrint this help message");
	}

	private static ClientInfo[] loadClients(String clientFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(clientFile));
		String line = null;
		Collection result = new ArrayList();
		boolean isSim = true;
		do {
			line = in.readLine();
			if (line!=null) {
				line = line.trim();
				if (line.startsWith("#") || line.equals("")) continue;
				if (line.equalsIgnoreCase("[simulation]")) isSim = true;
				else if (line.equalsIgnoreCase("[agents]")) isSim = false;
				else {
					int index = line.indexOf(":");
					String addressString = null;
					int port = DEFAULT_CLIENT_PORT;
					try {
						if (index==-1) addressString = line;
						else {
							addressString = line.substring(0,index);
							port = Integer.parseInt(line.substring(index+1));
						}
						InetAddress address = InetAddress.getByName(addressString);
						result.add(new ClientInfo(isSim,address,port));
					}
					catch (UnknownHostException e) {
						System.err.println("Cannot find address \""+addressString+"\"");
					}
					catch (NumberFormatException e) {
						System.err.println("Bad port number in "+line);
					}
				}
			}
		} while (line!=null);
		return (ClientInfo[])result.toArray(new ClientInfo[result.size()]);
	}

	private static TeamInfo[] loadTeams(String teamsDirName) throws IOException {
		File teamsDir = new File(teamsDirName);
		if (!teamsDir.isDirectory()) {
			System.err.println("Error loading teams: "+teamsDirName+" is not a directory");
			return null;
		}
		Collection result = new ArrayList();
		File[] subdirs = teamsDir.listFiles(new FileFilter() {public boolean accept(File f) {return f.isDirectory();}});
		for (int i=0;i<subdirs.length;++i) {
			// Make sure commandlines.txt exists
			File commandsFile = new File(subdirs[i].getAbsolutePath()+File.separator+"commandlines.txt");
			if (!commandsFile.exists()) {
				System.err.println("WARNING: Directory "+subdirs[i].getAbsolutePath()+" does not contain \"commandlines.txt\"");
				continue;
			}
			File[] allFiles = subdirs[i].listFiles(new FileFilter() {public boolean accept(File f) {return !f.getName().equals("commandlines.txt");}});
			Collection allCommands = new ArrayList();
			BufferedReader in = new BufferedReader(new FileReader(commandsFile));
			String line = null;
			do {
				line = in.readLine();
				if (line!=null) {
					line = line.trim();
					if (line.startsWith("#")) continue;
					allCommands.add(line);
				}
			} while (line!=null);
			in.close();
			String[] commands = (String[])allCommands.toArray(new String[allCommands.size()]);
			String[] files = new String[allFiles.length];
			for (int j=0;j<files.length;++j) files[j] = allFiles[j].getAbsolutePath();
			result.add(new TeamInfo(commands,files));
		}
		return (TeamInfo[])result.toArray(new TeamInfo[result.size()]);
	}

	private static class ClientInfo {
		boolean isSimulation;
		InetAddress address;
		int port;

		public ClientInfo(boolean sim, InetAddress a, int p) {
			isSimulation = sim;
			address = a;
			port = p;
		}

		public String toString() {
			return address.getHostName()+":"+port+" "+(isSimulation?"running simulation":"running agents");
		}
	}

	private static class TeamInfo {
		String[] commands;
		String[] files;

		public TeamInfo(String[] c, String[] f) {
			commands = c;
			files = f;
		}
	}
}
