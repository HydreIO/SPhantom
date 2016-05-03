package sceat.domain.common.mq;

import sceat.domain.protocol.packets.PacketPhantomBootServer;
import sceat.domain.protocol.packets.PacketPhantomDestroyInstance;
import sceat.domain.protocol.packets.PacketPhantomGradeUpdate;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
import sceat.domain.protocol.packets.PacketPhantomKillProcess;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomReduceServer;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;

public interface IMessaging {

	void sendServer(PacketPhantomServerInfo pkt);

	void takeLead(PacketPhantomHeartBeat pkt);

	void heartBeat(PacketPhantomHeartBeat pkt);

	void sendPlayer(PacketPhantomPlayer pkt);

	void gradeUpdate(PacketPhantomGradeUpdate pkt);

	void bootServer(PacketPhantomBootServer pkt);

	void destroyInstance(PacketPhantomDestroyInstance pkt);

	void reduceServer(PacketPhantomReduceServer pkt);

	void killProcess(PacketPhantomKillProcess pkt);

}
