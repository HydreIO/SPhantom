package sceat.domain.common.thread;

import java.util.concurrent.TimeUnit;

import sceat.domain.common.system.Log;

public interface ScThread {

	public static void sleep(int i, TimeUnit unit) {
		try {
			Thread.sleep(unit.toMillis(i));
		} catch (InterruptedException e) {
			Log.trace(e);
		}
	}

}
