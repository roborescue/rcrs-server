package rescuecore2.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import rescuecore2.messages.Message;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A bunch of useful tools for encoding and decoding things like integers.
 */
public final class EncodingTools {
  /** The size of an INT_32 in bytes. */
  public static final int INT_32_SIZE = 4;

  /** Charset for encoding/decoding strings. Should always be UTF-8 */
  private static final Charset CHARSET = Charset.forName("UTF-8");

  /**
   * Private constructor: this is a utility class.
   */
  private EncodingTools() {
  }

  /**
   * Turn off the checkstyle test for magic numbers since we use a lot of them
   * here
   */
  /** CHECKSTYLE:OFF:MagicNumber */

  /**
   * Write a 32-bit integer to an OutputStream, big-endian style.
   *
   * @param i   The integer to write.
   * @param out The OutputStream to write it to.
   * @throws IOException If the OutputStream blows up.
   */
  public static void writeInt32(int i, OutputStream out) throws IOException {
    // Most significant byte first
    out.write((byte) (i >> 24) & 0xFF);
    out.write((byte) (i >> 16) & 0xFF);
    out.write((byte) (i >> 8) & 0xFF);
    out.write((byte) i & 0xFF);
  }

  /**
   * Write a 32-bit integer to a DataOutput, big-endian style.
   *
   * @param i   The integer to write.
   * @param out The DataOutput to write it to.
   * @throws IOException If the DataOutput blows up.
   */
  public static void writeInt32(int i, DataOutput out) throws IOException {
    // DataOutput writes big-endian
    out.write(i);
  }

  /**
   * Write a 32-bit integer to a byte array, big-endian style.
   *
   * @param i      The integer to write.
   * @param out    The buffer to write it to.
   * @param offset Where in the buffer to write it.
   */
  public static void writeInt32(int i, byte[] out, int offset) {
    // Most significant byte first
    out[offset] = (byte) ((i >> 24) & 0xFF);
    out[offset + 1] = (byte) ((i >> 16) & 0xFF);
    out[offset + 2] = (byte) ((i >> 8) & 0xFF);
    out[offset + 3] = (byte) (i & 0xFF);
  }

  /**
   * Read a 32-bit integer from an input stream, big-endian style.
   *
   * @param in The InputStream to read from.
   * @return The next big-endian, 32-bit integer in the stream.
   * @throws IOException  If the InputStream blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static int readInt32(InputStream in) throws IOException {
    int first = in.read();
    if (first == -1) {
      throw new EOFException("Broken input pipe. Read 0 bytes of 4.");
    }
    int second = in.read();
    if (second == -1) {
      throw new EOFException("Broken input pipe. Read 1 bytes of 4.");
    }
    int third = in.read();
    if (third == -1) {
      throw new EOFException("Broken input pipe. Read 2 bytes of 4.");
    }
    int fourth = in.read();
    if (fourth == -1) {
      throw new EOFException("Broken input pipe. Read 3 bytes of 4.");
    }
    return (first << 24) | (second << 16) | (third << 8) | fourth;
  }

  /**
   * Read a 32-bit integer from a DataInput.
   *
   * @param in The DataInput to read from.
   * @return The next big-endian, 32-bit integer in the stream.
   * @throws IOException  If the DataInput blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static int readInt32(DataInput in) throws IOException {
    return in.readInt();
  }

  /**
   * Read a 32-bit integer from an input stream, little-endian style.
   *
   * @param in The InputStream to read from.
   * @return The next little-endian, 32-bit integer in the stream.
   * @throws IOException  If the InputStream blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static int readInt32LE(InputStream in) throws IOException {
    int first = in.read();
    if (first == -1) {
      throw new EOFException("Broken input pipe. Read 0 bytes of 4.");
    }
    int second = in.read();
    if (second == -1) {
      throw new EOFException("Broken input pipe. Read 1 bytes of 4.");
    }
    int third = in.read();
    if (third == -1) {
      throw new EOFException("Broken input pipe. Read 2 bytes of 4.");
    }
    int fourth = in.read();
    if (fourth == -1) {
      throw new EOFException("Broken input pipe. Read 3 bytes of 4.");
    }
    return (fourth << 24) | (third << 16) | (second << 8) | first;
  }

  /**
   * Read a 32-bit integer from a byte array, big-endian style.
   *
   * @param in     The buffer to read from.
   * @param offset Where to begin reading.
   * @return The next big-endian, 32-bit integer in the buffer.
   */
  public static int readInt32(byte[] in, int offset) {
    return (in[offset] << 24) | (in[offset + 1] << 16) | (in[offset + 2] << 8) | (in[offset + 3]);
  }

