package sceat.domain.protocol.handler;

public class PacketWatchDog implements Runnable {
	public static final long MAX_PACKET_READ_MILLIS = 5000;

	private PacketHandler owner;
	private Thread thread;

	public PacketWatchDog(PacketHandler owner) {
		this.owner = owner;
		this.thread = new Thread(this , "Watchdog Thread");
	}

	public void notifyEnd() {
		if (thread.isAlive()){
			thread.interrupt();
			this.thread = new Thread(this , "Watchdog Thread");
		}
	}

	public void notifyStart() {
		if (!thread.isAlive())
			thread.start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(MAX_PACKET_READ_MILLIS);
			owner.reorganisePackets();
		} catch (InterruptedException ignored) {
			thread.interrupt();
		}
	}
}
