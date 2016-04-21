package sceat.domain.compute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import sceat.domain.network.server.Server;
import sceat.domain.network.server.Vps;

@FunctionalInterface
public interface Sequencer<V, S, X> {

	/**
	 * blblblblblbl
	 * 
	 * @return a mapping <ServerLabel,VpsLabel> for the vps reallocation and a set<VpsLabel> to destroy
	 */
	public SequenceContener compute();

	// exemple
	default boolean transfert(V v, Collection<S> collection, Predicate<S> predicate, Adder<V, S> adder, Predicate<S> canAccept, BiConsumer<V, S> thenAdd) { // predicate = srv != Reduction srv != close
		return collection.stream().filter(predicate).map(s -> adder.add(v, s, canAccept, thenAdd)).reduce((a, b) -> (a && b)).get();
	}

	// exemple
	default boolean add(V v, S s, Predicate<S> canAccept, BiConsumer<V, S> thenAdd) { // if(vps.canAccept then vps.add(s)
		if (canAccept.test(s)) {
			thenAdd.accept(v, s);
			return true;
		}
		return false;
	}

	// exemple // la collection ne doit pas contenir v
	default boolean dispatch(V v6, Collection<S> v6coll, Collection<V> x, Worker<V, S> worker, Predicate<S> noClose, Adder<V, S> adder, Predicate<S> canAccept, BiConsumer<V, S> thenAdd) {
		if (x.contains(v6)) x.remove(v6);
		return x.stream().map(v -> worker.transfert(v, v6coll, noClose, adder, canAccept, thenAdd)).reduce((a, b) -> (a && b)).get();
	}

	default SequenceContener sequence(Collection<V> coll, Dispatcher<V, S> dispatcher) {
		Collection<V> clone = new ArrayList<V>();
		Map<String, String> map = new HashMap<String, String>();
		Set<String> empty = new HashSet<String>();
		coll.forEach(clone::add);

		Set<Vps> vps = null;
		Map<String, HashSet<String>> collected = vps.stream().collect(
				Collectors.toMap(Vps::getLabel, v -> new HashSet<String>(v.getServers().stream().map(Server::getLabel).collect(Collectors.toList()))));
		Adder<Vps, Server> addtest = (v, s, predicate, consume) -> predicate.test(s);
	}

	@FunctionalInterface
	public interface BoolConsumer<T> {

		public boolean accept(T t);

		/**
		 * If accept(t) is false, test y
		 * 
		 * @param t
		 * @param y
		 * @return true if this or y is true, false otherwise
		 */
		default boolean orElse(T t, BoolConsumer<? super T> y) {
			if (accept(t)) return true;
			return y.accept(t);
		}
	}
	
	@FunctionalInterface
	public interface BoolConsumer<T> {

		public boolean accept(T t);

		/**
		 * If accept(t) is false, test y
		 * 
		 * @param t
		 * @param y
		 * @return true if this or y is true, false otherwise
		 */
		default boolean orElse(T t, BoolConsumer<? super T> y) {
			if (accept(t)) return true;
			return y.accept(t);
		}
	}

	/**
	 * Dispatch V on X
	 * 
	 * @author MrSceat
	 *
	 * @param <V>
	 * @param <S>
	 */
	@FunctionalInterface
	public interface Dispatcher<V, S> {
		public boolean dispatch(V v6, Collection<S> v6coll, Collection<V> x, Worker<V, S> worker, Predicate<S> noClose, Adder<V, S> adder, Predicate<S> canAccept, BiConsumer<V, S> thenAdd);
	}

	/**
	 * Transfer V on V
	 * 
	 * @author MrSceat
	 *
	 * @param <V>
	 * @param <S>
	 */
	@FunctionalInterface
	public interface Worker<V, S> {
		public boolean transfert(V v, Collection<S> collection, Predicate<S> predicate, Adder<V, S> adder, Predicate<S> canAccept, BiConsumer<V, S> thenAdd);
	}

	/**
	 * Add S on V
	 * 
	 * @author MrSceat
	 *
	 * @param <V>
	 * @param <S>
	 */
	@FunctionalInterface
	public interface Adder<V, S> {
		public boolean add(V v, S s, Predicate<S> predicate, BiConsumer<V, S> consume);
	}

	public static class SequenceContener {
		public Map<String, Set<String>> remap;
		public Set<String> toDestroy;

		public SequenceContener(Map<String, Set<String>> map, Set<String> todest) {
			this.remap = map;
			this.toDestroy = todest;
		}
	}

}
