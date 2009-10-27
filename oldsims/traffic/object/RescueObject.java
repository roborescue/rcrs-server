package traffic.object;

import traffic.*;

/** The base class of all RoboCupRescue's objects */
public abstract class RescueObject implements Constants {
  public final int id;

  public RescueObject(int id) { this.id = id; }
  public abstract String type();

  /** sets a property
   *  @param property a urn indicating a type of the property
   *  @param value    value of the property
   */
  public void input(String property, int[] value) { /* nothing to do */ }

  public int hashCode() { return id; }

  public String toString() { return new StringBuffer().append(super.toString()).append(" (ID:").append(id).append(")").toString(); }
}
