package traffic;

import traffic.object.*;

public class Blockade implements Obstruction {
  public final Lane lane;

  public Blockade(Lane lane) { this.lane = lane; }

  public double minSafeDistance() { return 0; }
  public double lengthToForwardOfLane() { return lane.road.length() / 2d; }
  public MotionlessObject motionlessPosition() { return lane.road; }
}
