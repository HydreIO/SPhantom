package sceat.domain.protocol.packets;

import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;

public class PacketPhantomBroadcast extends PacketPhantom {
	public PacketPhantomBroadcast() {
		// unused
	}

	@Override
	protected void serialize_() {
		// unused
	}

	@Override
	protected void deserialize_() {
		// unused
	}

	@Override
	public void handleData(MessagesType type) {
		throwCantHandle("PacketBroadcast");
	}

	@Override
	public void send() {
		throwCantSend("PacketBroadcast");
	}

}
