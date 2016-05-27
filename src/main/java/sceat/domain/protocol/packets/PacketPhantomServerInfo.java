package sceat.domain.protocol.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.UUID;

import sceat.Main;
import sceat.domain.Manager;
import sceat.domain.common.mq.Broker;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Vps;
import sceat.domain.trigger.PhantomTrigger;
import sceat.domain.utils.New;
import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.commons.util.collection.Set;
import fr.aresrpg.commons.util.map.EnumHashMap;
import fr.aresrpg.commons.util.map.EnumMap;
import fr.aresrpg.commons.util.map.HashMap;
import fr.aresrpg.commons.util.map.Map;
import fr.aresrpg.sdk.mc.Grades;
import fr.aresrpg.sdk.mc.ServerType;
import fr.aresrpg.sdk.mc.Statut;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

public class PacketPhantomServerInfo extends PacketPhantom {

	private String label;
	private String vpsLabel;
	private ServerType type;
	private int maxp;
	private int port;
	private String ip;
	private EnumMap<Grades, Set<UUID>> players = new EnumHashMap<>(Grades.class);
	private Statut state;
	private boolean fromSymbiote = false; // if the packet came from symbiote, then we must get from the map like a closing server and not from "Server.fromPacket"

	public PacketPhantomServerInfo(Statut state, String label, String vpsLabel, InetAddress ip, ServerType type, int maxp, int port, EnumMap<Grades, Set<UUID>> pl, boolean fromSymbiote) { // NOSONAR ENUMMAP C DEJA UNE PUTIN D'IMPLEM ALORS FERME LA
		this.ip = ip.getHostAddress();
		this.vpsLabel = vpsLabel;
		this.label = label;
		this.type = type;
		this.players = pl == null ? new EnumHashMap<>(Grades.class) : pl;
		if (pl == null) Arrays.stream(Grades.values()).forEach(g -> players.put(g, new HashSet<>())); // NOSONAR CLOSE TA MERE
		this.maxp = maxp;
		this.fromSymbiote = fromSymbiote;
		this.state = state;
		this.port = port;
	}

	public PacketPhantomServerInfo() {
		// deserial
	}

	@Override
	protected void serialize_() {
		writeString(getLabel());
		writeString(this.vpsLabel);
		writeByte(getType().getId());
		writeInt(getMaxp());
		writeString(this.ip);
		writeEnumMap(this.players, d -> writeString(d.name()), d -> writeCollection(d, e -> writeString(e.toString())));
		writeString(getState().name());
		writeBoolean(isFromSymbiote());
		writeInt(getPort());
	}

	@Override
	protected void deserialize_() {
		this.label = readString();
		this.vpsLabel = readString();
		this.type = ServerType.fromByte(readByte());
		this.maxp = readInt();
		this.ip = readString();
		this.players = readEnumMap(() -> Grades.valueOf(readString()), () -> readCollection(new HashSet<UUID>(), () -> UUID.fromString(readString())), Grades.class);
		if (players.safeGet(Grades.ADMIN) == null) Arrays.stream(Grades.values()).forEach(g -> players.put(g, New.set())); // NOSONAR closeable comme ta mere qui boit de l'eau chaude a la caraffe
		this.state = Statut.valueOf(readString());
		this.fromSymbiote = readBoolean();
		this.port = readInt();
	}

	public int getPort() {
		return port;
	}

	public Map<UUID, String> getPlayersMap() {
		HashMap<UUID, String> map = new HashMap<>();
		getPlayers().forEach(p -> map.put(p, getLabel()));
		return map;
	}

	@Override
	public String toString() {
		return "PacketUpdateServer [" + getLabel() + "|" + getState().name() + "|players(" + getPlayers().size() + ")]";
	}

	@Override
	public void handleData(MessagesType tp) {
		if (cameFromLocal()) return;
		Log.packet(this, true);
		Manager m = Manager.getInstance();
		if (getState() == Statut.CLOSING) {
			Server srv = Server.fromPacket(this, true);
			Vps curr = null;
			if (srv == null) {
				Log.out("PacketPhantomServerInfo : State Closing | the server " + getLabel() + " is not registered | Ignoring (cause closing) ! break");
				return;
			} else if (getVpsLabel() == null) {
				bite: for (Vps vps : Core.getInstance().getVps().values()) {
					for (Server s : vps.getServers())
						if (s.getLabel().equalsIgnoreCase(getLabel())) {
							curr = vps;
							break bite;
						}
				}
			} else curr = srv.getVps();
			Set<Server> ss = Core.getInstance().getServersByType().safeGet(getType());
			ss.safeRemove(srv);
			m.getServersByLabel().safeRemove(getLabel());
			if (curr == null) {
				// vps not found osef car tt façon on le vire
				Log.out("PacketPhantomServerInfo : State Closing | the server " + getLabel() + " is registered but not in a Vps object | Info ! break");
				return;
			}
			curr.getServers().safeRemove(srv);
			Vps vsss = curr;
			PhantomTrigger.getAll().forEach(t -> t.handleVps(vsss)); // trigger
			PacketPhantomServerInfo.fromServer(srv).send();
			return;
		}
		Server srvf = Server.fromPacket(this, false);
		srvf.heartBeat();
		m.getServersByLabel().put(getLabel(), srvf);
		Core.getInstance().getServersByType().safeGet(getType()).add(srvf);
		Set<UUID> pll = getPlayers();
		m.getPlayersOnNetwork().putAll(getPlayersMap());
		Core.getInstance().getPlayersByType().safeGet(getType()).addAll(pll);
		PacketPhantomServerInfo.fromServer(srvf).send();
		Vps v = Core.getInstance().getVps().getOrDefault(vpsLabel, null);
		if (v != null) PhantomTrigger.getAll().forEach(t -> t.handleVps(v));
	}

	public static PacketPhantomServerInfo fromServer(Server srv) {
		return new PacketPhantomServerInfo(srv.getStatus(), srv.getLabel(), srv.getVpsLabel(), srv.getIpadress(), srv.getType(), srv.getMaxPlayers(), srv.getMaxPlayers(), srv.getPlayersMap(), false);
	}

	public boolean isFromSymbiote() {
		return this.fromSymbiote;
	}

	public Statut getState() {
		return state;
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

	public int getMaxp() {
		return maxp;
	}

	public String getLabel() {
		return label;
	}

	public ServerType getType() {
		return type;
	}

	public InetAddress getIp() {
		try {
			return InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			Main.printStackTrace(e);
			return null;
		}
	}

	public EnumMap<Grades, Set<UUID>> getPlayersPerGrade() { // NOSONAR c déja une implem putin
		return players;
	}

	public Set<UUID> getPlayers() {
		return getPlayersPerGrade().values().stream().reduce((s1, s2) -> {
			s1.addAll(s2);
			return s1;
		}).orElseGet(HashSet<UUID>::new);
	}

	@Override
	public void send() {
		Broker.get().sendServer(this);
	}

}
