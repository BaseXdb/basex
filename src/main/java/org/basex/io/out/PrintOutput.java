package org.basex.io.out;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.Prop;
import org.basex.util.Token;

/**
 * This class is a stream-wrapper for textual data. Note that the internal
 * byte representation (usually UTF8) will be directly output without
 * further character conversion.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class PrintOutput extends OutputStream {
  /** Output stream reference. */
  private final OutputStream os;
  /** Maximum numbers of bytes to write. */
  protected int max = Integer.MAX_VALUE;
  /** Number of bytes written. */
  protected int size;

  /** Protected default constructor. */
  protected PrintOutput() {
    this((OutputStream) null);
  }

  /**
   * Constructor, given a filename.
   * @param fn filename
   * @throws IOException I/O exception
   */
  public PrintOutput(final String fn) throws IOException {
    this(new BufferOutput(new FileOutputStream(fn)));
  }

  /**
   * Constructor, given an output stream.
   * @param out output stream reference
   */
  private PrintOutput(final OutputStream out) {
    os = out;
  }

  /**
   * Returns a new instance for the given output stream.
   * @param out output stream reference
   * @return print output
   */
  public static PrintOutput get(final OutputStream out) {
    return out instanceof PrintOutput ? (PrintOutput) out :
      new PrintOutput(
        out instanceof ByteArrayOutputStream ||
        out instanceof BufferedOutputStream ||
        out instanceof BufferOutput ||
        out instanceof ArrayOutput ? out : new BufferOutput(out));
  }

  @Override
  public void write(final int b) throws IOException {
    if(size++ < max && os != null) os.write(b);
  }

  /**
   * Writes a character as UTF8.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  public void utf8(final int ch) throws IOException {
    if(ch <= 0x7F) {
      write(ch);
    } else if(ch <= 0x7FF) {
      write(ch >>  6 & 0x1F | 0xC0);
      write(ch >>  0 & 0x3F | 0x80);
    } else if(ch <= 0xFFFF) {
      write(ch >> 12 & 0x0F | 0xE0);
      write(ch >>  6 & 0x3F | 0x80);
      write(ch >>  0 & 0x3F | 0x80);
    } else {
      write(ch >> 18 & 0x07 | 0xF0);
      write(ch >> 12 & 0x3F | 0x80);
      write(ch >>  6 & 0x3F | 0x80);
      write(ch >>  0 & 0x3F | 0x80);
    }
  }

  /**
   * Writes a string to the output stream, suffixed by a 0 byte.
   * @param str string to be written
   * @throws IOException I/O exception
   */
  public final void writeString(final String str) throws IOException {
    print(Token.token(str));
    write(0);
  }

  /**
   * Prints a string to the output stream.
   * @param str string to be written
   * @throws IOException I/O exception
   */
  public final void print(final String str) throws IOException {
    print(Token.token(str));
  }

  /**
   * Prints a string and newline to the output stream.
   * @param str string to be written
   * @throws IOException I/O exception
   */
  public final void println(final String str) throws IOException {
    print(str);
    print(Prop.NL);
  }

  /**
   * Prints a token to the output stream.
   * @param token token to be written
   * @throws IOException I/O exception
   */
  public final void print(final byte[] token) throws IOException {
    for(final byte t : token) write(t);
  }

  /**
   * Prints a token to the output stream.
   * @param token token to be written
   * @throws IOException I/O exception
   */
  public final void println(final byte[] token) throws IOException {
    print(token);
    print(Prop.NL);
  }

  /**
   * Returns the number of written bytes.
   * @return number of written bytes
   */
  public final int size() {
    return size;
  }

  @Override
  public final void flush() throws IOException {
    if(os != null) os.flush();
  }

  @Override
  public final void close() throws IOException {
    if(os != null) {
      if(os == System.out || os == System.err) os.flush();
      else os.close();
    }
  }

  /**
   * Checks if stream can output more characters; can be overwritten to
   * interrupt streaming at some point.
   * @return result of check
   */
  public boolean finished() {
    return false;
  }
}
