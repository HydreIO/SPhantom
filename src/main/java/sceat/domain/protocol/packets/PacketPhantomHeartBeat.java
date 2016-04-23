package sceat.domain.protocol.packets;

import sceat.domain.Heart;

public class PacketPhantomHeartBeat extends PacketPhantom {

	private boolean running;
	private long lastHandShake;

	public PacketPhantomHeartBeat() {
		setRunning(true);
	}

	@Override
	protected void serialize_() {
		writeBoolean(isRunning());
		writeLong(getLastHandShake());
	}

	@Override
	protected void deserialize_() {
		this.running = readBoolean();
		this.lastHandShake = readLong();
	}

	@Override
	public void handleData() {
		Heart.getInstance().transfuse(this);
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

}
