package org.basex.io.in;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class uses an internal buffer to speed up input stream processing.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class BufferInput extends InputStream {
  /** Size of the input buffer. */
  private static final int CAPACITY = 1 << 14;

  /** Byte buffer. */
  final byte[] array;
  /** Current buffer position. */
  int bpos;
  /** Current buffer size. */
  int bsize;
  /** Total length of input to be processed ({@code -1} if unknown). */
  long length;
  /** Input file (can be {@code null}). */
  private IO input;

  /** Reference to the data input stream (can be {@code null}). */
  private final InputStream is;
  /** Buffer marker to jump back ({@code -1} if not available). */
  private int bmark;
  /** Number of read bytes. */
  private long read;

  /**
   * Returns a buffered input stream.
   * @param is input stream
   * @return stream
   */
  public static BufferInput get(final InputStream is) {
    return is instanceof final BufferInput bi ? bi : new BufferInput(is);
  }

  /**
   * Returns a buffered input stream.
   * @param input input to be read
   * @return stream
   * @throws IOException I/O Exception
   */
  public static BufferInput get(final IO input) throws IOException {
    final InputStream is = input.inputStream();
    return is instanceof final BufferInput bi ? bi : new BufferInput(is, input.length());
  }

  /**
   * Constructor.
   * @param input input to be read
   * @throws IOException I/O Exception
   */
  public BufferInput(final IO input) throws IOException {
    this(input.inputStream());
    this.input = input;
    length = input.length();
  }

  /**
   * Constructor.
   * @param is input stream
   */
  protected BufferInput(final InputStream is) {
    this(is, is instanceof final BufferInput bi ? bi.length : -1);
  }

  /**
   * Initializes the file reader.
   * @param is input stream
   * @param length of input ({@code -1} if unknown)
   */
  private BufferInput(final InputStream is, final long length) {
    this.is = is;
    this.length = length;
    array = new byte[CAPACITY];
  }

  /**
   * Empty constructor with fixed input.
   * @param array array input
   */
  protected BufferInput(final byte[] array) {
    this.array = array;
    bsize = array.length;
    length = bsize;
    is = null;
  }

  /**
   * Returns the IO reference.
   * @return file reference, or {@code null}
   */
  public final IO io() {
    return input;
  }

  /**
   * Returns the next byte. By default, this method calls {@link #readByte()};
   * {@code -1} is returned if all bytes have been read.
   * @return next byte
   * @throws IOException I/O exception
   */
  @Override
  public int read() throws IOException {
    return readByte();
  }

  /**
   * Returns the next unsigned byte.
   * {@code -1} is returned if all bytes have been read.
   * @return next unsigned byte
   * @throws IOException I/O exception
   * @see InputStream#read()
   */
  protected int readByte() throws IOException {
    final int blen = array.length;
    final byte[] buf = array;
    if(bpos >= bsize) {
      if(bsize == blen) {
        // reset mark if buffer is full
        read += bsize;
        bmark = -1;
        bsize = 0;
        bpos = 0;
      }
      int r;
      while((r = is.read(buf, bsize, blen - bsize)) == 0);
      if(r < 0) return -1;
      bsize += r;
    }
    return buf[bpos++] & 0xFF;
  }

  /**
   * Reads a string from the input stream, suffixed by a {@code 0} byte.
   * @return string
   * @throws IOException I/O Exception
   */
  public final String readString() throws IOException {
    final ByteList bl = new ByteList();
    for(int l; (l = read()) > 0;) bl.add(l);
    return bl.toString();
  }

  /**
   * Reads a byte array from the input stream, suffixed by a {@code 0} byte.
   * @return token
   * @throws IOException I/O Exception
   */
  public final byte[] readBytes() throws IOException {
    final ByteList bl = new ByteList();
    for(int l; (l = readByte()) > 0;) bl.add(l);
    return bl.finish();
  }

  @Override
  public final void close() throws IOException {
    if(is != null && !(is instanceof FilterInputStream)) is.close();
  }

  /**
   * Returns the number of read bytes.
   * @return read bytes
   */
  public final long size() {
    return read + bpos;
  }

  /**
   * Returns the buffer position.
   * @return buffer position
   */
  public final long position() {
    return bpos;
  }

  /**
   * Returns the input length (can be {@code -1}).
   * @return input length
   */
  public final long length() {
    return length;
  }

  @Override
  public final boolean markSupported() {
    return true;
  }

  @Override
  public final void mark(final int m) {
    bmark = bpos;
  }

  @Override
  public final void reset() throws IOException {
    if(bmark == -1) throw new IOException("Mark cannot be reset.");
    bpos = bmark;
  }

  /**
   * Retrieves and returns the whole data and closes the stream.
   * @return contents
   * @throws IOException I/O exception
   */
  public byte[] content() throws IOException {
    try {
      if(length > -1) {
        // input length is known in advance
        final int sl = Array.checkCapacity(length);
        final byte[] bytes = new byte[sl];
        final int s = fill(bytes, sl);
        return s == sl ? bytes : Arrays.copyOf(bytes, s);
      }
      // parse until end of stream
      final ByteList list = new ByteList();
      final byte[] bytes = new byte[CAPACITY];
      for(int s; (s = fill(bytes, bytes.length)) > 0;) list.add(bytes, 0, s);
      return list.finish();
    } finally {
      close();
    }
  }

  /**
   * Fills the specified array with buffered bytes and bytes from the input stream.
   * @param bytes target array
   * @param end target end position
   * @return number of assigned bytes
   * @throws IOException I/O exception
   */
  private int fill(final byte[] bytes, final int end) throws IOException {
    // adopt buffered bytes, request the remaining ones from the input stream
    final int buffered = Math.min(bsize - bpos, end);
    Array.copy(array, bpos, buffered, bytes, 0);
    bpos += buffered;
    int o = buffered;
    while(o < end) {
      final int r = is.read(bytes, o, end - o);
      if(r < 0) break;
      o += r;
    }
    read += o - buffered;
    return o;
  }
}
