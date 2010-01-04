package org.basex.build.xml;

import java.io.IOException;
import java.util.Arrays;
import org.basex.io.IO;
import org.basex.io.BufferInput;
import org.basex.io.CachedInput;
import org.basex.util.Token;

/**
 * This class provides a convenient access to the XML input.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class XMLInput {
  /** Input stream reference. */
  BufferInput[] in = new BufferInput[1];

  /** Input pointer. */
  int ip;
  /** Input file. */
  IO file;
  /** Current line. */
  int line = 1;
  /** Current column. */
  int col = 1;

  /** Buffer with most current characters. */
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
  public XMLInput(final IO f) throws IOException {
    in[0] = f.buffer();
    in[0].encoding();
    file = f;
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
    if(ip == 0) {
      if(ch == '\n') {
        line++;
        col = 0;
      }
      col++;
    }
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
  public void finish() throws IOException {
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
   * Returns the file length.
   * @return file position
   */
  public long length() {
    return in[0].length();
  }
}

