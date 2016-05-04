package sceat.domain.common.system;

import sceat.domain.config.SPhantomConfig;

public interface Config {

	public static SPhantomConfig get() {
		return Root.get().getSphantomConfig();
	}

}
