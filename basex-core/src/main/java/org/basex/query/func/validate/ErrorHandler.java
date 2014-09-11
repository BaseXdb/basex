package org.basex.query.func.validate;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Error handler.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class ErrorHandler extends DefaultHandler {
  /** Will contain all raised validation exception messages. */
  private final TokenList exceptions = new TokenList();

  @Override
  public void fatalError(final SAXParseException ex) {
    error(ex, "Fatal");
  }

  @Override
  public void error(final SAXParseException ex) {
    error(ex, "Error");
  }

  @Override
  public void warning(final SAXParseException ex) {
    error(ex, "Warning");
  }

  /**
   * Adds an error message.
   * @param ex exception
   * @param type type of error
   */
  private void error(final SAXParseException ex, final String type) {
    // may be recursively called if external validator (e.g. Saxon) is used
    String msg = ex.getMessage();
    if(msg.contains("Exception:")) {
      Throwable e = ex;
      while(e.getCause() != null) e = e.getCause();
      if(e instanceof SAXException) msg = e.getLocalizedMessage();
    } else {
      final TokenBuilder report = new TokenBuilder();
      final String id = ex.getSystemId();
      if(id != null) report.add(IO.get(id).name()).add(", ");
      report.addExt(ex.getLineNumber()).add(Text.COL).addExt(ex.getColumnNumber());
      report.add(": ").add(msg);
      msg = report.toString();
    }
    exceptions.add(type + Text.COL + msg);
  }

  /**
   * Returns the exception messages.
   * @return exception messages
   */
  TokenList getExceptions() {
    return exceptions;
  }
}
