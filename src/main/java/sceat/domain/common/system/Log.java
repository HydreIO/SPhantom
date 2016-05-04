package sceat.domain.common.system;

import sceat.domain.protocol.packets.PacketPhantom;

public interface Log {
	void logOut(String log);

	void logPkt(PacketPhantom pkt, boolean in);

	void logTrace(Exception e);

	void logTrace(Throwable t);

	public static void out(String log) {
		Root.get().logOut(log);
	}

	public static void packet(PacketPhantom pkt, boolean in) {
		Root.get().logPkt(pkt, in);
	}

	public static void trace(Exception e) {
		Root.get().logTrace(e);
	}

	public static void trace(Throwable t) {
		Root.get().logTrace(t);
	}
}