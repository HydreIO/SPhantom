package sceat.domain.adapter.ssh;

import java.net.InetAddress;

import sceat.domain.minecraft.RessourcePack;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;

public interface Iphantom {

	public Server createServer(ServerType type, int maxPlayers, InetAddress ip, RessourcePack pack, String... destinationKeys);

	public void rebootServer();

	public void stopServer();

	public void destroyServer();

	public Vps[] retrieveOnlineInstances();

}
