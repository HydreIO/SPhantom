package sceat.domain.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.compute.Sequencer;
import sceat.domain.compute.Sequencer.BoolBiConsumer;
import sceat.domain.compute.Sequencer.Chainer;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.config.SPhantomConfig.McServerConfigObject;
import sceat.domain.network.server.Servers;
import sceat.domain.network.server.Vpss;
import sceat.domain.trigger.PhantomTrigger;
import sceat.domain.utils.New;
import sceat.domain.utils.ServerLabel;
import fr.aresrpg.commons.concurrent.ConcurrentHashMap;
import fr.aresrpg.commons.concurrent.ConcurrentMap;
import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.commons.util.collection.Set;
import fr.aresrpg.commons.util.schedule.Schedule;
import fr.aresrpg.commons.util.schedule.Scheduled;
import fr.aresrpg.commons.util.schedule.Scheduler;
import fr.aresrpg.commons.util.schedule.TimeUnit;
import fr.aresrpg.sdk.mc.ServerType;
import fr.aresrpg.sdk.mc.Statut;
import fr.aresrpg.sdk.network.Server;
import fr.aresrpg.sdk.network.Vps;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomBootServer;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomDestroyInstance;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomReduceServer;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.util.Defqon;
import fr.aresrpg.sdk.util.OperatingMode;

/**
 * This is where the magic happens
 * 
 * @author MrSceat
 *
 */
public class Core implements Scheduled {

	private OperatingMode mode = OperatingMode.NORMAL;
	private boolean process = false;
	private boolean initialised = false;
	private static Core instance = new Core();
	private int deployedInstances = -1;
	private boolean procc = false;
	private boolean pro = false;
	/**
	 * pour gerer l'offre de vps je doit connaitre la marge qu'il reste par serverType, moins il y a de marge plus je vais incrementer la priorit� du server provider
	 * <p>
	 * plus il y en a plus je decrement
	 */
	private ConcurrentHashMap<ServerType, Set<UUID>> playersByType = new ConcurrentHashMap<>();

	/**
	 * Les vps via leur label
	 */
	private ConcurrentHashMap<String, Vps> vps = new ConcurrentHashMap<>();

	/**
	 * Servers par type pour la gestion d'ouverture d'instance et de servers, remplis dans le packetHandler
	 */
	private ConcurrentHashMap<ServerType, Set<Server>> serversByType = new ConcurrentHashMap<>();

	private Core() {
	}

	public static void init() {
		Core core = getInstance();
		Arrays.stream(ServerType.values()).forEach(t -> { // NOSONAR closeable
					core.serversByType.put(t, new HashSet<Server>());
					core.playersByType.put(t, new HashSet<UUID>());
				});
		SPhantom.getInstance().getSphantomConfig().getServers().stream().map(vs -> new Vps(vs.getName(), vs.getRam(), core.getByName(vs.getIp()), New.set(), System.currentTimeMillis()))
				.forEach(v -> ServerProvider.getInstance().getConfigInstances().put(v.getLabel(), Vpss.register(v)));
		Scheduler.getScheduler().register(core);
		core.initialised = true;
	}

	// internal use for bypass tryCatch block
	private InetAddress getByName(String name) {
		try {
			return InetAddress.getByName(name);
		} catch (UnknownHostException e) {
			Log.trace(e);
		}
		return null;
	}

	@Schedule(rate = 30, unit = TimeUnit.SECONDS)
	public void checkFreeSpace() {
		try {
			if (procc || !this.initialised || !SPhantom.getInstance().isLeading() || SPhantom.getInstance().isLocal()) return;
			procc = true;
			boolean decrem = true;
			int plTot = Manager.getInstance().getPlayersOnNetwork().size();
			for (ServerType v : ServerType.values()) { // NOSONAR squid:S135
				if (v == ServerType.PROXY) continue;
				int playersCount = playersByType.get(v).size();
				int totspace = SPhantom.getInstance().getSphantomConfig().getInstances().get(v).getMaxPlayers() * serversByType.get(v).size();
				if (SPhantom.logDiv()) Log.out("[FreeSpace CHECK] |" + v + "|players(" + playersCount + ")|totspace(" + totspace + ")");
				if (totspace <= 0) continue;
				if (totspace * getMode().getPercentPl() <= playersCount) {
					ServerProvider.getInstance().incrementPriority();
					if (SPhantom.logDiv()) Log.out("[DEFQON |incrementPriority();"); // NOSONAR squid minecraft
					int nbr = 3;
					if (plTot < 200) nbr = 1; // NOSONAR le poulpe est le meilleur ami de l'homme
					else if (plTot < 1000) nbr = 2;
					deployServer(v, nbr);
					decrem = false;
				} else if (totspace * (getMode().getPercentPl() / 2) > playersCount && serversByType.get(v).stream().filter(sr -> sr.getStatus() == Statut.OPEN).count() > 1) reduceServer(v);
			}
			if (decrem) {
				ServerProvider.getInstance().decrementPriority();
				if (SPhantom.logDiv()) Log.out("[DEFQON |decrementPriority(); /Yup/");
			}
		} catch (Exception e) {
			procc = false;
			Main.printStackTrace(e);
		}
		procc = false;
	}

