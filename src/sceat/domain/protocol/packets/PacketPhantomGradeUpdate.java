package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.minecraft.Grades;
import sceat.domain.network.server.Server;

public class PacketPhantomGradeUpdate extends PacketPhantom {

	@Override
	protected void serialize_() {
		writeString(getPlayer().toString());
		writeInt(getLast().getValue());
		writeInt(getNew_().getValue());
		writeString(getServerLabel());
	}

	@Override
	protected void deserialize_() {
		this.player = UUID.fromString(readString());
		this.last = Grades.fromValue(readInt(), true);
		this.new_ = Grades.fromValue(readInt(), true);
		this.server = readString();
	}

	private UUID player;
	private Grades last;
	private Grades new_;
	private String server;

	public PacketPhantomGradeUpdate(UUID player, String srv, Grades last, Grades neww) {
		this.player = player;
		this.last = last;
		this.server = srv;
		this.new_ = neww;
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

	public Grades getLast() {
		return last;
	}

	public Grades getNew_() {
		return new_;
	}

}
