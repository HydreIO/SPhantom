package sceat.domain.common.utils;

public interface IRegistrable<T> {
	T register();

	T unregister();
}
