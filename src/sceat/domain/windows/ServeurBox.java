package sceat.domain.windows;

import sceat.domain.Serveur;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.Panel.Orientation;
import com.googlecode.lanterna.gui.component.Table;
import com.googlecode.lanterna.gui.layout.LinearLayout;
import com.googlecode.lanterna.terminal.Terminal.Color;

public class ServeurBox {

	private Panel serversBox = new Panel(new Border.Invisible(), Orientation.HORISONTAL);
	private Panel serverBox_2 = new Panel(new Border.Invisible(), Orientation.VERTICAL);
	private Panel allplayersBox = new Panel("Server", new Border.Bevel(false), Orientation.HORISONTAL);
	private ActionListBox cmdbox = new ActionListBox();
	private Panel infoxbox = new Panel("Infos", new Border.Bevel(false), Orientation.VERTICAL);

	private Table stafflist = new Table(2, "Staff");
	private Table playerslist = new Table(4, "Players");

	private NetworkWindow window;
	private Serveur server;

	public ServeurBox(NetworkWindow windo, Serveur s) {
		this.window = windo;
		this.server = s;
		infoxbox.addComponent(new Label("Name : lobbyMain1", Color.BLUE));
		infoxbox.addComponent(new Label("Type : lobbyMain", Color.BLUE));
		infoxbox.addComponent(new Label("Index : 1", Color.BLUE));
		infoxbox.addComponent(new Label("Status : OPEN", Color.BLUE));
		infoxbox.addComponent(new Label("Players : 0", Color.BLUE));
		infoxbox.addComponent(new Label("Staff : 0", Color.BLUE));
		infoxbox.addComponent(new Label("MaxPlayers : 100", Color.BLUE));
		infoxbox.addComponent(new Label("Host : 0.0.0.0:0000", Color.BLUE));

		cmdbox.addAction("Boot server", new Action() {

			@Override
			public void doAction() {
				bootServer();
			}
		});
		cmdbox.addAction("Close server", new Action() {

			@Override
			public void doAction() {
				closeServer();
			}
		});
		cmdbox.addAction("Reboot server", new Action() {

			@Override
			public void doAction() {
				rebootServer();
			}
		});
		cmdbox.addAction("Backup", new Action() {

			@Override
			public void doAction() {
				backup();
			}
		});
		cmdbox.addAction("Sync Plugins", new Action() {

			@Override
			public void doAction() {
				syncPlugin();
			}
		});
		cmdbox.addAction("Sync Maps", new Action() {

			@Override
			public void doAction() {
				syncMap();
			}
		});
		serverBox_2.addComponent(infoxbox, LinearLayout.GROWS_HORIZONTALLY, LinearLayout.GROWS_VERTICALLY);
		serverBox_2.addComponent(cmdbox, LinearLayout.MAXIMIZES_VERTICALLY);
		allplayersBox.addComponent(stafflist, LinearLayout.MAXIMIZES_HORIZONTALLY, LinearLayout.MAXIMIZES_VERTICALLY);
		allplayersBox.addComponent(playerslist, LinearLayout.MAXIMIZES_HORIZONTALLY, LinearLayout.MAXIMIZES_VERTICALLY);
		serversBox.addComponent(serverBox_2, LinearLayout.MAXIMIZES_VERTICALLY);
		serversBox.addComponent(allplayersBox, LinearLayout.MAXIMIZES_HORIZONTALLY, LinearLayout.MAXIMIZES_VERTICALLY);
	}

	public void show() {
		getWindow().getSelectionSrv().addComponent(serversBox, LinearLayout.MAXIMIZES_HORIZONTALLY, LinearLayout.MAXIMIZES_VERTICALLY);
	}

	public void hide() {
		getWindow().getSelectionSrv().removeComponent(serversBox);
	}

	public Serveur getServer() {
		return server;
	}

	public NetworkWindow getWindow() {
		return window;
	}

	public void bootServer() {

	}

	public void rebootServer() {

	}

	public void closeServer() {

	}

	public void backup() {

	}

	public void syncPlugin() {

	}

	public void syncMap() {

	}
}
