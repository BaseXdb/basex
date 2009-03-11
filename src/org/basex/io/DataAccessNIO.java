package org.basex.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.basex.util.Num;

/**
 * This class allows positional read and write access to a database file.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DataAccessNIO {
  /** Reference to the data input stream. */
  private final RandomAccessFile file;
  /** File length. */
  private long len;
  /** Read Write file channel. */
  private FileChannel fc;
  /** Mapped Byte Buffer Window. */
  private MappedByteBuffer mbytebuffer;
  /** Multiplicator. */
  private static final int MULTI = 100;
  /** Direct byte buffer size. */
  private static final int DBBUFFERSIZE = IO.BLOCKSIZE;
  /** Window size. Choose a multiple of block size. */
  private static final int BUFFERSIZE =  MULTI * IO.BLOCKSIZE;
  /** Direct byte buffer for updates. */
  private final ByteBuffer dbbuffer;
  /** Offset. */
  private long off;
  /** Writing Position. */
  private long writePosition;

  /**
   * Constructor, initializing the file reader.
   * @param db name of the database
   * @param fn the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessNIO(final String db, final String fn) throws IOException {
    this(IO.dbfile(db, fn));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessNIO(final String f) throws IOException {
    this(new File(f));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessNIO(final File f) throws IOException {
    file = new RandomAccessFile(f, "rw");
    fc = file.getChannel();
    len = file.length();
    dbbuffer = ByteBuffer.allocateDirect(DBBUFFERSIZE);
    if(len <= BUFFERSIZE) {
      mbytebuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, len);
    } else {
      mbytebuffer = fc.map(FileChannel.MapMode.READ_WRITE,
          0, BUFFERSIZE);
    }
    // init offset
    off = 0;
    // init write position
    writePosition = 0;
  }

  /**
   * Flushes the buffered data.
   */
  public synchronized void flush() {
    try {
      fc.position(writePosition);
      fc.write(dbbuffer);
    } catch(IOException e) {
      e.printStackTrace();
    }
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
    try {
      len = file.length();
    } catch(IOException e) {
      e.printStackTrace();
    }
    return len;
  }

  /**
   * Reads an 5-byte value from the specified file offset.
   * @param p position
   * @return long value
   */
  public synchronized long read5(final long p) {
    cursor(p);
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
    for(int i = 0; i < l; i++) {
      try {
        b[i] = mbytebuffer.get();
      } catch (BufferUnderflowException e) {
        moveWindow(off + BUFFERSIZE);
      }
    }
    return b;
  }

  /**
   * Returns the current file position for reading purposes.
   * @return text as byte array
   */
  public synchronized long pos() {
    return off + mbytebuffer.position();
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
   * Reads a byte value from the specified position.
   * @param p position
   * @return integer value
   */
  public synchronized byte readByte(final long p) {
    cursor(p);
    return readByte();
  }

  /**
   * Reads a byte value.
   * @return integer value
   */
  public synchronized byte readByte() {
    return (byte) read();
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
    // can array size exceed remaining buffer size???
    mbytebuffer.get(array);
    return array;
  }

  /**
   * Append a value to the file and return it's offset.
   * @param p write position
   * @param v byte array to be appended
   */
  public synchronized void writeBytes(final long p, final byte[] v) {
    try {
      writePosition = p;
      fc.position(p);
    } catch(IOException e) {
      e.printStackTrace();
    }
    writeNum(v.length);
    for(final byte b : v) write(b);  }

  // private methods...

  /**
   * Moves reading window. Sets new offset and resets position.
   * @param p new position
   */
  private synchronized void moveWindow(final long p) {
    try {
      // check if mapped buffer exceeds remaining file length
      if((len - p) < BUFFERSIZE) {
        mbytebuffer = fc.map(FileChannel.MapMode.READ_WRITE, 
            p , len - p);
      } else {
        mbytebuffer = fc.map(FileChannel.MapMode.READ_WRITE, 
            p, BUFFERSIZE);
      }
      off = p;
      mbytebuffer.position(0);
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the disk cursor.
   * @param p read position
   */
  public synchronized void cursor(final long p) {
    // check if window has to be moved forward or backward
    if(p > (off + BUFFERSIZE) || p < off) {
      moveWindow(p);
    } else {
      // don't move window. Just set new position
      mbytebuffer.position((int) (p - off));
    }
  }
  
  /**
   * Reads the next byte.
   * @return next byte
   */
  public synchronized int read() {
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
   */
  public synchronized int readNum() {
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
  public synchronized int readInt() {
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
    // check if buffer is already full get new buffer
    if(off == DBBUFFERSIZE) {
      try {
        fc.write(dbbuffer);
        dbbuffer.clear();
        writePosition = writePosition + DBBUFFERSIZE;
      } catch(IOException e) {
        e.printStackTrace();
      }
      off = 0;
    }
    dbbuffer.put((byte) b);
  }
}
