package sceat.infra.input;

import java.util.Scanner;

import sceat.domain.shell.Input.PhantomInput;

public class ScannerInput implements PhantomInput {

	@SuppressWarnings("unused")
	private static ScannerInput instance = new ScannerInput();

	private ScannerInput() {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (true)
			push(scanner.next());
	}

}
