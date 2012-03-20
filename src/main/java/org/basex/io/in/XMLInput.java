package org.basex.io.in;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.basex.io.IO;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class provides methods for reading XML input and recursive entities.
 * The input encoding will be guessed by analyzing the first bytes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class XMLInput extends InputStream {
  /** Input file. */
  private IO input;

  /** Input streams. */
  private TextInput[] inputs = new TextInput[1];
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
    inputs[0] = new TextInput(io.inputStream());
    input = io;
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
    return input;
  }

  /**
   * Jumps the specified number of characters back.
   * @param p number of characters
   */
  public void prev(final int p) {
    pp -= p;
    pos();
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
   * @param val values to insert
   * @param s add spaces
   * @return true if everything went alright
   * @throws IOException I/O exception
   */
  public boolean add(final byte[] val, final boolean s) throws IOException {
    if(s) add(new TextInput(new ArrayInput(Token.SPACE)));
    add(new TextInput(new ArrayInput(val)));
    if(s) add(new TextInput(new ArrayInput(Token.SPACE)));
    return ip < 32;
  }

  /**
   * Inserts a cached input buffer.
   * @param ti buffer to be added
   */
  private void add(final TextInput ti) {
    if(++ip == inputs.length) inputs = Arrays.copyOf(inputs, ip << 1);
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
    return Math.max(0, inputs[0].size() + pp);
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

  /**
   * Retrieves and returns the whole text and closes the stream.
   * @return contents
   * @throws IOException I/O exception
   */
  public byte[] content() throws IOException {
    // guess input size
    final int l = Math.max(32, input == null ? 0 : (int) input.length());
    final TokenBuilder tb = new TokenBuilder(l);
    try {
      for(int ch; (ch = read()) != -1;) tb.add(ch);
    } finally {
      close();
    }
    return tb.finish();
  }
}
