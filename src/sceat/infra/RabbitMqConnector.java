package sceat.infra;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import sceat.SPhantom;
import sceat.domain.messaging.IMessaging;
import sceat.domain.messaging.destinationKey;
import sceat.domain.messaging.dao.DAO_HeartBeat;
import sceat.domain.messaging.dao.Jms_AgarMode;

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

	public RabbitMqConnector(String user, String pass) {
		init(user, pass);
	}

	public RabbitMqReceiver getReceiver() {
		return this.receiver;
	}

	/**
	 * Enum de type de messages
	 * 
	 * @author MrSceat
	 *
	 */
	public static enum messagesType {
		Update_Server("exchange_server"),
		AgarMode("exchange_agarmode"),
		HeartBeat("exchange_heartbeat"),
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
	public void init(String user, String passwd) {
		instance = this;
		getFactory().setHost("94.23.218.25");
		getFactory().setPort(5672);
		getFactory().setUsername(user);
		getFactory().setPassword(passwd);
		try {
			connection = getFactory().newConnection();
			channel = getConnection().createChannel();
		} catch (IOException | TimeoutException e) {
			SPhantom.print("Unable to access message broker RMQ, Sphantom is going down..", true);
			SPhantom.printStackTrace(e);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				SPhantom.printStackTrace(e);
			}
			SPhantom.shutDown();
			return;
		}
		SPhantom.print("Sucessfully connected to broker RMQ");
		exchangeDeclare(messagesType.Update_Server);
		exchangeDeclare(messagesType.AgarMode);
		exchangeDeclare(messagesType.HeartBeat);
		exchangeDeclare(messagesType.TakeLead);

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
			SPhantom.printStackTrace(e);
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
			SPhantom.printStackTrace(e);
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
	 * @param json
	 *            le message
	 */
	public void basicPublich(messagesType msg, String key, String json) {
		try {
			getChannel().basicPublish(msg.getName(), routing_enabled ? key : "", null, json.getBytes());
		} catch (IOException e) {
			SPhantom.printStackTrace(e);
		}
	}

	@Override
	public void sendServer(String json) {
		basicPublich(messagesType.Update_Server, destinationKey.HUBS_AND_PROXY, json);
	}

	@Override
	public void sendAgarMode(Jms_AgarMode jms) {
		basicPublich(messagesType.AgarMode, destinationKey.SRV_AGARES, jms.toJson());
		basicPublich(messagesType.AgarMode, destinationKey.HUBS_AGARES, jms.toJson());
	}

	@Override
	public void takeLead(DAO_HeartBeat json) {
		basicPublich(messagesType.TakeLead, destinationKey.SPHANTOM, json.toJson());
	}

	@Override
	public void heartBeat(DAO_HeartBeat json) {
		basicPublich(messagesType.HeartBeat, destinationKey.SPHANTOM, json.toJson());
	}

}
