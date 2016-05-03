package sceat.domain.common;

import sceat.SPhantom;
import sceat.domain.network.server.Vps;

public interface IPhantom {

	int countDeployedInstance();

	void destroyServer(String label);

	Vps deployInstance(String label, int ram);

	boolean exist(String label);

	static IPhantom get() {
		return SPhantom.getInstance().getIphantom();
	}

}