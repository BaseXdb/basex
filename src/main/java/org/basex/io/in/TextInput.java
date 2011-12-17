package org.basex.io.in;

import java.io.IOException;
import java.util.Arrays;

import org.basex.io.IO;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class provides a convenient access to text input.
 * The encoding will initially be guessed by analyzing the first bytes.
 *
 * @author BaseX Team 2005-11, BSD License
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

    final TokenBuilder tb = new TokenBuilder(Math.max(32, (int) in.length()));
    final TextInput ti = new TextInput(in);
    try {
      if(enc != null) ti.encoding(enc);
      for(int ch; (ch = ti.next()) != -1;) {
        // normalize newlines
        if(ch == '\r') {
          if(ti.next() != '\n') ti.prev(1);
          ch = '\n';
        }
        tb.add(ch);
      }
    } finally {
      ti.close();
    }
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
    while(ch == -1 && ip != 0) ch = in[--ip].readChar();
    last[lp++] = ch;
    lp &= 0x0F;
    if(ip == 0 && ch == '\n') ++line;
    return ch;
  }

  /**
   * Inserts some bytes in the input stream.
   * @param val values to insert
   * @param s add spaces
   * @return true if everything went alright
   * @throws IOException I/O exception
   */
  public boolean add(final byte[] val, final boolean s) throws IOException {
    if(s) add(new ArrayInput(Token.SPACE));
    add(new ArrayInput(val));
    if(s) add(new ArrayInput(Token.SPACE));
    return ip < 32;
  }

  /**
   * Inserts a cached input buffer.
   * @param ci buffer to be added
   * @throws IOException I/O exception
   */
  private void add(final ArrayInput ci) throws IOException {
    if(++ip == in.length) in = Arrays.copyOf(in, ip << 1);
    in[ip] = ci;
    ci.encoding();
  }

  /**
   * Finishes the file input.
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

