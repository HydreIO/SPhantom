package sceat.domain.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.ressources.Constant;
import fr.aresrpg.commons.util.map.EnumHashMap;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.util.minecraft.ServerType;

public class SPhantomConfig {

	private Configuration config;
	private String rabbitUser = "user";
	private String rabbitPass = "pass";
	private String rabbitAdress = "127.0.0..";
	private int rabbitPort = 0000;
	private int deployedVpsRam = 8;
	private String vultrKey = "key";
	private int maxInstance = 10;
	private List<VpsConfigObject> servers = new ArrayList<>();
	private EnumMap<ServerType, McServerConfigObject> instances = new EnumHashMap<>(ServerType.class);
	private static final String PATH = "/SPhantom.yml";

	private static boolean reloading = false;

	public SPhantomConfig() {
		Log.out("Searching SPhantom.yml..");
		File cong = new File(Main.getFolder().getAbsolutePath() + PATH);
		boolean mkdir = true;
		try {
			mkdir = cong.exists() ? false : cong.createNewFile();
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(cong);
		} catch (IOException e3) {
			Main.printStackTrace(e3);
		}
		if (mkdir) {
			this.servers.add(new VpsConfigObject("Ovh_001", "127.0.0.", "user", "pass", 16));
			this.servers.add(new VpsConfigObject("Vultr_001", "127.0.0.", "user", "pass", 8));
			Arrays.stream(ServerType.values()).forEach(v -> this.instances.put(v, new McServerConfigObject(75, 50, 2))); // NOSONAR j'attend tjr ton stream closer
			getConfig().set("Broker.User", rabbitUser);
			getConfig().set("Broker.Pass", rabbitPass);
			getConfig().set("Broker.Adress", rabbitAdress);
			getConfig().set("Broker.Port", rabbitPort);
			getConfig().set("MaxDeployedInstances", this.maxInstance);
			getConfig().set("DeployedVpsRam", deployedVpsRam);
			getConfig().set(Constant.CONFIG_OPTIONAL_VAR + ".Vultr_Api_Key", getVultrKey());
			getServers().forEach(s -> s.write(getConfig()));
			getInstances().forEach((k, v) -> v.setType(k).write(getConfig()));
			saveConfig(getConfig(), cong);
			Log.out("SPhantom.yml just created to " + cong.getAbsolutePath() + " Please configure and reload /!\\");
		} else {
			Log.out("SPhantom.yml found in " + cong.getAbsolutePath() + " !");
		}
		load(false);
	}

	public static SPhantomConfig get() {
		return SPhantom.getInstance().getSphantomConfig();
	}

	public int getRamFor(ServerType type) {
		return instances.get(type).getRamNeeded();
	}

	public int getDeployedVpsRam() {
		return deployedVpsRam;
	}

	public int getMaxInstance() {
		return maxInstance;
	}

	public String getRabbitAdress() {
		return rabbitAdress;
	}

	public int getRabbitPort() {
		return rabbitPort;
	}

	public static boolean isReloading() {
		return reloading;
	}

	public static void setReloading(boolean var) {
		reloading = var;
	}

