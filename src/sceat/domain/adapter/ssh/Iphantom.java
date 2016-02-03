package sceat.domain.adapter.ssh;

import java.net.InetAddress;

import sceat.domain.minecraft.RessourcePack;
import sceat.domain.network.Server;
import sceat.domain.network.Server.ServerType;

public interface Iphantom {

	public Server createServer(ServerType type, int maxPlayers, InetAddress ip, RessourcePack pack, String... destinationKeys);

	public void rebootServer();

	public void stopServer();

	public void destroyServer();

}
