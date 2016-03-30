package sceat.domain.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import sceat.SPhantom;
import sceat.domain.network.Server.ServerType;
import sceat.domain.schedule.Schedule;
import sceat.domain.schedule.Scheduled;
import sceat.domain.schedule.Scheduler;
import sceat.domain.schedule.TimeUnit;

/**
 * This is where the magic happens
 * 
 * @author MrSceat
 *
 */
public class Core implements Scheduled {

	private OperatingMode mode = OperatingMode.Normal;
	private boolean process = false;
	private static Core instance;

	private ConcurrentHashMap<ServerType, Set<UUID>> playersByType = new ConcurrentHashMap<Server.ServerType, Set<UUID>>();

	public Core() {
		instance = this;
		Arrays.stream(ServerType.values()).forEach(t -> playersByType.put(t, new HashSet<UUID>()));
		Scheduler.getScheduler().register(this);
	}

	public static Core getInstance() {
		return instance;
	}

	public ConcurrentHashMap<ServerType, Set<UUID>> getPlayersByType() {
		return playersByType;
	}

	public int countPlayers(ServerType type) {
		return getPlayersByType().get(type).size();
	}

	public OperatingMode getMode() {
		return mode;
	}

	private boolean isProcessing() {
		return this.process;
	}

	private void setProcess(boolean var) {
		this.process = var;
	}

	public void setMode(OperatingMode mode) {
		this.mode = mode;
	}

	/**
	 * Passage en mode eco entre 2h et 8h am
	 */
	@Schedule(rate = 20, unit = TimeUnit.MINUTES)
	public void modeUpdate() {
		if (SPhantom.getInstance().isTimeBetween(2, 8)) setMode(OperatingMode.Eco);
	}

	@Schedule(rate = 30, unit = TimeUnit.SECONDS)
	public void coreUpdate() {
		if (isProcessing()) return;
		setProcess(true);

		setProcess(false);
	}

	public static enum OperatingMode {
		Eco,
		Normal,
		NoLag
	}

}
