package sceat;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.protocol.IMessaging;
import sceat.infra.connector.mq.RabbitMqConnector;

public class SPhantom {

	private static SPhantom instance;
	private ExecutorService executor;
	private ExecutorService pinger;
	private ExecutorService peaceMaker;
	boolean running;
	private IMessaging messageBroker;
	private SPhantomConfig config;

	public SPhantom(String user, String pass) { // don't change the implementation order !
		instance = this;
		this.running = true;
		new Manager();
		this.pinger = Executors.newSingleThreadExecutor();
		this.peaceMaker = Executors.newSingleThreadExecutor();
		this.config = new SPhantomConfig();
		this.executor = Executors.newFixedThreadPool(30);
		this.messageBroker = new RabbitMqConnector(user, pass);
		new Heart().takeLead();
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
					Main.shutDown();
					break;
				case "forcelead":
					Heart.getInstance().takeLead();
					break;
				default:
					break;
			}
		}

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

	public IMessaging getMessageBroker() {
		return messageBroker;
	}

	public static void print(String txt) {
		print(txt, true);
	}

	public static void print(String txt, boolean log) {
		if (log) Main.getLogger().info(txt);
		else System.out.println(new java.sql.Timestamp(System.currentTimeMillis()).toString().substring(0, 16) + " | [Sphantom] > " + txt);
	}

	public static SPhantom getInstance() {
		return instance;
	}

	public boolean isRunning() {
		return running;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

}
