package sceat.domain.adapter.general;

import sceat.domain.network.server.Vps;

public interface IPhantom {

	public int countDeployedInstance();

	public void destroyServer(String label);

	public Vps deployInstance(String label, int ram);

}