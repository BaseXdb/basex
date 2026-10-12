package org.basex.io.in;

import java.io.*;
import java.math.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is an input stream for project specific data types.
 * It bears resemblance to Java's {@link DataInputStream}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DataInput extends BufferInput {
  /**
   * Constructor.
   * @param io input to be read
   * @throws IOException I/O exception
   */
  public DataInput(final IO io) throws IOException {
    super(io);
  }

  /**
   * Reads a boolean value.
   * @return boolean value
   * @throws IOException I/O exception
   */
  public boolean readBool() throws IOException {
    return read() == 1;
  }

  /**
   * Reads a token, represented by its compressed length and its byte array.
   * @return byte array
   * @throws IOException I/O exception
   */
  public byte[] readToken() throws IOException {
    final int l = readNum();
    return l == 0 ? Token.EMPTY : readNBytes(l);
  }

  /**
   * Reads a double value.
   * @return double value
   * @throws IOException I/O exception
   */
  public double readDouble() throws IOException {
    return Token.toDouble(readToken());
  }

  /**
   * Reads a double value that has been written as compact integer or as raw bits.
   * @return double value
   * @throws IOException I/O exception
   */
  public double readDoubleCompact() throws IOException {
    final long n = readZigzag();
    return (n & 1) == 0 ? n >> 1 : Double.longBitsToDouble(read8());
  }

  /**
   * Reads the raw bits of a float value.
   * @return float value
   * @throws IOException I/O exception
   */
  public float readFloatBits() throws IOException {
    return Float.intBitsToFloat(read4());
  }

  /**
   * Reads a decimal value from its scale and unscaled value.
   * @return decimal value
   * @throws IOException I/O exception
   */
  public BigDecimal readDecimal() throws IOException {
    final long n = readZigzag();
    final int scale = (int) (n >> 1);
    return (n & 1) == 0 ? BigDecimal.valueOf(readZigzag(), scale) :
      new BigDecimal(new BigInteger(readToken()), scale);
  }

  /**
   * Reads a zigzag-encoded long value.
   * @return read value
   * @throws IOException I/O exception
   */
  public long readZigzag() throws IOException {
    final int n = readNum();
    // marker: read full value; otherwise, use compact number
    final long z = n == 0x3FFF ? read8() : n & 0xFFFFFFFFL;
    // restore original value
    return z >>> 1 ^ -(z & 1);
  }

  /**
   * Reads a distance-mapped integer array.
   * @return integer array
   * @throws IOException I/O exception
   */
  public IntList readDiffs() throws IOException {
    final int[] tmp = new int[readNum()];
    final int al = tmp.length;
    for(int a = 0; a < al; a++) tmp[a] = (a == 0 ? 0 : tmp[a - 1]) + readNum();
    return new IntList(tmp);
  }

  /**
   * Reads a compressed integer array.
   * @return integer array
   * @throws IOException I/O exception
   */
  public int[] readNums() throws IOException {
    return readNums(readNum());
  }

  /**
   * Reads compressed integer values of the specified size.
   * @param s array size
   * @return integer array
   * @throws IOException I/O exception
   */
  private int[] readNums(final int s) throws IOException {
    final int[] tmp = new int[s];
    for(int a = 0; a < s; a++) tmp[a] = readNum();
    return tmp;
  }

  /**
   * Reads a token array.
   * @return token array
   * @throws IOException I/O exception
   */
  public byte[][] readTokens() throws IOException {
    final int l = readNum();
    final byte[][] tmp = new byte[l][];
    for(int i = 0; i < l; i++) tmp[i] = readToken();
    return tmp;
  }

  /**
   * Reads a compressed integer value; see {@link Num} for more.
   * @return read value
   * @throws IOException I/O exception
   */
  public int readNum() throws IOException {
    return readNum(read());
  }

  /**
   * Reads a compressed integer value, the first byte of which has already been read.
   * @param v first byte
   * @return read value
   * @throws IOException I/O exception
   */
  public int readNum(final int v) throws IOException {
    return v == -1 ? 0 : switch((v & 0xC0) >>> 6) {
      case 0  -> v;
      case 1  -> (v & 0x3F) << 8 | read();
      case 2  -> (v & 0x3F) << 24 | read() << 16 | read() << 8 | read();
      default -> read() << 24 | read() << 16 | read() << 8 | read();
    };
  }

  /**
   * Reads a long value.
   * @return read value
   * @throws IOException I/O exception
   */
  public long readLong() throws IOException {
    final int v = readNum();
    return v == 0x3FFF ? read8() : v;
  }

  /**
   * Reads an array of long values.
   * @param s array size
   * @return array of longs
   * @throws IOException I/O exception
   */
  public long[] readLongs(final int s) throws IOException {
    final long[] tmp = new long[s];
    for(int a = 0; a < s; a++) tmp[a] = read8();
    return tmp;
  }

  /**
   * Reads a long value.
   * @return read value
   * @throws IOException I/O exception
   */
  private long read8() throws IOException {
    return (long) read() << 56 | (long) read() << 48 | (long) read() << 40 | (long) read() << 32 |
        (long) read() << 24 | read() << 16 | read() << 8 | read();
  }

  /**
   * Reads an integer value.
   * @return read value
   * @throws IOException I/O exception
   */
  private int read4() throws IOException {
    return read() << 24 | read() << 16 | read() << 8 | read();
  }

  /**
   * Reads a short value.
   * @return read value
   * @throws IOException I/O exception
   */
  public short read2() throws IOException {
    return (short) (read() << 8 | read());
  }
}
