package sceat.domain.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UtilGson {

	@SuppressWarnings("rawtypes")
	public static String getJsonFrom(Object instance, Class clazz) {
		return new GsonBuilder().create().toJson(instance, clazz);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object getInstanceFromJson(String json, Class clazz) {
		return new GsonBuilder().create().fromJson(json, clazz);
	}

	public static String getStringFromJson(String json) {
		return new GsonBuilder().create().fromJson(json, String.class);
	}

	static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	public static String serialize(Object instance) {
		return gson.toJson(instance);
	}

	public static <T> T deserialize(String json, Class<T> clazz) {
		return (T) gson.fromJson(json, clazz);
	}

}