  /**
   * Read a 32-bit integer from a byte array, big-endian style. This is equivalent
   * to calling {@link #readInt32(byte[], int) readInt32(in, 0)}.
   *
   * @param in The buffer to read from.
   * @return The first big-endian, 32-bit integer in the buffer.
   */
  public static int readInt32(byte[] in) {
    return readInt32(in, 0);
  }

  /**
   * Write a String to an OutputStream. Strings are always in UTF-8.
   *
   * @param s   The String to write.
   * @param out The OutputStream to write to.
   * @throws IOException If the OutputStream blows up.
   */
  public static void writeString(String s, OutputStream out) throws IOException {
    byte[] bytes = s.getBytes(CHARSET);
    writeInt32(bytes.length, out);
    out.write(bytes);
  }

  /**
   * Write a String to a DataOutput. Strings are always in UTF-8.
   *
   * @param s   The String to write.
   * @param out The DataOutput to write to.
   * @throws IOException If the DataOutput blows up.
   */
  public static void writeString(String s, DataOutput out) throws IOException {
    byte[] bytes = s.getBytes(CHARSET);
    writeInt32(bytes.length, out);
    out.write(bytes);
  }

  /**
   * Write a String to a byte array. Strings are always in UTF-8.
   *
   * @param s      The String to write.
   * @param out    The byte array to write to. Make sure it's big enough!
   * @param offset The index to start writing from.
   */
  public static void writeString(String s, byte[] out, int offset) {
    byte[] bytes = s.getBytes(CHARSET);
    writeInt32(bytes.length, out, offset);
    System.arraycopy(bytes, 0, out, offset + 4, bytes.length);
  }

  /**
   * Read a String from an InputStream.
   *
   * @param in The InputStream to read.
   * @return The string that was read.
   * @throws IOException  If the InputStream blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static String readString(InputStream in) throws IOException {
    int length = readInt32(in);
    byte[] buffer = new byte[length];
    int count = 0;
    while (count < length) {
      int read = in.read(buffer, count, length - count);
      if (read == -1) {
        throw new EOFException("Broken input pipe. Read " + count + " bytes of " + length + ".");
      }
      count += read;
    }
    return new String(buffer, CHARSET);
  }

  /**
   * Read a String from a DataInput.
   *
   * @param in The DataInput to read.
   * @return The string that was read.
   * @throws IOException  If the DataInput blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static String readString(DataInput in) throws IOException {
    int length = readInt32(in);
    byte[] buffer = new byte[length];
    in.readFully(buffer);
    return new String(buffer, CHARSET);
  }

  /**
   * Read a String from a byte array. This is equivalent to calling
   * {@link #readString(byte[], int) readString(in, 0)}.
   *
   * @param in The byte array to read.
   * @return The string that was read.
   */
  public static String readString(byte[] in) {
    return readString(in, 0);
  }

  /**
   * Read a String from a byte array.
   *
   * @param in     The byte array to read.
   * @param offset The index in the array to read from.
   * @return The string that was read.
   */
  public static String readString(byte[] in, int offset) {
    int length = readInt32(in, offset);
    byte[] buffer = new byte[length];
    System.arraycopy(in, offset + 4, buffer, 0, length);
    return new String(buffer, CHARSET);
  }

  /**
   * Read a fixed number of bytes from an InputStream into an array.
   *
   * @param size The number of bytes to read.
   * @param in   The InputStream to read from.
   * @return A new byte array containing the bytes.
   * @throws IOException If the read operation fails.
   */
  public static byte[] readBytes(int size, InputStream in) throws IOException {
    byte[] buffer = new byte[size];
    int total = 0;
    while (total < size) {
      int read = in.read(buffer, total, size - total);
      if (read == -1) {
        throw new EOFException("Broken input pipe. Read " + total + " bytes of " + size + ".");
      }
      total += read;
    }
    return buffer;
  }

  /**
   * Read a fixed number of bytes from a DataInput into an array.
   *
   * @param size The number of bytes to read.
   * @param in   The DataInput to read from.
   * @return A new byte array containing the bytes.
   * @throws IOException If the read operation fails.
   */
  public static byte[] readBytes(int size, DataInput in) throws IOException {
    byte[] buffer = new byte[size];
    in.readFully(buffer);
    return buffer;
  }

