package firesimulator.kernel;

import firesimulator.io.IOConstans;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import firesimulator.kernel.viewer.ViewerFrame;
import firesimulator.simulator.Simulator;
import firesimulator.util.Configuration;
import firesimulator.world.World;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author tn
 *
 */
public class VirtualKernel implements Kernel,IOConstans {
    private static final Log LOG = LogFactory.getLog(VirtualKernel.class);
	
	private World world;
	protected ViewerFrame viewerFrame;
	private Simulator sim;
	private int goalCycle=1;
	
	public VirtualKernel(World world){
            LOG.info("configuration environment mode");		
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
            LOG.fatal("unable to load scenery file. exiting.");
            System.exit(1);
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
