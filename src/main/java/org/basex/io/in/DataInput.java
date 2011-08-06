package org.basex.io.in;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import org.basex.util.Num;
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
   * Reads a boolean value.
   * @return boolean value
   * @throws IOException IO Exception
   */
  public boolean readBool() throws IOException {
    return read() == 1;
  }

  /**
   * Reads a token.
   * @return byte array
   * @throws IOException IO Exception
   */
  public byte[] readToken() throws IOException {
    final int l = readNum();
    if(l == 0) return Token.EMPTY;
    final byte[] array = new byte[l];
    for(int i = 0; i < l; ++i) array[i] = readByte();
    return array;
  }

  /**
   * Reads a double value.
   * @return byte array
   * @throws IOException IO Exception
   */
  public double readDouble() throws IOException {
    return Token.toDouble(readToken());
  }

  /**
   * Reads a distance-mapped integer array.
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
   * Reads a compressed integer array.
   * @return integer array
   * @throws IOException IO Exception
   */
  public int[] readNums() throws IOException {
    return readNums(readNum());
  }

  /**
   * Reads compressed integer values of the specified size.
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
   * Reads a token array.
   * @return double array
   * @throws IOException IO Exception
   */
  public byte[][] readTokens() throws IOException {
    final int l = readNum();
    final byte[][] array = new byte[l][];
    for(int i = 0; i < l; ++i) array[i] = readToken();
    return array;
  }

  /**
   * Reads a compressed integer value; see {@link Num} for more.
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
   * Reads an array of long values.
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
   * Read a long value.
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
