package sceat.domain.protocol.packets;

import sceat.SPhantom;
import sceat.domain.Heart;
import sceat.domain.protocol.MessagesType;

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
	public void handleData(MessagesType tp) {
		if (tp == MessagesType.HEART_BEAT) {
			if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketHeartBeat [Last " + new java.sql.Timestamp(getLastHandShake()).toString().substring(0, 16) + "]");
			Heart.getInstance().transfuse(this);
		} else if (tp == MessagesType.TAKE_LEAD) { // inutile mais en cas ou je rajoute un autre type pour ce pkt
			if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketTakeLead []");
			Heart.getInstance().transfuse(this);
		}
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
