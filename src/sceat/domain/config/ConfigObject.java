package sceat.domain.config;

import java.util.List;
import java.util.Map;

import sceat.domain.network.Server.ServerType;

public class ConfigObject {

	private String RabbitUser;
	private String RabbitPassword;
	private List<VpsConfigObject> servers;
	private Map<ServerType, >
	

	/**
	 * Repr�sente la configuration d'un serveur d�di� ou d'un vps
	 * 
	 * @author MrSceat
	 *
	 */
	public static class VpsConfigObject {

		private String ip;
		private List<PortRangeObject> portRangeList;

	}

	public static class PortRangeObject {
		
		private int portMin;
		private int portMax;
	}

	public static class McServerConfigObject {

		private int maxPlayers;
		private int playersBeforeOpenNewInstance;
		

	}

}
