package sceat.domain.minecraft;

public enum Grades {

	Fondateur((byte)0, "Fondateur"),
	Admin((byte)1, "Admin"),
	SysAdmin((byte)2, "SysAdmin"),
	Resp((byte)3, "Resp"),
	Moderateur((byte)4, "Moderateur"),
	Developpeur((byte)5, "Developpeur"),
	Architecte((byte)6, "Architecte"),
	Web_Developpeur((byte)7, "Web_Developpeur"),
	Graphiste((byte)8, "Graphiste"),
	Builder((byte)9, "Builder"),
	Helper((byte)10, "Helper"),
	Build_Test((byte)11, "Build_Test"),
	Staff((byte)12, "Staff"),
	Ami((byte)13, "Ami"),
	Partenaire((byte)14, "Partenaire"),
	Youtube((byte)15, "Youtube"),
	Streamer((byte)16, "Streamer"),
	Musclay((byte)17, "Musclay"),
	Lva((byte)18, "Lva"),
	Vip_plus((byte)19, "Vip_plus"),
	Vip((byte)20, "Vip"),
	Joueur((byte)21, "Joueur");

	private byte _perm;
	private String _gName;

	private Grades(byte perm, String name) {
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

	public byte getValue() {
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

	public static Grades fromValue(byte value, boolean canBeNull) {
		for (Grades g : values()) {
			if (g.getValue() == value)
				return g;
		}
		if (canBeNull)
			return null;

		throw new NullPointerException("Le grade avec la valeur" + value + " n'existe pas");
	}
}
