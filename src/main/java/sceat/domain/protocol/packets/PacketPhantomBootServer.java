package sceat.domain.protocol.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.protocol.MessagesType;

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
		if (cameFromLocal()) return;
		if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketBootServer [" + getLabel() + "|MaxP(" + getMaxP() + ")|Ram(" + getRam() + ")]");
		Server.fromPacket(new PacketPhantomServerInfo(Statut.CREATING, label, vpsLabel, ip, type, maxP, new HashMap<>(), type.getKeysAsSet(), false), false);
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

}
