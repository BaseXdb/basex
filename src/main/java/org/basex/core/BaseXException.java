package org.basex.core;

import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This is a simple container for sessions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class BaseXException extends Exception {
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
    this(ex.getMessage() != null ? ex.getMessage() : ex.toString());
  }

  /**
   * Creates the error message from the specified info and extension array.
   * @param info info message
   * @param ext info extensions
   * @return argument
   */
  public static String message(final String info, final Object[] ext) {
    for(int i = 0; i < ext.length; ++i) {
      if(ext[i] instanceof byte[]) {
        ext[i] = Token.string((byte[]) ext[i]);
      } else if(ext[i] instanceof Throwable) {
        final Throwable th = (Throwable) ext[i];
        ext[i] = th.getMessage() != null ? th.getMessage() : th.toString();
      } else if(!(ext[i] instanceof String)) {
        ext[i] = ext[i].toString();
      }
      // [CG] XQuery/Exception: verify if/which strings are to be chopped
      //final String s = t[i].toString();
      //t[i] = s.length() > 1000 ? s.substring(0, 1000) + DOTS : s;
    }
    return Util.info(info, ext);
  }
}
