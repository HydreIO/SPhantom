package sceat.api;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.minecraft.RessourcePack;
import sceat.domain.minecraft.Statut;
import sceat.domain.network.Core;
import sceat.domain.network.Core.OperatingMode;
import sceat.domain.network.ServerProvider;
import sceat.domain.network.ServerProvider.Defqon;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.network.server.Vps;
import sceat.domain.network.server.Vps.VpsState;

public interface PhantomApi {

	/**
	 * Get all the vps registered in Sphantom
	 * 
	 * @return a {@code Map<String, VpsApi>}
	 */
	public static Map<String, VpsApi> getAllVps() {
		return Core.getInstance().getVps().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * The defense readiness condition (DEFCON) is an alert state used by the United States Armed Forces.
	 * <p>
	 * The DEFQON is a Hardstyle group and the emergency system for deploying new instance in Sphantom !
	 * <p>
	 * 
	 * @return
	 */
	public static Defqon getDefkonLevel() {
		return ServerProvider.getInstance().getDefqon();
	}

	/**
	 * 
	 * @return the number of players on the entire network
	 */
	public static int countAllPlayers() {
		return Manager.getInstance().countPlayersOnNetwork();
	}

	/**
	 * The operating mode define the politic of the overspan, eco mode dont care about lag and love save money, Nolag mode dont care about money and open servers massively
	 * 
	 * @return the mode ECO, NORMAL, NOLAG
	 */
	public static OperatingMode getOpMode() {
		return Core.getInstance().getMode();
	}

	public interface VpsApi {

		/**
		 * Provide api for a vps
		 * 
		 * @param vpsLabel
		 *            name
		 * @return the VpsApi or null if no vps is registered with this label
		 */
		public static VpsApi get(String vpsLabel) {
			return SPhantom.getInstance().getVpsApi(vpsLabel);
		}

		String getLabel();

		/**
		 * When a vps is initialised he got a long value who represent his date of birth
		 * <p>
		 * <blockquote>Pattern :
		 * <p>
		 * {@code LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));}
		 * 
		 * <pre></pre>
		 * </blockquote>
		 * <p>
		 * If the vps is not a dynamic instance, the value will correspond to the instant when Sphantom has booted
		 * 
		 * @return a String representation of the date the server was created
		 */
		String getCreatedInfos();

		/**
		 * When Sphantom start he may have some vps in his config file, so these vps are registered with somes baisc data and wait to be updated by his symbiote !
		 * <p>
		 * at the moment where sphantom will receive a packetSymbiote, the {@link Vps#isUpdated()} value is set to true
		 * 
		 * @return true if the vps is updated
		 */
		boolean isUpdated();

		/**
		 * Sphantom perform a simple reduction on the servers of the vps
		 * <p>
		 * if a server has the state {@link Statut#REDUCTION} or {@link Statut#CLOSING} he will be excluded from the operations
		 * 
		 * @return the ram in GO available on the {@code Vps}
		 */
		int getAvailableRam();

		/**
		 * 
		 * @return the state of the server
		 */
		VpsState getState();

		/**
		 * 
		 * @return the ip
		 */
		InetAddress getIp();

		/**
		 * 
		 * @return all the servers is the vps
		 */
		Set<ServerApi> getAllServers();

	}

	public interface ServerApi {

		/**
		 * Provide api for a server
		 * 
		 * @param srvLabel
		 *            name
		 * @return the ServerApi or null if no server has this label
		 */
		public static ServerApi get(String srvLabel) {
			return SPhantom.getInstance().getServerApi(srvLabel);
		}

		/**
		 * 
		 * @return the label
		 */
		String getLabel();

		/**
		 * 
		 * @return The name of the current vps
		 */
		String getVpsLabel();

		/**
		 * 
		 * @return the number of players on the server
		 */
		int countPlayers();

		/**
		 * 
		 * @return The ressourcePack enum, not used yet
		 */
		RessourcePack getPack();

		/**
		 * 
		 * @return The state of the server
		 */
		Statut getStatus();

		/**
		 * 
		 * @return maximum players accepted on the server
		 */
		int getMaxPlayers();

		/**
		 * 
		 * @return the type of the server
		 */
		ServerType getType();

		/**
		 * 
		 * @return ip of the server
		 */
		InetAddress getIpadress();

		/**
		 * A string representation of the last time Sphantom has received a heartbeat from the server <blockquote>
		 * 
		 * <pre>
		 * format : 15/07 12:35
		 * </pre>
		 * 
		 * </blockquote>
		 * 
		 * @return the last timeout
		 */
		String getLastTimeout();

		/**
		 * 
		 * @return all players
		 */
		Set<UUID> getPlayers();

	}
}
