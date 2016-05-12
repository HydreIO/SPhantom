package sceat.domain;

import java.util.Arrays;
import java.util.Collection;
import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.commons.util.collection.Set;
import java.util.UUID;

import sceat.domain.minecraft.Grades;
import sceat.domain.network.server.Server;
import fr.aresrpg.commons.concurrent.ConcurrentHashMap;
import fr.aresrpg.commons.concurrent.ConcurrentMap;
import fr.aresrpg.commons.concurrent.ConcurrentSet;

public class Manager {

	private static Manager instance = new Manager();

	private ConcurrentHashMap<String, Server> serversByLabel = new ConcurrentHashMap<>();
	private ConcurrentSet<UUID> playersOnNetwork = new ConcurrentSet<>();
	private ConcurrentHashMap<Grades, Set<UUID>> playersPerGrade = new ConcurrentHashMap<>();

	private Manager() {
	}

	public static void init() {
		Manager i = instance;
		Arrays.stream(Grades.values()).forEach(g -> i.playersPerGrade.put(g, new HashSet<>()));
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

	public Set<UUID> getPlayersOnNetwork() {
		return playersOnNetwork;
	}

	public ConcurrentMap<Grades, Set<UUID>> getPlayersPerGrade() {
		return playersPerGrade;
	}
}
