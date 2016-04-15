package sceat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sceat.domain.Heart;
import sceat.domain.ressources.Constant;
import sceat.domain.schedule.Scheduler;

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
		Constant.bootPrint().forEach(SPhantom::print);
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
		getLogger().log(Level.SEVERE, e.getMessage(), e);
	}

	public static void printStackTrace(Throwable e) {
		getLogger().log(Level.SEVERE, e.getMessage(), e);
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
		SPhantom.print("Shutdown..");
		SPhantom.getInstance().getExecutor().shutdown();
		Scheduler.getScheduler().shutdown();
		if (Heart.getInstance() != null) Heart.getInstance().broke();
		SPhantom.print("Bye.");
		SPhantom.getInstance().running = false;
		System.exit(0);
	}

	public static CommandLine setupOptions(Options opt, String[] args) {
		opt.addOption("local", false, "Disable messaging for local test");
		opt.addOption("gui", false, "boot has gui (if not specified Sphantom will boot has tui)");
		try {
			return new DefaultParser().parse(opt, args);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static File getFolder() {
		return folder;
	}
}
