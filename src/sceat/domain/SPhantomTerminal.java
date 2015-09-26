package sceat.domain;

import sceat.SPhantom;
import sceat.domain.windows.NetworkWindow;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.GUIScreen.Position;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenWriter;
import com.googlecode.lanterna.terminal.Terminal.Color;

public class SPhantomTerminal {

	private GUIScreen screen;
	private ScreenWriter writer;
	private boolean running = false;

	public SPhantomTerminal() {
		screen = TerminalFacade.createGUIScreen();
		if (screen == null) {
			SPhantom.print("Couldn't allocate a terminal!");
			return;
		}
		writer = new ScreenWriter(getScreen());

	}

	public void show() {
		this.running = true;
		getScreen().startScreen();
		getWriter().setBackgroundColor(Color.BLACK);
		getWriter().setForegroundColor(Color.WHITE);
		getGUI().showWindow(new NetworkWindow(this), Position.CENTER);
		refreshScreen();
	}

	public void awaitForInput() {
		this.running = true;
		show();
		while (isRunning()) {
			Key key = getScreen().readInput();
			if (key != null) handleInput(key);
			if (getScreen().resizePending()) refreshScreen();
		}
	}

	public void handleInput(Key key) {
		if (key.getKind() == null) return;
		if (key.getKind() == Key.Kind.Escape) shutdown();
	}

	public ScreenWriter getWriter() {
		return writer;
	}

	public void refreshScreen() {
		screen.getScreen().refresh();
	}

	public Screen getScreen() {
		return screen.getScreen();
	}

	public GUIScreen getGUI() {
		return screen;
	}

	public boolean isRunning() {
		return this.running;
	}

	public void shutdown() {
		this.running = false;
		SPhantom.print("Shutdown du terminal !");
		while (getGUI().getActiveWindow() != null)
			getGUI().getActiveWindow().close();
		SPhantom.print("Shutdown du screen");
		getScreen().stopScreen();

	}

}
