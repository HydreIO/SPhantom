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
import sceat.domain.config.SPhantomConfig;
import sceat.domain.forkupdate.ForkUpdate;
import sceat.domain.messaging.IMessaging;
import sceat.infra.RabbitMqConnector;

public class SPhantom {

	boolean deploy = false;

	private static BufferedWriter writer;
	private static File folder;

	public static void main(String[] args) {
		folder = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		initLogger();
		print("__________________________________________________");
		print("___________________ SPHANTOM _____________________");
		print("Booting..");
		if (args.length == 2) {
			if (args[0].endsWith("-auth")) {
				String user = args[1].substring(0, args[1].indexOf('@'));
				String pass = args[1].substring(args[1].indexOf('@') + 1);
				new SPhantom(user, pass);
			} else {
				print("Incorrect args ! type ./sphantom start -auth user@pass");
			}
		} else {
			print("Incorrect args ! type ./sphantom start -auth user@pass");
		}
		print("Shutdown..");
		print("Bye.");

		System.exit(1);
	}

	private static SPhantom instance;

	private ForkUpdate updater;
	private ExecutorService executor;
	private ExecutorService pinger;
	private boolean running;
	private IMessaging messageBroker;
	private SPhantomTerminal terminal;
	private Manager manager;
	private SPhantomConfig config;

	public SPhantom(String user, String pass) {
		instance = this;
		this.running = true;
		this.pinger = Executors.newSingleThreadExecutor();
		this.config = new SPhantomConfig();
		this.manager = new Manager();
		this.executor = Executors.newFixedThreadPool(30);
		this.updater = new ForkUpdate();
		this.messageBroker = new RabbitMqConnector(user, pass);
		if (deploy) awaitForInput();
		else {
			terminal = new SPhantomTerminal();
			getTerminal().awaitForInput();
		}
	}

	public SPhantomConfig getSphantomConfig() {
		return config;
	}

	public ExecutorService getPinger() {
		return pinger;
	}

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

	public static void printStackTrace(Exception e) {
		e.printStackTrace();
		for (StackTraceElement ez : e.getStackTrace())
			log(ez.toString());
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
		print("Shutdown..");
		getInstance().getExecutor().shutdown();
		getInstance().getUpdater().shutdown();
		print("Bye.");
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

	public static void print(String txt, boolean error, boolean log) {
		String s = new java.sql.Timestamp(System.currentTimeMillis()).toString().substring(0, 19) + " | [" + (error ? "Error" : "Sphantom") + "] > " + txt;
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
			fw = new FileWriter(f, true);
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
			getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
