package sceat.domain.protocol;

import sceat.SPhantom;
import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.network.Server;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomPlayer.PlayerAction;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;
import sceat.infra.connector.mq.RabbitMqConnector.messagesType;

/**
 * Le PacketSender peut se mettre en paus en cas de prise du lead par un autre replica,
 * <p>
 * pour des raison d'affichage custom je ne met pas en pause le PacketHandler pour la simple raison qu'en continuant a process les packets je pourrai afficher via JavaFX le nombre de joueur etc
 * 
 * @author MrSceat
 *
 */
public class PacketHandler {

	private static PacketHandler instance = new PacketHandler();
	private static Manager m = Manager.getInstance();

	private PacketHandler() {
	}

	public static PacketHandler getInstance() {
		return instance;
	}

	public void handle(messagesType type, String msg) {
		if (m == null) SPhantom.print("Le manager est null !");
		switch (type) {
			case HeartBeat:
				Heart.getInstance().transfuse(msg);
				break;
			case TakeLead:
				Heart.getInstance().transplant(msg);
				break;
			case Update_Server:
				PacketPhantomServerInfo var1 = PacketPhantomServerInfo.fromJson(msg);
				m.getServersByLabel().put(var1.getLabel(), Server.fromPacket(var1));
				break;
			case Update_PlayerAction:
				PacketPhantomPlayer var2 = PacketPhantomPlayer.fromJson(msg);
				if (var2.getAction() == PlayerAction.Connect) {
					m.getPlayersOnNetwork().add(var2.getPlayer());
					m.getPlayersPerGrade().get(var2.getGrade()).add(var2.getPlayer());
				} else {
					m.getPlayersOnNetwork().removeIf(e -> e == var2.getPlayer());
					m.getPlayersPerGrade().get(var2.getGrade()).removeIf(e -> e == var2.getPlayer());
				}
				break;
			default:
				break;
		}
	}

}
