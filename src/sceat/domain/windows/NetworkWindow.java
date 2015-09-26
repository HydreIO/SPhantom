package sceat.domain.windows;

import java.util.Map;

import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.SPhantomTerminal;
import sceat.domain.Serveur;
import sceat.domain.Serveur.ServeurType;
import sceat.domain.utils.New;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component.Alignment;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.Panel.Orientation;
import com.googlecode.lanterna.gui.dialog.DialogButtons;
import com.googlecode.lanterna.gui.dialog.DialogResult;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.layout.LinearLayout;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.Terminal.Color;

public class NetworkWindow extends Window {

	private SPhantomTerminal terminal;

	private Panel networkingPanel = new Panel("Networking", new Border.Bevel(false), Orientation.VERTICAL);
	private Panel infosPlayersPanel = new Panel("Global", new Border.Bevel(false), Orientation.VERTICAL);;

	private Label onlineCount = new Label("Players online : 0", Color.BLUE);
	private Label staffOnlineCount = new Label("Staff online : 0", Color.BLUE);
	private Label networkStatus = new Label("Network Status : " + Manager.Network_Status, Color.MAGENTA, true);
	private Button changeStatus = new Button("Update network status", new Action() {

		@Override
		public void doAction() {
			openChangeStatus();
		}
	});
	private Panel selectionSrv = new Panel("Servers", new Border.Standard(), Orientation.HORISONTAL);
	private Panel selectionSrvhide = new Panel("", new Border.Invisible(), Orientation.HORISONTAL);

	private ActionListBox servers = new ActionListBox();
	private Map<ServeurType, ActionListBox> serverInfosBox = New.map();

	private Map<ServeurType, Panel> serversInfosCadre = New.map();

	public NetworkWindow(SPhantomTerminal terminal) {
		super("Sphantom Terminal | � Sceat.Network");
		this.terminal = terminal;
		setBorder(new Border.Bevel(false));
		servers.addBorder(new Border.Bevel(false), "Servers");
		servers.setAlignment(Alignment.TOP_LEFT);
		for (final ServeurType type : ServeurType.values()) {
			final Panel pan = new Panel(new Border.Bevel(false), Panel.Orientation.VERTICAL);
			pan.setVisible(false);
			serversInfosCadre.put(type, pan);
			serverInfosBox.put(type, new ActionListBox());
			initInfosPanel(type);
			servers.addAction(type.name(), new Action() {

				@Override
				public void doAction() {
					hideServeursInfosPanel(type);
					showServeursInfosPanel(type);
				}
			});
		}
		build();
	}

	public void openChangeStatus() {
		// TODO change status rabbit
	}

	public SPhantomTerminal getTerminal() {
		return terminal;
	}

	public void syncInfos(int onlines, int staffonline) {
		getOnlineCount().setText("Players online : " + onlines);
		getStaffOnlineCount().setText("Staff online : " + staffonline);
	}

	public void hideServeursInfosPanel(ServeurType type) {
		for (Panel p : serversInfosCadre.values())
			selectionSrvhide.removeComponent(p);
	}

	public void showServeursInfosPanel(ServeurType type) {
		Panel serversInfoscadre = serversInfosCadre.get(type);
		selectionSrvhide.addComponent(serversInfoscadre);
		serversInfoscadre.setVisible(true);
	}

	public void initInfosPanel(ServeurType type) {
		Panel serversInfoscadre = serversInfosCadre.get(type);
		Serveur[] array = SPhantom.getInstance().getManager().getServersArray(type);
		ActionListBox b = serverInfosBox.get(type);
		serversInfoscadre.addComponent(b, LinearLayout.MAXIMIZES_VERTICALLY);
		for (final Serveur s : array)
			b.addAction(s != null ? s.getName() : "Empty", new Action() {

				@Override
				public void doAction() {
					showServeurInfos(s);
				}
			});
	}

	public void showServeurInfos(Serveur s) {

	}

	public void init() {
	}

	public Label getStaffOnlineCount() {
		return staffOnlineCount;
	}

	public Panel getSelectionSrv() {
		return selectionSrv;
	}

	public void build() {
		Panel horisontalPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
		Panel servertypeCadre = new Panel(new Border.Standard(), Panel.Orientation.HORISONTAL);
		this.addComponent(new EmptySpace());
		this.addComponent(new EmptySpace());
		getNetworkingPanel().addComponent(networkStatus);
		getNetworkingPanel().addComponent(changeStatus);
		getInfosPlayersPanel().addComponent(getOnlineCount());
		getInfosPlayersPanel().addComponent(getStaffOnlineCount());
		horisontalPanel.addComponent(getNetworkingPanel(), LinearLayout.MAXIMIZES_HORIZONTALLY);
		horisontalPanel.addComponent(getInfosPlayersPanel(), LinearLayout.MAXIMIZES_HORIZONTALLY);
		servertypeCadre.addComponent(servers, LinearLayout.MAXIMIZES_VERTICALLY);
		addComponent(horisontalPanel, LinearLayout.MAXIMIZES_HORIZONTALLY);
		selectionSrvhide.addComponent(servertypeCadre, LinearLayout.MAXIMIZES_VERTICALLY);
		selectionSrv.addComponent(selectionSrvhide, LinearLayout.MAXIMIZES_VERTICALLY);
		addComponent(selectionSrv, LinearLayout.MAXIMIZES_HORIZONTALLY, LinearLayout.MAXIMIZES_VERTICALLY);
		new ServeurBox(this, null).show();
	}

	public void showExit() {
		DialogResult result = MessageBox.showMessageBox(getOwner(), "Sphantom", "exit Sphantom ?", DialogButtons.OK_CANCEL);
		if (result == DialogResult.OK) getTerminal().shutdown();
	}

	public Panel getNetworkingPanel() {
		return networkingPanel;
	}

	public Panel getInfosPlayersPanel() {
		return infosPlayersPanel;
	}

	public Label getOnlineCount() {
		return onlineCount;
	}

	@Override
	public void onKeyPressed(Key key) {
		super.onKeyPressed(key);
		if (key.getKind() == null) return;
		switch (key.getKind()) {
			case Escape:
				showExit();
				break;

			default:
				break;
		}
	}

}
