package viewer.object;

import java.util.*;
import rescuecore.RescueConstants;

public abstract class PointObject extends MotionlessObject {
  public PointObject(int id) { super(id); }

  private int m_x;
  private int m_y;
  
  public int x() { return m_x; }
  public int y() { return m_y; }

  public void setX(int value) { m_x = value; }
  public void setY(int value) { m_y = value; }

  public void input(String property, int[] value) {
      if ("X".equals(property)) {
          setX(value[0]);
      }
      if ("Y".equals(property)) {
          setY(value[0]);
      }
      super.input(property, value);
  }

  public abstract HashSet links();
}
