package sceat.domain.schedule;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Scheduler {

	private static Scheduler instance = new Scheduler();

	public static Scheduler getScheduler() {
		return instance;
	}

	private ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Scheduler pool - [Thrd: %d]").build());

	private Scheduler() {
	}

	public void shutdown() {
		pool.shutdown();
	}

	public void register(Scheduled scheduled) {
		Arrays.stream(scheduled.getClass().getMethods()).parallel().filter((m) -> m.isAnnotationPresent(Schedule.class)).forEach((m) -> {
			Schedule s = m.getAnnotation(Schedule.class);
			long ns = s.unit().toNanos(s.rate());
			pool.scheduleAtFixedRate(() -> {
				try {
					m.invoke(scheduled, new Object[m.getParameterCount()]);
				} catch (Exception e) {
					System.out.println("[Scheduler] Error with " + m.getName());
					e.printStackTrace();
				}
			}, ns, ns, java.util.concurrent.TimeUnit.NANOSECONDS);
		});
	}

}
