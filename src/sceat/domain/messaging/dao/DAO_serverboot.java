package sceat.domain.messaging.dao;

import sceat.domain.server.Serveur;

public class DAO_serverboot extends DAO {

	private Serveur bootingServer;
	private String rootingkey;

	public DAO_serverboot(Serveur toboot) {
		this.bootingServer = toboot;
		this.rootingkey = toboot.getIpadress();
	}

	public String getRootingKey() {
		return rootingkey;
	}

	public Serveur getBootingServer() {
		return bootingServer;
	}

}
