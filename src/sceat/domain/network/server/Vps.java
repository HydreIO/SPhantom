package sceat.domain.network.server;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import sceat.SPhantom;
import sceat.domain.network.Core;

public class Vps {

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

	/**
	 * heavy
	 * 
	 * @return
	 */
	public int getAvailableRam() {
		return ram - getServers().stream().mapToInt(t -> SPhantom.getInstance().getSphantomConfig().getRamFor(t.getType())).reduce((a, b) -> a + b).getAsInt();
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

}