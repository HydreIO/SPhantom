package sceat.domain;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import sceat.domain.config.SPhantomConfig;
import sceat.domain.config.SPhantomConfig.PortRange;
import sceat.domain.network.server.Server;
import fr.aresrpg.commons.concurrent.ConcurrentHashMap;
import fr.aresrpg.commons.concurrent.ConcurrentMap;
import fr.aresrpg.sdk.mc.ServerType;

public class Manager {

	private static Manager instance = new Manager();
	private ConcurrentHashMap<String, Server> serversByLabel = new ConcurrentHashMap<>();
	private ConcurrentHashMap<UUID, String> playersOnNetwork = new ConcurrentHashMap<>();
	private CopyOnWriteArrayList<Integer> usedPorts = new CopyOnWriteArrayList<>();

	private Manager() {
	}

	public static void init() {
		// nobitchies
	}

	public CopyOnWriteArrayList<Integer> getUsedPorts() { // NOSONAR non je peut pas mettre list
		return usedPorts;
	}

	/**
	 * Gen a port who isnt already used
	 * 
	 * @param type
	 * @return the port or -1 if all port allowed are used
	 */
	public synchronized int genPort(ServerType type) {
		PortRange range = SPhantomConfig.get().getInstances().get(type).getPortRange();
		int port = range.getMinPort();
		while (getUsedPorts().contains(port++))
			if (port > range.getMaxPort()) return -1;
		return port;
	}

	public static boolean isPortUsed(int port) {
		return instance.getUsedPorts().contains(port);
	}

	public static void usePort(int port) {
		instance.usedPorts.add(port);
	}

	public static void unusePort(int port) {
		instance.usedPorts.remove(Integer.valueOf(port));
	}

	public static Manager getInstance() {
		return instance;
	}

	public Collection<Server> getServers() {
		return getServersByLabel().values();
	}

	public int countPlayersOnNetwork() {
		return getPlayersOnNetwork().size();
	}

	public ConcurrentMap<String, Server> getServersByLabel() {
		return serversByLabel;
	}

	public ConcurrentMap<UUID, String> getPlayersOnNetwork() {
		return playersOnNetwork;
	}

}
