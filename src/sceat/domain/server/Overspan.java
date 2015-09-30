package sceat.domain.server;

import sceat.domain.server.Serveur.ServeurType;
import sceat.domain.shell.ShellExecuter;

public class Overspan {

	public static void bootServer(ServeurType type, int index) {
		ShellExecuter.OVH_1.runScript("cd /home/minecraft && ./Server " + (type.name() + index) + " start");
	}

	public static void bootServer(String name) {
		ShellExecuter.OVH_1.runScript("cd /home/minecraft && ./Server " + name + " start");
	}

}
