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

	private Label name = new Label("Name : ..", Color.BLUE);
	private Label type = new Label("Type : ..", Color.BLUE);
	private Label index = new Label("Index : ..", Color.BLUE);
	private Label status = new Label("Status : ..", Color.BLUE);
	private Label players = new Label("Players : ..", Color.BLUE);
	private Label staff = new Label("Staff : ..", Color.BLUE);
	private Label maxp = new Label("MaxPlayers : ..", Color.BLUE);
	private Label host = new Label("Host : 0.0.0.0:0000", Color.BLUE);
	private Label ping = new Label("Ping : ..", Color.BLUE);

	public ServeurBox(NetworkWindow windo, Serveur s) {
		this.window = windo;
		this.server = s;
		infoxbox.addComponent(name);
		infoxbox.addComponent(type);
		infoxbox.addComponent(index);
		infoxbox.addComponent(status);
		infoxbox.addComponent(players);
		infoxbox.addComponent(staff);
		infoxbox.addComponent(maxp);
		infoxbox.addComponent(host);
		infoxbox.addComponent(ping);

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

	public void syncServerInfos(int staffcount) {
		status.setText("Status : " + getServer().getStatut());
		players.setText("Players : " + getServer().getPlayersCount());
		staff.setText("Staff : " + staffcount);
		maxp.setText("MaxPlayers : " + getServer().getMaxPlayers());
		host.setText("Host : " + getServer().getIpadress());
		ping.setText("Ping : " + (getServer().ping == -1 ? "DOWN" : getServer().ping));
		// infoxbox.addComponent(new Label("Status : " + getServer().getStatut(), Color.BLUE));
		// infoxbox.addComponent(new Label("Players : " + getServer().getPlayersCount(), Color.BLUE));
		// infoxbox.addComponent(new Label("Staff : " + staffcount, Color.BLUE));
		// infoxbox.addComponent(new Label("MaxPlayers : " + getServer().getMaxPlayers(), Color.BLUE));
		// infoxbox.addComponent(new Label("Host : " + getServer().getIpadress(), Color.BLUE));
		// infoxbox.addComponent(new Label("Ping : " + (getServer().ping == -1 ? "DOWN" : getServer().ping), Color.BLUE));
	}

	public void show() {
		getWindow().getSelectionSrv().addComponent(serversBox, LinearLayout.MAXIMIZES_HORIZONTALLY, LinearLayout.MAXIMIZES_VERTICALLY);
		getWindow().currentShowingBox = this;
	}

	public void hide() {
		getWindow().getSelectionSrv().removeComponent(serversBox);
		getWindow().currentShowingBox = null;
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
