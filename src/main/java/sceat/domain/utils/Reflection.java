package sceat.domain.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import sceat.domain.utils.bistream.BiStream;

public class Reflection {

	public static boolean debug = false;

	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		return (Constructor<T>) Arrays.stream(clazz.getConstructors()).parallel().filter((c) -> compare(c.getParameterTypes(), parameterTypes)).findFirst().orElse(null);
	}

	public static <T> Constructor<T> getConstructor(ParamClass<T> type, Class<?>... parameterTypes) {
		return getConstructor(type.getClazz(), parameterTypes);
	}

	public static <T> T newInstance(Class<T> clazz, Object... arguments) {
		try {
			return getConstructor(clazz, getClass(arguments)).newInstance(arguments);
		} catch (InstantiationException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}

	public static <T> T newInstance(ParamClass<T> type, Object... arguments) {
		return newInstance(type.getClazz(), arguments);
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
		if (clazz == null) return null;
		Method method = Arrays.stream(clazz.getDeclaredMethods()).parallel()
				.filter((m) -> m.getName().equals(methodName) && compare(m.getReturnType(), returnType) && compare(m.getParameterTypes(), parameterTypes)).parallel().findFirst()
				.orElse(getMethod(clazz.getSuperclass(), methodName, returnType, parameterTypes));
		if (method != null) method.setAccessible(true);
		return method;
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return getMethod(clazz, methodName, Object.class, parameterTypes);
	}

	public static <T> T invoke(Object instance, String methodName, Class<T> returnType, Object... arguments) {
		try {
			return returnType.cast(getMethod(instance.getClass(), methodName, returnType, getClass(arguments)).invoke(instance, arguments));
		} catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}

	public static <T> T invoke(Object instance, String methodName, ParamClass<T> returnType, Object... arguments) {
		return invoke(instance, methodName, returnType.getClazz(), arguments);
	}

	public static Object invoke(Object instance, String methodName, Object... arguments) {
		return invoke(instance, methodName, Object.class, arguments);
	}

	public static Field getField(Class<?> clazz, String fieldName, Class<?> fieldType) {
		if (clazz == null) return null;
		Field field = Arrays.stream(clazz.getDeclaredFields()).parallel().filter((f) -> f.getName().equals(fieldName) && compare(f.getType(), fieldType)).findFirst()
				.orElse(getField(clazz.getSuperclass(), fieldName, fieldType));
		if (field != null) field.setAccessible(true);
		return field;
	}

	public static <T> T get(Class<?> fieldClass, String fieldName, Class<T> fieldType) {
		try {
			return fieldType.cast(getField(fieldClass, fieldName, fieldType).get(null));
		} catch (IllegalAccessException | NullPointerException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}

	public static <T> T get(Class<?> fieldClass, String fieldName, ParamClass<T> fieldType) {
		return get(fieldClass, fieldName, fieldType.getClazz());
	}

	public static <T> T get(Object instance, String fieldName, Class<T> fieldType) {
		try {
			return fieldType.cast(getField(instance.getClass(), fieldName, fieldType).get(instance));
		} catch (IllegalAccessException | NullPointerException e) {
			if (debug) e.printStackTrace();
			return null;
		}
	}

	public static <T> T get(Object instance, String fieldName, ParamClass<T> fieldType) {
		return get(instance, fieldName, fieldType.getClazz());
	}

	public static Object get(Object instance, String fieldName) {
		return get(instance, fieldName, Object.class);
	}

	public static void set(Class<?> fieldClass, String fieldName, Object value) {
		try {
			getField(fieldClass, fieldName, value.getClass()).set(null, value);
		} catch (IllegalAccessException | NullPointerException e) {
			if (debug) e.printStackTrace();
		}
	}

	public static void set(Object instance, String fieldName, Object value) {
		try {
			getField(instance.getClass(), fieldName, value.getClass()).set(instance, value);
		} catch (IllegalAccessException | NullPointerException e) {
			if (debug) e.printStackTrace();
		}
	}

	public static Class<?>[] getClass(Object... objs) {
		return (Class<?>[]) Arrays.stream(objs).map((o) -> o == null ? Object.class : o.getClass()).toArray(Class[]::new);
	}

	public static boolean compare(Class<?> clazz, Class<?> clazz2) {
		return clazz2.equals(Object.class) || clazz.isAssignableFrom(clazz2) || clazz2.isAssignableFrom(clazz) || getPrimitive(clazz).isAssignableFrom(clazz2)
				|| clazz.isAssignableFrom(getPrimitive(clazz2));
	}

	public static boolean compare(Class<?>[] clazz, Class<?>[] clazz2) {
		return clazz.length == clazz2.length && BiStream.wrap(clazz, clazz2).filter(Reflection::compare).count() == clazz.length;
	}

	public static Class<?> getPrimitive(Class<?> clazz) {
		if (clazz.equals(Byte.class)) return byte.class;
		if (clazz.equals(Short.class)) return boolean.class;
		if (clazz.equals(Integer.class)) return int.class;
		if (clazz.equals(Long.class)) return long.class;
		if (clazz.equals(Character.class)) return char.class;
		if (clazz.equals(Float.class)) return float.class;
		if (clazz.equals(Double.class)) return double.class;
		if (clazz.equals(Boolean.class)) return boolean.class;
		return clazz;
	}
}