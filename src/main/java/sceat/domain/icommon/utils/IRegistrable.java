package sceat.domain.icommon.utils;

public interface IRegistrable<T> {
	T register();

	T unregister();
}
