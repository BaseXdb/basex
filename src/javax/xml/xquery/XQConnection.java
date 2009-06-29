/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import java.io.*;

/** 
 * A connection (session) with a specific XQuery engine. Connections are
 * obtained through an <code>XQDataSource</code> object.
 * <p>
 * XQuery expressions are executed and results are returned within the
 * context of a connection. They are either executed through <code>XQExpression</code>
 * or <code>XQPreparedExpression</code> objects. <p>
 *  
 * <pre>
 *  XQDataSource ds;// obtain the XQuery datasource
 *  ... 
 *  XQConnection conn = ds.getConnection();
 *
 *  XQPreparedExpression expr = conn.prepareExpression("for $i in ...");
 *  XQResultSequence result = expr.executeQuery();
 *  // - or - 
 *  XQExpression expr = conn.createExpression();
 *  XQSequence result = expr.executeQuery("for $i in..");
 *
 *  // The sequence can now be iterated 
 *  while (result.next())
 *  { 
 *     String str  = result.getItemAsString();
 *     System.out.println(" output "+ str);
 *  }
 *  result.close();
 *  expr.close(); 
 *  conn.close();  // close the connection and free all resources..
 *   
 * </pre>
 * 
 * A connection holds also default values for <code>XQExpression</code> and
 * <code>XQPreparedExpression</code> properties. An application can
 * override these defaults by passing an <code>XQStaticContext</code>
 * object to the <code>setStaticContext()</code> method.
 * <p>
 *
 * By default a connection operates in auto-commit mode, which means that
 * each xquery is executed and committed in an individual transaction. If
 * auto-commit mode is disabled, a transaction must be ended explicitly by
 * the application calling <code>commit()</code> or
 * <code>rollback()</code>.
 * <p>
 *
 * An <code>XQConnection</code> object can be created on top of an
 * existing JDBC connection. If an <code>XQConnection</code> is
 * created on top of the JDBC connection, it inherits the transaction context
 * from the JDBC connection. Also, in this case, if the auto-commit mode is
 * changed, or a transaction is ended using commit or rollback,
 * it also changes the underlying JDBC connection.
 * <p>
 *
 * An XQJ driver is not required to provide finalizer methods for 
 * the connection and other objects. Hence it is strongly recommended that 
 * users call close method explicitly to free any resources. It is also 
 * recommended that they do so under a final block to ensure that the object
 * is closed even when there are exceptions. Not closing this object explicitly
 * might result in serious memory leaks. <p>
 *
 * When the <code>XQConnection</code> is closed any <code>XQExpression</code>
 * and <code>XQPreparedExpression</code> objects obtained from it are also
 * implicitly closed. <p>
 *
 */
public interface XQConnection extends XQDataFactory
{
  /** 
   * Closes the connection. This also closes any <code>XQExpression</code> and
   * <code>XQPreparedExpression</code> obtained from this connection.
   * Once the connection is closed, no method other than <code>close</code> 
   * or the <code>isClosed</code> method may be called on the connection object.
   * Calling close on an <code>XQConnection</code> object that is already closed 
   * has no effect.
   * 
   * Note that an XQJ driver is not required to provide finalizer methods for  
   * the connection and other objects. Hence it is strongly recommended that 
   * users call this method explicitly to free any resources. It is also 
   * recommended that they do so under a final block to ensure that the object 
   * is closed even when there are exceptions. 
   * 
   * @exception XQException     if there is an error during closing the connection.
   */  
  public void close() throws XQException ;

  /**
   * Sets the auto-commit attribute to the given state. If a connection is in
   * auto-commit mode, each xquery is executed and committed in an individual
   * transaction. When auto-commit mode is disabled, xqueries are grouped in
   * a transaction that must be ended explicitly by the application calling
   * <code>commit()</code> or <code>rollback()</code>.<br>
   *
   * By default, new connections are in auto-commit mode.<br>
   *
   * If the value of auto-commit is changed in the middle of a transaction,
   * the transaction is committed. If <code>setAutoCommit</code>
   * is called and the auto-commit attribute is not changed from its
   * current value, the request is treated as a no-op.
   *
   * @param autoCommit        <code>true</code> to enable auto-commit mode;
   *                          <code>false</code> to disable it 
   * @exception XQException   if (1) the connection is in a closed state, 
   *                          or (2) auto-commit is turned off but the
   *                          implementation doesn't support transactions
   */
  public void setAutoCommit(boolean autoCommit) throws XQException;

