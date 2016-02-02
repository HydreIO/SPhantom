package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.network.Grades;
import sceat.domain.utils.UtilGson;

public class PacketPhantomPlayer extends PacketPhantom {

	private UUID player;
	private Grades grade;
	private PlayerAction action;

	public PacketPhantomPlayer(UUID uid, Grades grad, PlayerAction action) {
		this.player = uid;
		this.grade = grad;
		this.action = action;
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
