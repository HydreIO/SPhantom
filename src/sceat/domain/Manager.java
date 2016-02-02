package sceat.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import sceat.SPhantom;
import sceat.domain.network.Grades;
import sceat.domain.server.Server;
import sceat.domain.server.Server.ServerType;
import sceat.domain.utils.ServerLabel;

public class Manager {

	private static Manager instance;
	private static ConcurrentHashMap<String, Server> serversByLabel = new ConcurrentHashMap<String, Server>();
	private static CopyOnWriteArraySet<UUID> playersOnNetwork = new CopyOnWriteArraySet<UUID>();
	private static ConcurrentHashMap<Grades, List<UUID>> playersPerGrade = new ConcurrentHashMap<Grades, List<UUID>>();

	public Manager() {
		instance = this;
		Arrays.stream(Grades.values()).forEach(g -> playersPerGrade.put(g, new ArrayList<UUID>()));
	}

	public void initSynchronisation() {
		SPhantom.getInstance().getExecutor().execute(() -> {
			SPhantom.print("Synchronising... please wait !");
			try {
				Thread.sleep(7000);
			} catch (Exception e) {
				e.printStackTrace();
			}

		});
	}

	public void createServer(ServerType type) {
		String label = ServerLabel.newLabel(type);
	}

	public static Manager getInstance() {
		return instance;
	}

	public Collection<Server> getServers() {
		return getServersByLabel().values();
	}

	public ConcurrentHashMap<String, Server> getServersByLabel() {
		return serversByLabel;
	}

	public CopyOnWriteArraySet<UUID> getPlayersOnNetwork() {
		return playersOnNetwork;
	}

	public ConcurrentHashMap<Grades, List<UUID>> getPlayersPerGrade() {
		return playersPerGrade;
	}

	public static enum Notifier {
		PacketPhantomServerInfo,
		PacketPhantomPlayer
	}

}
