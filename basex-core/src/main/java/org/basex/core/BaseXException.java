package org.basex.core;

import java.io.*;

import org.basex.util.*;

/**
 * Database exception, extending the {@link IOException}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXException extends IOException {
  /**
   * Constructs an exception with the specified message and extension.
   * @param message message with optional placeholders
   * @param ext optional message extension
   */
  public BaseXException(final String message, final Object... ext) {
    super(Util.info(message, ext));
    for(final Object o : ext) {
      if(o instanceof Throwable) {
        initCause((Throwable) o);
        break;
      }
    }
  }

  /**
   * Constructs an exception from the specified exception instance.
   * @param ex exception
   */
  public BaseXException(final Exception ex) {
    super(Util.message(ex));
    initCause(ex);
  }
}
