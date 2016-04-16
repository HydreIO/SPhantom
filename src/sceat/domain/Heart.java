package sceat.domain;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.protocol.PacketSender;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
import sceat.domain.protocol.packets.PacketPhantomSecurity;
import sceat.domain.schedule.Schedule;
import sceat.domain.schedule.Scheduled;
import sceat.domain.schedule.Scheduler;
import sceat.domain.schedule.TimeUnit;

public class Heart implements Scheduled {

	private static Heart instance;
	private ConcurrentLinkedDeque<PacketPhantomHeartBeat> replicas = new ConcurrentLinkedDeque<PacketPhantomHeartBeat>();
	private PacketPhantomHeartBeat localBeat;
	private boolean alive;
	private boolean local;

	public Heart(boolean local) {
		instance = this;
		this.local = local;
		this.alive = true;
		if (local) SPhantom.print("Local mode ! No replicas service.");
		else Scheduler.getScheduler().register(this);
		this.localBeat = new PacketPhantomHeartBeat(new PacketPhantomSecurity(Main.serial.toString(), Main.security.toString()));
	}

	public static Heart getInstance() {
		return instance;
	}

	public ConcurrentLinkedDeque<PacketPhantomHeartBeat> getReplicas() {
		return replicas;
	}

	public PacketPhantomHeartBeat getLocalBeat() {
		return localBeat;
	}

	public Heart takeLead() {
		if (this.local) return this; // if local, disable rabbit & replica
		SPhantom.print("Take lead !");
		SPhantom.getInstance().setLead(true);
		getReplicas().add(getLocalBeat().handshake());
		PacketSender.getInstance().takeLead(getLocalBeat());
		return this;
	}

	/**
	 * Called when another heart take the lead
	 * 
	 * @param json
	 */
	public void transplant(PacketPhantomHeartBeat pkt) {
		if (pkt.cameFromLocal()) return;
		SPhantom.print("Another instance has taken the lead ! SPhantom is going to sleep");
		SPhantom.getInstance().setLead(false);
		getReplicas().addLast(dao);
	}

	/**
	 * Called when others hearts heartbeat
	 * 
	 * @param json
	 */
	public void transfuse(PacketPhantomHeartBeat pkt) {
		getReplicas().stream().filter(d -> d.getSecurity().correspond(pkt.getSecurity())).forEach(this::handShake);
	}

	public boolean isAlive() {
		return this.alive;
	}

	private void handShake(PacketPhantomHeartBeat bt) {
		bt.handshake();
	}

	/**
	 * Kill this heart
	 */
	public void broke() {
		SPhantom.print("Broking heart !");
		this.alive = false;
		SPhantom.getInstance().getPeaceMaker().shutdown();
	}

	/**
	 * Remove other instance when they aren't reachable and update the lead
	 */
	@Schedule(rate = 1, unit = TimeUnit.SECONDS)
	public void murder() {
		if (!isAlive() || getReplicas().isEmpty()) return;
		PacketSender.getInstance().heartBeat(getLocalBeat().handshake());
		Iterator<DAO_HeartBeat> it = getReplicas().iterator();
		while (it.hasNext()) {
			DAO_HeartBeat da = it.next();
			if (da.isDead() && !da.isLocal()) it.remove();
		}
		PacketSender.getInstance().pause(!getReplicas().peekLast().isLocal());
	}

}
