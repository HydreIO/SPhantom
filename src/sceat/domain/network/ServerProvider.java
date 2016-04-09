package sceat.domain.network;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import sceat.SPhantom;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;

public class ServerProvider {

	private static ServerProvider instance;
	/**
	 * Map des serveurs mis a jour en fonction de la ram dispo sur les Vps et la ram demandée pour le type du serveur
	 */
	private ConcurrentHashMap<ServerType, Vps> ordered = new ConcurrentHashMap<ServerType, Vps>();

	public ServerProvider() {
		instance = this;
	}

	public static ServerProvider getInstance() {
		return instance;
	}

	private ConcurrentHashMap<ServerType, Vps> getOrdered() {
		return ordered;
	}

	/**
	 * On recup le vps aproprié puis on le remplace par le premier adéquat trouvé dans la liste des vps online
	 * <p>
	 * synchronized pour que les opérations de remplacement ne soit pas baisées
	 * 
	 * @param type
	 * @return
	 */
	public synchronized Vps getVps(ServerType type) {
		if (SPhantom.getInstance().logprovider) SPhantom.print("Asking Vps for type : " + type.name());
		Vps vp = getOrdered().get(type);
		if (SPhantom.getInstance().logprovider) SPhantom.print("Found vps : " + (vp == null ? "NULL :(" : vp.getLabel()));
		SPhantomConfig sc = SPhantom.getInstance().getSphantomConfig();
		if (vp == null) {
			vp = searchFirst(sc.getRamFor(type), Optional.empty());
			if (SPhantom.getInstance().logprovider) SPhantom.print("Force found vps : " + (vp == null ? "Not found again.. Houston we have a problem" : vp.getLabel()));
			if (vp == null) return null; // si on trouve vraiment pas de vps on return null et tant pis aucun serveur ne s'ouvrira il faudra attendre l'ouverture d'une instance automatiquement
		}
		int availableRam = vp.getAvailableRam() - sc.getRamFor(type);
		if (SPhantom.getInstance().logprovider) SPhantom.print("Available ram : " + availableRam);
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
