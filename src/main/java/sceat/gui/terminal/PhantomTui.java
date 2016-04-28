package sceat.gui.terminal;

import java.io.IOException;
import java.util.Arrays;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class PhantomTui {

	public static void main(String[] args) throws IOException {
		// Setup terminal and screen layers
		Terminal terminal = new DefaultTerminalFactory().createTerminal();
		Screen screen = new TerminalScreen(terminal);
		screen.startScreen();

		// Create window to hold the panel
		BasicWindow window = new BasicWindow();
		window.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.FULL_SCREEN));
		window.setCloseWindowWithEscape(true);

		// Create gui and start gui
		MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.RED));
		gui.addWindowAndWait(window);
	}
}
