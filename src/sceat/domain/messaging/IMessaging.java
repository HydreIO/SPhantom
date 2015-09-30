package sceat.domain.messaging;

import sceat.domain.messaging.dao.DAO_HeartBeat;
import sceat.domain.messaging.dao.Jms_AgarMode;

public interface IMessaging {

	public void sendServer(String json);

	public void sendAgarMode(Jms_AgarMode jms);

	public void takeLead(DAO_HeartBeat json);

	public void heartBeat(DAO_HeartBeat json);

}
