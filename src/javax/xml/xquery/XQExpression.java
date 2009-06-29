/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/**  
 * This interface describes the execute immediate functionality for 
 * expressions. This object can be created from the <code>XQConnection</code>
 * and the execution can be done using the <code>executeQuery()</code> or
 * <code>executeCommand()</code> method, passing in the XQuery expression. <p>
 * 
 * All external variables defined in the prolog of the expression to be executed must
 * be set in the dynamic context of this expression using the bind methods. 
 * Also, variables bound in this expression but not defined as external in
 * the prolog of the expression to be executed, are simply ignored.
 * For example, if variables <code>$var1</code> and <code>$var2</code> are bound,
 * but the query only defines <code>$var1</code> as external, no error
 * will be reported for the binding of <code>$var2</code>. It will simply
 * be ignored.
 *
 * When the expression is executed using the <code>executeQuery</code>
 * method, if the execution is successful, then 
 * an <code>XQResultSequence</code> object is returned. 
 * The <code>XQResultSequence</code> object is tied to
 * the <code>XQExpression</code> from which it was prepared and is
 * closed implicitly if that <code>XQExpression</code> is either closed or re-executed.  <p>
 *
 * The <code>XQExpression</code> object is dependent on 
 * the <code>XQConnection</code> object from which it was created and is only
 * valid for the duration of that object.
 * Thus, if the <code>XQConnection</code> object is closed then
 * this <code>XQExpression</code> object  will be implicitly closed
 * and it can no longer be used. <p>
 *
 * An XQJ driver is not required to provide finalizer methods for 
 * the connection and other objects. Hence it is strongly recommended that 
 * users call close method explicitly to free any resources. It is also 
 * recommended that they do so under a final block to ensure that the object
 * is closed even when there are exceptions. Not closing this object implicitly
 * or explicitly might result in serious memory leaks. <p>
 *
 * When the <code>XQExpression</code> is closed any <code>XQResultSequence</code>
 * object obtained from it is also implicitly closed. <p>
 *
 * Example -
 *
 * <pre>
 *  XQConnection conn = XQDatasource.getConnection();
 *  XQExpression expr = conn.createExpression();
 * 
 *  expr.bindInt(new QName("x"), 21, null);
 * 
 *  XQSequence result = expr.executeQuery(
 *     "declare variable $x as xs:integer external;
 *     for $i in $x return $i");
 *   
 *  while (result.next())
 *  {
 *     // process results ...
 *  }
 * 
 *  // Execute some other expression on the same object
 *  XQSequence result = expr.executeQuery("for $i in doc('foo.xml') return $i");
 *  ... 
 *
 *  result.close(); // close the result
 *  expr.close(); 
 *  conn.close(); 
 * </pre>
 * 
 */
public interface XQExpression extends XQDynamicContext
{
  /** 
   * Attempts to cancel the execution if both the XQuery engine and XQJ
   * driver support aborting the execution of an <code>XQExpression</code>. This method can
   * be used by one thread to cancel an <code>XQExpression</code>, that is being executed
   * in another thread. If cancellation is not supported or the attempt to
   * cancel the execution was not successful, the method returns without any
   * error. If the cancellation is successful, an <code>XQException</code> is
   * thrown, to indicate that it has been aborted, by <code>executeQuery</code>,
   * <code>executeCommand</code> or any method accessing the <code>XQResultSequence</code>
   * returned by <code>executeQuery</code>. If applicable, any open <code>XQResultSequence</code>
   * and <code>XQResultItem</code> objects will also be implicitly closed in this case.
   * 
   * @exception XQException     if the expression is in a closed state 
   */
  public void cancel() throws XQException;

  /** 
   * Checks if the expression is in a closed state.
   *
   * @return                    <code>true</code> if the expression is in
   *                            a closed state, <code>false</code> otherwise
   */
  public boolean isClosed();

  /** 
   * Closes the expression object and release associated resources. 
   *
   * Once the expression is closed, all methods on this object other than the 
   * <code>close</code> or <code>isClosed</code> will raise exceptions.  
   * This also closes any result sequences  obtained from this expression.
   * Calling <code>close</code> on an <code>XQExpression</code> object
   * that is already closed has no effect.
   *
   * @exception XQException     if there are errors when closing the expression
   */
  public void close() throws XQException;  

  /** 
   * Executes an implementation-defined command.
   * Calling this method implicitly closes any previous result sequence
   * obtained from this expression.
   *
   * @param cmd                 the input command as a string 
   * @exception XQException     if (1) there are errors when executing the command,
   *                            or (2) the expression is in a closed state
   */
  public void executeCommand(String cmd) throws XQException ;

  /** 
   * Executes an implementation-defined command.
   * Calling this method implicitly closes any previous result sequence
   * obtained from this expression.
   *
   * @param cmd                 the input command as a string reader
   * @exception XQException     if (1) there are errors when executing the command,
   *                            (2) the expression is in a closed state,
   *                            or (3) the execution is cancelled
   */
  public void executeCommand(java.io.Reader cmd) throws XQException;

  /** 
   * Executes a query expression. This implicitly closes any previous 
   * result sequences obtained from this expression.
   * 
   * @param query               the input query expression string.
   *                            Cannot be <code>null</code>
   * @return                    an <code>XQResultSequence</code> object containing
   *                            the result of the query execution
   * @exception XQException     if (1) there are errors when executing the query, 
   *                            (2) the expression is in a closed state, 
   *                            (3) the execution is cancelled,
   *                            (4) the query parameter is <code>null</code>
   */
  public XQResultSequence executeQuery(String query) throws XQException ;

  /** 
   * Executes a query expression. This implicitly closes any previous 
   * result sequences obtained from this expression.
   * 
   * @param query               the input query expression as a reader object.
   *                            Cannot be <code>null</code>
   * @return                    an <code>XQResultSequence</code> object containing
   *                            the result of the query execution
   * @exception XQException     if (1) there are errors when executing the query, 
   *                            (2) the expression is in a closed state, 
   *                            (3) the execution is cancelled, or
   *                            (4) the query parameter is <code>null</code>
   */ 
  public XQResultSequence executeQuery(java.io.Reader query) throws XQException;

  /** 
   * Executes a query expression. This implicitly closes any previous 
   * result sequences obtained from this expression.
   * 
   * If the query specifies a version declaration including an encoding, the
   * XQJ implementation may try use this information to parse the query. In
   * absence of the version declaration, the assumed encoding is
   * implementation dependent.
   *
   * @param query               the input query expression as a input stream object.
   *                            Cannot be <code>null</code>
   * @return                    an <code>XQResultSequence</code> containing the
   *                            result of the query execution
   * @exception XQException     if (1) there are errors when executing the query, 
   *                            (2) the expression is in a closed state, 
   *                            (3) the execution is cancelled, or
   *                            (4) the xquery parameter is <code>null</code>
   */
  public XQResultSequence executeQuery(java.io.InputStream query) throws XQException;

  /**
   * Gets an <code>XQStaticContext</code> representing the values for all
   * expression properties. Note that these properties cannot be changed; in
   * order to change, a new <code>XQExpression</code> needs to be created.
   *
   * @return                    an <code>XQStaticContext</code> representing 
   *                            the values for all expression properties
   * @exception XQException     if the expression is in a closed state
   */
  public XQStaticContext getStaticContext() throws XQException;

}
