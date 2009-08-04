package firesimulator.kernel;

import firesimulator.io.RIO;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.ArrayList;

import firesimulator.simulator.Simulator;
import firesimulator.util.Configuration;
import firesimulator.world.World;


public class RealKernel implements Kernel{

	private RIO rio;
	private String host;
	private int port;
	private World world;
	private int id;

	public RealKernel(World world,String host,int port) {
		this.world=world;
		this.host=host;
		this.port=port;		
	}
	
	public void establishConnection(){
		InetAddress ad=null;
		try{
			ad= InetAddress.getByName(host);
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		rio =new RIO(ad,port);
		int[] data=rio.connect(world);
		id = data[3];
                System.out.println("Connected to kernel. Simulator ID is " + id);
		if(Configuration.isActive("store")){
			System.out.println("storing initial data in "+Configuration.getValue("store"));
			FileOutputStream fos;
			try {
				File f=new File(Configuration.getValue("store"));
				if(f.exists())f.delete();
				fos = new FileOutputStream(f);	
				DataOutputStream dos=new DataOutputStream(fos);		
				for(int i=0;i<data.length;i++){
					dos.writeInt(data[i]);
				}
				dos.close();
				fos.close();			
			} catch (Exception e1) {
				System.out.println("storing was not sucesfull");
				e1.printStackTrace();
			}
		}
	}
	
	public void signalReadyness(){
		rio.sendReadyness(id);		
	}
	
	public boolean waitForNextCycle(){
		rio.receiveCommands(world);
		return true;
	}
	
	public void sendUpdate(){
		//rio.sendUpdate(world.getUpdates());
		Collection all = new ArrayList();
		all.addAll(world.getBuildings());
		all.addAll(world.getFirebrigades());
		rio.sendUpdate(id,world.getTime(),all);
		world.clearUpdates();
	}
	
	public void receiveUpdate(){
		rio.receiveUpdate(world);
	}

	public void register(Simulator sim) {}
	
}
