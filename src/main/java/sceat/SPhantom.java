package sceat;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.adapter.api.PhantomApi;
import sceat.domain.adapter.api.PhantomApi.ServerApi;
import sceat.domain.adapter.api.PhantomApi.VpsApi;
import sceat.domain.adapter.general.Iphantom;
import sceat.domain.adapter.mq.IMessaging;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.network.Core;
import sceat.domain.network.Core.OperatingMode;
import sceat.domain.network.ServerProvider;
import sceat.domain.protocol.PacketSender;
import sceat.domain.protocol.Security;
import sceat.domain.protocol.handler.PacketHandler;
import sceat.domain.protocol.packets.PacketPhantom;
import sceat.domain.shell.Input;
import sceat.gui.terminal.PhantomTui;
import sceat.infra.connector.general.VultrConnector;
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
	private boolean logPkt = true;
	public boolean logHeart = false;
	public boolean logprovider = true;
	public boolean logDiv = true;
	private Iphantom iphantom;
	private Security security;
	private PhantomApi mainApi;

	/**
	 * Init sphantom
	 * 
	 * @param local
	 *            if true, allow the broker to send message (false for debugmode without broker)
	 */
	public SPhantom(Boolean local) { // don't change the implementation order !
		instance = this;
		this.security = new Security(Main.serial, Main.security);
		PacketPhantom.registerPkts();
		this.running = true;
		this.local = local;
		this.pinger = Executors.newSingleThreadExecutor(new ThreadFactory() {
			final AtomicInteger count = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "Pinger Pool - [Thrd: " + count.incrementAndGet() + "]");
				return thread;
			}
		});
		this.peaceMaker = Executors.newSingleThreadExecutor(new ThreadFactory() {
			final AtomicInteger count = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "PeaceMaker Pool - [Thrd: " + count.incrementAndGet() + "]");
				return thread;
			}
		});
		this.executor = Executors.newFixedThreadPool(70, new ThreadFactory() {
			final AtomicInteger count = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "Main Pool - [Thrd: " + count.incrementAndGet() + "]");
				return thread;
			}
		});
		this.config = new SPhantomConfig();
		this.iphantom = new VultrConnector();
		new Manager();
		new ServerProvider();
		new Core();
		new PacketHandler();
		new PacketSender(getSphantomConfig().getRabbitUser(), getSphantomConfig().getRabbitPassword(), local);
		new Heart(local).takeLead();
	}

	public PhantomApi getMainApi() {
		return mainApi;
	}

	public ServerApi getServerApi(String srv) {
		return Manager.getInstance().getServersByLabel().getOrDefault(srv, null);
	}

	public VpsApi getVpsApi(String vps) {
		return Core.getInstance().getVps().getOrDefault(vps, null);
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

	public static boolean logDiv() {
		return SPhantom.getInstance().logDiv;
	}

	public void setLead(boolean lead) {
		if (!lead) {
			print("----------------------------------------------------------");
			print("SPhantom instance has lost the lead !");
			print("Starting to run in background and wait for wakingUp !");
			print("try <forcelead> for set this instance to lead");
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
		Input input = Input.getInstance();
		while (isRunning()) {
			print("Send Input (type help for show cmds) :");
			print(".. >_");
			switch (input.next()) {
				case "help":
				case "Help":
					print("> shutdown [Close this Sphantom instance]");
					print("> forcelead [This instance will become the replica leader]");
					print("> logpkt [Enable/Disable the packet logger]");
					print("> logProvider [Enable/Disable the overspan logger]");
					print("> setMode <1|2|3> [Set operating mode Eco|Normal|NoLag]");
					print("> logHB [Enable/Disable the heartBeat logger]");
					print("> logDiv [Enable/Disable the global logger]");
					print("> vps [Show all vps]");
					print("> create_server");
					break;
				case "create_srv":
				case "create_server":
					try {
						PhantomTui.commandServ();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case "loghb":
				case "logHB":
					this.logHeart = !this.logHeart;
					print("HeartBeat logger " + (this.logHeart ? "enabled" : "disabled") + " !");
					break;
				case "vps":
					print("Vps registered : ");
					Core.getInstance().getVps().values().forEach(v -> print(v.toString() + "\n"));
					break;
				case "logdiv":
				case "logDiv":
					this.logDiv = !this.logDiv;
					print("Diver logger " + (this.logDiv ? "enabled" : "disabled") + " !");
					break;
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
				case "setmode1": // olalala aucun parsing, pabo dutou !
				case "setMode1":
					if (Core.getInstance().getMode() == OperatingMode.Eco) {
						print("Eco mode already enabled !");
						break;
					}
					Core.getInstance().setMode(OperatingMode.Eco, false);
					print("Eco mode enabled");
					break;
				case "setmode2":
				case "setMode2":
					if (Core.getInstance().getMode() == OperatingMode.Normal) {
						print("Normal mode already enabled !");
						break;
					}
					Core.getInstance().setMode(OperatingMode.Normal, false);
					print("Normal mode enabled");
					break;
				case "setmode3":
				case "setMode3":
					if (Core.getInstance().getMode() == OperatingMode.NoLag) {
						print("NoLag mode already enabled !");
						break;
					}
					Core.getInstance().setMode(OperatingMode.NoLag, false);
					print("NoLag mode enabled");
					break;
				default:
					print("Unknow command!");
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
		if (!PhantomTui.canlog) return;
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
