package sceat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sceat.domain.Manager;
import sceat.domain.SPhantomTerminal;
import sceat.domain.forkupdate.ForkUpdate;
import sceat.domain.messaging.IMessaging;
import sceat.infra.RabbitMqConnector;

public class SPhantom {

	private static BufferedWriter writer;
	private static File folder;

	public static void main(String[] args) {
		folder = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		initLogger();
		print("Démarrage de Sphantom");
		new SPhantom();
	}

	private static SPhantom instance;

	private ForkUpdate updater;
	private ExecutorService executor;
	private boolean running;
	private IMessaging messageBroker;
	private SPhantomTerminal terminal;
	boolean deploy = false;
	private Manager manager;

	public SPhantom() {
		instance = this;
		this.running = true;
		this.manager = new Manager();
		this.executor = Executors.newFixedThreadPool(30);
		this.updater = new ForkUpdate();
		this.messageBroker = new RabbitMqConnector();
		if (deploy) awaitForInput();
		else {
			terminal = new SPhantomTerminal();
			getTerminal().awaitForInput();
		}
	}

	// MinecraftPingReply data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname("mc.sceat.network").setPort(25565));
	// print(data.getDescription() + "  --  " + data.getPlayers().getOnline() + "/" + data.getPlayers().getMax());

	public Manager getManager() {
		return manager;
	}

	public void awaitForInput() {
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		while (isRunning()) {
			print("Input : exit|gui");
			String nex = scan.next();
			switch (nex) {
				case "exit":
					shutDown();
					break;
				case "gui":
					terminal = new SPhantomTerminal();
					getTerminal().awaitForInput();
					break;
				default:
					print("Input : exit|gui");
					break;
			}
		}

	}

	public SPhantomTerminal getTerminal() {
		return terminal;
	}

	public static File getFolder() {
		return folder;
	}

	public IMessaging getMessageBroker() {
		return messageBroker;
	}

	public static void shutDown() {
		print("Arret de Sphantom..");
		getInstance().getExecutor().shutdown();
		getInstance().getUpdater().shutdown();
		print("Sphantom éteint !");
		endLogger();
		getInstance().running = false;
		System.exit(0);
	}

	public static void print(String txt) {
		print(txt, true);
	}

	public static void print(String txt, boolean log) {
		String s = new java.sql.Timestamp(System.currentTimeMillis()).toString().substring(0, 19) + " | [Sphantom] > " + txt;
		System.out.println(s);
		if (log) log(s);
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
