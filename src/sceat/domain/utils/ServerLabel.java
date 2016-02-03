package sceat.domain.utils;

import java.util.Random;

import sceat.domain.Manager;
import sceat.domain.network.Server.ServerType;

public class ServerLabel {
	private static Random r = new Random();

	public static String newLabel(ServerType type) {
		String label;
		while (Manager.getInstance().getServersByLabel().containsKey(label = type.name() + "-" + r.nextInt(5000)))
			;
		return label;
	}

}