  /**
   * Write a double to an OutputStream.
   *
   * @param d   The double to write.
   * @param out The OutputStream to write it to.
   * @throws IOException If the OutputStream blows up.
   */
  public static void writeDouble(double d, OutputStream out) throws IOException {
    long bits = Double.doubleToLongBits(d);
    out.write((byte) (bits >> 56) & 0xFF);
    out.write((byte) (bits >> 48) & 0xFF);
    out.write((byte) (bits >> 40) & 0xFF);
    out.write((byte) (bits >> 32) & 0xFF);
    out.write((byte) (bits >> 24) & 0xFF);
    out.write((byte) (bits >> 16) & 0xFF);
    out.write((byte) (bits >> 8) & 0xFF);
    out.write((byte) bits & 0xFF);
  }

  /**
   * Write a double to a DataOutput.
   *
   * @param d   The double to write.
   * @param out The DataOutput to write it to.
   * @throws IOException If the DataOutput blows up.
   */
  public static void writeDouble(double d, DataOutput out) throws IOException {
    out.writeDouble(d);
  }

  /**
   * Write a double to a byte array.
   *
   * @param d      The double to write.
   * @param out    The buffer to write it to.
   * @param offset Where in the buffer to write it.
   */
  public static void writeDouble(double d, byte[] out, int offset) {
    long bits = Double.doubleToLongBits(d);
    out[offset + 0] = (byte) ((bits >> 56) & 0xFF);
    out[offset + 1] = (byte) ((bits >> 48) & 0xFF);
    out[offset + 2] = (byte) ((bits >> 40) & 0xFF);
    out[offset + 3] = (byte) ((bits >> 32) & 0xFF);
    out[offset + 4] = (byte) ((bits >> 24) & 0xFF);
    out[offset + 5] = (byte) ((bits >> 16) & 0xFF);
    out[offset + 6] = (byte) ((bits >> 8) & 0xFF);
    out[offset + 7] = (byte) (bits & 0xFF);
  }

  /**
   * Read a double from an input stream.
   *
   * @param in The InputStream to read from.
   * @return The next double in the stream.
   * @throws IOException  If the InputStream blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static double readDouble(InputStream in) throws IOException {
    long[] data = new long[8];
    for (int i = 0; i < data.length; ++i) {
      data[i] = in.read();
      if (data[i] == -1) {
        throw new EOFException("Broken input pipe. Read " + i + " bytes of 8.");
      }
    }
    long result = data[0] << 56 | data[1] << 48 | data[2] << 40 | data[3] << 32 | data[4] << 24 | data[5] << 16
        | data[6] << 8 | data[7];
    return Double.longBitsToDouble(result);
  }

  /**
   * Read a double from a DataInput.
   *
   * @param in The DataInput to read from.
   * @return The next double in the stream.
   * @throws IOException  If the DataInput blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static double readDouble(DataInput in) throws IOException {
    return in.readDouble();
  }

  /**
   * Read a double from a byte array.
   *
   * @param in     The buffer to read from.
   * @param offset Where to begin reading.
   * @return The next double in the buffer.
   */
  public static double readDouble(byte[] in, int offset) {
    long[] parts = new long[8];
    for (int i = 0; i < 8; ++i) {
      parts[i] = in[offset + i];
    }
    long result = parts[0] << 56 | parts[1] << 48 | parts[2] << 40 | parts[3] << 32 | parts[4] << 24 | parts[5] << 16
        | parts[6] << 8 | parts[7];
    return Double.longBitsToDouble(result);
  }

  /**
   * Read a double from a byte array. This is equivalent to calling
   * {@link #readDouble(byte[], int) readDouble(in, 0)}.
   *
   * @param in The buffer to read from.
   * @return The first double in the buffer.
   */
  public static double readDouble(byte[] in) {
    return readDouble(in, 0);
  }

  /**
   * Write a boolean to an OutputStream.
   *
   * @param b   The boolean to write.
   * @param out The OutputStream to write it to.
   * @throws IOException If the OutputStream blows up.
   */
  public static void writeBoolean(boolean b, OutputStream out) throws IOException {
    out.write(b ? 1 : 0);
  }

