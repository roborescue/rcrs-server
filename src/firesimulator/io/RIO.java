package firesimulator.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;

import firesimulator.world.RescueObject;
import firesimulator.world.World;

/**
 * @author tn
 * Modified by Cameron Skinner
 *
 */
public class RIO implements IOConstans{
	private IO io;

	public RIO(InetAddress kernelIP, int kernelPort){
		io=new TCPIO(kernelIP,kernelPort);
	}
	
	public int[] connect(World world){
		System.out.print("sending SK_CONNECT..");
		sendConnect();
		System.out.print("ok\nwaiting for KS_CONNECT_OK..");
		int[] data=receiveConnectOK(world);
		System.out.println("ok");
		return data;
	}
	
	public void receiveUpdate(World world){
		int[]data;
		data=receive();   
		if(data[0]!=UPDATE){
			System.out.println("warning: received not an UPDATE");
		}
		world.processUpdate(data,3,data[2]); // Skip the header, size and time
	}
	
	public void sendReadyness(int id){
		System.out.print("sending SK_ACKNOWLEDGE..");
		sendAcknowledge(id);
		System.out.println("ok");
	}
	
	public void sendConnect(){
		send(SK_CONNECT,new byte[]{0,0,0,0});
	}
	
	public void sendAcknowledge(int id){
		byte[] b = new byte[4];
		b[0] = (byte)((id>>24)&0xFF);
		b[1] = (byte)((id>>16)&0xFF);
		b[2] = (byte)((id>>8)&0xFF);
		b[3] = (byte)(id&0xFF);
		send(SK_ACKNOWLEDGE,b);
	}
	
	public void receiveCommands(World world){
		int[] data=receive();
		if(data[0]!=COMMANDS){
			System.out.println("warning: received not a COMMANDS");
		}
		world.processCommands(data);
	}
	
	
	public int[] receiveConnectOK(World world){
		int[]data;
		data=receive();
		if(data[0]!=KS_CONNECT_OK){
			System.out.println("warning: received not an KS_CONNECT_OK");
		}
		world.processUpdate(data,3,INIT_TIME); // Skip the header, size and simulator id
		return data;
	}
	
	private int[] receive(){
		return io.receive();
	}
	
	public void sendUpdate(int id, int time, Collection objects){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(id);
			dos.writeInt(time);
			for(Iterator i=objects.iterator();i.hasNext();){
				RescueObject o=(RescueObject)i.next();
				o.encode(dos);		
			}
			dos.writeInt(TYPE_NULL);
		  	dos.close();
		  	send(SK_UPDATE, baos.toByteArray());
		  	baos.close();
		} catch (Exception e) {
			e.printStackTrace();  
			System.exit(1); 
		}
	}
	
	private void send (int header, byte[] body) {
		try{
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			DataOutputStream dos=new DataOutputStream(baos);
			dos.writeInt(header);
			dos.writeInt(body.length);
			dos.write(body);
			dos.writeInt(HEADER_NULL);
			dos.close();
			byte[] ludpBody=baos.toByteArray();
			baos.close();
			io.send(ludpBody);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}

}
