package sceat.domain.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import sceat.SPhantom;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.config.SPhantomConfig.McServerConfigObject;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
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
	private boolean initialised = false;
	private static Core instance;

	private ConcurrentHashMap<ServerType, Set<UUID>> playersByType = new ConcurrentHashMap<Server.ServerType, Set<UUID>>();
	private CopyOnWriteArraySet<Vps> vps = new CopyOnWriteArraySet<Vps>();

	public Core() {
		instance = this;
		Arrays.stream(ServerType.values()).forEach(t -> playersByType.put(t, new HashSet<UUID>()));
		Scheduler.getScheduler().register(this);
		call();
	}

	/**
	 * on r�cup les instances
	 */
	private void call() {
		SPhantom.print("Initialising Core...");
		SPhantom.getInstance().getExecutor().execute(() -> {
			SPhantom.print("Retrieving online existing instances from ETCD (vps/dedicated/...)");
			long cur = System.currentTimeMillis();
			Vps[] instances = SPhantom.getInstance().getIphantom().retrieveOnlineInstances();
			Arrays.stream(instances).forEach(vps::add);
			Core.this.initialised = true;
			SPhantom.print("Core initialised ! (" + (System.currentTimeMillis() - cur) + "ms)");
		});
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
		if (isProcessing() || !this.initialised || !SPhantom.getInstance().isLeading() || SPhantom.getInstance().isLocal()) return;
		setProcess(true);

		setProcess(false);
	}

	public void growServers(ServerType type, int nbr) {

	}

	private void deployProxy() {

	}

	private void deployServer(ServerType type) {
		if (type == ServerType.Proxy) {
			deployProxy();
			return;
		}
		SPhantomConfig conf = SPhantom.getInstance().getSphantomConfig();
		McServerConfigObject obj = conf.getInstances().get(type);
		SPhantom.getInstance().getIphantom().createServer(type, obj.getMaxPlayers(), /*ip*/, type.getPack(), type.getKeys());
	}

	public static enum OperatingMode {
		Eco,
		Normal,
		NoLag
	}

}
