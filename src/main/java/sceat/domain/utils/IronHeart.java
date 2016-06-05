package sceat.domain.utils;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import sceat.SPhantom;
import sceat.infra.connector.mq.RabbitMqConnector;
import fr.aresrpg.commons.domain.util.schedule.Schedule;
import fr.aresrpg.commons.domain.util.schedule.Scheduled;
import fr.aresrpg.commons.domain.util.schedule.Scheduler;
import fr.aresrpg.commons.domain.util.schedule.TimeUnit;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomHeartBeat;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomHeartBeat.BeatType;
import fr.aresrpg.sdk.protocol.util.Pulse;
import fr.aresrpg.sdk.system.Broker;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.system.Root;

public class IronHeart implements Scheduled {

	private static final IronHeart instance = new IronHeart();
	private ConcurrentLinkedDeque<Pulse> replicas = new ConcurrentLinkedDeque<>();
	private Pulse localPulse = Pulse.inject(Root.get().getSecurity());
	private boolean alive = true;
	private boolean local;

	private IronHeart() {

	}

	public static IronHeart get() {
		return instance;
	}

	public static void init(boolean local) {
		instance.local = local;
		if (local) Log.out("Local mode ! No replicas service.");
		else Scheduler.getScheduler().register(instance);
	}

	public Pulse getLocalPulse() {
		return localPulse;
	}

	/**
	 * Au démarrage Sphantom override les autres instance en s'imposant sur le réseau rabbit ! il prend donc le lead et dit aux autres sphantom de se mettre en pause
	 */
	public void takeLead() {
		if (isLocal()) return;
		if (SPhantom.getInstance().isLeading()) {
			Log.out("Instance already leading !");
			return;
		}
		Log.out("Take lead !");
		SPhantom.getInstance().setLead(true);
		getReplicas().add(getLocalPulse().pulse());
	}

	/**
	 * Quand Sphantom reçoit le packet lead d'une autre instance alors il se met en pause !
	 * 
	 * @param pulse
	 *            la pulsation de la nouvelle instance
	 */
	public void transplant(Pulse pulse) {
		Log.out("Another instance has taken the lead ! SPhantom is going to sleep");
		SPhantom.getInstance().setLead(false);
		getReplicas().addLast(pulse);
	}

	/**
	 * Quand Sphantom reçoit un packet Heatbeat il met a jour le timeout de la pulsation refférente du packet
	 * 
	 * @param p
	 */
	public void ironBeat(Pulse p) {
		getReplicas().stream().filter(p::equals).forEach(Pulse::pulse);
	}

	public static void beat(Pulse p) {
		instance.ironBeat(p);
	}

	public static void lead() {
		instance.takeLead();
	}

	public static void letLead(Pulse pulse) {
		instance.transplant(pulse);
	}

	/**
	 * Kill this heart (en général due au shutdown de sphantom)
	 */
	public void broke() {
		Log.out("Broking heart !");
		this.alive = false;
		SPhantom.getInstance().getPeaceMaker().shutdown();
	}

	public Deque<Pulse> getReplicas() {
		return replicas;
	}

	public boolean isAlive() {
		return this.alive;
	}

	public boolean isLocal() {
		return this.local;
	}

	/**
	 * Remove other instance when they aren't reachable and update the lead
	 */
	@Schedule(rate = 5, unit = TimeUnit.SECONDS)
	public void murder() {
		try {
			if (!isAlive() || getReplicas().isEmpty()) return;
			Broker.get().heartBeat(new PacketPhantomHeartBeat(BeatType.BEAT, getLocalPulse().pulse()));
			Iterator<Pulse> it = getReplicas().iterator();
			while (it.hasNext()) {
				Pulse da = it.next();
				if (da.isDead() && !da.getSecurity().isLocal()) it.remove();
			}
			RabbitMqConnector.getInstance().pause(!getReplicas().peekLast().getSecurity().isLocal());
		} catch (Exception e) {
			Log.trace(e);
		}
	}

}
