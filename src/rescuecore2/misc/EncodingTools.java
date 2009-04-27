package rescuecore2.misc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.nio.charset.Charset;

/**
   A bunch of useful tools for encoding and decoding things like integers.
 */
public final class EncodingTools {
    /** Charset for encoding/decoding strings. Should always be UTF-8 */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /**
       Private constructor: this is a utility class.
    */
    private EncodingTools() {}

    /** Turn off the checkstyle test for magic numbers since we use a lot of them here */
    /** CHECKSTYLE:OFF:MagicNumber */

    /**
       Write a 32-bit integer to an OutputStream, big-endian style.
       @param i The integer to write.
       @param out The OutputStream to write it to.
       @throws IOException If the OutputStream blows up.
     */
    public static void writeInt32(int i, OutputStream out) throws IOException {
        // Most significant byte first
        out.write((byte) (i >> 24) & 0xFF);
        out.write((byte) (i >> 16) & 0xFF);
        out.write((byte) (i >> 8) & 0xFF);
        out.write((byte) i & 0xFF);
    }

    /**
       Write a 32-bit integer to a byte array, big-endian style.
       @param i The integer to write.
       @param out The buffer to write it to.
       @param offset Where in the buffer to write it.
     */
    public static void writeInt32(int i, byte[] out, int offset) {
        // Most significant byte first
        out[offset] = (byte) ((i >> 24) & 0xFF);
        out[offset + 1] = (byte) ((i >> 16) & 0xFF);
        out[offset + 2] = (byte) ((i >> 8) & 0xFF);
        out[offset + 3] = (byte) (i & 0xFF);
    }

    /**
       Read a 32-bit integer from an input stream, big-endian style.
       @param in The InputStream to read from.
       @return The next big-endian, 32-bit integer in the stream.
       @throws IOException If the InputStream blows up.
     */
    public static int readInt32(InputStream in) throws IOException {
        int result = 0;
        result |= in.read() << 24;
        result |= in.read() << 16;
        result |= in.read() << 8;
        result |= in.read();
        return result;
    }

    /**
       Read a 32-bit integer from a byte array, big-endian style.
       @param in The buffer to read from.
       @param offset Where to begin reading.
       @return The next big-endian, 32-bit integer in the buffer.
     */
    public static int readInt32(byte[] in, int offset) {
        return (in[offset] << 24) | (in[offset + 1] << 16) | (in[offset + 2] << 8) | (in[offset + 3]);
    }

    /**
       Read a 32-bit integer from a byte array, big-endian style. This is equivalent to calling {@link #readInt32(byte[], int) readInt32(in, 0)}.
       @param in The buffer to read from.
       @return The first big-endian, 32-bit integer in the buffer.
     */
    public static int readInt32(byte[] in) {
        return readInt32(in, 0);
    }

    /**
       Write a String to an OutputStream. Strings are always in UTF-8.
       @param s The String to write.
       @param out The OutputStream to write to.
       @throws IOException If the OutputStream blows up.
     */
    public static void writeString(String s, OutputStream out) throws IOException {
        byte[] bytes = s.getBytes(CHARSET);
        writeInt32(bytes.length, out);
        out.write(bytes);
    }

    /**
       Write a String to a byte array. Strings are always in UTF-8.
       @param s The String to write.
       @param out The byte array to write to. Make sure it's big enough!
       @param offset The index to start writing from.
     */
    public static void writeString(String s, byte[] out, int offset) {
        byte[] bytes = s.getBytes(CHARSET);
        writeInt32(bytes.length, out, offset);
        System.arraycopy(bytes, 0, out, offset + 4, bytes.length);
    }

    /**
       Read a String from an InputStream.
       @param in The InputStream to read.
       @throws IOException If the InputStream blows up.
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
       Read a String from a byte array. This is equivalent to calling {@link #readString(byte[], int) readString(in, 0)}.
       @param in The byte array to read.
    */
    public static String readString(byte[] in) {
        return readString(in, 0);
    }

    /**
       Read a String from a byte array.
       @param in The byte array to read.
       @param offset The index in the array to read from.
    */
    public static String readString(byte[] in, int offset) {
        int length = readInt32(in, offset);
        byte[] buffer = new byte[length];
        System.arraycopy(in, offset + 4, buffer, 0, length);
        return new String(buffer, CHARSET);
    }

    /**
       Read a fixed number of bytes from an InputStream into an array.
       @param size The number of bytes to read.
       @param in The InputStream to read from.
       @return A new byte array containing the bytes.
       @throws IOException If the read operation fails.
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

    /*
    public static byte[] encodeProperty(Property prop) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        prop.write(out);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(prop.getID());
        return result.toByteArray();
    }
    */


    /** CHECKSTYLE:ON:MagicNumber */
}