package sceat.domain.common;

import sceat.SPhantom;
import fr.aresrpg.sdk.network.Vps;

public interface IPhantom {

	int countDeployedInstance();

	void destroyServer(String label);

	Vps deployInstance(String label, int ram);

	boolean exist(String label);

	static IPhantom get() {
		return SPhantom.getInstance().getIphantom();
	}

}