package sceat.domain.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import sceat.domain.common.Joiner;

@FunctionalInterface
public interface PhantomFactory {

	String named();

	default ThreadFactory build() {
		return new ThreadFactory() {
			final AtomicInteger count = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				String name = named();
				int var = count.incrementAndGet();
				if (!name.contains("$d")) name = Joiner.of(name).append(" $d");
				return new Thread(r, name.replace("$d", String.valueOf(var)));
			}
		};
	}

	/**
	 * Build un threadFactory basique avec juste un rename des thread ! Mettre $d pour le num du thread
	 * 
	 * @param pattern
	 * @return
	 */
	public static PhantomFactory create(String pattern) {
		return () -> pattern;
	}

}
