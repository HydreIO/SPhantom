package sceat.domain.common;

@FunctionalInterface
public interface Joiner {

	String join();

	default String append(String str) {
		return join() + str;
	}

	public static Joiner of(String str) {
		return () -> str;
	}
}
