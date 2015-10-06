package sceat.domain.server;

import sceat.SPhantom;
import sceat.domain.forkupdate.ForkUpdateHandler;
import sceat.domain.forkupdate.ForkUpdateListener;
import sceat.domain.forkupdate.ForkUpdateType;
import sceat.domain.forkupdate.IForkUpdade;
import sceat.domain.messaging.dao.Jms_AgarMode;
import sceat.domain.server.Serveur.ServeurType;
import sceat.domain.server.serverTypes.ServeurAgares.AgarMode;
import sceat.domain.shell.ShellExecuter;

/**
 * NOT IMPLEMENTED YET
 * 
 * @author MrSceat
 *
 */
public class Overspan implements IForkUpdade {

	public static void bootServer(ServeurType type, int index) {
		ShellExecuter.OVH_1.runScript("cd /home/minecraft && ./Server " + (type.name() + index) + " start");
	}

	public static void bootServer(String name) {
		ShellExecuter.OVH_1.runScript("cd /home/minecraft && ./Server " + name + " start");
	}

	public Overspan() {
		ForkUpdateListener.register(this);
	}

	@ForkUpdateHandler(rate = ForkUpdateType.SEC_05)
	public void temporaryForceUpdate() {
		SPhantom.getInstance().getMessageBroker().sendAgarMode(new Jms_AgarMode(1, AgarMode.Normal));
		SPhantom.getInstance().getMessageBroker().sendAgarMode(new Jms_AgarMode(2, AgarMode.Hardcore));
	}

}
