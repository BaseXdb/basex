/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;
import javax.xml.namespace.QName;

/** 
 * This interface describes an expression that can be prepared for multiple
 * subsequent executions. A prepared expression can be created from the
 * connection. <p>
 * The preparation of the expression does the static analysis of the expression 
 * using the static context information. <p>
 *
 * The dynamic context information, such as values for bind variables, can then
 * be set using the setter methods. When setting values for bind variables,
 * these  variables should be present as external 
 * variables in the prolog of the prepared expression.<p>  
 *
 * The static type information of the query can also be retrieved if the XQuery
 * implementation provides it using the <code>getStaticResultType</code> 
 * method.<p>
 *
 * When the expression is executed using the <code>executeQuery</code>
 * method, if the execution is successful, then 
 * an <code>XQResultSequence</code> object is returned. 
 * The <code>XQResultSequence</code> object is tied to
 * the <code>XQPreparedExpression</code> from which it was prepared and is
 * closed implicitly if that  expression is either closed or if re-executed.  <p>
 *
 * The <code>XQPreparedExpression</code> object is dependent on 
 * the <code>XQConnection</code> object from which it was created and is only
 * valid for the duration of that object.
 * Thus, if the <code>XQConnection</code> object is closed then
 * this <code>XQPreparedExpression</code> object  will be implicitly closed
 * and it can no longer be used.<p>
 *
 * An XQJ driver is not required to provide finalizer methods for 
 * the connection and other objects. Hence it is strongly recommended that 
 * users call close method explicitly to free any resources. It is also 
 * recommended that they do so under a final block to ensure that the object
 * is closed even when there are exceptions. 
 * Not closing this object implicitly or explicitly might result in serious memory 
 * leaks.<p>
 *
 * When the <code>XQPreparedExpression</code> is closed any 
 * <code>XQResultSequence</code> object obtained from it
 * is also implicitly closed. <p>
 *
 * Example -
 * <pre>
 *  XQConnection conn = XQDataSource.getconnection();
 *  XQPreparedExpression expr = conn.prepareExpression
 *          ("for $i in (1) return 'abc' "); 
 * 
 *  // get the sequence type out.. This would be something like xs:string *
 *  XQSequenceType type = expr.getStaticResultType();
 *
 *  XQSequence result1 = expr.executeQuery();
 * 
 *  // process the result..
 *  result1.next();
 *  System.out.println(" First result1 "+ result1.getAtomicValue());
 *
 *  XQResultSequence result2 = expr.executeQuery();
 *
 *  // result1 is implicitly closed 
 *  // recommended to close the result sequences explicitly.
 *
 *  // process the result..
 *  while (result2.next()) 
 *     System.out.println(" result is "+ result2.getAtomicValue());
 * 
 *  result2.close(); 
 *  expr.close(); // closing expression implicitly closes all result sequence or
 *                // items obtained from this expression.
 *  conn.close(); // closing connections will close expressions and results.
 * </pre>
 */

public interface XQPreparedExpression extends XQDynamicContext
{
  /** 
   * Attempts to cancel the execution if both the XQuery engine and XQJ
   * driver support aborting the execution of an <code>XQPreparedExpression</code>.
   * This method can be used by one thread to cancel an <code>XQPreparedExpression</code>,
   * that is being executed in another thread. If cancellation is not supported or
   * the attempt to cancel the execution was not successful, the method returns without
   * any error. If the cancellation is successful, an <code>XQException</code>
   * is thrown, to indicate that it has been aborted, by <code>executeQuery</code>,
   * <code>executeCommand</code> or any method accessing the <code>XQResultSequence</code>
   * returned by <code>executeQuery</code>. If applicable, any open <code>XQResultSequence</code>
   * and <code>XQResultItem</code> objects will also be implicitly closed in this case.
   *
   * @exception XQException     if the prepared expression is in a closed state 
   */
  public  void cancel() throws XQException;

  /** 
   * Checks if the prepared expression in a closed state. 
   *
   * @return                    <code>true</code> if the prepared expression is in
   *                            a closed state, <code>false</code> otherwise.
   */
  public boolean isClosed() ;

  /** 
   * Closes the expression object  and release all resources associated with
   * this prepared expression. This also closes any result sequences obtained
   * from this expression.
   *
   * Once the expression is closed, all methods on this object other than the
   * <code>close</code> or <code>isClosed</code> will raise exceptions.
   * Calling close on an <code>XQExpression</code> object that is already closed has no 
   * effect.
   *
   * @exception XQException     if there are errors when closing the expression
   */
  public void close() throws XQException;   

  /** 
   * Executes the prepared query expression. 
   * Calling this method implicitly closes any previous result sequence 
   * obtained from this expression.
   *
   * @return                    the xquery sequence object containing the result of the 
   *                            query execution
   * @exception XQException     if (1) there are errors when executing the prepared 
   *                            expression, (2) the prepared expression is in a closed state,
   *                            or (3) the query execution is cancelled
   */
  public XQResultSequence executeQuery()  throws XQException;

 /**
   * Retrieves all the external variables defined in the prolog of the 
   * prepared expression.
   *
   * @return                    an array of <code>QName</code> objects for all the external
   *                            variables defined in the prolog of a prepared expression. 
   *                            Empty array if there are no external variables present.
   * @exception XQException     if the prepared expression is in a closed state
   */
  public QName[] getAllExternalVariables() throws XQException;

  /**
   * Retrieves the names of all unbound external variables. 
   *
   * @return                    the <code>QName</code> for all the external variables defined
   *                            in the prolog of a prepared expression that are yet to be bound 
   *                            with a value. If there are no such variables an empty array 
   *                            is returned
   * @exception XQException     if the prepared expression is in a closed state
   */
  public QName[] getAllUnboundExternalVariables() throws XQException;

  /**
   * Gets the static type information of the result sequence. If an 
   * implementation does not do static typing of the query, then 
   * this method must return an <code>XQSequenceType</code> object
   * corresponding to the XQuery sequence type <code>item()*</code>.
   *
   * @return                    <code>XQSequenceType</code> containing the static
   *                            result information.
   * @exception XQException     if the prepared expression is in a closed state
   */
  public XQSequenceType getStaticResultType() throws XQException;

 /**
   * Retrieves the static type of a given external variable.
   *
   * @param name                the name of the external variable
   * @return                    the static type information of the variable as defined
   *                            in the prolog of the prepared expression
   * @exception XQException     if (1) the variable does not exist in the static
   *                            context of the expression, or (2) the sequence is
   *                            in a closed state, or (3) the <code>name</code>
   *                            parameter is <code>null</code>
   */
  public XQSequenceType getStaticVariableType(QName name) throws XQException;

  /**
   * Gets an <code>XQStaticContext</code> representing the values for all
   * expression properties. Note that these properties cannot be changed; in
   * order to change, a new <code>XQPreparedExpression</code> needs to be created.
   *
   * @return                    an <code>XQStaticContext</code> representing 
   *                            the values for all expression properties
   * @exception XQException     if the expression is in a closed state
   */
  public XQStaticContext getStaticContext() throws XQException;

};
