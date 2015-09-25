package sceat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sceat.domain.forkupdate.ForkUpdate;
import ch.jamiete.mcping.MinecraftPing;
import ch.jamiete.mcping.MinecraftPingOptions;
import ch.jamiete.mcping.MinecraftPingReply;

public class SPhantom {

	private static BufferedWriter writer;
	private static File folder;

	public static void main(String[] args) {
		folder = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		initLogger();
		print("Démarrage de SPhantom");
		SPhantom phantom = new SPhantom();
		phantom.shutDown();
	}

	private static SPhantom instance;

	private ForkUpdate updater;
	private ExecutorService executor;
	private boolean running;

	public SPhantom() {
		this.running = true;
		this.executor = Executors.newFixedThreadPool(30);
		this.updater = new ForkUpdate();
		try {
			MinecraftPingReply data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname("mc.sceat.network").setPort(25565));
			print(data.getDescription() + "  --  " + data.getPlayers().getOnline() + "/" + data.getPlayers().getMax());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File getFolder() {
		return folder;
	}

	public void shutDown() {
		print("Arret de SPhantom..");
		getExecutor().shutdown();
		getUpdater().shutdown();
		print("SPhantom éteint !");
		endLogger();
		this.running = false;
	}

	public static void print(String txt) {
		String s = new java.sql.Timestamp(System.currentTimeMillis()) + " || SPhantom |: " + txt;
		System.out.println(s);
		log(s);
	}

	public static SPhantom getInstance() {
		return instance;
	}

	public boolean isRunning() {
		return running;
	}

	public ForkUpdate getUpdater() {
		return updater;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	private static BufferedWriter getWriter() {
		return writer;
	}

	private static void initLogger() {
		FileWriter fw = null;
		File f = new File(getFolder().getAbsolutePath() + "/SPhantom.log");
		try {
			if (!f.exists()) f.createNewFile();
			fw = new FileWriter(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		writer = new BufferedWriter(fw);
	}

	private static void endLogger() {
		try {
			getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void log(String txt) {
		try {
			getWriter().newLine();
			getWriter().write(txt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
