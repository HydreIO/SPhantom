package sceat.domain.protocol.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import sceat.Main;
import sceat.SPhantom;
import sceat.domain.protocol.MessagesType;
import sceat.domain.protocol.Security;

public abstract class PacketPhantom {
	
	

	public static void registerPkts() {
		SPhantom.print("Initialising packets...");
		try {
			registerPacket((byte) 1, PacketPhantomServerInfo.class);
			registerPacket((byte) 2, PacketPhantomHeartBeat.class);
			registerPacket((byte) 3, PacketPhantomPlayer.class);
			registerPacket((byte) 4, PacketPhantomBootServer.class);
			registerPacket((byte) 5, PacketPhantomSymbiote.class);
			registerPacket((byte) 6, PacketPhantomDestroyInstance.class);
			registerPacket((byte) 7, PacketPhantomReduceServer.class);
			registerPacket((byte) 8, PacketPhantomServerCrash.class);
		} catch (PacketIdAlrealyUsedException e) {
			Main.printStackTrace(e);
		}
	}

	public static final int MAX_PACKET_SIZE = 1024;

	private static final HashMap<Byte, Class<? extends PacketPhantom>> packets = new HashMap<>();
	private Security secu;

	private static void registerPacket(byte id, Class<? extends PacketPhantom> packet) throws PacketIdAlrealyUsedException {
		if (packets.containsKey(id)) throw new PacketIdAlrealyUsedException(id, packets.get(id));
		packets.put(id, packet);
		SPhantom.print(packet.getName().substring(packet.getName().lastIndexOf('.') + 1) + "[" + id + "] Registered");
	}

	/**
	 * méthode apellé pendant pendant la serialization
	 */
	public void encodeSecurity() {
		writeString(getSecu().getSerial());
		writeString(getSecu().getSecurity());
	}

	/**
	 * méthode apellé pendant pendant la deserialization
	 */
	public void decodeSecurity() {
		this.secu = new Security(readString(), readString());
	}

	@FunctionalInterface
	public interface Deserializer<T> {
		T deserialize();
	}

	@FunctionalInterface
	public interface Serializer<T> {
		void serialize(T value);
	}

	public void writePacket(PacketPhantom pkt, Serializer<PacketPhantom> pktS) {
		pktS.serialize(pkt);
	}

	public <P extends PacketPhantom> P readPacket(Deserializer<P> pkt) {
		return pkt.deserialize();
	}

	public <U, V> Map<U, V> readMap(Deserializer<U> ud, Deserializer<V> vd) {
		int size = readInt();
		Map<U, V> map = new HashMap<>();
		for (int i = 0; i < size; i++)
			map.put(ud.deserialize(), vd.deserialize());
		return map;
	}

	public <U, V> void writeMap(Map<U, V> map, Serializer<U> ud, Serializer<V> vd) {
		writeInt(map.size());
		for (Map.Entry<U, V> e : map.entrySet()) {
			ud.serialize(e.getKey());
			vd.serialize(e.getValue());
		}
	}

	public <C extends Collection<T>, T> C readCollection(C collection, Deserializer<T> deserializer) {
		int size = readInt();
		for (int i = 0; i < size; i++)
			collection.add(deserializer.deserialize());
		return collection;
	}

	public <T> void writeCollection(Collection<T> collection, Serializer<T> serializer) {
		writeInt(collection.size());
		for (T t : collection)
			serializer.serialize(t);

	}

	public static Byte getPacketId(Class<? extends PacketPhantom> packet) {
		for (Map.Entry<Byte, Class<? extends PacketPhantom>> entry : packets.entrySet()) {
			if (entry.getValue().equals(packet)) return entry.getKey();
		}
		return null;
	}

	public static Byte getPacketId(PacketPhantom packet) {
		return getPacketId(packet.getClass());
	}

	public static Class<? extends PacketPhantom> getPacket(byte id) {
		return packets.get(id);
	}

	private byte[] buffer = new byte[MAX_PACKET_SIZE];
	private volatile int writePos = 1;
	private volatile int readPos = 1;

	protected PacketPhantom() {
		buffer[0] = getPacketId(this);
	}

	/**
	 * Methode apell� dans le packetSender avant d'envoyer le packet
	 * 
	 * @param secu
	 */
	public void setSecu(Security secu) {
		this.secu = secu;
	}

	/**
	 * Get the read position
	 * 
	 * @return write position
	 */
	public int getReadPos() {
		return readPos;
	}

	/**
	 * Get the write position
	 * 
	 * @return write position
	 */
	public int getWritePos() {
		return writePos;
	}

	/**
	 * Set the read position
	 * 
	 * @param read
	 *            position
	 */
	public void setReadPos(int readPos) {
		this.readPos = readPos;
	}

	/**
	 * Set the write position
	 * 
	 * @param write
	 *            position
	 */
	public void setWritePos(int writePos) {
		this.writePos = writePos;
	}

