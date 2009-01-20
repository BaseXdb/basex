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
public final class DataAccessMMA {
  /** Reference to the data input stream. */
  private final RandomAccessFile file;
  /** File length. */
  private long len;
  /** Read Write Filechannel. */
  private FileChannel rwChannel;
  /** Mapped Byte Buffer Window. */
  private MappedByteBuffer[] mbytebuffer;
  /** Window size. */
  private static final int BUFFERSIZE = 1 << 20;
  /** Specifies current buffer window. */
  private int selector;
  /** Offset. */
  private int off;
  /** Window array size. */
  private int numberofwindows;

  /**
   * Constructor, initializing the file reader.
   * @param db name of the database
   * @param fn the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessMMA(final String db, final String fn) throws IOException {
    this(IO.dbfile(db, fn));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessMMA(final String f) throws IOException {
    this(new File(f));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessMMA(final File f) throws IOException {
    file = new RandomAccessFile(f, "rw");
    rwChannel = file.getChannel();
    len = file.length();
    long tmplen = len;
    int i = 0;
    // (int) Math.ceil(len / BUFFERSIZE) + 1;
    numberofwindows = (int) (len - 1 + BUFFERSIZE) / BUFFERSIZE;
    mbytebuffer = new MappedByteBuffer[numberofwindows];
    while(tmplen > BUFFERSIZE) {
      mbytebuffer[i] = rwChannel.map(FileChannel.MapMode.READ_WRITE, 
          i * BUFFERSIZE, BUFFERSIZE);
      // perform persisting changes
      mbytebuffer[i].force();
      tmplen = tmplen - BUFFERSIZE;
      i++;
    } 
    if (tmplen != 0) {
      mbytebuffer[i] = rwChannel.map(FileChannel.MapMode.READ_WRITE, 
          0, tmplen);
      // perform persisting changes
      mbytebuffer[i].force();
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
 // checks if token length exceeds current window buffer size
    if(l > mbytebuffer[selector].remaining()) {
      int ll = 0;
      while(ll < l) {
        mbytebuffer[selector].get(b, ll, mbytebuffer[selector].remaining());
        ll = +mbytebuffer[selector].remaining();
        // move window
        selector++;
        mbytebuffer[selector].position(0);
        off = 0;
      }
    } else {
      mbytebuffer[selector].get(b);
    }
    return b;
  }

  /**
   * Returns the current file position.
   * @return text as byte array
   */
  public synchronized long pos() {
    return ((selector - 1) * BUFFERSIZE) + mbytebuffer[selector].position();
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
    for(int i = 0; i < array.length; i++) array[i] = (byte) read();
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
   * Sets the disk cursor.
   * @param p read position
   */
  public synchronized void cursor(final long p) {
    // select buffer window
    // selector = (int) Math.ceil(p / BUFFERSIZE);
    selector = (int) (p + BUFFERSIZE) / BUFFERSIZE - 1;
    // calculate offset
    off = (int) p % BUFFERSIZE; //(int) (p & BUFFERSIZE);
    // set pointer
    try {
      mbytebuffer[selector].position(off);
    }  catch (Exception e) {
      System.out.println(numberofwindows);
      System.out.println(selector);
      System.out.println(p);
      System.out.println(length());
      System.out.println(e);
      System.out.println(BUFFERSIZE);
      System.out.println(off);
      System.out.println("**********");
    }
  }

  /**
   * Reads the next byte.
   * @return next byte
   */
  public synchronized int read() {
    if(mbytebuffer[selector].position() == BUFFERSIZE) {
      // next buffer window
      selector++;
      mbytebuffer[selector].position(0);
      off = 0;
    }
    return mbytebuffer[selector].get() & 0xFF;
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
    if(mbytebuffer[selector].remaining() == 0) {
      // next buffer window
      selector++;
      // check if array 
      if(selector == numberofwindows) {
        
      } else {
        mbytebuffer[selector].position(0);
        off = 0;
      }
    }
    mbytebuffer[selector].put((byte) b);
//    mbytebuffer[selector].force();
  }
}
