package sceat.domain.messaging.protocol;

public abstract class PacketPhantom {

	public abstract String toJson();

	public byte[] getBytes() {
		return toJson().getBytes();
	}

}