  /**
   * Write a boolean to a DataOutput.
   *
   * @param b   The boolean to write.
   * @param out The DataOutput to write it to.
   * @throws IOException If the DataOutput blows up.
   */
  public static void writeBoolean(boolean b, DataOutput out) throws IOException {
    out.writeBoolean(b);
  }

  /**
   * Write a boolean to a byte array.
   *
   * @param b      The boolean to write.
   * @param out    The buffer to write it to.
   * @param offset Where in the buffer to write it.
   */
  public static void writeBoolean(boolean b, byte[] out, int offset) {
    out[offset] = (byte) (b ? 1 : 0);
  }

  /**
   * Read a boolean from an input stream.
   *
   * @param in The InputStream to read from.
   * @return The next boolean in the stream.
   * @throws IOException  If the InputStream blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static boolean readBoolean(InputStream in) throws IOException {
    int b = in.read();
    return b == 1;
  }

  /**
   * Read a boolean from a DataInput.
   *
   * @param in The DataInput to read from.
   * @return The next boolean in the stream.
   * @throws IOException  If the DataInput blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static boolean readBoolean(DataInput in) throws IOException {
    return in.readBoolean();
  }

  /**
   * Read a boolean from a byte array.
   *
   * @param in     The buffer to read from.
   * @param offset Where to begin reading.
   * @return The next boolean in the buffer.
   */
  public static boolean readBoolean(byte[] in, int offset) {
    return in[offset] == 1;
  }

  /**
   * Read a boolean from a byte array. This is equivalent to calling
   * {@link #readBoolean(byte[], int) readBoolean(in, 0)}.
   *
   * @param in The buffer to read from.
   * @return The first boolean in the buffer.
   */
  public static boolean readBoolean(byte[] in) {
    return readBoolean(in, 0);
  }

  /**
   * Call InputStream.skip until exactly <code>count</code> bytes have been
   * skipped. If InputStream.skip ever returns a negative number then an
   * EOFException is thrown.
   *
   * @param in    The InputStream to skip.
   * @param count The number of bytes to skip.
   * @throws IOException If the bytes cannot be skipped for some reason.
   */
  public static void reallySkip(InputStream in, long count) throws IOException {
    long done = 0;
    while (done < count) {
      long next = in.skip(count - done);
      if (next < 0) {
        throw new EOFException();
      }
      done += next;
    }
  }

  /**
   * Call DataInput.skip until exactly <code>count</code> bytes have been skipped.
   * If DataInput.skip ever returns a negative number then an EOFException is
   * thrown.
   *
   * @param in    The DataInput to skip.
   * @param count The number of bytes to skip.
   * @throws IOException If the bytes cannot be skipped for some reason.
   */
  public static void reallySkip(DataInput in, int count) throws IOException {
    int done = 0;
    while (done < count) {
      int next = in.skipBytes(count - done);
      if (next < 0) {
        throw new EOFException();
      }
      done += next;
    }
  }

  /**
   * Write an entity to a stream.
   *
   * @param e   The entity to write.
   * @param out The OutputStream to write to.
   * @throws IOException If there is a problem writing to the stream.
   */
  public static void writeEntity(Entity e, OutputStream out) throws IOException {
    // Type URN, entityID, size, content
    // Gather the content first
    ByteArrayOutputStream gather = new ByteArrayOutputStream();
    e.write(gather);
    byte[] bytes = gather.toByteArray();

    // Type URN
    writeString(Registry.getCurrentRegistry().toURN_Str(e.getURN()), out);
    // EntityID
    writeInt32(e.getID().getValue(), out);
    // Size
    writeInt32(bytes.length, out);
    // Content
    out.write(bytes);
  }

  /**
   * Write an entity to a DataOutput.
   *
   * @param e   The entity to write.
   * @param out The DataOutput to write to.
   * @throws IOException If there is a problem writing to the stream.
   */
  public static void writeEntity(Entity e, DataOutput out) throws IOException {
    // Type URN, entityID, size, content
    // Gather the content first
    ByteArrayOutputStream gather = new ByteArrayOutputStream();
    e.write(gather);
    byte[] bytes = gather.toByteArray();

    // Type URN
    writeString(Registry.getCurrentRegistry().toURN_Str(e.getURN()), out);
    // EntityID
    writeInt32(e.getID().getValue(), out);
    // Size
    writeInt32(bytes.length, out);
    // Content
    out.write(bytes);
  }

