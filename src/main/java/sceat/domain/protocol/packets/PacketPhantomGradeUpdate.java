package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.common.mq.Broker;
import sceat.domain.network.server.Server;
import fr.aresrpg.sdk.mc.Grades;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

public class PacketPhantomGradeUpdate extends PacketPhantom {
	private UUID player;
	private Grades lastGrade;
	private Grades newGrade;
	private String server;

	public PacketPhantomGradeUpdate() {
	}

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
	public void handleData(MessagesType tp) {
		if (cameFromLocal()) return;
		Log.packet(this, true);
		getServer().getPlayersMap().get(lastGrade).safeRemove(getPlayer());
		getServer().getPlayersMap().get(newGrade).add(player);
	}

	@Override
	public String toString() {
		return "PacketPlayerGrade [" + getPlayer() + "|New(" + getNewGrade().name() + ")|Last(" + getLastGrade().name() + ")]";
	}

	public String getServerLabel() {
		return server;
	}

	public Server getServer() {
		return Manager.getInstance().getServersByLabel().safeGet(getServerLabel());
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

	@Override
	public void send() {
		Broker.get().gradeUpdate(this);
	}

}
