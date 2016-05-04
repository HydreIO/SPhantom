package sceat.domain.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import sceat.domain.common.system.Log;

public class Try {

	public static boolean debug = false;

	@FunctionalInterface
	public interface TryRunnable<T> {
		T run() throws Throwable;

	}

	@FunctionalInterface
	public interface TryVoidRunnable {
		void run() throws Exception;

		public static TryVoidRunnable empty(TryVoidRunnable t) {
			return t;
		}

		default TryVoidRunnable andThen(TryVoidRunnable runnable) {
			return () -> {
				run();
				runnable.run();
			};
		}
	}

	public static <T> T or(TryRunnable<T> runnable, T defaultValue, boolean stackTrace) {
		try {
			return runnable.run();
		} catch (Throwable e) {
			if (stackTrace) Log.trace(e);
			return defaultValue;
		}
	}

	public static <T> T or(TryRunnable<T> runnable, T defaultValue) {
		return or(runnable, defaultValue, debug);
	}

	public static void orVoid(TryVoidRunnable runnable, boolean stackTrace) {
		try {
			runnable.run();
		} catch (Exception e) {
			if (stackTrace) Log.trace(e);
		}
	}

	/**
	 * Execute actions if exceptions throwed
	 * 
	 * @param runnable
	 * @param stackTrace
	 * @param r
	 *            actions
	 */
	public static void orVoidWithActions(TryVoidRunnable runnable, boolean stackTrace, Runnable r) {
		try {
			runnable.run();
		} catch (Exception e) {
			if (stackTrace) Log.trace(e);
			r.run();
		}

	}

	public static void orVoid(TryVoidRunnable runnable) {
		orVoid(runnable, debug);
	}

	public static <T> T orNull(TryRunnable<T> runnable, boolean stackTrace) {
		return or(runnable, null, stackTrace);
	}

	public static <T> T orNull(TryRunnable<T> runnable) {
		return or(runnable, null);
	}

	public static boolean orFalse(TryRunnable<Boolean> runnable, boolean stackTrace) {
		return or(runnable, false, stackTrace);
	}

	public static boolean orFalse(TryRunnable<Boolean> runnable) {
		return or(runnable, false);
	}

	public static boolean orTrue(TryRunnable<Boolean> runnable, boolean stackTrace) {
		return or(runnable, true, stackTrace);
	}

	public static boolean orTrue(TryRunnable<Boolean> runnable) {
		return or(runnable, true);
	}

	public static Number or0(TryRunnable<Number> runnable, boolean stackTrace) {
		return or(runnable, 0, stackTrace);
	}

	public static Number or0(TryRunnable<Number> runnable) {
		return or(runnable, 0);
	}

	public static <T> Collection<T> orEmpty(TryRunnable<Collection<T>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptyList(), stackTrace);
	}

	public static <T> List<T> orEmptyList(TryRunnable<List<T>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptyList(), stackTrace);
	}

	public static <T> List<T> orEmptyList(TryRunnable<List<T>> runnable) {
		return or(runnable, Collections.emptyList());
	}

	public static <T> Set<T> orEmptySet(TryRunnable<Set<T>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptySet(), stackTrace);
	}

	public static <T> Set<T> orEmptySet(TryRunnable<Set<T>> runnable) {
		return or(runnable, Collections.emptySet());
	}

	public static <T> NavigableSet<T> orEmptyNavigableSet(TryRunnable<NavigableSet<T>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptyNavigableSet(), stackTrace);
	}

	public static <T> NavigableSet<T> orEmptyNavigableSet(TryRunnable<NavigableSet<T>> runnable) {
		return or(runnable, Collections.emptyNavigableSet());
	}

	public static <T> SortedSet<T> orEmptySortedSet(TryRunnable<SortedSet<T>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptySortedSet(), stackTrace);
	}

	public static <T> SortedSet<T> orEmptySortedSet(TryRunnable<SortedSet<T>> runnable) {
		return or(runnable, Collections.emptySortedSet());
	}

	public static <T> Enumeration<T> orEmptyEnumeration(TryRunnable<Enumeration<T>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptyEnumeration(), stackTrace);
	}

	public static <T> Enumeration<T> orEmptyEnumeration(TryRunnable<Enumeration<T>> runnable) {
		return or(runnable, Collections.emptyEnumeration());
	}

	public static <T> Iterator<T> orEmptyIterator(TryRunnable<Iterator<T>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptyIterator(), stackTrace);
	}

	public static <T> Iterator<T> orEmptyIterator(TryRunnable<Iterator<T>> runnable) {
		return or(runnable, Collections.emptyIterator());
	}

	public static <T> ListIterator<T> orEmptyListIterator(TryRunnable<ListIterator<T>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptyListIterator(), stackTrace);
	}

	public static <T> ListIterator<T> orEmptyListIterator(TryRunnable<ListIterator<T>> runnable) {
		return or(runnable, Collections.emptyListIterator());
	}

	public static <K, V> Map<K, V> orEmptyMap(TryRunnable<Map<K, V>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptyMap(), stackTrace);
	}

	public static <K, V> Map<K, V> orEmptyMap(TryRunnable<Map<K, V>> runnable) {
		return or(runnable, Collections.emptyMap());
	}

	public static <K, V> NavigableMap<K, V> orEmptyNavigableMap(TryRunnable<NavigableMap<K, V>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptyNavigableMap(), stackTrace);
	}

	public static <K, V> NavigableMap<K, V> orEmptyNavigableMap(TryRunnable<NavigableMap<K, V>> runnable) {
		return or(runnable, Collections.emptyNavigableMap());
	}

	public static <K, V> SortedMap<K, V> orEmptySortedMap(TryRunnable<SortedMap<K, V>> runnable, boolean stackTrace) {
		return or(runnable, Collections.emptySortedMap(), stackTrace);
	}

	public static <K, V> SortedMap<K, V> orEmptySortedMap(TryRunnable<SortedMap<K, V>> runnable) {
		return or(runnable, Collections.emptySortedMap());
	}
}