  /**
   * Read an entity from a stream.
   *
   * @param in The InputStream to read from.
   * @return A new Entity, or null if the entity URN is not recognised.
   * @throws IOException If there is a problem reading from the stream.
   */
  public static Entity readEntity(InputStream in) throws IOException {
    String urn = readString(in);
    if ("".equals(urn)) {
      return null;
    }
    int entityID = readInt32(in);
    int size = readInt32(in);
    byte[] content = readBytes(size, in);
    Entity result = Registry.getCurrentRegistry().createEntity(Registry.getCurrentRegistry().toURN_Id(urn),
        new EntityID(entityID));
    if (result != null) {
      result.read(new ByteArrayInputStream(content));
    }
    return result;
  }

  /**
   * Read an entity from a DataInput.
   *
   * @param in The DataInput to read from.
   * @return A new Entity, or null if the entity URN is not recognised.
   * @throws IOException If there is a problem reading from the stream.
   */
  public static Entity readEntity(DataInput in) throws IOException {
    String urn = readString(in);
    if ("".equals(urn)) {
      return null;
    }
    int entityID = readInt32(in);
    int size = readInt32(in);
    byte[] content = readBytes(size, in);
    Entity result = Registry.getCurrentRegistry().createEntity(Registry.getCurrentRegistry().toURN_Id(urn),
        new EntityID(entityID));
    if (result != null) {
      result.read(new ByteArrayInputStream(content));
    }
    return result;
  }

  /**
   * Write a property to a stream.
   *
   * @param p   The property to write.
   * @param out The OutputStream to write to.
   * @throws IOException If there is a problem writing to the stream.
   */
  public static void writeProperty(Property p, OutputStream out) throws IOException {
    // Type
    writeString(Registry.getCurrentRegistry().toURN_Str(p.getURN()), out);
    writeBoolean(p.isDefined(), out);
    if (p.isDefined()) {
      ByteArrayOutputStream gather = new ByteArrayOutputStream();
      p.write(gather);
      byte[] bytes = gather.toByteArray();
      // Size
      writeInt32(bytes.length, out);
      // Data
      out.write(bytes);
    }
  }

  /**
   * Write a property to a DataOutput.
   *
   * @param p   The property to write.
   * @param out The DataOutput to write to.
   * @throws IOException If there is a problem writing to the stream.
   */
  public static void writeProperty(Property p, DataOutput out) throws IOException {
    // Type
    writeString(Registry.getCurrentRegistry().toURN_Str(p.getURN()), out);
    writeBoolean(p.isDefined(), out);
    if (p.isDefined()) {
      ByteArrayOutputStream gather = new ByteArrayOutputStream();
      p.write(gather);
      byte[] bytes = gather.toByteArray();
      // Size
      writeInt32(bytes.length, out);
      // Data
      out.write(bytes);
    }
  }

  /**
   * Read a property from a stream.
   *
   * @param in The InputStream to read from.
   * @return A new Property, or null if the property URN is not recognised.
   * @throws IOException If there is a problem reading from the stream.
   */
  public static Property readProperty(InputStream in) throws IOException {
    String urn = readString(in);
    if ("".equals(urn)) {
      return null;
    }
    boolean defined = readBoolean(in);
    Property result = Registry.getCurrentRegistry().createProperty(Registry.getCurrentRegistry().toURN_Id(urn));
    if (defined) {
      int size = readInt32(in);
      byte[] content = readBytes(size, in);
      if (result != null) {
        result.read(new ByteArrayInputStream(content));
      }
    }
    return result;
  }

  /**
   * Read a property from a DataInput.
   *
   * @param in The DataInput to read from.
   * @return A new Property, or null if the property URN is not recognised.
   * @throws IOException If there is a problem reading from the stream.
   */
  public static Property readProperty(DataInput in) throws IOException {
    String urn = readString(in);
    if ("".equals(urn)) {
      return null;
    }
    boolean defined = readBoolean(in);
    Property result = Registry.getCurrentRegistry().createProperty(Registry.getCurrentRegistry().toURN_Id(urn));
    if (defined) {
      int size = readInt32(in);
      byte[] content = readBytes(size, in);
      if (result != null) {
        result.read(new ByteArrayInputStream(content));
      }
    }
    return result;
  }

