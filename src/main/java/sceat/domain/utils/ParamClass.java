package sceat.domain.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ParamClass<T> {
	private final Type type;

	protected ParamClass() {
		if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
			this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		} else this.type = null;

	}

	public final T newInstance(Object... args) {
		return Reflection.newInstance(getClazz(), args);
	}

	public final Type getType() {
		return this.type;
	}

	public final Class<T> getClazz() {
		return Types.<T> getClazz(type);
	}
}