	public void load(boolean async) {
		Runnable r = () -> {
			Log.out("Loading configuration.. please wait !");
			long cur = System.currentTimeMillis();
			loadConfig();
			Log.out("Applying changes...");
			setReloading(true);
			this.maxInstance = getConfig().getInt("MaxDeployedInstances");
			this.rabbitUser = getConfig().getString("Broker.User");
			Log.out("Broker_user [ok]");
			this.rabbitPass = getConfig().getString("Broker.Pass");
			Log.out("Broker_pass [ok]");
			this.rabbitAdress = getConfig().getString("Broker.Adress");
			Log.out("Broker_adress [ok]");
			this.rabbitPort = getConfig().getInt("Broker.Port");
			Log.out("Broker_port [ok]");
			this.deployedVpsRam = getConfig().getInt("DeployedVpsRam");
			Log.out("Vultr_Pass [ok]");
			this.vultrKey = getConfig().getString(Constant.CONFIG_OPTIONAL_VAR + ".Vultr_Api_Key");
			Log.out("Vultr_key [ok]");
			Log.out("Clearing servers config..");
			this.servers.clear();
			Log.out("Clearing instances config..");
			this.instances.clear();
			Configuration cc = getConfig().getSection("Instances.Servers");
			Log.out("Servers list filling..");
			cc.getKeys().forEach(k -> {
				Configuration section = cc.getSection(k);
				this.servers.add(new VpsConfigObject(k, section.getString("ip"), section.getString("user"), section.getString("pass"), section.getInt("ram")));
			});
			Log.out("Servers list [ok]");
			Log.out("Instances config map filling");
			Configuration cc2 = getConfig().getSection("Instances.Types");
			cc2.getKeys().forEach(k -> {
				Configuration section = cc2.getSection(k);
				this.instances.put(ServerType.valueOf(k), new McServerConfigObject(section.getInt("maxPlayers"), section.getInt("playersBeforeNewInstance"), section.getInt("ram")));
			});
			Log.out("Instances config map [ok]");
			Log.out("Done ! (" + (System.currentTimeMillis() - cur) + "ms)");
			setReloading(false);
		};
		if (async) SPhantom.getInstance().getExecutor().execute(r);
		else r.run();
	}

	public String getVultrKey() {
		return vultrKey;
	}

	public String getRabbitUser() {
		return rabbitUser;
	}

	public String getRabbitPassword() {
		return rabbitPass;
	}

	public List<VpsConfigObject> getServers() {
		return servers;
	}

	public EnumMap<ServerType, McServerConfigObject> getInstances() { // NOSONAR enumMap is already a interface implem
		return instances;
	}

	public void write() {
		// unused
	}

	/**
	 * Repr�sente la configuration d'un serveur d�di� ou d'un vps
	 * 
	 * @author MrSceat
	 *
	 */
	public static class VpsConfigObject extends ConfigurationWrite {

		private String name;
		private String ip;
		private String user;
		private String pass;
		private int ram;

		public VpsConfigObject(String name, String ip, String user, String pass, int ram) {
			this.ip = ip;
			this.user = user;
			this.pass = pass;
			this.name = name;
			this.ram = ram;
		}

		public int getRam() {
			return ram;
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

		private String getPath() {
			return "Instances.Servers." + getName() + ".";
		}

		public void write(Configuration c) {
			super.write(c, getPath() + "ip", getIp());
			super.write(c, getPath() + "user", getUser());
			super.write(c, getPath() + "pass", getPass());
			super.write(c, getPath() + "ram", getRam());
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
		private int percentplayersBeforeNewInstance;
		private int ramNeeded;

		public McServerConfigObject(int maxplayer, int playerbeforOpennew, int ramMax) {
			this.maxPlayers = maxplayer;
			this.percentplayersBeforeNewInstance = playerbeforOpennew;
			this.ramNeeded = ramMax;
		}

		public McServerConfigObject setType(ServerType type) {
			this.type = type;
			return this;
		}

		public int getMaxPlayers() {
			return maxPlayers;
		}

		public int getPercentPlayersBeforeOpenNewInstance() {
			return percentplayersBeforeNewInstance;
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
			super.write(c, getPath() + "maxPlayers", getMaxPlayers());
			super.write(c, getPath() + "PercentplayersBeforeNewInstance", getPercentPlayersBeforeOpenNewInstance());
			super.write(c, getPath() + "ram", getRamNeeded());
		}
	}

	public Configuration getConfig() {
		return config;
	}

	private void loadConfig() {
		try {
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(Main.getFolder().getAbsolutePath() + PATH));
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
		Log.out("Config loaded");
	}

	public void saveConfig() {
		saveConfig(getConfig(), Main.getFolder().getAbsolutePath() + PATH);
		Log.out("Config saved");
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
