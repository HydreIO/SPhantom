package sceat.infra.connector.mq;

import java.io.IOException;
import java.util.List;

import sceat.Main;
import sceat.domain.protocol.handler.PacketHandler;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import fr.aresrpg.sdk.protocol.util.MessagesType;
import fr.aresrpg.sdk.protocol.util.RoutingKey;

public class RabbitMqReceiver {

	private static RabbitMqConnector connector;
	private static String qname;
	private static final List<String> routingKeys = RoutingKey.genKeys();
	private static final RoutingKey current = RoutingKey.SPHANTOM;

	public RabbitMqReceiver() {
		init();
	}

	// initialisation dans RabbitMqConnector
	public void init() {
		connector = RabbitMqConnector.getInstance();
		try {
			qname = getChannel().queueDeclare().getQueue();
			bind();
			startReceiver();
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
	}

	private static RabbitMqConnector getConnector() {
		return connector;
	}

	private static Channel getChannel() {
		return getConnector().getChannel();
	}

	/**
	 * i?i on "bind" un type de message sur une destination
	 *
	 * @param msg
	 *            le type de message
	 * @param key
	 *            la destination
	 */
	private void bind(MessagesType msg) {
		routingKeys.stream().filter(key -> key.contains(current.getKey())).forEach(v -> bind(v, msg.getName()));
	}

	private void bind(String dek, String msg) {
		try {
			getChannel().queueBind(qname, msg, dek);
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
	}

	/**
	 * On s'occupe de bind les message en fonction du serveur actuel
	 */
	private void bind() {
		bind(MessagesType.BOOT_SERVER);
		bind(MessagesType.DESTROY_INSTANCE);
		bind(MessagesType.HEART_BEAT);
		bind(MessagesType.REDUCE_SERVER);
		bind(MessagesType.SYMBIOTE_INFOS);
		bind(MessagesType.TAKE_LEAD);
		bind(MessagesType.UPDATE_PLAYER_ACTION);
		bind(MessagesType.UPDATE_PLAYER_GRADE);
		bind(MessagesType.UPDATE_SERVER);
	}

	/**
	 * Fonction rabbitMq pour recevoir les messages (pour faire simple c'est une callable dans un nouveau thread dont le futur est notre message)
	 *
	 * @throws IOException
	 */
	private static void startReceiver() throws IOException {
		getChannel().basicConsume(qname, true, new DefaultConsumer(getChannel()) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				MessagesType messageType = MessagesType.fromString(envelope.getExchange(), true);
				PacketHandler.getInstance().handle(messageType, body);
			}
		});
	}
}
