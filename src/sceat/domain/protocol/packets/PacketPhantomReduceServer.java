package sceat.domain.protocol.packets;

public class PacketPhantomReduceServer extends PacketPhantom {

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

	private String label;
	private String vpsLabel;

	public PacketPhantomReduceServer(String label, String vps) {
		this.label = label;
		this.vpsLabel = vps;
	}

	public String getLabel() {
		return label;
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

}
