package traffic.object;

import traffic.*;

/** The base class of all RoboCupRescue's objects */
public abstract class RescueObject implements Constants {
  public final int id;

  public RescueObject(int id) { this.id = id; }

  public int hashCode() { return id; }

  public String toString() { return new StringBuffer().append(super.toString()).append(" (ID:").append(id).append(")").toString(); }
}
