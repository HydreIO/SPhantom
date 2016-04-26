package sceat.domain.protocol.handler;

public class PacketWatchDog extends Thread {
	public static final long MAX_PACKET_READ_MILLIS = 5000;

	private PacketHandler owner;

	public PacketWatchDog(PacketHandler owner) {
		super("Packet WatchDog");
		this.owner = owner;
	}

	public void notifyEnd() {
		if (isAlive()) stopThread();
	}

	public void notifyStart() {
		if (!isAlive()) start();
	}

	@Override
	public synchronized void start() {
		super.start();
	}

	public void stopThread() {
		this.interrupt();
		super.start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(MAX_PACKET_READ_MILLIS);
			owner.reorganisePackets();
		} catch (InterruptedException ignored) {
			interrupt();
		}
	}
}
