package sceat.domain.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sceat.SPhantom;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.utils.New;

@SuppressWarnings("unchecked")
public class ServerProvider {

	private static ServerProvider instance;
	private int priority = 0;
	private Defqon defqon = Defqon.FIVE;

	/**
	 * Map des instances préenregistré dans la config (généralement les gros dédié de base pour les joueurs constants)
	 * <p>
	 * on cherchera d'abord a remplir ceux la avant de toucher au instances vultr
	 */
	private ConcurrentHashMap<String, Vps> configInstances = new ConcurrentHashMap<String, Vps>();
	/**
	 * Map des instances suplémentaire loué a l'heure, mises a jour en fonction de la ram dispo sur les Vps et la ram demandée pour le type du serveur
	 */
	private ConcurrentHashMap<ServerType, Vps> ordered = new ConcurrentHashMap<ServerType, Vps>();

	public ServerProvider() {
		instance = this;
		SPhantom.getInstance().getSphantomConfig().getServers().stream().map(vs -> new Vps(vs.getName(), vs.getRam(), getByName(vs.getIp()), New.set()))
				.forEach(v -> configInstances.put(v.getLabel(), v));
	}

	public void incrementPriority() {
		this.priority++;
		checkDefqon();
	}

	private void checkDefqon() {
		if (getPriority() >= 29) setDefqon(Defqon.ONE);
		else if (getPriority() >= 19) setDefqon(Defqon.TWO);
		else if (getPriority() >= 10) setDefqon(Defqon.THREE);
		else if (getPriority() >= 3) setDefqon(Defqon.FOUR);
		else setDefqon(Defqon.FIVE);
	}

	public void decrementPriority() {
		if (this.priority <= 0) return;
		this.priority--;
		checkDefqon();
	}

	public void setDefqon(Defqon defqon) {
		this.defqon = defqon;
	}

	public Defqon getDefqon() {
		return defqon;
	}

	public int getPriority() {
		return priority;
	}

	public static enum Defqon {
		FIVE,
		FOUR,
		THREE,
		TWO,
		ONE
	}

	// internal use for bypass tryCatch block
	private InetAddress getByName(String name) {
		try {
			return InetAddress.getByName(name);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ServerProvider getInstance() {
		return instance;
	}

	public ConcurrentHashMap<ServerType, Vps> getOrdered() {
		return ordered;
	}

	public ConcurrentHashMap<String, Vps> getConfigInstances() {
		return configInstances;
	}

	/**
	 * On recup le vps aproprié puis on le remplace par le premier adéquat trouvé dans la liste des vps online
	 * <p>
	 * synchronized pour que les opérations de remplacement ne soit pas baisées
	 * 
	 * @param type
	 * @param exclude
	 *            chercher un vps autre que celui en parametre
	 * @return first proper vps for the serverType, null if no instance is found
	 */
	public synchronized Vps getVps(ServerType type, Optional<Vps> exclude) {
		boolean log = SPhantom.getInstance().logprovider;
		long time = System.currentTimeMillis();
		if (log) SPhantom.print("Asking Vps for type : " + type.name());
		SPhantomConfig sc = SPhantom.getInstance().getSphantomConfig();
		Vps vp = null;
		for (Vps vss : getConfigInstances().values())
			if (vss.getAvailableRam(true) >= sc.getRamFor(type)) { // recherche prioritaire dans les machines configurée (vps/dédié non loué a l'heure)
				vp = vss;
				if (exclude.isPresent() && vss == exclude.get()) continue;
				break;
			}
		Vps vf = getOrdered().get(type);
		if (vp == null) vp = exclude.isPresent() ? vf == exclude.get() ? null : vf : vf;
		if (log) SPhantom.print("Found vps : " + (vp == null ? "NULL :(" : vp.getLabel()));
		if (vp == null) {
			vp = searchFirst(sc.getRamFor(type), exclude);
			if (SPhantom.getInstance().logprovider) SPhantom.print("Force found vps : " + (vp == null ? "Not found again.. Houston we have a problem" : vp.getLabel()));
			if (vp == null) return null; // si on trouve vraiment pas de vps on return null et tant pis aucun serveur ne s'ouvrira il faudra attendre l'ouverture d'une instance automatiquement
		}
		int availableRam = vp.getAvailableRam(true) - sc.getRamFor(type);
		if (log) SPhantom.print("Available ram : " + availableRam);
		for (Entry<ServerType, Vps> e : ordered.entrySet()) {
			Vps value = e.getValue();
			ServerType key = e.getKey();
			int ramfor = sc.getRamFor(key);
			int ramavail = value == null ? -1 : value == vp ? availableRam : value.getAvailableRam(true);
			if (ramfor > ramavail) {
				Vps neew = searchFirst(ramfor, Optional.<Vps> of(vp), exclude);
				ordered.put(key, neew);
			}
		}
		if (log) SPhantom.print("Founded in " + (System.currentTimeMillis() - time) + "ms");
		return vp;
	}

	private Vps searchFirst(int ramNeeded, Optional<Vps>... exclude) {
		Set<Vps> comp = new HashSet<Vps>();
		Arrays.stream(exclude).filter(e -> e.isPresent()).forEach(o -> comp.add(o.get()));
		return Core.getInstance().getVps().values().stream().filter(vp -> vp.getAvailableRam(true) >= ramNeeded && (comp.isEmpty() || !comp.contains(vp))).findFirst().orElse(null);
	}

}
