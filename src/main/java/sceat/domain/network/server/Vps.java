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
import sceat.domain.adapter.api.PhantomApi.ServerApi;
import sceat.domain.adapter.api.PhantomApi.VpsApi;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.ServerProvider;
import sceat.domain.network.server.Server.ServerType;

public class Vps implements Comparable<Vps>, VpsApi {

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
		return new Vps(label, ram, ip, new HashSet<Server>(), System.currentTimeMillis()).setState(VpsState.Deploying);
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

	public Vps register() {
		Core.getInstance().getVps().put(getLabel(), this);
		return this;
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
						.mapToInt(t -> SPhantom.getInstance().getSphantomConfig().getRamFor(t.getType())).reduce((a, b) -> a + b).orElse(0);
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
		Deploying((byte) 0),
		Online((byte) 1),
		Destroying((byte) 2);

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
