package sceat.domain.network.server;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import sceat.SPhantom;
import sceat.api.PhantomApi.ServerApi;
import sceat.api.PhantomApi.VpsApi;
import sceat.domain.icommon.IPhantom;
import sceat.domain.icommon.utils.ICrash;
import sceat.domain.icommon.utils.IRegistrable;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.ServerProvider;
import sceat.domain.network.server.Server.ServerType;

public class Vps implements Comparable<Vps>, VpsApi, ICrash, IRegistrable<Vps> {

	private String label;
	private int ram;
	private InetAddress ip;
	private VpsState state;

	/**
	 * internal use only
	 */
	private boolean updated = false;
	private long createdMilli;
	/**
	 * labels
	 */
	private Set<Server> servers;

	public static Vps fromBoot(String label, int ram, InetAddress ip) {
		return new Vps(label, ram, ip, new HashSet<Server>(), System.currentTimeMillis()).setState(VpsState.DEPLOYING);
	}

	@Override
	public String toString() {
		return "-< [Vps]: Label('" + label + "')|TotRam(" + ram + ")|RamAv(" + getAvailableRam(true) + ")|Ip('" + ip.getHostAddress() + "')|State('" + state + "')|Servers(" + servers.size()
				+ ")|Updated(" + isUpdated() + ")|Created('" + getCreatedInfos() + "') >-";
	}

	public String getCreatedInfos() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
	}

	public Vps(String label, int ram, InetAddress ip, Set<Server> srvs, long created) {
		this.label = label;
		this.ram = ram;
		this.servers = srvs;
		this.createdMilli = created;
		this.ip = ip;
	}

	public boolean isCrashed() {
		return getState() == VpsState.TIMEOUT;
	}

	@Override
	public void handleCrash() {
		if (!isCrashed()) throw new IllegalAccessError("Cannot handle crash of a vps not even crashed");
		if (!isDaemon() && !IPhantom.get().exist(getLabel())) { // si c'est une instance cloud et qu'elle n'existe plus sur le cloud (en gros supp manuellement)
			getServers().forEach(Server::unregister);
			unregister();
		} else {
			getServers().stream().filter(Server::hasTimeout).forEach(s -> s.setStatus(Statut.CRASHED)); // sinon on laisse en standby pour qu'un dev puisse aller voir ce qui ce passe
		}

	}

	public boolean isUpdated() {
		return this.updated;
	}

	public Vps setUpdated(boolean updated) {
		this.updated = updated;
		return this;
	}

	public boolean canAccept(Server srv) {
		return getAvailableRam(true) >= SPhantom.getInstance().getSphantomConfig().getRamFor(srv.getType());
	}

	public Vps register() {
		Core.getInstance().getVps().put(getLabel(), this);
		return this;
	}

	/**
	 * Unregister the vps but not his servers
	 */
	public Vps unregister() {
		if (isDaemon()) throw new IllegalAccessError("Cannot unregister a configured vps ! (" + getLabel() + ")");
		if (Core.getInstance().getVps().containsKey(getLabel())) Core.getInstance().getVps().remove(getLabel());
		for (Entry<ServerType, Vps> e : ServerProvider.getInstance().getOrdered().entrySet())
			if (e.getValue().getLabel().equals(getLabel())) ServerProvider.getInstance().getOrdered().put(e.getKey(), null);
		return null;
	}

	/**
	 * heavy
	 * 
	 * @return
	 */
	public int getAvailableRam(boolean excludeClosing) {
		return ram
				- getServers()
						.stream()
						.filter(s -> excludeClosing ? s.getStatus() != Statut.REDUCTION && s.getStatus() != Statut.CLOSING && s.getStatus() != Statut.CRASHED && s.getStatus() != Statut.OVERHEAD
								: true).mapToInt(t -> SPhantom.getInstance().getSphantomConfig().getRamFor(t.getType())).reduce((a, b) -> a + b).orElse(0);
	}

	public int getAvailableRam() {
		return getAvailableRam(true);
	}

	public VpsState getState() {
		return state;
	}

	public Vps setState(VpsState st) {
		this.state = st;
		return this;
	}

	/**
	 * 
	 * @return false if the vps is a cloud/vultr instance
	 */
	public boolean isDaemon() {
		return SPhantom.getInstance().getSphantomConfig().getServers().stream().anyMatch(p -> p.getName().equals(getLabel()));
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

	public Set<ServerApi> getAllServers() {
		return getServers().stream().map(s -> s).collect(Collectors.toSet());
	}

	public long getCreatedMilli() {
		return createdMilli;
	}

	/**
	 * Check si le vps a été créé depuis suffisament de temps pour être destroyed (50min mini) pour ne pas payer inutilement
	 * 
	 * @return true si le vps a été créé il y a plus de 50min
	 */
	public boolean canBeDestroyed() {
		return Instant.ofEpochMilli(getCreatedMilli()).plus(50, ChronoUnit.MINUTES).isBefore(Instant.now());
	}

	public static enum VpsState {
		DEPLOYING((byte) 0),
		ONLINE((byte) 1),
		TIMEOUT((byte) 2),
		DESTROYING((byte) 3);

		private byte id;

		private VpsState(byte id) {
			this.id = id;
		}

		public byte getId() {
			return id;
		}

		public static VpsState fromId(byte id) {
			return Arrays.stream(values()).filter(i -> i.id == id).findFirst().orElse(null);
		}
	}

	@Override
	public int compareTo(Vps o) {
		return getAvailableRam(true) - o.getAvailableRam(true);
	}

}