	/**
	 * Cette methode du futur confectionn�es par mes soins m'a pris 3 putain de jours !
	 * <p>
	 * Elle effectue une sorte de d�fragmentation pour regrouper un maximum les serveurs sur les vps et ainsi pouvoir fermer les instances en trop ! Economie d'argent morray !
	 */
	@Schedule(rate = 1, unit = TimeUnit.HOURS)
	public void balk() {
		try {
			if (SPhantom.getInstance().isLocal()) return;
			Sequencer.<Vps, Server> phantomSequencing((list, tk, dispatcher, worker, noClose, adder, canAccept, thenAdd) -> {
				Queue<Vps> queue = new LinkedList<>();
				list.sort((t1, t2) -> t1.compareTo(t2));
				list.forEach(queue::add);
				final int size = queue.size();
				for (int i = 0; i < size; i++)
					Chainer.<Queue<Vps>> of(q -> {
						Vps v6 = q.poll();
						q.forEach(e -> dispatcher.dispatch(e, tk.take(v6), q, worker, noClose, adder, canAccept, thenAdd));
						return q;
					});
			}, new ArrayList<Vps>(getVps().values()), takesupp -> takesupp.getServers(),
					(v6, v6coll, list, worker, noClose, adder, canAccept, thenAdd) -> list.stream().map(v -> worker.transfert(v, v6coll, noClose, adder, canAccept, thenAdd)).reduce((a, b) -> a && b)
							.get(), (v, collec, noclos, addr, canaccp, theadd) -> collec.stream().filter(noclos).map(s -> addr.add(v, s, canaccp, theadd)).reduce((a, b) -> a && b).get(),
					sz -> sz.getStatus() != Statut.CLOSING && sz.getStatus() != Statut.REDUCTION && sz.getStatus() != Statut.CRASHED && sz.getStatus() != Statut.OVERHEAD,
					(vz, sr, predicate, consume) -> predicate.test(vz, sr) && consume.accept(vz, sr), (vt, st) -> Vpss.canAccept(vt, st), BoolBiConsumer.<Vps, Server> of((uv, ud) -> {
						ud.setStatus(Statut.REDUCTION);
						Log.out("balk() [DEFRAGMENTATION SEQUENCING] | Reduction on " + ud.getLabel() + " |Actual Vps : " + ud.getVpsLabel());
						new PacketPhantomReduceServer(ud.getLabel(), uv.getLabel()).send();
						Core.getInstance().deployServerOnVps(ud.getType(), uv, true);
						return true;
					}));
		} catch (Exception e) {
			Main.printStackTrace(e);
		}
	}

	/**
	 * on verifie içi si la map playersByType servant pour l'overspan est bien a jour ! si le nombre de joueurs n'est pas egal au nombre trouv� via la reduction directement efféctuée sur les serveurs
	 * <p>
	 * alors on remap manuellement la hashmap
	 */
	@Schedule(rate = 1, unit = TimeUnit.MINUTES)
	public void repairMap() {
		Arrays.stream(ServerType.values()).forEach(v -> { // NOSONAR closeable
					if (!serversByType.get(v).isEmpty()) {
						int totplayers = serversByType.get(v).stream().filter(s -> s.getStatus() == Statut.OPEN).mapToInt(ss -> ss.countPlayers()).reduce((a, b) -> a + b).orElse(0);
						if (totplayers != playersByType.get(v).size()) Core.this.remapPlayersByType();
					}
				});
	}

	private void remapPlayersByType() {
		if (SPhantom.logDiv()) Log.out("[Map repairing] | /!\\ Remaping players /!\\ !");
		Arrays.stream(ServerType.values()).forEach(v -> { // NOSONAR closeable
					playersByType.put(v, serversByType.get(v).stream().filter(s -> s.getStatus() == Statut.OPEN).map(s -> s.getPlayers()).reduce((a, b) -> {
						a.addAll(b);
						return a;
					}).orElseGet(New::set));
				});
	}

	public ConcurrentHashMap<String, Vps> getVps() { // NOSONAR ouai car la ConcurrentMap.mappingCount exist pas
		return vps;
	}

