package sceat.domain.network.server;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import sceat.api.PhantomApi.ServerApi;
import sceat.domain.Manager;
import sceat.domain.common.utils.IRegistrable;
import sceat.domain.minecraft.Grades;
import sceat.domain.minecraft.RessourcePack;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;
import sceat.domain.utils.ServerLabel;
import fr.aresrpg.sdk.protocol.RoutingKey;

public class Server implements ServerApi, IRegistrable<Server> {

	private String label;
	private String vpsLabel;
	private ServerType type;
	private int maxPlayers;
	private Statut status;
	private RessourcePack pack;
	private Map<Grades, Set<UUID>> players = new HashMap<Grades, Set<UUID>>();
	private InetAddress ipadress;
	private long timeout;

	public Server(String label, ServerType type, Statut state, int maxplayer, InetAddress ip, RessourcePack pack) {
		this.label = label;
		this.type = type;
		this.maxPlayers = maxplayer;
		this.status = state;
		this.pack = pack;
		this.ipadress = ip;
	}

	/**
	 * Au moment ou un packet server arrive c'est la qu'on synchronise les joueurs
	 * <p>
	 * si le packet provient du symbiote on ne sync pas les joueurs
	 * 
	 * @param pkt
	 *            le pkt
	 * @param canBeNull
	 *            false pour creer et enregistrer le serveur si jamais il n'est pas trouv�
	 * @return
	 */
	public static Server fromPacket(PacketPhantomServerInfo pkt, boolean canBeNull) {
		Server sr = null; // je ne peut pas use la methode getOrDefault de la concurrentHashmap car je doit modif le serveur contenu dans la map :(
		boolean neww = false;
		Map<String, Server> sbl = Manager.getInstance().getServersByLabel();
		if (sbl.containsKey(pkt.getLabel())) {
			sr = sbl.get(pkt.getLabel());
			if (sr.getStatus() != Statut.REDUCTION) sr.setStatus(pkt.getState()); // si on connait le serv et qu'il est en reduction alors on ne change pas le statut
			if (!pkt.isFromSymbiote()) sr.setPlayers(pkt.getPlayersPerGrade()); // sa voudra dire qu'on a reçu un packet avant d'avoir pu informer le serveur qu'il devait se reduire
		} else {
			sr = canBeNull ? null : new Server(pkt.getLabel(), pkt.getType(), pkt.getState(), pkt.getMaxp(), pkt.getIp(), RessourcePack.RESSOURCE_PACK_DEFAULT).setPlayers(pkt.getPlayersPerGrade());
			neww = true; // si on créé on a pas besoin de verifier si le pkt vient du symbiote car de tt fa�on la liste des joueurs (seul field que le symbiote ne connait pas) devra attendre de se sync later
		}
		if (sr != null) {
			boolean hasvps = pkt.getVpsLabel() != null;
			if (hasvps) {
				Core.getInstance().checkVps(pkt.getVpsLabel()); // verification de l'existance du vps, instanciation en cas de NULL (des qu'un packet symbiote arrivera il sera update)
				sr.setVpsLabel(pkt.getVpsLabel());
			}
			if (neww) sr.register().registerInVps();
		}
		return sr;
	}

	public static Server fromScratch(ServerType type, int maxPlayers, InetAddress ip, RessourcePack pack) {
		return new Server(ServerLabel.newLabel(type), type, Statut.CREATING, maxPlayers, ip, pack);
	}

	public Server setVpsLabel(String label) {
		this.vpsLabel = label;
		return this;
	}

	public long getTimeout() {
		return timeout;
	}

	@Override
	public String getLastTimeout() {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(getTimeout()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
	}

	@Override
	public String getVpsLabel() {
		return vpsLabel;
	}

	public void heartBeat() {
		this.timeout = System.currentTimeMillis();
	}

	public boolean hasTimeout() {
		return System.currentTimeMillis() > this.timeout + 11000;
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

	public Server setIpadress(InetAddress ipadress) {
		this.ipadress = ipadress;
		return this;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public InetAddress getIpadress() {
		return ipadress;
	}

	@Override
	public ServerType getType() {
		return type;
	}

	@Override
	public int getMaxPlayers() {
		return maxPlayers;
	}

	@Override
	public Statut getStatus() {
		return status;
	}

	@Override
	public RessourcePack getPack() {
		return pack;
	}

	public Set<UUID> getPlayers(Grades gr) {
		return getPlayersMap().get(gr);
	}

	public Map<Grades, Set<UUID>> getPlayersMap() {
		return players;
	}

	@Override
	public int countPlayers() {
		return getPlayersMap().entrySet().stream().mapToInt(e -> e.getValue().size()).reduce((a, b) -> a + b).getAsInt();
	}

	@Override
	public Set<UUID> getPlayers() {
		return getPlayersMap().values().stream().reduce((t, u) -> {
			t.addAll(u);
			return t;
		}).orElseGet(Collections::emptySet);
	}

	public Vps getVps() {
		return Core.getInstance().getVps().getOrDefault(getVpsLabel(), null);
	}

	public enum ServerType {
		PROXY((byte) 0, RessourcePack.RESSOURCE_PACK_DEFAULT, RoutingKey.PROXY),
		LOBBY((byte) 1, RessourcePack.RESSOURCE_PACK_DEFAULT, RoutingKey.HUBS),
		AGARES((byte) 2, RessourcePack.AGARES, RoutingKey.SERVERS),
		ARES_RPG((byte) 3, RessourcePack.ARESRPG, RoutingKey.SERVERS),
		IRON((byte) 4, RessourcePack.IRON, RoutingKey.SERVERS);

		private byte id;
		private RoutingKey key;
		private RessourcePack pack;

		private ServerType(byte id, RessourcePack pack, RoutingKey key) {
			this.key = key;
			this.id = id;
			this.pack = pack;
		}

		public byte getId() {
			return id;
		}

		public RessourcePack getPack() {
			return pack;
		}

		public static ServerType fromByte(byte id) {
			return Arrays.stream(values()).filter(i -> i.id == id).findFirst().orElse(null);
		}

		public RoutingKey getKey() {
			return key;
		}

	}

	@Override
	public Server register() {
		Manager.getInstance().getServersByLabel().put(getLabel(), this);
		Core.getInstance().getServersByType().get(getType()).add(this);
		return this;
	}

	public Server registerInVps() {
		Core.getInstance().getVps().get(getVpsLabel()).getServers().add(this);
		return this;
	}

	@Override
	public Server unregister() {
		Manager.getInstance().getServersByLabel().remove(getLabel());
		Core.getInstance().getServersByType().get(getType()).remove(this);
		return this;
	}

}