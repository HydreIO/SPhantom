package sceat.domain.protocol.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import sceat.SPhantom;
import sceat.domain.common.mq.Broker;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.trigger.PhantomTrigger;
import fr.aresrpg.commons.util.collection.Set;
import fr.aresrpg.commons.util.map.EnumHashMap;
import fr.aresrpg.sdk.mc.Grades;
import fr.aresrpg.sdk.mc.Statut;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

public class PacketPhantomBootServer extends PacketPhantom {

	private String label;
	private String vpsLabel;
	private ServerType type;
	private InetAddress ip;
	int port;
	private int ram;
	private int maxP;

	public PacketPhantomBootServer(Server srv) {
		this.label = srv.getLabel();
		this.type = srv.getType();
		this.vpsLabel = srv.getVpsLabel();
		this.ip = srv.getIpadress();
		this.ram = SPhantom.getInstance().getSphantomConfig().getRamFor(getType());
		this.maxP = srv.getMaxPlayers();
		this.port = srv.getPort();
	}

	public PacketPhantomBootServer() {
		// For deserialization
	}

	@Override
	protected void serialize_() {
		writeString(getLabel());
		writeString(getVpsLabel());
		writeByte(getType().getId());
		writeString(getIp().getHostAddress());
		writeInt(getPort());
		writeInt(getRam());
		writeInt(getMaxP());
	}

	@Override
	protected void deserialize_() {
		this.label = readString();
		this.vpsLabel = readString();
		this.type = ServerType.fromByte(readByte());
		try {
			this.ip = InetAddress.getByName(readString());
		} catch (UnknownHostException e) {
			Log.trace(e);
		}
		this.port = readInt();
		this.ram = readInt();
		this.maxP = readInt();
	}

	public int getPort() {
		return port;
	}

	@Override
	public void handleData(MessagesType te) {
		if (vpsLabel != null) {
			Vps v = Core.getInstance().getVps().getOrDefault(vpsLabel, null);
			if (v != null) PhantomTrigger.getAll().forEach(t -> t.handleVps(v));
		}
		if (cameFromLocal()) return;
		Log.packet(this, true);
		Server.fromPacket(new PacketPhantomServerInfo(Statut.CREATING, label, vpsLabel, ip, type, maxP, port, new EnumHashMap<Grades, Set<UUID>>(Grades.class), false), false);
	}

	@Override
	public String toString() {
		return "PacketBootServer [" + getLabel() + "|MaxP(" + getMaxP() + ")|Ram(" + getRam() + ")]";
	}

	public InetAddress getIp() {
		return ip;
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

	public String getLabel() {
		return label;
	}

	public ServerType getType() {
		return type;
	}

	public int getRam() {
		return ram;
	}

	public int getMaxP() {
		return maxP;
	}

	@Override
	public void send() {
		Broker.get().bootServer(this);
	}

}
