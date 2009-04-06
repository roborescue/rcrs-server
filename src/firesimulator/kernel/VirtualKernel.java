package firesimulator.kernel;

import firesimulator.io.IOConstans;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import firesimulator.kernel.viewer.ViewerFrame;
import firesimulator.simulator.Simulator;
import firesimulator.util.Configuration;
import firesimulator.world.World;

/**
 * @author tn
 *
 */
public class VirtualKernel implements Kernel,IOConstans {
	
	private World world;
	protected ViewerFrame viewerFrame;
	private Simulator sim;
	private int goalCycle=1;
	
	public VirtualKernel(World world){
		System.out.println("configuration environment mode");		
		this.world=world;
		viewerFrame=createFrame();
	}
    
    public ViewerFrame createFrame(){
        return new ViewerFrame(world,this,Configuration.getValue("virtual"));
    }

    public ViewerFrame getViewerFrame(){
        return viewerFrame;
    }

	public void establishConnection() {
		System.out.println("scenery file is \""+Configuration.getValue("virtual")+"\"");
		try {
			File file=new File(Configuration.getValue("virtual"));
			FileInputStream fis=new FileInputStream(file);
			DataInputStream is=new DataInputStream(fis);
			long size=file.length()/4;
			int[] data=new int[(int)size];
			for(int i=0;i<size;i++){
				data[i]=is.readInt();
			}
			System.out.println("creating world model");
			world.processUpdate(data,2,INIT_TIME);
			world.printSummary();
		} catch (Exception e) {
			System.out.println("unable to load scenery file. exiting.");
			System.exit(1);
		}
	}


	public void signalReadyness() {	
        viewerFrame.initDone();
	}
	
	public boolean waitForNextCycle() {
		viewerFrame.informProgress(world.getTime(),goalCycle);
		if(world.getTime()==goalCycle) {
			viewerFrame.updateMap();	
			return false;
		} 
		return true;
	}


	public void sendUpdate() {
	}


	public void receiveUpdate() {
		world.setTime(world.getTime()+1);
		viewerFrame.em.nextCycle();
	}

	public int getGoalCycle(){
		return goalCycle;
	}

	private void setGoalCycle(int goal){
		goalCycle=goal;
	}

	public void jumpTo(int i,boolean reset) {
		if(sim==null)return;
		//for autorun
		if(world.getTime()>=i||reset)
			sim.reset();
		setGoalCycle(i);
		sim.goLoop();
	}
	
	public void register(Simulator sim) {
		this.sim=sim;
		viewerFrame.register(sim);
	}

}
