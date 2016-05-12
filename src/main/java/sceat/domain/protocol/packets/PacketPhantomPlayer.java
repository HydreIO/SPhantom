package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.common.mq.Broker;
import sceat.domain.minecraft.Grades;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.protocol.PacketSender;
import sceat.domain.utils.ServerLabel;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

public class PacketPhantomPlayer extends PacketPhantom {

	private UUID player;
	private PlayerAction action;
	private Grades grade;
	private String serverLabelLast;
	private String serverLabelNew;

	public PacketPhantomPlayer(UUID uid, PlayerAction action, Grades gr, String serverlabelLast, String serverLabelNew) {
		this.player = uid;
		this.action = action;
		this.grade = gr;
		this.serverLabelLast = serverlabelLast;
		this.serverLabelNew = serverLabelNew;
	}

	public PacketPhantomPlayer() {
		// deserialization
	}

	@Override
	protected void serialize_() {
		writeString(this.player.toString());
		writeByte(this.action.getId());
		writeString(this.serverLabelLast);
		writeString(this.serverLabelNew);
		writeByte(grade.getValue());
	}

	@Override
	protected void deserialize_() {
		this.player = UUID.fromString(readString());
		this.action = PlayerAction.valueOf(readByte());
		this.serverLabelLast = readString();
		this.serverLabelNew = readString();
		this.grade = Grades.fromValue(readByte(), true);
	}

	@Override
	public String toString() {
		return "PacketPlayer [" + getPlayer() + "|" + getAction().name() + "|Last(" + getServerLast() + ")|New(" + getServerNew() + ")]";
	}

	@Override
	public void handleData(MessagesType type) {
		if (cameFromLocal()) return;
		Log.packet(this, true);
		Manager m = Manager.getInstance();
		switch (action) {
			case CONNECT:
				m.getPlayersOnNetwork().put(getPlayer(), serverLabelNew);
				m.getPlayersPerGrade().get(getGrade()).add(getPlayer());
				getServerNew().getPlayersMap().get(getGrade()).add(getPlayer());
				break;
			case DISCONNECT:
				m.getPlayersOnNetwork().safeRemove(getPlayer());
				m.getPlayersPerGrade().get(getGrade()).safeRemove(getPlayer());
				getServerLast().getPlayers().removeIf(e -> e == getPlayer());
				break;
			case SERVER_SWITCH:
				m.getPlayersOnNetwork().put(getPlayer(), serverLabelNew);
				getServerLast().getPlayersMap().get(getGrade()).safeRemove(getPlayer());
				getServerNew().getPlayersMap().get(getGrade()).add(getPlayer());
				break;
			default:
				throw new IllegalStateException();
		}
		PacketSender.getInstance().sendPlayer(this);
	}

	public Grades getGrade() {
		return grade;
	}

	public ServerType getServerTypeLast() {
		if (getAction() == PlayerAction.CONNECT) throw new NullPointerException("Impossible de récuperer le type de serveurLast car le joueur vient de se connecter sur le network !");
		return ServerLabel.getTypeWithLabel(this.serverLabelLast);
	}

	public ServerType getServerTypeNew() {
		if (getAction() == PlayerAction.DISCONNECT) throw new NullPointerException("Impossible de récuperer le type de serveurNew car le joueur vient de se d�connecter du network !");
		return ServerLabel.getTypeWithLabel(this.serverLabelNew);
	}

	/**
	 * Attention cette methode peut creer du sale NPE
	 * 
	 * @return le serveur via le manager grace au label
	 */
	public Server getServerLast() {
		if (getAction() == PlayerAction.CONNECT) throw new NullPointerException("Impossible de récuperer le serveurLast car le joueur vient de se connecter sur le network !");
		return Manager.getInstance().getServersByLabel().get(this.serverLabelLast);
	}

	public Server getServerNew() {
		if (getAction() == PlayerAction.DISCONNECT) throw new NullPointerException("Impossible de récuperer le serveurNew car le joueur vient de se déconnecter du network !");
		return Manager.getInstance().getServersByLabel().get(this.serverLabelNew);
	}

	public UUID getPlayer() {
		return player;
	}

	public PlayerAction getAction() {
		return action;
	}

	public enum PlayerAction {
		CONNECT((byte) 0),
		DISCONNECT((byte) 1),
		SERVER_SWITCH((byte) 2);
		private final byte id;

		PlayerAction(byte id) {
			this.id = id;
		}

		public byte getId() {
			return id;
		}

		public static PlayerAction valueOf(byte id) {
			for (PlayerAction action : values())
				if (action.id == id) return action;
			return null;
		}
	}

	@Override
	public void send() {
		Broker.get().sendPlayer(this);
	}

}
