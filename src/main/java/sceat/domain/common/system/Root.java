package sceat.domain.common.system;

import java.util.function.Consumer;

import sceat.SPhantom;

public interface Root {

	public static <T> T chain(T t, Consumer<T> consumer) {
		consumer.accept(t);
		return t;
	}

	void exit();

	public static void exit(boolean crash) {
		if (crash) System.exit(1);
		get().exit();
	}

	public static SPhantom get() {
		return SPhantom.getInstance();
	}

}