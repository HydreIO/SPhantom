package sceat.domain.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;

public class ServerProvider {

	private static ServerProvider instance;
	/**
	 * Map des serveurs mis a jour en fonction de la ram dispo sur les Vps et la ram demandée pour le type du serveur
	 */
	private ConcurrentHashMap<ServerType, Vps> ordered = new ConcurrentHashMap<ServerType, Vps>();

	public ServerProvider() {
		instance = this;
	}

	public static ServerProvider getInstance() {
		return instance;
	}

	public ConcurrentSkipListSet<Vps> getOrdered() {
		return ordered;
	}

	public static Vps provideVps(Set<Vps> vpss) {

	}
}
