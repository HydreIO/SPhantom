package sceat.domain.network.server;

import java.net.InetAddress;
import java.util.Map.Entry;

import sceat.SPhantom;
import sceat.domain.common.IPhantom;
import sceat.domain.network.Core;
import sceat.domain.network.ServerProvider;
import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.sdk.mc.ServerType;
import fr.aresrpg.sdk.mc.Statut;
import fr.aresrpg.sdk.network.Server;
import fr.aresrpg.sdk.network.Vps;
import fr.aresrpg.sdk.util.VpsState;

public class Vpss {

	public static Vps fromBoot(String label, int ram, InetAddress ip) {
		return new Vps(label, ram, ip, new HashSet<Server>(), System.currentTimeMillis()).setState(VpsState.DEPLOYING);
	}

	public static void handleCrash(Vps v) {
		if (!v.isCrashed()) throw new IllegalAccessError("Cannot handle crash of a vps not even crashed");
		if (!isDaemon(v) && !IPhantom.get().exist(v.getLabel())) { // si c'est une instance cloud et qu'elle n'existe plus sur le cloud (en gros supp manuellement)
			v.getServers().forEach(Servers::unregister);
			unregister(v);
		} else {
			v.getServers().stream().filter(Server::hasTimeout).forEach(s -> s.setStatus(Statut.CRASHED)); // sinon on laisse en standby pour qu'un dev puisse aller voir ce qui ce passe
		}

	}

	public static boolean canAccept(Vps v, Server srv) {
		return getAvailableRam(v, true) >= SPhantom.getInstance().getSphantomConfig().getRamFor(srv.getType());
	}

	public static Vps register(Vps v) {
		Core.getInstance().getVps().put(v.getLabel(), v);
		return v;
	}

	/**
	 * Unregister the vps but not his servers
	 */
	public static Vps unregister(Vps v) {
		if (isDaemon(v)) throw new IllegalAccessError("Cannot unregister a configured vps ! (" + v.getLabel() + ")");
		Core.getInstance().getVps().remove(v.getLabel());
		for (Entry<ServerType, Vps> e : ServerProvider.getInstance().getOrdered().entrySet())
			if (e.getValue().getLabel().equals(v.getLabel())) ServerProvider.getInstance().getOrdered().put(e.getKey(), null);
		return null;
	}

	/**
	 * heavy
	 * 
	 * @return
	 */
	public static int getAvailableRam(Vps v, boolean excludeClosing) {
		return v.getRam()
				- v.getServers()
						.stream()
						.filter(s -> excludeClosing ? s.getStatus() != Statut.REDUCTION && s.getStatus() != Statut.CLOSING && s.getStatus() != Statut.CRASHED && s.getStatus() != Statut.OVERHEAD
								: true).mapToInt(t -> SPhantom.getInstance().getSphantomConfig().getRamFor(t.getType())).reduce((a, b) -> a + b).orElse(0);
	}

	/**
	 * 
	 * @return false if the vps is a cloud/vultr instance
	 */
	public static boolean isDaemon(Vps v) {
		return SPhantom.getInstance().getSphantomConfig().getServers().stream().anyMatch(p -> p.getName().equals(v.getLabel()));
	}

	public static int compareTo(Vps a, Vps o) { // NOSONAR non j'ai pas envie de override equals alors tu t'humidifie et te care toi meme dans ton cul
		return getAvailableRam(a, true) - getAvailableRam(o, true);
	}

}
