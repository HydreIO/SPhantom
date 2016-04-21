package sceat.domain.compute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Vps;
import sceat.domain.protocol.PacketSender;
import sceat.domain.protocol.packets.PacketPhantomReduceServer;

@FunctionalInterface
public interface Sequencer<V, S, X> {

	/**
	 * blblblblblbl
	 * 
	 * @return a mapping <ServerLabel,VpsLabel> for the vps reallocation and a set<VpsLabel> to destroy
	 */
	public SequenceContener compute();

	// exemple
	default boolean transfert(V v, Collection<S> collection, Predicate<S> noClose, Adder<V, S> adder, BiPredicate<V, S> canAccept, BoolBiConsumer<V, S> thenAdd) { // predicate = srv != Reduction srv != close
		return collection.stream().filter(noClose).map(s -> adder.add(v, s, canAccept, thenAdd)).reduce((a, b) -> (a && b)).get();
	}

	// exemple
	default boolean add(V v, S s, BiPredicate<V, S> canAccept, BoolBiConsumer<V, S> thenAdd) { // if(vps.canAccept then vps.add(s)
		return (canAccept.test(v, s) && thenAdd.accept(v, s));
	}

	// exemple // la collection ne doit pas contenir v
	default boolean dispatch(V v6, Collection<S> v6coll, Collection<V> x, Worker<V, S> worker, Predicate<S> noClose, Adder<V, S> adder, BiPredicate<V, S> canAccept, BoolBiConsumer<V, S> thenAdd) {
		if (x.contains(v6)) x.remove(v6);
		return x.stream().map(v -> worker.transfert(v, v6coll, noClose, adder, canAccept, thenAdd)).reduce((a, b) -> (a && b)).get();
	}

	default SequenceContener sequence(Collection<V> coll, Dispatcher<V, S> dispatcher) {

		Set<Vps> vps = null;

		// Il faut faire un petit test de securité et set le vps label sur tt les serveurs avant la sequence
		vps.forEach(v -> v.getServers().forEach(s -> s.setVpsLabel(v.getLabel())));

		Collection<V> clone = new ArrayList<V>();
		Map<String, String> map = new HashMap<String, String>();
		Set<String> empty = new HashSet<String>();
		coll.forEach(clone::add);

		Map<String, HashSet<String>> collected = vps.stream().collect(
				Collectors.toMap(Vps::getLabel, v -> new HashSet<String>(v.getServers().stream().map(Server::getLabel).collect(Collectors.toList()))));

		Vps vp = null;
		Server sr = null;
		Predicate<Server> reduclose = (s) -> s.getStatus() != Statut.CLOSING && s.getStatus() != Statut.REDUCTION;

		Adder<Vps, Server> addtest = (v, s, predicate, consume) -> (predicate.test(v, s) && consume.accept(v, s));
		addtest.add(vp, sr, (v, s) -> v.canAccept(s), (v1, v2) -> {
			v2.setStatus(Statut.REDUCTION);
			PacketSender.getInstance().reduceServer(new PacketPhantomReduceServer(v2.getLabel(), v1.getLabel()));
			Core.getInstance().deployServerOnVps(v2.getType(), v1);
			return true;
		});
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
	public interface BoolBiConsumer<T, Y> {

		public boolean accept(T t, Y y);

		/**
		 * If accept(t,y) is false, test other
		 * 
		 * @param t
		 * @param y
		 * @param other
		 * @return true if this or y is true, false otherwise
		 */
		default boolean orElse(T t, Y y, BoolBiConsumer<? super T, ? super Y> other) {
			if (accept(t, y)) return true;
			return other.accept(t, y);
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
		/**
		 * Try to dispatch all the servers from the gived vps to a collection<Vps>
		 * 
		 * @param v6
		 *            Current tested vps
		 * @param v6coll
		 *            the server list of the current vps
		 * @param x
		 *            the collection of vps
		 * @param worker
		 *            the worker for transfert process
		 * @param noClose
		 *            predicate for test if the server is not in REDUCTION or CLOSE
		 * @param adder
		 *            the add method
		 * @param canAccept
		 *            predicate who test if the vps can accept the server
		 * @param thenAdd
		 *            BiConsumer for add the server on the vps
		 * @return
		 */
		public boolean dispatch(V v6, Collection<S> v6coll, Collection<V> x, Worker<V, S> worker, Predicate<S> noClose, Adder<V, S> adder, Predicate<S> canAccept, BoolBiConsumer<V, S> thenAdd);
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
		/**
		 * Take all the server on the vps who haven't REDUCTION or CLOSE statut en try to transfert them on the gived vps
		 * 
		 * @param v
		 *            the gived vps who receive servers
		 * @param collection
		 *            the server list on the current vps
		 * @param noClose
		 *            predicate for test if the server is not in REDUCTION or CLOSE
		 * @param adder
		 *            the add method
		 * @param canAccept
		 *            predicate who test if the vps can accept the server
		 * @param thenAdd
		 *            BiConsumer for add the server on the vps
		 * @return true if the current vps has been fully transfered, false if somes servers are still on the vps
		 */
		public boolean transfert(V v, Collection<S> collection, Predicate<S> noClose, Adder<V, S> adder, BiPredicate<V, S> canAccept, BoolBiConsumer<V, S> thenAdd);
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
		/**
		 * Add the server on the vps if it can accept him
		 * 
		 * @param v
		 *            the vps
		 * @param s
		 *            the server to add
		 * @param canAccept
		 *            predicate who test if the vps can accept the server
		 * @param thenAdd
		 *            BiConsumer for add the server on the vps
		 * @return true if @param canAccept is true
		 */
		public boolean add(V v, S s, BiPredicate<V, S> canAccept, BoolBiConsumer<V, S> thenAdd);
	}

}
