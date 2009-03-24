/*
 Author: Cameron Skinner
 May 2005: Modified IO.java to implement TCP communication instead of TCP.
*/

package traffic;

import java.io.*;
import java.net.*;
import java.util.*;
import traffic.object.*;

public class TCPIO extends IO {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public TCPIO(InetAddress kernelAddress, int kernelPort) {
		super(kernelAddress,kernelPort);
		try{
			socket  = new Socket(kernelAddress, kernelPort);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) { e.printStackTrace();  System.exit(1); }
	}

	public byte[] receive() {
		try {
			//		  int length = in.readInt();
			int length = in.read();
			length = length<<8 | in.read();
			length = length<<8 | in.read();
			length = length<<8 | in.read();
			int total = 0;
			byte[] result = new byte[length];
			while (total < length) {
				total += in.read(result,total,length-total);
			}
			return result;
		} catch (Exception e) { e.printStackTrace();  System.exit(1); }
		return null;
	}

	public void send(byte[] body) {
		try {
			int len = body.length;
			out.writeByte((byte)(len>>24)&0xFF);
			out.writeByte((byte)(len>>16)&0xFF);
			out.writeByte((byte)(len>>8)&0xFF);
			out.writeByte((byte)len&0xFF);
			out.write(body);
			out.flush();
		} catch (Exception e) { e.printStackTrace();  System.exit(1); }
	}

	public void close() {
		try {
			in.close();
			out.close();
			socket.close();
		}
		catch (IOException e) {
			e.printStackTrace(); System.exit(1);
		}
	}
}
