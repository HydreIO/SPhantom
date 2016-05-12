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
import sceat.domain.trigger.PhantomTrigger;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

public class PacketPhantomSymbiote extends PacketPhantom {

	private String vpsLabel;
	private VpsState state;
	private int ram;
	private long created;
	private InetAddress ip;

	public PacketPhantomSymbiote(String vpsLabel, VpsState state, int ram, InetAddress ip, long created) {
		this.vpsLabel = vpsLabel;
		this.created = created;
		this.state = state;
		this.ip = ip;
		this.ram = ram;
	}

	public PacketPhantomSymbiote() {
	}

	@Override
	protected void serialize_() {
		writeString(getVpsLabel());
		writeByte(getState().getId());
		writeInt(getRam());
		writeLong(created);
		writeString(getIp().getHostAddress());
	}

	@Override
	protected void deserialize_() {
		this.vpsLabel = readString();
		this.state = VpsState.fromId(readByte());
		this.ram = readInt();
		this.created = readLong();
		try {
			this.ip = InetAddress.getByName(readString());
		} catch (UnknownHostException e) {
			Main.printStackTrace(e);
		}
	}

	public long getCreated() {
		return created;
	}

	@Override
	public String toString() {
		return "PacketSymbiote [" + getVpsLabel() + "|" + getState() + "|" + getIp().getHostAddress() + "|Ram(" + getRam() + ")]";
	}

	@Override
	public void handleData(MessagesType type) {
		Log.packet(this, true);
		ConcurrentHashMap<String, Vps> varmap = Core.getInstance().getVps();
		if (varmap.containsKey(getVpsLabel())) varmap.get(getVpsLabel()).setUpdated(true).setState(getState()).setCreatedMilli(getCreated());
		else new Vps(getVpsLabel(), getRam(), getIp(), new HashSet<Server>(), getCreated()).register().setUpdated(true).setState(getState());
		Vps v = Core.getInstance().getVps().getOrDefault(vpsLabel, null);
		if (v != null) PhantomTrigger.getAll().forEach(t -> t.handleVps(v));
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

	@Override
	public void send() {
		throwCantSend("PacketSymbiote");
	}

}
