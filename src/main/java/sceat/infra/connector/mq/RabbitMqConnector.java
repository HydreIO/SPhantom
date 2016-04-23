package sceat.infra.connector.mq;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.adapter.mq.IMessaging;
import sceat.domain.protocol.DestinationKey;
import sceat.domain.protocol.packets.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqConnector implements IMessaging {

	private static RabbitMqConnector instance;
	private RabbitMqReceiver receiver;

	public static boolean routingEnabled = true;

	private ConnectionFactory factory = new ConnectionFactory();
	private static Connection connection;

	private static Channel channel;

	public static final String type = routingEnabled ? "direct" : "fanout";

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
	public enum MessagesType {
		UPDATE_SERVER("exchange_server" , false , 1),
		UPDATE_PLAYER_ACTION("exchange_playerAction", false, 1),
		UPDATE_PLAYER_GRADE("exchange_playerGrade", false, 1),
		HEART_BEAT("exchange_heartbeat", false, 1),
		BOOT_SERVER("exchange_symbiote_bootServer", false , 1),
		SYMBIOTE_INFOS("exchange_symbiote", false, 1),
		REDUCE_SERVER("exchange_reduceServer", false, 1),
		DESTROY_INSTANCE("exchange_destroyVps", false, 1),
		TAKE_LEAD("exchange_takelead", false, 1);

		private final boolean canBeDropped;
		private String exchangeName;
		private int priority;

		MessagesType(String name , boolean canBeDropped , int priority) {
			this.exchangeName = name;
			this.canBeDropped = canBeDropped;
			this.priority = priority;
		}

		public String getName() {
			return exchangeName;
		}

		public boolean canBeDropped() {
			return canBeDropped;
		}

		public static MessagesType fromString(String exchangeN, boolean notNull) {
			for (MessagesType e : values())
				if (e.getName().equals(exchangeN))
					return e;
			if (notNull)
				throw new NullPointerException("Aucun type de message n'a pour valeur " + exchangeN);
			return null;
		}

		public int getPriority() {
			return priority;
		}
	}

	/**
	 * Initialisation de la connection et du channel, ainsi que d�claration des messages json a envoyer (par leur nom : banP etc) On initialise aussi les receiver (une fois le channel cr��)
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
		Arrays.stream(MessagesType.values()).forEach(this::exchangeDeclare);
		this.receiver = new RabbitMqReceiver();
	}

	/**
	 * Utilis� pour fermer la connection onDisable // A METTRE ONDISABLE()
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
	 * D�claration d'un nouveau type d'�change (type de message comme le ban d'un joueur)
	 *
	 * @param exchange
	 */
	public void exchangeDeclare(MessagesType msg) {
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
	public void basicPublich(MessagesType msg, String key, byte[] array) {
		try {
			getChannel().basicPublish(msg.getName() , routingEnabled ? key : "", null, array);
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
	}

	@Override
	public void sendServer(PacketPhantomServerInfo pkt) {
		if (SPhantom.getInstance().logPkt()) SPhantom.print(">>>>]SEND] PacketServer |to:HUBS_PROXY_SPHANTOM");
		basicPublich(MessagesType.UPDATE_SERVER, DestinationKey.HUBS_PROXY_SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void takeLead(PacketPhantomHeartBeat pkt) {
		basicPublich(MessagesType.TAKE_LEAD, DestinationKey.SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void heartBeat(PacketPhantomHeartBeat pkt) {
		basicPublich(MessagesType.HEART_BEAT, DestinationKey.SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void sendPlayer(PacketPhantomPlayer pkt) {
		basicPublich(MessagesType.UPDATE_PLAYER_ACTION, DestinationKey.ALL_SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void bootServer(PacketPhantomBootServer pkt) {
		basicPublich(MessagesType.BOOT_SERVER, DestinationKey.HUBS_PROXY_SPHANTOM_SYMBIOTE, pkt.toByteArray());
	}

	@Override
	public void destroyInstance(PacketPhantomDestroyInstance pkt) {
		basicPublich(MessagesType.DESTROY_INSTANCE, DestinationKey.SPHANTOM, pkt.toByteArray());
	}

	@Override
	public void reduceServer(PacketPhantomReduceServer pkt) {
		basicPublich(MessagesType.REDUCE_SERVER, DestinationKey.ALL_SPHANTOM, pkt.toByteArray());
	}

}
