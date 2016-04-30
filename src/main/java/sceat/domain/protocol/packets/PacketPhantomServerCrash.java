package sceat.domain.protocol.packets;

import java.util.Set;

import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Vps;
import sceat.domain.protocol.MessagesType;
import sceat.domain.protocol.PacketSender;
import sceat.domain.trigger.PhantomTrigger;

public class PacketPhantomServerCrash extends PacketPhantom {

	@Override
	protected void serialize_() {
		writeString(getLabel());
		writeString(getVpsLabel());
	}

	@Override
	protected void deserialize_() {
		this.label = readString();
		this.vpsLabel = readString();
	}

	@Override
	public void handleData(MessagesType type) {
		Manager m = Manager.getInstance();
		Server srv = Manager.getInstance().getServersByLabel().get(getLabel());
		Vps curr = null;
		if (srv == null) {
			SPhantom.print("PacketPhantomServerCrash : State Crashed | the server " + getLabel() + " is not registered | Ignoring (cause crash) ! break");
			return;
		} else if (getVpsLabel() == null) {
			bite: for (Vps vps : Core.getInstance().getVps().values()) {
				for (Server s : vps.getServers())
					if (s.getLabel().equalsIgnoreCase(getLabel())) {
						curr = vps;
						break bite;
					}
			}
		} else curr = srv.getVps();
		Set<Server> ss = Core.getInstance().getServersByType().get(srv.getType());
		if (ss.contains(srv)) ss.remove(srv);
		m.getServersByLabel().remove(getLabel());
		if (curr == null) {
			// vps not found osef car tt façon on le vire
			SPhantom.print("PacketPhantomServerInfo : State Crashed | the server " + getLabel() + " is registered but not in a Vps object | Info ! break");
			return;
		}
		Vps vss = curr;
		PhantomTrigger.getAll().forEach(t -> t.handleVps(vss)); // trigger
		if (curr.getServers().contains(srv)) curr.getServers().remove(srv);
		PacketSender.getInstance().sendServer(PacketPhantomServerInfo.fromServer(srv));
	}

	private String label;
	private String vpsLabel;

	public PacketPhantomServerCrash(String label) {
		this.label = label;
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

	public String getLabel() {
		return label;
	}

}
