package org.basex.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.util.Token;

/**
 * This is an output stream for project specific data types.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
   * @param fn File to write to
   * @throws IOException in case of write errors
   */
  public DataOutput(final String db, final String fn) throws IOException {
    this(db, fn, 4096);
  }
  
  /**
   * Convenience constructor with a specified buffer size.
   * The specified buffer size is used.
   * @param db name of the database
   * @param fn name of the file to write to
   * @param bufs size of the buffer to use
   * @throws IOException in case of write errors
   */
  public DataOutput(final String db, final String fn, final int bufs)
      throws IOException {
    os = new BufferedOutput(new FileOutputStream(IO.dbfile(db, fn)), bufs);
  }
  
  @Override
  public void write(final int b) throws IOException {
    os.write(b);
    size++;
  }

  /**
   * Writes a boolean value to the output stream.
   * @param b boolean value
   * @throws IOException in case of write errors
   */
  public void writeBool(final boolean b) throws IOException {
    write(b ? 1 : 0);
  }
  
  /**
   * Writes a string to the output stream.
   * @param s string
   * @throws IOException in case of write errors
   */
  public void writeString(final String s) throws IOException {
    writeBytes(Token.token(s));
  }

  /**
   * Writes the specified token to the output stream.
   * @param text text to be written
   * @return number of written bytes
   * @throws IOException in case of write errors
   */
  public int writeToken(final byte[] text) throws IOException {
    final int s = writeNum(text.length);
    for(final byte t : text) write(t);
    return s + text.length;
  }

  /**
   * Writes the specified array to the output stream.
   * @param array array to be written
   * @throws IOException in case of write errors
   */
  public void writeBytes(final byte[] array) throws IOException {
    writeNum(array.length);
    for(final byte a : array) write(a);
  }

  /**
   * Writes the specified array to the output stream.
   * @param array array to be written
   * @throws IOException in case of write errors
   */
  public void writeBooleans(final boolean[] array) throws IOException {
    writeNum(array.length);
    for(final boolean a : array) writeBool(a);
  }

  /**
   * Writes the specified array to the output stream; null references
   * are replaced with an empty array.
   * @param array array to be written
   * @throws IOException in case of write errors
   */
  public void writeBytesArray(final byte[][] array) throws IOException {
    writeNum(array.length);
    for(final byte[] a : array) writeBytes(a != null ? a : Token.EMPTY);
  }

  /**
   * Writes the specified array to the output stream.
   * @param array array to be written
   * @throws IOException in case of write errors
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
   * @throws IOException in case of write errors
   */
  public int writeNum(final int v) throws IOException {
    if(v < 0 || v > 0x3FFFFFFF) {
      write(0xC0); write(v >>> 24); write(v >>> 16); write(v >>>  8); write(v);
      return 5;
    }
    if(v > 0x3FFF) {
      write(v >>> 24 | 0x80); write(v >>> 16); write(v >>>  8); write(v);
      return 4;
    }
    if(v > 0x3F) {
      write(v >>>  8 | 0x40); write(v);
      return 2;
    }
    write(v);
    return 1;
  }

  /**
   * Writes an integer value to the specified output stream.
   * @param v value to be written
   * @throws IOException in case of write errors
   */
  public void writeInt(final int v) throws IOException {
    write(v >>> 24);
    write(v >>> 16);
    write(v >>>  8);
    write(v);
  }

  /**
   * Writes an integer value to the specified output stream.
   * @param v value to be written
   * @throws IOException in case of write errors
   */
  public void write1(final int v) throws IOException {
    write(v);
  }

  /**
   * Writes an integer value to the specified output stream.
   * @param v value to be written
   * @throws IOException in case of write errors
   */
  public void write2(final int v) throws IOException {
    write(v >>> 8);
    write(v);
  }

  /**
   * Writes an integer value to the specified output stream.
   * @param v value to be written
   * @throws IOException in case of write errors
   */
  public void write5(final long v) throws IOException {
    write((byte) (v >>> 32));
    write((byte) (v >>> 24));
    write((byte) (v >>> 16));
    write((byte) (v >>>  8));
    write((byte) v);
  }
 
  /**
   * Return the number of written bytes.
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
