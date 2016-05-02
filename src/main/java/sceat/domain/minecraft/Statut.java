package sceat.domain.minecraft;

public enum Statut {
	CREATING((byte) 0),
	BOOTING((byte) 1),
	OPEN((byte) 2),
	REDUCTION((byte) 3),
	REBOOTING((byte) 4),
	CLOSING((byte) 5),
	CRASHED((byte) 6),
	OVERHEAD((byte) 7);
	private byte id;

	private Statut(byte value) {
		this.id = value;
	}

	/**
	 * Retourne l'id (0 pour ouvert, 1 pour fermé, 2 pour reboot, 3 pour maintenance)
	 * 
	 * @return l'id
	 */
	public int getValue() {
		return this.id;
	}

	public static Statut fromValue(byte value) {
		for (Statut s : values()) {
			if (s.getValue() == value) return s;
		}
		throw new NullPointerException("Aucun serveur ne possede la valeur : " + value);
	}
}
