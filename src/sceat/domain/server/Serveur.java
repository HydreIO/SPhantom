package sceat.domain.server;

import java.util.List;
import java.util.Set;

import sceat.SPhantom;
import sceat.domain.Manager.ServerInfo;
import sceat.domain.network.Grades;
import sceat.domain.network.Statut;
import sceat.domain.utils.New;
import sceat.domain.utils.UtilGson;

import com.google.gson.annotations.Expose;

public class Serveur {

	@Expose
	private int max_player;
	@SuppressWarnings("unchecked")
	@Expose
	private Set<String>[] playersPerGrade = new Set[Grades.values().length];
	@Expose
	private List<String> players = New.arrayList();
	@Expose
	private ServeurType type;
	@Expose
	private Statut statu;
	@Expose
	private int index;
	@Expose
	private String ipadress;
	public short ping;

	public String getIpadress() {
		return ipadress;
	}

	public Serveur setPlayersPerGrade(Set<String>[] playersPerGrade) {
		this.playersPerGrade = playersPerGrade;
		return this;
	}

	public static Serveur fromServerInfo(ServerInfo infos) {
		Serveur s = new Serveur();
		s.ipadress = infos.getHost() + ":" + infos.getPort();
		s.type = infos.getType();
		s.index = infos.getIndex();
		s.statu = Statut.CLOSED;
		s.max_player = 0;
		return s;
	}

	public String toJson() {
		return UtilGson.serialize(this);
	}

	/**
	 * Sur SPhantom on � besoin que de peu d'infos donc on recup direct l'objet
	 * 
	 * @param json
	 * @return
	 */
	public static Serveur fromJson(String json) {
		return UtilGson.deserialize(json, Serveur.class);
	}

	public void synchronize() {
		SPhantom.getInstance().getMessageBroker().sendServer(toJson());
	}

	public Set<String>[] getPlayersPerGrade() {
		return playersPerGrade;
	}

	/**
	 * 
	 * @return une collection d'uuid
	 */
	public List<String> getPlayers() {
		return players;
	}

	/**
	 * Return une collection d'uuid en fonction du grade
	 * 
	 * @param gr
	 * @return
	 */
	public Set<String> getPlayers(Grades gr) {
		return this.playersPerGrade[gr.getValue()];
	}

	public int getPlayersCount() {
		return getPlayers().size();
	}

	/**
	 * Set d'uuid
	 * 
	 * @param players
	 */
	public void setPlayers(List<String> players) {
		this.players = players;
	}

	public Serveur setStatus(Statut s) {
		this.statu = s;
		return this;
	}

	public boolean isFull() {
		return getPlayersCount() >= getMaxPlayers();
	}

	/**
	 * Return true si le serveur est a moiti� rempli ou +
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
		return getType().name() + getIndex();
	}

	public int getMaxPlayers() {
		return this.max_player;
	}

	public ServeurType getType() {
		return this.type;
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
		lobbyMain("�7Lobby �8Main"),
		lobbyAresRpg("�7Lobby �6AresRpg"),
		lobbyAgares("�7Lobby �2Agar.es"),
		lobbyIron("�7Lobby �3Iron"),
		aresRpg("�aAres�a�lRpg"),
		iron("�3Iron"),
		agares("�6Agar.es"),
		build("�9Build"),
		proxy("proxy");

		private String name;

		private ServeurType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

}