  /**
   * Gets the auto-commit attribute of this connection
   * 
   * @return                  the auto-commit attribute of this connection.
   *                          <code>true</code> if the connection operates
   *                          in auto-commit mode; otherwise <code>false</code>
   * @exception XQException   if the connection is in a closed state
   */
  public boolean getAutoCommit() throws XQException;

  /** 
   * Makes all changes made in the current transaction permanent and releases
   * any locks held by the datasource. This method should be used only when
   * auto-commit mode is disabled.
   * 
   * Any <code>XQResultSequence</code>, or <code>XQResultItem</code> may be
   * implicitly closed upon commit, if the holdability property of the
   * sequence is set to <code>XQConstants.HOLDTYPE_CLOSE_CURSORS_AT_COMMIT</code>.
   *
   * @exception XQException     if the connection is in a closed state
   *                             or this connection is operating in auto-commit mode
   */  
  public void commit() throws XQException ;

  /**
   * Creates a new <code>XQExpression</code> object that can be used
   * to perform execute immediate operations with XQuery expressions.
   * The properties of the connection's default <code>XQStaticContext</code> are
   * copied to the returned <code>XQExpression</code>.
   * 
   * @return                    <code>XQExpression</code> that can be used to execute
   *                            multiple expressions
   * @exception XQException     if the connection is in a closed state
   */
  public XQExpression createExpression() throws XQException;

 /**
   * Creates a new <code>XQExpression</code> object that can be used to
   * perform execute immediate operations with XQuery expressions. The
   * properties of the specified <code>XQStaticContext</code> values are
   * copied to the returned <code>XQExpression</code>.
   *
   * @param properties          <code>XQStaticContext</code> containing
   *                            values of expression properties
   * @return                    <code>XQExpression</code> that can be used to execute
   *                            multiple expressions
   * @exception XQException     if (1) the connection is in a closed state, or
   *                            (2) the specified argument is <code>null</code>
   */
  public XQExpression createExpression(XQStaticContext properties) throws XQException;

  /**
   * Gets the metadata for this connection.
   *
   * @return                    <code>XQMetadata</code> representing the metadata of
   *                            this connection
   * @exception XQException     if the connection is in a closed state
   */
  public XQMetaData getMetaData() throws XQException;

  /** 
   * Checks if the connection is closed. 
   *
   * @return                    <code>true</code> if the connection is in a closed state,
   *                            <code>false</code> otherwise
   */
  public boolean isClosed();

  /**
   * Prepares an expression for execution. <p>
   *
   * The properties of the connection's default <code>XQStaticContext</code> are
   * copied to the returned <code>XQPreparedExpression</code>.
   *
   * @param xquery              the XQuery expression as a <code>String</code>.
   *                            Cannot be <code>null</code>
   * @return                    the prepared XQuery expression
   * @exception XQException     if (1) the connection is in a closed state, 
   *                            (2) there are errors preparing the expression,
   *                            or (3) the xquery parameter is <code>null</code>
   */
  public XQPreparedExpression prepareExpression(String xquery) 
          throws XQException; 

  /**
   * Prepares an expression for execution.  <p>
   *
   * The properties of the specified <code>XQStaticContext</code> values are
   * copied to the returned <code>XQPreparedExpression</code>.
   *
   * @param xquery              the XQuery expression as a <code>String</code>.
   *                            Cannot be <code>null</code>
   * @param properties          <code>XQStaticContext</code> containing
   *                            values of expression properties. 
   * @return                    the prepared XQuery expression
   * @exception XQException     if (1) the connection is in a closed state, or
   *                            (2) the specified argument is <code>null</code>
   */
   public XQPreparedExpression prepareExpression(java.lang.String xquery,
                    XQStaticContext properties)  throws XQException;

