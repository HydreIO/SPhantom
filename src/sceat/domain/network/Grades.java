package sceat.domain.network;

public enum Grades {

	Fondateur(0, "Fondateur"),
	Admin(1, "Admin"),
	Moderateur(2, "Moderateur"),
	Developpeur(3, "Developpeur"),
	Architecte(4, "Architecte"),
	Web_Developpeur(5, "Web_Developpeur"),
	Graphiste(6, "Graphiste"),
	Builder(7, "Builder"),
	Helper(8, "Helper"),
	Build_Test(9, "Build_Test"),
	Staff(10, "Staff"),
	Ami(11, "Ami"),
	Partenaire(12, "Partenaire"),
	Youtube(13, "Youtube"),
	Streamer(14, "Streamer"),
	Musclay(15, "Musclay"),
	Lva(16, "Lva"),
	Vip_plus(17, "Vip_plus"),
	Vip(18, "Vip"),
	Joueur(19, "Joueur");

	private int _perm;
	private String _gName;

	private Grades(int perm, String name) {
		this._perm = perm;
		this._gName = name;
	}

	public boolean isBetterOrSimilarThan(int perm) {
		return this._perm <= perm;
	}

	public boolean isBetterThan(int perm) {
		return this._perm < perm;
	}

	public boolean correspond(String name) {
		return this._gName.equals(name);
	}

	public int getValue() {
		return this._perm;
	}

	public String getName() {
		return this._gName;
	}

	public static Grades fromName(String name, boolean canBeNull) {
		for (Grades g : values()) {
			if (g.correspond(name)) return g;
		}
		if (canBeNull) return null;
		throw new NullPointerException("Le grade " + name + " n'existe pas");
	}

	public static Grades fromValue(int value, boolean canBeNull) {
		for (Grades g : values()) {
			if (g.getValue() == value) return g;
		}
		if (canBeNull) return null;

		throw new NullPointerException("Le grade avec la valeur" + value + " n'existe pas");
	}
}
