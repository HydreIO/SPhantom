package sceat.infra.input;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sceat.domain.shell.Input.PhantomInput;

public class ScannerInput implements PhantomInput {

	private static final ScannerInput instance = new ScannerInput();
	private ExecutorService exe = Executors.newSingleThreadExecutor();

	private ScannerInput() {
	}

	public static void init() {
		final ScannerInput input = instance;
		input.exe.execute(() -> {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			while (true)
				input.push(scanner.next());
		});
	}

	public static void shutDown() {
		instance.exe.shutdownNow();
	}

}
