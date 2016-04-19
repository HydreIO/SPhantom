package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.minecraft.Grades;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.utils.ServerLabel;

public class PacketPhantomPlayer extends PacketPhantom {

	private UUID player;
	private Grades grade;
	private Grades newGrade;
	private PlayerAction action;
	private String serverLabel;

	@Override
	protected void serialize_() {
		writeString(this.player.toString());
		writeInt(this.grade.getValue());
		writeInt(this.newGrade.getValue());
		writeString(this.action.name());
		writeString(this.serverLabel);
	}

	@Override
	protected void deserialize_() {
		this.player = UUID.fromString(readString());
		this.grade = Grades.fromValue(readInt(), true);
		this.newGrade = Grades.fromValue(readInt(), true);
		this.action = PlayerAction.valueOf(readString());
		this.serverLabel = readString();
	}

	public PacketPhantomPlayer(UUID uid, Grades grade, PlayerAction action, String serverLabel) {
		this.player = uid;
		this.grade = grade;
		this.action = action;
		this.newGrade = null;
		this.serverLabel = serverLabel;
	}

	public PacketPhantomPlayer(UUID uid, Grades lastGrade, Grades newGrade, String serverlabel) {
		this.player = uid;
		this.grade = lastGrade;
		this.action = PlayerAction.Grade_Update;
		this.newGrade = newGrade;
		this.serverLabel = serverlabel;
	}

	public Grades getNewGrade() {
		return newGrade;
	}

	public ServerType getServerType() {
		if (getAction() == PlayerAction.Disconnect) throw new NullPointerException("Impossible de récuperer le type de serveur car le joueur vient de se déconnecter !");
		return ServerLabel.getTypeWithLabel(this.serverLabel);
	}

	/**
	 * Attention cette methode peut creer du sale NPE
	 * 
	 * @return le serveur via le manager grace au label
	 */
	public Server getServer() {
		if (getAction() == PlayerAction.Disconnect) throw new NullPointerException("Impossible de récuperer le serveur car le joueur vient de se déconnecter !");
		return Manager.getInstance().getServersByLabel().get(this.serverLabel);
	}

	public UUID getPlayer() {
		return player;
	}

	public Grades getGrade() {
		return grade;
	}

	public PlayerAction getAction() {
		return action;
	}

	public static enum PlayerAction {
		Connect,
		Disconnect,
		Grade_Update
	}

}
