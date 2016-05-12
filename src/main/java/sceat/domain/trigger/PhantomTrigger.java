package sceat.domain.trigger;

import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.commons.util.collection.Set;

import sceat.api.PhantomApi.VpsApi;
import sceat.domain.network.Core.OperatingMode;
import sceat.domain.network.ServerProvider.Defqon;

public class PhantomTrigger {

	private static PhantomTrigger instance = new PhantomTrigger();
	private Set<Trigger> trg = new HashSet<Trigger>();

	private PhantomTrigger() {
	}

	public static void init() {
	}

	public static Set<Trigger> getAll() {
		return instance.trg;
	}

	public interface Trigger {

		void handleDefcon(Defqon d);

		void handleOpMode(OperatingMode o);

		void handleVps(VpsApi a);

		public static Trigger n3w(Trigger t) {
			instance.trg.add(t);
			return t;
		}

	}
}
