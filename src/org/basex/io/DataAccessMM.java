package org.basex.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.basex.util.Num;

/**
 * This class allows positional read and write access to a database file.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DataAccessMM {
  /** Reference to the data input stream. */
  private final RandomAccessFile file;
  /** File length. */
  private long len;
  /** Read Write Filechannel. */
  private FileChannel rwChannel;
  /** Mapped Byte Buffer Window. */
  private MappedByteBuffer mbytebuffer;
  /** Window size. */
  private static final int BUFFERSIZE =  IO.BLOCKSIZE; //Integer.MAX_VALUE;
  /** Offset. */
  private long off;

  /**
   * Constructor, initializing the file reader.
   * @param db name of the database
   * @param fn the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessMM(final String db, final String fn) throws IOException {
    this(IO.dbfile(db, fn));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessMM(final String f) throws IOException {
    this(new File(f));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessMM(final File f) throws IOException {
    file = new RandomAccessFile(f, "rw");
    rwChannel = file.getChannel();
    len = file.length();
    if(len <= BUFFERSIZE) {
      mbytebuffer = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, len);
      off = 0;
    } else {
      mbytebuffer = rwChannel.map(FileChannel.MapMode.READ_WRITE, 
          0, BUFFERSIZE);
    }
    // secure persisting changes
    mbytebuffer.force();
  }

  /**
   * Closes the data access.
   * @throws IOException in case of write errors
   */
  public synchronized void close() throws IOException {
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
   * Reads an 5-byte value from the specified file offset.
   * @param p position
   * @return long value
   * @throws IOException if setting cursor went wrong
   */
  public synchronized long read5(final long p) throws IOException {
    cursor(p);
    return ((long) read() << 32) + ((long) read() << 24) +
      (read() << 16) + (read() << 8) + read();
  }

  /**
   * Reads a {@link Num} value from disk.
   * @param p text position
   * @return read num
   * @throws IOException if setting curser went wrong
   */
  public synchronized int readNum(final long p) throws IOException {
    cursor(p);
    return readNum();
  }

  /**
   * Reads a token from disk.
   * @param p text position
   * @return text as byte array
   * @throws IOException if setting curser went wrong
   */
  public synchronized byte[] readToken(final long p) throws IOException {
    cursor(p);
    int l = readNum();
    final byte[] b = new byte[l];
    // checks if token length exceeds current window buffer size
    if(l > mbytebuffer.remaining()) {
      int ll = 0;
      while(ll < l) {
        mbytebuffer.get(b, ll, mbytebuffer.remaining());
        ll = +mbytebuffer.remaining();
        moveWindow(off + BUFFERSIZE);
      }
    } else {
      mbytebuffer.get(b);
    }
    return b;
  }

  /**
   * Returns the current file position.
   * @return text as byte array
   */
  public synchronized long pos() {
    return off + mbytebuffer.position();
  }

  /**
   * Reads an integer value from the specified position.
   * @param p position
   * @return integer value
   * @throws IOException if setting curser went wrong
   */
  public synchronized int readInt(final long p) throws IOException {
    cursor(p);
    return readInt();
  }

  /**
   * Reads a byte value from the specified position.
   * @param p position
   * @return integer value
   * @throws IOException if setting curser went wrong
   */
  public synchronized byte readByte(final long p) throws IOException {
    cursor(p);
    return readByte();
  }

  /**
   * Reads a byte value.
   * @return integer value
   * @throws IOException if read went wrong
   */
  public synchronized byte readByte() throws IOException {
    return (byte) read();
  }

  /**
   * Reads a number of bytes in range from -> to and returns them as array.
   * @param from starting position for reading
   * @param to ending position for reading
   * @return byte array
   * @throws IOException if read went wrong
   */
  public synchronized byte[] readBytes(final long from, final long to) 
  throws IOException {
    final byte[] array = new byte[(int) (to - from)];
    cursor(from);
    // can array size exceed remaining buffer size???
    mbytebuffer.get(array);
    return array;
  }

  /**
   * Append a value to the file and return it's offset.
   * @param p write position
   * @param v byte array to be appended
   * @throws IOException if setting curser went wrong
   */
  public synchronized void writeBytes(final long p, final byte[] v) 
  throws IOException {
    cursor(p);
    writeNum(v.length);
    mbytebuffer.put(v);
  }
  
  // private methods...

  /**
   * Moves reading window. Sets new offset and resets position.
   * @param p new position
   * @throws IOException if mapping went wrong
   */
  private synchronized void moveWindow(final long p) throws IOException {
    // check if mapped buffer exceeds remaining file length
    if((len - p) < BUFFERSIZE) {
      mbytebuffer = rwChannel.map(FileChannel.MapMode.READ_WRITE, 
          p, len - p + 1);
    } else {
      mbytebuffer = rwChannel.map(FileChannel.MapMode.READ_WRITE, 
          p, BUFFERSIZE);
    }
    // position starts at 0
    off = p - 1;
    mbytebuffer.position(0);
  }
  
  /**
   * Sets the disk cursor.
   * @param p read position
   * @throws IOException if moving window went wrong
   */
  public synchronized void cursor(final long p) throws IOException {
    // check if window have to be moved forward or backward
    if(p > (off + BUFFERSIZE) || p < off) {
      moveWindow(p);
      mbytebuffer.position(0);
    } else {
      // don't move window. Just set new position 
      mbytebuffer.position((int) (p - off));
    }
  }

  /**
   * Reads the next byte.
   * @return next byte
   * @throws IOException if moving window went wrong
   */
  public synchronized int read() throws IOException {
    if(mbytebuffer.position() == BUFFERSIZE) {
      moveWindow(off + BUFFERSIZE);
    }
    return mbytebuffer.get() & 0xFF;
  }

  /**
   * Checks if more bytes can be read.
   * @return result of check
   */
  public synchronized boolean more() {
    return pos() < len;
  }

  /**
   * Reads the next compressed number and returns it as integer.
   * @return next integer
   * @throws IOException if read went wrong
   */
  public synchronized int readNum() throws IOException {
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
   * @throws IOException if read went wrong
   */
  public synchronized int readInt() throws IOException {
    return (read() << 24) + (read() << 16) + (read() << 8) + read();
  }
  
  /**
   * Append a value to the file and return it's offset.
   * @param v number to be appended
   * @throws IOException if write went wrong
   */
  private synchronized void writeNum(final int v) throws IOException {
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
   * @throws IOException if write went wrong
   */
  private synchronized void write(final int b) throws IOException {
    if(mbytebuffer.remaining() == 0) {
      moveWindow(off + BUFFERSIZE);
    }
    mbytebuffer.put((byte) b);
  }
}
