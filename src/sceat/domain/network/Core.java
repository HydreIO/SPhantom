package sceat.domain.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.config.SPhantomConfig.McServerConfigObject;
import sceat.domain.minecraft.RessourcePack;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.ServerProvider.Defqon;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.protocol.PacketSender;
import sceat.domain.protocol.packets.PacketPhantomBootServer;
import sceat.domain.protocol.packets.PacketPhantomDestroyInstance;
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
	private int deployedInstances = -1;

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
	}

	@Schedule(rate = 30, unit = TimeUnit.SECONDS)
	public void checkFreeSpace() {
		try {
			if (!this.initialised || !SPhantom.getInstance().isLeading() || SPhantom.getInstance().isLocal()) return;
			boolean decrem = true;
			int plTot = Manager.getInstance().getPlayersOnNetwork().size();
			for (ServerType v : ServerType.values()) {
				if (v == ServerType.Proxy) continue;
				int playersCount = playersByType.get(v).size();
				int totspace = SPhantom.getInstance().getSphantomConfig().getInstances().get(v).getMaxPlayers() * serversByType.get(v).size();
				if (SPhantom.logDiv()) SPhantom.print("Core.checkFreeSpace() |" + v + "|pl(" + playersCount + ")|totspace(" + totspace + ")");
				if (totspace <= 0) {
					if (SPhantom.logDiv()) SPhantom.print("Core.checkFreeSpace() |No server found for " + v + "|continue;");
					continue;
				}
				if (totspace * getMode().getPercentPl() <= playersCount) {
					ServerProvider.getInstance().incrementPriority();
					if (SPhantom.logDiv()) SPhantom.print("Core.checkFreeSpace() |incrementPriority();");
					int nbr = 3;
					if (plTot < 200) nbr = 1;
					else if (plTot < 1000) nbr = 2;
					deployServer(v, nbr);
					decrem = false;
				}
			}
			if (decrem) {
				ServerProvider.getInstance().decrementPriority();
				if (SPhantom.logDiv()) SPhantom.print("Core.checkFreeSpace() |decrementPriority(); /Yup/");
			}
		} catch (Exception e) {
			Main.printStackTrace(e);
		}
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
		if (SPhantom.logDiv()) SPhantom.print("Core.repairMap() | /!\\ Remaping players /!\\ !");
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
		try {
			new Vps(label, 0, InetAddress.getByName("127.0.0.1"), New.set()).register();
		} catch (UnknownHostException e) {
			Main.printStackTrace(e);
		}
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

	/**
	 * Change le mode
	 * 
	 * @param mode
	 * @param auto
	 *            true via sphantom, false via cmd
	 */
	public void setMode(OperatingMode mode, boolean auto) {
		if (SPhantom.logDiv()) SPhantom.print("Setting mode " + mode + " [" + (auto ? "AUTO" : "MANUAL") + "]");
		this.mode = mode;
	}

	/**
	 * Passage en mode eco entre 2h et 8h am
	 */
	@Schedule(rate = 20, unit = TimeUnit.MINUTES)
	public void modeUpdate() {
		if (SPhantom.getInstance().isTimeBetween(2, 3) || SPhantom.getInstance().isTimeBetween(7, 8)) setMode(OperatingMode.Eco, true);
		else if (SPhantom.getInstance().isTimeBetween(8, 9)) setMode(OperatingMode.Normal, true);
	}

	private boolean pro = false;

	@Schedule(rate = 45, unit = TimeUnit.SECONDS)
	public void VpsCount() {
		if (pro) return;
		pro = true;
		this.deployedInstances = SPhantom.getInstance().getIphantom().countDeployedInstance();
		if (SPhantom.logDiv()) SPhantom.print("Iphantom custom deployed instances = " + deployedInstances);
		pro = false;
	}

	@Schedule(rate = 2, unit = TimeUnit.MINUTES)
	public void coreUpdate() {
		try {
			if (isProcessing() || !this.initialised || !SPhantom.getInstance().isLeading() || SPhantom.getInstance().isLocal()) return;
			setProcess(true);
			Defqon defqon = ServerProvider.getInstance().getDefqon();
			if (SPhantom.logDiv()) SPhantom.print("coreUpdate() | Actual defqon : " + defqon);
			switch (defqon) {
				case FOUR:
					deployInstances(1 + getMode().getVar());
					break;
				case THREE:
					deployInstances(3 + getMode().getVar());
					break;
				case TWO:
					deployInstances(5 + getMode().getVar());
					break;
				case ONE:
					deployInstances(8 + getMode().getVar());
					break;
				default:
					Set<String> torm = new HashSet<String>();
					getVps().forEach((k, v) -> {
						if (v.getServers().isEmpty() && !ServerProvider.getInstance().getConfigInstances().containsKey(k)) {
							if (SPhantom.logDiv()) SPhantom.print("Vps reduction |Destroying instance : " + k);
							SPhantom.getInstance().getIphantom().destroyServer(k);
							torm.add(k);
						}
					});
					torm.forEach(s -> {
						getVps().remove(s);
						for (Entry<ServerType, Vps> e : ServerProvider.getInstance().getOrdered().entrySet())
							if (e.getValue().getLabel().equals(s)) ServerProvider.getInstance().getOrdered().put(e.getKey(), null);
					});
					PacketSender.getInstance().triggerDestroyInstance(new PacketPhantomDestroyInstance(torm));
					break;
			}

			setProcess(false);
		} catch (Exception e) {
			Main.printStackTrace(e);
		}
	}

	private Set<Vps> deployInstances(int nbr) {
		Set<Vps> vp = new HashSet<Vps>();
		int max = SPhantom.getInstance().getSphantomConfig().getMaxInstance();
		int current = (int) (deployedInstances == -1 ? getVps().mappingCount() : deployedInstances);
		for (int i = 0; i < nbr; i++) {
			if (current >= max) {
				SPhantom.print("[" + max + "] instances are already deployed ! For bypass this security please change the Sphantom config");
				break;
			}
			vp.add(SPhantom.getInstance().getIphantom().deployInstance(ServerLabel.newVpsLabel(), 8).register());
		}
		return vp;
	}

	private Set<Server> deployServer(ServerType type, int nbr) {
		if (type == ServerType.Proxy) return deployProxy(nbr);
		if (SPhantom.logDiv()) SPhantom.print("Deploy Server |Type_" + type + "|Nbr(" + nbr + ")");
		SPhantomConfig conf = SPhantom.getInstance().getSphantomConfig();
		McServerConfigObject obj = conf.getInstances().get(type);
		Set<Server> set = new HashSet<Server>();
		for (int i = 0; i < nbr; i++) {
			Vps vp = ServerProvider.getInstance().getVps(type);
			if (vp == null) break;
			Server srv = Server.fromScratch(type, obj.getMaxPlayers(), vp.getIp(), RessourcePack.RESSOURCE_PACK_DEFAULT, type.getKeys());
			set.add(srv);
			serversByType.get(type).add(srv);
			Manager.getInstance().getServersByLabel().put(srv.getLabel(), srv);
			vp.getServers().add(srv);
			PacketSender.getInstance().bootServer(new PacketPhantomBootServer(srv));
		}
		return set;
	}

	private Set<Server> deployProxy(int nbr) {
		SPhantom.print("[DEPLOY PROXY] Not implemented Yet !");
		return new HashSet<Server>();
	}

	public static enum OperatingMode {
		Eco(0.8F, -1),
		Normal(0.6F, 0),
		NoLag(0.4F, 1);

		private float percentPl;
		private int var;

		private OperatingMode(float percent, int var) {
			this.percentPl = percent;
			this.var = var;
		}

		public int getVar() {
			return var;
		}

		public float getPercentPl() {
			return percentPl;
		}
	}

}
