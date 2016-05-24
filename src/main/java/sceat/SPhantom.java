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
import sceat.domain.config.SPhantomConfig;
import sceat.domain.network.Core;
import sceat.domain.network.Core.OperatingMode;
import sceat.domain.network.ServerProvider;
import sceat.domain.protocol.PacketSender;
import sceat.domain.protocol.handler.PacketHandler;
import sceat.domain.protocol.packets.PacketRegistry;
import sceat.domain.shell.Input;
import sceat.domain.trigger.PhantomTrigger;
import sceat.gui.web.GrizzlyWebServer;
import sceat.infra.connector.general.VultrConnector;
import sceat.infra.input.ScannerInput;
import fr.aresrpg.commons.concurrent.ThreadBuilder;
import fr.aresrpg.commons.concurrent.Threads;
import fr.aresrpg.commons.condition.functional.Executable;
import fr.aresrpg.commons.condition.match.Matcher;
import fr.aresrpg.commons.condition.match.Matcher.Case;
import fr.aresrpg.sdk.Weed;
import fr.aresrpg.sdk.concurrent.Async;
import fr.aresrpg.sdk.lang.BaseLang;
import fr.aresrpg.sdk.protocol.Security;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.system.Root;

public class SPhantom implements Async {

	private static SPhantom instance;
	private static boolean $ynchronized = false; // NOSONAR laisse mon swag
	private ExecutorService executor;
	private ExecutorService pinger;
	private ExecutorService peaceMaker;
	boolean running;
	private SPhantomConfig config;
	private boolean lead = false;
	private boolean local = false;
	private boolean logPkt = true;
	private boolean logHeart = false;
	private boolean logprovider = true;
	private boolean logDiv = true;
	private IPhantom iphantom;
	private Security security;
	private PhantomApi mainApi;
	private InetAddress ip;
	private static final String ENABLED = "enabled";
	private static final String DISABLED = "disabled";
	private static final Pattern modeM = Pattern.compile("^setmode ((?:\\d))$");

