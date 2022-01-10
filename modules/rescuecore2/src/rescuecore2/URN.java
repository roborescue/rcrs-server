package rescuecore2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface URN {
  public int getURNId();

  public String getURNStr();

  public String name();

  static <T extends Enum<?> & URN> Map<Integer, T> generateMap(Class<T> urnEnum) {
    Map<Integer, T> map = new HashMap<>();

    for (T t : urnEnum.getEnumConstants())
      map.put(t.getURNId(), t);
    return Collections.unmodifiableMap(map);
  }

  static <T extends Enum<?> & URN> Map<String, T> generateMapStr(Class<T> urnEnum) {
    Map<String, T> map = new HashMap<>();

    for (T t : urnEnum.getEnumConstants())
      map.put(t.getURNStr(), t);
    return Collections.unmodifiableMap(map);
  }
}