package sceat.domain.protocol.packets;

public abstract class PacketPhantom {

	public abstract String toJson();

	public byte[] getBytes() {
		return toJson().getBytes();
	}

}
