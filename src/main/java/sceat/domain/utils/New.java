package sceat.domain.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.commons.util.collection.Set;
import fr.aresrpg.commons.util.map.HashMap;
import fr.aresrpg.commons.util.map.Map;

public class New {

	public static <E> List<E> arrayList() {
		return new ArrayList<>();
	}

	public static <E> Collection<E> coll() {
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> list(E... values) {
		return Arrays.asList(values);
	}

	public static <K, V> Map<K, V> map() {
		return new HashMap<>();
	}

	public static <E> Set<E> set() {
		return new HashSet<>();
	}

	/**
	 * Attention ! si la liste est vide yaura une belle erreur
	 *
	 * @param col
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<? extends T> col) {
		if (col.isEmpty()) throw new IndexOutOfBoundsException("La liste ne doit pas être vide putin !");
		return (T[]) Array.newInstance(col.get(0).getClass(), col.size());
	}

}
