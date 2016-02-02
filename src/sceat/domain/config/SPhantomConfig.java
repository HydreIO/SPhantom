package sceat.domain.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.Manager;
import sceat.domain.server.Server.ServerType;

public class SPhantomConfig {

	private Configuration config;

	public SPhantomConfig() {
		SPhantom.print("Searching SPhantom.yml..");
		File cong = new File(Main.folder.getAbsolutePath() + "/SPhantom.yml");
		boolean mkdir = true;

		try {
			mkdir = cong.exists() ? false : cong.createNewFile();
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(cong);
		} catch (IOException e3) {
			Main.printStackTrace(e3);
		}

		if (mkdir) {
			getConfig().set("Servers",
					Arrays.asList(new Manager.ServerInfo(ServerType.Proxy, 1, "127.0.0.1", 25565).toJson(), new Manager.ServerInfo(ServerType.Proxy, 2, "127.0.0.1", 25566).toJson()));
			saveConfig(getConfig(), cong);
			SPhantom.print("SPhantom.yml just created to " + cong.getAbsolutePath() + " Please configure and reload /!\\");
		} else {
			SPhantom.print("SPhantom.yml found in " + cong.getAbsolutePath() + " !");
		}
	}

	public Configuration getConfig() {
		return config;
	}

	public void loadConfig() {
		try {
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(Main.folder.getAbsolutePath() + "/SPhantom.yml"));
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
		SPhantom.print("Config reloaded");
	}

	public void saveConfig() {
		saveConfig(getConfig(), Main.getFolder().getAbsolutePath() + "/SPhantom.yml");
		SPhantom.print("Config saved");
	}

	protected void saveConfig(Configuration config, File f) {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, f);
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
	}

	protected void saveConfig(Configuration config, String filename) {
		saveConfig(config, new File(filename));
	}

}
