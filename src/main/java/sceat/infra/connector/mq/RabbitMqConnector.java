package sceat.infra.connector.mq;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.common.java.Lambdas;
import sceat.domain.common.mq.Broker;
import sceat.domain.common.system.Config;
import sceat.domain.common.system.Log;
import sceat.domain.common.system.Root;
import sceat.domain.common.thread.ScThread;
import sceat.domain.protocol.MessagesType;
import sceat.domain.protocol.RoutingKey;
import sceat.domain.protocol.packets.PacketPhantomBootServer;
import sceat.domain.protocol.packets.PacketPhantomDestroyInstance;
import sceat.domain.protocol.packets.PacketPhantomGradeUpdate;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
import sceat.domain.protocol.packets.PacketPhantomKillProcess;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomReduceServer;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;
import sceat.domain.utils.Try;
import sceat.domain.utils.Try.TryVoidRunnable;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqConnector implements Broker {

	private static final RabbitMqConnector instance = new RabbitMqConnector();
	private RabbitMqReceiver receiver;
	private ConnectionFactory factory = new ConnectionFactory();
	private static Connection connection;
	private static Channel channel;
	public static final String type = "direct";

	private RabbitMqConnector() {
	}

	public static void init(String user, String pass, boolean local) {
		instance.initt(user, pass, local);
	}

	public RabbitMqReceiver getReceiver() {
		return this.receiver;
	}

	public void initt(String user, String passwd, boolean local) {
		if (local) {
			SPhantom.print("Local mode ! No messaging service.");
			return;
		}
		Lambdas.<ConnectionFactory> emptyConsumer((f) -> {
			f.setHost(Config.get().getRabbitAdress());
			f.setPort(Config.get().getRabbitPort());
			f.setUsername(user);
			f.setPassword(passwd);
		}).accept(getFactory());
		Try.orVoidWithActions(TryVoidRunnable.empty(() -> {
			connection = getFactory().newConnection();
			channel = connection.createChannel();
		}), true, () -> {
			Log.out("Unable to access message broker RMQ, ScorchedRoot is going down..");
			ScThread.sleep(3, TimeUnit.SECONDS); // ultime swag !!!!!!
				Root.exit(false);
			});
		Log.out("Sucessfully connected to broker RMQ");
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
			getChannel().basicPublish(msg.getName(), key, null, array);
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
	}

	private final String SPHANTOM_key = RoutingKey.genKey(RoutingKey.SPHANTOM);
	private final String ALL_SPHANTOM_key = RoutingKey.genKey(RoutingKey.SPHANTOM, RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SERVERS);
	private final String BOOTSERVER_key = RoutingKey.genKey(RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SPHANTOM, RoutingKey.SYMBIOTE);
	private final String SYMBIOTE_key = RoutingKey.genKey(RoutingKey.SYMBIOTE);
	private final String SERVER_INFOS_key = RoutingKey.genKey(RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SPHANTOM);

	@Override
	public void sendServer(PacketPhantomServerInfo pkt) {
		basicPublich(MessagesType.UPDATE_SERVER, this.SERVER_INFOS_key, pkt.toByteArray());
	}

	@Override
	public void takeLead(PacketPhantomHeartBeat pkt) {
		basicPublich(MessagesType.TAKE_LEAD, this.SPHANTOM_key, pkt.toByteArray());
	}

	@Override
	public void heartBeat(PacketPhantomHeartBeat pkt) {
		basicPublich(MessagesType.HEART_BEAT, this.SPHANTOM_key, pkt.toByteArray());
	}

	@Override
	public void sendPlayer(PacketPhantomPlayer pkt) {
		basicPublich(MessagesType.UPDATE_PLAYER_ACTION, this.ALL_SPHANTOM_key, pkt.toByteArray());
	}

	@Override
	public void bootServer(PacketPhantomBootServer pkt) {
		basicPublich(MessagesType.BOOT_SERVER, this.BOOTSERVER_key, pkt.toByteArray());
	}

	@Override
	public void destroyInstance(PacketPhantomDestroyInstance pkt) {
		basicPublich(MessagesType.DESTROY_INSTANCE, this.SPHANTOM_key, pkt.toByteArray());
	}

	@Override
	public void reduceServer(PacketPhantomReduceServer pkt) {
		basicPublich(MessagesType.REDUCE_SERVER, this.ALL_SPHANTOM_key, pkt.toByteArray());
	}

	@Override
	public void killProcess(PacketPhantomKillProcess pkt) {
		basicPublich(MessagesType.KILL_PROCESS, this.SYMBIOTE_key, pkt.toByteArray());
	}

	@Override
	public void gradeUpdate(PacketPhantomGradeUpdate pkt) {
		basicPublich(MessagesType.UPDATE_PLAYER_GRADE, this.ALL_SPHANTOM_key, pkt.toByteArray());
	}

}
