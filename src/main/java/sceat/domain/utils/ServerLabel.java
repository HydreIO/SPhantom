package sceat.domain.utils;

import java.util.Random;
import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.network.Core;
import fr.aresrpg.sdk.mc.ServerType;

public class ServerLabel {
	private static Random r = new Random();

	private ServerLabel() {
	}

	public static String newLabel(ServerType type) {
		String label;
		while (Manager.getInstance().getServersByLabel().containsKey(label = type.name().toLowerCase() + "-" + r.nextInt(5000)))
			;
		return label;
	}

	public static String newVpsLabel() {
		String label;
		while (Core.getInstance().getVps().containsKey(label = UUID.randomUUID().toString().substring(4, 8) + "_ares_" + r.nextInt(5000)))
			;
		return label;
	}

	public static ServerType getTypeWithLabel(String label) {
		return ServerType.valueOf(label.split("-")[0]); // attention j'utilise aussi cette methode dans AresInternal dans le packetPhantomPlayer
	}

}
