/*
 * Copyright # 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import java.util.EventObject;

/**
 * An event object that provides information about the
 * source of a connection-related event.  <code>XQConnectionEvent</code>
 * objects are generated when an application closes a pooled connection
 * and when an error occurs.  The <code>XQConnectionEvent</code> object
 * contains the folowing information:
 * <ul>
 *   <li>The pooled connection closed by the application
 *   <li>In the case of an error, the <code>XQException</code>
 *       to be thrown to the application
 * </ul>
 */

public class XQConnectionEvent extends EventObject {
	
  /**
   * The <code>XQException</code> that will be thrown to the
   * application when an error occurs and the pooled connection is no
   * longer usable.
   */
  private XQException ex = null;

  /**
   * Constructs an <code>XQConnectionEvent</code> object initialized with
   * the given <code>PooledXQConnection</code> object. <code>XQException</code>
   * defaults to <code>null</code>.
   *
   * @param con the pooled connection that is the source of the event
   */
  public XQConnectionEvent(PooledXQConnection con) {
    super(con);         
  }

  /**
   * Constructs an <code>XQConnectionEvent</code> object initialized with
   * the given <code>PooledXQConnection</code> object and 
   * <code>XQException</code> object.
   *
   * @param con the pooled connection that is the source of the event
   * @param ex the XQException to be thrown to the application
   */
  public XQConnectionEvent(PooledXQConnection con, XQException ex) {
    super(con);  
    this.ex = ex;
  }
 
  /**
   * Retrieves the <code>XQException</code> for this
   * <code>XQConnectionEvent</code> object.
   *
   * @return the <code>XQException</code> to be thrown or <code>null</code>
   */
  public XQException getXQException() { return ex; }

}
