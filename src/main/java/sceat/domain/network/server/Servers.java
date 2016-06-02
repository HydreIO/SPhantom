package sceat.domain.network.server;

import sceat.domain.Manager;
import sceat.domain.network.Core;
import sceat.domain.utils.ServerLabel;
import fr.aresrpg.commons.concurrent.ConcurrentMap;
import fr.aresrpg.sdk.network.Server;
import fr.aresrpg.sdk.network.Vps;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomServerInfo;
import fr.aresrpg.sdk.util.minecraft.ServerType;
import fr.aresrpg.sdk.util.minecraft.Statut;

/**
 * Cette class était l'ancienne implem de server que j'ai refactor dans AresInternal
 * <p>
 * Elle ne contient que des methode Utils static, je la refferait peut etre un jour au propre (ce n'est pas non plus extremement important)
 * 
 * @author MrSceat
 *
 */
public class Servers {

	private Servers() {

	}

	/**
	 * Au moment ou un packet server arrive c'est la qu'on synchronise les joueurs
	 * <p>
	 * si le packet provient du symbiote on ne sync pas les joueurs
	 * 
	 * @param pkt
	 *            le pkt
	 * @param canBeNull
	 *            false pour creer et enregistrer le serveur si jamais il n'est pas trouv�
	 * @return
	 */
	public static Server fromPacket(PacketPhantomServerInfo pkt, boolean canBeNull) {
		Server sr = null; // NOSONAR je ne peut pas use la methode getOrDefault de la concurrentHashmap car je doit modif le serveur contenu dans la map :(
		boolean neww = false;
		ConcurrentMap<String, Server> sbl = Manager.getInstance().getServersByLabel();
		if (sbl.containsKey(pkt.getLabel())) {
			sr = sbl.safeGet(pkt.getLabel());
			if (sr.getStatus() != Statut.REDUCTION) sr.setStatus(pkt.getState()); // si on connait le serv et qu'il est en reduction alors on ne change pas le statut
			if (!pkt.isFromSymbiote()) sr.setPlayers(pkt.getPlayersPerGrade()); // sa voudra dire qu'on a reçu un packet avant d'avoir pu informer le serveur qu'il devait se reduire
		} else {
			sr = canBeNull ? null : new Server(pkt.getLabel(), pkt.getType(), pkt.getState(), pkt.getMaxp()).setPlayers(pkt.getPlayersPerGrade());
			neww = true; // si on créé on a pas besoin de verifier si le pkt vient du symbiote car de tt fa�on la liste des joueurs (seul field que le symbiote ne connait pas) devra attendre de se sync later
		}
		if (sr != null) {
			boolean hasvps = pkt.getVpsLabel() != null;
			if (hasvps) {
				Core.getInstance().checkVps(pkt.getVpsLabel()); // verification de l'existance du vps, instanciation en cas de NULL (des qu'un packet symbiote arrivera il sera update)
				sr.setVpsLabel(pkt.getVpsLabel());
			}
			if (neww) {
				register(sr);
				registerInVps(sr);
			}
		}
		return sr;
	}

	public static Server fromScratch(ServerType type, int maxPlayers) {
		return new Server(ServerLabel.newLabel(type), type, Statut.CREATING, maxPlayers);
	}

	public static Vps getVps(Server s) {
		return Core.getInstance().getVps().getOrDefault(s.getVpsLabel(), null);
	}

	public static Server register(Server s) {
		Manager.getInstance().getServersByLabel().put(s.getLabel(), s);
		Core.getInstance().getServersByType().safeGet(s.getType()).add(s);
		return s;
	}

	public static Server registerInVps(Server s) {
		Core.getInstance().getVps().get(s.getVpsLabel()).getServers().add(s);
		return s;
	}

	public static Server unregister(Server s) {
		Manager.getInstance().getServersByLabel().safeRemove(s.getLabel());
		Core.getInstance().getServersByType().safeGet(s.getType()).safeRemove(s);
		return s;
	}
}