package sceat.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import sceat.domain.minecraft.Grades;
import sceat.domain.network.Server;

public class Manager {

	private static Manager instance;

	private static ConcurrentHashMap<String, Server> serversByLabel = new ConcurrentHashMap<String, Server>();
	private static CopyOnWriteArraySet<UUID> playersOnNetwork = new CopyOnWriteArraySet<UUID>();
	private static ConcurrentHashMap<Grades, HashSet<UUID>> playersPerGrade = new ConcurrentHashMap<Grades, HashSet<UUID>>();

	public Manager() {
		instance = this;
		Arrays.stream(Grades.values()).forEach(g -> playersPerGrade.put(g, new HashSet<UUID>()));
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

	public ConcurrentHashMap<String, Server> getServersByLabel() {
		return serversByLabel;
	}

	public CopyOnWriteArraySet<UUID> getPlayersOnNetwork() {
		return playersOnNetwork;
	}

	public ConcurrentHashMap<Grades, HashSet<UUID>> getPlayersPerGrade() {
		return playersPerGrade;
	}

	public static enum Notifier {
		PacketPhantomServerInfo,
		PacketPhantomPlayer
	}

}
