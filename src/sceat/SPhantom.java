package sceat;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.forkupdate.ForkUpdate;
import sceat.domain.messaging.IMessaging;
import sceat.domain.server.Overspan;
import sceat.domain.shell.SPhantomTerminal;
import sceat.infra.RabbitMqConnector;

public class SPhantom {

	private static Logger logger = Logger.getLogger("SPhantom.class");
	private static File folder;
	public static final UUID serial = UUID.randomUUID();
	public static final UUID security = UUID.randomUUID();

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
				print("Shutdown..");
				print("Bye.");
				System.exit(1);
			}
		} else {
			print("Incorrect args ! type ./sphantom start -auth user@pass");
			print("Shutdown..");
			print("Bye.");
			System.exit(1);
		}
		SPhantom.getInstance().awaitForInput();
	}

	private static SPhantom instance;

	private ForkUpdate updater;
	private ExecutorService executor;
	private ExecutorService pinger;
	private ExecutorService peaceMaker;
	private boolean running;
	private IMessaging messageBroker;
	private SPhantomTerminal terminal;
	private Manager manager;
	private SPhantomConfig config;

	public SPhantom(String user, String pass) { // don't change the implementation order !
		instance = this;
		this.running = true;
		this.pinger = Executors.newSingleThreadExecutor();
		this.peaceMaker = Executors.newSingleThreadExecutor();
		this.config = new SPhantomConfig();
		this.manager = new Manager();
		this.executor = Executors.newFixedThreadPool(30);
		this.updater = new ForkUpdate();
		this.messageBroker = new RabbitMqConnector(user, pass);
		new Heart().takeLead();
		new Overspan();
		terminal = new SPhantomTerminal();
		getTerminal().awaitForInput();
	}

	public SPhantomConfig getSphantomConfig() {
		return config;
	}

	public ExecutorService getPinger() {
		return pinger;
	}

	public ExecutorService getPeaceMaker() {
		return peaceMaker;
	}

	public Manager getManager() {
		return manager;
	}

	public void awaitForInput() {
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		print("----------------------------------------------------------");
		print("SPHantom instance has lost the lead !");
		print("Starting to run in background and wait for wakingUp !");
		print("SPhantom can't attach the TUI until he get the lead ! try <forcelead> or switch to the leading SPhantom instance");
		print("----------------------------------------------------------");
		while (isRunning()) {
			print("Input : shutdown|forcelead");
			String nex = scan.next();
			switch (nex) {
				case "shutdown":
					shutDown();
					break;
				case "forcelead":
					Heart.getInstance().takeLead();
					wakeUp();
					break;
				default:
					break;
			}
		}

	}

	public static void printStackTrace(Exception e) {
		getLogger().log(Level.SEVERE, e.getMessage(), e);
	}

	public void stackTrace(int maxline) {
		String msg = new String();
		int i = 0;
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			msg += "\n[" + e.getClassName() + "]" + e.getMethodName() + ":" + e.getLineNumber();
			if (i == maxline) break;
			else i++;
		}
		System.out.println(msg);
	}

	public void pause() {
		if (getTerminal() == null) return;
		if (getTerminal().isRunning()) getTerminal().shutdown();
	}

	public void wakeUp() {
		if (!getTerminal().isRunning()) getTerminal().awaitForInput();
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
		if (Heart.getInstance() != null) Heart.getInstance().broke();
		print("Bye.");
		getInstance().running = false;
		System.exit(0);
	}

	public static void print(String txt) {
		print(txt, true);
	}

	public static void print(String txt, boolean log) {
		if (log) getLogger().info(txt);
		else System.out.println(new java.sql.Timestamp(System.currentTimeMillis()).toString().substring(0, 16) + " | [Sphantom] > " + txt);

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

	public static Logger getLogger() {
		return logger;
	}

	private static void initLogger() {
		FileHandler file;
		Logger rootLogger = Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		if (handlers[0] instanceof ConsoleHandler) {
			rootLogger.removeHandler(handlers[0]);
		}

		try {
			file = new FileHandler(getFolder().getAbsolutePath() + "/SPhantom.log");
			file.setFormatter(new Formatter() {

				@Override
				public String format(LogRecord record) {
					return new java.sql.Timestamp(System.currentTimeMillis()).toString().substring(0, 16) + " | [Sphantom] > " + record.getMessage() + "\n";
				}
			});
			ConsoleHandler hand = new ConsoleHandler();
			hand.setFormatter(new Formatter() {

				@Override
				public String format(LogRecord record) {
					return new java.sql.Timestamp(System.currentTimeMillis()).toString().substring(0, 16) + " | [Sphantom] > " + record.getMessage() + "\n";
				}
			});
			logger.addHandler(hand);
			logger.addHandler(file);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}

	}

}
