package sceat.domain.protocol.packets;

import sceat.domain.protocol.MessagesType;

public class PacketPhantomKillProcess extends PacketPhantom {

	@Override
	protected void serialize_() {
		writeString(serverLabel);
		writeString(vpsLabel);
		writeBoolean(removeFolder);
		writeBoolean(extractLogs);
	}

	@Override
	protected void deserialize_() {
		this.serverLabel = readString();
		this.vpsLabel = readString();
		this.removeFolder = readBoolean();
		this.extractLogs = readBoolean();
	}

	@Override
	public void handleData(MessagesType type) {
	}

	private String serverLabel;
	private String vpsLabel;
	private boolean removeFolder;
	private boolean extractLogs;

	public PacketPhantomKillProcess() {
	}

	/**
	 * Notifie le symbiote du crash/timeout d'un serveur pour qu'il puisse kill le process
	 * 
	 * @param label
	 *            the mcServer name
	 * @param vpsLabel
	 *            the vps name
	 * @param removeFolder
	 *            if the symbiote must remove the folder of the server (the folder may be usefull for crash inspection)
	 * @param extractLogs
	 *            if the symbiote must extract the crash logs to ./crashLogs/$(srvLabel)
	 */
	public PacketPhantomKillProcess(String label, String vpsLabel, boolean removeFolder, boolean extractLogs) {
		this.serverLabel = label;
		this.vpsLabel = vpsLabel;
		this.removeFolder = removeFolder;
		this.extractLogs = extractLogs;
	}

	public String getVpsLabel() {
		return vpsLabel;
	}

	public String getServerLabel() {
		return serverLabel;
	}

	public boolean removeFolder() {
		return this.removeFolder;
	}

	public boolean extractLogs() {
		return this.extractLogs;
	}

}
