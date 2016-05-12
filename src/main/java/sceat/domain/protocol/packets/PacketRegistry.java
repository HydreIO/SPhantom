package sceat.domain.protocol.packets;

import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.protocol.PacketPhantom.PacketIdAlrealyUsedException;
import fr.aresrpg.sdk.system.Log;

public class PacketRegistry {

	private static PacketRegistry in = new PacketRegistry();

	private PacketRegistry() {

	}

	public static void registry() {
		Log.out("Initialising packets...");
		in.registerPacket(1, PacketPhantomServerInfo.class); // servers
		in.registerPacket(2, PacketPhantomHeartBeat.class); // sphantom ping
		in.registerPacket(3, PacketPhantomPlayer.class); // players connect/disconnect
		in.registerPacket(4, PacketPhantomBootServer.class); // server boot
		in.registerPacket(5, PacketPhantomSymbiote.class); // vps
		in.registerPacket(6, PacketPhantomDestroyInstance.class); // destroy vps for other sphantom
		in.registerPacket(7, PacketPhantomReduceServer.class); // reduction
		in.registerPacket(8, PacketPhantomKillProcess.class); // kill server cg overhead
		in.registerPacket(9, PacketPhantomGradeUpdate.class); // player grade
		in.registerPacket(10, PacketPhantomBanned.class);
		in.registerPacket(11, PacketPhantomBroadcast.class);
	}

	private void registerPacket(int id, Class<? extends PacketPhantom> clazz) {
		try {
			PacketPhantom.registerPacket((byte) id, clazz);
		} catch (PacketIdAlrealyUsedException e) {
			Log.trace(e);
		}
	}

}
