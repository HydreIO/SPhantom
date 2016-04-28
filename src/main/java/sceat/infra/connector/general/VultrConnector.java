package sceat.infra.connector.general;

import java.util.concurrent.ConcurrentHashMap;

import sceat.SPhantom;
import sceat.domain.adapter.general.Iphantom;
import sceat.domain.network.Core;
import sceat.domain.network.server.Vps;
import sceat.domain.network.server.Vps.VpsState;

public class VultrConnector implements Iphantom {

	// vultr peut refuser de destroy une instance si elle a été créé ya pas longtemps, faut donc foutre une liste pour add le vps a destroy et retester tout les X temps puis le virer (en laissant le vpsState sur destroying)

	@Override
	public Vps deployInstance(String label, int ram) {
		return Vps.fromBoot(label, ram, /* ip recup depuis Jvultr */null);
	}

	@Override
	public void destroyServer(String label) {
		ConcurrentHashMap<String, Vps> vps = Core.getInstance().getVps();
		if (!vps.contains(label)) SPhantom.print("Try destroying vps instance : [" + label + "] /!\\ This instance is not registered in Sphantom or already destroyed /!\\");
		else {
			if (SPhantom.logDiv()) SPhantom.print("Destroying instance : " + label);
			Vps vp = vps.get(label).setState(VpsState.Destroying);
			// Jvultr.destroyInstance
			SPhantom.getInstance().getExecutor().execute(() -> {
				try {
					Thread.sleep(6000); // on attend un peu que le vps soit bien destroy
				} catch (Exception e) {
					e.printStackTrace();
				}
				vp.unregister();
			});
		}
	}

	@Override
	public int countDeployedInstance() {
		return 0;
	}

}
