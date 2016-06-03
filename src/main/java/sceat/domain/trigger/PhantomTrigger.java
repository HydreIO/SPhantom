package sceat.domain.trigger;

import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.commons.util.collection.Set;
import fr.aresrpg.sdk.phantom.PhantomApi.VpsApi;
import fr.aresrpg.sdk.util.Defqon;
import fr.aresrpg.sdk.util.OperatingMode;

public class PhantomTrigger {

	private static PhantomTrigger instance = new PhantomTrigger();
	private Set<Trigger> trg = new HashSet<>();

	private PhantomTrigger() {
	}

	public static void init() {
		// init instance
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
