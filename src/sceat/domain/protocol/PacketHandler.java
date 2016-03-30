package sceat.domain.protocol;

import java.util.Set;
import java.util.UUID;

import sceat.SPhantom;
import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.network.Core;
import sceat.domain.network.Server;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomPlayer.PlayerAction;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;
import sceat.infra.connector.mq.RabbitMqConnector.messagesType;

/**
 * Le PacketSender peut se mettre en pause en cas de prise du lead par un autre replica,
 * <p>
 * pour des raison d'affichage custom je ne met pas en pause le PacketHandler pour la simple raison qu'en continuant a process les packets je pourrai afficher via JavaFX le nombre de joueur etc
 * 
 * @author MrSceat
 *
 */
public class PacketHandler {

	private static PacketHandler instance;
	private static Manager m;

	public PacketHandler() {
		instance = this;
		m = Manager.getInstance();
	}

	public static PacketHandler getInstance() {
		return instance;
	}

	/**
	 * Les listes des joueurs s'updatent à chaque reception de packet. Les packets serveur servent à mettre à jour globalement Sphantom notamment quand une nouvelle instance de sphantom est lancée, il ne permettent pas d'enlever des joueurs des autres listes mais remplacent la liste des joueurs dans
	 * la map <serveurLabel,Serveur>
	 * <p>
	 * les updates par player permettent d'ajouter un joueur dans toutes les listes ainsi que de l'enlever quand il se déconnecte
	 * <p>
	 * Les deux updates sont requises pour éviter un lourd traitement de données si on avait uniquement les packets serveur, il y a d'autres raisons pratique mais c'est assez complex et je galere a m'en souvenir donc je completerai ce commentaire plus tard !
	 * 
	 * @param type
	 * @param msg
	 */
	public synchronized void handle(messagesType type, String msg) {
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
				Set<UUID> players = var1.getPlayers();
				m.getPlayersOnNetwork().addAll(players);
				m.getPlayersPerGrade().entrySet().forEach(e -> e.getValue().addAll(var1.getPlayersPerGrade().get(e.getKey())));
				Core.getInstance().getPlayersByType().get(var1.getType()).addAll(players);
				break;
			case Update_PlayerAction:
				PacketPhantomPlayer var2 = PacketPhantomPlayer.fromJson(msg);
				if (var2.getAction() == PlayerAction.Connect) {
					m.getPlayersOnNetwork().add(var2.getPlayer());
					m.getPlayersPerGrade().get(var2.getGrade()).add(var2.getPlayer());
					var2.getServer().getPlayersMap().get(var2.getGrade()).add(var2.getPlayer());
					Core.getInstance().getPlayersByType().get(var2.getServerType()).add(var2.getPlayer());
				} else if (var2.getAction() == PlayerAction.Grade_Update) {
					m.getPlayersPerGrade().get(var2.getGrade()).removeIf(e -> e == var2.getPlayer());
					m.getPlayersPerGrade().get(var2.getNewGrade()).add(var2.getPlayer());
					var2.getServer().getPlayersMap().get(var2.getGrade()).removeIf(e -> e == var2.getPlayer());
					var2.getServer().getPlayersMap().get(var2.getNewGrade()).add(var2.getPlayer());
				} else {
					m.getPlayersOnNetwork().removeIf(e -> e == var2.getPlayer());
					m.getPlayersPerGrade().get(var2.getGrade()).removeIf(e -> e == var2.getPlayer());
					var2.getServer().getPlayers().removeIf(e -> e == var2.getPlayer());
					Core.getInstance().getPlayersByType().get(var2.getServerType()).removeIf(e -> e == var2.getPlayer());
				}
				break;
			default:
				break;
		}
	}
}
