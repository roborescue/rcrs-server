/*
  Modified LIO to use TCP instead of LongUDP by Cameron Skinner
*/

package firesimulator.io;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author tn
 *
 */
public class TCPIO extends IO {
    private static final Log LOG = LogFactory.getLog(TCPIO.class);

	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	LinkedList pktList;
	
	public TCPIO(InetAddress kernelIP, int kernelPort){
		pktList=new LinkedList();
		try {
			socket=new Socket(kernelIP,kernelPort);
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		}
		catch(Exception e){
                    LOG.fatal("TCPIO constructor failed", e);
                    System.exit(1);
		}
	}
	
	public void send(byte[] body){
		try {
			//			out.writeInt(body.length);
			out.write((byte)(body.length>>24)&0xFF);
			out.write((byte)(body.length>>16)&0xFF);
			out.write((byte)(body.length>>8)&0xFF);
			out.write((byte)body.length&0xFF);
			out.write(body,0,body.length);
			out.flush();
		} 
		catch(Exception e){
                    LOG.fatal("TCPIO failed to send bytes", e);
                    System.exit(1);	 	
		}
	}
	
	public byte[] receiveImpl(){
		try{
			//			int length = in.readInt();
			int length = in.read();
			length = length<<8 | in.read();
			length = length<<8 | in.read();
			length = length<<8 | in.read();
			if (length<0) return null;
			byte[] buffer = new byte[length];
			int total = 0;
			while (total < length) {
				total += in.read(buffer,total,length-total);
			}
                        return buffer;
		}catch(Exception e){
                    LOG.fatal("TCPIO failed to received bytes", e);
                    System.exit(1);
		}
		return null;
	}
}
