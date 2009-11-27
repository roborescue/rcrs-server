package traffic.object;

public class Car extends Humanoid {
  public Car(int id) { super(id); }

  public double maxAcceleration() { return MAX_ACCELERATION; }
  public double maxVelocity()     { return MAX_VELOCITY; }

  // ------------------------------------------------------ Obstruction
  public double minSafeDistance() { return MIN_SAFE_DISTANCE_BETWEEN_CARS; }
}
