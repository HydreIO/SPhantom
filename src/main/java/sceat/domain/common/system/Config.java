package sceat.domain.common.system;

import sceat.SPhantom;
import sceat.domain.config.SPhantomConfig;

public interface Config {

	public static SPhantomConfig get() {
		return SPhantom.getInstance().getSphantomConfig();
	}

}
