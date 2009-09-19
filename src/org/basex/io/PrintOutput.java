package org.basex.io;

import static org.basex.core.Text.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.util.Token;

/**
 * This class is a stream-wrapper for textual data. Note that the internal
 * byte representation (usually UTF8) will be directly output without
 * further character conversion.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class PrintOutput extends OutputStream {
  /** The OutputStream we operate on. */
  protected OutputStream os;
  /** Maximum numbers of bytes to write. */
  protected int max = Integer.MAX_VALUE;
  /** Number of bytes written. */
  protected int size;

  /** Protected default constructor. */
  protected PrintOutput() { }

  /**
   * Constructor, given a filename.
   * @param fn filename
   * @throws IOException I/O exception
   */
  public PrintOutput(final String fn) throws IOException {
    os = new BufferedOutput(new FileOutputStream(fn));
  }

  /**
   * Constructor, given an output stream.
   * @param out output stream reference
   */
  public PrintOutput(final OutputStream out) {
    os = out;
  }

  /**
   * Constructor, given an output stream.
   * @param out output stream reference
   * @param m maximum number of bytes to write
   */
  public PrintOutput(final OutputStream out, final int m) {
    os = out;
    max = m;
  }

  @Override
  public void write(final int b) throws IOException {
    if(size++ < max) os.write(b);
  }

  /**
   * Writes a character to the output stream.
   * @param ch string to be written
   * @throws IOException I/O exception
   */
  public final void print(final char ch) throws IOException {
    write((byte) ch);
  }

  /**
   * Writes a string to the output stream.
   * @param str string to be written
   * @param i number of spaces to indent
   * @throws IOException I/O exception
   */
  public final void print(final int i, final byte[] str) throws IOException {
    for(int a = 0; a < i - str.length; a++) print(' ');
    print(str);
  }

  /**
   * Writes a string to the output stream.
   * @param str string to be written
   * @param i number of spaces to indent
   * @throws IOException I/O exception
   */
  public final void print(final byte[] str, final int i) throws IOException {
    print(str);
    for(int a = 0; a < i - str.length; a++) print(' ');
  }

  /**
   * Writes a string to the output stream.
   * @param str string to be written
   * @throws IOException I/O exception
   */
  public final void print(final String str) throws IOException {
    print(Token.token(str));
  }

  /**
   * Writes a string and newline to the output stream.
   * @param str string to be written
   * @throws IOException I/O exception
   */
  public final void println(final String str) throws IOException {
    print(str);
    print(NL);
  }

  /**
   * Writes a newline to the output stream.
   * @throws IOException I/O exception
   */
  public final void println() throws IOException {
    print(NL);
  }

  /**
   * Writes a token to the output stream.
   * @param token token to be written
   * @throws IOException I/O exception
   */
  public final void print(final byte[] token) throws IOException {
    for(final byte t : token) write(t);
  }

  /**
   * Writes a token to the output stream.
   * @param token token to be written
   * @throws IOException I/O exception
   */
  public final void println(final byte[] token) throws IOException {
    print(token);
    print(NL);
  }

  /**
   * Returns the number of written bytes.
   * @return number of written bytes.
   */
  public final int size() {
    return size;
  }

  @Override
  public void flush() throws IOException {
    if(os != null) os.flush();
  }

  @Override
  public void close() throws IOException {
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
