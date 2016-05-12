package sceat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import sceat.api.PhantomApi;
import sceat.api.PhantomApi.ServerApi;
import sceat.api.PhantomApi.VpsApi;
import sceat.domain.Heart;
import sceat.domain.Manager;
import sceat.domain.common.IPhantom;
import sceat.domain.common.system.Root;
import sceat.domain.config.SPhantomConfig;
import sceat.domain.network.Core;
import sceat.domain.network.Core.OperatingMode;
import sceat.domain.network.ServerProvider;
import sceat.domain.protocol.PacketSender;
import sceat.domain.protocol.handler.PacketHandler;
import sceat.domain.protocol.packets.PacketRegistry;
import sceat.domain.shell.Input;
import sceat.domain.trigger.PhantomTrigger;
import sceat.gui.terminal.PhantomTui;
import sceat.gui.web.GrizzlyWebServer;
import sceat.infra.connector.general.VultrConnector;
import sceat.infra.input.ScannerInput;
import fr.aresrpg.commons.concurrent.ThreadBuilder;
import fr.aresrpg.commons.concurrent.Threads;
import fr.aresrpg.commons.condition.match.Matcher;
import fr.aresrpg.commons.condition.match.Matcher.Case;
import fr.aresrpg.sdk.concurrent.Async;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.protocol.Security;
import fr.aresrpg.sdk.system.Log;

public class SPhantom implements Async, Log, Root {

	private static SPhantom instance;
	private ExecutorService executor;
	private ExecutorService pinger;
	private ExecutorService peaceMaker;
	boolean running;
	private SPhantomConfig config;
	private boolean lead = false;
	private boolean local = false;
	private boolean logPkt = true;
	public boolean logHeart = false;
	public boolean logprovider = true;
	public boolean logDiv = true;
	private IPhantom iphantom;
	private Security security;
	private PhantomApi mainApi;
	private InetAddress ip;
	private final String enabled = "enabled";
	private final String disabled = "disabled";
	private final Pattern modeM = Pattern.compile("^setmode ((?:\\d))$");

	/**
	 * Init sphantom
	 * 
	 * @param local
	 *            if true, allow the broker to send message (false for debugmode without broker).
	 */
	public SPhantom(Boolean local) { // don't change the implementation order !
		instance = this;
		setupIp();
		this.security = new Security(Main.serial, Main.security);
		PacketRegistry.registry();
		this.running = true;
		this.local = local;
		this.pinger = Executors.newSingleThreadExecutor(new ThreadBuilder().setName("Pinger Pool - [Thrd: %1%]").toFactory());
		this.peaceMaker = Executors.newSingleThreadExecutor(new ThreadBuilder().setName("PeaceMaker Pool - [Thrd: %1%]").toFactory());
		this.executor = Executors.newFixedThreadPool(70, new ThreadBuilder().setName("Main Pool - [Thrd: %1%]").toFactory());
		this.config = new SPhantomConfig();
		this.iphantom = new VultrConnector();
		PhantomTrigger.init();
		Manager.init();
		ServerProvider.init();
		Core.init();
		ScannerInput.init();
		PacketHandler.init();
		PacketSender.init(getSphantomConfig().getRabbitUser(), getSphantomConfig().getRabbitPassword(), local);
		new Heart(local).takeLead();
		startWebPanel(); // ne pas start deux sphantom sur la meme ip sinon le port va être déja utilisé abruti ! de tt façon quel interet d'un replica sur la meme machine..
	}

	public void startWebPanel() {
		print("Starting web panel..");
		try {
			new GrizzlyWebServer(81);
			print("Web panel started!");
		} catch (IOException e) {
			print("[ERREUR] Unable to start web server !");
			print("____________________________________________________\n");
			Main.printStackTrace(e);
			print("\n____________________________________________________");

		}
	}

