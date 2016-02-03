package sceat.domain.minecraft;

public enum Grades {

	Fondateur(0, "Fondateur"),
	Admin(1, "Admin"),
	SysAdmin(2, "SysAdmin"),
	Resp(3, "Resp"),
	Moderateur(4, "Moderateur"),
	Developpeur(5, "Developpeur"),
	Architecte(6, "Architecte"),
	Web_Developpeur(7, "Web_Developpeur"),
	Graphiste(8, "Graphiste"),
	Builder(9, "Builder"),
	Helper(10, "Helper"),
	Build_Test(11, "Build_Test"),
	Staff(12, "Staff"),
	Ami(13, "Ami"),
	Partenaire(14, "Partenaire"),
	Youtube(15, "Youtube"),
	Streamer(16, "Streamer"),
	Musclay(17, "Musclay"),
	Lva(18, "Lva"),
	Vip_plus(19, "Vip_plus"),
	Vip(20, "Vip"),
	Joueur(21, "Joueur");

	private int _perm;
	private String _gName;

	private Grades(int perm, String name) {
		this._perm = perm;
		this._gName = name;
	}

	public boolean isBetterOrSimilarThan(int perm) {
		return this._perm <= perm;
	}

	public boolean isBetterOrSimilarThan(Grades gr) {
		return isBetterOrSimilarThan(gr.getValue());
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
