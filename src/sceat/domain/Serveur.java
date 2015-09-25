package sceat.domain;

import java.util.ArrayList;
import java.util.Collection;

import sceat.domain.messaging.destinationKey;
import sceat.domain.utils.New;
import sceat.domain.utils.UtilGson;

import com.google.gson.annotations.Expose;

public class Serveur {

	private String name;
	private int max_player;
	/**
	 * Collection d'uuid
	 */
	@SuppressWarnings("unchecked")
	@Expose
	private Collection<String>[] playersPerGrade = new Collection[Grades.values().length];
	@Expose
	private Collection<String> players = New.coll();
	@Expose
	private ServeurType type;
	@Expose
	private Statut statu;
	private RessourcePack pack;
	@Expose
	private int index;
	private Long lastHandshake;
	private Collection<destinationKey> keys = new ArrayList<destinationKey>();
	@Expose
	private String ipadress;

	public Serveur() {
		this.statu = Statut.CLOSED;
		this.pack = RessourcePack.RESSOURCE_PACK_DEFAULT;
		this.lastHandshake = System.currentTimeMillis();
	}

	public String getIpadress() {
		return ipadress;
	}

	/**
	 * Return la derniere fois qu'il a indiqué être vivant
	 * 
	 * @return
	 */
	public Long getLastHandShake() {
		return this.lastHandshake;
	}

	public String toJson() {
		return UtilGson.serialize(this);
	}

	/**
	 * Sur SPhantom on à besoin que de peu d'infos donc on recup direct l'objet
	 * 
	 * @param json
	 * @return
	 */
	public static Serveur fromJson(String json) {
		return UtilGson.deserialize(json, Serveur.class);
	}

	public void synchronize() {
		Sceat.getJmsConnector().syncServer(toJson());
	}

	public boolean close() {
		if (getStatut() == Statut.CLOSED) return false;
		// TODO: run close server
		return true;
	}

	public boolean boot() {
		if (getStatut() != Statut.CLOSED) return false;
		// TODO: run start server
		return true;
	}

	/**
	 * Indique que ce serveur n'a pas crash
	 * 
	 * @return
	 */
	public Serveur handShake() {
		this.lastHandshake = System.currentTimeMillis();
		return this;
	}

	public RessourcePack getPack() {
		return this.pack;
	}

	/**
	 * 
	 * @return une collection d'uuid
	 */
	public Collection<String> getPlayers() {
		return players;
	}

	/**
	 * Return une collection d'uuid en fonction du grade
	 * 
	 * @param gr
	 * @return
	 */
	public Collection<String> getPlayers(Grades gr) {
		return this.playersPerGrade[gr.getValue()];
	}

	public int getPlayersCount() {
		return getPlayers().size();
	}

	/**
	 * Set une collection d'uuid
	 * 
	 * @param players
	 */
	public void setPlayers(Collection<String> players) {
		this.players = players;
	}

	public Collection<destinationKey> getDestinations() {
		return keys;
	}

	public Serveur setStatus(Statut s) {
		this.statu = s;
		return this;
	}

	public boolean isFull() {
		return getPlayersCount() >= getMaxPlayers();
	}

	/**
	 * Return true si le serveur est a moitié rempli ou +
	 * 
	 * @return
	 */
	public boolean isMidFilled() {
		return getPlayersCount() >= getMaxPlayers() / 2;
	}

	public boolean isLobby() {
		switch (this.type) {
			case lobbyAgares:
			case lobbyAresRpg:
			case lobbyIron:
			case lobbyMain:
				return true;
			default:
				return false;
		}
	}

	public Serveur setIndex(int index) {
		this.index = index;
		return this;
	}

	public int getIndex() {
		return this.index;
	}

	public Statut getStatut() {
		return this.statu;
	}

	public String getName() {
		return this.name;
	}

	public int getMaxPlayers() {
		return this.max_player;
	}

	public ServeurType getType() {
		return this.type;
	}

	public Serveur addDestination(destinationKey key) {
		this.keys.add(key);
		return this;
	}

	public Serveur setPack(RessourcePack pack) {
		this.pack = pack;
		return this;
	}

	public Serveur setName(String name) {
		this.name = name;
		return this;
	}

	public Serveur setMaxPlayers(int max) {
		this.max_player = max;
		return this;
	}

	public Serveur setType(ServeurType type) {
		this.type = type;
		return this;
	}

	public static enum ServeurType {
		lobbyMain("§7Lobby §8Main"),
		lobbyAresRpg("§7Lobby §6AresRpg"),
		lobbyAgares("§7Lobby §2Agar.es"),
		lobbyIron("§7Lobby §3Iron"),
		aresrpg("§aAres§a§lRpg"),
		iron("§3Iron"),
		agares("§6Agar.es"),
		build("§9Build");

		private String name;

		private ServeurType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

}
