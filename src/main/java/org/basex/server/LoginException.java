package org.basex.server;

import static org.basex.core.Text.*;
import java.io.IOException;

/**
 * This container is thrown if a wrong user/password combination was specified.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class LoginException extends IOException {
  /**
   * Constructor.
   */
  LoginException() {
    super(SERVERLOGIN);
  }
}
