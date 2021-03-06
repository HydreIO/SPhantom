package sceat.domain.network;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;

import sceat.SPhantom;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.network.server.Vpss;
import sceat.domain.trigger.PhantomTrigger;
import fr.aresrpg.commons.domain.concurrent.ConcurrentHashMap;
import fr.aresrpg.commons.domain.concurrent.ConcurrentMap;
import fr.aresrpg.commons.domain.util.collection.HashSet;
import fr.aresrpg.commons.domain.util.collection.Set;
import fr.aresrpg.sdk.network.Vps;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.util.Defqon;
import fr.aresrpg.sdk.util.minecraft.ServerType;

@SuppressWarnings("unchecked")
public class ServerProvider {

	private static ServerProvider instance = new ServerProvider();
	private int priority = 0;
	private Defqon defqon = Defqon.FIVE;

	/**
	 * Map des instances préenregistré dans la config (généralement les gros dédié de base pour les joueurs constants)
	 * <p>
	 * on cherchera d'abord a remplir ceux la avant de toucher au instances vultr
	 */
	private ConcurrentHashMap<String, Vps> configInstances = new ConcurrentHashMap<>();
	/**
	 * Map des instances suplémentaire loué a l'heure, mises a jour en fonction de la ram dispo sur les Vps et la ram demandée pour le type du serveur
	 */
	private ConcurrentHashMap<ServerType, Vps> ordered = new ConcurrentHashMap<>();

	private ServerProvider() {
	}

	public static void init() {
		// mange mon gros sonar
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
		PhantomTrigger.getAll().forEach(t -> t.handleDefcon(defqon));
	}

	public Defqon getDefqon() {
		return defqon;
	}

	public int getPriority() {
		return priority;
	}

	public static ServerProvider getInstance() {
		return instance;
	}

	public ConcurrentMap<ServerType, Vps> getOrdered() {
		return ordered;
	}

	public ConcurrentMap<String, Vps> getConfigInstances() {
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
	public synchronized Vps getVps(ServerType type, Optional<Vps> exclude) { // NOSONAR sorry pour la cyclomatic complexity
		boolean log = SPhantom.getInstance().isLogprovider();
		long time = System.currentTimeMillis();
		if (log) Log.out("[Provider] Asking Vps for type : " + type.name());
		SPhantomConfig sc = SPhantom.getInstance().getSphantomConfig();
		Vps vp = null;
		for (Vps vss : getConfigInstances().values())
			if (Vpss.getAvailableRam(vss, true) >= sc.getRamFor(type)) { // recherche prioritaire dans les machines configurée (vps/dédié non loué a l'heure)
				vp = vss;
				if (!vp.isUpdated() || (exclude.isPresent() && vss == exclude.get())) {
					vp = null;
					continue;
				}
				break;
			}
		Vps vf = getOrdered().safeGet(type);
		if (vp == null) vp = exclude.isPresent() ? vf == exclude.get() ? null : vf : vf;
		if (vf != null && !vf.isUpdated()) vf = null; // NOSONAR bitch
		if (log) Log.out("Found vps : " + (vp == null ? "NULL :(" : vp.getLabel()));
		if (vp == null) {
			vp = searchFirst(sc.getRamFor(type), exclude);
			if (vp != null && !vp.isUpdated()) vp = null;
			if (SPhantom.getInstance().isLogprovider()) Log.out("Force found vps : " + (vp == null ? "Not found again.. Wait for defqon to grow" : vp.getLabel()));
			if (vp == null) return null; // si on trouve vraiment pas de vps on return null et tant pis aucun serveur ne s'ouvrira il faudra attendre l'ouverture d'une instance automatiquement
		}
		int rfm = sc.getRamFor(type);
		int availableRam = Vpss.getAvailableRam(vp, true) - rfm;
		if (log) Log.out("Available ram : " + (availableRam + rfm));
		for (Entry<ServerType, Vps> e : ordered.entrySet()) {
			Vps value = e.getValue();
			ServerType key = e.getKey();
			int ramfor = sc.getRamFor(key);
			int ramavail = value == null ? -1 : value == vp ? availableRam : Vpss.getAvailableRam(value, true);
			if (ramfor > ramavail) {
				Vps neew = searchFirst(ramfor, Optional.of(vp), exclude);
				ordered.put(key, neew);
			}
		}
		if (log) Log.out("Founded in " + (System.currentTimeMillis() - time) + "ms");
		return vp;
	}

	private Vps searchFirst(int ramNeeded, Optional<Vps>... exclude) {
		Set<Vps> comp = new HashSet<>();
		Arrays.stream(exclude).filter(Optional::isPresent).forEach(o -> comp.add(o.get())); // NOSONAR closeable
		return Core.getInstance().getVps().values().stream().filter(vp -> Vpss.getAvailableRam(vp, true) >= ramNeeded && (comp.isEmpty() || !comp.contains(vp))).findFirst().orElse(null);
	}

}
