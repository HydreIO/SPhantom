package sceat.domain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import sceat.domain.minecraft.Grades;
import sceat.domain.network.server.Server;

public class Manager {

	private static Manager instance;

	private static ConcurrentHashMap<String, Server> serversByLabel = new ConcurrentHashMap<>();
	private static CopyOnWriteArraySet<UUID> playersOnNetwork = new CopyOnWriteArraySet<>();
	private static ConcurrentHashMap<Grades, Set<UUID>> playersPerGrade = new ConcurrentHashMap<>();

	public Manager() {
		instance = this;
		Arrays.stream(Grades.values()).forEach(g -> playersPerGrade.put(g, new HashSet<>()));
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

	public Map<String, Server> getServersByLabel() {
		return serversByLabel;
	}

	public Set<UUID> getPlayersOnNetwork() {
		return playersOnNetwork;
	}

	public Map<Grades, Set<UUID>> getPlayersPerGrade() {
		return playersPerGrade;
	}

	public enum Notifier {
		PACKET_PHANTOM_SERVER_INFO,
		PACKET_PHANTOM_PLAYER
	}

}
