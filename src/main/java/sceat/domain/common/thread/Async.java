package sceat.domain.common.thread;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import sceat.domain.common.system.Root;

public interface Async {

	void run(Runnable r);

	<T> CompletableFuture<T> supply(Supplier<T> t);

	public static Async get() {
		return Root.get();
	}

}
