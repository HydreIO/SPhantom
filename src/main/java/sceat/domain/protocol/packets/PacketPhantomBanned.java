package sceat.domain.protocol.packets;

import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;

public class PacketPhantomBanned extends PacketPhantom {

	@Override
	protected void serialize_() {
	}

	@Override
	protected void deserialize_() {
	}

	public PacketPhantomBanned() {
	}

	@Override
	public void handleData(MessagesType type) {
	}

	@Override
	public void send() {
		throwCantSend("PacketPhantomBanned");
	}

}
