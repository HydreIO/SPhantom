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
import sceat.domain.minecraft.Statut;
import sceat.domain.network.ServerProvider.Defqon;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.schedule.Schedule;
import sceat.domain.schedule.Scheduled;
import sceat.domain.schedule.Scheduler;
import sceat.domain.schedule.TimeUnit;
import sceat.domain.utils.New;
import sceat.domain.utils.ServerLabel;

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
	 * pour gerer l'offre de vps je doit connaitre la marge qu'il reste par serverType, moins il y a de marge plus je vais incrementer la priorité du server provider
	 * <p>
	 * plus il y en a plus je decrement
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
		Arrays.stream(ServerType.values()).forEach(t -> {
			serversByType.put(t, new HashSet<Server>());
			playersByType.put(t, new HashSet<UUID>());
		});
		Scheduler.getScheduler().register(this);
		call();
	}

	@Schedule(rate = 10, unit = TimeUnit.SECONDS)
	public void checkFreeSpace() {
		boolean decrem = true;
		for (ServerType v : ServerType.values()) {
			int playersCount = playersByType.get(v).size();
			int totspace = SPhantom.getInstance().getSphantomConfig().getInstances().get(v).getMaxPlayers() * serversByType.get(v).size();
			int availableSpace = totspace - playersCount;
			if (totspace * getMode().getPercentPl() <= playersCount) {
				ServerProvider.getInstance().incrementPriority();
				decrem = false;
			}
		}
		if (decrem) ServerProvider.getInstance().decrementPriority();
	}

	/**
	 * on verifie içi si la map playersByType servant pour l'overspan est bien a jour ! si le nombre de joueurs n'est pas egal au nombre trouvé via la reduction directement efféctuée sur les serveurs
	 * <p>
	 * alors on remap manuellement la hashmap
	 */
	@Schedule(rate = 1, unit = TimeUnit.MINUTES)
	public void repairMap() {
		Arrays.stream(ServerType.values()).forEach(v -> {
			int totplayers = serversByType.get(v).stream().filter(s -> (s.getStatus() == Statut.OPEN)).mapToInt(ss -> ss.countPlayers()).reduce((a, b) -> a + b).getAsInt();
			if (totplayers != playersByType.get(v).size()) Core.this.remapPlayersByType();
		});
	}

	private void remapPlayersByType() {
		Arrays.stream(ServerType.values()).forEach(v -> {
			playersByType.put(v, serversByType.get(v).stream().filter(s -> (s.getStatus() == Statut.OPEN)).map(s -> s.getPlayers()).reduce((a, b) -> {
				a.addAll(b);
				return a;
			}).orElse(New.set()));
		});
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
		Defqon defqon = ServerProvider.getInstance().getDefqon();
		switch (defqon) {
			case FOUR:
				
				break;
			case THREE:

				break;
			case TWO:

				break;
			case ONE:

				break;
			default:
				break;
		}
		setProcess(false);
	}

	private void deployProxy() {

	}

	private Vps deployInstance() {
		return SPhantom.getInstance().getIphantom().deployInstance(ServerLabel.newVpsLabel(), 8).register();
	}

	private Set<Vps> deployInstances(int nbr) {
		Set<Vps> vp = new HashSet<Vps>();
		for (int i = 0; i < nbr; i++)
			vp.add(deployInstance());
		return vp;
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
		Eco(0.8F),
		Normal(0.6F),
		NoLag(0.4F);

		private float percentPl;

		private OperatingMode(float percent) {
			this.percentPl = percent;
		}

		public float getPercentPl() {
			return percentPl;
		}
	}

}
