package org.basex.io.serial;

import java.io.*;

/**
 * This class indicates exceptions during the decoding of the input stream.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class EncodingException extends IOException {
  /**
   * Constructor.
   * @param cp code points
   */
  public EncodingException(final int cp) {
    super("Invalid character (code point: " + cp + ")");
  }
}
