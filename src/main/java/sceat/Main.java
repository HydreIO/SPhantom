package sceat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sceat.domain.Heart;
import sceat.domain.common.mq.Broker;
import sceat.gui.web.GrizzlyWebServer;
import sceat.infra.input.ScannerInput;
import fr.aresrpg.commons.util.schedule.Scheduler;
import fr.aresrpg.sdk.util.Constant;

public class Main {

	public static Logger logger = Logger.getLogger("SPhantom.class");
	public static File folder;
	public static final UUID serial = UUID.randomUUID();
	public static final UUID security = UUID.randomUUID();

	public static boolean GUImode = false;
	public static boolean LocalMode = false;

	public static void main(String[] args) {
		assert false;
		folder = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		initLogger();
		Options opt = new Options();
		CommandLine cmd = setupOptions(opt, args);
		if (cmd == null) throw new NullPointerException("Unable to setup the commandLine.. Aborting..");
		Constant.sphantom.forEach(SPhantom::print);
		GUImode = cmd.hasOption("gui");
		LocalMode = cmd.hasOption("local");
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		loader.setDefaultAssertionStatus(true);
		try {
			loader.loadClass("sceat.SPhantom").getConstructor(Boolean.class).newInstance(LocalMode);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		SPhantom.getInstance().awaitForInput();
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void printStackTrace(Exception e) {
		getLogger().log(Level.SEVERE, "[Trace] > " + stackTrace(e));
	}

	public static void printStackTrace(Throwable e) {
		getLogger().log(Level.SEVERE, "[Trace] > " + stackTrace(e));
	}

	private static String stackTrace(Throwable cause) {
		if (cause == null) return "";
		StringWriter sw = new StringWriter(1024);
		final PrintWriter pw = new PrintWriter(sw);
		cause.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

	private static String stackTrace(Exception cause) {
		if (cause == null) return "";
		StringWriter sw = new StringWriter(1024);
		final PrintWriter pw = new PrintWriter(sw);
		cause.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

	private static void initLogger() {
		FileHandler file;
		Logger rootLogger = Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		if (handlers[0] instanceof ConsoleHandler) {
			rootLogger.removeHandler(handlers[0]);
		}

		try {
			file = new FileHandler(Main.folder.getAbsolutePath() + "/SPhantom.log");
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
			Main.logger.addHandler(hand);
			Main.logger.addHandler(file);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void shutDown() {
		fr.aresrpg.commons.log.Logger.MAIN_LOGGER.info("Bye.");
		Broker.get().close();
		GrizzlyWebServer.stop();
		ScannerInput.shutDown();
		SPhantom.getInstance().getExecutor().shutdown();
		Scheduler.getScheduler().shutdown();
		if (Heart.getInstance() != null) Heart.getInstance().broke();
		fr.aresrpg.commons.log.Logger.MAIN_LOGGER.info("Bye.");
		SPhantom.getInstance().running = false;
		System.exit(0);
	}

	public static CommandLine setupOptions(Options opt, String[] args) {
		opt.addOption("local", false, "Disable messaging for local test");
		opt.addOption("gui", false, "boot has gui (if not specified Sphantom will boot has tui)");
		try {
			return new BasicParser().parse(opt, args);
		} catch (ParseException e) {
			fr.aresrpg.commons.log.Logger.MAIN_LOGGER.info(e);
			return null;
		}
	}

	public static File getFolder() {
		return folder;
	}

}
