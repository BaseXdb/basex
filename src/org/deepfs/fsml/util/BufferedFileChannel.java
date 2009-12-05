package org.deepfs.fsml.util;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.basex.core.Prop;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * <h1>Buffered {@link FileChannel} implementation.</h1>
 * <p>
 * This implementation is optimized for sequential reading of a file or a
 * fragment of a file. An instance of this class can either represent a regular
 * file on disk or a fragment of a file (the instance is a "subchannel").
 * </p>
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class BufferedFileChannel {

  /** The default buffer size. */
  public static final int DEFAULT_BUFFER_SIZE = 1 << 12;

  /** The file we are reading from. */
  private final File f;
  /** The filename of the (sub)channel. */
  private final String name;
  /** The underlying {@link FileChannel} instance. */
  private final FileChannel fc;
  /** The {@link ByteBuffer} that is used for buffering the data. */
  private final ByteBuffer buf;
  /** The "first" byte of this {@link BufferedFileChannel} (absolute value). */
  private final long mark;
  /**
   * Maximum number of bytes that can be read from the FileChannel. The value
   * may be negative. In this case, the rightmost <code>-rem</code> bytes of the
   * buffer <code>buf</code> must not be read.
   */
  private long rem;

  /**
   * Standard constructor for creating a {@link BufferedFileChannel} from a
   * complete file.
   * @param file the file to read from
   * @throws IOException if any error occurs while creating the
   *           {@link BufferedFileChannel}
   */
  public BufferedFileChannel(final File file) throws IOException {
    this(file, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Creates a {@link BufferedFileChannel} from a complete file with the given
   * buffer size.
   * @param file the file to read from
   * @param bufferSize the size of the buffer
   * @throws IOException if any error occurs while creating the
   *           {@link BufferedFileChannel}
   */
  public BufferedFileChannel(final File file, final int bufferSize)
      throws IOException {
    this(file, ByteBuffer.allocate(bufferSize));
  }

  /**
   * Creates a {@link BufferedFileChannel} from a complete file and uses the
   * given buffer for caching the data. This constructor can be used for
   * creating many {@link BufferedFileChannel} instances (consecutively) with
   * the same direct byte buffer to avoid the allocation- and
   * garbage-collection-overhead for multiple direct byte buffers.
   * @param file the file to read from
   * @param buffer the {@link ByteBuffer} to use for caching the data
   * @throws IOException if any error occurs while creating the
   *           {@link BufferedFileChannel}
   */
  public BufferedFileChannel(final File file, final ByteBuffer buffer)
      throws IOException {
    f = file;
    name = file.getAbsolutePath();
    fc = new RandomAccessFile(file, "r").getChannel();
    mark = 0;
    buffer.clear();
    buffer.position(buffer.limit());
    rem = fc.size();
    buf = buffer;
  }

  /**
   * Creates a {@link BufferedFileChannel}.
   * @param file the file to read from
   * @param subfilename the name of the subfile
   * @param fileChannel the underlying {@link FileChannel} instance
   * @param buffer the {@link ByteBuffer} to use
   * @param bytesToRead the maximum number of bytes to read from the FileChannel
   *          (including the already buffered bytes)
   * @throws IOException if any error occurs while creating the
   *           {@link BufferedFileChannel}
   */
  private BufferedFileChannel(final File file, final String subfilename,
      final FileChannel fileChannel, final ByteBuffer buffer,
      final long bytesToRead) throws IOException {
    f = file;
    name = subfilename.equals("") ? f.getAbsolutePath() : f.getAbsolutePath()
        + Prop.SEP + subfilename;
    fc = fileChannel;
    buf = buffer;
    mark = absolutePosition();
    rem = bytesToRead - buf.remaining();
  }

  /**
   * <p>
   * Creates a "subChannel" view for this {@link BufferedFileChannel} instance
   * that shares the underlying {@link FileChannel} and buffer.
   * </p>
   * <p>
   * The content of the new channel will start at this channel's current
   * position and it's size depends on the given parameter
   * <code>bytesToRead</code>.
   * </p>
   * <p>
   * <b> The original channel must not be used before {@link #finish()} has been
   * called on the {@link BufferedFileChannel} created by this method.</b> After
   * finishing the subChannel, the original channel's position is incremented by
   * <code>bytesToRead</code>.
   *</p>
   * @param subfilename the name of the subfile
   * @param bytesToRead the maximum number of bytes to read
   * @return the new "subChannel"
   * @throws IOException if any error occurs while creating the channel
   */
  public BufferedFileChannel subChannel(final String subfilename,
      final int bytesToRead)
      throws IOException {
    if(remaining() < bytesToRead) throw new EOFException(
        "Requested number of bytes to read is too large.");
    rem -= bytesToRead;
    rem += buf.remaining();
    return new BufferedFileChannel(f, subfilename, fc, buf, bytesToRead);
  }

  /**
   * Returns the size of the buffer.
   * @return the size of the buffer
   */
  public int getBufferSize() {
    return buf.capacity();
  }

  /**
   * Skips <code>n</code> bytes in the ByteBuffer.
   * @param n number of bytes to skip. May be negative
   * @throws IOException if any error occurs while reading the file
   */
  public void skip(final long n) throws IOException {
    if(n == 0) return;
    final int buffered = buf.remaining();
    if(n > 0) {
      if(rem + buffered < n) throw new EOFException(f.getAbsolutePath());
      if(buffered < n) {
        final long skip = n - buffered;
        fc.position(fc.position() + skip);
        buf.position(buf.limit());
        rem -= skip;
      } else {
        assert n < Integer.MAX_VALUE;
        buf.position(buf.position() + (int) n);
      }
    } else { // n < 0
      if(n < -position()) throw new IllegalArgumentException(
          "Negative channel " + "position");
      final int bPos = buf.position();
      if(bPos < -n) {
        final int bufLim = buf.limit();
        final long skip = n - bufLim;
        fc.position(fc.position() + skip);
        buf.position(bufLim);
        rem -= skip + buffered;
      } else buf.position(buf.position() + (int) n);
    }
  }

  /**
   * Returns this channel's position.
   * @return This channel's position, a non-negative integer counting the number
   *         of bytes from the beginning of the channel to the current position
   * @throws IOException if any error occurs while reading from the channel
   */
  public long position() throws IOException {
    return absolutePosition() - mark;
  }

  /**
   * Returns this channel's absolute position.
   * @return this channel's absolute position
   * @throws IOException if any error occurs while reading from the channel
   */
  public long absolutePosition() throws IOException {
    return fc.position() - buf.remaining();
  }

  /**
   * Sets this channel's position.
   * @param newPosition the new position, a non-negative integer counting the
   *          number of bytes from the beginning of the channel
   * @throws IOException if any error occurs while reading from the channel
   */
  public void position(final long newPosition) throws IOException {
    skip(newPosition - position());
  }

  /**
   * Returns the number of remaining bytes in this {@link BufferedFileChannel}.
   * @return the number of remaining bytes
   */
  private long remaining() {
    assert rem + buf.remaining() >= 0;
    return rem + buf.remaining();
  }

  /**
   * Returns the size of the channel.
   * @return the size of the channel
   * @throws IOException if any error occurs while calculating the size
   */
  public long size() throws IOException {
    return fc.position() + rem - mark;
  }

  /**
   * Checks if the current {@link BufferedFileChannel} instance is a
   * sub-channel.
   * @return true if the current instance is a sub-channel
   * @throws IOException if any i/o error occurs
   */
  public boolean isSubChannel() throws IOException {
    return mark != 0 || fc.size() > fc.position() + rem;
  }

  /**
   * Resets the channel to its initial position.
   * @throws IOException if any error occurs while reading from the channel
   */
  public void reset() throws IOException {
    final int bPos = buf.position();
    final long offset = absolutePosition() - mark;
    if(offset == 0) return;
    assert offset > 0;
    if(bPos < offset || bPos == buf.limit()) {
      fc.position(mark);
      buf.position(buf.limit());
    } else {
      buf.position(bPos - (int) offset);
    }
    rem += offset;
  }

  /**
   * Reads <code>dst.length</code> bytes from the {@link BufferedFileChannel}.
   * @param dst the arrray to write the data to
   * @return the filled byte array
   * @throws IOException if there are less than <code>dst.length</code> bytes
   *           available or any error occurs while reading from the channel
   */
  public byte[] get(final byte[] dst) throws IOException {
    int bytesToRead = dst.length;
    if(buffer(bytesToRead)) {
      buf.get(dst, 0, bytesToRead);
    } else { // buf is too small
      final int buffered = buf.remaining();
      bytesToRead -= buffered;
      if(rem < bytesToRead) throw new EOFException(f.getAbsolutePath());
      buf.get(dst, 0, buffered); // copy buffered bytes
      buf.clear(); // clear buffer
      // read remaining bytes directly into target buffer and fill the buffer
      final ByteBuffer tmp = ByteBuffer.wrap(dst, buffered, bytesToRead);
      rem -= fc.read(new ByteBuffer[] { tmp, buf});
      buf.flip();
    }
    return dst;
  }

  /**
   * <p>
   * Reads <code>length</code> bytes from the {@link BufferedFileChannel}.
   * </p>
   * <p>
   * <b> Assure that that the buffer is large enough and that enough bytes are
   * buffered (via {@link #buffer(int)}). Otherwise, a
   * {@link BufferUnderflowException} may be thrown.</b>
   * </p>
   * @param dst the arrray to write the data to
   * @param start the first position to write the data to
   * @param length the number of bytes to read
   */
  public void get(final byte[] dst, final int start, final int length) {
    buf.get(dst, start, length);
  }

  /**
   * <p>
   * Relative <i>get</i> method. Reads the byte at this channel's current
   * position, and then increments the position.
   * </p>
   * <p>
   * <b> Assure that enough at least one byte is buffered via
   * {@link #buffer(int) #buffer(1)}. Otherwise, a
   * {@link BufferUnderflowException} may be thrown.</b>
   * </p>
   * @return The byte at the channel's current position
   */
  public int get() {
    return buf.get() & 0xFF;
  }

  /**
   * <p>
   * Relative <i>get</i> method. Reads two bytes at this channel's current
   * position, and then increments the position by two.
   * </p>
   * <p>
   * <b> Assure that enough at least two bytes are buffered via
   * {@link #buffer(int) #buffer(2)}. Otherwise, a
   * {@link BufferUnderflowException} may be thrown.</b>
   * </p>
   * @return The next two bytes at the channel's current position as integer
   */
  public int getShort() {
    return buf.getShort() & 0xFFFF;
  }

  /**
   * <p>
   * Relative <i>get</i> method. Reads four bytes at this channel's current
   * position, and then increments the position by four.
   * </p>
   * <p>
   * <b> Assure that enough at least four bytes are buffered via
   * {@link #buffer(int) #buffer(4)}. Otherwise, a
   * {@link BufferUnderflowException} may be thrown.</b>
   * </p>
   * @return The next four bytes at the channel's current position as integer
   */
  public int getInt() {
    return buf.getInt();
  }

  /**
   * <p>
   * Finishes reading from a channel created by {@link #subChannel(String, int)}
   * . Any subsequent read from this BufferedFileChannel will fail.
   * </p>
   * <p>
   * <b>This method must be called for every {@link BufferedFileChannel} that
   * was created by {@link #subChannel(String, int)} instead of calling
   * {@link #close()} .</b>
   * </p>
   * @throws IOException if any error occurs while finishing the channel
   * @see #close()
   */
  public void finish() throws IOException {
    final long len = remaining();
    skip(len);
  }

  /**
   * <p>
   * Closes the underlying {@link FileChannel}.
   * </p>
   * <p>
   * <b>This method must not be called for {@link BufferedFileChannel} instances
   * that were created by {@link #subChannel(String, int)}.</b>
   * </p>
   * @throws IOException if any error occurs while closing the
   *           {@link FileChannel}
   * @see #finish()
   */
  public void close() throws IOException {
    if(isSubChannel()) {
      throw new IOException("Can't close SubChannel, only the "
          + "main BufferedFileChannel can be closed.");
    }
    fc.close();
  }

  /**
   * <p>
   * Buffers <code>n</code> bytes, if the underlying buffer is large enough.
   * Does nothing, if the buffer is too small.
   * </p>
   * @param n the number of bytes to buffer
   * @return <b>true</b> if the buffer is large enough to contain all
   *         <code>n</code> bytes, <b>false</b> if it is too small
   * @throws IOException if any error occurs while reading the file
   */
  public boolean buffer(final int n) throws IOException {
    final int buffered = buf.remaining();
    if(rem < n - buffered) throw new EOFException(f.getAbsolutePath());
    if(buffered < n) {
      if(n > buf.capacity()) return false;
      buf.compact();
      final int readBytes = fc.read(buf);
      if(readBytes + buffered < n || readBytes == -1) throw new EOFException(
          f.getAbsolutePath());
      rem -= readBytes; // rem may be negative
      buf.flip();
    }
    return true;
  }

  /**
   * Returns the absolute file name of current file.
   * @return the file name
   */
  public String getFileName() {
    return name;
  }

  /**
   * Returns the associated file.
   * @return the file
   */
  File getAssociatedFile() {
    return f;
  }

  /**
   * Returns the offset of this (sub)channel regarding to the regular file in
   * the file system.
   * @return the offset of the current subchannel inside the regular file or
   *         zero if this file channel is not a subchannel
   */
  public long getOffset() {
    return mark;
  }

  /**
   * Modifies this channel's byte order. The byte order is only relevant for the
   * {@link #getShort()} and {@link #getInt()} methods.
   * @param order The new byte order, either {@link ByteOrder#BIG_ENDIAN
   *          BIG_ENDIAN} or {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}
   */
  public void setByteOrder(final ByteOrder order) {
    buf.order(order);
  }

  /**
   * Returns this FileChannel's byte order.
   * @return the {@link ByteOrder}
   */
  public ByteOrder getByteOrder() {
    return buf.order();
  }

  /**
   * Reads a line of text. A line is considered to be terminated by any one of a
   * line feed ('\n'), a carriage return ('\r'), or a carriage return followed
   * immediately by a linefeed.
   * @param inputEncoding the input encoding
   * @return A (UTF-8-)String containing the contents of the line, not including
   *         any line-termination characters, or null if the end of the stream
   *         has been reached
   * @throws IOException if an I/O error occurs
   */
  public String readLine(final String inputEncoding) throws IOException {
    final boolean utf = inputEncoding.equalsIgnoreCase(Token.UTF8);
    final TokenBuilder tb = new TokenBuilder(100);
    out: while(true) {
      final int max = buf.remaining();
      for(int i = 0; i < max; i++) {
        final int b = get();
        if(b == '\n') {
          break out;
        } else if(b == '\r') {
          if(remaining() > 0 && buffer(1) && get() != '\n') skip(-1);
          break out;
        } else {
          if(utf) tb.add((byte) b);
          else tb.addUTF(b);
        }
      }
      final long r = remaining();
      if(r == 0) return null;
      final int bs = getBufferSize();
      buffer(r < bs ? (int) r : bs);
    }
    return tb.toString();
  }

  @Override
  public String toString() {
    return getFileName();
  }
}
