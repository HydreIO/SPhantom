package sceat.infra.connector.mq;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.common.mq.Broker;
import sceat.domain.common.system.Config;
import sceat.domain.protocol.packets.PacketPhantomBootServer;
import sceat.domain.protocol.packets.PacketPhantomDestroyInstance;
import sceat.domain.protocol.packets.PacketPhantomGradeUpdate;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
import sceat.domain.protocol.packets.PacketPhantomKillProcess;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomReduceServer;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import fr.aresrpg.commons.concurrent.Threads;
import fr.aresrpg.commons.condition.Try;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.RoutingKey;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.system.Root;

public class RabbitMqConnector implements Broker {

	private static final RabbitMqConnector instance = new RabbitMqConnector();
	private RabbitMqReceiver receiver;
	private ConnectionFactory factory = new ConnectionFactory();
	private static Connection connection;
	private static Channel channel;
	public static final String TYPE = "direct";

	private final String sphantomKey = RoutingKey.genKey(RoutingKey.SPHANTOM);
	private final String allsphantomKey = RoutingKey.genKey(RoutingKey.SPHANTOM, RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SERVERS);
	private final String bootserverKey = RoutingKey.genKey(RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SPHANTOM, RoutingKey.SYMBIOTE);
	private final String symbioteKey = RoutingKey.genKey(RoutingKey.SYMBIOTE);
	private final String serverinfosKey = RoutingKey.genKey(RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SPHANTOM);

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
		getFactory().setHost(Config.get().getRabbitAdress());
		getFactory().setPort(Config.get().getRabbitPort());
		getFactory().setUsername(user);
		getFactory().setPassword(passwd);
		Try.test(() -> {
			connection = getFactory().newConnection();
			channel = connection.createChannel();
		}).catchEx((a) -> {
			Log.out("Unable to access message broker RMQ, ScorchedRoot is going down..");
			Log.trace(a);
			Threads.uSleep(3, TimeUnit.SECONDS);
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
		Try.test(() -> {
			getChannel().close();
			getConnection().close();
		}).catchEx(Log::trace);
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
			getChannel().exchangeDeclare(msg.getName(), TYPE);
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

	@Override
	public void sendServer(PacketPhantomServerInfo pkt) {
		basicPublich(MessagesType.UPDATE_SERVER, this.serverinfosKey, pkt.toByteArray());
	}

	@Override
	public void takeLead(PacketPhantomHeartBeat pkt) {
		basicPublich(MessagesType.TAKE_LEAD, this.sphantomKey, pkt.toByteArray());
	}

	@Override
	public void heartBeat(PacketPhantomHeartBeat pkt) {
		basicPublich(MessagesType.HEART_BEAT, this.sphantomKey, pkt.toByteArray());
	}

	@Override
	public void sendPlayer(PacketPhantomPlayer pkt) {
		basicPublich(MessagesType.UPDATE_PLAYER_ACTION, this.allsphantomKey, pkt.toByteArray());
	}

	@Override
	public void bootServer(PacketPhantomBootServer pkt) {
		basicPublich(MessagesType.BOOT_SERVER, this.bootserverKey, pkt.toByteArray());
	}

	@Override
	public void destroyInstance(PacketPhantomDestroyInstance pkt) {
		basicPublich(MessagesType.DESTROY_INSTANCE, this.sphantomKey, pkt.toByteArray());
	}

	@Override
	public void reduceServer(PacketPhantomReduceServer pkt) {
		basicPublich(MessagesType.REDUCE_SERVER, this.allsphantomKey, pkt.toByteArray());
	}

	@Override
	public void killProcess(PacketPhantomKillProcess pkt) {
		basicPublich(MessagesType.KILL_PROCESS, this.symbioteKey, pkt.toByteArray());
	}

	@Override
	public void gradeUpdate(PacketPhantomGradeUpdate pkt) {
		basicPublich(MessagesType.UPDATE_PLAYER_GRADE, this.allsphantomKey, pkt.toByteArray());
	}

}
