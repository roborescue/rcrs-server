package traffic3.simulator;

import traffic3.manager.WorldManager;

import traffic3.objects.TrafficAgent;

/**
 * The Simulator computes forces for agents.
 */
public class Simulator {
    private WorldManager worldManager;
    private double stepTimeWidth;
    private double stepTime = 0;
    private boolean simulateAsRealTime = false;

    /**
     * Constructor.
     * @param wm world manager
     * @param stw step time width
     */
    public Simulator(WorldManager wm, double stw) {
        worldManager = wm;
        stepTimeWidth = stw;
    }

    /**
     * step.
     */
    public void step() {
        TrafficAgent[] agentList = worldManager.getAgentList();
        long start = System.currentTimeMillis();
        for (int i = 0; i < agentList.length; i++) {
            TrafficAgent agent = agentList[i];
            /*
              TrafficAreaNode dest = agent.getDestination();
              if(dest!=null) {
              // agent.setLocation(dest.getX(), dest.getY(), dest.getZ());
              }
            */
            agent.plan();
        }
        for (int i = 0; i < agentList.length; i++) {
            TrafficAgent agent = agentList[i];
            agent.step(stepTimeWidth);
        }
        long end = System.currentTimeMillis();
        int diff = (int)(stepTimeWidth - (end - start));
        if (simulateAsRealTime && diff > 0) {
            try {
                Thread.sleep(diff);
            }
            catch (InterruptedException exc) {
                exc.printStackTrace();
            }
        }
        stepTime += stepTimeWidth;
    }

    /**
     * set step time.
     * @param st step time
     */
    public void setTime(double st) {
        stepTime = st;
    }

    /**
     * get step time.
     * @return step time
     */
    public double getTime() {
        return stepTime;
    }
}
