package org.basex.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.basex.util.Num;

/**
 * This class allows positional read and write access to a database file.
 *
 * NIO1: Use one MappedByteBufferList. Instead of reading a block from disk,
 * it only maps the specified region.
 * Mapping the Region is using alot of cpu time.
 * Method readToken(long) has bad performance.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DataAccessNIO1 {
  /** Buffer limit (buffer size - 1). */
  private static final int BUFLIMIT = IO.BLOCKSIZE - 1;
  /** Blocksize. */
  private static final int BLOCKSIZE = IO.BLOCKSIZE;

  /** Number of buffers. */
  private static final int BUFFERS = 1;

  /** Reference to the data input stream. */
  private final RandomAccessFile file;
  /** File length. */
  private long len;

  /** Bytebuffer window list. */
  private ByteBufferList mbytebufferlist;
  
  /** Current MappedByteBuffer. */
  private MappedByteBuffer mbytebuffer;
  
  /** Read Write Filechannel. */
  private final FileChannel fc;
  
  /** tmp Buffer for writing. */
  private final ByteBuffer tmpblock;
  
  /** Block index. */
  private int blockindex;

  /** Positions. */
  private final long[] pos = new long[BUFFERS];
  /** Offset. */
  private int off;
  /** Current buffer reference. */
  private int c;

  /**
   * Constructor, initializing the file reader.
   * @param db name of the database
   * @param fn the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessNIO1(final String db, final String fn) throws IOException {
    this(IO.dbfile(db, fn));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessNIO1(final String f) throws IOException {
    this(new File(f));
  }

  /**
   * Constructor, initializing the file reader.
   * @param f the file to be read
   * @throws IOException IO Exception
   */
  public DataAccessNIO1(final File f) throws IOException {
    file = new RandomAccessFile(f, "rw");
    len = file.length();
    fc = file.getChannel();

    tmpblock = ByteBuffer.allocateDirect(BLOCKSIZE);
    mbytebufferlist = new ByteBufferList(1);
    readBlock(0);
    //optional
//    mbytebuffer.force();
    for(int i = 1; i < BUFFERS; i++) pos[i] = -1;
  }

  /**
   * Flushes the buffered data.
   */
  public synchronized void flush() {
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
    int ll = BLOCKSIZE - off;
    
    if(ll >= l) {
      for (int i = off; i < l + off; i++) {
        b[i - off] = mbytebuffer.get(i);
      }
    } else {
      for (int i = off; i < ll + off; i++)
        b[i - off] = mbytebuffer.get(i);
      l -= ll;
      while(l > BLOCKSIZE) {
        nextBlock();
        for (int i = 0; i < BLOCKSIZE; i++)
          b[ll + i] = mbytebuffer.get(i);
        l -= BLOCKSIZE;
        ll += BLOCKSIZE;
      }
      nextBlock();
      for (int i = 0; i < l; i++)
        b[ll + i] = mbytebuffer.get(i);
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
   * Reads the next block from disk.
   */
  private synchronized void nextBlock() {
    if (pos[c] + BLOCKSIZE == len - 1) {
      //create a new block
      try {
        fc.position(pos[c] + BLOCKSIZE);
        tmpblock.clear();
        fc.write(tmpblock);
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
    cursor(pos[c] + BLOCKSIZE);
  }
  
  /**
   * Sets the disk cursor.
   * @param p read position
   */
  public synchronized void cursor(final long p) {
    off = (int) (p & BUFLIMIT);
    
    final long ps = p - off;
    final int o = c;
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
      pos[c] = n;
      blockindex = (int) (n / BLOCKSIZE);
      if(blockindex < mbytebufferlist.length()) {
        // check if block is already mapped
        if(mbytebufferlist.get(blockindex) == null) {
          mbytebuffer = fc.map(FileChannel.MapMode.READ_WRITE,
              n, BLOCKSIZE);
          mbytebufferlist.set(mbytebuffer, blockindex);
        } else {
          // else choose mapped block
          mbytebuffer = mbytebufferlist.get(blockindex);
        }
      } else {
        mbytebuffer = fc.map(FileChannel.MapMode.READ_WRITE,
            n, BLOCKSIZE);
        mbytebufferlist.add(mbytebuffer);
      }
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Reads the next byte.
   * @return next byte
   */
  public synchronized int read() {
    if(off == BLOCKSIZE) {
      nextBlock();
      off = 0;
    }
    return mbytebuffer.get(off++) & 0xFF;
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
    if(off == BLOCKSIZE) {
      nextBlock();
      off = 0;
    }
    mbytebuffer.position(off);
    mbytebuffer.put((byte) b);
    off++;
    if(len < pos[c] + off) len = pos[c] + off;
  }
}
