package sceat.domain.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.network.Server;
import sceat.domain.network.Server.ServerType;
import sceat.domain.ressources.Constant;

public class SPhantomConfig {

	private Configuration config;
	private String RabbitUser = "user";
	private String RabbitPassword = "pass";
	private String VultrKey = "key";
	private String VultrUser = "user";
	private String VultrPass = "pass";
	private List<VpsConfigObject> servers = new ArrayList<SPhantomConfig.VpsConfigObject>();
	private Map<ServerType, McServerConfigObject> instances = new HashMap<Server.ServerType, SPhantomConfig.McServerConfigObject>();

	public static boolean isReloading = false;

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
			this.servers.add(new VpsConfigObject("Ovh_001", "127.0.0.1", "user", "pass", 25565, true));
			this.servers.add(new VpsConfigObject("Vultr_001", "127.0.0.2", "user", "pass", 25566, false));
			Arrays.stream(ServerType.values()).forEach(v -> this.instances.put(v, new McServerConfigObject(75, 50, 2)));
			getConfig().set("Broker.User", "user");
			getConfig().set("Broker.Pass", "pass");
			getConfig().set(Constant.CONFIG_Optional_var + ".Vultr_User", getVultrUser());
			getConfig().set(Constant.CONFIG_Optional_var + ".Vultr_Pass", getVultrPass());
			getConfig().set(Constant.CONFIG_Optional_var + ".Vultr_Api_Key", getVultrKey());
			getServers().forEach(s -> s.write(getConfig()));
			getInstances().forEach((k, v) -> v.setType(k).write(getConfig()));
			saveConfig(getConfig(), cong);
			SPhantom.print("SPhantom.yml just created to " + cong.getAbsolutePath() + " Please configure and reload /!\\");
		} else {
			SPhantom.print("SPhantom.yml found in " + cong.getAbsolutePath() + " !");
		}
		load(false);
	}

	public String getVultrUser() {
		return VultrUser;
	}

	public String getVultrPass() {
		return VultrPass;
	}

	public static boolean isReloading() {
		return isReloading;
	}

	public void load(boolean async) {
		Runnable r = () -> {
			SPhantom.print("Loading configuration.. please wait !");
			long cur = System.currentTimeMillis();
			loadConfig();
			SPhantom.print("Applying changes...");
			isReloading = true;
			this.RabbitUser = getConfig().getString("Broker.User");
			SPhantom.print("Broker_user [ok]");
			this.RabbitPassword = getConfig().getString("Broker.Pass");
			SPhantom.print("Broker_pass [ok]");
			this.VultrUser = getConfig().getString(Constant.CONFIG_Optional_var + ".Vultr_User");
			SPhantom.print("Vultr_User [ok]");
			this.VultrPass = getConfig().getString(Constant.CONFIG_Optional_var + ".Vultr_Pass");
			SPhantom.print("Vultr_Pass [ok]");
			this.VultrKey = getConfig().getString(Constant.CONFIG_Optional_var + ".Vultr_Api_Key");
			SPhantom.print("Vultr_key [ok]");
			SPhantom.print("Clearing servers config..");
			this.servers.clear();
			SPhantom.print("Clearing instances config..");
			this.instances.clear();
			Configuration cc = getConfig().getSection("Instances.Servers");
			SPhantom.print("Servers list filling..");
			cc.getKeys().forEach(
					k -> {
						Configuration section = cc.getSection(k);
						this.servers.add(new VpsConfigObject(k, section.getString("ip"), section.getString("user"), section.getString("pass"), section.getInt("port"), section
								.getBoolean("allowMultiInstance")));
					});
			SPhantom.print("Servers list [ok]");
			SPhantom.print("Instances config map filling");
			Configuration cc2 = getConfig().getSection("Instances.Types");
			cc2.getKeys().forEach(k -> {
				Configuration section = cc.getSection(k);
				this.instances.put(ServerType.valueOf(k), new McServerConfigObject(section.getInt("maxPlayers"), section.getInt("playersBeforeNewInstance"), section.getInt("ram")));
			});
			SPhantom.print("Instances config map [ok]");
			SPhantom.print("Done ! (" + (System.currentTimeMillis() - cur) + "ms)");
			isReloading = false;
		};
		if (async) SPhantom.getInstance().getExecutor().execute(r);
		else r.run();
	}

	public String getVultrKey() {
		return VultrKey;
	}

	public String getRabbitUser() {
		return RabbitUser;
	}

	public String getRabbitPassword() {
		return RabbitPassword;
	}

	public List<VpsConfigObject> getServers() {
		return servers;
	}

	public Map<ServerType, McServerConfigObject> getInstances() {
		return instances;
	}

	public void write() {

	}

	/**
	 * Représente la configuration d'un serveur dédié ou d'un vps
	 * 
	 * @author MrSceat
	 *
	 */
	public static class VpsConfigObject extends ConfigurationWrite {

		private String name;
		private String ip;
		private String user;
		private String pass;
		/**
		 * If this vps can support multiple mc servers
		 */
		private boolean allowMultiInstance;
		private int port;

		public VpsConfigObject(String name, String ip, String user, String pass, int port, boolean multi) {
			this.ip = ip;
			this.user = user;
			this.pass = pass;
			this.name = name;
			this.port = port;
			this.allowMultiInstance = multi;
		}

		public boolean allowMultiInstances() {
			return this.allowMultiInstance;
		}

		public String getName() {
			return name;
		}

		public String getIp() {
			return ip;
		}

		public String getUser() {
			return user;
		}

		public String getPass() {
			return pass;
		}

		public int getPort() {
			return port;
		}

		private String getPath() {
			return "Instances.Servers." + getName() + ".";
		}

		public void write(Configuration c) {
			write(c, getPath() + "ip", getIp());
			write(c, getPath() + "user", getUser());
			write(c, getPath() + "pass", getPass());
			write(c, getPath() + "port", getPort());
			write(c, getPath() + "allowMultiInstance", allowMultiInstances());
		}

	}

	public static class ConfigurationWrite {

		public void write(Configuration c, String path, Object towrite) {
			c.set(path, towrite);
		}
	}

	public static class McServerConfigObject extends ConfigurationWrite {

		private ServerType type;
		private int maxPlayers;
		private int playersBeforeOpenNewInstance;
		private int ramNeeded;

		public McServerConfigObject(int maxplayer, int playerbeforOpennew, int ramMax) {
			this.maxPlayers = maxplayer;
			this.playersBeforeOpenNewInstance = playerbeforOpennew;
			this.ramNeeded = ramMax;
		}

		public McServerConfigObject setType(ServerType type) {
			this.type = type;
			return this;
		}

		public int getMaxPlayers() {
			return maxPlayers;
		}

		public int getPlayersBeforeOpenNewInstance() {
			return playersBeforeOpenNewInstance;
		}

		public int getRamNeeded() {
			return ramNeeded;
		}

		public ServerType getType() {
			return type;
		}

		private String getPath() {
			return "Instances.Types." + getType().name() + ".";
		}

		public void write(Configuration c) {
			write(c, getPath() + "maxPlayers", getMaxPlayers());
			write(c, getPath() + "playersBeforeNewInstance", getPlayersBeforeOpenNewInstance());
			write(c, getPath() + "ram", getRamNeeded());
		}
	}

	public Configuration getConfig() {
		return config;
	}

	private void loadConfig() {
		try {
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(Main.folder.getAbsolutePath() + "/SPhantom.yml"));
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
		SPhantom.print("Config loaded");
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
