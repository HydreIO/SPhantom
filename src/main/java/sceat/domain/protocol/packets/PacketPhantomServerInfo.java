package sceat.domain.protocol.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.minecraft.Grades;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.protocol.PacketSender;

public class PacketPhantomServerInfo extends PacketPhantom {

	private String label;
	private String vpsLabel;
	private ServerType type;
	private int maxp;
	private String ip;
	private Map<Grades, Set<UUID>> players = new HashMap<Grades, Set<UUID>>();
	private Set<String> keys = new HashSet<String>();
	private Statut state;
	private boolean fromSymbiote = false; // if the packet came from symbiote, then we must get from the map like a closing server and not from "Server.fromPacket"

	public PacketPhantomServerInfo(Statut state, String label, String vpsLabel, InetAddress ip, ServerType type, int maxp, Map<Grades, Set<UUID>> pl, Set<String> keys, boolean fromSymbiote) {
		this.ip = ip.getHostAddress();
		this.vpsLabel = vpsLabel;
		this.label = label;
		this.keys = keys == null ? new HashSet<String>() : keys;
		this.type = type;
		this.players = pl == null ? new HashMap<Grades, Set<UUID>>() : pl;
		this.maxp = maxp;
		this.state = state;
	}

	public PacketPhantomServerInfo() {
	}

	@Override
	protected void serialize_() {
		writeString(getLabel());
		writeString(this.vpsLabel);
		writeString(getType().name());
		writeInt(getMaxp());
		writeString(this.ip);
		writeMap(this.players, d -> writeString(d.name()), d -> writeCollection(d, e -> writeString(e.toString())));
		writeCollection(this.keys, this::writeString);
		writeString(getState().name());
		writeBoolean(isFromSymbiote());
	}

	@Override
	protected void deserialize_() {
		this.label = readString();
		this.vpsLabel = readString();
		this.type = ServerType.valueOf(readString());
		this.maxp = readInt();
		this.ip = readString();
		this.players = readMap(() -> Grades.valueOf(readString()), () -> readCollection(new HashSet<UUID>(), () -> UUID.fromString(readString())));
		this.keys = readCollection(new HashSet<String>(), this::readString);
		this.state = Statut.valueOf(readString());
		this.fromSymbiote = readBoolean();
	}

	@Override
	public void handleData() {
		if (cameFromLocal())
			return;
		if (state == Statut.CLOSING) {
			Server srv = Server.fromPacket(this, true);
			Vps curr = null;
			if (srv == null) {
				SPhantom.print("PacketPhantomServerInfo : State Closing | the server " + label + " is not registered | Ignoring ! break");
				return;
			} else if (vpsLabel== null) {
				bite:
				for (Vps vps : Core.getInstance().getVps().values()) {
					for (Server s : vps.getServers())
						if (s.getLabel().equalsIgnoreCase(getLabel())) {
							curr = vps;
							break bite;
						}
				}
			} else curr = srv.getVps();
			Set<Server> ss = Core.getInstance().getServersByType().get(type);
			if (ss.contains(srv)) ss.remove(srv);
			Manager.getInstance().getServersByLabel().remove(label);
			if (curr == null) {
				// vps not found osef car tt fa?on on le vire
				SPhantom.print("PacketPhantomServerInfo : State Closing | the server " + label + " is registered but not in a Vps object | Info ! break");
				return;
			}
			if (curr.getServers().contains(srv)) curr.getServers().remove(srv);
			PacketSender.getInstance().sendServer(PacketPhantomServerInfo.fromServer(srv));
			return;
		}
		Server srvf = Server.fromPacket(this, false);
		srvf.heartBeat();
		Manager.getInstance().getServersByLabel().put(label, srvf);
		Core.getInstance().getServersByType().get(type).add(srvf);
		Set<UUID> ps = getPlayers();
		Manager.getInstance().getPlayersOnNetwork().addAll(ps);
		Manager.getInstance().getPlayersPerGrade().entrySet().forEach(e -> e.getValue().addAll(getPlayersPerGrade().get(e.getKey())));
		Core.getInstance().getPlayersByType().get(type).addAll(ps);
		PacketSender.getInstance().sendServer(PacketPhantomServerInfo.fromServer(srvf));
	}

	public static PacketPhantomServerInfo fromServer(Server srv) {
		return new PacketPhantomServerInfo(srv.getStatus(), srv.getLabel(), srv.getVps().getLabel(), srv.getIpadress(), srv.getType(), srv.getMaxPlayers(), srv.getPlayersMap(), srv.getKeys(), false);
	}

	public boolean isFromSymbiote() {
		return this.fromSymbiote;
	}

	public Statut getState() {
		return state;
	}

	public Collection<String> getKeys() {
		return keys;
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
			e.printStackTrace();
			return null;
		}
	}

	public Map<Grades, Set<UUID>> getPlayersPerGrade() {
		return players;
	}

	public Set<UUID> getPlayers() {
		return getPlayersPerGrade().values().stream().reduce((s1, s2) -> {
			s1.addAll(s2);
			return s1;
		}).get();
	}

}