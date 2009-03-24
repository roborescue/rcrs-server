package viewer;

import java.util.*;

public class Util implements Constants {
  public static void myassert(boolean cond) {
    if (cond == false)
      throw (new Error());
  }

  public static void myassert(boolean cond, String message) {
    if (cond == false)
      throw (new Error(message));
  }

  public static void myassert(boolean cond, Object obj) {
    if (cond == false)
      throw (new Error(obj.toString()));
  }
}
