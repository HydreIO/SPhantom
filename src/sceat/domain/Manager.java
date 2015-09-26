package sceat.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import sceat.SPhantom;
import sceat.domain.Serveur.ServeurType;
import sceat.domain.forkupdate.ForkUpdateHandler;
import sceat.domain.forkupdate.ForkUpdateType;
import sceat.domain.forkupdate.IForkUpdade;
import sceat.domain.network.Statut;
import sceat.domain.utils.New;

public class Manager implements IForkUpdade {

	public static Statut Network_Status = Statut.OPEN;
	public static int tot_srv = 0;

	private Map<String, Serveur> servers = New.map();
	private Set<String> onlineStaff = New.set();
	private Map<ServeurType, Serveur[]> serversByType = New.map();

	public Manager() {
		for (ServeurType type : ServeurType.values())
			switch (type) {
				case agares:
					serversByType.put(type, new Serveur[2]);
					tot_srv += 2;
					break;
				case aresRpg:
					serversByType.put(type, new Serveur[2]);
					tot_srv += 2;
					break;
				case build:
					serversByType.put(type, new Serveur[1]);
					tot_srv += 1;
					break;
				case iron:
					serversByType.put(type, new Serveur[1]);
					tot_srv += 1;
					break;
				case lobbyAgares:
					serversByType.put(type, new Serveur[1]);
					tot_srv += 1;
					break;
				case lobbyAresRpg:
					serversByType.put(type, new Serveur[1]);
					tot_srv += 1;
					break;
				case lobbyIron:
					serversByType.put(type, new Serveur[1]);
					tot_srv += 1;
					break;
				case lobbyMain:
					serversByType.put(type, new Serveur[2]);
					tot_srv += 2;
					break;
				case proxy:
					serversByType.put(type, new Serveur[2]);
					tot_srv += 2;
					break;
				default:
					break;
			}
	}

	public Serveur[] getServersArray(ServeurType type) {
		return serversByType.get(type);
	}

	public Collection<Serveur> getServers() {
		return servers.values();
	}

	public void receiveServer(String json) {
		Serveur s = Serveur.fromJson(json);
		servers.put(s.getName(), s);
	}

	public Serveur getServer(String name) {
		if (servers.containsKey(name)) return servers.get(name);
		return null;
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

	public void print(String txt) {
		SPhantom.print(txt);
	}

	@ForkUpdateHandler(rate = ForkUpdateType.SEC_01)
	public void log() {
		Set<String> stf = New.set();
		for (Serveur s : getServers()) {
			for (int i = 0; i < s.getPlayersPerGrade().length - 1; i++) {
				stf.addAll(s.getPlayersPerGrade()[i]);
			}
		}
	}
}
