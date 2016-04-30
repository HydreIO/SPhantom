package sceat.domain.protocol.packets;

import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.server.Vps;
import sceat.domain.protocol.MessagesType;
import sceat.domain.trigger.PhantomTrigger;

public class PacketPhantomReduceServer extends PacketPhantom {

	private String label;
	private String vpsLabel;

	public PacketPhantomReduceServer(String label, String vps) {
		this.label = label;
		this.vpsLabel = vps;
	}

	public PacketPhantomReduceServer() {
	}

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
		Vps v = Core.getInstance().getVps().getOrDefault(vpsLabel, null);
		if (v != null) PhantomTrigger.getAll().forEach(t -> t.handleVps(v));
		if (cameFromLocal()) return;
		if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketReduceServer [Srv: " + getLabel() + "|Vps: " + getVpsLabel() + "]");
		Manager.getInstance().getServersByLabel().get(label).setStatus(Statut.REDUCTION);
	}

	public String getLabel() {
		return label;
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

}
