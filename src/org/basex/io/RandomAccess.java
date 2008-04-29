package org.basex.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class allows positional read access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class RandomAccess {
  /** Default buffer size - must be a power of two. */
  private static final int BUFSIZE = 1 << IO.BLOCKPOWER;
  /** Buffer limit (buffer size - 1). */
  private static final int BUFLIMIT = BUFSIZE - 1;
  /** Reference to the data input stream. */
  private final RandomAccessFile file;
  /** File length. */
  private long len;
  /** Buffer. */
  private final byte[] buffer = new byte[4096];
  /** Current position. */
  private long pos;
  /** Current buffer position. */
  private int off;

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public RandomAccess(final File f) throws IOException {
    file = new RandomAccessFile(f, "r");
    file.read(buffer);
    len = file.length();
  }
  
  /**
   * Closes the data access.
   * @throws IOException in case of write errors
   */
  public synchronized void close() throws IOException {
    file.close();
  }

  /**
   * Sets the disk cursor.
   * @param p read position
   * @throws IOException I/O exception
   */
  public void cursor(final long p) throws IOException {
    final int pp = (int) (p & BUFLIMIT);
    final long ps = p - pp;
    if(ps != pos) {
      pos = ps;
      file.seek(ps);
      file.read(buffer);
    }
    off = pp;
  }

  /**
   * Reads the next byte.
   * @return next byte
   * @throws IOException I/O exception
   */
  public int read() throws IOException {
    if(off == BUFSIZE) {
      pos += BUFSIZE;
      off = 0;
      file.read(buffer);
    }
    return buffer[off++] & 0xFF;
  }

  /**
   * Returns the input position.
   * @return position
   */
  public long pos() {
    return pos + off;
  }

  /**
   * Checks if more bytes can be read.
   * @return result of check
   */
  public boolean more() {
    return pos() < len;
  }
}
