package sceat.infra.connector.mq;

import java.io.IOException;
import java.util.Arrays;

import sceat.Main;
import sceat.domain.protocol.PacketHandler;
import sceat.domain.protocol.destinationKey;
import sceat.domain.protocol.packets.PacketPhantom.PacketNotRegistredException;
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
	 * içi on "bind" un type de message sur une destination
	 *
	 * @param msg
	 *            le type de message
	 * @param key
	 *            la destination
	 */
	private void bind(messagesType msg) {
		bind(destinationKey.ALL_SPHANTOM, msg.getName());
		bind(destinationKey.SPHANTOM, msg.getName());
		bind(destinationKey.HUBS_PROXY_SPHANTOM_SYMBIOTE, msg.getName());
		bind(destinationKey.HUBS_PROXY_SPHANTOM, msg.getName());
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
				messagesType messageType = messagesType.fromString(envelope.getExchange(), true);
				try {
					PacketHandler.getInstance().handle(messageType, body);
				} catch (IllegalAccessException | InstantiationException | PacketNotRegistredException e) {
					Main.printStackTrace(e);
				}
			}
		});
	}
}
