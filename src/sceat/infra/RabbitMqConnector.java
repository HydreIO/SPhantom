package sceat.infra;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import sceat.domain.messaging.IMessaging;
import sceat.domain.messaging.destinationKey;

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

	public RabbitMqConnector() {
		init();
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
		Boot_Server("exchange_server_boot"),
		Close_Server("exchange_server_close"),
		Update_Server("exchange_server");

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
	public void init() {
		instance = this;
		getFactory().setHost("94.23.218.25");
		getFactory().setPort(5672);
		getFactory().setUsername("sceat");
		getFactory().setPassword("3ffZ37a6F3srgMc58fE");
		try {
			connection = getFactory().newConnection();
			channel = getConnection().createChannel();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
		exchangeDeclare(messagesType.Update_Server);

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
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}

	@Override
	public void sendServer(String json) {
		basicPublich(messagesType.Update_Server, destinationKey.HUBS_AND_PROXY, json);
	}

}
