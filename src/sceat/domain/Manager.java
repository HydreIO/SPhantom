package sceat.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sceat.SPhantom;
import sceat.domain.Serveur.ServeurType;
import sceat.domain.config.Configuration;
import sceat.domain.forkupdate.ForkUpdateHandler;
import sceat.domain.forkupdate.ForkUpdateListener;
import sceat.domain.forkupdate.ForkUpdateType;
import sceat.domain.forkupdate.IForkUpdade;
import sceat.domain.messaging.dao.DAO;
import sceat.domain.network.Grades;
import sceat.domain.network.Statut;
import sceat.domain.utils.New;
import sceat.domain.utils.UtilGson;
import sceat.domain.windows.NetworkWindow;
import ch.jamiete.mcping.MinecraftPing;
import ch.jamiete.mcping.MinecraftPingOptions;
import ch.jamiete.mcping.MinecraftPingReply;
import ch.jamiete.mcping.MinecraftPingReply.Player;

import com.google.gson.annotations.Expose;

public class Manager implements IForkUpdade {

	public static Statut Network_Status = Statut.OPEN;
	public static int tot_srv = 0;

	private Set<ServerInfo> serversInfos = New.set();

	private Map<String, Serveur> servers = New.map();
	private Set<String> onlineStaff = New.set();
	private Set<String> onlinePlayers = New.set();
	private Map<ServeurType, Serveur[]> serversByType = New.map();

	// -------- Config nombre servers --------

	public static int proxy = 2;
	public static int lobbyMain = 2;
	public static int lobbyAresRpg = 1;
	public static int lobbyAgares = 1;
	public static int lobbyIron = 1;
	public static int build = 1;
	public static int agares = 2;
	public static int aresRpg = 2;
	public static int iron = 1;

	// ---------------------------------------
	@SuppressWarnings("unchecked")
	public Manager() {
		Configuration conf = SPhantom.getInstance().getSphantomConfig().getConfig();
		for (String confs : (List<String>) conf.getList("Servers"))
			serversInfos.add(ServerInfo.fromJson(confs));

		for (ServeurType type : ServeurType.values())
			switch (type) {
				case agares:
					serversByType.put(type, new Serveur[agares]);
					tot_srv += agares;
					break;
				case aresRpg:
					serversByType.put(type, new Serveur[aresRpg]);
					tot_srv += aresRpg;
					break;
				case build:
					serversByType.put(type, new Serveur[build]);
					tot_srv += build;
					break;
				case iron:
					serversByType.put(type, new Serveur[iron]);
					tot_srv += iron;
					break;
				case lobbyAgares:
					serversByType.put(type, new Serveur[lobbyAgares]);
					tot_srv += lobbyAgares;
					break;
				case lobbyAresRpg:
					serversByType.put(type, new Serveur[lobbyAresRpg]);
					tot_srv += lobbyAresRpg;
					break;
				case lobbyIron:
					serversByType.put(type, new Serveur[lobbyIron]);
					tot_srv += lobbyIron;
					break;
				case lobbyMain:
					serversByType.put(type, new Serveur[lobbyMain]);
					tot_srv += lobbyMain;
					break;
				case proxy:
					serversByType.put(type, new Serveur[proxy]);
					tot_srv += proxy;
					break;
				default:
					break;
			}

		for (ServerInfo inf : getServersInfos())
			putServer(Serveur.fromServerInfo(inf));

		startPingWorker();
		ForkUpdateListener.register(this);
	}

	public void putServer(Serveur s) {
		getServersArray(s.getType())[s.getIndex() - 1] = s;
		servers.put(s.getName(), s);
	}

	public Serveur[] getServersArray(ServeurType type) {
		return serversByType.get(type);
	}

	public Collection<Serveur> getServers() {
		return servers.values();
	}

	public void receiveServer(String json) {
		putServer(Serveur.fromJson(json));
	}

	public Serveur getServer(String name) {
		if (servers.containsKey(name)) return servers.get(name);
		return null;
	}

	public Set<ServerInfo> getServersInfos() {
		return serversInfos;
	}

	public Set<String> getOnlinePlayers() {
		return onlinePlayers;
	}

	public Set<String> getOnlineStaff() {
		return onlineStaff;
	}

	private boolean process = false;

	@ForkUpdateHandler(rate = ForkUpdateType.SEC_01)
	public void syncServers() {
		if (process) return;
		process = true;
		for (Serveur s : getServers())
			s.synchronize();
		process = false;
	}

	@ForkUpdateHandler(rate = ForkUpdateType.SEC_01)
	public void log() {
		Set<String> stf = New.set();
		Set<String> onlinepl = New.set();
		for (Serveur s : getServers()) {
			onlinepl.addAll(s.getPlayers());
			Set<String> ssf = New.set();
			for (int i = 0; i < (Grades.values().length - 1); i++) {
				if (s.getPlayersPerGrade()[1] != null) ssf.addAll(s.getPlayersPerGrade()[i]);
			}
			stf.addAll(ssf);
			NetworkWindow wind = SPhantom.getInstance().getTerminal().getWindow();
			if (wind != null) wind.getServersBox().get(s.getName()).syncServerInfos(ssf);
		}
		this.onlineStaff = stf;
		this.onlinePlayers = onlinepl;
		if (SPhantom.getInstance().getTerminal().getWindow() != null) SPhantom.getInstance().getTerminal().getWindow().syncInfos(onlinepl.size(), stf.size());
	}

	public void startPingWorker() {
		SPhantom.print("Starting pingWorker..");
		SPhantom.getInstance().getPinger().execute(new Runnable() {

			@Override
			public void run() {
				SPhantom.print("pingWorker started in new thread !");
				while (SPhantom.getInstance().isRunning()) {
					for (ServerInfo s : serversInfos) {
						if (s == null) continue;

						long ms = System.currentTimeMillis();
						MinecraftPingReply data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname(s.getHost()).setPort(s.getPort()).setTimeout(300));
						Serveur sr = Serveur.fromServerInfo(s);
						sr.ping = (short) (System.currentTimeMillis() - ms);
						if (data == null) {
							sr.ping = -1;
							sr.setStatus(Statut.CLOSED);
						}
						List<String> str = new ArrayList<String>();
						if (data != null && data.getPlayers() != null && data.getPlayers().getSample() != null) for (Player pl : data.getPlayers().getSample())
							str.add(pl.getId());
						sr.setPlayers(str);
						putServer(sr);

					}
					sleep(1000); // Petite pause d'une seconde
				}
			}
		});
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			SPhantom.printStackTrace(e);
		}
	}

	public static class ServerInfo extends DAO {
		@Expose
		private ServeurType type;
		@Expose
		private int index;
		@Expose
		private String host;
		@Expose
		private int port;

		public ServerInfo(ServeurType type, int index, String host, int port) {
			this.type = type;
			this.index = index;
			this.host = host;
			this.port = port;
		}

		public ServeurType getType() {
			return type;
		}

		public String getHost() {
			return host;
		}

		public int getIndex() {
			return index;
		}

		public String toJson() {
			return toJson(this);
		}

		public int getPort() {
			return port;
		}

		public static ServerInfo fromJson(String json) {
			return UtilGson.deserialize(json, ServerInfo.class);
		}

	}

}
