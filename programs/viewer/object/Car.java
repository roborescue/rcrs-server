package viewer.object;

import rescuecore.RescueConstants;

public class Car extends Humanoid {
  public Car(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_CAR; }
}
