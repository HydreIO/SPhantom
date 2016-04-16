package sceat.domain.protocol.packets;

import sceat.domain.protocol.Security;

public class PacketPhantomHeartBeat extends PacketPhantom {

	private boolean running;
	private long lastHandShake;

	public PacketPhantomHeartBeat(Security sec) {
		super(sec);
		setRunning(true);
	}

	public PacketPhantomHeartBeat handshake() {
		this.lastHandShake = System.currentTimeMillis();
		return this;
	}

	public boolean isDead() {
		return System.currentTimeMillis() > getLastHandShake() + 5000;
	}

	public long getLastHandShake() {
		return lastHandShake;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isRunning() {
		return this.running;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PacketPhantom> T serialize() {
		writeBoolean(isRunning());
		writeLong(getLastHandShake());
		encodeSecurity();
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PacketPhantom> T deserialize() {
		this.running = readBoolean();
		this.lastHandShake = readLong();
		decodeSecurity();
		return (T) this;
	}

}
