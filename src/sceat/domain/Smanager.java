package sceat.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import sceat.SPhantom;
import sceat.domain.messaging.protocol.PacketPhantom;
import sceat.domain.messaging.protocol.PacketPhantomServerInfo;
import sceat.domain.network.Grades;
import sceat.domain.server.Server;
import sceat.domain.server.Server.ServerType;

public class Smanager {

	private static Smanager instance;
	private Random r = new Random();

	private static Map<String, Server> serversByLabel = new HashMap<String, Server>();
	private static Set<UUID> playersOnNetwork = new HashSet<UUID>();
	private static Map<Grades, List<UUID>> playersPerGrade = new HashMap<Grades, List<UUID>>();
	private static Map<ServerType, List<Server>> allServers = new HashMap<Server.ServerType, List<Server>>();

	public Smanager() {
		instance = this;
		Arrays.stream(ServerType.values()).forEach(v -> allServers.put(v, new ArrayList<Server>()));
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

	public void notifyPacket(Notifier n, PacketPhantom pkt) {
		switch (n) {
			case PacketPhantomServerInfo:
				assert pkt instanceof PacketPhantomServerInfo : "This packet is not an instance of PacketPhantomServerInfo !";
				PacketPhantomServerInfo pk = (PacketPhantomServerInfo) pkt;
				break;
			default:
				break;
		}
	}

	public static Smanager getInstance() {
		return instance;
	}

	public static Collection<List<Server>> getServers() {
		return getAllServers().values();
	}

	public static List<Server> getServers(ServerType type) {
		return getAllServers().get(type);
	}

	public static Map<ServerType, List<Server>> getAllServers() {
		return allServers;
	}

	public static Map<String, Server> getServersByLabel() {
		return serversByLabel;
	}

	public static Set<UUID> getPlayersOnNetwork() {
		return playersOnNetwork;
	}

	public static Map<Grades, List<UUID>> getPlayersPerGrade() {
		return playersPerGrade;
	}

	private String getNewLabel(ServerType type) {
		String label;
		while (getServersByLabel().containsKey(label = type.name() + "-" + r.nextInt(5000)))
			;
		return label;
	}

	public static enum Notifier {
		PacketPhantomServerInfo,
	}

}
