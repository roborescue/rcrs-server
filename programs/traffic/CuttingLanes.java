package traffic;

import java.util.*;
import traffic.object.*;

public class CuttingLanes implements Obstruction {
  public final List lanes;
  private final double m_lengthToForward;
  private final MotionlessObject m_motionlessPosition;

  public CuttingLanes(List lanes, double lengthToForward, MotionlessObject motionlessPosition) {
    this.lanes = lanes;
    m_lengthToForward = lengthToForward;
    m_motionlessPosition = motionlessPosition;
  }

  public CuttingLanes(Lane lane, double lengthToForward, MotionlessObject motionlessPosition) {
    this.lanes = Collections.singletonList(lane);
    m_lengthToForward = lengthToForward;
    m_motionlessPosition = motionlessPosition;
  }

  public double minSafeDistance() { return 0; }
  public double lengthToForwardOfLane() { return m_lengthToForward; }
  public MotionlessObject motionlessPosition() { return m_motionlessPosition; }
}
