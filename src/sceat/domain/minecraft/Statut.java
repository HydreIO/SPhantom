package sceat.domain.minecraft;

public enum Statut {
	CREATING(0),
	BOOTING(1),
	OPEN(2),
	REBOOTING(3),
	CLOSING(4);
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
