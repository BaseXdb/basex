package org.basex.core;

import java.io.*;

import org.basex.util.*;

/**
 * Database exception, extending the {@link IOException}.
 *
 * @author BaseX Team 2005-13, BSD License
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

  /**
   * Creates the error message from the specified text and extension array.
   * @param text text message with optional placeholders
   * @param ext info extensions
   * @return argument
   */
  public static String message(final String text, final Object[] ext) {
    final int es = ext.length;
    for(int e = 0; e < es; ++e) {
      if(ext[e] instanceof byte[]) {
        ext[e] = Token.string((byte[]) ext[e]);
      } else if(ext[e] instanceof Throwable) {
        ext[e] = Util.message((Throwable) ext[e]);
      } else if(!(ext[e] instanceof String)) {
        ext[e] = String.valueOf(ext[e]);
      }
    }
    return Util.info(text, ext);
  }
}
