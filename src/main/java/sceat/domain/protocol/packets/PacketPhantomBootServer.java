package sceat.domain.protocol.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.common.mq.Broker;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.trigger.PhantomTrigger;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

public class PacketPhantomBootServer extends PacketPhantom {

	private String label;
	private String vpsLabel;
	private ServerType type;
	private InetAddress ip;
	private int ram;
	private int maxP;

	public PacketPhantomBootServer(Server srv) {
		this.label = srv.getLabel();
		this.type = srv.getType();
		this.vpsLabel = srv.getVpsLabel();
		this.ip = srv.getIpadress();
		this.ram = SPhantom.getInstance().getSphantomConfig().getRamFor(getType());
		this.maxP = srv.getMaxPlayers();
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
			Main.printStackTrace(e);
		}
		this.ram = readInt();
		this.maxP = readInt();
	}

	@Override
	public void handleData(MessagesType te) {
		if (vpsLabel != null) {
			Vps v = Core.getInstance().getVps().getOrDefault(vpsLabel, null);
			if (v != null) PhantomTrigger.getAll().forEach(t -> t.handleVps(v));
		}
		if (cameFromLocal()) return;
		Log.packet(this, true);
		Server.fromPacket(new PacketPhantomServerInfo(Statut.CREATING, label, vpsLabel, ip, type, maxP, new HashMap<>(), false), false);
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
