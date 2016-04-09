package sceat.infra.connector.general;

import java.net.InetAddress;

import sceat.domain.adapter.general.Iphantom;
import sceat.domain.minecraft.RessourcePack;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;

public class VultrConnector implements Iphantom {

	@Override
	public Server createServer(ServerType type, int maxPlayers, InetAddress ip, RessourcePack pack, String... destinationKeys) {
		Server.
		return null;
	}

	@Override
	public void rebootServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroyServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vps deployInstance(String label, int ram) {
		return Vps.fromBoot(label, ram, /*ip recup depuis Jvultr*/null);
	}

}
