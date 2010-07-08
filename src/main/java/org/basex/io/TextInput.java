package org.basex.io;

import java.io.IOException;
import java.util.Arrays;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class provides a convenient access to text input.
 * The encoding will be determined, analyzing the input.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class TextInput {
  /** Input stream reference. */
  private BufferInput[] in = new BufferInput[1];
  /** Input pointer. */
  private int ip;
  /** Input file. */
  private final IO file;
  /** Current line. */
  private int line = 1;

  /** Buffer with most recent characters. */
  private final int[] last = new int[16];
  /** Read position. */
  private int lp;
  /** Backward pointer. */
  private int pp;

  /**
   * Constructor.
   * @param f file reference
   * @throws IOException I/O exception
   */
  public TextInput(final IO f) throws IOException {
    in[0] = f.buffer();
    in[0].encoding();
    file = f;
  }

  /**
   * Returns the contents of the specified file.
   * @param in input path
   * @return file contents
   * @throws IOException I/O exception
   */
  public static TokenBuilder content(final IO in) throws IOException {
    return content(in, null);
  }

  /**
   * Returns the contents of the specified file, using the specified encoding.
   * @param in input path
   * @param enc encoding (will be ignored if set to {@code null})
   * @return file contents
   * @throws IOException I/O exception
   */
  public static TokenBuilder content(final IO in, final String enc)
      throws IOException {
    final TextInput ti = new TextInput(in);
    if(enc != null) ti.encoding(enc);
    final int len = (int) ti.length();
    final TokenBuilder tb = new TokenBuilder(len).factor(1.2);
    while(ti.pos() < len) tb.addUTF(ti.next());
    ti.close();
    return tb;
  }

  /**
   * Sets a new encoding.
   * @param e encoding
   * @throws IOException I/O exception
   */
  public void encoding(final String e) throws IOException {
    in[0].encoding(e);
  }

  /**
   * Returns the IO reference.
   * @return file reference
   */
  public IO io() {
    return file;
  }

  /**
   * Jumps the specified number of characters back.
   * @param p number of characters
   */
  public void prev(final int p) {
    pp -= p;
    pos();
  }

  /**
   * Reads the next character from the cached input buffers.
   * @return next character
   * @throws IOException I/O exception
   */
  public int next() throws IOException {
    if(pp != 0) return last[lp + pp++ & 0x0F];

    int ch = in[ip].readChar();
    while(ch == 0 && ip != 0) ch = in[--ip].readChar();
    last[lp++] = ch;
    lp &= 0x0F;
    if(ip == 0 && ch == '\n') line++;
    return ch;
  }

  /**
   * Inserts some bytes in the input stream.
   * @param val values to insert
   * @param s add spaces
   * @return true if everything went alright
   */
  public boolean add(final byte[] val, final boolean s) {
    if(s) a(new CachedInput(Token.SPACE));
    a(new CachedInput(val));
    if(s) a(new CachedInput(Token.SPACE));
    return ip < 20;
  }

  /**
   * Inserts a cached input buffer.
   * @param ci buffer to be added
   */
  private void a(final CachedInput ci) {
    if(++ip == in.length) in = Arrays.copyOf(in, ip << 1);
    in[ip] = ci;
    ci.encoding();
  }

  /**
   * Finishes file input.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    in[0].close();
  }

  /**
   * Returns the current file position.
   * @return file position
   */
  public int pos() {
    return Math.max(0, in[0].size() + pp);
  }

  /**
   * Returns the current line.
   * @return line
   */
  public int line() {
    return line;
  }

  /**
   * Returns the file length.
   * @return file position
   */
  public long length() {
    return in[0].length();
  }
}

