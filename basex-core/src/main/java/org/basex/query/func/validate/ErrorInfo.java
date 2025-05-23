package org.basex.query.func.validate;

import org.basex.io.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Error info.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ErrorInfo {
  /** Error level. */
  enum Level {
    /** Fatal. */ FATAL,
    /** Error. */ ERROR,
    /** Warning. */ WARNING;

    @Override
    public String toString() {
      return Strings.capitalize(Enums.string(this));
    }
  }

  /** Message. */
  final String message;
  /** Level. */
  final Level level;
  /** URL. */
  String url;
  /** Line number. */
  int line = Integer.MIN_VALUE;
  /** Column number. */
  int column = Integer.MIN_VALUE;

  /**
   * Constructor.
   * @param ex exception
   * @param level type
   * @param schema schema url
   */
  ErrorInfo(final SAXException ex, final Level level, final IO schema) {
    this.level = level;

    String m = ex.getMessage();
    Throwable e = ex;
    if(m.contains("Exception:")) {
      // may be recursively called if external validator (e.g. Saxon) is used
      e = Util.rootException(e);
      if(e instanceof SAXException) m = e.getLocalizedMessage();
    }

    if(ex instanceof final SAXParseException se) {
      final String id = se.getSystemId();
      if(id != null) {
        final IO io = IO.get(id);
        if(schema == null || !io.isDir() && !schema.equals(io)) url = id;
      }
      line = se.getLineNumber();
      column = se.getColumnNumber();
    }
    message = m;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(url != null) sb.append(url).append(", ");
    if(line != Integer.MIN_VALUE) sb.append(line).append(':').append(column).append(": ");
    return sb.append(message).toString();
  }
}