	public void stopWebPanel() {
		print("WebPanel stoping...");
		GrizzlyWebServer.stop();
		print("WebPanel stopped.");
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setupIp() {
		print("Oppening socket to get Ip...");

		Socket s = null;
		try {
			s = new Socket("google.com", 80);
			print("Ip founded ! [" + s.getLocalAddress().getHostName() + "]");
			this.ip = s.getLocalAddress();
		} catch (IOException e) {
			Main.printStackTrace(e);
			print("Unable to find the Ip !");
			Threads.uSleep(3, TimeUnit.SECONDS);
			Main.shutDown();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				Log.trace(e);
			}
		}
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

	public IPhantom getIphantom() {
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

	public SPhantomConfig getSphantomConfig() {
		return config;
	}

	public ExecutorService getPinger() {
		return pinger;
	}

	public ExecutorService getPeaceMaker() {
		return peaceMaker;
	}

	private <T, R> Case<T, R> when(Predicate<T> p, Runnable r) {
		return Matcher.when(p, r);
	}

	public void awaitForInput() {
		Input input = Input.getInstance();
		while (isRunning()) {
			print("Send Input (type help for show cmds) :");
			print(".. >_");
			Matcher.match(input.next(), when("help"::equalsIgnoreCase, () -> {
				print("> shutdown [Close this Sphantom instance]");
				print("> forcelead [This instance will become the replica leader]");
				print("> logpkt [Enable/Disable the packet logger]");
				print("> logProvider [Enable/Disable the overspan logger]");
				print("> setMode <1|2|3> [Set operating mode Eco|Normal|NoLag]");
				print("> logHB [Enable/Disable the heartBeat logger]");
				print("> logDiv [Enable/Disable the global logger]");
				print("> vps [Show all vps]");
				print("> create_server");
			}), when("loghb"::equalsIgnoreCase, () -> {
				this.logHeart = !this.logHeart;
				print("HeartBeat logger " + (this.logHeart ? enabled : disabled) + " !");
			}), when("vps"::equalsIgnoreCase, () -> {
				print("Vps registered : ");
				Core.getInstance().getVps().values().forEach(v -> print(v.toString() + "\n"));
			}), when("logdiv"::equalsIgnoreCase, () -> {
				this.logDiv = !this.logDiv;
				print("Diver logger " + (this.logDiv ? enabled : disabled) + " !");
			}), when("exit"::equalsIgnoreCase, Main::shutDown), when("forcelead"::equalsIgnoreCase, Heart.getInstance()::takeLead), when("logpkt"::equalsIgnoreCase, () -> {
				this.logPkt = !this.logPkt;
				print("Packet logger " + (this.logPkt ? enabled : disabled) + " !");
			}), when("logprovider"::equalsIgnoreCase, () -> {
				this.logprovider = !this.logprovider;
				print("ServerProvider logger " + (this.logprovider ? enabled : disabled) + " !");
			}), when(a -> {
				java.util.regex.Matcher m = modeM.matcher(a.toLowerCase());
				if (m.matches()) {
					m.start();
					updateMode(Integer.parseInt(m.group(1)));
				}
				return false;
			}, () -> {}), Matcher.def(() -> print("Unknow command!")));
		}

	}

	private void updateMode(int var) {
		OperatingMode m = var == 1 ? OperatingMode.Eco : var == 2 ? OperatingMode.NoLag : OperatingMode.NoLag;
		if (Core.getInstance().getMode() == m) Log.out(m.name() + " mode is already enabled");
		else Core.getInstance().setMode(OperatingMode.NoLag, false);
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

	@Override
	public void logOut(String log) {
		print(log);
	}

	@Override
	public void logPkt(PacketPhantom pkt, boolean in) {
		if (logPkt) logOut((in ? "<RECV] " : "[SEND> ") + pkt.toString());
	}

	@Override
	public void logTrace(Exception e) {
		Main.printStackTrace(e);
	}

	@Override
	public void logTrace(Throwable t) {
		Main.printStackTrace(t);
	}

	@Override
	public void run(Runnable r) {
		getExecutor().execute(r);
	}

	@Override
	public <T> CompletableFuture<T> supply(Supplier<T> t) {
		return CompletableFuture.<T> supplyAsync(t, getExecutor());
	}

	@Override
	public void exit() {
		Main.shutDown();
	}

}