	/**
	 * Init sphantom
	 * 
	 * @param local
	 *            if true, allow the broker to send message (false for debugmode without broker).
	 */
	public SPhantom(Boolean local) { // don't change the implementation order !
		instance = this;
		Weed.init(new Root() {

			@Override
			public BaseLang getLang() {
				return null;
			}

			@Override
			public Async getAsync() {
				return getInstance();
			}

			@Override
			public void exit() {
				Main.shutDown();
			}
		});
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
		CompletableFuture.runAsync(() -> {
			Threads.uSleep(2, TimeUnit.MINUTES);
			$ynchronized = true;
		});
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

	public static boolean isSynchronized() {
		return $ynchronized;
	}

	public void startWebPanel() {
		Log.out("Starting web panel..");
		try {
			GrizzlyWebServer.init(81);
			Log.out("Web panel started!");
		} catch (IOException e) {
			Log.out("[ERREUR] Unable to start web server !");
			Log.trace(e);
		}
	}

	public void stopWebPanel() {
		Log.out("WebPanel stoping...");
		GrizzlyWebServer.stop();
		Log.out("WebPanel stopped.");
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setupIp() {
		Log.out("Oppening socket to get Ip...");
		try (Socket s = new Socket("google.com", 80)) {
			Log.out("Ip founded ! [" + s.getLocalAddress().getHostName() + "]");
			this.ip = s.getLocalAddress();
		} catch (IOException e) {
			Log.trace(e);
			Log.out("Unable to find the Ip !");
			Threads.uSleep(3, TimeUnit.SECONDS);
			Main.shutDown();
		}
	}

	public PhantomApi getMainApi() {
		return mainApi;
	}

	public ServerApi getServerApi(String srv) {
		return Manager.getInstance().getServersByLabel().safeGetOrDefault(srv, null);
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
		Log l = Log.getInstance();
		if (!lead) {
			l.logOut("----------------------------------------------------------");
			l.logOut("SPhantom instance has lost the lead !");
			l.logOut("Starting to run in background and wait for wakingUp !");
			l.logOut("try <forcelead> for set this instance to lead");
			l.logOut("----------------------------------------------------------");
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

	private <T, R> Case<T, R> when(Predicate<T> p, Executable e) {
		return Matcher.when(p, e);
	}

	public void awaitForInput() {
		Input input = Input.getInstance();
		Log l = Log.getInstance();
		while (isRunning()) {
			l.logOut("Send Input (type help for show cmds) :");
			l.logOut(".. >_");
			Matcher.match(input.next(), when("help"::equalsIgnoreCase, () -> {
				l.logOut("> shutdown [Close this Sphantom instance]");
				l.logOut("> forcelead [This instance will become the replica leader]");
				l.logOut("> logpkt [Enable/Disable the packet logger]");
				l.logOut("> logProvider [Enable/Disable the overspan logger]");
				l.logOut("> setMode <1|2|3> [Set operating mode Eco|Normal|NoLag]");
				l.logOut("> logHB [Enable/Disable the heartBeat logger]");
				l.logOut("> logDiv [Enable/Disable the global logger]");
				l.logOut("> vps [Show all vps]");
				l.logOut("> create_server");
			}), when("loghb"::equalsIgnoreCase, () -> {
				this.setLogHeart(!this.isLogHeart());
				l.logOut("HeartBeat logger " + (this.isLogHeart() ? ENABLED : DISABLED) + " !");
			}), when("vps"::equalsIgnoreCase, () -> {
				l.logOut("Vps registered : ");
				Core.getInstance().getVps().values().forEach(v -> l.logOut(v.toString() + "\n"));
			}), when("logdiv"::equalsIgnoreCase, () -> {
				this.logDiv = !this.logDiv;
				l.logOut("Diver logger " + (this.logDiv ? ENABLED : DISABLED) + " !");
			}), when("exit"::equalsIgnoreCase, Main::shutDown), when("forcelead"::equalsIgnoreCase, Heart.getInstance()::takeLead), when("logpkt"::equalsIgnoreCase, () -> {
				this.logPkt = !this.logPkt;
				l.logOut("Packet logger " + (this.logPkt ? ENABLED : DISABLED) + " !");
			}), when("logprovider"::equalsIgnoreCase, () -> {
				this.setLogprovider(!this.isLogprovider());
				l.logOut("ServerProvider logger " + (this.isLogprovider() ? ENABLED : DISABLED) + " !");
			}), when(a -> {
				java.util.regex.Matcher m = modeM.matcher(a.toLowerCase());
				if (m.matches()) {
					m.start();
					updateMode(Integer.parseInt(m.group(1)));
				}
				return false;
			}, () -> {}), Matcher.def(() -> Log.out("Unknow command!")));
		}

	}

	private void updateMode(int var) {
		OperatingMode m = var == 1 ? OperatingMode.ECO : var == 2 ? OperatingMode.NOLAG : OperatingMode.NOLAG;
		if (Core.getInstance().getMode() == m) Log.out(m.name() + " mode is already enabled");
		else Core.getInstance().setMode(OperatingMode.NOLAG, false);
	}

	public void stackTrace(int maxline) {
		String msg = new String();
		int i = 0;
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			msg += "\n[" + e.getClassName() + "]" + e.getMethodName() + ":" + e.getLineNumber();
			if (i == maxline) break;
			else i++;
		}
		Log.out(msg);
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
	public void run(Runnable r) {
		getExecutor().execute(r);
	}

	@Override
	public <T> CompletableFuture<T> supply(Supplier<T> t) {
		return CompletableFuture.<T> supplyAsync(t, getExecutor());
	}

	public boolean isLogprovider() {
		return logprovider;
	}

	public void setLogprovider(boolean logprovider) {
		this.logprovider = logprovider;
	}

	public boolean isLogHeart() {
		return logHeart;
	}

	public void setLogHeart(boolean logHeart) {
		this.logHeart = logHeart;
	}

}
