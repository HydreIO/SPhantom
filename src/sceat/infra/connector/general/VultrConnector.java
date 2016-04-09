package sceat.infra.connector.general;

import java.util.concurrent.ConcurrentHashMap;

import sceat.SPhantom;
import sceat.domain.adapter.general.Iphantom;
import sceat.domain.network.Core;
import sceat.domain.network.server.Vps;
import sceat.domain.network.server.Vps.VpsState;

public class VultrConnector implements Iphantom {

	@Override
	public Vps deployInstance(String label, int ram) {
		return Vps.fromBoot(label, ram, /* ip recup depuis Jvultr */null);
	}

	@Override
	public void destroyServer(String label) {
		ConcurrentHashMap<String, Vps> vps = Core.getInstance().getVps();
		if (!vps.contains(label)) SPhantom.print("Try destroying vps instance : [" + label + "] /!\\ This instance is not registered in Sphantom or already destroyed /!\\");
		else {
			vps.get(label).setState(VpsState.Destroying);
			// Jvultr.destroyInstance
			vps.remove(label); // remove apres pour que le vps disparaisse une fois réellement detruit (affichage panelweb)
		}
	}

}
