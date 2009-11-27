package traffic.object;

public class Civilian extends Humanoid {
  public Civilian(int id) { super(id); }

  public double maxAcceleration() { return MAX_CIV_ACCELERATION; }
  public double maxVelocity()     { return MAX_CIV_VELOCITY; }

  // ------------------------------------------------------ Obstruction
  public double minSafeDistance()
    { return MIN_SAFE_DISTANCE_BETWEEN_CIVILIAN; }
}
