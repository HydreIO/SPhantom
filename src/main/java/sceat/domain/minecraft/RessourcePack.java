package sceat.domain.minecraft;

public enum RessourcePack {

	RESSOURCE_PACK_DEFAULT(""),
	ARESRPG("http://aresrpg.fr/download/AresAddon6.zip"),
	AGARES(""),
	IRON("");

	private String url;

	private RessourcePack(String url) {
		this.url = url;
	}

	public String getUrl() {
		return this.url;
	}
}
