package sceat.domain.network.server;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import sceat.SPhantom;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.ServerProvider;
import sceat.domain.network.server.Server.ServerType;

public class Vps implements Comparable<Vps> {

	private String label;
	private int ram;
	private InetAddress ip;
	private VpsState state;

	/**
	 * labels
	 */
	private Set<Server> servers;

	public static Vps fromBoot(String label, int ram, InetAddress ip) {
		return new Vps(label, ram, ip, new HashSet<Server>()).setState(VpsState.Deploying);
	}

	public Vps(String label, int ram, InetAddress ip, Set<Server> srvs) {
		this.label = label;
		this.ram = ram;
		this.servers = srvs;
		this.ip = ip;
	}

	public Vps register() {
		Core.getInstance().getVps().put(getLabel(), this);
		return this;
	}

	public boolean canAccept(Server srv) {
		return getAvailableRam(true) >= SPhantom.getInstance().getSphantomConfig().getRamFor(srv.getType());
	}

	/**
	 * called in VultrConnector
	 */
	public void unregister() {
		if (Core.getInstance().getVps().containsKey(getLabel())) Core.getInstance().getVps().remove(getLabel());
		for (Entry<ServerType, Vps> e : ServerProvider.getInstance().getOrdered().entrySet())
			if (e.getValue().getLabel().equals(getLabel())) ServerProvider.getInstance().getOrdered().put(e.getKey(), null);
	}

	/**
	 * heavy
	 * 
	 * @return
	 */
	public int getAvailableRam(boolean excludeClosing) {
		return ram
				- getServers().stream().filter(s -> excludeClosing ? s.getStatus() != Statut.REDUCTION && s.getStatus() != Statut.CLOSING : true)
						.mapToInt(t -> SPhantom.getInstance().getSphantomConfig().getRamFor(t.getType())).reduce((a, b) -> a + b).getAsInt();
	}

	public VpsState getState() {
		return state;
	}

	public Vps setState(VpsState st) {
		this.state = st;
		return this;
	}

	public InetAddress getIp() {
		return ip;
	}

	public String getLabel() {
		return label;
	}

	public int getRam() {
		return ram;
	}

	public Set<Server> getServers() {
		return servers;
	}

	public static enum VpsState {
		Deploying,
		Online,
		Destroying
	}

	@Override
	public int compareTo(Vps o) {
		return getAvailableRam(true) - o.getAvailableRam(true);
	}

}
