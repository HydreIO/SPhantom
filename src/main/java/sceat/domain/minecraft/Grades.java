package sceat.domain.minecraft;

public enum Grades {

	FONDATEUR((byte)0, "Fondateur"),
	ADMIN((byte)1, "Admin"),
	SYS_ADMIN((byte)2, "SysAdmin"),
	RESP((byte)3, "Resp"),
	MODERATEUR((byte)4, "Moderateur"),
	DEVELOPPEUR((byte)5, "Developpeur"),
	ARCHITECTE((byte)6, "Architecte"),
	WEB_DEVELOPPEUR((byte)7, "Web_Developpeur"),
	GRAPHISTE((byte)8, "Graphiste"),
	BUILDER((byte)9, "Builder"),
	HELPER((byte)10, "Helper"),
	BUILD_TEST((byte)11, "Build_Test"),
	STAFF((byte)12, "Staff"),
	AMI((byte)13, "Ami"),
	PARTENAIRE((byte)14, "Partenaire"),
	YOUTUBE((byte)15, "Youtube"),
	STREAMER((byte)16, "Streamer"),
	MUSCLAY((byte)17, "Musclay"),
	LVA((byte)18, "Lva"),
	VIP_PLUS((byte)19, "Vip_plus"),
	VIP((byte)20, "Vip"),
	JOUEUR((byte)21, "Joueur");

	private byte id;
	private String name;

	private Grades(byte id, String name) {
		this.id = id;
		this.name = name;
	}

	public boolean isBetterOrSimilarThan(int perm) {
		return this.id <= perm;
	}

	public boolean isBetterOrSimilarThan(Grades gr) {
		return isBetterOrSimilarThan(gr.getValue());
	}

	public boolean isBetterThan(int perm) {
		return this.id < perm;
	}

	public boolean correspond(String name) {
		return this.name.equals(name);
	}

	public byte getValue() {
		return this.id;
	}

	public String getName() {
		return this.name;
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
