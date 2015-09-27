package sceat.infra;

import java.io.IOException;

import sceat.SPhantom;
import sceat.domain.messaging.destinationKey;
import sceat.infra.RabbitMqConnector.messagesType;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
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
			SPhantom.printStackTrace(e);
		}
	}

	private static RabbitMqConnector getConnector() {
		return connector;
	}

	private static Channel getChannel() {
		return getConnector().getChannel();
	}

	/**
	 * Içi on "bind" un type de message sur une destination
	 * 
	 * @param msg
	 *            le type de message
	 * @param key
	 *            la destination
	 */
	private static void bind(messagesType msg) {
		try {
			getChannel().queueBind(qname, msg.getName(), destinationKey.SPHANTOM);
		} catch (IOException e) {
			SPhantom.printStackTrace(e);
		}
	}

	/**
	 * On s'occupe de bind les message en fonction du serveur actuel
	 */
	private static void bind() {
		bind(messagesType.Update_Server);
	}

	/**
	 * Fonction rabbitMq pour recevoir les messages (pour faire simple c'est une callable dans un nouveau thread dont le futur est notre message)
	 * 
	 * @throws IOException
	 */
	private static void startReceiver() throws IOException {
		Consumer consumer = new DefaultConsumer(getChannel()) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				messagesType messageType = messagesType.fromString(envelope.getExchange(), true);

				switch (messageType) {
					case Update_Server:
						SPhantom.getInstance().getManager().receiveServer(message);
						break;
					default:
						break;
				}

			}
		};
		getChannel().basicConsume(qname, true, consumer);
	}
}
