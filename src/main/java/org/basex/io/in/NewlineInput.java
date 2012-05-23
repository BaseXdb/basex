package org.basex.io.in;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class provides a convenient access to text input. System dependent
 * line breaks ({@code \r\n}, {@code \n}, {@code \r}) will be normalized to
 * newline characters {@code \n}, and the input encoding will be guessed by
 * analyzing the first bytes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class NewlineInput extends TextInput {
  /** Next value ({@code -2} means no caching). */
  private int next = -2;

  /**
   * Constructor.
   * @param is input stream
   * @throws IOException I/O exception
   */
  public NewlineInput(final InputStream is) throws IOException {
    super(is);
  }

  /**
   * Constructor.
   * @param in input
   * @throws IOException I/O exception
   */
  public NewlineInput(final IO in) throws IOException {
    super(in);
  }

  @Override
  public NewlineInput encoding(final String encoding) throws IOException {
    super.encoding(encoding);
    return this;
  }

  @Override
  public int read() throws IOException {
    int n = next;
    if(n != -2) {
      next = -2;
    } else {
      n = super.read();
    }
    if(n != '\r') return n;
    n = super.read();
    if(n != '\n') next = n;
    return '\n';
  }

  /**
   * Reads a single line.
   * @return line
   * @throws IOException I/O exception
   */
  public TokenBuilder readLine() throws IOException {
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final int ch = read();
      if(ch == -1) return tb.isEmpty() ? null : tb;
      if(ch == '\n') return tb;
      tb.add(ch);
    }
  }
}
