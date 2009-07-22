package org.basex.build.fs.parser;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import org.basex.io.IO;

/**
 * <p>
 * Buffered {@link FileChannel} implementation.
 * </p>
 * <p>
 * This implementation is optimized for sequential reading of a file or a
 * fragment of a file.
 * </p>
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class BufferedFileChannel {
  /** The file we are reading from. */
  private final File f;
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
   * @param file the file to read from.
   * @throws IOException if any error occurs while creating the
   *           {@link BufferedFileChannel}.
   */
  public BufferedFileChannel(final File file) throws IOException {
    this(file, IO.BLOCKSIZE);
  }

  /**
   * Creates a {@link BufferedFileChannel} from a complete file with the given
   * buffer size.
   * @param file the file to read from.
   * @param bufferSize the size of the buffer.
   * @throws IOException if any error occurs while creating the
   *           {@link BufferedFileChannel}.
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
   * @param file the file to read from.
   * @param buffer the {@link ByteBuffer} to use for caching the data.
   * @throws IOException if any error occurs while creating the
   *           {@link BufferedFileChannel}.
   */
  public BufferedFileChannel(final File file, final ByteBuffer buffer)
      throws IOException {
    f = file;
    fc = new RandomAccessFile(file, "r").getChannel();
    mark = 0;
    buffer.clear();
    buffer.position(buffer.limit());
    rem = fc.size();
    buf = buffer;
  }

  /**
   * Creates a {@link BufferedFileChannel}.
   * @param file the file to read from.
   * @param fileChannel the underlying {@link FileChannel} instance.
   * @param buffer the {@link ByteBuffer} to use.
   * @param remaining the maximum number of bytes to read from the FileChannel
   *          (excluding the already buffered bytes).
   * @throws IOException if any error occurs while creating the
   *           {@link BufferedFileChannel}.
   */
  private BufferedFileChannel(final File file, final FileChannel fileChannel,
      final ByteBuffer buffer, final long remaining) throws IOException {
    f = file;
    fc = fileChannel;
    buf = buffer;
    mark = absolutePosition();
    rem = remaining;
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
   * @param bytesToRead the maximum number of bytes to read.
   * @return the new "subChannel".
   * @throws IOException if any error occurs while creating the channel.
   */
  public BufferedFileChannel subChannel(final int bytesToRead)
      throws IOException {
    final long remaining = bytesToRead - buf.remaining();
    if(remaining > rem) throw new IllegalArgumentException(
        "Requested number of bytes to read is too large.");
    rem -= remaining;
    return new BufferedFileChannel(f, fc, buf, remaining);
  }

  /**
   * Skips <code>n</code> bytes in the ByteBuffer.
   * @param n number of bytes to skip. May be negative.
   * @throws IOException if any error occurs while reading the file.
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
      } else {
        buf.position(buf.position() + (int) n);
        rem -= n;
      }
    }
  }

  /**
   * Returns this channel's position.
   * @return This channel's position, a non-negative integer counting the number
   *         of bytes from the beginning of the channel to the current position
   * @throws IOException if any error occurs while reading from the channel.
   */
  public long position() throws IOException {
    return absolutePosition() - mark;
  }

  /**
   * Returns this channel's absolute position.
   * @return this channel's absolute position.
   * @throws IOException if any error occurs while reading from the channel.
   */
  public long absolutePosition() throws IOException {
    return fc.position() - buf.remaining();
  }

  /**
   * Sets this channel's position.
   * @param newPosition the new position, a non-negative integer counting the
   *          number of bytes from the beginning of the channel.
   * @throws IOException if any error occurs while reading from the channel.
   */
  public void position(final long newPosition) throws IOException {
    skip(newPosition - position());
  }

  /**
   * Returns the number of remaining bytes in this {@link BufferedFileChannel}.
   * @return the number of remaining bytes.
   */
  public long remaining() {
    assert rem + buf.remaining() >= 0;
    return rem + buf.remaining();
  }

  /**
   * Returns the size of the channel.
   * @return the size of the channel.
   * @throws IOException if any error occurs while calculating the size.
   */
  public long size() throws IOException {
    return fc.position() + rem - mark;
  }

  /**
   * Checks if the current {@link BufferedFileChannel} instance is a
   * sub-channel.
   * @return true if the current instance is a sub-channel.
   * @throws IOException if any i/o error occurs.
   */
  public boolean isSubChannel() throws IOException {
    return mark != 0 || fc.size() > rem + buf.remaining();
  }

  /**
   * Resets the channel to its initial position.
   * @throws IOException if any error occurs while reading from the channel.
   */
  public void reset() throws IOException {
    final int bPos = buf.position();
    final long offset = absolutePosition() - mark;
    if(offset == 0) return;
    assert offset > 0;
    if(bPos < offset) {
      fc.position(mark);
      buf.position(buf.limit());
    } else {
      buf.position(bPos - (int) offset);
    }
    rem += offset;
  }

  /**
   * Reads <code>dst.length</code> bytes from the {@link BufferedFileChannel}.
   * @param dst the arrray to write the data to.
   * @return the filled byte array.
   * @throws IOException if there are less than <code>dst.length</code> bytes
   *           available or any error occurs while reading from the channel.
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
   * @param dst the arrray to write the data to.
   * @param start the first position to write the data to.
   * @param length the number of bytes to read.
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
   * @return The next two bytes at the channel's current position as integer.
   */
  public int getShort() {
    return (buf.get() & 0xFF) << 8 | buf.get() & 0xFF;
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
   * @return The next four bytes at the channel's current position as integer.
   */
  public int getInt() {
    return (buf.get() & 0xFF) << 24 //
        | (buf.get() & 0xFF) << 16 //
        | (buf.get() & 0xFF) << 8 //
        | buf.get() & 0xFF;
  }

  /**
   * <p>
   * Finishes reading from a channel created by {@link #subChannel(int)}. Any
   * subsequent read from this BufferedFileChannel will fail.
   * </p>
   * <p>
   * <b>This method must be called for every {@link BufferedFileChannel} that
   * was created by {@link #subChannel(int)} instead of calling {@link #close()}
   * .</b>
   * </p>
   * @throws IOException if any error occurs while finishing the channel.
   * @see #close()
   */
  public void finish() throws IOException {
    final long len = buf.remaining() + rem;
    skip(len);
  }

  /**
   * <p>
   * Closes the underlying {@link FileChannel}.
   * </p>
   * <p>
   * <b>This method must not be called for {@link BufferedFileChannel} instances
   * that were created by {@link #subChannel(int)}.</b>
   * </p>
   * @throws IOException if any error occurs while closing the
   *           {@link FileChannel}.
   * @see #finish()
   */
  public void close() throws IOException {
    fc.close();
  }

  /**
   * <p>
   * Buffers <code>n</code> bytes, if the underlying buffer is large enough.
   * Does nothing, if the buffer is too small.
   * </p>
   * @param n the number of bytes to buffer.
   * @return <b>true</b> if the buffer is large enough to contain all
   *         <code>n</code> bytes, <b>false</b> if it is too small.
   * @throws IOException if any error occurs while reading the file.
   */
  public boolean buffer(final int n) throws IOException {
    final int buffered = buf.remaining();
    if(rem < n - buffered) throw new EOFException(f.getAbsolutePath());
    if(buffered < n) {
      if(n > buf.capacity()) return false;
      buf.compact();
      rem -= fc.read(buf); // rem may be negative
      buf.flip();
    }
    return true;
  }

  /**
   * Returns the absolute file name of current file.
   * @return the file name.
   */
  public String getFileName() {
    return f.getAbsolutePath();
  }

  /**
   * Modifies this channel's byte order.
   * @param order The new byte order, either {@link ByteOrder#BIG_ENDIAN
   *          BIG_ENDIAN} or {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}
   *
   */
  public void setByteOrder(final ByteOrder order) {
    buf.order(order);
  }
}
