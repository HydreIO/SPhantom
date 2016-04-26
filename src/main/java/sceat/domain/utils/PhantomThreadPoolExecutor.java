package sceat.domain.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import sceat.Main;

public class PhantomThreadPoolExecutor extends ThreadPoolExecutor {

	private static final int bts = Integer.SIZE - 3;

	public PhantomThreadPoolExecutor(int nThreads) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public PhantomThreadPoolExecutor(int nThreads, ThreadFactory factory) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), factory);
	}

	/**
	 * Stop the threadpool from retrieving new task from the waiting queue without intterupt workers and drain the queue
	 * 
	 * @return all the task who weren't processed
	 */
	public List<Runnable> safeDrain() {
		ArrayList<Runnable> taskList = new ArrayList<Runnable>();
		try {
			pause(1 << bts);
			BlockingQueue<Runnable> q = getQueue();
			q.drainTo(taskList);
			if (!q.isEmpty()) {
				for (Runnable r : q.toArray(new Runnable[0])) {
					if (q.remove(r)) taskList.add(r);
				}
			}
		} catch (Exception e) {
			Main.printStackTrace(e);
		} finally {
			pause(-1 << bts);
		}
		return taskList;
	}

	private void pause(int value) {
		Reflection.invoke(this, "advanceRunState", value);
	}

}
