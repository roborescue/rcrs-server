package traffic.object;

import java.util.*;

public abstract class MotionlessObject extends RealObject {
  public MotionlessObject(int id) { super(id); }

  public MotionlessObject motionlessPosition() { return this; }
  public abstract boolean isAdjacentTo(MotionlessObject obj);
}
