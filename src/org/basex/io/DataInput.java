package org.basex.io;

import java.io.IOException;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This is an input stream for project specific data types.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DataInput extends BufferInput {
  /**
   * Initializes the file reader.
   * @param db name of the database
   * @param file the file to be read
   * @throws IOException IO Exception
   */
  public DataInput(final String db, final String file) throws IOException {
    super(IO.dbfile(db, file));
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
   * Reads a string from the input stream.
   * @return string
   * @throws IOException IO Exception
   */
  public String readString() throws IOException {
    return Token.string(readBytes());
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
    for(int i = 0; i < l; i++) array[i] = readByte();
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
    if(s == 0) return Array.NOINTS;
    final int[] array = new int[s];
    for(int a = 0; a < s; a++) array[a] = readNum();
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
    for(int i = 0; i < l; i++) array[i] = readBytes();
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
   * Reads an integer value from the input stream.
   * @return integer value
   * @throws IOException IO Exception
   */
  public long read5() throws IOException {
    return ((long) read() << 32) + ((long) read() << 24) +
      (read() << 16) + (read() << 8) + read();
  }
}
