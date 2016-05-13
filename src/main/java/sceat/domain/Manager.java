package sceat.domain;

import java.util.Collection;
import java.util.UUID;

import sceat.domain.network.server.Server;
import fr.aresrpg.commons.concurrent.ConcurrentHashMap;
import fr.aresrpg.commons.concurrent.ConcurrentMap;

public class Manager {

	private static Manager instance = new Manager();

	private ConcurrentHashMap<String, Server> serversByLabel = new ConcurrentHashMap<>();
	private ConcurrentHashMap<UUID, String> playersOnNetwork = new ConcurrentHashMap<>();

	private Manager() {
	}

	public static void init() {
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
