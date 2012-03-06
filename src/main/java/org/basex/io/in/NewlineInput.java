package org.basex.io.in;

import java.io.IOException;
import java.io.InputStream;

import org.basex.io.IO;
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
  /**
   * Constructor.
   * @param is input stream
   * @param enc encoding
   * @throws IOException I/O exception
   */
  public NewlineInput(final InputStream is, final String enc) throws IOException {
    super(is);
    if(enc != null) encoding(enc);
  }

  /**
   * Constructor.
   * @param in input
   * @param enc encoding
   * @throws IOException I/O exception
   */
  public NewlineInput(final IO in, final String enc) throws IOException {
    this(in.inputStream(), enc);
  }

  @Override
  public int read() throws IOException {
    final int ch = super.read();
    if(ch != '\r') return ch;
    if(super.read() != '\n') prev(1);
    return '\n';
  }

  /**
   * Reads a single line.
   * @return line
   * @throws IOException I/O exception
   */
  public String readLine() throws IOException {
    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final int ch = read();
      if(ch == -1) return tb.isEmpty() ? null : tb.toString();
      if(ch == '\n') return tb.toString();
      tb.add(ch);
    }
  }
}
