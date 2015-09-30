package sceat.domain.messaging.dao;

import java.util.UUID;

import sceat.SPhantom;
import sceat.domain.utils.UtilGson;

import com.google.gson.annotations.Expose;

public class DAO_HeartBeat {

	@Expose
	private String serial;
	@Expose
	private String security;
	@Expose
	private boolean running;
	@Expose
	private long lastHandShake;

	public DAO_HeartBeat(UUID serial, UUID security) {
		this.security = security.toString();
		this.serial = serial.toString();
		setRunning(true);
	}

	public DAO_HeartBeat handshake() {
		this.lastHandShake = System.currentTimeMillis();
		return this;
	}

	public String getSecurity() {
		return security;
	}

	public boolean isDead() {
		return System.currentTimeMillis() > getLastHandShake() + 5000;
	}

	public boolean correspond(DAO_HeartBeat dao) {
		return dao.getSerial().equals(getSerial()) && dao.getSecurity().equals(getSecurity());
	}

	public long getLastHandShake() {
		return lastHandShake;
	}

	public boolean isLocal() {
		return getSerial().equals(SPhantom.serial.toString()) && getSecurity().equals(SPhantom.security.toString());
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isRunning() {
		return this.running;
	}

	public String getSerial() {
		return serial;
	}

	public String toJson() {
		return UtilGson.serialize(this);
	}

	public static DAO_HeartBeat fromJson(String json) {
		return UtilGson.deserialize(json, DAO_HeartBeat.class);
	}
}
