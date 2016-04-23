package sceat.domain.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class New {

	public static <E> List<E> arrayList() {
		return new ArrayList<E>();
	}

	public static <E> Collection<E> coll() {
		return new ArrayList<E>();
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> arrayList(E... values) {
		return Arrays.asList(values);
	}

	public static <K, V> Map<K, V> map() {
		return new HashMap<K, V>();
	}

	public static <E> Set<E> set() {
		return new HashSet<E>();
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
