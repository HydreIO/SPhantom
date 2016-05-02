package sceat.domain.compute;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@FunctionalInterface
public interface Sequencer<V, S> {

	public void compute(List<V> list, TakeSupplier<V, Collection<S>> tk, Dispatcher<V, S> dispatcher, Worker<V, S> worker, Predicate<S> noClose, Adder<V, S> adder, BiPredicate<V, S> canAccept,
			BoolBiConsumer<V, S> thenAdd);

	public static <V, S> void phantomSequencing(Sequencer<V, S> sequencer, List<V> list, TakeSupplier<V, Collection<S>> tk, Dispatcher<V, S> dispatcher, Worker<V, S> worker, Predicate<S> noClose,
			Adder<V, S> adder, BiPredicate<V, S> canAccept, BoolBiConsumer<V, S> thenAdd) {
		sequencer.compute(list, tk, dispatcher, worker, noClose, adder, canAccept, thenAdd);
	}

	@FunctionalInterface
	public interface TakeSupplier<A, B> {
		public B take(A a);
	}

	@FunctionalInterface
	public interface Chainer<T> {
		public T chain(T t);

		public static <T> Chainer<T> of(Chainer<T> chainer) {
			return chainer;
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

		default BoolBiConsumer<T, Y> andThen(BoolBiConsumer<? super T, ? super Y> other) {
			return (T c1, Y c2) -> {
				accept(c1, c2);
				other.accept(c1, c2);
				return true;
			};
		}

		public static <T, Y> BoolBiConsumer<T, Y> of(BoolBiConsumer<T, Y> other) {
			return other;
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
		 *            predicate for test if the server is not in REDUCTION,CRASHED,OVERHEAD or CLOSE
		 * @param adder
		 *            the add method
		 * @param canAccept
		 *            predicate who test if the vps can accept the server
		 * @param thenAdd
		 *            BiConsumer for add the server on the vps
		 * @return
		 */
		public boolean dispatch(V v6, Collection<S> v6coll, Collection<V> x, Worker<V, S> worker, Predicate<S> noClose, Adder<V, S> adder, BiPredicate<V, S> canAccept, BoolBiConsumer<V, S> thenAdd);
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
		 * Take all the server on the vps who haven't REDUCTION CRASH OVERHEAD or CLOSE statut en try to transfert them on the gived vps
		 * 
		 * @param v
		 *            the gived vps who receive servers
		 * @param collection
		 *            the server list on the current vps
		 * @param noClose
		 *            predicate for test if the server is not in REDUCTION CRASH OVERHEAD or CLOSE
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
