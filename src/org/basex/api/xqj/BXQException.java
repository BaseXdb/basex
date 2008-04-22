package org.basex.api.xqj;

import javax.xml.xquery.XQException;

/**
 * BaseX XQuery exception.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public  class BXQException extends XQException {
  /** 
   * Constructor.
   * @param ex query exception
   */
  public BXQException(org.basex.query.xquery.XQException ex) {
     this(ex.getMessage());
     setStackTrace(ex.getStackTrace());
  }

  /** 
   * Constructor.
   * @param msg query message
   */
  public BXQException(String msg) {
     super(msg);
  }
}
