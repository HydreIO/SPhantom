package sceat;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sceat.domain.utils.IronHeart;
import sceat.gui.web.GrizzlyWebServer;
import sceat.infra.input.ScannerInput;
import fr.aresrpg.commons.domain.log.Logger;
import fr.aresrpg.commons.domain.util.schedule.Scheduler;
import fr.aresrpg.sdk.system.Broker;
import fr.aresrpg.sdk.system.Log;
import fr.aresrpg.sdk.util.Constant;

public class Main {

	private static File folder;
	public static final UUID serial = UUID.randomUUID();
	public static final UUID security = UUID.randomUUID();

	private static boolean localMode = false;

	private Main() {
	}

	public static void main(String[] args) {
		assert false;
		folder = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		Options opt = new Options();
		CommandLine cmd = setupOptions(opt, args);
		if (cmd == null) throw new NullPointerException("Unable to setup the commandLine.. Aborting..");
		Constant.SPHANTOM.forEach(Log::out);
		localMode = cmd.hasOption("local");
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		loader.setDefaultAssertionStatus(true);
		try {
			loader.loadClass("sceat.SPhantom").getConstructor(Boolean.class).newInstance(localMode);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			Logger.MAIN_LOGGER.error(e);
		}
		SPhantom.getInstance().awaitForInput();
	}

	public static void printStackTrace(Throwable e) {
		Logger.MAIN_LOGGER.severe("[Trace] > " + stackTrace(e));
	}

	private static String stackTrace(Throwable cause) {
		if (cause == null) return "";
		StringWriter sw = new StringWriter(1024);
		final PrintWriter pw = new PrintWriter(sw);
		cause.printStackTrace(pw); // NOSONAR no need to use a logger here !
		pw.flush();
		return sw.toString();
	}

	public static void shutDown() {
		fr.aresrpg.commons.domain.log.Logger.MAIN_LOGGER.info("Bye.");
		Broker.get().close();
		GrizzlyWebServer.stop();
		ScannerInput.shutDown();
		SPhantom.getInstance().getExecutor().shutdown();
		Scheduler.getScheduler().shutdown();
		if (IronHeart.get() != null) IronHeart.get().broke();
		fr.aresrpg.commons.domain.log.Logger.MAIN_LOGGER.info("Bye.");
		SPhantom.getInstance().running = false;
		System.exit(0); // NOSONAR system.exit is required here
	}

	public static CommandLine setupOptions(Options opt, String[] args) {
		opt.addOption("local", false, "Disable messaging for local test");
		try {
			return new BasicParser().parse(opt, args);
		} catch (ParseException e) {
			fr.aresrpg.commons.domain.log.Logger.MAIN_LOGGER.info(e);
			return null;
		}
	}

	public static File getFolder() {
		return folder;
	}

}
