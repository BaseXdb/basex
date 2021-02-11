package org.basex.io.random;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class allows positional read and write access to a database file.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DataAccess implements Closeable {
  /** Buffer manager. */
  private final Buffers buffers = new Buffers();
  /** Reference to the data input stream. */
  private final RandomAccessFile raf;
  /** File size. */
  private long length;
  /** Changed flag. */
  private boolean changed;
  /** Offset. */
  private int off;

  /**
   * Constructor, initializing the file reader.
   * @param file the file to be read
   * @throws IOException I/O Exception
   */
  public DataAccess(final IOFile file) throws IOException {
    RandomAccessFile f = null;
    try {
      f = new RandomAccessFile(file.file(), "rw");
      length = f.length();
      raf = f;
      cursor(0);
    } catch(final IOException ex) {
      if(f != null) f.close();
      throw ex;
    }
  }

  /**
   * Flushes the buffered data.
   */
  public synchronized void flush() {
    try {
      for(final Buffer buffer : buffers.all()) {
        if(buffer.dirty) writeBlock(buffer);
      }
      if(changed) {
        raf.setLength(length);
        changed = false;
      }
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  @Override
  public synchronized void close() {
    flush();
    try {
      raf.close();
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Returns the current file position.
   * @return position in the file
   */
  public long cursor() {
    return buffer(false).pos + off;
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
  public synchronized byte read1(final long pos) {
    cursor(pos);
    return read1();
  }

  /**
   * Reads a byte value.
   * @return integer value
   */
  public synchronized byte read1() {
    return (byte) read();
  }

  /**
   * Reads an integer value from the specified position.
   * @param pos position
   * @return integer value
   */
  public synchronized int read4(final long pos) {
    cursor(pos);
    return read4();
  }

  /**
   * Reads an integer value.
   * @return integer value
   */
  public synchronized int read4() {
    return (read() << 24) + (read() << 16) + (read() << 8) + read();
  }

  /**
   * Reads a 5-byte value from the specified file offset.
   * @param pos position
   * @return long value
   */
  public synchronized long read5(final long pos) {
    cursor(pos);
    return read5();
  }

  /**
   * Reads a 5-byte value.
   * @return long value
   */
  public synchronized long read5() {
    return ((long) read() << 32) + ((long) read() << 24) + (read() << 16) + (read() << 8) + read();
  }

  /**
   * Reads a {@link Num} value from disk.
   * @param pos text position
   * @return read num
   */
  public synchronized int readNum(final long pos) {
    cursor(pos);
    return readNum();
  }

  /**
   * Reads a token from disk.
   * @param pos text position
   * @return text as byte array
   */
  public synchronized byte[] readToken(final long pos) {
    cursor(pos);
    return readToken();
  }

  /**
   * Reads the next token from disk.
   * @return text as byte array
   */
  public synchronized byte[] readToken() {
    final int l = readNum();
    return readBytes(l);
  }

  /**
   * Reads a number of bytes from the specified offset.
   * @param pos position
   * @param len length
   * @return byte array
   */
  public synchronized byte[] readBytes(final long pos, final int len) {
    cursor(pos);
    return readBytes(len);
  }

  /**
   * Reads a number of bytes.
   * @param len length
   * @return byte array
   */
  public synchronized byte[] readBytes(final int len) {
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
  public void cursor(final long pos) {
    off = (int) (pos & IO.BLOCKSIZE - 1);
    final long b = pos - off;
    if(!buffers.cursor(b)) return;

    final Buffer buffer = buffers.current();
    try {
      if(buffer.dirty) writeBlock(buffer);
      buffer.pos = b;
      raf.seek(buffer.pos);
      if(buffer.pos < raf.length())
        raf.readFully(buffer.data, 0, (int) Math.min(length - buffer.pos, IO.BLOCKSIZE));
    } catch(final IOException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Reads the next compressed number and returns it as integer.
   * @return next integer
   */
  public synchronized int readNum() {
    final int value = read();
    switch(value & 0xC0) {
    case 0:
      return value;
    case 0x40:
      return (value - 0x40 << 8) + read();
    case 0x80:
      return (value - 0x80 << 24) + (read() << 16) + (read() << 8) + read();
    default:
      return (read() << 24) + (read() << 16) + (read() << 8) + read();
    }
  }

  /**
   * Writes a 5-byte value to the specified position.
   * @param pos position in the file
   * @param value value to be written
   */
  public void write5(final long pos, final long value) {
    cursor(pos);
    write((byte) (value >>> 32));
    write((byte) (value >>> 24));
    write((byte) (value >>> 16));
    write((byte) (value >>> 8));
    write((byte) value);
  }

  /**
   * Writes an integer value to the specified position.
   * @param pos write position
   * @param value byte array to be appended
   */
  public void write4(final long pos, final int value) {
    cursor(pos);
    write4(value);
  }

  /**
   * Writes an integer value to the file.
   * @param value value to be written
   */
  public void write4(final int value) {
    write(value >>> 24);
    write(value >>> 16);
    write(value >>>  8);
    write(value);
  }

  /**
   * Writes a number to the file.
   * @param value value to be written
   */
  public void writeNum(final int value) {
    if(value < 0 || value > 0x3FFFFFFF) {
      write(0xC0); write(value >>> 24); write(value >>> 16); write(value >>> 8); write(value);
    } else if(value > 0x3FFF) {
      write(value >>> 24 | 0x80); write(value >>> 16);
      write(value >>> 8); write(value);
    } else if(value > 0x3F) {
      write(value >>> 8 | 0x40); write(value);
    } else {
      write(value);
    }
  }

  /**
   * Writes a byte array to the file.
   * @param data data containing the bytes to be written
   * @param offset offset of first byte
   * @param len number of bytes to be written
   */
  public void writeBytes(final byte[] data, final int offset, final int len) {
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
   * Writes a token to the file.
   * @param pos write position
   * @param value value to be written
   */
  public void writeToken(final long pos, final byte[] value) {
    cursor(pos);
    final int len = value.length;
    writeNum(len);
    writeBytes(value, 0, len);
  }

  /**
   * Returns the offset to a free slot for writing an entry with the specified length.
   * Fills the original space with 0xFF to facilitate future write operations.
   * @param pos original offset
   * @param size size of new text entry
   * @return new offset to store text
   */
  public long free(final long pos, final int size) {
    // old text size (available space)
    int os = readNum(pos) + (int) (cursor() - pos);

    // extend available space by subsequent zero-bytes
    cursor(pos + os);
    for(; pos + os < length && os < size && read() == 0xFF; os++);

    long o = pos;
    if(pos + os == length) {
      // entry is placed last: reset file length (discard last entry)
      length(pos);
    } else {
      int t = size;
      if(os < size) {
        // gap is too small for new entry...
        // reset cursor to overwrite entry
        cursor(pos);
        t = 0;
        // place new entry after last entry
        o = length;
      } else {
        // gap is large enough: set cursor to overwrite remaining bytes
        cursor(pos + size);
      }
      // fill gap with 0xFF for future updates
      while(t++ < os) write(0xFF);
    }
    return o;
  }

  /**
   * Sets the file length.
   * @param len file length
   */
  private synchronized void length(final long len) {
    if(len != length) {
      changed = true;
      length = len;
    }
  }

  // PRIVATE METHODS ==============================================================================

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
