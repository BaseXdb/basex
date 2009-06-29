/*
 * Copyright # 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import java.util.Properties;

/**
 * A factory for <code>PooledXQConnection</code> objects.
 * An object that implements this interface will typically be
 * registered with a JNDI based naming service.
 */
public interface ConnectionPoolXQDataSource {
	
  /**
   * Attempts to establish a physical connection to an XML datasource
   * that can be used as a pooled connection.
   *
   * @return  a <code>PooledXQConnection</code> object that is a physical
   *         connection to the XML datasource that this
   *         <code>ConnectionPoolXQDataSource</code> object represents
   * @exception XQException if a datasource access error occurs
   */
  PooledXQConnection getPooledConnection() throws XQException;
      
  /**
   * Attempts to establish a physical connection to an XML datasource
   * using the supplied username and password, that can be used as a
   * pooled connection.
   *
   * @param user the user on whose behalf the connection is being made
   * @param password the user's password
   * @return  a <code>PooledXQConnection</code> object that is a physical
   *         connection to the XML datasource that this
   *         <code>ConnectionPoolXQDataSource</code> object represents
   * @exception XQException if a datasource access error occurs
   */
  PooledXQConnection getPooledConnection(String user, String password) throws XQException;
  
  /** 
   * Gets the maximum time in seconds that this datasource can wait while
   * attempting to connect to a database. 
   * A value of zero means that the timeout is the default system timeout 
   * if there is one; otherwise, it means that there is no timeout. 
   * When a datasource object is created, the login timeout is 
   * initially zero. 
   * It is implementation-defined whether the returned login timeout is
   * actually used by the data source implementation.
   *
   * @return                    the datasource login time limit
   * @exception XQException     if a datasource access error occurs
   */
  public int getLoginTimeout() throws XQException;

 /** 
  * Retrieves the log writer for this datasource object.
  * The log writer is a character output stream to which all logging and
  * tracing messages for this datasource will be printed. This includes
  * messages printed by the methods of this object, messages printed by
  * methods of other objects manufactured by this object, and so on.
  * When a datasource object is created, the log writer is
  * initially <code>null</code>; in other words, the default is for logging
  * to be disabled.
  *
  * @return                    the log writer for this datasource or
  *                            <code>null</code> if logging is disabled
  * @exception XQException     if a datasource access error occurs
  */
 public java.io.PrintWriter getLogWriter() throws XQException;

 /**
  * Returns an array containing the property names supported by this
  * datasource. 
  *
  * @return      <code>String[]</code> an array of property names
  *              supported by this implementation
  */
public String[] getSupportedPropertyNames();

/**
  * Sets the named property to the specified value.  
  * If a property with the same name was already set, then this method
  * will override the old value for that property with the new value.<p>
  * If the implementation does not support the given property or if it
  * can determine that the value given for this property is invalid, then
  * an exception is thrown. If an exception is thrown, then no previous
  * value is overwritten.
  *
  * @param name                the name of the property to set
  * @param value               the value of the named property
  * @exception XQException     if (1) the given property is not recognized,
  *                            or (2) the value for the given property is
  *                            determined to be invalid
  */
 public void setProperty(String name, String value) throws XQException;

 /**
  * Returns the current value of the named property if set, else
  * <code>null</code>. If the implementation does not support the
  * given property then an exception is raised. 
  *
  * @param name                the name of the property whose value is
  *                            needed
  * @return                    <code>String</code> representing the value of
  *                            the required property if set, else
  *                            <code>null</code>
  * @exception XQException     if a given property is not supported
  */
 public String getProperty(String name) throws XQException;

/**
  * Sets the data source properties from the specified <code>Properties</code>
  * instance.  Properties set before this call will still apply but 
  * those with the same name as any of these properties will be replaced. 
  * Properties set after this call also apply and may
  * replace properties set during this call.<p>
  * If the implementation does not support one or more of the given
  * property names, or if it can determine that the value given for a
  * specific property is invalid, then an exception is thrown. If an
  * exception is thrown, then no previous value is overwritten.
  * is invalid, then an exception is raised.
  * @param props               the list of properties to set
  * @exception XQException     if (1) a given property is not recognized,
  *                            or (2) the value for a given property is
  *                            determined to be invalid
  */
  public void setProperties(Properties props) throws XQException;

  /** 
   * Sets the maximum time in seconds that this datasource will wait while
   * attempting to connect to a database. A value of zero specifies that
   * the timeout is the default system timeout if there is one; otherwise,
   * it specifies that there is no timeout. When a datasource
   * object is created, the login timeout is initially zero.
   * It is implementation-defined whether the specified login timeout is
   * actually used by the data source implementation.
   *
   * @param seconds             the datasource login time limit
   * @exception XQException     if a datasource access error occurs
   */
  public void setLoginTimeout(int seconds) throws XQException;

 /** 
  * Sets the log writer for this datasource object to the given
  * <code>java.io.PrintWriter</code> object. The log writer is a character output
  * stream to which all logging and tracing messages for this datasource
  * will be printed. This includes messages printed by the methods of this
  * object, messages printed by methods of other objects manufactured by
  * this object, and so on. When a datasource object is created
  * the log writer is initially <code>null</code>; in other words, the default
  * is for logging to be disabled.
  *
  * @param out                 the new log writer; to disable logging, set to
  *                            <code>null</code>
  * @exception XQException     if a datasource access error occurs
  */
 public void setLogWriter(java.io.PrintWriter out) throws XQException;

}
