package org.basex.io.in;

import static org.basex.util.Token.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

import org.basex.io.IO;
import org.basex.util.list.ByteList;

/**
 * This class uses a byte buffer to speed up input stream processing.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class BufferInput extends InputStream {
  /** UTF8 cache. */
  private final byte[] cache = new byte[4];
  /** Byte buffer. */
  protected final byte[] buffer;
  /** Current buffer position. */
  protected int pos;
  /** Current buffer size (set to {@code -1} if buffer is still empty). */
  protected int size = -1;

  /** Reference to the data input stream. */
  private InputStream in;
  /** Default encoding for text files. */
  private String enc = UTF8;
  /** Charset decoder. */
  private CharsetDecoder csd;
  /** Total input length. */
  private long length;
  /** Number of read bytes. */
  private int read;
  /** Marker. */
  private int mark;

  /**
   * Initializes the file reader.
   * @param file the file to be read
   * @throws IOException I/O Exception
   */
  public BufferInput(final File file) throws IOException {
    this(new FileInputStream(file));
    length = file.length();
  }

  /**
   * Initializes the file reader.
   * @param is input stream
   */
  public BufferInput(final InputStream is) {
    this(is, IO.BLOCKSIZE);
  }

  /**
   * Initializes the file reader.
   * @param is input stream
   * @param bs buffer size
   */
  public BufferInput(final InputStream is, final int bs) {
    buffer = new byte[bs];
    in = is;
  }

  /**
   * Empty constructor.
   * @param buf buffer
   */
  protected BufferInput(final byte[] buf) {
    buffer = buf;
    size = buf.length;
    length = size;
  }

  /**
   * Determines the file encoding.
   * @return guessed encoding
   * @throws IOException I/O exception
   */
  public final String encoding() throws IOException {
    // cache first bytes
    if(size == -1) size = in.read(buffer);
    final byte a = size > 0 ? buffer[0] : 0;
    final byte b = size > 1 ? buffer[1] : 0;
    final byte c = size > 2 ? buffer[2] : 0;
    final byte d = size > 3 ? buffer[3] : 0;
    if(a == -1 && b == -2 || a == '<' && b == 0 && c == '?' && d == 0) {
      // BOM: ff fe
      enc = UTF16LE;
      if(a == -1) pos = 2;
    } else if(a == -2 && b == -1 || a == 0 && b == '<' && c == 0 && d == '?') {
      // BOM: fe ff
      enc = UTF16BE;
      if(a == -2) pos = 2;
    } else if(a == -0x11 && b == -0x45 && c == -0x41) {
      // BOM: ef bb bf
      pos = 3;
    }
    return enc;
  }

  /**
   * Sets a new encoding.
   * @param encoding encoding
   * @throws IOException I/O Exception
   */
  public final void encoding(final String encoding) throws IOException {
    try {
      enc = normEncoding(encoding, enc);
      csd = Charset.forName(encoding).newDecoder();
    } catch(final Exception ex) {
      throw new IOException(ex.toString());
    }
  }

  /**
   * Returns the next byte (see {@link InputStream#read}.
   * {@code 0} is returned if all bytes have been read.
   * @return next byte
   * @throws IOException I/O exception
   */
  @Override
  public int read() throws IOException {
    if(pos >= size) {
      if(size != -1) read += size;
      size = in.read(buffer);
      if(size <= 0) return -1;
      pos = 0;
    }
    return buffer[pos++] & 0xFF;
  }

  /**
   * Reads a string from the input stream, suffixed by a {@code 0} byte.
   * @return string
   * @throws IOException I/O Exception
   */
  public final String readString() throws IOException {
    return bytes().toString();
  }

  /**
   * Reads a bytes array from the input stream, suffixed by a {@code 0} byte.
   * @return token
   * @throws IOException I/O Exception
   */
  public final byte[] readBytes() throws IOException {
    return bytes().toArray();
  }

  /**
   * Returns the next character, or {@code 0} if all bytes have been read.
   * Erroneous characters are ignored.
   * @return next character
   * @throws IOException I/O exception
   */
  public final int readChar() throws IOException {
    // handle different encodings
    final int ch = read();
    if(ch == -1) return ch;

    // encoding can be safely compared by references...
    if(enc == UTF16LE) return ch | read() << 8;
    if(enc == UTF16BE) return ch << 8 | read();
    if(ch < 0x80) return ch;
    if(enc == UTF8) {
      final int cl = cl((byte) ch);
      cache[0] = (byte) ch;
      for(int c = 1; c < cl; ++c) cache[c] = (byte) read();
      return cp(cache, 0);
    }

    // convert other encodings.. loop until all needed bytes have been read
    int p = 0;
    while(true) {
      if(p == 4) return -cache[0];
      cache[p++] = (byte) ch;
      try {
        final CharBuffer cb = csd.decode(
            ByteBuffer.wrap(Arrays.copyOf(cache, p)));
        int i = 0;
        for(int c = 0; c < cb.limit(); ++c) i |= cb.get(c) << (c << 3);
        return i;
      } catch(final CharacterCodingException ex) {
        // tolerate erroneous characters
        return ch;
      }
    }
  }

  @Override
  public final void close() throws IOException {
    if(in != null && !(in instanceof ZipInputStream)) in.close();
  }

  /**
   * Returns the number of read bytes.
   * @return read bytes
   */
  public final int size() {
    return read + pos;
  }

  /**
   * Returns the input length.
   * @return input length
   */
  public final long length() {
    return length;
  }

  /**
   * Sets the input length.
   * @param l input length
   */
  public final void length(final long l) {
    length = l;
  }

  @Override
  public final boolean markSupported() {
    return read < buffer.length;
  }

  @Override
  public synchronized void mark(final int m) {
    mark = pos;
  }

  @Override
  public final synchronized void reset() {
    read = mark;
    pos = mark;
  }

  /**
   * Reads bytes from the input stream, suffixed by a {@code 0} byte.
   * @return byte list
   * @throws IOException I/O Exception
   */
  private ByteList bytes() throws IOException {
    final ByteList bl = new ByteList();
    for(int l; (l = read()) > 0;) bl.add(l);
    return bl;
  }
}
