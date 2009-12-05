package firesimulator.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author tn
 *
 */
public class UDPIO extends IO {
    private static final Log LOG = LogFactory.getLog(UDPIO.class);
	
	int kernelPort;
	InetAddress kernelIP;
	DatagramSocket socket;
	InetAddress lastIP;
	int lastPort;
	LinkedList pktList;
	
	short ludpID=0;
	
	public UDPIO(InetAddress kernelIP, int kernelPort){
		pktList=new LinkedList();
		this.kernelIP=kernelIP;
		this.kernelPort=kernelPort;
		try{	socket=new DatagramSocket();}
		catch(Exception e){
                    LOG.fatal("UDPIO constructor failed", e);
                    System.exit(1);
		}
	}
	
	private LUDPMessage getMessage(int id){
		LUDPMessage m;
		for(Iterator it=pktList.iterator();it.hasNext();)
			if( id==(m=(LUDPMessage)it.next()).getId()) return m;
		return null; 
	}
	
	public void send(byte[] ludpBody){
		ByteArrayOutputStream baos;
		DataOutputStream dos;
		final short packageID = ludpID++;
		int MAX_BODY_SIZE=PACKAGE_SIZE-8;
		final short partitions= (short)((ludpBody.length+MAX_BODY_SIZE-1)/MAX_BODY_SIZE);
		int offset=0;
		try{
			for(short n=0;n<partitions;n++){
				int size=Math.min(ludpBody.length-offset,MAX_BODY_SIZE);
				baos=new ByteArrayOutputStream();
				dos=new DataOutputStream(baos);
				dos.writeShort(0x0008);
				dos.writeShort(packageID);
				dos.writeShort(n);
				dos.writeShort(partitions);
				dos.write(ludpBody,offset,size);
				dos.close();
				byte[] data=baos.toByteArray();
				socket.send(new DatagramPacket(data,data.length,kernelIP,kernelPort));
				offset+=size;
			}
		}catch(Exception e){
                    LOG.fatal("UDPIO failed to send bytes", e);
                    System.exit(1);	 	
		}
	}
	
	public byte[] receiveImpl(){
		try{
			LUDPMessage m;
			do{
				byte[] buffer=new byte[PACKAGE_SIZE];
				DatagramPacket pkt = new DatagramPacket(buffer,buffer.length);
				socket.receive(pkt);
				lastIP=pkt.getAddress();
				lastPort=pkt.getPort();
				int ludpID = (short) ((((int) buffer[2] & 0xff) << 8) | ((int) buffer[3] & 0xff));
				int nth    = (short) ((((int) buffer[4] & 0xff) << 8) | ((int) buffer[5] & 0xff));
				int total  = (short) ((((int) buffer[6] & 0xff) << 8) | ((int) buffer[7] & 0xff));
                                byte[] packetData = pkt.getData();
				byte[] body = new byte[packetData.length - 8];
                                System.arraycopy(packetData, 8, body, 0, body.length); //getIntArray(pkt.getData(), 8, pkt.getLength());
				m=getMessage(ludpID);
				if(m==null) pktList.add(m=new LUDPMessage(ludpID,total));
				m.store(body,nth);
			}while(!m.isComplete());
			pktList.remove(m);
			return udpsToLudp(m.getParts());
		}catch(Exception e){
                    LOG.fatal("UDPIO failed to receive bytes", e);
                    System.exit(1);
		}
		return null;
	}

	private byte[] udpsToLudp(byte[][] udps) {
		int size = 0;
	   	for (int i = 0;  i < udps.length;  i ++)
			size += udps[i].length;
	   	byte[] result = new byte[size];
	   	for (int i = 0, pos = 0;  i < udps.length;  i++) {
			int len = udps[i].length;
		 	System.arraycopy(udps[i], 0, result, pos, len);
		 	pos += len;
	   	}
		return result;
	}
}
