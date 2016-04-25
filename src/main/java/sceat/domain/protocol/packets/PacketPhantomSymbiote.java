package sceat.domain.protocol.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Vps;
import sceat.domain.network.server.Vps.VpsState;
import sceat.infra.connector.mq.RabbitMqConnector.MessagesType;

public class PacketPhantomSymbiote extends PacketPhantom {
	private String vpsLabel;
	private VpsState state;
	private int ram;
	private InetAddress ip;

	public PacketPhantomSymbiote(String vpsLabel, VpsState state, int ram, InetAddress ip) {
		this.vpsLabel = vpsLabel;
		this.state = state;
		this.ip = ip;
		this.ram = ram;
	}

	public PacketPhantomSymbiote() {
	}

	@Override
	protected void serialize_() {
		writeString(getVpsLabel());
		writeString(getState().name());
		writeInt(getRam());
		writeString(getIp().getHostAddress());
	}

	@Override
	protected void deserialize_() {
		this.vpsLabel = readString();
		this.state = VpsState.valueOf(readString());
		this.ram = readInt();
		try {
			this.ip = InetAddress.getByName(readString());
		} catch (UnknownHostException e) {
			Main.printStackTrace(e);
		}
	}

	@Override
	public void handleData(MessagesType type) {
		if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketSymbiote [" + getVpsLabel() + "|" + getState() + "|" + getIp().getHostAddress() + "|Ram(" + getRam() + ")]");
		ConcurrentHashMap<String, Vps> varmap = Core.getInstance().getVps();
		if (varmap.containsKey(getVpsLabel())) varmap.get(getVpsLabel()).setState(getState());
		else new Vps(getVpsLabel(), getRam(), getIp(), new HashSet<Server>()).register();
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

	public InetAddress getIp() {
		return ip;
	}

	public VpsState getState() {
		return state;
	}

	public int getRam() {
		return ram;
	}

}
