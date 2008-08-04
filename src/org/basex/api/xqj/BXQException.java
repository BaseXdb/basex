package org.basex.api.xqj;

import javax.xml.xquery.XQException;

/**
 * Java XQuery API - Exception.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
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
   * Constructor.
   * @param msg query message
   */
  BXQException(String msg) {
     super(msg);
  }
}
