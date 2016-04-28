package sceat.gui.terminal;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import sceat.domain.network.server.Server.ServerType;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class PhantomTui {

	public static class ServerAsk {
		public ServerType type;
		public int nbr;
	}

	public static boolean canlog = true;

	public static void commandServ() throws IOException {
		canlog = false;
		ServerAsk ask = new ServerAsk();
		Terminal term = new DefaultTerminalFactory().createTerminal();
		Screen screen = new TerminalScreen(term);
		screen.startScreen();

		BasicWindow window = new BasicWindow();

		ComboBox<String> box = new ComboBox<String>();
		Arrays.stream(ServerType.values()).map(ServerType::name).forEach(box::addItem);
		window.setHints(Arrays.asList(Window.Hint.EXPANDED));
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(2));
		MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.RED));
		panel.addComponent(new EmptySpace(new TerminalSize(0, 0)));
		panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
		panel.addComponent(new Label("ServerType : "));
		panel.addComponent(box);
		panel.addComponent(new Label("Nombre : "));
		TextBox tb = new TextBox();
		tb.setValidationPattern(Pattern.compile("[0-9]*"));
		panel.addComponent(tb);
		panel.addComponent(new EmptySpace(new TerminalSize(0, 0)));
		Button b = new Button("Send", () -> {
			ask.type = ServerType.valueOf(box.getText());
			ask.nbr = Integer.parseInt(tb.getText());
			MessageDialog.showMessageDialog(gui, "Infos", "You just created " + ask.nbr + " Server with type " + ask.type, MessageDialogButton.Close);
			try {
				screen.stopScreen();
			} catch (Exception e) {
				e.printStackTrace();
			}
			canlog = true;
		});
		panel.addComponent(new EmptySpace(new TerminalSize(1, 1)));
		panel.addComponent(new EmptySpace(new TerminalSize(0, 0)));
		panel.addComponent(b);
		window.setComponent(panel.withBorder(Borders.doubleLine("Boot Server")));
		gui.addWindowAndWait(window);
	}

	public static Label label = new Label("");
	public static Terminal terminal;

	public static void newInput(String txt) {
		label.setText(label.getText() + "\n" + txt);
		try {
			terminal.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		commandServ();
		// Setup terminal and screen layers
		/*
		 * terminal = new DefaultTerminalFactory().createTerminal(); Screen screen = new TerminalScreen(terminal); screen.startScreen();
		 * 
		 * BasicWindow console2 = new BasicWindow("Console"); console2.setCloseWindowWithEscape(true); console2.setHints(Arrays.asList(Window.Hint.EXPANDED)); TextBox box = new TextBox(new TerminalSize(100, 1)); Panel panel = new Panel(); label.setSize(new TerminalSize(100, 100));
		 * panel.setLayoutManager(new LinearLayout(Direction.VERTICAL)); panel.addComponent(label.withBorder(Borders.singleLine())); panel.addComponent(box.withBorder(Borders.singleLine())); box.setInputFilter(new InputFilter() {
		 * 
		 * @Override public boolean onInput(Interactable interactable, KeyStroke keyStroke) { if (keyStroke.getKeyType() == KeyType.Enter) { if (box.getText().length() > 1) newInput(box.getText()); box.setText(""); } return true; } }); console2.setComponent(panel); // Create gui and start gui
		 * MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.RED)); gui.addWindowAndWait(console2);
		 */

	}

	public static void test() throws IOException {
		terminal = new DefaultTerminalFactory().createTerminal();

		Screen screen = new TerminalScreen(terminal);
		screen.startScreen();
		// Create window to hold the panel
		BasicWindow console = new BasicWindow();
		console.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.EXPANDED));

		Panel mainPanel = new Panel();
		Panel sphantomPanel = new Panel();
		Panel mainPanel2 = new Panel();
		Panel GlobalPanel = new Panel();
		Panel consolePanel = new Panel();
		sphantomPanel.setPosition(TerminalPosition.TOP_LEFT_CORNER);
		mainPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		mainPanel2.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

		mainPanel2.addComponent(GlobalPanel.withBorder(Borders.doubleLine("Général")));
		mainPanel2.addComponent(consolePanel.withBorder(Borders.doubleLine("Console")));
		mainPanel.addComponent(sphantomPanel.withBorder(Borders.doubleLine("Sphantom")));
		mainPanel.addComponent(mainPanel2);

		console.setComponent(mainPanel);
		MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.RED));
		gui.addWindowAndWait(console);
	}
}
