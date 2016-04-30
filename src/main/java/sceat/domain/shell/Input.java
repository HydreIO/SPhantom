package sceat.domain.shell;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Input {

	private static Input instance = new Input();
	private ConcurrentLinkedQueue<String> handlers = new ConcurrentLinkedQueue<String>();

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

}
