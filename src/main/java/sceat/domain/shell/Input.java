package sceat.domain.shell;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Input {

	private static Input instance = new Input();
	private Queue<String> inputQueue = new ConcurrentLinkedQueue<>();

	private Input() {}

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
			instance.push(input);
		}
	}

	public void push(String input){
		inputQueue.add(input);
	}

	public String next() {
		while (true)
			if (!inputQueue.isEmpty())
				return inputQueue.poll();
	}

}
