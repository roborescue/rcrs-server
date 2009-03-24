package traffic;

import traffic.object.*;

public class Destination implements Obstruction {
  public final MotionlessObject destination;
  public final double posEx;

  public Destination(MotionlessObject destination, double posEx) {
    this.destination = destination;
    this.posEx = posEx;
  }

  public double minSafeDistance() { return 0; }
  public double lengthToForwardOfLane() { return posEx; }
  public MotionlessObject motionlessPosition() { return destination; }
}
