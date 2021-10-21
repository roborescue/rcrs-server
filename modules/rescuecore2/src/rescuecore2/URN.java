package rescuecore2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface URN {
	public int getUrn();
//	public String getStringUrn(); //TODO
	public String name();

	static <T extends Enum<?> & URN> Map<Integer, T> generateMap(
			Class<T> urnEnum) {
		Map<Integer, T> map = new HashMap<>();

		for (T t : urnEnum.getEnumConstants())
			map.put(t.getUrn(), t);
		return Collections.unmodifiableMap(map);
	}
}
