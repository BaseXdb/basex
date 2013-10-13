package org.basex.io.serial;

import java.io.*;

/**
 * This class indicates exceptions during input stream processing.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class InputException extends IOException {
  /**
   * Constructor.
   */
  public InputException() {
    this("Invalid input, or wrong encoding specified");
  }

  /**
   * Constructor.
   * @param msg error message
   */
  public InputException(final String msg) {
    super(msg);
  }
}
