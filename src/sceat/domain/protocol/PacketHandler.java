package sceat.domain.protocol;

import java.util.Set;
import java.util.UUID;

import sceat.SPhantom;
import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Vps;
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
	public synchronized void handle(messagesType type, String msg, byte[] array) {
		if (m == null) SPhantom.print("Le manager est null !");
		switch (type) {
			case HeartBeat:
				if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketHeartBeat []");
				Heart.getInstance().transfuse(msg);
				break;
			case TakeLead:
				if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketTakeLead []");
				Heart.getInstance().transplant(msg);
				break;
			case Symbiote:
				// le packet symbiote doit contenir le label du vps, son statut et sa ram
				// si le vps existe on sync son statut sinon on le crée et on le vps.register
				break;
			case Update_Server:
				PacketPhantomServerInfo var1 = (PacketPhantomServerInfo) PacketPhantomServerInfo.fromByteArray(array);
				if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketServer [" + var1.getLabel() + "|" + var1.getState().name() + "|players(" + var1.getPlayers().size() + ")]");
				if (var1.getState() == Statut.CLOSING) {
					Server srv = Server.fromPacket(var1, true);
					Vps curr = null;
					if (srv == null) {
						SPhantom.print("PacketPhantomServerInfo : State Closing | the server " + var1.getLabel() + " is not registered | Ignoring ! break");
						break;
					} else if (var1.getVpsLabel() == null) {
						bite: for (Vps vps : Core.getInstance().getVps().values()) {
							for (Server s : vps.getServers())
								if (s.getLabel().equalsIgnoreCase(var1.getLabel())) {
									curr = vps;
									break bite;
								}
						}
					} else curr = srv.getVps();
					Set<Server> ss = Core.getInstance().getServersByType().get(var1.getType());
					if (ss.contains(srv)) ss.remove(srv);
					m.getServersByLabel().remove(var1.getLabel());
					if (curr == null) {
						// vps not found osef car tt façon on le vire
						SPhantom.print("PacketPhantomServerInfo : State Closing | the server " + var1.getLabel() + " is registered but not in a Vps object | Info ! break");
						break;
					}
					if (curr.getServers().contains(srv)) curr.getServers().remove(srv);
					break;
				}
				Server srvf = Server.fromPacket(var1, false);
				srvf.heartBeat();
				m.getServersByLabel().put(var1.getLabel(), srvf);
				Core.getInstance().getServersByType().get(var1.getType()).add(srvf);
				Set<UUID> players = var1.getPlayers();
				m.getPlayersOnNetwork().addAll(players);
				m.getPlayersPerGrade().entrySet().forEach(e -> e.getValue().addAll(var1.getPlayersPerGrade().get(e.getKey())));
				Core.getInstance().getPlayersByType().get(var1.getType()).addAll(players);
				PacketSender.getInstance().sendServer(PacketPhantomServerInfo.fromServer(srvf));
				break;
			case Update_PlayerAction:
				PacketPhantomPlayer var2 = PacketPhantomPlayer.fromJson(msg);
				if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketPlayer [" + var2.getPlayer() + "|" + var2.getAction().name() + "|"
						+ (var2.getAction() == PlayerAction.Grade_Update ? (var2.getGrade().name() + " to " + var2.getNewGrade().name()) : var2.getGrade().name()) + "]");
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
				PacketSender.getInstance().sendPlayer(var2);
				break;
			default:
				break;
		}
	}
}
