package sceat.domain.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import sceat.SPhantom;
import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.minecraft.Grades;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Vps;
import sceat.domain.protocol.packets.PacketPhantom.PacketNotRegistredException;
import sceat.domain.protocol.packets.PacketPhantomBootServer;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomPlayer.PlayerAction;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;
import sceat.domain.protocol.packets.PacketPhantomSymbiote;
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
	 * @throws PacketNotRegistredException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public synchronized void handle(messagesType type, byte[] array) throws IllegalAccessException, InstantiationException, PacketNotRegistredException {
		if (m == null) SPhantom.print("Le manager est null !");
		boolean logPkt = SPhantom.getInstance().logPkt();
		switch (type) {
			case HeartBeat:
				PacketPhantomHeartBeat pkt = PacketPhantomHeartBeat.fromByteArray(array).deserialize();
				if (logPkt) SPhantom.print("<<<<]RECV] PacketHeartBeat [Last " + new java.sql.Timestamp(pkt.getLastHandShake()).toString().substring(0, 16) + "]");
				Heart.getInstance().transfuse(pkt);
				return;
			case TakeLead:
				PacketPhantomHeartBeat var2 = PacketPhantomHeartBeat.fromByteArray(array).deserialize();
				if (logPkt) SPhantom.print("<<<<]RECV] PacketTakeLead []");
				Heart.getInstance().transplant(var2);
				return;
			case BootServer: // Les autres instances de sphantom vont recevoir ce packet tout comme le symbiote et elle pourront ainsi afficher un new srv en statut CREATING
				PacketPhantomBootServer var = PacketPhantomBootServer.fromByteArray(array).deserialize();
				if (var.cameFromLocal()) return;
				if (logPkt) SPhantom.print("<<<<]RECV] PacketBootServer [" + var.getLabel() + "|MaxP(" + var.getMaxP() + ")|Ram(" + var.getRam() + ")]");
				Server.fromPacket(new PacketPhantomServerInfo(Statut.CREATING, var.getLabel(), var.getVpsLabel(), var.getIp(), var.getType(), var.getMaxP(), new HashMap<Grades, Set<UUID>>(), var
						.getType().getKeysAsSet(), false), false);
				return;
			case Symbiote_Infos:
				PacketPhantomSymbiote var4 = PacketPhantomSymbiote.fromByteArray(array).deserialize();
				if (logPkt) SPhantom.print("<<<<]RECV] PacketSymbiote [" + var4.getVpsLabel() + "|" + var4.getState() + "|" + var4.getIp().getHostAddress() + "|Ram(" + var4.getRam() + ")]");
				ConcurrentHashMap<String, Vps> varmap = Core.getInstance().getVps();
				if (varmap.containsKey(var4.getVpsLabel())) varmap.get(var4.getVpsLabel()).setState(var4.getState());
				else new Vps(var4.getVpsLabel(), var4.getRam(), var4.getIp(), new HashSet<Server>()).register();
				return;
			case Update_Server:
				PacketPhantomServerInfo var1 = PacketPhantomServerInfo.fromByteArray(array).deserialize();
				if (var1.cameFromLocal()) return;
				if (logPkt) SPhantom.print("<<<<]RECV] PacketUpdateServer [" + var1.getLabel() + "|" + var1.getState().name() + "|players(" + var1.getPlayers().size() + ")]");
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
					PacketSender.getInstance().sendServer(PacketPhantomServerInfo.fromServer(srv));
					return;
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
				return;
			case Update_PlayerAction:
				PacketPhantomPlayer var3 = PacketPhantomPlayer.fromByteArray(array).deserialize();
				if (var3.cameFromLocal()) return;
				if (logPkt) SPhantom.print("<<<<]RECV] PacketPlayer [" + var3.getPlayer() + "|" + var3.getAction().name() + "|"
						+ (var3.getAction() == PlayerAction.Grade_Update ? (var3.getGrade().name() + " to " + var3.getNewGrade().name()) : var3.getGrade().name()) + "]");
				if (var3.getAction() == PlayerAction.Connect) {
					m.getPlayersOnNetwork().add(var3.getPlayer());
					m.getPlayersPerGrade().get(var3.getGrade()).add(var3.getPlayer());
					var3.getServer().getPlayersMap().get(var3.getGrade()).add(var3.getPlayer());
					Core.getInstance().getPlayersByType().get(var3.getServerType()).add(var3.getPlayer());
				} else if (var3.getAction() == PlayerAction.Grade_Update) {
					m.getPlayersPerGrade().get(var3.getGrade()).removeIf(e -> e == var3.getPlayer());
					m.getPlayersPerGrade().get(var3.getNewGrade()).add(var3.getPlayer());
					var3.getServer().getPlayersMap().get(var3.getGrade()).removeIf(e -> e == var3.getPlayer());
					var3.getServer().getPlayersMap().get(var3.getNewGrade()).add(var3.getPlayer());
				} else {
					m.getPlayersOnNetwork().removeIf(e -> e == var3.getPlayer());
					m.getPlayersPerGrade().get(var3.getGrade()).removeIf(e -> e == var3.getPlayer());
					var3.getServer().getPlayers().removeIf(e -> e == var3.getPlayer());
					Core.getInstance().getPlayersByType().get(var3.getServerType()).removeIf(e -> e == var3.getPlayer());
				}
				PacketSender.getInstance().sendPlayer(var3);
				return;
			default:
				return;
		}
	}
}
