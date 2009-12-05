package traffic;

import java.util.*;
import traffic.object.*;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class Util implements Constants {
    private static final Log LOG = LogFactory.getLog(Util.class);
    
  // -------------------------------------------------------------- Collections
  public static Object getAtRandom(List list) {
    if(ASSERT)Util.myassert(!list.isEmpty(), "list must not be empty");
    return list.get(RANDOM.nextInt(list.size()));
  }

  // ------------------------------------------------------------------- myassert
  /** TODO: replace with myassert premitive in Java1.4 */
  public static void myassert(boolean cond) {
    if (!ASSERT) throw (new Error("Don't use myassert() without \"if(ASSERT)\""));
    if (cond == false) throw (new Error());
  }
  public static void myassert(boolean cond, String message) {
    if (!ASSERT) throw (new Error("Don't use myassert() without \"if(ASSERT)\""));
    if (cond == false) throw (new Error(message));
  }
  public static void myassert(boolean cond, String message, Object obj) {
    if (!ASSERT) throw (new Error("Don't use myassert() without \"if(ASSERT)\""));
    if (cond == false) throw (new Error(message + " : " + obj));
  }
  public static void myassert(boolean cond, String message, double d) {
    if (!ASSERT) throw (new Error("Don't use myassert() without \"if(ASSERT)\""));
    if (cond == false) throw (new Error(message + " : " + d));
  }

  // ------------------------------------------------------------------ profile
  private static HashMap m_nameTimeMap = new HashMap();

  public static void addTime(long startTime, String checkedPoingName) {
    Long time = (Long) m_nameTimeMap.get(checkedPoingName);
    long t = (time != null) ? time.longValue() : 0;
    m_nameTimeMap.put(checkedPoingName, new Long(t + (System.currentTimeMillis() - startTime)));
  }

  public static void printTimes() {
      LOG.debug("Processing time\n\t[rate]\t[sec]\t@ [name]");
    Iterator it;
    it = m_nameTimeMap.values().iterator();
    long totalTime = 0;
    while (it.hasNext())
      totalTime += ((Long) it.next()).longValue();
    it = m_nameTimeMap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry e = (Map.Entry) it.next();
      double time = (double) ((Long) e.getValue()).longValue();
      LOG.debug("\t" + (((int) (time / totalTime * 100d)) / 100d) + "\t" + (time / 1000) + "\t@ " + ((String) e.getKey()));
    }
  }
}
