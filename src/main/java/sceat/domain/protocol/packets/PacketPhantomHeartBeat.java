package sceat.domain.protocol.packets;

import sceat.SPhantom;
import sceat.domain.Heart;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

public class PacketPhantomHeartBeat extends PacketPhantom {

	private boolean running;
	private long lastHandShake = System.currentTimeMillis();

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
			if (SPhantom.getInstance().logHeart) Log.out("<<<<]RECV] PacketHeartBeat [Last " + new java.sql.Timestamp(getLastHandShake()).toString().substring(0, 16) + "]");
			Heart.getInstance().transfuse(this);
		} else if (tp == MessagesType.TAKE_LEAD) { // inutile mais en cas ou je rajoute un autre type pour ce pkt
			if (cameFromLocal()) return;
			if (SPhantom.getInstance().logPkt()) Log.out("<<<<]RECV] PacketTakeLead []");
			Heart.getInstance().transplant(this);
		}
	}

	public PacketPhantomHeartBeat handshake() {
		this.lastHandShake = System.currentTimeMillis();
		return this;
	}

	public void setLastHandShake(long lastHandShake) {
		this.lastHandShake = lastHandShake;
	}

	public void resetHandShake() {
		this.lastHandShake = System.currentTimeMillis();
	}

	public boolean isDead() {
		return System.currentTimeMillis() > getLastHandShake() + 15000;
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

	@Override
	public void send() {
		throw new IllegalAccessError("PacketPhantomHeartBeat is a particular packet ! don't use this methode for send it !");
	}

}
