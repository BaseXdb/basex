package org.basex.io;

import static org.basex.io.IOConstants.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.basex.util.Num;

/**
 * This class allows positional read access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DataAccess {
  /** Default buffer size - must be a power of two. */
  private static final int BUFSIZE = 1 << BLOCKPOWER;
  /** Buffer limit (buffer size - 1). */
  private static final int BUFLIMIT = BUFSIZE - 1;
  /** Number of buffers. */
  private static final int BUFFERS = 3;

  /** Reference to the data input stream. */
  private final RandomAccessFile file;
  /** File length. */
  private long len;

  /** Buffers. */
  private final byte[][] buffer = new byte[BUFFERS][BUFSIZE];
  /** Positions. */
  private final long[] pos = new long[BUFFERS];
  /** Positions. */
  private final boolean[] dirty = new boolean[BUFFERS];
  /** Positions. */
  private boolean dirt;
  /** Offset. */
  private short off;
  /** Current buffer reference. */
  private int c;

  /**
   * Constructor, initializing the file reader.
   * @param db name of the database
   * @param fn the file to be read
   * @throws IOException IO Exception
   */
  public DataAccess(final String db, final String fn) throws IOException {
    this(IOConstants.dbfile(db, fn));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccess(final File f) throws IOException {
    file = new RandomAccessFile(f, "rw");
    file.read(buffer[0]);
    for(int i = 1; i < BUFFERS; i++) pos[i] = -1;
    len = file.length();
  }
  
  /**
   * Flushes the buffered data.
   * @throws IOException in case of write errors
   */
  public synchronized void flush() throws IOException {
    for(int i = 0; i < BUFFERS; i++) {
      if(dirty[i]) writeBlock(buffer[i], pos[i]);
    }
    dirt = false;
  }
  
  /**
   * Closes the data access.
   * @throws IOException in case of write errors
   */
  public synchronized void close() throws IOException {
    if(dirt) flush();
    file.close();
  }

  /**
   * Returns file length.
   * @return file length
   */
  public synchronized long length() {
    return len;
  }

  /**
   * Reads an integer value from the specified position.
   * @param p position
   * @return integer value
   */
  public synchronized int read4(final long p) {
    cursor(p << 2);
    return (read() << 24) + (read() << 16) + (read() << 8) + read();
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
    int ll = BUFSIZE - off;
    if(ll >= l) {
      System.arraycopy(buffer[c], off, b, 0, l);
    } else {
      System.arraycopy(buffer[c], off, b, 0, ll);
      l -= ll;
      while(l > BUFSIZE) {
        nextBlock();
        System.arraycopy(buffer[c], 0, b, ll, BUFSIZE);
        l -= BUFSIZE;
        ll += BUFSIZE;
      }
      nextBlock();
      System.arraycopy(buffer[c], 0, b, ll, l);
    }
    return b;
  }

  /**
   * Returns the current file position.
   * @return text as byte array
   */
  public synchronized long pos() {
    return pos[c] + off;
  }

  /**
   * Returns the first id of an index.
   * @param p position
   * @return id
   */
  public synchronized int firstID(final int p) {
    cursor(p);
    readNum();
    for(int i = 0; i < 4; i++) read();
    return readNum();
  }

  /**
   * Reads an integer value from the specified position.
   * @param p position
   * @return integer value
   */
  public synchronized int readInt(final long p) {
    cursor(p);
    return readInt();
  }

  /**
   * Reads a number of bytes in range from -> to and returns them as array.
   * @param from starting position for reading
   * @param to ending position for reading
   * @return byte array
   */
  public synchronized byte[] readBytes(final long from, final long to) {
    final byte[] array = new byte[(int) (to - from)];
    cursor(from);
    for(int i = 0; i < array.length; i++) array[i] = (byte) read();
    return array;
  }
   
  /**
   * Reads a number of int values in range from -> to and returns them as array.
   * @param from starting position for reading
   * @param to ending position for reading
   * @return int array
   */
  public synchronized int[] readInts(final long from, final long to) {
    final int[] array = new int[(int) (to - from) >> 2];
    cursor(from);
    for(int i = 0; i < array.length; i++) array[i] = readInt();
    return array;
  }

  /**
   * Append a value to the file and return it's offset.
   * @param p write position
   * @param v byte array to be appended
   */
  public synchronized void writeBytes(final long p, final byte[] v) {
    cursor(p);
    writeNum(v.length);
    for(final byte b : v) write(b);
  }

  // private methods...

  /**
   * Reads the next block from disk.
   */
  private synchronized void nextBlock() {
    cursor(pos[c] + BUFSIZE);
  }
  
  /**
   * Sets the disk cursor.
   * @param p read position
   */
  private synchronized void cursor(final long p) {
    off = (short) (p & BUFLIMIT);
    
    final long ps = p - off;
    int o = c;
    do {
      if(pos[c] == ps) return;
    } while((c = (c + 1) % BUFFERS) != o);
    
    c = (o + 1) % BUFFERS;
    readBlock(ps);
  }

  /**
   * Reads the block at the specified file position from disk.
   * @param n read position
   */
  private synchronized void readBlock(final long n) {
    try {
      if(dirt) flush();
      pos[c] = n;
      file.seek(n);
      file.read(buffer[c]);
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Writes the specified block to disk.
   * @param bf buffer to write
   * @param ps file position
   * @throws IOException I/O exception
   */
  private synchronized void writeBlock(final byte[] bf, final long ps)
      throws IOException {
    file.seek(ps);
    file.write(bf);
  }

  /**
   * Reads the next byte.
   * @return next byte
   */
  private synchronized int read() {
    if(off == BUFSIZE) {
      nextBlock();
      off = 0;
    }
    return buffer[c][off++] & 0xFF;
  }

  /**
   * Reads the next compressed number and returns it as integer.
   * @return next integer
   */
  private synchronized int readNum() {
    final int v = read();
    switch(v & 0xC0) {
    case 0:
      return v;
    case 0x40:
      return ((v - 0x40) << 8) + read();
    case 0x80:
      return ((v - 0x80) << 24) + (read() << 16) + (read() << 8) + read();
    default:
      return (read() << 24) + (read() << 16) + (read() << 8) + read();
    }
  }

  /**
   * Reads an integer value from the specified position
   * (without cursor correction).
   * @return integer value
   */
  private synchronized int readInt() {
    return (read() << 24) + (read() << 16) + (read() << 8) + read();
  }
  
  /**
   * Append a value to the file and return it's offset.
   * @param v number to be appended
   */
  private synchronized void writeNum(final int v) {
    if(v < 0 || v > 0x3FFFFFFF) {
      write(0xC0); write(v >>> 24); write(v >>> 16); write(v >>>  8); write(v);
    } else if(v > 0x3FFF) {
      write(v >>> 24 | 0x80); write(v >>> 16);
      write(v >>>  8); write(v);
    } else if(v > 0x3F) {
      write(v >>>  8 | 0x40); write(v);
    } else {
      write(v);
    }
  }

  /**
   * Writes the next byte.
   * @param b byte to be written
   */
  private synchronized void write(final int b) {
    if(off == BUFSIZE) {
      nextBlock();
      off = 0;
    }
    buffer[c][off++] = (byte) b;
    if(len < pos[c] + off) len = pos[c] + off;
    dirty[c] = true;
    dirt = true;
  }
}
