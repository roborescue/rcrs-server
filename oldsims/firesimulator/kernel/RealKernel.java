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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class RealKernel implements Kernel{
    private static final Log LOG = LogFactory.getLog(RealKernel.class);

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
                    LOG.fatal("Couldn't look up server address", e);
                    System.exit(1);
		}
		rio = new RIO(ad,port);
		id = rio.connect(world);
                LOG.info("Connected to kernel. Simulator ID is " + id);
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
