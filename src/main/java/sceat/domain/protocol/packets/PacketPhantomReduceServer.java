package sceat.domain.protocol.packets;

import sceat.domain.Manager;
import sceat.domain.minecraft.Statut;

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
	public void handleData() {
		Manager.getInstance().getServersByLabel().get(label).setStatus(Statut.REDUCTION);
	}

	public String getLabel() {
		return label;
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

}
