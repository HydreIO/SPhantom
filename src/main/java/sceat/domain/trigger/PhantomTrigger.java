package sceat.domain.trigger;

import java.util.HashSet;
import java.util.Set;

import sceat.domain.adapter.api.PhantomApi.VpsApi;
import sceat.domain.network.Core.OperatingMode;
import sceat.domain.network.ServerProvider.Defqon;

public class PhantomTrigger {

	private static PhantomTrigger instance;
	private Set<Trigger> trg = new HashSet<Trigger>();

	public PhantomTrigger() {
		instance = this;
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
