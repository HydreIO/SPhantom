package sceat.domain.adapter.mq;

import sceat.domain.protocol.packets.PacketPhantomBootServer;
import sceat.domain.protocol.packets.PacketPhantomDestroyInstance;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomReduceServer;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;

public interface IMessaging {

	public void sendServer(PacketPhantomServerInfo pkt);

	public void takeLead(PacketPhantomHeartBeat pkt);

	public void heartBeat(PacketPhantomHeartBeat pkt);

	public void sendPlayer(PacketPhantomPlayer pkt);

	public void bootServer(PacketPhantomBootServer pkt);

	public void destroyInstance(PacketPhantomDestroyInstance pkt);

	public void reduceServer(PacketPhantomReduceServer pkt);

}
