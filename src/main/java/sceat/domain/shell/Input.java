package sceat.domain.shell;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Input {

	private static Input instance = new Input();
	private Set<Consumer<String>> handlers = new HashSet<Consumer<String>>();

	private Input() {
	}

	public static void register(Consumer<String> handler) {
		instance.handlers.add(handler);
	}

	public static Set<Consumer<String>> getHandlers() {
		return instance.handlers;
	}

}
