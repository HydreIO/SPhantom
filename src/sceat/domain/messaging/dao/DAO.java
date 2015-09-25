package sceat.domain.messaging.dao;

import sceat.domain.utils.UtilGson;

public class DAO {

	public <T> String toJson(T ty) {
		return UtilGson.serialize(ty);
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String json, T clazz) {
		return (T) UtilGson.deserialize(json, clazz.getClass());
	}

}
