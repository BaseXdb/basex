package org.basex.io.in;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is an input stream for project specific data types.
 * It bears resemblance to Java's {@link DataInputStream}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DataInput extends BufferInput {
  /**
   * Constructor.
   * @param io the input to be read
   * @throws IOException I/O Exception
   */
  public DataInput(final IO io) throws IOException {
    super(io);
  }

  /**
   * Reads a boolean value.
   * @return boolean value
   * @throws IOException I/O Exception
   */
  public boolean readBool() throws IOException {
    return read() == 1;
  }

  /**
   * Reads a token, represented by its compressed length and its byte array.
   * @return byte array
   * @throws IOException I/O Exception
   */
  public byte[] readToken() throws IOException {
    final int l = readNum();
    if(l == 0) return Token.EMPTY;
    final byte[] tmp = new byte[l];
    for(int i = 0; i < l; ++i) tmp[i] = (byte) read();
    return tmp;
  }

  /**
   * Reads a double value.
   * @return byte array
   * @throws IOException I/O Exception
   */
  public double readDouble() throws IOException {
    return Token.toDouble(readToken());
  }

  /**
   * Reads a distance-mapped integer array.
   * @return integer array
   * @throws IOException I/O Exception
   */
  public IntList readDiffs() throws IOException {
    final int[] tmp = new int[readNum()];
    final int al = tmp.length;
    for(int a = 0; a < al; ++a) tmp[a] = (a == 0 ? 0 : tmp[a - 1]) + readNum();
    return new IntList(tmp);
  }

  /**
   * Reads a compressed integer array.
   * @return integer array
   * @throws IOException I/O Exception
   */
  public int[] readNums() throws IOException {
    return readNums(readNum());
  }

  /**
   * Reads compressed integer values of the specified size.
   * @param s array size
   * @return integer array
   * @throws IOException I/O Exception
   */
  private int[] readNums(final int s) throws IOException {
    final int[] tmp = new int[s];
    for(int a = 0; a < s; ++a) tmp[a] = readNum();
    return tmp;
  }

  /**
   * Reads a token array.
   * @return double array
   * @throws IOException I/O Exception
   */
  public byte[][] readTokens() throws IOException {
    final int l = readNum();
    final byte[][] tmp = new byte[l][];
    for(int i = 0; i < l; ++i) tmp[i] = readToken();
    return tmp;
  }

  /**
   * Reads a compressed integer value; see {@link Num} for more.
   * @return read value
   * @throws IOException I/O Exception
   */
  public int readNum() throws IOException {
    final int v = read();
    if(v == -1) return 0;
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
   * @throws IOException I/O Exception
   */
  public long[] readLongs(final int s) throws IOException {
    final long[] tmp = new long[s];
    for(int a = 0; a < s; ++a) tmp[a] = read8();
    return tmp;
  }

  /**
   * Read a long value.
   * @return read value
   * @throws IOException I/O Exception
   */
  private long read8() throws IOException {
    return ((long) read() << 56) + ((long) (read() & 255) << 48)
        + ((long) (read() & 255) << 40) + ((long) (read() & 255) << 32)
        + ((long) (read() & 255) << 24) + ((read() & 255) << 16)
        + ((read() & 255) << 8) + (read() & 255);
  }
}
