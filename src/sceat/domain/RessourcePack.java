package sceat.domain;

public enum RessourcePack {

	RESSOURCE_PACK_DEFAULT(""),
	ARESRPG("http://aresrpg.fr/download/AresAddon6.zip"),
	AGARES(""),
	IRON("");

	private String _url;

	private RessourcePack(String url) {
		this._url = url;
	}

	public String getUrl() {
		return this._url;
	}
}
