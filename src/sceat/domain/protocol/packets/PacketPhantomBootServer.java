package sceat.domain.protocol.packets;

import sceat.SPhantom;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;

@SuppressWarnings("unchecked")
public class PacketPhantomBootServer extends PacketPhantom {

	private String label;
	private ServerType type;
	private int ram;
	private int maxP;

	public PacketPhantomBootServer(Server srv) {
		this.label = srv.getLabel();
		this.type = srv.getType();
		this.ram = SPhantom.getInstance().getSphantomConfig().getRamFor(getType());
		this.maxP = srv.getMaxPlayers();
	}

	@Override
	protected <T extends PacketPhantom> T serialize_() {
		writeString(getLabel());
		writeString(getType().name());
		writeInt(getRam());
		writeInt(getMaxP());
		return (T) this;
	}

	@Override
	protected <T extends PacketPhantom> T deserialize_() {
		this.label = readString();
		this.type = ServerType.valueOf(readString());
		this.ram = readInt();
		this.maxP = readInt();
		return (T) this;
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
