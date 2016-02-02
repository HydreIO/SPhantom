package sceat.domain.protocol;

import sceat.domain.protocol.dao.DAO_HeartBeat;

public interface IMessaging {

	public void sendServer(String json);

	public void takeLead(DAO_HeartBeat json);

	public void heartBeat(DAO_HeartBeat json);

}