	public ConcurrentMap<ServerType, Set<Server>> getServersByType() { // NOSONAR
		return serversByType;
	}

	public void checkVps(String label) {
		if (getVps().containsKey(label)) return;
		try {
			Vpss.register(new Vps(label, 0, InetAddress.getLocalHost(), New.set(), System.currentTimeMillis()));
		} catch (UnknownHostException e) {
			Main.printStackTrace(e);
		}
	}

	public static Core getInstance() {
		return instance;
	}

	public ConcurrentMap<ServerType, Set<UUID>> getPlayersByType() {
		return playersByType;
	}

	public int countPlayers(ServerType type) {
		return getPlayersByType().safeGet(type).size();
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
		if (SPhantom.logDiv()) Log.out("Setting mode " + mode + " [" + (auto ? "AUTO" : "MANUAL") + "]");
		PhantomTrigger.getAll().forEach(t -> t.handleOpMode(mode));
		this.mode = mode;
	}

	/**
	 * Passage en mode eco entre 2h et 8h am
	 */
	@Schedule(rate = 20, unit = TimeUnit.MINUTES)
	public void modeUpdate() {
		if (SPhantom.getInstance().isTimeBetween(2, 3) || SPhantom.getInstance().isTimeBetween(7, 8)) setMode(OperatingMode.ECO, true);
		else if (SPhantom.getInstance().isTimeBetween(8, 9)) setMode(OperatingMode.NORMAL, true);
	}

	@Schedule(rate = 45, unit = TimeUnit.SECONDS)
	public void vpsCount() {
		if (pro || SPhantom.getInstance().isLocal()) return;
		pro = true;
		this.deployedInstances = SPhantom.getInstance().getIphantom().countDeployedInstance();
		if (SPhantom.logDiv()) Log.out("[Network] Custom deployed instances = " + deployedInstances);
		pro = false;
	}

