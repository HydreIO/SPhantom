package sceat.domain.forkupdate;

import java.lang.reflect.Method;
import java.util.HashSet;

public class ForkUpdateListener {

	private static void addToHashMapList(ForkUpdateType type, Method m, Object o) {
		HashSet<Method> methods = new HashSet<Method>();
		if (ForkUpdate.methods.get(type) != null) methods = ForkUpdate.methods.get(type);
		methods.add(m);
		ForkUpdate.methods.put(type, methods);
		ForkUpdate.listener.put(m, o);
	}

	public static void register(IForkUpdade listener) {
		for (Method method : listener.getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(ForkUpdateHandler.class)) {
				ForkUpdateHandler annot = method.getAnnotation(ForkUpdateHandler.class);
				addToHashMapList(annot.rate(), method, listener);
			}
		}
	}

}
