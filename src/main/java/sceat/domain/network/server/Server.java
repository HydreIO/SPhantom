package sceat.domain.network.server;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import sceat.api.PhantomApi.ServerApi;
import sceat.domain.Manager;
import sceat.domain.common.utils.IRegistrable;
import sceat.domain.network.Core;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;
import sceat.domain.utils.ServerLabel;
import fr.aresrpg.commons.concurrent.ConcurrentMap;
import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.commons.util.collection.Set;
import fr.aresrpg.commons.util.map.EnumHashMap;
import fr.aresrpg.commons.util.map.EnumMap;
import fr.aresrpg.sdk.mc.Grades;
import fr.aresrpg.sdk.mc.RessourcePack;
import fr.aresrpg.sdk.mc.ServerType;
import fr.aresrpg.sdk.mc.Statut;

public class Server implements ServerApi, IRegistrable<Server> {

	private String label;
	private String vpsLabel;
	private ServerType type;
	private int maxPlayers;
	private int port;
	private Statut status;
	private EnumMap<Grades, Set<UUID>> players = new EnumHashMap<>(Grades.class);
	private InetAddress ipadress;
	private long timeout;

	public Server(String label, ServerType type, Statut state, int maxplayer, int port, InetAddress ip) {
		this.label = label;
		this.type = type;
		this.maxPlayers = maxplayer;
		this.status = state;
		this.ipadress = ip;
		this.port = port;
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
		Server sr = null; // NOSONAR je ne peut pas use la methode getOrDefault de la concurrentHashmap car je doit modif le serveur contenu dans la map :(
		boolean neww = false;
		ConcurrentMap<String, Server> sbl = Manager.getInstance().getServersByLabel();
		if (sbl.containsKey(pkt.getLabel())) {
			sr = sbl.safeGet(pkt.getLabel());
			if (sr.getStatus() != Statut.REDUCTION) sr.setStatus(pkt.getState()); // si on connait le serv et qu'il est en reduction alors on ne change pas le statut
			if (!pkt.isFromSymbiote()) sr.setPlayers(pkt.getPlayersPerGrade()); // sa voudra dire qu'on a reçu un packet avant d'avoir pu informer le serveur qu'il devait se reduire
		} else {
			sr = canBeNull ? null : new Server(pkt.getLabel(), pkt.getType(), pkt.getState(), pkt.getMaxp(), pkt.getPort(), pkt.getIp()).setPlayers(pkt.getPlayersPerGrade());
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

	public int getPort() {
		return port;
	}

	public static Server fromScratch(ServerType type, int maxPlayers, InetAddress ip, int port) {
		return new Server(ServerLabel.newLabel(type), type, Statut.CREATING, maxPlayers, port, ip);
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

	public Server setPlayers(EnumMap<Grades, Set<UUID>> players) { // NOSONAR already an impl
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

	public Set<UUID> getPlayers(Grades gr) {
		return getPlayersMap().safeGet(gr);
	}

	public EnumMap<Grades, Set<UUID>> getPlayersMap() { // NOSONAR already an impl
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
		}).orElseGet(HashSet::new);
	}

	public Vps getVps() {
		return Core.getInstance().getVps().getOrDefault(getVpsLabel(), null);
	}

	@Override
	public Server register() {
		Manager.getInstance().getServersByLabel().put(getLabel(), this);
		Core.getInstance().getServersByType().safeGet(getType()).add(this);
		return this;
	}

	public Server registerInVps() {
		Core.getInstance().getVps().get(getVpsLabel()).getServers().add(this);
		return this;
	}

	@Override
	public Server unregister() {
		Manager.getInstance().getServersByLabel().safeRemove(getLabel());
		Core.getInstance().getServersByType().safeGet(getType()).safeRemove(this);
		return this;
	}

	@Override
	public RessourcePack getPack() {
		return getType().getPack();
	}

}