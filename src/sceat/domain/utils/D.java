package sceat.domain.utils;

@FunctionalInterface
public interface D extends Runnable {

	public static D o(D r) {
		return r;
	}

	@Override
	public void run();

	default void If(boolean condition) {
		if (condition) run();
	}

}
