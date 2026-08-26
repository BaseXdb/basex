package org.basex.server;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.util.*;

/**
 * This exception is thrown if a wrong user/password combination was specified.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LoginException extends IOException {
  /** Username (can be {@code null}). */
  private String name;

  /**
   * Constructor.
   * @param name username (can be {@code null})
   */
  public LoginException(final String name) {
    this(ACCESS_DENIED_X, name == null ? "-" : name);
    this.name = name;
  }

  /**
   * Constructs an exception with the specified message and extension.
   * @param message message
   * @param ext message extension
   */
  public LoginException(final String message, final Object... ext) {
    super(Util.info(message, ext));
  }

  /**
   * Returns the username.
   * @return username (can be {@code null})
   */
  public String name() {
    return name;
  }
}
