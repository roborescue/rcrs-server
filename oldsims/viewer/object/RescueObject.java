package viewer.object;

import viewer.*;

/** The base class of each RoboCupRescue's object */
public abstract class RescueObject implements Constants {
  public final int id;

  public RescueObject(int id) { this.id = id; }
  public abstract int type();

  /** sets a property
   *  @param property an integer indicating a type of the property
   *  @param value    value of the property
   */
  public void input(int property, int[] value) { /* nothing to do */ }

  public int hashCode() { return id; }

  public String toString() { return new StringBuffer().append(super.toString()).append(" (ID:").append(id).append(")").toString(); }
}