  /**
   * Prepares an expression for execution. <p>
   *
   * The properties of the connection's default <code>XQStaticContext</code> are
   * copied to the returned <code>XQPreparedExpression</code>.
   * 
   * @param xquery              the XQuery expression as a <code>Reader</code>.
   *                            Cannot be <code>null</code>
   * @return                    the prepared XQuery expression
   * @exception XQException     if (1) the connection is in a closed state, 
   *                            (2) there are errors preparing the expression,
   *                            or (3) the xquery parameter is <code>null</code>
   */
  public XQPreparedExpression prepareExpression(Reader xquery) 
     throws XQException;

  /**
   * Prepares an expression for execution. <p>
   *
   * The properties of the specified <code>XQStaticContext</code> values are
   * copied to the returned <code>XQPreparedExpression</code>.
   *
   * @param xquery              the XQuery expression as a <code>Reader</code>.
   *                            Cannot be <code>null</code>
   * @param properties          <code>XQStaticContext</code> containing
   *                            values of expression properties
   * @return                    the prepared XQuery expression
   * @exception XQException     if (1) the connection is in a closed state, or
   *                            (2) the specified argument is <code>null</code>
   */
   public XQPreparedExpression prepareExpression(Reader xquery,
                    XQStaticContext properties) throws XQException;

  /**
   * Prepares an expression for execution. <p>
   *
   * The properties of the connection's default <code>XQStaticContext</code> are
   * copied to the returned <code>XQPreparedExpression</code>.
   * 
   * @param xquery              the XQuery expression as an <code>InputStream</code>.
   *                            Cannot be <code>null</code>
   * @return                    the prepared XQuery expression
   * @exception XQException     if (1) the connection is in a closed state, 
   *                            (2) there are errors preparing the expression
   *                            or (3) the xquery parameter is <code>null</code>
   */
  public XQPreparedExpression prepareExpression(InputStream xquery) throws XQException; 

  /**
   * Prepares an expression for execution. <p>
   *
   * The properties of the specified <code>XQStaticContext</code> values are
   * copied to the returned <code>XQPreparedExpression</code>.
   *
   * @param xquery              the XQuery expression as an <code>InputStream</code>.
   *                            Cannot be <code>null</code>
   * @param properties          <code>XQStaticContext</code> containing
   *                            values of expression properties
   * @return                    the prepared XQuery expression
   * @exception XQException     if (1) the connection is in a closed state, or
   *                            (2) the specified argument is <code>null</code>
   */
  public XQPreparedExpression prepareExpression(InputStream xquery,
                   XQStaticContext properties) throws XQException; 

  /** 
   * Undoes all changes made in the current transaction and releases any
   * locks held by the datasource. This method should be used only when
   * auto-commit mode is disabled.
   *
   * @exception XQException     if the connection is in a closed state
   *                            or this connection is operating
   *                            in auto-commit mode
   */  
  public void rollback() throws XQException ;


  /**
   * Gets an <code>XQStaticContext</code> representing the default values for
   * all expression properties. In order to modify the defaults, it is not
   * sufficient to modify the values in the returned
   * <code>XQStaticContext</code> object; in addition
   * <code>setStaticContext</code> should be called to make those new values
   * effective.
   *
   * @return                    <code>XQStaticContext</code> representing the
   *                            default values for all expression properties
   *
   * @exception XQException     if the connection is in a closed state 
   */
  public XQStaticContext getStaticContext() throws XQException;

  /**
   * Sets the default values for all expression properties. The
   * implementation will read out all expression properties from the
   * specified <code>XQStaticContext</code> and update its private copy.
   *
   * @param properties          <code>XQStaticContext</code> containing
   *                            values of expression properties
   *
   * @exception XQException     if the connection is in a closed state 
   */
  public void setStaticContext(XQStaticContext properties) throws XQException;

};
