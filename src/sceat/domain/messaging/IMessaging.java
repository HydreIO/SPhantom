package sceat.domain.messaging;

import sceat.domain.messaging.dao.DAO_serverboot;
import sceat.domain.messaging.dao.DAO_serverclose;

public interface IMessaging {

	public void sendServer(String json);

	public void bootServer(DAO_serverboot dao);

	public void closeServer(DAO_serverclose dao);

}
