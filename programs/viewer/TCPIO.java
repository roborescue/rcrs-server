/*
 Author: Cameron Skinner
 May 2005: Modified UdpIO.java to implement TCP communication instead of TCP.
*/

package viewer;

import java.io.*;
import java.net.*;
import java.util.*;
import rescuecore.RescueConstants;

public class TCPIO extends IO {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public TCPIO(InetAddress kernelAddress, int kernelPort) {
		try{
			socket  = new Socket(kernelAddress, kernelPort);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) { e.printStackTrace();  System.exit(1); }
		//		new File(Main.ACTION_LOG_FILE_NAME).delete();
	}

	protected byte[] receive() {
		try {
			int byte0 = in.read();
			int byte1 = in.read();
			int byte2 = in.read();
			int byte3 = in.read();
			int length = byte0<<24 | byte1<<16 | byte2<<8 | byte3;
			//			System.out.println("Received: "+Integer.toHexString(byte0)+" "+Integer.toHexString(byte1)+" "+Integer.toHexString(byte2)+" "+Integer.toHexString(byte3));
			//			System.out.println("Length = "+length);
			int total = 0;
			byte[] result = new byte[length];
			while (total < length) {
				total += in.read(result,total,length-total);
				//				System.out.println("Read "+total+" of "+length+" bytes");
			}
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(result));
			//			int header = dis.readInt();
			//			if (header == RescueConstants.COMMANDS) {
			//				saveActionLogFile(result);
			//			}
			return result;
		} catch (Exception e) { e.printStackTrace();  System.exit(1); }
		return null;
	}

	protected void send(byte[] body) {
		try {
			int len = body.length;
			//			out.writeInt(len);
			int byte0 = (len>>24) & 0xFF;
			int byte1 = (len>>16) & 0xFF;
			int byte2 = (len>>8) & 0xFF;
			int byte3 = len & 0xFF;
			//			System.out.println("dataToSend length: "+len);
			//			System.out.println(Integer.toHexString(byte0)+" "+Integer.toHexString(byte1)+" "+Integer.toHexString(byte2)+" "+Integer.toHexString(byte3));
			out.writeByte((byte)byte0);
			out.writeByte((byte)byte1);
			out.writeByte((byte)byte2);
			out.writeByte((byte)byte3);
			out.write(body);
			out.flush();
			//			System.out.println("sending complete");
		} catch (Exception e) { e.printStackTrace();  System.exit(1); }
	}

	public void close() {
		try
			{
				in.close();
				out.close();
				socket.close();
			}
		catch (IOException e)
			{
				e.printStackTrace(); System.exit(1);
			}
	}

	/*
	private void saveActionLogFile(byte[] data) {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(Main.ACTION_LOG_FILE_NAME, true));
			dos.writeInt(data.length);
			dos.write(data, 0, data.length);
			dos.flush();
		} catch (Exception e) { Util.myassert(false, e); }
		finally	{
			try	{
				if (dos!=null) dos.close();
			}
			catch (IOException e) {
				Util.myassert(false, e);
			}
		}
	}
	*/

	/*
	  private class LoggedOutputStream extends OutputStream {
	  private OutputStream down;
	  private StringBuffer buffer;
	  private int count;

	  public LoggedOutputStream(OutputStream down) {
	  this.down = down;
	  buffer = new StringBuffer();
	  count = 0;
	  }

	  public void write(int i) throws IOException {
	  buffer.append(Integer.toHexString(i));
	  buffer.append(" ");
	  ++count;
	  if (count==4) {
	  System.out.println(buffer.toString());
	  buffer.setLength(0);
	  count = 0;
	  }
	  down.write(i);
	  }

	  public void flush() throws IOException {
	  if (count!=0) {
	  System.out.println(buffer.toString());
	  buffer.setLength(0);
	  count = 0;
	  }
	  System.out.println("Flushed");
	  down.flush();
	  }

	  public void close() throws IOException {
	  if (count!=0) {
	  System.out.println(buffer.toString());
	  buffer.setLength(0);
	  count = 0;
	  }
	  System.out.println("Flushed");
	  down.close();
	  }
	  }
	*/
}
