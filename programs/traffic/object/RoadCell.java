package traffic.object;

import java.util.*;
import traffic.*;

public class RoadCell implements Constants {
  public static final RoadCell DUMMY_CELL = new RoadCell(null);
  public final Road road;
  private RoadCell m_back    = DUMMY_CELL;
  private RoadCell m_forward = DUMMY_CELL;

  public RoadCell(Road road) { this.road = road; }
  public RoadCell(Road road, RoadCell back, RoadCell forward) {
    this.road = road;
    setBack(back);
    setForward(forward);
  }

  public void setBack(RoadCell back) {
    if(ASSERT)Util.myassert(back != null, "back of RoadCell must be instance of RoadCell");
    m_back = back;
  }
  public void setForward(RoadCell forward) {
    if(ASSERT)Util.myassert(forward != null, "forward of RoadCell must be instance of RoadCell");
    m_forward = forward;
  }

  public RoadCell back()    { return m_back;    }
  public RoadCell forward() { return m_forward; }

  private static final HashMap LANE_ROAD_CELL_MAP = new HashMap();

  // CAUTION: Be carefull to use a list of RoadCells.  Some of them
  // are loops.  So if you do not care it, it gets into an inifinite
  // loop.
  public static RoadCell roadList(Lane lane) {
    if (lane == null)
      return RoadCell.DUMMY_CELL;
    Lane delegateLane = (Lane) lane.road.lanesTo(lane.forward).get(0);
    RoadCell result = (RoadCell) LANE_ROAD_CELL_MAP.get(delegateLane);
    if (result == null) {
      result = new RoadCell(lane.road);
      LANE_ROAD_CELL_MAP.put(delegateLane, result);
      result   .setBack(roadList(lane.adjacentLaneOfSamePriorityRoadVia(lane.back   )));
      result.setForward(roadList(lane.adjacentLaneOfSamePriorityRoadVia(lane.forward)));
    }
    return result;
  }
}
