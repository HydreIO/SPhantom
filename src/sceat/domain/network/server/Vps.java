package sceat.domain.network.server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import sceat.domain.network.server.Server.ServerType;

public class Vps {

	private String label;
	private int ram;
	private InetAddress ip;
	private Map<String, ServerType> servers;

	public static Vps fromBoot(String label, int ram, InetAddress ip) {
		return new Vps(label, ram, ip, new HashMap<String, Server.ServerType>());
	}

	public Vps(String label, int ram, InetAddress ip, Map<String, ServerType> srvs) {
		this.label = label;
		this.ram = ram;
		this.servers = srvs;
		this.ip = ip;
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

	public Map<String, ServerType> getServers() {
		return servers;
	}

}
