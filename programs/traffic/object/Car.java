package traffic.object;

import rescuecore.RescueConstants;

public class Car extends Humanoid {
  public Car(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_CAR; }

  public double maxAcceleration() { return MAX_ACCELERATION; }
  public double maxVelocity()     { return MAX_VELOCITY; }

  // ------------------------------------------------------ Obstruction
  public double minSafeDistance() { return MIN_SAFE_DISTANCE_BETWEEN_CARS; }
}
