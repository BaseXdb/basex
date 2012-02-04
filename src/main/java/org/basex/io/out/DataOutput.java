package org.basex.io.out;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Num;
import org.basex.util.Token;
import org.basex.util.list.IntList;

/**
 * This is an output stream for project specific data types.
 * It bears resemblance to Java's {@link DataOutputStream}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DataOutput extends OutputStream {
  /** The underlying output stream. */
  private final OutputStream os;
  /** Number of written bytes. */
  private long size;

  /**
   * Constructor, specifying an output stream.
   * @param out output stream to operate on
   */
  public DataOutput(final OutputStream out) {
    os = out;
  }

  /**
   * Constructor, specifying a file.
   * @param db name of the database
   * @throws IOException I/O exception
   */
  public DataOutput(final IOFile db) throws IOException {
    this(db, IO.BLOCKSIZE);
  }

  /**
   * Constructor, specifying a file and a buffer size.
   * The specified buffer size is used.
   * @param db name of the database
   * @param bufs size of the buffer to use
   * @throws IOException I/O exception
   */
  public DataOutput(final IOFile db, final int bufs) throws IOException {
    os = new BufferOutput(new FileOutputStream(db.file()), bufs);
  }

  @Override
  public void write(final int b) throws IOException {
    os.write(b);
    ++size;
  }

  /**
   * Writes a boolean value.
   * @param b boolean value
   * @throws IOException I/O exception
   */
  public void writeBool(final boolean b) throws IOException {
    write(b ? 1 : 0);
  }

  /**
   * Writes a token, represented by its compressed length and its byte array.
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
   * Writes a double value.
   * @param num array to be written
   * @return number of written bytes
   * @throws IOException I/O exception
   */
  public int writeDouble(final double num) throws IOException {
    return writeToken(Token.token(num));
  }

  /**
   * Writes tokens. {@code null} references are replaced by an empty array.
   * @param array array to be written
   * @throws IOException I/O exception
   */
  public void writeTokens(final byte[][] array) throws IOException {
    writeNum(array.length);
    for(final byte[] a : array) writeToken(a != null ? a : Token.EMPTY);
  }

  /**
   * Writes distances between integers.
   * @param array array to be written
   * @throws IOException I/O exception
   */
  public void writeDiffs(final IntList array) throws IOException {
    final int al = array.size();
    writeNum(al);
    int c = 0;
    for(int a = 0; a < al; a++) {
      final int t = array.get(a);
      writeNum(t - c);
      c = t;
    }
  }

  /**
   * Writes compressed numbers; see {@link Num} for more.
   * @param array array to be written
   * @throws IOException I/O exception
   */
  public void writeNums(final int[] array) throws IOException {
    writeNum(array.length);
    for(final int a : array) writeNum(a);
  }

  /**
   * Writes a compressed integer value; see {@link Num} for more.
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
   * Writes long values.
   * NOTE: the long values are not compressed!
   * @param array array to be written
   * @throws IOException I/O exception
   */
  public void writeLongs(final long[] array) throws IOException {
    writeNum(array.length);
    for(final long a : array) write8(a);
  }

  /**
   * Writes a byte value.
   * @param v value to be written
   * @throws IOException I/O exception
   */
  public void write1(final int v) throws IOException {
    write(v);
  }

  /**
   * Writes a short value.
   * @param v value to be written
   * @throws IOException I/O exception
   */
  public void write2(final int v) throws IOException {
    write(v >>> 8);
    write(v);
  }

  /**
   * Writes an integer value.
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
   * Writes 5 bytes of a long value.
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
   * Writes a long value.
   * @param v value to be written
   * @throws IOException I/O exception
   */
  void write8(final long v) throws IOException {
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
   * Writes a byte array.
   * @param bytes array to be written
   * @throws IOException I/O exception
   */
  public void writeBytes(final byte[] bytes) throws IOException {
    for(final byte b : bytes) write(b);
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
