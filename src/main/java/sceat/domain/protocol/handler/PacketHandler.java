package sceat.domain.protocol.handler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sceat.Main;
import sceat.domain.protocol.packets.PacketPhantom;
import sceat.infra.connector.mq.RabbitMqConnector;

/**
 * Le PacketSender peut se mettre en pause en cas de prise du lead par un autre replica,
 * <p>
 * pour des raison d'affichage custom je ne met pas en pause le PacketHandler pour la simple raison qu'en continuant a process les packets je pourrai afficher via JavaFX le nombre de joueur etc
 * 
 * @author MrSceat
 *
 */
public class PacketHandler {

	private class RawPacket{
		private RabbitMqConnector.MessagesType type;
		private byte[] data;

		public RawPacket(RabbitMqConnector.MessagesType type, byte[] data) {
			this.type = type;
			this.data = data;
		}
	}

	private class PacketDeserializer implements Runnable{
		private RawPacket rawPacket;

		public PacketDeserializer(RawPacket rawPacket, PacketWatchDog watchDog) {
			this.rawPacket = rawPacket;
		}

		public PacketDeserializer(RawPacket rawPacket) {
			this.rawPacket = rawPacket;
		}

		@Override
		public void run() {
			try {
				PacketPhantom.fromByteArray(rawPacket.data).handleData();
			} catch (Exception e) {
				Main.printStackTrace(e);
			}
			rawPackets.remove(rawPacket);
			if(rawPackets.isEmpty())
				watchDog.notifyEnd();

		}
	}
	private static PacketHandler instance;

	private List<RawPacket> rawPackets;
	private PacketWatchDog watchDog;
	private ExecutorService pool;

	public PacketHandler() {
		instance = this;
		rawPackets = new CopyOnWriteArrayList<>();
		watchDog = new PacketWatchDog(this);
		pool = Executors.newFixedThreadPool(10);
	}

	public static PacketHandler getInstance() {
		return instance;
	}

	/**
	 * Les listes des joueurs s'updatent ? chaque reception de packet. Les packets serveur servent ? mettre ? jour globalement Sphantom notamment quand une nouvelle instance de sphantom est lanc?e, il ne permettent pas d'enlever des joueurs des autres listes mais remplacent la liste des joueurs dans
	 * la map <serveurLabel,Serveur>
	 * <p>
	 * les updates par player permettent d'ajouter un joueur dans toutes les listes ainsi que de l'enlever quand il se d?connecte
	 * <p>
	 * Les deux updates sont requises pour ?viter un lourd traitement de donn?es si on avait uniquement les packets serveur, il y a d'autres raisons pratique mais c'est assez complex et je galere a m'en souvenir donc je completerai ce commentaire plus tard !
	 * 
	 * @param type
	 * @param msg
	 */

	public void handle(RabbitMqConnector.MessagesType type, byte[] msg) {
		RawPacket packet = new RawPacket(type , msg);
		rawPackets.add(packet);
		pool.execute(new PacketDeserializer(packet));
		watchDog.notifyStart();
	}

	public void reorganisePackets() {
		pool.shutdownNow();//Ignore runnables
		pool = Executors.newFixedThreadPool(10);//Recreate
		//Remove Packet can be dropped
		Iterator<RawPacket> packets = rawPackets.iterator();
		while (packets.hasNext())
			if(packets.next().type.canBeDropped())
				packets.remove();
		rawPackets.sort((i1 , i2) -> Integer.compare(i1.type.getPriority() , i2.type.getPriority()));
		rawPackets.forEach(e -> pool.execute(new PacketDeserializer(e)));
	}
}
