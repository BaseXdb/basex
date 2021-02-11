package org.basex.io.in;

import java.io.*;

/**
 * This class indicates exceptions during input stream processing.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class InputException extends IOException {
  /**
   * Constructor.
   * @param msg error message
   */
  InputException(final String msg) {
    super(msg);
  }
}
