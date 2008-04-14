package org.basex.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

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
  private int size;
  
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
   * A default buffersize will be used.
   * DBSUFFIX will be added to the filename.
   * @param db name of the database
   * @param fn File to write to
   * @throws IOException in case of write errors
   */
  public DataOutput(final String db, final String fn) throws IOException {
    this(db, fn, 4096);
  }
  
  /**
   * Convenience constructor with underlying.
   * The specified buffersize is used.
   * DBSUFFIX will be added to the filename.
   * @param db name of the database
   * @param fn name of the file to write to
   * @param bufs size of the buffer to use
   * @throws IOException in case of write errors
   */
  public DataOutput(final String db, final String fn, final int bufs)
      throws IOException {
    final File path = IOConstants.dbfile(db, fn);
    os = new BufferedOutput(new FileOutputStream(path), bufs);
  }
  
  /*
   * Writes a single byte, wrapped in an integer, to the output stream
   * @see java.io.OutputStream#write(int)
   */
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
   * @return length of written bytes
   * @throws IOException in case of write errors
   */
  public int writeToken(final TokenBuilder text) throws IOException {
    final int s = writeNum(text.size);
    for(int i = 0; i < text.size; i++) write(text.chars[i]);
    return s + text.size;
  }

  /**
   * Writes the specified token to the output stream.
   * @param text text to be written
   * @return length of written bytes
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
   * Writes the specified array to the output stream; null references
   * are replaced with an empty array.
   * @param array array to be written
   * @throws IOException in case of write errors
   */
  public void writeNumsArray(final int[][] array) throws IOException {
    writeNum(array.length);
    for(final int[] a : array) writeNums(a != null ? a : Array.NOINTS);
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
   * Writes the specified array to the output stream.
   * @param array array to be written
   * @throws IOException in case of write errors
   */
  public void writeInts(final int[] array) throws IOException {
    for(final int a : array) writeInt(a);
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
   * Writes the specified array to the output stream; null references
   * are replaced with an empty array. No size info is written!!
   * @param array array to be written
   * @throws IOException in case of write errors
   */
  public void writeBytesArrayFlat(final byte[][] array) throws IOException {
    for(final byte[] a : array) writeBytesFlat(a != null ? a : Token.EMPTY);  
  }
  
  /**
   * Writes the specifies array to output stream as follows.
   * Input: {a, b, c, d, e, ...}
   * Written: {a, a+b, a+b+c, a+b+c+d, a+b+c+d+e, ...}
   * @param array inputarray
   * @throws IOException in case of wrtie errors
   */
  public void writeStructureWithOffsets(final int[] array) throws IOException {
    int b = 0;
    for(final int a : array) {
      b += a;
      writeInt(b);  
    }
  }

  /**
   * Writes the specified array to the output stream; null references
   * are replaced with an empty array.
   * @param array array to be written
   * @throws IOException in case of write errors
   */
  public void writeIntArray(final int[][] array) throws IOException {
    for(final int[] a : array) writeInts(a != null ? a : Array.NOINTS);
  }
  
  /**
   * Writes the specified array to the output stream. No size info is written!!
   * @param array array to be written
   * @throws IOException in case of write errors
   */
  public void writeBytesFlat(final byte[] array) throws IOException {
    for(final byte a : array) write(a);
  }
 
  /**
   * Return the number of written bytes.
   * This is not necessary e.g. the file size.
   * @return number of written bytes
   */
  public int size() {
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
