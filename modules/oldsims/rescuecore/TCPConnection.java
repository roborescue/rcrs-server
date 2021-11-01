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
   The TCPSocket class sends messages via TCP
 */
public class TCPConnection implements Connection {
	private Socket socket;
    private List q;
    private ReadThread read;
    private IOException toThrow;
	private InetAddress destination;
	private int port;
	private OutputStream out;

    /**
       Generate a new TCPSocket with a given destination
	   @param destination The target machine
	   @param port The target port
	   @throws SocketException if something goes wrong
	*/
    public TCPConnection(InetAddress destination, int port) throws SocketException, IOException {
		socket = new Socket(destination,port);
		socket.setSoTimeout(1000);
		q = new LinkedList();
		toThrow = null;
		read = new ReadThread();
		read.start();
		this.destination = destination;
		this.port = port;
		out = socket.getOutputStream();
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
		try {
			out.close();
			socket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void send(byte[] bytes) throws IOException {
		out.write((byte)((bytes.length>>24)&0xFF));
		out.write((byte)((bytes.length>>16)&0xFF));
		out.write((byte)((bytes.length>>8)&0xFF));
		out.write((byte)(bytes.length&0xFF));
		out.write(bytes);
		out.flush();
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
			return (byte[])q.remove(0);
		}
    }

    private class ReadThread extends Thread {
		private boolean running;
		private boolean alive;
		private final Object aliveLock = new Object();
		private InputStream in;

		ReadThread() throws IOException {
			running = true;
			alive = true;
			in = socket.getInputStream();
		}

		public void kill() {
			running = false;
			synchronized(aliveLock) {
				while (alive) try {aliveLock.wait(1000);} catch (InterruptedException e) {}
			}
		}

		public void run() {
			while (running) {
				try {
					int length = in.read()<<24 | in.read()<<16 | in.read()<<8 | in.read();
					if (length<0) {
                                            running = false;
                                            continue;
                                        }
					byte[] data = new byte[length];
					int count = 0;
					while (count<length) {
						int amount = in.read(data,count,length-count);
						count += amount;
					}
					synchronized(q) {
						q.add(data);
						q.notifyAll();
					}
				}
				catch (InterruptedIOException e) {}
				catch (SocketException e) {
					running = false;
				}
				catch (IOException e) {
					e.printStackTrace();
					synchronized(q) {
						toThrow = e;
					}
				}
			}
			try {
				in.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			synchronized(aliveLock) {
				alive = false;
				aliveLock.notifyAll();
			}
		}
    }
}
