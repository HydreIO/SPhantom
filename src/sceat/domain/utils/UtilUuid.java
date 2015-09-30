package sceat.domain.utils;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import sceat.SPhantom;

import com.google.common.collect.ImmutableList;

public class UtilUuid implements Callable<Map<String, UUID>> {

	private static Map<UUID, String> plist = new HashMap<UUID, String>();
	private static Map<String, UUID> uidlist = new HashMap<String, UUID>();

	private static final double PROFILES_PER_REQUEST = 100;
	private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
	private static final String PROFILE_URL2 = "https://sessionserver.mojang.com/session/minecraft/profile/";
	private final static JSONParser jsonParser = new JSONParser();
	private final List<String> names;
	private final boolean rateLimiting;

	public UtilUuid(List<String> names, boolean rateLimiting) {
		this.names = ImmutableList.copyOf(names);
		this.rateLimiting = rateLimiting;
	}

	public UtilUuid(List<String> names) {
		this(names, true);
	}

	public Map<String, UUID> call() throws Exception {
		Map<String, UUID> uuidMap = new HashMap<String, UUID>();
		int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
		for (int i = 0; i < requests; i++) {
			HttpURLConnection connection = createConnection();
			String body = JSONArray.toJSONString(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
			writeBody(connection, body);
			JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
			for (Object profile : array) {
				JSONObject jsonProfile = (JSONObject) profile;
				String id = (String) jsonProfile.get("id");
				String name = (String) jsonProfile.get("name");
				UUID uuid = UtilUuid.getUUID(id);
				uuidMap.put(name, uuid);
			}
			if (rateLimiting && i != requests - 1) {
				Thread.sleep(100L);
			}
		}
		return uuidMap;
	}

	public static String getName(UUID uuid) {
		if (plist.containsKey(uuid)) { return plist.get(uuid); }
		HttpURLConnection connection;
		JSONObject response = null;
		try {
			connection = createConnection2(uuid);
			response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
		} catch (Exception e) {
			SPhantom.print("Erreur de r�cuperation du name pour l'uuid " + uuid.toString());
			return "Unkhnow";
		}
		if (response == null) return null;
		String name = (String) response.get("name");
		plist.put(uuid, name);
		return name;
	}

	private static void writeBody(HttpURLConnection connection, String body) throws Exception {
		OutputStream stream = connection.getOutputStream();
		stream.write(body.getBytes());
		stream.flush();
		stream.close();
	}

	private static HttpURLConnection createConnection() throws Exception {
		URL url = new URL(PROFILE_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		return connection;
	}

	private static HttpURLConnection createConnection2(UUID uuid) throws Exception {
		URL url = new URL(PROFILE_URL2 + uuid.toString().replace("-", ""));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		return connection;
	}

	private static UUID getUUID(String id) {
		return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
	}

	public static byte[] toBytes(UUID uuid) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
		byteBuffer.putLong(uuid.getMostSignificantBits());
		byteBuffer.putLong(uuid.getLeastSignificantBits());
		return byteBuffer.array();
	}

	public static UUID fromBytes(byte[] array) {
		if (array.length != 16) { throw new IllegalArgumentException("Illegal byte array length: " + array.length); }
		ByteBuffer byteBuffer = ByteBuffer.wrap(array);
		long mostSignificant = byteBuffer.getLong();
		long leastSignificant = byteBuffer.getLong();
		return new UUID(mostSignificant, leastSignificant);
	}

	public static UUID getUUIDOf(String name) {
		if (uidlist.containsKey(name)) { return uidlist.get(name); }
		UUID ui = null;
		try {
			ui = new UtilUuid(Arrays.asList(name)).call().get(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		uidlist.put(name, ui);
		return ui;
	}

	public static void removePlayerFromMap(String name) {
		if (uidlist.containsKey(name)) {
			uidlist.remove(name);
		}
		if (plist.containsValue(name)) {
			for (UUID uid : plist.keySet()) {
				if (plist.get(uid).equals(name)) {
					plist.remove(uid);
					break;
				}
			}
		}
	}

}