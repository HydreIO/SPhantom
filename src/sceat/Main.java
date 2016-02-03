package sceat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import sceat.domain.schedule.Scheduler;
import sceat.domain.utils.Constant;

public class Main {

	public static Logger logger = Logger.getLogger("SPhantom.class");
	public static File folder;
	public static final UUID serial = UUID.randomUUID();
	public static final UUID security = UUID.randomUUID();

	public static boolean GUImode = false;
	public static String VultrKey;
	public static InetAddress IpOvh;
	public static String userOvh;
	public static String passOvh;

	public static void main(String[] args) {
		assert false;
		folder = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		initLogger();
		Options opt = new Options();
		CommandLine cmd = setupOptions(opt, args);
		if (cmd == null) throw new NullPointerException("Unable to setup the commandLine.. Aborting..");
		Constant.startingText().forEach(SPhantom::print);
		if (cmd.hasOption("gui")) GUImode = true;
		if (cmd.hasOption("vultr")) VultrKey = cmd.getOptionValue("vultr");
		if (cmd.hasOption("ovh")) {
			try {
				String cm = cmd.getOptionValue("ovh");
				userOvh = cm.substring(cm.indexOf(":") + 1, cm.indexOf("@"));
				passOvh = cm.substring(cm.indexOf("@") + 1);
				IpOvh = InetAddress.getByName(cm.substring(0, cm.indexOf(":")));
			} catch (UnknownHostException e) {
				SPhantom.print("[WARN] UnknowHost for the -ovh arg !");
				SPhantom.print("[WARN] Dedicated server support : DISABLED");
			}
		}
		if (cmd.hasOption("auth")) {
			String str = cmd.getOptionValue("auth");
			if (!str.contains("@")) {
				SPhantom.print("[WARN] Invalid argument ! syntaxe must be \"user@pass\" for -auth");
				SPhantom.print("Shuting down..");
				SPhantom.print("Bye.");
				System.exit(1);
			}
			String user = str.substring(0, args[1].indexOf('@'));
			String pass = str.substring(args[1].indexOf('@') + 1);
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			loader.setDefaultAssertionStatus(true);
			try {
				loader.loadClass("SPhantom.class").getConstructor(String.class, String.class).newInstance(user, pass);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			SPhantom.getInstance().awaitForInput();
		} else {
			SPhantom.print("[WARN] User not specified ! try -auth user@pass");
			SPhantom.print("Shuting down..");
			SPhantom.print("Bye.");
			System.exit(1);
		}

	}

	public static Logger getLogger() {
		return logger;
	}

	public static void printStackTrace(Exception e) {
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
		opt.addOption("auth", true, "RabbitMq User@Pass");
		opt.addOption("vultr", true, "Vultr ApiKey");
		opt.addOption("ovh", true, "0.0.0.0:user@pass");
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
