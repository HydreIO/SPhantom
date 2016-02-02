package sceat.domain.network;

public enum Statut {
	BOOTING(0),
	OPEN(1),
	CLOSING(2);
	private int _value;

	private Statut(int value) {
		this._value = value;
	}

	/**
	 * Retourne la valeur (0 pour ouvert, 1 pour fermé, 2 pour reboot, 3 pour maintenance)
	 * 
	 * @return
	 */
	public int getValue() {
		return this._value;
	}

	public static Statut fromValue(int value) {
		for (Statut s : values()) {
			if (s.getValue() == value) return s;
		}
		throw new NullPointerException("Aucun serveur ne possede la valeur : " + value);
	}
}