  /**
   * Write a message to a stream.
   *
   * @param m   The message to write.
   * @param out The OutputStream to write to.
   * @throws IOException If there is a problem writing to the stream.
   */
  public static void writeMessage(Message m, OutputStream out) throws IOException {
    // Type URN, size, content
    // Gather the content first
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    m.write(bytes);
    byte[] content = bytes.toByteArray();

    // Type URN
    writeString(Registry.getCurrentRegistry().toURN_Str(m.getURN()), out);
    // Size
    writeInt32(content.length, out);
    // Content
    out.write(content);
  }

  /**
   * Write a message to a DataOutput.
   *
   * @param m   The message to write.
   * @param out The DataOutput to write to.
   * @throws IOException If there is a problem writing to the stream.
   */
  public static void writeMessage(Message m, DataOutput out) throws IOException {
    // Type URN, size, content
    // Gather the content first
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    m.write(bytes);
    byte[] content = bytes.toByteArray();

    // Type URN
    writeString(Registry.getCurrentRegistry().toURN_Str(m.getURN()), out);
    // Size
    writeInt32(content.length, out);
    // Content
    out.write(content);
  }

  /**
   * Read a message from a stream.
   *
   * @param in The InputStream to read from.
   * @return A new Message, or null if the message URN is not recognised.
   * @throws IOException If there is a problem reading from the stream.
   */
  public static Message readMessage(InputStream in) throws IOException {
    String urn = readString(in);
    if ("".equals(urn)) {
      return null;
    }
    int size = readInt32(in);
    byte[] content = readBytes(size, in);
    Message result = Registry.getCurrentRegistry().createMessage(Registry.getCurrentRegistry().toURN_Id(urn),
        new ByteArrayInputStream(content));
    return result;
  }

  /**
   * Read a message from a DataInput.
   *
   * @param in The DataInput to read from.
   * @return A new Message, or null if the message URN is not recognised.
   * @throws IOException If there is a problem reading from the stream.
   */
  public static Message readMessage(DataInput in) throws IOException {
    String urn = readString(in);
    if ("".equals(urn)) {
      return null;
    }
    int size = readInt32(in);
    byte[] content = readBytes(size, in);
    Message result = Registry.getCurrentRegistry().createMessage(Registry.getCurrentRegistry().toURN_Id(urn),
        new ByteArrayInputStream(content));
    return result;
  }

  /**
   * Read a 32-bit float from an input stream, big-endian style.
   *
   * @param in The InputStream to read from.
   * @return The next big-endian, 32-bit float in the stream.
   * @throws IOException  If the InputStream blows up.
   * @throws EOFException If the end of the stream is reached.
   */
  public static float readFloat32(InputStream in) throws IOException {
    int i = readInt32(in);
    return Float.intBitsToFloat(i);
  }

  /**
   * Write a 32-bit float to an OutputStream, big-endian style.
   *
   * @param f   The float to write.
   * @param out The OutputStream to write it to.
   * @throws IOException If the OutputStream blows up.
   */
  public static void writeFloat32(float f, OutputStream out) throws IOException {
    /* Aftershock Requirement:2013 */
    int i = Float.floatToIntBits(f);
    writeInt32(i, out);
  }

  /**
   * Write a 32-bit float to a DataOutput, big-endian style.
   *
   * @param f   The float to write.
   * @param out The DataOutput to write it to.
   * @throws IOException If the DataOutput blows up.
   */
  public static void writeFloat32(float f, DataOutput out) throws IOException {
    /* Aftershock Requirement:2013 */
    // DataOutput writes big-endian
    out.write(Float.floatToIntBits(f));
  }

  /**
   * Write a 32-bit float to a byte array, big-endian style.
   *
   * @param f      The float to write.
   * @param out    The buffer to write it to.
   * @param offset Where in the buffer to write it.
   */
  public static void writeFloat32(float f, byte[] out, int offset) {
    /* Aftershock Requirement:2013 */
    // Most significant byte first
    int i = Float.floatToIntBits(f);
    writeInt32(i, out, offset);

  }

  /**
   * Read a 32-bit float from a DataInput.
   *
   * @param in The DataInput to read from.
   * @return The next big-endian, 32-bit float in the stream.
   * @throws IOException  If the DataInput blows up.
   * @throws EOFException If the end of the stream is reached.
   */

  public static float readFloat32(DataInput in) throws IOException {
    /* Aftershock Requirement:2013 */
    return Float.intBitsToFloat(in.readInt());
  }

  /** CHECKSTYLE:ON:MagicNumber */

}
