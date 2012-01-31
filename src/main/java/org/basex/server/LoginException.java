package org.basex.server;

import static org.basex.core.Text.*;
import java.io.IOException;

import org.basex.util.Util;

/**
 * This exception is thrown if a wrong user/password combination was specified.
 *
 * @author BaseX Team 2005-12, BSD License
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
   * @param s message
   * @param e message extension
   */
  public LoginException(final String s, final Object... e) {
    super(Util.info(s, e));
  }
}
