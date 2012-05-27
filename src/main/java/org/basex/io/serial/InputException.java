package org.basex.io.serial;

import java.io.*;

/**
 * This class indicates exceptions during the decoding of the input stream.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class InputException extends IOException {
  /**
   * Constructor.
   */
  public InputException() {
    super("Invalid input, or wrong encoding specified");
  }
}
