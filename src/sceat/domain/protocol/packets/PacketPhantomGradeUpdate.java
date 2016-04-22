package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.minecraft.Grades;

public class PacketPhantomGradeUpdate extends PacketPhantom {

	@Override
	protected void serialize_() {
		writeString(getPlayer().toString());
		writeInt(getLast().getValue());
		writeInt(getNew_().getValue());
	}

	@Override
	protected void deserialize_() {
		this.player = UUID.fromString(readString());
		this.last = Grades.fromValue(readInt(), true);
		this.new_ = Grades.fromValue(readInt(), true);
	}

	private UUID player;
	private Grades last;
	private Grades new_;

	public PacketPhantomGradeUpdate(UUID player, Grades last, Grades neww) {
		this.player = player;
		this.last = last;
		this.new_ = neww;
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
