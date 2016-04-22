package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.minecraft.Grades;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.utils.ServerLabel;

public class PacketPhantomPlayer extends PacketPhantom {

	private UUID player;
	private PlayerAction action;
	private Grades grade;
	private String serverLabel_last;
	private String serverLabel_new;

	@Override
	protected void serialize_() {
		writeString(this.player.toString());
		writeString(this.action.name());
		writeString(this.serverLabel_last);
		writeString(this.serverLabel_new);
		writeString(grade.name());
	}

	@Override
	protected void deserialize_() {
		this.player = UUID.fromString(readString());
		this.action = PlayerAction.valueOf(readString());
		this.serverLabel_last = readString();
		this.serverLabel_new = readString();
		this.grade = Grades.valueOf(readString());
	}

	public PacketPhantomPlayer(UUID uid, PlayerAction action, Grades gr, String serverlabelLast, String serverLabelNew) {
		this.player = uid;
		this.action = action;
		this.grade = gr;
		this.serverLabel_last = serverlabelLast;
		this.serverLabel_new = serverLabelNew;
	}

	public Grades getGrade() {
		return grade;
	}

	public ServerType getServerTypeLast() {
		if (getAction() == PlayerAction.Connect) throw new NullPointerException("Impossible de récuperer le type de serveurLast car le joueur vient de se connecter sur le network !");
		return ServerLabel.getTypeWithLabel(this.serverLabel_last);
	}

	public ServerType getServerTypeNew() {
		if (getAction() == PlayerAction.Disconnect) throw new NullPointerException("Impossible de récuperer le type de serveurNew car le joueur vient de se déconnecter du network !");
		return ServerLabel.getTypeWithLabel(this.serverLabel_new);
	}

	/**
	 * Attention cette methode peut creer du sale NPE
	 * 
	 * @return le serveur via le manager grace au label
	 */
	public Server getServerLast() {
		if (getAction() == PlayerAction.Connect) throw new NullPointerException("Impossible de récuperer le serveurLast car le joueur vient de se connecter sur le network !");
		return Manager.getInstance().getServersByLabel().get(this.serverLabel_last);
	}

	public Server getServerNew() {
		if (getAction() == PlayerAction.Disconnect) throw new NullPointerException("Impossible de récuperer le serveurNew car le joueur vient de se déconnecter du network !");
		return Manager.getInstance().getServersByLabel().get(this.serverLabel_new);
	}

	public UUID getPlayer() {
		return player;
	}

	public PlayerAction getAction() {
		return action;
	}

	public static enum PlayerAction {
		Connect,
		Switch,
		Disconnect,
	}

}
