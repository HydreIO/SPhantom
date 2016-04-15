package sceat.domain.protocol.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import sceat.Main;
import sceat.SPhantom;

import com.jcraft.jsch.Packet;

public abstract class PacketPhantom {

	public static final int maxPacketSize = 512;
	private static final HashMap<Byte, Class<? extends PacketPhantom>> packets = new HashMap<>();

	private static void registerPacket(byte id, Class<? extends PacketPhantom> packet) throws PacketIdAlrealyUsedException {
		if (packets.containsKey(id)) throw new PacketIdAlrealyUsedException(id, packets.get(id));
		packets.put(id, packet);
		SPhantom.print(packet.getCanonicalName() + "[" + id + "] Registered");
	}

	public static void init() {
		SPhantom.print("Initialising packets...");
		registerPacket(PacketPhantomPlayer.ID, PacketPhantomPlayer.class);
		registerPacket(PacketPhantomServerInfo.ID, PacketPhantomServerInfo.class);
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

	private byte[] buffer = new byte[maxPacketSize];
	private volatile int writePos = 1;
	private volatile int readPos = 1;

	protected PacketPhantom() {
		buffer[0] = getPacketId(this);
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

	public abstract void serialize();

	public abstract void deserialize();

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

	public static Packet fromByteArray(byte[] bytes) throws IllegalAccessException, InstantiationException, PacketNotRegistredException {
		if (!packets.containsKey(bytes[0])) throw new PacketNotRegistredException(bytes[0]);
		PacketPhantom p = getPacket(bytes[0]).newInstance();
		p.buffer = bytes;
		return p;
	}

}
