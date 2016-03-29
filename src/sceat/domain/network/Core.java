package sceat.domain.network;

import sceat.domain.schedule.Schedule;
import sceat.domain.schedule.Scheduled;
import sceat.domain.schedule.Scheduler;
import sceat.domain.schedule.TimeUnit;

/**
 * This is where the magic happens
 * 
 * @author MrSceat
 *
 */
public class Core implements Scheduled {

	private OperatingMode mode = OperatingMode.Normal;

	public Core() {
		Scheduler.getScheduler().register(this);
	}

	public OperatingMode getMode() {
		return mode;
	}

	@Schedule(rate = 30, unit = TimeUnit.SECONDS)
	public void run() {

	}

	public static enum OperatingMode {
		Eco,
		Normal,
		NoLag
	}

}
