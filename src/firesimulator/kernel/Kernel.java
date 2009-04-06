package firesimulator.kernel;

import firesimulator.simulator.Simulator;



/**
 * @author tn
 *
 */
public interface Kernel {

	public void register(Simulator sim);

	public void establishConnection();
	
	public void signalReadyness();
	
	public boolean waitForNextCycle();
	
	public void sendUpdate();
	
	public void receiveUpdate();	

}
