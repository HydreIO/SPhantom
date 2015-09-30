package sceat.domain.serverTypes;

import sceat.domain.Serveur;

public class ServeurAgares extends Serveur {

	private AgarMode agarmode;

	public static enum AgarMode {
		Null,
		Normal,
		Hardcore
	}

	public ServeurAgares(ServeurType type, int index) {
		setAgarMode(AgarMode.Null);
	}

	public AgarMode getAgarMode() {
		return agarmode;
	}

	public void setAgarMode(AgarMode agarmode) {
		this.agarmode = agarmode;
	}
}
