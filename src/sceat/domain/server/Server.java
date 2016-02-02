package sceat.domain.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import sceat.domain.messaging.protocol.PacketPhantomServerInfo;
import sceat.domain.network.Grades;
import sceat.domain.network.RessourcePack;
import sceat.domain.network.Statut;

public class Server {

	public static Server fromPacket(PacketPhantomServerInfo pkt) {
		return new Server(pkt.getLabel(), pkt.getType(), pkt.getMaxp(), pkt.getIp(), RessourcePack.RESSOURCE_PACK_DEFAULT, pkt.getKeys().stream().toArray(String[]::new));
	}

	private String label;
	private ServerType type;
	private int maxPlayers;
	private Statut status;
	private RessourcePack pack;
	private Map<Grades, Set<UUID>> players = new HashMap<Grades, Set<UUID>>();
	private Collection<String> keys = new ArrayList<String>();
	private InetAddress ipadress;

	public Server(String label, ServerType type, int maxplayer, InetAddress ip, RessourcePack pack, String... destinationKeys) {
		this.label = label;
		this.type = type;
		this.maxPlayers = maxplayer;
		this.status = Statut.CLOSED;
		this.pack = pack;
		this.ipadress = ip;
		Arrays.stream(destinationKeys).forEach(keys::add);
	}

	public Server setStatus(Statut st) {
		this.status = st;
		return this;
	}

	public Server setType(ServerType type) {
		this.type = type;
		return this;
	}

	public Server setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
		return this;
	}

	public Server setPack(RessourcePack pack) {
		this.pack = pack;
		return this;
	}

	public Server setPlayers(Map<Grades, Set<UUID>> players) {
		this.players = players;
		return this;
	}

	public Server setKeys(Collection<String> keys) {
		this.keys = keys;
		return this;
	}

	public Server setIpadress(InetAddress ipadress) {
		this.ipadress = ipadress;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public InetAddress getIpadress() {
		return ipadress;
	}

	public Collection<String> getKeys() {
		return keys;
	}

	public ServerType getType() {
		return type;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public Statut getStatus() {
		return status;
	}

	public RessourcePack getPack() {
		return pack;
	}

	public Map<Grades, Set<UUID>> getPlayersMap() {
		return players;
	}

	public Set<UUID> getPlayers(Grades gr) {
		return getPlayersMap().get(gr);
	}

	public Set<UUID> getPlayers() {
		return getPlayersMap().values().stream().reduce((t, u) -> {
			t.addAll(u);
			return t;
		}).get();
	}

	public static enum ServerType {
		Proxy,
		Lobby,
		Agares,
		AresRpg,
		Iron,
	}

}