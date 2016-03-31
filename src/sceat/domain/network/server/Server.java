package sceat.domain.network.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import sceat.domain.minecraft.Grades;
import sceat.domain.minecraft.RessourcePack;
import sceat.domain.minecraft.Statut;
import sceat.domain.protocol.destinationKey;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;
import sceat.domain.utils.ServerLabel;

public class Server {

	/**
	 * Au moment ou un packet server arrive c'est la qu'on synchronise les joueurs
	 * 
	 * @param pkt
	 * @return
	 */
	public static Server fromPacket(PacketPhantomServerInfo pkt) {
		return new Server(pkt.getLabel(), pkt.getType(), pkt.getState(), pkt.getMaxp(), pkt.getIp(), RessourcePack.RESSOURCE_PACK_DEFAULT, pkt.getKeys().stream().toArray(String[]::new))
				.setPlayers(pkt.getPlayersPerGrade());
	}

	public static Server fromScratch(ServerType type, int maxPlayers, InetAddress ip, RessourcePack pack, String... destinationKeys) {
		return new Server(ServerLabel.newLabel(type), type, Statut.CREATING, maxPlayers, ip, pack, destinationKeys);
	}

	private String label;
	private ServerType type;
	private int maxPlayers;
	private Statut status;
	private RessourcePack pack;
	private Map<Grades, Set<UUID>> players = new HashMap<Grades, Set<UUID>>();
	private Collection<String> keys = new ArrayList<String>();
	private InetAddress ipadress;
	/**
	 * Lors de la gestion, sphantom decide en fonction du nombre de joueurs combien d'instance de ce type de serveur sont requise
	 * <p>
	 * si il y a déja suffisament d'instance, "needed" passe sur false et permet ainsi la destruction du serveur si le dernier joueur se déconnecte
	 */
	private boolean needed = true;

	public Server(String label, ServerType type, Statut state, int maxplayer, InetAddress ip, RessourcePack pack, String... destinationKeys) {
		this.label = label;
		this.type = type;
		this.maxPlayers = maxplayer;
		this.status = state;
		this.pack = pack;
		this.ipadress = ip;
		Arrays.stream(destinationKeys).forEach(keys::add);
	}

	public boolean isNeeded() {
		return this.needed;
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
		Proxy(RessourcePack.RESSOURCE_PACK_DEFAULT, destinationKey.PROXY, destinationKey.HUBS_AND_PROXY, destinationKey.ALL),
		Lobby(RessourcePack.RESSOURCE_PACK_DEFAULT, destinationKey.ALL, destinationKey.HUBS, destinationKey.HUBS_AND_PROXY),
		Agares(RessourcePack.AGARES, destinationKey.ALL, destinationKey.SERVEURS, destinationKey.SRV_AGARES),
		AresRpg(RessourcePack.ARESRPG, destinationKey.ALL, destinationKey.SERVEURS, destinationKey.SRV_ARES),
		Iron(RessourcePack.IRON, destinationKey.ALL, destinationKey.SERVEURS, destinationKey.SRV_IRON);

		private String[] keys;
		private RessourcePack pack;

		private ServerType(RessourcePack pack, String... keys) {
			this.keys = keys;
			this.pack = pack;
		}

		public RessourcePack getPack() {
			return pack;
		}

		public String[] getKeys() {
			return keys;
		}

		public List<String> getKeysAslist() {
			return Arrays.asList(getKeys());
		}
	}

}