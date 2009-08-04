package traffic.object;

public abstract class RealObject extends RescueObject {
  public RealObject(int id) { super(id); }

  public abstract int x();
  public abstract int y();

  public abstract MotionlessObject motionlessPosition();
}
