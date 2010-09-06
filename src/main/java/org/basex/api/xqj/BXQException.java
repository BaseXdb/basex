package org.basex.api.xqj;

import javax.xml.xquery.XQException;
import org.basex.util.Util;

/**
 * Java XQuery API - Exception.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class BXQException extends XQException {
  /**
   * Constructor.
   * @param ex query exception
   */
  BXQException(final Exception ex) {
     this(ex.getMessage());
     setStackTrace(ex.getStackTrace());
  }

  /**
   * Constructs an exception with the specified message and extension.
   * @param s message
   * @param e message extension
   */
  BXQException(final String s, final Object... e) {
    super(Util.info(s, e));
  }
}
