package org.basex.server;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.util.*;

/**
 * This exception is thrown if a wrong user/password combination was specified.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class LoginException extends IOException {
  /**
   * Constructor.
   */
  public LoginException() {
    super(ACCESS_DENIED);
  }

  /**
   * Constructs an exception with the specified message and extension.
   * @param message message
   * @param ext message extension
   */
  public LoginException(final String message, final Object... ext) {
    super(Util.info(message, ext));
  }
}
