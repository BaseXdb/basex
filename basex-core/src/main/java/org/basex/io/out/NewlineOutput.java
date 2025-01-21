package org.basex.io.out;

import java.io.*;

/**
 * This class is a wrapper for outputting text with specific newline characters.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NewlineOutput extends PrintOutput {
  /** Print output. */
  private final PrintOutput po;
  /** Newline token. */
  private final byte[] newline;

  /**
   * Constructor, given an output stream.
   * @param po output stream reference
   * @param newline newline string
   */
  public NewlineOutput(final PrintOutput po, final byte[] newline) {
    super(po);
    this.newline = newline;
    this.po = po;
  }

  @Override
  public void print(final int cp, final Fallback fallback) throws IOException {
    if(cp == '\n') {
      for(final byte b : newline) po.print(b, fallback);
      lineLength = 0;
    } else {
      po.print(cp, fallback);
      ++lineLength;
    }
  }

  @Override
  public boolean finished() {
    return po.finished();
  }
}
