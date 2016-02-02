package sceat.domain;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.messaging.dao.DAO_HeartBeat;
import sceat.domain.schedule.Schedule;
import sceat.domain.schedule.Scheduled;
import sceat.domain.schedule.Scheduler;
import sceat.domain.schedule.TimeUnit;

public class Heart implements Scheduled {

	private static Heart instance;
	private ConcurrentLinkedDeque<DAO_HeartBeat> replicas = new ConcurrentLinkedDeque<DAO_HeartBeat>();
	private DAO_HeartBeat localBeat;
	private boolean alive;

	public Heart() {
		instance = this;
		this.alive = true;
		Scheduler.getScheduler().register(this);
		this.localBeat = new DAO_HeartBeat(Main.serial, Main.security);
	}

	public static Heart getInstance() {
		return instance;
	}

	public ConcurrentLinkedDeque<DAO_HeartBeat> getReplicas() {
		return replicas;
	}

	public DAO_HeartBeat getLocalBeat() {
		return localBeat;
	}

	public Heart takeLead() {
		SPhantom.print("Take lead !");
		getReplicas().add(getLocalBeat().handshake());
		SPhantom.getInstance().getMessageBroker().takeLead(getLocalBeat());
		return this;
	}

	/**
	 * Called when another heart take the lead
	 * 
	 * @param json
	 */
	public void transplant(String json) {
		DAO_HeartBeat dao = DAO_HeartBeat.fromJson(json);
		if (dao.correspond(getLocalBeat())) return;
		SPhantom.print("Another instance has taken the lead ! SPhantom is going to sleep");
		getReplicas().addLast(dao);
		SPhantom.getInstance().pause();
	}

	/**
	 * Called when others hearts heartbeat
	 * 
	 * @param json
	 */
	public void transfuse(String json) {
		DAO_HeartBeat dao = DAO_HeartBeat.fromJson(json);
		for (DAO_HeartBeat d : getReplicas())
			if (d.correspond(dao)) d.handshake();
	}

	public boolean isAlive() {
		return this.alive;
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
		SPhantom.getInstance().getMessageBroker().heartBeat(getLocalBeat().handshake());
		Iterator<DAO_HeartBeat> it = getReplicas().iterator();
		while (it.hasNext()) {
			DAO_HeartBeat da = it.next();
			if (da.isDead() && !da.isLocal()) {
				it.remove();
			}
		}
		if (!getReplicas().peekLast().isLocal()) {
			SPhantom.getInstance().pause();
		} else {
			SPhantom.getInstance().wakeUp();
		}
	}

}