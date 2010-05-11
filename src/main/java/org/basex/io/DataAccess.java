package org.basex.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.basex.util.Num;

/**
 * This class allows positional read and write access to a database file.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DataAccess {
  /** Buffer manager. */
  private final Buffers bm = new Buffers();
  /** Reference to the data input stream. */
  private final RandomAccessFile file;
  /** File length. */
  private long len;
  /** Offset. */
  private int off;

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccess(final File f) throws IOException {
    file = new RandomAccessFile(f, "rw");
    len = file.length();
    cursor(0);
  }

  /**
   * Flushes the buffered data.
   * @throws IOException I/O exception
   */
  public synchronized void flush() throws IOException {
    for(final Buffer b : bm.all()) if(b.dirty) writeBlock(b);
  }

  /**
   * Closes the data access.
   * @throws IOException I/O exception
   */
  public synchronized void close() throws IOException {
    flush();
    file.close();
  }

  /**
   * Returns the current file position.
   * @return text as byte array
   */
  public synchronized long pos() {
    return bm.curr().pos + off;
  }

  /**
   * Returns file length.
   * @return file length
   */
  public synchronized long length() {
    return len;
  }

  /**
   * Checks if more bytes can be read.
   * @return result of check
   */
  public synchronized boolean more() {
    return pos() < len;
  }

  /**
   * Reads a byte value.
   * @return integer value
   */
  public synchronized byte read1() {
    return (byte) read();
  }

  /**
   * Reads an integer value.
   * @return integer value
   */
  public synchronized int read4() {
    return (read() << 24) + (read() << 16) + (read() << 8) + read();
  }

  /**
   * Reads an integer value from the specified position.
   * @param p position
   * @return integer value
   */
  public synchronized int read4(final long p) {
    cursor(p);
    return read4();
  }

  /**
   * Reads a 5-byte value from the specified file offset.
   * @param p position
   * @return long value
   */
  public synchronized long read5(final long p) {
    cursor(p);
    return read5();
  }

  /**
   * Reads a 5-byte value.
   * @return long value
   */
  public synchronized long read5() {
    return ((long) read() << 32) + ((long) read() << 24) +
      (read() << 16) + (read() << 8) + read();
  }

  /**
   * Reads a {@link Num} value from disk.
   * @param p text position
   * @return read num
   */
  public synchronized int readNum(final long p) {
    cursor(p);
    return readNum();
  }

  /**
   * Reads a token from disk.
   * @param p text position
   * @return text as byte array
   */
  public synchronized byte[] readToken(final long p) {
    cursor(p);
    int l = readNum();

    final byte[] b = new byte[l];
    int ll = IO.BLOCKSIZE - off;
    Buffer bf = bm.curr();
    if(l <= ll) {
      System.arraycopy(bf.buf, off, b, 0, l);
    } else {
      System.arraycopy(bf.buf, off, b, 0, ll);
      l -= ll;
      while(l > IO.BLOCKSIZE) {
        bf = next();
        System.arraycopy(bf.buf, 0, b, ll, IO.BLOCKSIZE);
        ll += IO.BLOCKSIZE;
        l -= IO.BLOCKSIZE;
      }
      bf = next();
      System.arraycopy(bf.buf, 0, b, ll, l);
    }
    return b;
  }

  /**
   * Reads a number of bytes in range from -> to and returns them as array.
   * @param s starting position
   * @param l length
   * @return byte array
   */
  public synchronized byte[] readBytes(final long s, final int l) {
    final byte[] b = new byte[l];
    cursor(s);
    for(int i = 0; i < b.length; i++) b[i] = (byte) read();
    return b;
  }

  /**
   * Appends a value to the file and return it's offset.
   * @param p write position
   * @param v byte array to be appended
   */
  public synchronized void writeBytes(final long p, final byte[] v) {
    cursor(p);
    writeNum(v.length);
    for(final byte b : v) write(b);
  }

  /**
   * Sets the disk cursor.
   * @param p read position
   * @return buffer
   */
  public synchronized Buffer cursor(final long p) {
    off = (int) (p & IO.BLOCKSIZE - 1);

    final boolean ch = bm.cursor(p - off);
    final Buffer bf = bm.curr();
    if(ch) {
      try {
        if(bf.dirty) writeBlock(bf);
        bf.pos = p - off;
        file.seek(bf.pos);
        file.read(bf.buf);
      } catch(final IOException ex) {
        ex.printStackTrace();
      }
    }
    return bf;
  }

  /**
   * Reads the next compressed number and returns it as integer.
   * @return next integer
   */
  public synchronized int readNum() {
    final int v = read();
    switch(v & 0xC0) {
    case 0:
      return v;
    case 0x40:
      return (v - 0x40 << 8) + read();
    case 0x80:
      return (v - 0x80 << 24) + (read() << 16) + (read() << 8) + read();
    default:
      return (read() << 24) + (read() << 16) + (read() << 8) + read();
    }
  }

  /**
   * Writes the specified block to disk.
   * @param bf buffer to write
   * @throws IOException I/O exception
   */
  private synchronized void writeBlock(final Buffer bf) throws IOException {
    file.seek(bf.pos);
    file.write(bf.buf);
  }

  /**
   * Appends a value to the file and return it's offset.
   * @param v number to be appended
   */
  private synchronized void writeNum(final int v) {
    if(v < 0 || v > 0x3FFFFFFF) {
      write(0xC0); write(v >>> 24); write(v >>> 16); write(v >>> 8); write(v);
    } else if(v > 0x3FFF) {
      write(v >>> 24 | 0x80); write(v >>> 16);
      write(v >>> 8); write(v);
    } else if(v > 0x3F) {
      write(v >>> 8 | 0x40); write(v);
    } else {
      write(v);
    }
  }

  /**
   * Reads the next byte.
   * @return next byte
   */
  private synchronized int read() {
    final Buffer bf = off == IO.BLOCKSIZE ? next() : bm.curr();
    return bf.buf[off++] & 0xFF;
  }

  /**
   * Writes the next byte.
   * @param b byte to be written
   */
  private synchronized void write(final int b) {
    final Buffer bf = off == IO.BLOCKSIZE ? next() : bm.curr();
    bf.buf[off++] = (byte) b;
    len = Math.max(len, bf.pos + off);
    bf.dirty = true;
  }

  /**
   * Writes an integer value to the specified output stream.
   * @param v value to be written
   */
  public synchronized void writeInt(final int v) {
    write(v >>> 24);
    write(v >>> 16);
    write(v >>>  8);
    write(v);
  }

  /**
   * Returns the next block.
   * @return buffer
   */
  private synchronized Buffer next() {
    off = 0;
    return cursor(bm.curr().pos + IO.BLOCKSIZE);
  }
}
