package traffic3.objects.area.event;

import traffic3.objects.area.TrafficArea;
import traffic3.objects.TrafficAgent;

/**
 *
 */
public class TrafficAreaEvent {

    private TrafficArea area;
    private TrafficAgent agent;

    /**
     * Constructor.
     * @param ar area
     * @param ag agent
     */
    public TrafficAreaEvent(TrafficArea ar, TrafficAgent ag) {
        area = ar;
        agent = ag;
    }

    /**
     * get agent.
     * @return agent
     */
    public TrafficAgent getAgent() {
        return agent;
    }

    /**
     * get area.
     * @return area
     */
    public TrafficArea getArea() {
        return area;
    }
}
