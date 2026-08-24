package org.basex.io.random;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class allows positional read and write access to a database file.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DataAccess implements Closeable {
  /** Number of file readers (must be 1 << n). */
  private static final int READERS = 1 << 4;
  /** Maximum number of blocks to read ahead (must be 1 << n, at most half of the buffers). */
  private static final int AHEAD = 1 << 3;

  /** File readers, hashed by thread ID (entries can be {@code null}). */
  private final Reader[] readers = new Reader[READERS];
  /** Reader with write access; used for updates and if no other reader is available. */
  private final Reader master;
  /** File reference. */
  private final IOFile file;
  /** Indicates if the file supports thread-confined readers. */
  private final boolean poolable;
  /** Indicates if additional readers can be assigned. */
  private volatile boolean pooled;
  /** File size. */
  private volatile long length;
  /** Changed flag. */
  private boolean changed;

  /**
   * Constructor, initializing the file reader.
   * @param file the file to be read
   * @throws IOException I/O Exception
   */
  public DataAccess(final IOFile file) throws IOException {
    this(file, false);
  }

  /**
   * Constructor, initializing the file reader.
   * @param file the file to be read
   * @param poolable assign readers to threads; must be {@code false} if the caller positions
   *   the cursor and writes in separate calls
   * @throws IOException I/O Exception
   */
  public DataAccess(final IOFile file, final boolean poolable) throws IOException {
    this.file = file;
    this.poolable = poolable;
    pooled = poolable;
    Reader reader = null;
    try {
      reader = new Reader(true, null);
      master = reader;
      length = master.raf.length();
      master.cursor(0);
    } catch(final IOException ex) {
      if(reader != null) reader.raf.close();
      throw ex;
    }
  }

  /**
   * Flushes the buffered data.
   */
  public void flush() {
    synchronized(master) {
      try {
        for(final Buffer buffer : master.buffers.all()) {
          if(buffer.dirty) master.writeBlock(buffer);
        }
        if(changed) {
          master.raf.setLength(length);
          changed = false;
        }
        // all data has been written: readers can be assigned again
        pooled = poolable;
      } catch(final IOException ex) {
        Util.stack(ex);
      }
    }
  }

  @Override
  public void close() {
    flush();
    try {
      drain();
      synchronized(master) {
        master.raf.close();
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Returns the current file position.
   * @return position in the file
   */
  public long cursor() {
    return reader().position();
  }

  /**
   * Returns the file length.
   * @return file length
   */
  public long length() {
    return length;
  }

  /**
   * Checks if more bytes can be read.
   * @return result of check
   */
  public boolean more() {
    return cursor() < length;
  }

  /**
   * Reads a byte value from the specified position.
   * @param pos position
   * @return integer value
   */
  public byte read1(final long pos) {
    final Reader reader = reader();
    if(reader != master) return reader.read1(pos);
    synchronized(master) {
      return master.read1(pos);
    }
  }

  /**
   * Reads a byte value.
   * @return integer value
   */
  public byte read1() {
    final Reader reader = reader();
    if(reader != master) return reader.read1();
    synchronized(master) {
      return master.read1();
    }
  }

  /**
   * Reads an integer value from the specified position.
   * @param pos position
   * @return integer value
   */
  public int read4(final long pos) {
    final Reader reader = reader();
    if(reader != master) return reader.read4(pos);
    synchronized(master) {
      return master.read4(pos);
    }
  }

  /**
   * Reads an integer value.
   * @return integer value
   */
  public int read4() {
    final Reader reader = reader();
    if(reader != master) return reader.read4();
    synchronized(master) {
      return master.read4();
    }
  }

  /**
   * Reads a 5-byte value from the specified file offset.
   * @param pos position
   * @return long value
   */
  public long read5(final long pos) {
    final Reader reader = reader();
    if(reader != master) return reader.read5(pos);
    synchronized(master) {
      return master.read5(pos);
    }
  }

  /**
   * Reads a 5-byte value.
   * @return long value
   */
  public long read5() {
    final Reader reader = reader();
    if(reader != master) return reader.read5();
    synchronized(master) {
      return master.read5();
    }
  }

  /**
   * Reads a {@link Num} value from disk.
   * @param pos text position
   * @return read num
   */
  public int readNum(final long pos) {
    final Reader reader = reader();
    if(reader != master) return reader.readNum(pos);
    synchronized(master) {
      return master.readNum(pos);
    }
  }

  /**
   * Reads a {@link Num} value from the specified position, or the one that follows it.
   * @param pos text position
   * @param next read the subsequent value
   * @return read num
   */
  public int readNum(final long pos, final boolean next) {
    final Reader reader = reader();
    if(reader != master) return reader.readNum(pos, next);
    synchronized(master) {
      return master.readNum(pos, next);
    }
  }

  /**
   * Reads the next compressed number and returns it as integer.
   * @return next integer
   */
  public int readNum() {
    final Reader reader = reader();
    if(reader != master) return reader.readNum();
    synchronized(master) {
      return master.readNum();
    }
  }

  /**
   * Reads a token from disk.
   * @param pos text position
   * @return text as byte array
   */
  public byte[] readToken(final long pos) {
    final Reader reader = reader();
    if(reader != master) return reader.readToken(pos);
    synchronized(master) {
      return master.readToken(pos);
    }
  }

  /**
   * Reads the next token from disk.
   * @return text as byte array
   */
  public byte[] readToken() {
    final Reader reader = reader();
    if(reader != master) return reader.readToken();
    synchronized(master) {
      return master.readToken();
    }
  }

  /**
   * Reads a number of bytes from the specified offset.
   * @param pos position
   * @param len length
   * @return byte array
   */
  public byte[] readBytes(final long pos, final int len) {
    final Reader reader = reader();
    if(reader != master) return reader.readBytes(pos, len);
    synchronized(master) {
      return master.readBytes(pos, len);
    }
  }

  /**
   * Reads a number of bytes.
   * @param len length
   * @return byte array
   */
  public byte[] readBytes(final int len) {
    final Reader reader = reader();
    if(reader != master) return reader.readBytes(len);
    synchronized(master) {
      return master.readBytes(len);
    }
  }

  /**
   * Sets the disk cursor.
   * @param pos read position
   */
  public void cursor(final long pos) {
    reader().cursor(pos);
  }

  /**
   * Writes a 5-byte value to the specified position.
   * @param pos position in the file
   * @param value value to be written
   */
  public void write5(final long pos, final long value) {
    final Reader writer = writer();
    writer.cursor(pos);
    writer.write((byte) (value >>> 32));
    writer.write((byte) (value >>> 24));
    writer.write((byte) (value >>> 16));
    writer.write((byte) (value >>> 8));
    writer.write((byte) value);
  }

  /**
   * Writes an integer value to the specified position.
   * @param pos write position
   * @param value byte array to be appended
   */
  public void write4(final long pos, final int value) {
    final Reader writer = writer();
    writer.cursor(pos);
    writer.write4(value);
  }

  /**
   * Writes an integer value to the file.
   * @param value value to be written
   */
  public void write4(final int value) {
    writer().write4(value);
  }

  /**
   * Writes a number to the file.
   * @param value value to be written
   */
  public void writeNum(final int value) {
    writer().writeNum(value);
  }

  /**
   * Writes a byte array to the file.
   * @param data data containing the bytes to be written
   * @param offset offset of first byte
   * @param len number of bytes to be written
   */
  public void writeBytes(final byte[] data, final int offset, final int len) {
    writer().writeBytes(data, offset, len);
  }

  /**
   * Writes a token to the file.
   * @param pos write position
   * @param value value to be written
   */
  public void writeToken(final long pos, final byte[] value) {
    final Reader writer = writer();
    writer.cursor(pos);
    final int len = value.length;
    writer.writeNum(len);
    writer.writeBytes(value, 0, len);
  }

  /**
   * Returns the offset to a free slot for writing an entry with the specified length.
   * Fills the original space with 0xFF to facilitate future write operations.
   * @param pos original offset
   * @param size size of new text entry
   * @return new offset to store text
   */
  public long free(final long pos, final int size) {
    final Reader writer = writer();
    // old text size (available space)
    int os = writer.readNum(pos) + (int) (writer.position() - pos);

    // extend available space by subsequent zero-bytes
    writer.cursor(pos + os);
    for(; pos + os < length && os < size && writer.read() == 0xFF; os++);

    long o = pos;
    if(pos + os == length) {
      // entry is placed last: reset file length (discard last entry)
      length(pos);
    } else {
      int t = size;
      if(os < size) {
        // gap is too small for new entry...
        // reset cursor to overwrite entry
        writer.cursor(pos);
        t = 0;
        // place new entry after last entry
        o = length;
      } else {
        // gap is large enough: set cursor to overwrite remaining bytes
        writer.cursor(pos + size);
      }
      // fill gap with 0xFF for future updates
      while(t++ < os) writer.write(0xFF);
    }
    return o;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns a file reader for the current thread.
   * @return file reader
   */
  private Reader reader() {
    if(!pooled) return master;
    // a reader is confined to its owner, so its cursor and buffers need no monitor
    final Thread thread = Thread.currentThread();
    final Reader reader = readers[(int) (thread.threadId() & READERS - 1)];
    return reader != null && reader.owner == thread ? reader : newReader(thread);
  }

  /**
   * Assigns a file reader to the specified thread.
   * @param thread thread to assign the reader to
   * @return file reader
   */
  private Reader newReader(final Thread thread) {
    synchronized(readers) {
      if(!pooled) return master;
      final int start = (int) (thread.threadId() & READERS - 1);
      for(int i = 0; i < READERS; i++) {
        final int r = start + i & READERS - 1;
        final Reader reader = readers[r];
        if(reader == null) {
          try {
            final Reader rd = new Reader(false, thread);
            readers[r] = rd;
            return rd;
          } catch(final IOException ex) {
            // fall back to the master reader (e.g. if too many files are open)
            Util.debug(ex);
            return master;
          }
        }
        // adopt the reader of a thread that has terminated
        if(reader.owner == thread || !reader.owner.isAlive()) {
          reader.owner = thread;
          return reader;
        }
      }
      // all readers are in use: fall back to the shared master reader
      return master;
    }
  }

  /**
   * Returns the master reader and discards the cached data of all other readers.
   * @return master reader
   */
  private Reader writer() {
    if(pooled) {
      try {
        drain();
      } catch(final IOException ex) {
        Util.stack(ex);
      }
    }
    return master;
  }

  /**
   * Closes all assigned file readers.
   * @throws IOException I/O exception
   */
  private void drain() throws IOException {
    pooled = false;
    synchronized(readers) {
      // no reader can be in flight: the caller holds the write lock of the database
      for(int r = 0; r < READERS; r++) {
        final Reader reader = readers[r];
        if(reader != null) {
          readers[r] = null;
          reader.closed = true;
          reader.raf.close();
        }
      }
    }
  }

  /**
   * Sets the file length.
   * @param len file length
   */
  private void length(final long len) {
    if(len != length) {
      changed = true;
      length = len;
    }
  }

  /**
   * Positional read and write access with an exclusive file handle and buffer pool.
   */
  private final class Reader {
    /** Buffer manager. */
    private final Buffers buffers = new Buffers();
    /** Reference to the data input stream. */
    private final RandomAccessFile raf;
    /** Thread this reader is confined to; only assigned while its previous owner is gone. */
    private Thread owner;
    /** Indicates that the reader was closed by a concurrent update. */
    private volatile boolean closed;
    /** Offset. */
    private int off;
    /** Number of blocks to read ahead. */
    private int ahead = 1;
    /** Block that continues the current sequential run. */
    private long nextRead = -1;
    /** Buffer for reading ahead (can be {@code null}). */
    private byte[] scratch;

    /**
     * Constructor.
     * @param write open file for writing
     * @param owner thread this reader is confined to (can be {@code null})
     * @throws IOException I/O exception
     */
    private Reader(final boolean write, final Thread owner) throws IOException {
      raf = new RandomAccessFile(file.file(), write ? "rw" : "r");
      this.owner = owner;
    }

    /**
     * Returns the current file position.
     * @return position in the file
     */
    private long position() {
      return buffer(false).pos + off;
    }

    /**
     * Reads a byte value from the specified position.
     * @param pos position
     * @return integer value
     */
    private byte read1(final long pos) {
      cursor(pos);
      return read1();
    }

    /**
     * Reads a byte value.
     * @return integer value
     */
    private byte read1() {
      return (byte) read();
    }

    /**
     * Reads an integer value from the specified position.
     * @param pos position
     * @return integer value
     */
    private int read4(final long pos) {
      cursor(pos);
      return read4();
    }

    /**
     * Reads an integer value.
     * @return integer value
     */
    private int read4() {
      return (read() << 24) + (read() << 16) + (read() << 8) + read();
    }

    /**
     * Reads a 5-byte value from the specified file offset.
     * @param pos position
     * @return long value
     */
    private long read5(final long pos) {
      cursor(pos);
      return read5();
    }

    /**
     * Reads a 5-byte value.
     * @return long value
     */
    private long read5() {
      return ((long) read() << 32) + ((long) read() << 24) + (read() << 16) + (read() << 8) +
        read();
    }

    /**
     * Reads a {@link Num} value from disk.
     * @param pos text position
     * @return read num
     */
    private int readNum(final long pos) {
      cursor(pos);
      return readNum();
    }

    /**
     * Reads a {@link Num} value from the specified position, or the one that follows it.
     * @param pos text position
     * @param next read the subsequent value
     * @return read num
     */
    private int readNum(final long pos, final boolean next) {
      cursor(pos);
      final int value = readNum();
      return next ? readNum() : value;
    }

    /**
     * Reads the next compressed number and returns it as integer.
     * @return next integer
     */
    private int readNum() {
      final int value = read();
      return switch(value & 0xC0) {
        case 0    -> value;
        case 0x40 -> (value - 0x40 << 8) + read();
        case 0x80 -> (value - 0x80 << 24) + (read() << 16) + (read() << 8) + read();
        default   -> (read() << 24) + (read() << 16) + (read() << 8) + read();
      };
    }

    /**
     * Reads a token from disk.
     * @param pos text position
     * @return text as byte array
     */
    private byte[] readToken(final long pos) {
      cursor(pos);
      return readToken();
    }

    /**
     * Reads the next token from disk.
     * @return text as byte array
     */
    private byte[] readToken() {
      final int l = readNum();
      return readBytes(l);
    }

    /**
     * Reads a number of bytes from the specified offset.
     * @param pos position
     * @param len length
     * @return byte array
     */
    private byte[] readBytes(final long pos, final int len) {
      cursor(pos);
      return readBytes(len);
    }

    /**
     * Reads a number of bytes.
     * @param len length
     * @return byte array
     */
    private byte[] readBytes(final int len) {
      int l = len, ll = IO.BLOCKSIZE - off;
      final byte[] data = new byte[l];
      Array.copyToStart(buffer(false).data, off, Math.min(l, ll), data);
      if(l > ll) {
        l -= ll;
        while(l > IO.BLOCKSIZE) {
          Array.copyFromStart(buffer(true).data, IO.BLOCKSIZE, data, ll);
          ll += IO.BLOCKSIZE;
          l -= IO.BLOCKSIZE;
        }
        Array.copyFromStart(buffer(true).data, l, data, ll);
      }
      off += l;
      return data;
    }

    /**
     * Sets the disk cursor.
     * @param pos read position
     */
    private void cursor(final long pos) {
      off = (int) (pos & IO.BLOCKSIZE - 1);
      final long b = pos - off;
      if(!buffers.cursor(b)) return;

      final Buffer buffer = buffers.current();
      try {
        if(buffer.dirty) writeBlock(buffer);
        buffer.pos = b;
        // blocks that continue a sequential run are fetched in a single request; the master
        // reader is excluded, as it may hold data that has not been written to disk yet
        ahead = owner != null && b == nextRead ? Math.min(ahead << 1, AHEAD) : 1;
        final int count = (int) Math.min(ahead, length - b >> IO.BLOCKPOWER);
        nextRead = b + ((long) Math.max(count, 1) << IO.BLOCKPOWER);
        raf.seek(b);
        if(b < raf.length()) {
          if(count < 2) {
            raf.readFully(buffer.data, 0, (int) Math.min(length - b, IO.BLOCKSIZE));
          } else if(readAhead(b, count)) {
            // the requested block was evicted while reading ahead: fetch it again
            final Buffer current = buffers.current();
            current.pos = b;
            raf.seek(b);
            raf.readFully(current.data, 0, IO.BLOCKSIZE);
          }
        }
      } catch(final IOException ex) {
        // queries are compiled before database locks are acquired: a concurrent update may
        // have closed this reader in the meantime
        if(!closed) {
          Util.stack(ex);
        } else {
          buffers.cursor(b);
          final Buffer current = buffers.current();
          current.pos = b;
          readMaster(current);
        }
      }
    }

    /**
     * Reads consecutive blocks in a single request and caches them.
     * @param b first block position
     * @param count number of blocks
     * @return flag indicating if the first block was evicted again
     * @throws IOException I/O exception
     */
    private boolean readAhead(final long b, final int count) throws IOException {
      if(scratch == null) scratch = new byte[AHEAD << IO.BLOCKPOWER];
      raf.readFully(scratch, 0, count << IO.BLOCKPOWER);
      Array.copyToStart(scratch, 0, IO.BLOCKSIZE, buffers.current().data);
      for(int c = 1; c < count; c++) {
        final long pos = b + ((long) c << IO.BLOCKPOWER);
        buffers.cursor(pos);
        final Buffer buffer = buffers.current();
        if(buffer.dirty) writeBlock(buffer);
        buffer.pos = pos;
        Array.copyToStart(scratch, c << IO.BLOCKPOWER, IO.BLOCKSIZE, buffer.data);
      }
      return buffers.cursor(b);
    }

    /**
     * Reads a block through the master reader.
     * @param buffer target buffer
     */
    private void readMaster(final Buffer buffer) {
      synchronized(master) {
        try {
          master.raf.seek(buffer.pos);
          if(buffer.pos < master.raf.length()) {
            master.raf.readFully(buffer.data, 0,
                (int) Math.min(length - buffer.pos, IO.BLOCKSIZE));
          }
        } catch(final IOException ex) {
          Util.stack(ex);
        }
      }
    }

    /**
     * Writes an integer value to the file.
     * @param value value to be written
     */
    private void write4(final int value) {
      write(value >>> 24);
      write(value >>> 16);
      write(value >>>  8);
      write(value);
    }

    /**
     * Writes a number to the file.
     * @param value value to be written
     */
    private void writeNum(final int value) {
      if(value < 0 || value > 0x3FFFFFFF) {
        write(0xC0);
        write(value >>> 24);
        write(value >>> 16);
        write(value >>> 8);
      } else if(value > 0x3FFF) {
        write(value >>> 24 | 0x80);
        write(value >>> 16);
        write(value >>> 8);
      } else if(value > 0x3F) {
        write(value >>> 8 | 0x40);
      }
      write(value);
    }

    /**
     * Writes a byte array to the file.
     * @param data data containing the bytes to be written
     * @param offset offset of first byte
     * @param len number of bytes to be written
     */
    private void writeBytes(final byte[] data, final int offset, final int len) {
      final int last = offset + len;
      int o = offset;

      while(o < last) {
        final Buffer buffer = buffer();
        final int l = Math.min(last - o, IO.BLOCKSIZE - off);
        Array.copy(data, o, l, buffer.data, off);
        buffer.dirty = true;
        off += l;
        o += l;
        // adjust file size
        final long nl = buffer.pos + off;
        if(nl > length) length(nl);
      }
    }

    /**
     * Reads the next byte.
     * @return next byte
     */
    private int read() {
      final Buffer buffer = buffer();
      return buffer.data[off++] & 0xFF;
    }

    /**
     * Writes the next byte.
     * @param value byte to be written
     */
    private void write(final int value) {
      final Buffer buffer = buffer();
      buffer.dirty = true;
      buffer.data[off++] = (byte) value;
      final long nl = buffer.pos + off;
      if(nl > length) length(nl);
    }

    /**
     * Writes the specified block to disk.
     * @param buffer buffer to write
     * @throws IOException I/O exception
     */
    private void writeBlock(final Buffer buffer) throws IOException {
      final long pos = buffer.pos, len = Math.min(IO.BLOCKSIZE, length - pos);
      raf.seek(pos);
      raf.write(buffer.data, 0, (int) len);
      buffer.dirty = false;
    }

    /**
     * Returns a buffer which can be used for writing new bytes.
     * @return buffer
     */
    private Buffer buffer() {
      return buffer(off == IO.BLOCKSIZE);
    }

    /**
     * Returns the current or next buffer.
     * @param next next block
     * @return buffer
     */
    private Buffer buffer(final boolean next) {
      if(next) cursor(buffers.current().pos + IO.BLOCKSIZE);
      return buffers.current();
    }
  }
}