	@SuppressWarnings("unchecked")
	public <T extends PacketPhantom> T serialize() {
		encodeSecurity();
		serialize_();
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends PacketPhantom> T deserialize() {
		decodeSecurity();
		deserialize_();
		return (T) this;
	}

	protected abstract void serialize_();

	protected abstract void deserialize_();

	/**
	 * Method called after deserialization
	 * 
	 * @param type
	 */
	public abstract void handleData(MessagesType type);

	// ////////////////////////////////////////
	// Read //
	// //////////////////////////////////////

	public final byte readByte() {
		return buffer[readPos++];
	}

	public final byte[] readBytes(int size) {
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; i++)
			bytes[i] = readByte();
		return bytes;
	}

	public final boolean readBoolean() {
		return readByte() != 0;
	}

	public final short readShort() {
		return (short) (((readByte() << 8) & 0xFF00) | (readByte() & 0xFF));
	}

	public final int readInt() {
		return (readShort() << 16) | readShort() & 0xFFFF;
	}

	public final long readLong() {
		return ((long) readInt() << 32) | readInt() & 0xFFFFFFFFL;
	}

	public final float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	public final double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	public final String readString() {
		return new String(readBytes(readShort()));
	}

	public final String readLittleString() {
		return new String(readBytes(readByte()));
	}

	public final String readLongString() {
		return new String(readBytes(readInt()));
	}

	public final Object readObject() {
		try {
			byte[] bytes = readBytes(readShort());
			ByteArrayInputStream input = new ByteArrayInputStream(bytes);
			ObjectInputStream oInput = new ObjectInputStream(input);
			Object o = oInput.readObject();
			oInput.close();
			input.close();
			return o;
		} catch (IOException | ClassNotFoundException e) {
			Main.printStackTrace(e);
		}
		return null;
	}

	// ////////////////////////////////////////
	// Write //
	// //////////////////////////////////////

	public final PacketPhantom writeByte(byte b) {
		buffer[writePos++] = b;
		return this;
	}

	public final PacketPhantom writeBytes(byte[] bytes) {
		for (byte b : bytes)
			writeByte(b);
		return this;
	}

	public final PacketPhantom writeBoolean(boolean b) {
		return writeByte(b ? (byte) 1 : (byte) 0);
	}

	public final PacketPhantom writeShort(short s) {
		writeByte((byte) (s >> 8));
		writeByte((byte) s);
		return this;
	}

	public final PacketPhantom writeInt(int i) {
		writeShort((short) (i >> 16));
		writeShort((short) i);
		return this;
	}

	public final PacketPhantom writeLong(long l) {
		writeInt((int) (l >> 32));
		writeInt((int) l);
		return this;
	}

	public final PacketPhantom writeFloat(float f) {
		return writeInt(Float.floatToIntBits(f));
	}

	public final PacketPhantom writeDouble(double d) {
		return writeLong(Double.doubleToLongBits(d));
	}

	public final PacketPhantom writeString(String s) {
		writeShort((short) s.length());
		writeBytes(s.getBytes());
		return this;
	}

	public final PacketPhantom writeLittleString(String s) {
		writeByte((byte) s.length());
		writeBytes(s.getBytes());
		return this;
	}

	public final PacketPhantom writeLongString(String s) {
		writeInt(s.length());
		writeBytes(s.getBytes());
		return this;
	}

	public final PacketPhantom writeObject(Object o) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutputStream oStream = new ObjectOutputStream(stream);
			oStream.writeObject(o);
			oStream.close();
			byte[] bytes = stream.toByteArray();
			stream.close();
			writeShort((short) bytes.length);
			writeBytes(bytes);
		} catch (IOException e) {
			Main.printStackTrace(e);
		}
		return this;
	}

	public byte[] toByteArray() {
		return buffer;
	}

	public byte getId() {
		return buffer[0];
	}

	@SuppressWarnings("unchecked")
	public static <P extends PacketPhantom> P fromByteArray(byte[] bytes) throws IllegalAccessException, InstantiationException, PacketNotRegistredException {
		if (!packets.containsKey(bytes[0])) throw new PacketNotRegistredException(bytes[0]);
		PacketPhantom p = getPacket(bytes[0]).newInstance();
		p.buffer = bytes;
		return (P) p;
	}

	public static class PacketNotRegistredException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7749347832074629409L;

		public PacketNotRegistredException(byte id) {
			super("The packet with id " + id + " is not registred");
		}
	}

	public static class PacketIdAlrealyUsedException extends PacketException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6283626920744238599L;
		private final byte id;

		public PacketIdAlrealyUsedException(byte id, Class<? extends PacketPhantom> packet) {
			super(packet, "Id :" + id + " is alrealy used by " + packet.getName());
			this.id = id;
		}

		public byte getId() {
			return id;
		}
	}

	public static class PacketException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6893575558054311557L;
		private final Class<? extends PacketPhantom> packet;

		PacketException(Class<? extends PacketPhantom> packet, String s) {
			super(s);
			this.packet = packet;
		}

		public Class<? extends PacketPhantom> getPacket() {
			return packet;
		}
	}

	/**
	 * Interne a sphantom
	 * 
	 * @return true si c'est cet instance de sphantom qui � envoy� ce packet
	 */
	public boolean cameFromLocal() {
		return getSecu().correspond(SPhantom.getInstance().getSecurity());
	}

	public Security getSecu() {
		return secu;
	}

}
