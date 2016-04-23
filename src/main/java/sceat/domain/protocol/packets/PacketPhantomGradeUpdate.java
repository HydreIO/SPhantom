package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.minecraft.Grades;
import sceat.domain.network.server.Server;

public class PacketPhantomGradeUpdate extends PacketPhantom {
	private UUID player;
	private Grades lastGrade;
	private Grades newGrade;
	private String server;

	public PacketPhantomGradeUpdate() {}

	public PacketPhantomGradeUpdate(UUID player, String srv, Grades last, Grades neww) {
		this.player = player;
		this.lastGrade = last;
		this.server = srv;
		this.newGrade = neww;
	}

	@Override
	protected void serialize_() {
		writeString(getPlayer().toString());
		writeByte(getLastGrade().getValue());
		writeByte(getNewGrade().getValue());
		writeString(getServerLabel());
	}

	@Override
	protected void deserialize_() {
		this.player = UUID.fromString(readString());
		this.lastGrade = Grades.fromValue(readByte(), true);
		this.newGrade = Grades.fromValue(readByte(), true);
		this.server = readString();
	}

	@Override
	public void handleData() {
		Manager.getInstance().getPlayersPerGrade().get(lastGrade)
				.removeIf(e -> e.equals(player));
		Manager.getInstance().getPlayersPerGrade().get(newGrade).add(player);
		getServer().getPlayersMap().get(lastGrade).removeIf(e -> e.equals(player));
		getServer().getPlayersMap().get(newGrade).add(player);
	}

	public String getServerLabel() {
		return server;
	}

	public Server getServer() {
		return Manager.getInstance().getServersByLabel().get(getServerLabel());
	}

	public UUID getPlayer() {
		return player;
	}

	public Grades getLastGrade() {
		return lastGrade;
	}

	public Grades getNewGrade() {
		return newGrade;
	}

}
