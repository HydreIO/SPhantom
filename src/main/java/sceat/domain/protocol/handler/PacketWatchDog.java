package sceat.domain.protocol.handler;

import java.lang.Thread.State;

public class PacketWatchDog implements Runnable {
	public static final long MAX_PACKET_READ_MILLIS = 5000;

	private PacketHandler owner;
	private Thread thread;

	public PacketWatchDog(PacketHandler owner) {
		this.owner = owner;
		this.thread = new Thread(this, "Watchdog Thread");
	}

	public void notifyEnd() {
		if (thread.isAlive()) {
			thread.interrupt();
			flush();
		}
	}

	protected void flush() {
		this.thread = new Thread(this, "Watchdog Thread");
	}

	public void notifyStart() {
		if (!thread.isAlive()) {
			if (thread.getState() != State.NEW) flush();
			thread.start();
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(MAX_PACKET_READ_MILLIS);
			if (PacketHandler.getInstance().getRawPackets().size() > 30) owner.setNeedToSort(true);
		} catch (InterruptedException ignored) {
			thread.interrupt();
		}
	}
}
