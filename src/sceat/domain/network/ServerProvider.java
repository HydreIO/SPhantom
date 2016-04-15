package sceat.domain.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import sceat.SPhantom;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.utils.New;

public class ServerProvider {

	private static ServerProvider instance;

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

	private ConcurrentHashMap<ServerType, Vps> getOrdered() {
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
	 * @return first proper vps for the serverType, null if no instance is found
	 */
	public synchronized Vps getVps(ServerType type) {
		boolean log = SPhantom.getInstance().logprovider;
		long time = System.currentTimeMillis();
		if (log) SPhantom.print("Asking Vps for type : " + type.name());
		SPhantomConfig sc = SPhantom.getInstance().getSphantomConfig();
		Vps vp = null;
		for (Vps vss : getConfigInstances().values())
			if (vss.getAvailableRam() >= sc.getRamFor(type)) { // recherche prioritaire dans les machines configurée (vps/dédié non loué a l'heure)
				vp = vss;
				break;
			}
		if (vp == null) vp = getOrdered().get(type);
		if (log) SPhantom.print("Found vps : " + (vp == null ? "NULL :(" : vp.getLabel()));
		if (vp == null) {
			vp = searchFirst(sc.getRamFor(type), Optional.empty());
			if (SPhantom.getInstance().logprovider) SPhantom.print("Force found vps : " + (vp == null ? "Not found again.. Houston we have a problem" : vp.getLabel()));
			if (vp == null) return null; // si on trouve vraiment pas de vps on return null et tant pis aucun serveur ne s'ouvrira il faudra attendre l'ouverture d'une instance automatiquement
		}
		int availableRam = vp.getAvailableRam() - sc.getRamFor(type);
		if (log) SPhantom.print("Available ram : " + availableRam);
		for (Entry<ServerType, Vps> e : ordered.entrySet()) {
			Vps value = e.getValue();
			ServerType key = e.getKey();
			int ramfor = sc.getRamFor(key);
			int ramavail = value == null ? -1 : value == vp ? availableRam : value.getAvailableRam();
			if (ramfor > ramavail) {
				Vps neew = searchFirst(ramfor, Optional.<Vps> of(vp));
				ordered.put(key, neew);
			}
		}
		if (log) SPhantom.print("Founded in " + (System.currentTimeMillis() - time) + "ms");
		return vp;
	}

	private Vps searchFirst(int ramNeeded, Optional<Vps> exclude) {
		Vps first = null;
		for (Vps vp : Core.getInstance().getVps().values())
			if (vp.getAvailableRam() >= ramNeeded && (!exclude.isPresent() || exclude.get() != vp)) {
				first = vp;
				break;
			}
		return first;
	}

}
