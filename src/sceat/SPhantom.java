package sceat;

import java.util.Calendar;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.adapter.general.Iphantom;
import sceat.domain.adapter.mq.IMessaging;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.network.Core;
import sceat.domain.network.Core.OperatingMode;
import sceat.domain.protocol.PacketHandler;
import sceat.domain.protocol.PacketSender;
import sceat.domain.protocol.Security;
import sceat.domain.protocol.packets.PacketPhantom;
import sceat.infra.connector.mq.RabbitMqConnector;

public class SPhantom {

	private static SPhantom instance;
	private ExecutorService executor;
	private ExecutorService pinger;
	private ExecutorService peaceMaker;
	boolean running;
	private boolean brokerInit = false;
	private SPhantomConfig config;
	private boolean lead = false;
	private boolean local = false;
	private boolean logPkt = false;
	public boolean logprovider = false;
	private Iphantom iphantom;
	private Security security;

	/**
	 * Init sphantom
	 * 
	 * @param local
	 *            if true, allow the broker to send message (false for debugmode without broker)
	 */
	public SPhantom(Boolean local) { // don't change the implementation order !
		instance = this;
		this.security = new Security(Main.serial, Main.security);
		this.running = true;
		this.local = local;
		this.pinger = Executors.newSingleThreadExecutor();
		this.peaceMaker = Executors.newSingleThreadExecutor();
		this.executor = Executors.newFixedThreadPool(30);
		this.config = new SPhantomConfig();
		PacketPhantom.init();
		new Manager();
		new Core();
		new PacketHandler();
		new PacketSender(getSphantomConfig().getRabbitUser(), getSphantomConfig().getRabbitPassword(), local);
		new Heart(local).takeLead();
	}

	public boolean logPkt() {
		return this.logPkt;
	}

	public boolean isLocal() {
		return this.local;
	}

	public Iphantom getIphantom() {
		return iphantom;
	}

	public void setLead(boolean lead) {
		if (!lead) {
			print("----------------------------------------------------------");
			print("SPhantom instance has lost the lead !");
			print("Starting to run in background and wait for wakingUp !");
			print("SPhantom can't print report until he get the lead ! try <forcelead> or switch to the leading SPhantom instance");
			print("----------------------------------------------------------");
		}
		PacketSender.getInstance().pause(!lead);
		this.lead = lead;
	}

	public Security getSecurity() {
		return security;
	}

	public boolean isLeading() {
		return this.lead;
	}

	public boolean isTimeBetween(int var1, int var2) {
		int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		return h >= var1 && h <= var2;
	}

	public IMessaging initBroker(String user, String pass, boolean local) {
		if (brokerInit) throw new IllegalAccessError("Broker already initialised !");
		brokerInit = true;
		return new RabbitMqConnector(user, pass, local);
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
		while (isRunning()) {
			print("Actual Input :");
			print("> shutdown [Close l'instance de Sphantom]");
			print("> forcelead [Cette instance deviendra le leader du replica]");
			print("> logpkt [Active/desactive le logger des packets]");
			print("> logProvider [Active/desactive le logger de l'overspan]");
			print("> setMode <1|2|3> [Active/desactive le logger de l'overspan]");
			print(".. >_");
			String nex = scan.next();
			switch (nex) {
				case "shutdown":
					Main.shutDown();
					break;
				case "forcelead":
					Heart.getInstance().takeLead();
					break;
				case "logpkt":
					this.logPkt = !this.logPkt;
					print("Packet logger " + (this.logPkt ? "enabled" : "disabled") + " !");
					break;
				case "logprovider":
				case "logProvider":
					this.logprovider = !this.logprovider;
					print("ServerProvider logger " + (this.logprovider ? "enabled" : "disabled") + " !");
					break;
				case "setmode 1": // olalala aucun parsing, pabo dutou !
				case "setMode 1":
					if (Core.getInstance().getMode() == OperatingMode.Eco) {
						print("Eco mode already enabled !");
						break;
					}
					Core.getInstance().setMode(OperatingMode.Eco);
					print("Eco mode enabled");
					break;
				case "setmode 2":
				case "setMode 2":
					if (Core.getInstance().getMode() == OperatingMode.Normal) {
						print("Normal mode already enabled !");
						break;
					}
					Core.getInstance().setMode(OperatingMode.Normal);
					print("Normal mode enabled");
					break;
				case "setmode 3":
				case "setMode 3":
					if (Core.getInstance().getMode() == OperatingMode.NoLag) {
						print("NoLag mode already enabled !");
						break;
					}
					Core.getInstance().setMode(OperatingMode.NoLag);
					print("NoLag mode enabled");
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
