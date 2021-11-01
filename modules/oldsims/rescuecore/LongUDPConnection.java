/*
 * Last change: $Date: 2005/06/14 21:55:50 $
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

package rescuecore;

import java.net.*;
import java.util.*;
import java.io.*;

/**
   The LongUDPConnection class encapsulates the sending and receiving of LongUDPMessages to and from the kernel. Messages are received asynchronously and stored in a queue.
 */
public class LongUDPConnection implements Connection {
    private short nextID;
    private DatagramSocket socket;
    private List q;
    private ReadThread read;
    private IOException toThrow;
	private InetAddress destination;
	private int port;

    private final static long WAIT = 10000;

    /**
       Generate a new LongUDPConnection
	   @throws SocketException if something goes wrong
	*/
    public LongUDPConnection() throws SocketException {
		this(null,-1);
	}

    /**
       Generate a new LongUDPConnection with a given destination
	   @param destination The target machine
	   @param port The target port
	   @throws SocketException if something goes wrong
	*/
    public LongUDPConnection(InetAddress destination, int port) throws SocketException {
		nextID = 0;
		socket = new DatagramSocket();
		socket.setSoTimeout(1000);
		//	socket.setReceiveBufferSize(1000000);
		q = new LinkedList();
		toThrow = null;
		read = new ReadThread();
		read.start();
		this.destination = destination;
		this.port = port;
		//		System.err.println("Socket opened listening on port "+socket.getLocalPort());
    }

	public int getLocalPort() {
		return socket.getLocalPort();
	}

    /**
       Close the socket
	*/
    public void close() {
		read.kill();
		socket.close();
    }

    public void send(byte[] bytes) throws IOException {
		if (destination==null || port < 0) throw new IOException("No destination given");
		send(new LongUDPMessage(bytes),destination,port);
	}

    /**
       Send a message
       @param msg The LongUDPMessage to send
       @param destination The destination address
       @param port The destination port
       @throws IOException if something goes wrong during sending
	*/
    public void send(LongUDPMessage msg, InetAddress destination, int port) throws IOException {
		LongUDPFragment[] fragments = msg.fragment(nextID++);
		//		System.out.println("Sending message to "+destination+":"+port);
		for (int i=0;i<fragments.length;++i) {
			//			System.out.print("Sending fragment "+(i+1)+" of "+fragments.length+"...");
			byte[] buffer = fragments[i].toByteArray();
			//			Handy.printBytes(buffer);
			DatagramPacket packet = new DatagramPacket(buffer,buffer.length,destination,port);
			packet.setLength(buffer.length);
			try {
				socket.send(packet);
				//				System.out.println("sent");
			}
			catch (InterruptedIOException e) {
				System.out.println("send interrupted");
				--i;
			}
		}
    }

    public byte[] receive(int timeout) throws IOException, InterruptedException {
		synchronized(q) {
			if (toThrow!=null) throw toThrow;
			if (q.size()==0) {
				if (timeout<1) q.wait();
				else q.wait(timeout);
			}
			if (toThrow!=null) throw toThrow;
			if (q.size()==0) return null;
			return ((LongUDPMessage)q.remove(0)).getData();
		}
    }

    private class ReadThread extends Thread {
		private boolean running;
		private boolean alive;
		private final Object aliveLock = new Object();
		private Set partial;
		private long lastCheck;

		ReadThread() {
			running = true;
			alive = true;
			partial = new HashSet();
		}

		public void kill() {
			running = false;
			synchronized(aliveLock) {
				while (alive) try {aliveLock.wait(1000);} catch (InterruptedException e) {}
			}
		}

		public void run() {
			byte[] input = new byte[65536];
			lastCheck = System.currentTimeMillis();
			DatagramPacket packet = new DatagramPacket(input,input.length);
			while (running) {
				try {
					socket.receive(packet);
					//					System.out.println("Received packet of length "+packet.getLength());
					byte[] packetData = packet.getData();
					byte[] result = new byte[packet.getLength()];
					System.arraycopy(packetData,0,result,0,result.length);
					LongUDPFragment fragment = new LongUDPFragment(result);
					//					System.out.println("Received "+fragment);
					addFragment(fragment);
				}
				catch (InterruptedIOException e) {}
				catch (IOException e) {
					e.printStackTrace();
					synchronized(q) {
						toThrow = e;
					}
				}
				if (System.currentTimeMillis()>lastCheck+WAIT) checkForTimeouts();
			}
			synchronized(aliveLock) {
				alive = false;
				aliveLock.notifyAll();
			}
		}

		private void addFragment(LongUDPFragment fragment) {
			boolean found = false;
			for (Iterator it = partial.iterator();it.hasNext()&&!found;) {
				PartialMessage next = (PartialMessage)it.next();
				if (next.id==fragment.getID()) {
					found = true;
					if (next.addFragment(fragment)) {
						finishedMessage(next.fragments);
						it.remove();
					}
					//										System.out.println("New fragment for message "+next.id+". Fragment size: "+fragment.getData().length+" - now have "+next.received+" of "+next.total+" fragments with a total size of "+next.size);
					if (next.total > 10) System.out.println(next.received+"/"+next.total);
				}
			}
			if (!found) {
				PartialMessage pm = new PartialMessage(fragment.getID(),fragment.getTotal());
				//								System.out.println("Started receiving message "+pm.id);
				if (pm.addFragment(fragment)) {
					finishedMessage(pm.fragments);
				}
				else {
					partial.add(pm);
				}
			}
		}

		private void finishedMessage(LongUDPFragment[] fragments) {
			//						System.out.println("Finished receiving message "+fragments[0].getID());
			synchronized(q) {
				q.add(LongUDPMessage.defragment(fragments));
				q.notifyAll();
				//								System.err.println("Listening on port "+socket.getLocalPort()+" receive queue size: "+q.size());
			}
		}

		private void checkForTimeouts() {
			//	    System.out.println("Checking for timeouts");
			for (Iterator it = partial.iterator();it.hasNext();) {
				PartialMessage next = (PartialMessage)it.next();
				//		System.out.println("Checking "+next+" for timeout at time "+System.currentTimeMillis());
				if (System.currentTimeMillis() > next.last+WAIT) {
					System.out.println("Message "+next.id+" timed out - we received "+next.received+" of "+next.total+" packets");
					it.remove();
				}
			}
			lastCheck = System.currentTimeMillis();
		}

		private class PartialMessage {
			int id;
			int total;
			int received;
			long last;
			int size;
			LongUDPFragment[] fragments;

			PartialMessage(int id, int total) {
				this.id = id;
				this.total = total;
				this.received = 0;
				this.last = System.currentTimeMillis();
				fragments = new LongUDPFragment[total];
				size = 0;
			}

			public String toString() {
				return "Partial message "+id+": received "+received+" of "+total+", last fragment at time "+last;
			}

			/**
			   Returns true iff this message is now complete
			*/
			public boolean addFragment(LongUDPFragment fragment) {
				if (fragment.getID()==id) {
					if (fragments[fragment.getNumber()]==null) {
						fragments[fragment.getNumber()] = fragment;
						++received;
						last = System.currentTimeMillis();
						size += fragment.getData().length;
						if (received == total) return true;
					}
				}
				return false;
			}
		}
    }
}
