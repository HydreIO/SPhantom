package sceat.infra.connector.mq;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.adapter.mq.IMessaging;
import sceat.domain.protocol.destinationKey;
import sceat.domain.protocol.packets.PacketPhantomBootServer;
import sceat.domain.protocol.packets.PacketPhantomDestroyInstance;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomReduceServer;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqConnector implements IMessaging {

	private static RabbitMqConnector instance;
	private RabbitMqReceiver receiver;

	public static boolean routing_enabled = true;

	private ConnectionFactory factory = new ConnectionFactory();
	private static Connection connection;

	private static Channel channel;

	public static String type = routing_enabled ? "direct" : "fanout";

	public RabbitMqConnector(String user, String pass, boolean local) {
		init(user, pass, local);
	}

	public RabbitMqReceiver getReceiver() {
		return this.receiver;
	}

	/**
	 * Enum de type de messages
	 *
	 * @author MrSceat
	 */
	public static enum messagesType {
		Update_Server("exchange_server"),
		Update_PlayerAction("exchange_playerAction"),
		Update_PlayerGrade("exchange_playerGrade"),
		HeartBeat("exchange_heartbeat"),
		BootServer("exchange_symbiote_bootServer"),
		Symbiote_Infos("exchange_symbiote"),
		Reduce_Server("exchange_reduceServer"),
		Destroy_Instance("exchange_destroyVps"),
		TakeLead("exchange_takelead");

		private String exchangeName;

		private messagesType(String name) {
			this.exchangeName = name;
		}

		public String getName() {
			return exchangeName;
		}

		public static messagesType fromString(String exchangeN, boolean notNull) {
			for (messagesType e : values())
				if (e.getName().equals(exchangeN)) return e;
			if (notNull) throw new NullPointerException("Aucun type de message n'a pour valeur " + exchangeN);
			return null;
		}
	}

	/**
	 * Initialisation de la connection et du channel, ainsi que déclaration des messages json a envoyer (par leur nom : banP etc) On initialise aussi les receiver (une fois le channel créé)
	 */
	public void init(String user, String passwd, boolean local) {
		instance = this;
		if (local) {
			SPhantom.print("Local mode ! No messaging service.");
			return;
		}
		getFactory().setHost(SPhantom.getInstance().getSphantomConfig().getRabbitAdress());
		getFactory().setPort(SPhantom.getInstance().getSphantomConfig().getRabbitPort());
		getFactory().setUsername(user);
		getFactory().setPassword(passwd);
		try {
			connection = getFactory().newConnection();
			channel = getConnection().createChannel();
		} catch (IOException | TimeoutException e) {
			SPhantom.print("Unable to access message broker RMQ, Sphantom is going down..", true);
			Main.printStackTrace(e);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				Main.printStackTrace(e);
			}
			Main.shutDown();
			return;
		}
		SPhantom.print("Sucessfully connected to broker RMQ");
		Arrays.stream(messagesType.values()).forEach(this::exchangeDeclare);
		this.receiver = new RabbitMqReceiver();
	}

	/**
	 * Utilisé pour fermer la connection onDisable // A METTRE ONDISABLE()
	 */
	public void close() {
		try {
			getChannel().close();
			getConnection().close();
		} catch (IOException | TimeoutException e) {
			Main.printStackTrace(e);
		}
	}

	// **************** Getters ***************

	public ConnectionFactory getFactory() {
		return this.factory;
	}

	public Connection getConnection() {
		return connection;
	}

	public Channel getChannel() {
		return channel;
	}

	// **************** Utils *****************

	/**
	 * Déclaration d'un nouveau type d'échange (type de message comme le ban d'un joueur)
	 *
	 * @param exchange
	 */
	public void exchangeDeclare(messagesType msg) {
		try {
			getChannel().exchangeDeclare(msg.getName(), type);
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
	}

	public static RabbitMqConnector getInstance() {
		return instance;
	}

	/**
	 * Publication d'un message
	 *
	 * @param msg
	 *            le type du message
	 * @param key
	 *            le ou les endroit qui vont recevoir le message
	 * @param array
	 *            le pkt
	 */
	public void basicPublich(messagesType msg, String key, byte[] array) {
		try {
			getChannel().basicPublish(msg.getName(), routing_enabled ? key : "", null, array);
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
	}

	@Override
	public void sendServer(PacketPhantomServerInfo pkt) {
		if (SPhantom.getInstance().logPkt()) SPhantom.print(">>>>]SEND] PacketServer |to:HUBS_PROXY_SPHANTOM");
		basicPublich(messagesType.Update_Server, destinationKey.HUBS_PROXY_SPHANTOM, pkt.toByteArray());
	}

	// @Override
	// a ajouter dans les spécificitée lors de la création d'un serveur
	// public void sendAgarMode(Jms_AgarMode jms) {
	// basicPublich(messagesType.AgarMode, destinationKey.SRV_AGARES, jms.toJson());
	// basicPublich(messagesType.AgarMode, destinationKey.HUBS_AGARES, jms.toJson());
	// }

	@Override
	public void takeLead(PacketPhantomHeartBeat pkt) {
		basicPublich(messagesType.TakeLead, destinationKey.SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void heartBeat(PacketPhantomHeartBeat pkt) {
		basicPublich(messagesType.HeartBeat, destinationKey.SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void sendPlayer(PacketPhantomPlayer pkt) {
		basicPublich(messagesType.Update_PlayerAction, destinationKey.ALL_SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void bootServer(PacketPhantomBootServer pkt) {
		basicPublich(messagesType.BootServer, destinationKey.HUBS_PROXY_SPHANTOM_SYMBIOTE, pkt.toByteArray());
	}

	@Override
	public void destroyInstance(PacketPhantomDestroyInstance pkt) {
		basicPublich(messagesType.Destroy_Instance, destinationKey.SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void reduceServer(PacketPhantomReduceServer pkt) {
		basicPublich(messagesType.Reduce_Server, destinationKey.ALL_SPHANTOM, pkt.toByteArray());
	}

}
