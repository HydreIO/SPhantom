package sceat.domain.adapter.general;

import sceat.domain.network.server.Vps;

public interface Iphantom {

	// public Server createServer(ServerType type, int maxPlayers, InetAddress ip, RessourcePack pack, String... destinationKeys);

	public void destroyServer(String label);

	public Vps deployInstance(String label, int ram);

}
