package sceat.domain.protocol;

import sceat.domain.Manager;
import sceat.domain.Manager.Notifier;
import sceat.domain.protocol.packets.PacketPhantom;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomPlayer.PlayerAction;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;
import sceat.domain.server.Server;

public class PacketHandler {

	private static PacketHandler instance = new PacketHandler();

	private PacketHandler() {

	}

	public static PacketHandler getInstance() {
		return instance;
	}

	public void receive(Notifier n, PacketPhantom pkt) {
		Manager m = Manager.getInstance();
		switch (n) {
			case PacketPhantomServerInfo:
				assert pkt instanceof PacketPhantomServerInfo : "This packet is not an instance of PacketPhantomServerInfo !";
				PacketPhantomServerInfo var1 = (PacketPhantomServerInfo) pkt;
				m.getServersByLabel().put(var1.getLabel(), Server.fromPacket(var1));
				break;
			case PacketPhantomPlayer:
				assert pkt instanceof PacketPhantomPlayer : "This packet is not an instance of PacketPhantomPlayer !";
				PacketPhantomPlayer var2 = (PacketPhantomPlayer) pkt;
				if (var2.getAction() == PlayerAction.Connect) {
					m.getPlayersOnNetwork().add(var2.getPlayer());
					m.getPlayersPerGrade().get(var2.getGrade()).add(var2.getPlayer());
				} else {
					m.getPlayersOnNetwork().removeIf(e -> e == var2.getPlayer());
					m.getPlayersPerGrade().get(var2.getGrade()).removeIf(e -> e == var2.getPlayer());
				}
			default:
				break;
		}
	}

}
