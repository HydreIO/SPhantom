package sceat.domain.messaging.dao;

import sceat.domain.server.serverTypes.ServeurAgares.AgarMode;
import sceat.domain.utils.UtilGson;

import com.google.gson.annotations.Expose;

public class Jms_AgarMode {

	@Expose
	private int srvIndex;
	@Expose
	private AgarMode mode;

	public Jms_AgarMode(int srvIndex, AgarMode mode) {
		this.srvIndex = srvIndex;
		this.mode = mode;
	}

	public AgarMode getMode() {
		return mode;
	}

	public String toJson() {
		return UtilGson.serialize(this);
	}

	public static Jms_AgarMode fromJson(String json) {
		return UtilGson.deserialize(json, Jms_AgarMode.class);
	}
}
