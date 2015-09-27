package sceat.domain.messaging.dao;

import sceat.domain.utils.UtilGson;

public class DAO {

	public <T> String toJson(Object ins) {
		return UtilGson.serialize(ins);
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String json, T clazz) {
		return (T) UtilGson.deserialize(json, clazz.getClass());
	}

}
