package org.basex.io;

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
import org.basex.util.ByteList;
import org.basex.util.TokenBuilder;

/**
 * This class serves as a buffered wrapper for input streams.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class BufferInput extends InputStream {
  /** UTF8 cache. */
  private final byte[] cache = new byte[4];
  /** Byte buffer. */
  protected byte[] buffer;
  /** Current buffer position. */
  protected int pos;
  /** Input length. */
  protected long length;
  /** Current buffer size. */
  private int size;
  /** Number of read bytes. */
  private int len;
  /** Reference to the data input stream. */
  private InputStream in;
  /** Default encoding for text files. */
  private String enc = UTF8;
  /** Charset decoder. */
  private CharsetDecoder csd;

  /**
   * Initializes the file reader.
   * @param file the file to be read
   * @throws IOException IO Exception
   */
  public BufferInput(final String file) throws IOException {
    this(new File(file));
  }

  /**
   * Initializes the file reader.
   * @param file the file to be read
   * @throws IOException IO Exception
   */
  public BufferInput(final File file) throws IOException {
    this(new FileInputStream(file));
    length = file.length();
  }

  /**
   * Initializes the file reader.
   * @param is input stream
   * @throws IOException IO Exception
   */
  public BufferInput(final InputStream is) throws IOException {
    this(new byte[IO.BLOCKSIZE]);
    in = is;
    next();
  }

  /**
   * Empty constructor.
   * @param buf buffer
   */
  protected BufferInput(final byte[] buf) {
    buffer = buf;
    length = buf.length;
  }

  /**
   * Determines the file encoding.
   * @return guessed encoding
   */
  public final String encoding() {
    final byte a = length > 0 ? buffer[0] : 0;
    final byte b = length > 1 ? buffer[1] : 0;
    final byte c = length > 2 ? buffer[2] : 0;
    final byte d = length > 3 ? buffer[3] : 0;
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
   * @param e encoding
   * @throws IOException IO Exception
   */
  public final void encoding(final String e) throws IOException {
    try {
      enc = code(e, enc);
      csd = Charset.forName(e).newDecoder();
    } catch(final Exception ex) {
      throw new IOException(ex.toString());
    }
  }

  @Override
  public final int read() throws IOException {
    return readByte() & 0xFF;
  }

  /**
   * Returns the next byte, or 0 if all bytes have been read.
   * @return next byte
   * @throws IOException I/O exception
   */
  public byte readByte() throws IOException {
    if(pos >= size) {
      next();
      if(size <= 0) return 0;
    }
    return buffer[pos++];
  }

  /**
   * Reads a string from the input stream, suffixed by a 0 byte.
   * @return string
   * @throws IOException IO Exception
   */
  public final String readString() throws IOException {
    return content().toString();
  }

  /**
   * Reads a token from the input stream, suffixed by a 0 byte.
   * @return resulting byte list
   * @throws IOException IO Exception
   */
  public final ByteList content() throws IOException {
    final ByteList bl = new ByteList();
    byte l;
    while((l = readByte()) != 0) bl.add(l);
    return bl;
  }

  /**
   * Reads the next buffer entry.
   * @throws IOException I/O exception
   */
  private void next() throws IOException {
    pos = 0;
    len += size;
    size = in.read(buffer);
  }

  /**
   * Returns the next character, 0 if all bytes have been read or
   * a negative character value -1 if the read byte is invalid.
   * @return next character
   * @throws IOException I/O exception
   */
  public final int readChar() throws IOException {
    // handle different encodings
    byte ch = readByte();
    // comparison by references
    if(enc == UTF8) {
      final int cl = cl(ch);
      if(cl == 1) return ch & 0xFF;
      cache[0] = ch;
      for(int c = 1; c < cl; ++c) cache[c] = readByte();
      return cp(cache, 0);
    }
    if(enc == UTF16LE) return ch & 0xFF | (readByte() & 0xFF) << 8;
    if(enc == UTF16BE) return (ch & 0xFF) << 8 | readByte() & 0xFF;
    if(ch >= 0) return ch;

    // convert other encodings.. loop until all needed bytes have been read
    int p = 0;
    while(true) {
      if(p == 4) return -cache[0];
      cache[p++] = ch;
      try {
        final CharBuffer cb = csd.decode(
            ByteBuffer.wrap(Arrays.copyOf(cache, p)));
        int i = 0;
        for(int c = 0; c < cb.limit(); ++c) i |= cb.get(c) << (c << 3);
        return i;
      } catch(final CharacterCodingException ex) {
        ch = readByte();
      }
    }
  }

  /**
   * Reads a string.
   * @param tb token builder
   * @return true if more lines are found
   * @throws IOException IO Exception
   */
  public final boolean readLine(final TokenBuilder tb) throws IOException {
    tb.reset();
    while(true) {
      final int ch = readChar();
      if(ch == 0x0D) continue;
      if(ch == 0x00) return tb.size() != 0;
      if(ch == 0x0A) return true;
      tb.add(ch);
    }
  }

  @Override
  public final void close() throws IOException {
    if(in != null && !(in instanceof ZipInputStream)) in.close();
  }

  /**
   * Number of read bytes.
   * @return read bytes
   */
  final int size() {
    return len + pos;
  }

  /**
   * Length of input.
   * @return read bytes
   */
  final long length() {
    return length;
  }

  /**
   * Sets the input length.
   * @param l input length
   */
  final void length(final long l) {
    length = l;
  }
}
