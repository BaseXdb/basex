package org.basex.io;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.util.Token;

/**
 * This is an output stream for project specific data types.
 * It bears resemblance to Java's {@link DataOutputStream}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class DataOutput extends OutputStream {
  /** The underlying output stream. */
  private final OutputStream os;
  /** Number of written bytes. */
  private long size;

  /**
   * Constructor writing to an output stream.
   * Writes to the stream will not be buffered.
   * @param out Output Stream to operate on
   */
  public DataOutput(final OutputStream out) {
    os = out;
  }

  /**
   * Convenience constructor.
   * A default buffer size will be used.
   * @param db name of the database
   * @throws IOException I/O exception
   */
  public DataOutput(final File db) throws IOException {
    this(db, IO.BLOCKSIZE);
  }

  /**
   * Convenience constructor with a specified buffer size.
   * The specified buffer size is used.
   * @param db name of the database
   * @param bufs size of the buffer to use
   * @throws IOException I/O exception
   */
  public DataOutput(final File db, final int bufs) throws IOException {
    os = new BufferedOutput(new FileOutputStream(db), bufs);
  }

  @Override
  public void write(final int b) throws IOException {
    os.write(b);
    ++size;
  }

  /**
   * Writes the specified bytes to the output stream.
   * @param bytes array to be written
   * @throws IOException I/O exception
   */
  public void writeBytes(final byte[] bytes) throws IOException {
    for(final byte b : bytes) write(b);
  }

  /**
   * Writes a boolean value to the output stream.
   * @param b boolean value
   * @throws IOException I/O exception
   */
  public void writeBool(final boolean b) throws IOException {
    write(b ? 1 : 0);
  }

  /**
   * Writes a string to the output stream.
   * @param s string
   * @throws IOException I/O exception
   */
  public void writeString(final String s) throws IOException {
    writeToken(Token.token(s));
  }

  /**
   * Writes the specified token to the output stream.
   * @param tok array to be written
   * @return number of written bytes
   * @throws IOException I/O exception
   */
  public int writeToken(final byte[] tok) throws IOException {
    final int s = writeNum(tok.length);
    writeBytes(tok);
    return s + tok.length;
  }

  /**
   * Writes the specified double to the output stream.
   * @param num array to be written
   * @return number of written bytes
   * @throws IOException I/O exception
   */
  public int writeDouble(final double num) throws IOException {
    return writeToken(Token.token(num));
  }

  /**
   * Writes the specified tokens to the output stream; {@code null} references
   * are replaced by an empty array.
   * @param array array to be written
   * @throws IOException I/O exception
   */
  public void writeTokens(final byte[][] array) throws IOException {
    writeNum(array.length);
    for(final byte[] a : array) writeToken(a != null ? a : Token.EMPTY);
  }

  /**
   * Writes the specified nums to the output stream.
   * @param array array to be written
   * @throws IOException I/O exception
   */
  public void writeNums(final int[] array) throws IOException {
    writeNum(array.length);
    for(final int a : array) writeNum(a);
  }

  /**
   * Compresses and writes an integer value to the specified output stream.
   * By compressing, the size of the database files is reduced.
   * @param v value to be written
   * @return number of written values
   * @throws IOException I/O exception
   */
  public int writeNum(final int v) throws IOException {
    if(v < 0 || v > 0x3FFFFFFF) {
      write(0xC0); write(v >>> 24); write(v >>> 16); write(v >>> 8); write(v);
      return 5;
    }
    if(v > 0x3FFF) {
      write(v >>> 24 | 0x80); write(v >>> 16); write(v >>> 8); write(v);
      return 4;
    }
    if(v > 0x3F) {
      write(v >>> 8 | 0x40); write(v);
      return 2;
    }
    write(v);
    return 1;
  }


  /**
   * Writes the specified longs to the output stream.
   * NOTE: the long values are not compressed!
   * @param array array to be written
   * @throws IOException I/O exception
   */
  public void writeLongs(final long[] array) throws IOException {
    writeNum(array.length);
    for(final long a : array) write8(a);
  }

  /**
   * Writes a long value to the specified output stream.
   * @param v value to be written
   * @throws IOException I/O exception
   */
  public void write8(final long v) throws IOException {
    write((byte) (v >>> 56));
    write((byte) (v >>> 48));
    write((byte) (v >>> 40));
    write((byte) (v >>> 32));
    write((byte) (v >>> 24));
    write((byte) (v >>> 16));
    write((byte) (v >>>  8));
    write((byte)  v);
  }

  /**
   * Writes an integer value to the specified output stream.
   * @param v value to be written
   * @throws IOException I/O exception
   */
  public void write4(final int v) throws IOException {
    write(v >>> 24);
    write(v >>> 16);
    write(v >>>  8);
    write(v);
  }

  /**
   * Writes a byte value to the specified output stream.
   * @param v value to be written
   * @throws IOException I/O exception
   */
  public void write1(final int v) throws IOException {
    write(v);
  }

  /**
   * Writes a short value to the specified output stream.
   * @param v value to be written
   * @throws IOException I/O exception
   */
  public void write2(final int v) throws IOException {
    write(v >>> 8);
    write(v);
  }

  /**
   * Writes an long value to the specified output stream.
   * @param v value to be written
   * @throws IOException I/O exception
   */
  public void write5(final long v) throws IOException {
    write((byte) (v >>> 32));
    write((byte) (v >>> 24));
    write((byte) (v >>> 16));
    write((byte) (v >>>  8));
    write((byte) v);
  }

  /**
   * Returns the number of written bytes.
   * This is not necessarily e.g. the file size.
   * @return number of written bytes
   */
  public long size() {
    return size;
  }

  @Override
  public void flush() throws IOException {
    os.flush();
  }

  @Override
  public void close() throws IOException {
    os.close();
  }
}
