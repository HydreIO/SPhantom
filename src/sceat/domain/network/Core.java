package sceat.domain.network;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.config.SPhantomConfig.McServerConfigObject;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.schedule.Schedule;
import sceat.domain.schedule.Scheduled;
import sceat.domain.schedule.Scheduler;
import sceat.domain.schedule.TimeUnit;
import sceat.domain.utils.New;

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

	/**
	 * a quoi sert cte map ? rip jsai plus on verra
	 */
	private ConcurrentHashMap<ServerType, Set<UUID>> playersByType = new ConcurrentHashMap<Server.ServerType, Set<UUID>>();

	/**
	 * Les vps via leur label
	 */
	private ConcurrentHashMap<String, Vps> vps = new ConcurrentHashMap<String, Vps>();

	/**
	 * Servers par type pour la gestion d'ouverture d'instance et de servers, remplis dans le packetHandler
	 */
	private ConcurrentHashMap<ServerType, Set<Server>> serversByType = new ConcurrentHashMap<Server.ServerType, Set<Server>>();

	public Core() {
		instance = this;
		Arrays.stream(ServerType.values()).forEach(t -> playersByType.put(t, new HashSet<UUID>()));
		Scheduler.getScheduler().register(this);
		call();
	}

	public ConcurrentHashMap<String, Vps> getVps() {
		return vps;
	}

	public ConcurrentHashMap<ServerType, Set<Server>> getServersByType() {
		return serversByType;
	}

	public void checkVps(String label) {
		if (getVps().contains(label)) return;
		new Vps(label, 0, InetAddress.getByName("127.0.0.1"), New.set()).register();
	}

	/**
	 * on récup les instances
	 */
	private void call() {
		// changer pour l'ecoute continue via rabbit des msg des symbiotes pour mettre a jour la map donc virer cette methode et réécrire
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
		Manager m = Manager.getInstance();
		int percent = getMode().getPercentPl();
		getServersByType().entrySet().forEach(e -> {
			ServerType key = e.getKey();
			Set<Server> srvs = e.getValue();
			int trigg = SPhantom.getInstance().getSphantomConfig().getInstances().get(key).getPlayersBeforeOpenNewInstance();
		});
		setProcess(false);
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
		Server.fromScratch(type, maxPlayers, ip, pack, destinationKeys)
		// SPhantom.getInstance().getIphantom().createServer(type, obj.getMaxPlayers(), /*ip*/, type.getPack(), type.getKeys());
	}

	public static enum OperatingMode {
		Eco(10),
		Normal(20),
		NoLag(40);

		private int percentPl;

		private OperatingMode(int percent) {
			this.percentPl = percent;
		}

		public int getPercentPl() {
			return percentPl;
		}
	}

}
