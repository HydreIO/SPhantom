package sceat.infra.connector.mq;

import java.io.IOException;
import java.util.Arrays;

import sceat.Main;
import sceat.domain.protocol.PacketHandler;
import sceat.domain.protocol.destinationKey;
import sceat.infra.connector.mq.RabbitMqConnector.messagesType;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMqReceiver {

	private static RabbitMqConnector connector;
	private static String qname;

	public RabbitMqReceiver() {
		init();
	}

	// initialisation dans RabbitMqConnector
	public void init() {
		connector = RabbitMqConnector.getInstance();
		try {
			qname = getChannel().queueDeclare().getQueue();
			if (RabbitMqConnector.routing_enabled) bind();
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
	 * I�i on "bind" un type de message sur une destination
	 *
	 * @param msg
	 *            le type de message
	 * @param key
	 *            la destination
	 */
	private void bind(messagesType msg) {
		try {
			getChannel().queueBind(qname, msg.getName(), destinationKey.SPHANTOM);
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
	}

	/**
	 * On s'occupe de bind les message en fonction du serveur actuel
	 */
	private void bind() {
		Arrays.stream(messagesType.values()).forEach(this::bind);
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
				String message = new String(body, "UTF-8");
				messagesType messageType = messagesType.fromString(envelope.getExchange(), true);
				PacketHandler.getInstance().handle(messageType, message);
			}
		});
	}
}
