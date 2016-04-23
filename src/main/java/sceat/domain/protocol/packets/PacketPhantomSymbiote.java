package sceat.domain.protocol.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import sceat.Main;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Vps;
import sceat.domain.network.server.Vps.VpsState;

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

	public PacketPhantomSymbiote() {}

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
	public void handleData() {
		ConcurrentHashMap<String, Vps> map = Core.getInstance().getVps();
		if (map.containsKey(vpsLabel))
			map.get(vpsLabel).setState(state);
		else
			new Vps(vpsLabel, ram, ip, new HashSet<>()).register();
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