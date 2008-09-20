package org.basex.io;

import java.io.IOException;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This is an input stream for project specific data types.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
   */
  public boolean readBool() {
    return read() == 1;
  }
  
  /**
   * Reads a string from the input stream.
   * @return string
   */
  public String readString() {
    return Token.string(readBytes());
  }

  /**
   * Reads a byte array from the input stream.
   * @return byte array
   */
  public byte[] readBytes() {
    final int l = readNum();
    if(l == 0) return Token.EMPTY;
    final byte[] array = new byte[l];
    for(int i = 0; i < l; i++) array[i] = readByte();
    return array;
  }

  /**
   * Reads an integer array from the input stream.
   * @return integer array
   */
  public int[] readNums() {
    return readNums(readNum());
  }

  /**
   * Reads an integer array with the specified size from the input stream.
   * @param s array size
   * @return integer array
   */
  public int[] readNums(final int s) {
    if(s == 0) return Array.NOINTS;
    final int[] array = new int[s];
    for(int a = 0; a < s; a++) array[a] = readNum();
    return array;
  }

  /**
   * Reads a boolean array from the input stream.
   * @return boolean array
   */
  public boolean[] readBooleans() {
    final int l = readNum();
    final boolean[] array = new boolean[l];
    for(int i = 0; i < l; i++) array[i] = readBool();
    return array;
  }

  /**
   * Reads a double byte array from the input stream.
   * @return double array
   */
  public byte[][] readBytesArray() {
    final int l = readNum();
    final byte[][] array = new byte[l][];
    for(int i = 0; i < l; i++) array[i] = readBytes();
    return array;
  }

  /**
   * Reads and decompresses an integer value from the input stream.
   * @return read value
   */
  public int readNum() {
    final int v = read();
    switch((v & 0xC0) >>> 6) {
    case 0:
      return v;
    case 1:
      return (v & 0x3F) << 8 | read();
    case 2:
      return (v & 0x3F) << 24 | read() << 16 | read() << 8 | read();
    default:
      return read() << 24 | read() << 16 | read() << 8 | read();
    }
  }

  /**
   * Reads an integer value from the input stream.
   * @return integer value
   */
  public int readInt() {
    return (read() << 24) + (read() << 16) + (read() << 8) + read();
  }
}
