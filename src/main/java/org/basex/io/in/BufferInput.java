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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BufferInput extends InputStream {
  /** UTF8 cache. */
  private final byte[] cache = new byte[4];

  /** Byte buffer. */
  protected final byte[] buffer;
  /** Current buffer position. */
  protected int bpos;
  /** Current buffer size. */
  protected int bsize;

  /** Reference to the data input stream. */
  private InputStream in;
  /** Default encoding for text files. */
  private String enc = UTF8;
  /** Charset decoder. */
  private CharsetDecoder csd;

  /** Total length of input to be processed (may be {@code 0}). */
  private long length;
  /** Buffer marker to jump back (not available when set to {@code -1}. */
  private int bmark;
  /** Number of read bytes. */
  private int read;

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
    bsize = buf.length;
    length = bsize;
  }

  /**
   * Guesses the file encoding, based on the first characters.
   * @return encoding
   * @throws IOException I/O exception
   */
  public final String encoding() throws IOException {
    final int a = read();
    final int b = read();
    final int c = read();
    final int d = read();
    int skip = 0;
    if(a == 0xFF && b == 0xFE) { // BOM: FF FE
      enc = UTF16LE;
      skip = 2;
    } else if(a == 0xFE && b == 0xFF) { // BOM: FE FF
      enc = UTF16BE;
      skip = 2;
    } else if(a == 0xEF && b == 0xBB && c == 0xBF) { // BOM: EF BB BF
      skip = 3;
    } else if(a == '<' && b == 0 && c == '?' && d == 0) {
      enc = UTF16LE;
    } else if(a == 0 && b == '<' && c == 0 && d == '?') {
      enc = UTF16BE;
    }
    reset();
    for(int s = 0; s < skip; s++) read();
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
   * {@code -1} is returned if all bytes have been read.
   * @return next byte
   * @throws IOException I/O exception
   */
  @Override
  public int read() throws IOException {
    final int blen = buffer.length;
    final byte[] buf = buffer;
    if(bpos >= bsize) {
      if(bsize == 0 || bsize == blen) {
        // reset mark if buffer is full
        if(bsize == blen) bmark = -1;
        // buffer is empty or full: re-fill it
        bsize = 0;
        bpos = 0;
      }
      int r;
      while((r = in.read(buf, bsize, blen - bsize)) == 0);
      if(r < 0) return -1;
      bsize += r;
      read += r;
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
    for(int l; (l = read()) > 0;) bl.add(l);
    return bl.toArray();
  }

  /**
   * Returns the next character (code point), or {@code -1} if end of stream
   * is reached. Erroneous characters are ignored.
   * @return next character
   * @throws IOException I/O exception
   */
  public final int readChar() throws IOException {
    final int ch = read();
    if(ch == -1) return ch;

    // handle different encodings (comparing by references is safe here)
    final String e = enc;
    if(e == UTF16LE) return ch | read() << 8;
    if(e == UTF16BE) return ch << 8 | read();
    if(ch < 0x80) return ch;
    if(e == UTF8) {
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
    return read + bpos;
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
    return true;
  }

  @Override
  public synchronized void mark(final int m) {
    bmark = bpos;
  }

  @Override
  public final synchronized void reset() throws IOException {
    if(bmark == -1) throw new IOException("Mark cannot be reset.");
    bpos = bmark;
  }
}
