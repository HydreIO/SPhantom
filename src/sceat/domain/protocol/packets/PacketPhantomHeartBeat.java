package sceat.domain.protocol.packets;

public class PacketPhantomHeartBeat extends PacketPhantom {

	private boolean running;
	private long lastHandShake;

	public PacketPhantomHeartBeat() {
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
	protected <T extends PacketPhantom> T serialize_() {
		writeBoolean(isRunning());
		writeLong(getLastHandShake());
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends PacketPhantom> T deserialize_() {
		this.running = readBoolean();
		this.lastHandShake = readLong();
		return (T) this;
	}

}
