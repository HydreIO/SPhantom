package sceat.infra.input;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sceat.domain.shell.Input.PhantomInput;

public class ScannerInput implements PhantomInput {

	private static ScannerInput instance;
	private ExecutorService exe = Executors.newSingleThreadExecutor();

	public ScannerInput() {
		instance = this;
		exe.execute(() -> {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			while (true)
				push(scanner.next());
		});
	}

	public static void shutDown() {
		instance.exe.shutdownNow();
	}

}
