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

  public void input(int property, int[] value) {
    switch(property) {
    default: super.input(property, value); break;
    case RescueConstants.PROPERTY_X: setX(value[0]);  break;
    case RescueConstants.PROPERTY_Y: setY(value[0]);  break;
    }
  }

  public abstract HashSet links();
}
