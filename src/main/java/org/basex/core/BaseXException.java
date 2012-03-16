package org.basex.core;

import java.io.*;

import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Database exception, extending the {@link IOException}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXException extends IOException {
  /**
   * Constructs an exception with the specified message and extension.
   * @param s message
   * @param e message extension
   */
  public BaseXException(final String s, final Object... e) {
    super(Util.info(s, e));
  }

  /**
   * Constructs an exception from the specified exception instance.
   * @param ex exception
   */
  public BaseXException(final Exception ex) {
    super(Util.message(ex));
    setStackTrace(ex.getStackTrace());
  }

  /**
   * Creates the error message from the specified info and extension array.
   * @param info info message
   * @param ext info extensions
   * @return argument
   */
  public static String message(final String info, final Object[] ext) {
    final int es = ext.length;
    for(int e = 0; e < es; ++e) {
      if(ext[e] instanceof byte[]) {
        ext[e] = Token.string((byte[]) ext[e]);
      } else if(ext[e] instanceof Throwable) {
        ext[e] = Util.message((Throwable) ext[e]);
      } else if(!(ext[e] instanceof String)) {
        ext[e] = ext[e].toString();
      }
    }
    return Util.info(info, ext);
  }
}
