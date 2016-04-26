package sceat.domain;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.protocol.PacketSender;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
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
		this.localBeat = new PacketPhantomHeartBeat();
		this.localBeat.setSecu(SPhantom.getInstance().getSecurity());
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
		SPhantom.print("Another instance has taken the lead ! SPhantom is going to sleep");
		SPhantom.getInstance().setLead(false);
		getReplicas().addLast(pkt);
	}

	/**
	 * Called when others hearts heartbeat
	 * 
	 * @param json
	 */
	public void transfuse(PacketPhantomHeartBeat pkt) {
		getReplicas().stream().filter(d -> d.getSecu().correspond(pkt.getSecu())).forEach(this::handShake);
	}

	public boolean isAlive() {
		return this.alive;
	}

	private void handShake(PacketPhantomHeartBeat bt) {
		bt = new PacketPhantomHeartBeat();
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
	@Schedule(rate = 5, unit = TimeUnit.SECONDS)
	public void murder() {
		try {
			if (!isAlive() || getReplicas().isEmpty()) return;
			this.localBeat = new PacketPhantomHeartBeat();
			PacketSender.getInstance().heartBeat(getLocalBeat());
			Iterator<PacketPhantomHeartBeat> it = getReplicas().iterator();
			while (it.hasNext()) {
				PacketPhantomHeartBeat da = it.next();
				if (da.isDead() && !da.getSecu().isLocal()) it.remove();
			}
			PacketSender.getInstance().pause(!getReplicas().peekLast().getSecu().isLocal());
		} catch (Exception e) {
			Main.printStackTrace(e);
		}
	}

}
