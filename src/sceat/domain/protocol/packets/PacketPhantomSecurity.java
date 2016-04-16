package sceat.domain.protocol.packets;

import sceat.Main;

public class PacketPhantomSecurity extends PacketPhantom {

	private String serial;
	private String security;

	@Override
	public void serialize() {
		writeString(getSerial());
		writeString(getSecurity());
	}

	@Override
	public void deserialize() {
		this.serial = readString();
		this.security = readString();
	}

	public static PacketPhantomSecurity generateNull() {
		return new PacketPhantomSecurity("NaN", "NaN");
	}

	public PacketPhantomSecurity(String serial, String security) {
		this.security = security;
		this.serial = serial;
	}

	public PacketPhantomSecurity setSerial(String serial) {
		this.serial = serial;
		return this;
	}

	public PacketPhantomSecurity setSecurity(String security) {
		this.security = security;
		return this;
	}

	public boolean correspond(PacketPhantomSecurity pkt) {
		return pkt.getSerial().equals(getSerial()) && pkt.getSecurity().equals(getSecurity());
	}

	public boolean isLocal() {
		return getSerial().equals(Main.serial.toString()) && getSecurity().equals(Main.security.toString());
	}

	public String getSecurity() {
		return security;
	}

	public String getSerial() {
		return serial;
	}

}
