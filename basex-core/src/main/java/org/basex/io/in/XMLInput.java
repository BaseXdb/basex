package org.basex.io.in;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class provides methods for reading XML input and recursive entities.
 * The input encoding will be guessed by analyzing the first bytes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class XMLInput extends InputStream {
  /** Input streams. */
  private NewlineInput[] inputs = new NewlineInput[1];
  /** Input pointer. */
  private int ip;
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
    if(ip == 0 && ch == '\n') ++line;
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
    if(spaces) add(new NewlineInput(Token.token(" ")));
    add(new NewlineInput(value));
    if(spaces) add(new NewlineInput(Token.token(" ")));
    return ip < 32;
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
  public int pos() {
    return Math.max(ip, inputs[ip].size() + pp);
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
    return inputs[0].length();
  }
}
