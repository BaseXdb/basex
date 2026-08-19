package org.basex.io.in;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class provides methods for reading XML input and recursive entities.
 * The input encoding will be guessed by analyzing the first bytes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class XMLInput extends InputStream {
  /** Input streams. */
  private NewlineInput[] inputs = new NewlineInput[1];
  /** Input pointer. */
  private int ip;
  /** Current line. */
  private int line = 1;
  /** Number of characters read in the current line. */
  private int column;
  /** Entity expansion counter. */
  private int exp;

  /** Buffer with most recent characters. */
  private final int[] last = new int[16];
  /** Read position. */
  private int lp;
  /** Backward pointer. */
  private int pp;

  /**
   * Constructor.
   * @param io input reference
   * @throws IOException I/O exception
   */
  public XMLInput(final IO io) throws IOException {
    inputs[0] = new NewlineInput(io);
  }

  /**
   * Sets a new encoding.
   * @param e encoding
   * @throws IOException I/O exception
   */
  public void encoding(final String e) throws IOException {
    inputs[0].encoding(e);
  }

  /**
   * Returns the IO reference.
   * @return file reference
   */
  public IO io() {
    return inputs[0].io();
  }

  /**
   * Jumps the specified number of characters back.
   * @param p number of characters
   */
  public void prev(final int p) {
    pp -= p;
  }

  @Override
  public int read() throws IOException {
    if(pp != 0) return last[lp + pp++ & 0x0F];
    int ch = inputs[ip].read();
    while(ch == -1 && ip != 0) ch = inputs[--ip].read();
    last[lp++] = ch;
    lp &= 0x0F;
    if(ip == 0) {
      if(ch == '\n') {
        ++line;
        column = 0;
      } else {
        ++column;
      }
    }
    return ch;
  }

  /**
   * Inserts some bytes in the input stream.
   * @param value values to insert
   * @param spaces add spaces
   * @return true if everything went alright
   * @throws IOException I/O exception
   */
  public boolean add(final byte[] value, final boolean spaces) throws IOException {
    if(spaces) add(new NewlineInput(new IOContent(Token.cpToken(' '))));
    add(new NewlineInput(new IOContent(value)));
    if(spaces) add(new NewlineInput(new IOContent(Token.cpToken(' '))));
    return ++exp < (inputs[0].size() + 1) * 10 && ip < 32;
  }

  /**
   * Inserts a cached input buffer.
   * @param ti buffer to be added
   */
  private void add(final NewlineInput ti) {
    if(++ip == inputs.length) inputs = Array.copy(inputs, new NewlineInput[Array.newCapacity(ip)]);
    inputs[ip] = ti;
  }

  @Override
  public void close() throws IOException {
    inputs[0].close();
  }

  /**
   * Returns the current file position.
   * @return file position
   */
  public long pos() {
    return Math.max(ip, inputs[ip].size() + pp);
  }

  /**
   * Returns the current line.
   * @return line
   */
  public int line() {
    int ln = line;
    for(int p = pp; p < 0; p++) {
      if(last[lp + p & 0x0F] == '\n') --ln;
    }
    return ln;
  }

  /**
   * Returns the column of the next character to be read.
   * @return column
   */
  public int column() {
    return column + pp + 1;
  }

  /**
   * Returns the file length.
   * @return file position
   */
  public long length() {
    return inputs[0].length();
  }
}
