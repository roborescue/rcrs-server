package traffic.object;

import rescuecore.RescueConstants;

public class Civilian extends Humanoid {
  public Civilian(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_CIVILIAN; }

  public double maxAcceleration() { return MAX_CIV_ACCELERATION; }
  public double maxVelocity()     { return MAX_CIV_VELOCITY; }

  // ------------------------------------------------------ Obstruction
  public double minSafeDistance()
    { return MIN_SAFE_DISTANCE_BETWEEN_CIVILIAN; }
}
