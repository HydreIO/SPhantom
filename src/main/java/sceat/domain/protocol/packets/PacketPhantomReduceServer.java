package sceat.domain.protocol.packets;

import sceat.domain.Manager;
import sceat.domain.common.mq.Broker;
import sceat.domain.network.Core;
import sceat.domain.network.server.Vps;
import sceat.domain.trigger.PhantomTrigger;
import fr.aresrpg.sdk.mc.Statut;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

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
		Log.packet(this, true);
		Manager.getInstance().getServersByLabel().safeGet(label).setStatus(Statut.REDUCTION);
	}

	@Override
	public String toString() {
		return "PacketReduceServer [Srv: " + getLabel() + "|Vps: " + getVpsLabel() + "]";
	}

	public String getLabel() {
		return label;
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

	@Override
	public void send() {
		Broker.get().reduceServer(this);
	}

}
