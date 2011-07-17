package org.basex.io.in;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import org.basex.util.Token;
import org.basex.util.list.IntList;

/**
 * This is an input stream for project specific data types.
 * It bears resemblance to Java's {@link DataInputStream}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DataInput extends BufferInput {
  /**
   * Constructor.
   * @param file the file to be read
   * @throws IOException IO Exception
   */
  public DataInput(final File file) throws IOException {
    super(file);
  }

  /**
   * Reads a boolean value from the input stream.
   * @return boolean value
   * @throws IOException IO Exception
   */
  public boolean readBool() throws IOException {
    return read() == 1;
  }

  /**
   * Reads a byte array from the input stream.
   * @return byte array
   * @throws IOException IO Exception
   */
  public byte[] readBytes() throws IOException {
    final int l = readNum();
    if(l == 0) return Token.EMPTY;
    final byte[] array = new byte[l];
    for(int i = 0; i < l; ++i) array[i] = readByte();
    return array;
  }

  /**
   * Reads a double from the input stream.
   * @return byte array
   * @throws IOException IO Exception
   */
  public double readDouble() throws IOException {
    return Token.toDouble(readBytes());
  }

  /**
   * Reads an distance-mapped integer array from the input stream.
   * @return integer array
   * @throws IOException IO Exception
   */
  public IntList readDiffs() throws IOException {
    final int[] array = new int[readNum()];
    for(int a = 0; a < array.length; ++a) {
      array[a] = (a == 0 ? 0 : array[a - 1]) + readNum();
    }
    return new IntList(array);
  }

  /**
   * Reads an integer array from the input stream.
   * @return integer array
   * @throws IOException IO Exception
   */
  public int[] readNums() throws IOException {
    return readNums(readNum());
  }

  /**
   * Reads an integer array with the specified size from the input stream.
   * @param s array size
   * @return integer array
   * @throws IOException IO Exception
   */
  public int[] readNums(final int s) throws IOException {
    final int[] array = new int[s];
    for(int a = 0; a < s; ++a) array[a] = readNum();
    return array;
  }

  /**
   * Reads a double byte array from the input stream.
   * @return double array
   * @throws IOException IO Exception
   */
  public byte[][] readBytesArray() throws IOException {
    final int l = readNum();
    final byte[][] array = new byte[l][];
    for(int i = 0; i < l; ++i) array[i] = readBytes();
    return array;
  }

  /**
   * Reads and decompresses an integer value from the input stream.
   * @return read value
   * @throws IOException IO Exception
   */
  public int readNum() throws IOException {
    final int v = read();
    switch((v & 0xC0) >>> 6) {
      case 0:
        return v;
      case 1:
        return ((v & 0x3F) << 8) + read();
      case 2:
        return ((v & 0x3F) << 24) + (read() << 16) + (read() << 8) + read();
      default:
        return (read() << 24) + (read() << 16) + (read() << 8) + read();
    }
  }

  /**
   * Reads an array of longs with the specified size from the input stream.
   * @param s array size
   * @return array of longs
   * @throws IOException IO Exception
   */
  public long[] readLongs(final int s) throws IOException {
    final long[] array = new long[s];
    for(int a = 0; a < s; ++a) array[a] = read8();
    return array;
  }

  /**
   * Read a long value from the input stream.
   * @return read value
   * @throws IOException IO Exception
   */
  public long read8() throws IOException {
    return ((long) read() << 56) + ((long) (read() & 255) << 48)
        + ((long) (read() & 255) << 40) + ((long) (read() & 255) << 32)
        + ((long) (read() & 255) << 24) + ((read() & 255) << 16)
        + ((read() & 255) << 8) + ((read() & 255) << 0);
  }
}
