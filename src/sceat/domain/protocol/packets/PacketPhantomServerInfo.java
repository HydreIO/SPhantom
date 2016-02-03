package sceat.domain.protocol.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import sceat.domain.minecraft.Grades;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Server.ServerType;
import sceat.domain.utils.UtilGson;

public class PacketPhantomServerInfo extends PacketPhantom {

	private String label;
	private ServerType type;
	private int maxp;
	private String ip;
	private Map<Grades, Set<UUID>> players = new HashMap<Grades, Set<UUID>>();
	private Collection<String> keys = new ArrayList<String>();
	private Statut state;

	public PacketPhantomServerInfo(Statut state, InetAddress ip, ServerType type, int maxp, Map<Grades, Set<UUID>> pl, Collection<String> keys) {
		this.ip = ip.getHostAddress();
		this.keys = keys;
		this.type = type;
		this.players = pl;
		this.maxp = maxp;
		this.state = state;
	}

	public Statut getState() {
		return state;
	}

	public Collection<String> getKeys() {
		return keys;
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

	@Override
	public String toJson() {
		return UtilGson.serialize(this);
	}

	public static PacketPhantomServerInfo fromJson(String json) {
		return UtilGson.deserialize(json, PacketPhantomServerInfo.class);
	}

}
