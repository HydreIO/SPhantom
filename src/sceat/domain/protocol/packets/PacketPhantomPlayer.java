package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.minecraft.Grades;
import sceat.domain.network.Server;
import sceat.domain.network.Server.ServerType;
import sceat.domain.utils.ServerLabel;
import sceat.domain.utils.UtilGson;

public class PacketPhantomPlayer extends PacketPhantom {

	private UUID player;
	private Grades grade;
	private PlayerAction action;
	private String serverLabel;

	public PacketPhantomPlayer(UUID uid, Grades grad, PlayerAction action, String labelOnConnect) {
		this.player = uid;
		this.grade = grad;
		this.action = action;
		this.serverLabel = labelOnConnect;
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

	@Override
	public String toJson() {
		return UtilGson.serialize(this);
	}

	public static PacketPhantomPlayer fromJson(String json) {
		return UtilGson.deserialize(json, PacketPhantomPlayer.class);
	}

	public static enum PlayerAction {
		Connect,
		Disconnect
	}
}
