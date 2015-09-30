package sceat.domain.messaging;

import sceat.domain.messaging.dao.Jms_AgarMode;

public interface IMessaging {

	public void sendServer(String json);

	public void sendAgarMode(Jms_AgarMode jms);

}
