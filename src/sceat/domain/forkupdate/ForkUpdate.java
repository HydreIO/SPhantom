package sceat.domain.forkupdate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ForkUpdate implements Runnable {

	public static HashMap<ForkUpdateType, HashSet<Method>> methods = new HashMap<ForkUpdateType, HashSet<Method>>();
	public static HashMap<Method, Object> listener = new HashMap<Method, Object>();
	private ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
	private ScheduledExecutorService updater = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
	private static ForkUpdate instance;

	public ForkJoinPool getPool() {
		return pool;
	}

	public static ForkUpdate getInstance() {
		return instance;
	}

	public ForkUpdate() {
		instance = this;
		updater.scheduleAtFixedRate(this, 0L, 49L, TimeUnit.MILLISECONDS);
	}

	public void shutdown() {
		getPool().shutdownNow();
	}

	@Override
	public void run() {
		ForkUpdateType[] values;
		for (int length = (values = ForkUpdateType.values()).length, i = 0; i < length; ++i) {
			final ForkUpdateType updateType = values[i];
			if (!updateType.Elapsed()) continue;
			if (methods.get(updateType) != null) {
				Queue<Method> met = new LinkedList<Method>();
				for (Method m : methods.get(updateType))
					try {
						met.add(m);
					} catch (Exception e) {
						e.printStackTrace();
					}
				getPool().invoke(new Overclock(met)); // ont créé une tache recursive

			}

		}
	}

	public static class Overclock extends RecursiveAction {

		private static final long serialVersionUID = 1L;

		private Queue<Method> keys = new LinkedList<Method>();

		public Overclock(Queue<Method> methods) {
			this.keys = methods;
		}

		public void execute(Method m) {
			try {
				m.invoke(listener.get(m));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}

		}

		public Queue<Method> getKeys() {
			return this.keys;
		}

		@Override
		protected void compute() {
			Method m = getKeys().poll(); // on recupere et on enleve la premiere methode de la queue
			execute(m); // on execute les actions de la methode

			if (getKeys().isEmpty()) { // si il n'y a plus de method dans la queue
				return; // on termine
			}
			Overclock ov1 = new Overclock(getKeys()); // sinon on fait une nouvelle tache
			ov1.fork(); // on l'envoie dans un autre thread

			return;
		}
	}

}
