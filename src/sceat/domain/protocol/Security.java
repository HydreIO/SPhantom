package sceat.domain.protocol;

import sceat.Main;

public class Security {

	private String serial;
	private String security;

	public static Security generateNull() {
		return new Security("NaN", "NaN");
	}

	public Security(String serial, String security) {
		this.security = security;
		this.serial = serial;
	}

	public Security setSerial(String serial) {
		this.serial = serial;
		return this;
	}

	public Security setSecurity(String security) {
		this.security = security;
		return this;
	}

	public boolean correspond(Security pkt) {
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
