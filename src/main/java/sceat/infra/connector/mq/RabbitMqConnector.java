package sceat.infra.connector.mq;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import sceat.Main;
import sceat.domain.common.system.Config;
import sceat.domain.config.SPhantomConfig;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import fr.aresrpg.commons.concurrent.Threads;
import fr.aresrpg.commons.condition.Try;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomBanned;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomBootServer;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomBroadcast;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomDestroyInstance;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomGradeUpdate;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomHeartBeat;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomKillProcess;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomPlayer;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomReduceServer;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomServerInfo;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomSymbiote;
import fr.aresrpg.sdk.protocol.util.MessagesType;
import fr.aresrpg.sdk.protocol.util.RoutingKey;
import fr.aresrpg.sdk.system.Broker;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.system.Root;

public class RabbitMqConnector implements Broker {

	private static final RabbitMqConnector instance = new RabbitMqConnector();
	private RabbitMqReceiver receiver;
	private ConnectionFactory factory = new ConnectionFactory();
	private static Connection connection;
	private static Channel channel;
	public static final String TYPE = "direct";
	private boolean allowed = false;

	private final String sphantomKey = RoutingKey.genKey(RoutingKey.SPHANTOM);
	private final String allsphantomKey = RoutingKey.genKey(RoutingKey.SPHANTOM, RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SERVERS);
	private final String bootserverKey = RoutingKey.genKey(RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SPHANTOM, RoutingKey.SYMBIOTE);
	private final String symbioteKey = RoutingKey.genKey(RoutingKey.SYMBIOTE);
	private final String serverinfosKey = RoutingKey.genKey(RoutingKey.HUBS, RoutingKey.PROXY, RoutingKey.SPHANTOM);

	private RabbitMqConnector() {
	}

	/**
	 * Empeche Sphantom d'envoyer des packet mise a part HEART_BEAT/Takelead
	 * 
	 * @param pause
	 *            mettre en pause ou reveiller
	 */
	public void pause(boolean pause) {
		allowed = !pause;
	}

	public static void init(boolean local) {
		instance.initt(SPhantomConfig.get().getRabbitUser(), SPhantomConfig.get().getRabbitPassword(), local);
	}

	public RabbitMqReceiver getReceiver() {
		return this.receiver;
	}

	private void setSecurity(PacketPhantom pkt) {
		pkt.setSecu(Root.get().getSecurity());
	}

	public static void setConnection(Connection connection) {
		RabbitMqConnector.connection = connection;
	}

	public static void setChannel(Channel channel) {
		RabbitMqConnector.channel = channel;
	}

	public void initt(String user, String passwd, boolean local) {
		if (local) {
			Log.out("Local mode ! No messaging service.");
			return;
		}
		getFactory().setHost(Config.get().getRabbitAdress());
		getFactory().setPort(Config.get().getRabbitPort());
		getFactory().setUsername(user);
		getFactory().setPassword(passwd);
		Try.test(() -> {
			setConnection(getFactory().newConnection());
			setChannel(connection.createChannel());
		}).catchEx(a -> {
			Log.out("Unable to access message broker RMQ, ScorchedRoot is going down..");
			Log.trace(a);
			Threads.uSleep(3, TimeUnit.SECONDS);
			Root.safeExit();
		});
		Log.out("Sucessfully connected to broker RMQ");
		Arrays.stream(MessagesType.values()).forEach(this::exchangeDeclare); // NOSONAR TRIPLE FDP
		this.receiver = new RabbitMqReceiver();
	}

	/**
	 * Utilisé pour fermer la connection onDisable // A METTRE ONDISABLE()
	 */
	@Override
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

	/**
	 * Tout ce qui n'est pas interne comme le heartbeat doit verifier d'être sur le bon replica avant de pouvoir envoyer le packet, si cet instance de Sphantom est en pause alors on n'envoie rien
	 */
	@Override
	public void sendServer(PacketPhantomServerInfo pkt) {
		setSecurity(pkt);
		if (allowed) {
			Log.packet(pkt, false);
			basicPublich(MessagesType.UPDATE_SERVER, this.serverinfosKey, pkt.serialize().toByteArray());
		}
	}

	@Override
	public void takeLead(PacketPhantomHeartBeat pkt) {
		setSecurity(pkt);
		Log.packet(pkt, false);
		basicPublich(MessagesType.TAKE_LEAD, this.sphantomKey, pkt.serialize().toByteArray());
	}

	@Override
	public void heartBeat(PacketPhantomHeartBeat pkt) {
		setSecurity(pkt);
		Log.packet(pkt, false);
		basicPublich(MessagesType.HEART_BEAT, this.sphantomKey, pkt.serialize().toByteArray());
	}

	@Override
	public void sendPlayer(PacketPhantomPlayer pkt) {
		setSecurity(pkt);
		if (allowed) {
			Log.packet(pkt, false);
			basicPublich(MessagesType.UPDATE_PLAYER_ACTION, this.allsphantomKey, pkt.serialize().toByteArray());
		}
	}

	@Override
	public void bootServer(PacketPhantomBootServer pkt) {
		setSecurity(pkt);
		if (allowed) {
			Log.packet(pkt, false);
			basicPublich(MessagesType.BOOT_SERVER, this.bootserverKey, pkt.serialize().toByteArray());
		}
	}

	/**
	 * Only for get up to date other sphantom instances
	 * 
	 * @param pkt
	 */
	@Override
	public void destroyInstance(PacketPhantomDestroyInstance pkt) {
		setSecurity(pkt);
		if (allowed) {
			Log.packet(pkt, false);
			basicPublich(MessagesType.DESTROY_INSTANCE, this.sphantomKey, pkt.serialize().toByteArray());
		}
	}

	@Override
	public void reduceServer(PacketPhantomReduceServer pkt) {
		setSecurity(pkt);
		if (allowed) {
			Log.packet(pkt, false);
			basicPublich(MessagesType.REDUCE_SERVER, this.allsphantomKey, pkt.serialize().toByteArray());
		}
	}

	/**
	 * when mc serv CGoverhead
	 * 
	 * @param pkt
	 */
	@Override
	public void killProcess(PacketPhantomKillProcess pkt) {
		setSecurity(pkt);
		if (allowed) {
			Log.packet(pkt, false);
			basicPublich(MessagesType.KILL_PROCESS, this.symbioteKey, pkt.serialize().toByteArray());
		}
	}

	@Override
	public void sendGradeUpdate(PacketPhantomGradeUpdate pkt) {
		setSecurity(pkt);
		if (allowed) {
			Log.packet(pkt, false);
			basicPublich(MessagesType.UPDATE_PLAYER_GRADE, this.allsphantomKey, pkt.serialize().toByteArray());
		}
	}

	@Override
	public void banned(PacketPhantomBanned pkt) {
		PacketPhantom.throwCantSend(pkt);
	}

	@Override
	public void broadcast(PacketPhantomBroadcast pkt) {
		PacketPhantom.throwCantSend(pkt);
	}

	@Override
	public void sendSymbioteInfos(PacketPhantomSymbiote pkt) {
		PacketPhantom.throwCantSend(pkt);
	}

}
