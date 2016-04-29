package sceat.domain.shell;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class Input {

	private static Input instance = new Input();
	private Queue<String> handlers = new LinkedList<String>();

	private Input() {
	}

	public static Input getInstance() {
		return instance;
	}

	public interface PhantomInput {

		/**
		 * Send a command to Sphantom
		 * 
		 * @param input
		 *            command
		 */
		default void push(String input) {
			instance.handlers.add(input);
		}
	}

	public String next() {
		while (true)
			if (!handlers.isEmpty()) return handlers.poll();
	}

	public static class OutPut {

		private static OutPut instance = new OutPut();
		private Set<Consumer<String>> handlers = new HashSet<Consumer<String>>();

		private OutPut() {
		}

		public static void register(Consumer<String> handler) {
			instance.handlers.add(handler);
		}

		public static Set<Consumer<String>> getHandlers() {
			return instance.handlers;
		}

	}

}
