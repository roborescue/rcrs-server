package traffic;

import traffic.object.*;

public interface Obstruction {
    static final Obstruction DUMMY_OBSTRUCTION = new Obstruction() {
        public double minSafeDistance() { return 0; }
        public double lengthToForwardOfLane() { return 0; }
        public MotionlessObject motionlessPosition() { return null; }
      };

      static final Obstruction BLOCKED_BUILDING = new Obstruction() {
          public double minSafeDistance() { return 0; }
          public double lengthToForwardOfLane() { return 0; }
          public MotionlessObject motionlessPosition() { return null; }
        };

  double minSafeDistance();
  // CAUTION: rename this method's name, for example, lengthFromBackOfLane().
  double lengthToForwardOfLane();
  MotionlessObject motionlessPosition();
}