	@Schedule(rate = 2, unit = TimeUnit.MINUTES)
	public void coreUpdate() {
		try {
			if (isProcessing() || !this.initialised || !SPhantom.getInstance().isLeading() || SPhantom.getInstance().isLocal()) return;
			setProcess(true);
			Defqon defqon = ServerProvider.getInstance().getDefqon();
			if (SPhantom.logDiv()) Log.out("coreUpdate() | Actual defqon : " + defqon);
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
					Set<String> torm = new HashSet<>();
					getVps().forEach((k, v) -> {
						if (v.canBeDestroyed() && v.getServers().isEmpty() && !ServerProvider.getInstance().getConfigInstances().containsKey(k)) {
							if (SPhantom.logDiv()) Log.out("Vps reduction |Destroying instance : " + k);
							if (v != null) PhantomTrigger.getAll().forEach(t -> t.handleVps(v));
							SPhantom.getInstance().getIphantom().destroyServer(k);
							torm.add(k);
						}
					});
					new PacketPhantomDestroyInstance(torm).send();
					break;
			}

		} catch (Exception e) {
			setProcess(false);
			Main.printStackTrace(e);
		}
		setProcess(false);
	}

	private Set<Vps> deployInstances(int nbr) {
		Set<Vps> vp = new HashSet<>();
		int max = SPhantom.getInstance().getSphantomConfig().getMaxInstance();
		int current = (int) (deployedInstances == -1 ? getVps().mappingCount() : deployedInstances);
		for (int i = 0; i < nbr; i++) {
			if (current >= max) {
				Log.out("[" + max + "] instances are already deployed ! For bypass this security please change the Sphantom config");
				break;
			}
			vp.add(Vpss.register(SPhantom.getInstance().getIphantom().deployInstance(ServerLabel.newVpsLabel(), SPhantom.getInstance().getSphantomConfig().getDeployedVpsRam())));
		}
		return vp;
	}

	/**
	 * generally used for create srv with cmd
	 * 
	 * @param type
	 */
	public void forceDeployServer(ServerType type, int nbr) {
		if (!checkSynchronized()) return;
		if (type == ServerType.PROXY) Log.out("Can't deploy proxy forced !");
		else {
			Log.out("Deploy Server |Type_" + type + "|Nbr(" + nbr + ")");
			SPhantomConfig conf = SPhantom.getInstance().getSphantomConfig();
			McServerConfigObject obj = conf.getInstances().get(type);
			for (int i = 0; i < nbr; i++) {
				Vps vp = ServerProvider.getInstance().getVps(type, Optional.empty());
				if (vp == null) {
					Log.out("No vps available ! Deploying instance, please wait and retry in few minutes...");
					deployInstances(1);
					break;
				}
				int port = Manager.getInstance().genPort(type);
				if (port == -1) {
					Log.out("[DeployForced] Can't deploy server ! All port fort the type('" + type.name() + "')  are already in use");
					return;
				}
				Server srv = Servers.fromScratch(type, obj.getMaxPlayers(), vp.getIp(), port);
				srv.setVpsLabel(vp.getLabel());
				Log.out("Deploying server [Label('" + srv.getLabel() + "')|State('" + srv.getStatus() + "')|MaxP(" + srv.getMaxPlayers() + ")]");
				serversByType.get(type).add(srv);
				Manager.getInstance().getServersByLabel().put(srv.getLabel(), srv);
				vp.getServers().add(srv);
				new PacketPhantomBootServer(srv, obj.getRamNeeded()).send();
			}
		}
	}

	private boolean checkSynchronized() {
		if (!SPhantom.isSynchronized()) {
			Log.out("Sphantom is not synchronized yet ! server/instance deploying cancelled");
			return false;
		}
		return true;
	}

	private Set<Server> deployServer(ServerType type, int nbr) {
		if (!checkSynchronized()) return new HashSet<>();
		if (type == ServerType.PROXY) return deployProxy(nbr);
		if (SPhantom.logDiv()) Log.out("Deploy Server |Type_" + type + "|Nbr(" + nbr + ")");
		SPhantomConfig conf = SPhantom.getInstance().getSphantomConfig();
		McServerConfigObject obj = conf.getInstances().get(type);
		Set<Server> set = new HashSet<>();
		for (int i = 0; i < nbr; i++) { // NOSONAR
			Vps vp = ServerProvider.getInstance().getVps(type, Optional.empty());
			if (vp == null) break;
			int port = Manager.getInstance().genPort(type);
			if (port == -1) {
				Log.out("[DeployAuto] Can't deploy server ! All port fort the type('" + type.name() + "') are already in use");
				break;
			}
			Server srv = Servers.fromScratch(type, obj.getMaxPlayers(), vp.getIp(), port);
			set.add(srv);
			srv.setVpsLabel(vp.getLabel());
			serversByType.get(type).add(srv);
			Manager.getInstance().getServersByLabel().put(srv.getLabel(), srv);
			vp.getServers().add(srv);
			new PacketPhantomBootServer(srv, obj.getRamNeeded()).send();
		}
		return set;
	}

	public void deployServerOnVps(ServerType type, Vps v, boolean fromBalk) {
		if (type == ServerType.PROXY) {
			deplyProxyOnVps(v);
			return;
		}
		SPhantomConfig conf = SPhantom.getInstance().getSphantomConfig();
		McServerConfigObject obj = conf.getInstances().get(type);
		if (fromBalk) Log.out("[DEFRAGMENTATION SEQUENCING] | Transfert on " + v.getLabel() + " |Type : " + type + "\n_______________________________________________]");
		else if (SPhantom.logDiv()) Log.out("Deploy Server ON VPS |Type_" + type + "|Vps = " + v.getLabel());
		int port = Manager.getInstance().genPort(type);
		if (port == -1) {
			Log.out("[DeployOnVps] Can't deploy server ! All port fort the type('" + type.name() + "') are already in use");
			return;
		}
		Server srv = Servers.fromScratch(type, obj.getMaxPlayers(), v.getIp(), port);
		serversByType.get(type).add(srv);
		Manager.getInstance().getServersByLabel().put(srv.getLabel(), srv);
		v.getServers().add(srv);
		srv.setVpsLabel(v.getLabel());
		new PacketPhantomBootServer(srv, obj.getRamNeeded()).send();
	}

	/**
	 * set reduction statut to the server of @param type who has less players
	 * 
	 * @param type
	 */
	private void reduceServer(ServerType type) {
		if (type == ServerType.PROXY) {
			reduceProxy();
			return;
		}
		Optional<Server> s = getServersByType().safeGet(type).stream().filter(sr -> sr.getStatus() == Statut.OPEN).min(Comparator.comparingInt(Server::countPlayers));
		if (!s.isPresent()) return;
		Server srv = s.get().setStatus(Statut.REDUCTION);
		new PacketPhantomReduceServer(srv.getLabel(), srv.getVpsLabel()).send();
	}

	private void reduceProxy() {
		// hum
	}

	private Set<Server> deployProxy(int nbr) {
		Log.out("[DEPLOY PROXY] Not implemented Yet ! " + nbr);
		return new HashSet<>();
	}

	private void deplyProxyOnVps(Vps v) {
		v.getAvailableRam(); // sonar baisé
		Log.out("[DEPLOY PROXY] Not implemented Yet !");
	}

}
