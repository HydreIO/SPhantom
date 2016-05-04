package sceat.domain.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Enum des endroits ou on pourra envoyer les messages
 * 
 * @author MrSceat
 *
 */
public enum RoutingKey {
	SPHANTOM("SP", (byte) 0),
	SYMBIOTE("SY", (byte) 1),
	PROXY("PR", (byte) 2),
	SERVERS("SE", (byte) 3),
	HUBS("HU", (byte) 4);

	private String key;
	private byte value;

	private RoutingKey(String key, byte value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public byte getValue() {
		return value;
	}

	public static List<String> genKeys() {
		RoutingKey[] values = RoutingKey.values();
		List<String> list = new ArrayList<String>();
		for (int x = 0; x < (1 << values.length); x++) {
			StringJoiner sj = new StringJoiner(":");
			for (int y = 0; y < values.length; y++)
				if ((x & (1 << y)) > 0) sj.add(values[y].getKey());
			list.add(sj.toString());
		}
		return list;
	}

	public static String genKey(RoutingKey... keys) {
		Set<RoutingKey> set = new HashSet<RoutingKey>(); // a set can't have duplicated element so if an element can't be added its because the array contain duplicated keys
		if (keys.length != Arrays.stream(keys).filter(set::add).count()) throw new IllegalArgumentException("duplicated element is not allowed in this array !");
		return Arrays.stream(keys).sorted(Comparator.comparingInt(RoutingKey::getValue)).map(RoutingKey::getKey).collect(Collectors.joining(":"));
	}

}