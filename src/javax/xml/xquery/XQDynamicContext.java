/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;
import java.io.*;
import javax.xml.namespace.QName;
import org.w3c.dom.Node;

/**  
 * <code>XQDynamicContext</code> provides access to the dynamic context as defined in
 * <a href="http://www.w3.org/TR/xquery/#eval_context"><i>2.1.2
 * Dynamic Context, XQuery 1.0: An XML Query Language</i></a>. 
 * The following components can be accessed:
 *
 * <ul>
 *   <li>The context item can be set</li>
 *   <li>The variable values can be bound</li>
 *   <li>The implicit time zone can be retrieved or specified</li>
 * </ul>
 *
 * Where the prolog of the expression specifies the static type of external
 * variables, this interface allows the dynamic type and value of the
 * variable to be specified.
 *
 * Note that in case of an <code>XQPreparedExpression</code>, values may only be bound
 * to those variables that are defined in the prolog of the expression.
 *
 * <p>
 * Example -
 *
 * <pre>
 *
 *  XQConnection conn = XQDataSource.getConnection();
 *
 *  // create an XQPreparedExpression with external variable
 *  XQPreparedExpression e1 = conn.prepareExpression("declare variable $i as xs:int external;
 *                                                    $i + 10");
 *
 *  // bind an int to the external variable
 *  e1.bindInt(new QName("i"), 200, null);
 *
 *  // this will fail as the expression has no external variable $foo
 *  e1.bindInt(new QName("foo"), 200, null); 
 *
 *  // this will fail as xs:double is not compatible with an external
 *  variable declared as xs:int
 *  e1.bindDouble(new QName("i"), 2e2, null);
 *
 *  // create an XQExpression with external variable
 *
 *  XQExpression e2 = conn.createExpression();
 *
 *  // bind a value to $i and $foo
 *  e2.bindInt(new QName("i"), 200, null); 
 *  e2.bindInt(new QName("foo"), 200, null);
 *
 *  // the value bound to $foo is ignored as the expression doesn't
 *  // declare $foo as external variable
 *
 *  e2.executeQuery("declare variable $i as xs:int external; $i + 10");
 *
 * </pre>
 * <br>
 * <br>
 * Binding a value to the context item is achieved in the same way as binding a
 * value to an external variable. However, instead of specifying the variable's
 * name as first argument of the <code>bindXXX()</code> method, use 
 * {@link XQConstants#CONTEXT_ITEM XQConstants.CONTEXT_ITEM} as the first
 * argument.
 * <br>
 * <br>
 * <b>Binding mode</b>
 * <p>
 * The default binding mode is immediate. In other words, the external
 * variable value specified by the application is consumed during the
 * <code>bindXXX()</code> method.
 * <p>
 * An application has the ability to set the binding mode to deferred. In deferred
 * mode an application cannot assume that the bound value will be
 * consumed during the invocation of the <code>bindXXX</code> method. In
 * such scenario the order in which the bindings are evaluated is
 * implementation-dependent, and an implementation doesn't necessarily need
 * to consume a binding if it can evaluate the query without requiring the
 * external variable. The XQJ implementation is also free to read the bound
 * value either at bind time or during the subsequent evaluation and
 * processing of the query results.
 * <p>
 * Also note that in deferred binding mode, bindings are only active for a
 * single execution cycle. The application is required to explicitly
 * re-bind values to every external variable before each execution. Failing
 * to do so will result in an <code>XQException</code>, as the
 * implementation will assume during the next execution that none of the
 * external variables are bound.
 * <p>
 * Finally, note that in deferred binding mode, any error condition
 * specified to throw an exception during the <code>bindXXX()</code> methods,
 * may as well be thrown later during the query's evaluation.
 * <p>
 * Example - in case of an immediate binding mode, bindings stay active
 * over executions
 * <pre>
 *  // BINDING_MODE_IMMEDIATE is the default, no need to change it
 *  QName v = new QName(v);
 *
 *  XQPreparedExpression e = c.prepareExpression("declare variable $v
 *                                                external; $v");
 *  e.bindInt(v, 1)
 *
 *  // successful execution
 *  e.executeQuery();
 *
 *  // successful execution
 *  e.executeQuery(); 
 * </pre>
 * <br>
 * <br>
 * Example - in case of a deferred binding mode, bindings are only valid
 *           for a single execution
 * <pre>
 *  // BINDING_MODE_IMMEDIATE is the default, change it to
 *  // BINDING_MODE_DEFERRED
 *  XQStaticContext cntxt = c.getStaticContext();
 *  cntxt.setBindingMode(XQConstants.BINDING_MODE_DEFERRED);
 *  c.setStaticContext(cntxt);
 *
 *  QName v = new QName(v);
 *
 *  XQPreparedExpression e = c.prepareExpression("declare variable $v
 *                                                external; $v");
 *  e.bindInt(v, 1)
 *
 *  // successful execution
 *  XQSequence s = e.executeQuery();
 *
 *  while (s.next())
 *    System.out.println(s.getInt());
 *
 *  // an error is reported during the next query
 *  // evaluation as not all external variables are bound
 *  s = e.executeQuery(); 
 *
 *  while (s.next())
 *    System.out.println(s.getInt());
 * </pre>
 */
public interface XQDynamicContext 
{
  /**
    * Gets the implicit timezone
    *
    * @return                 the implicit timezone. This may have been set by an
    *                         application using the <code>setImplicitTimeZone</code> method
    *                         or provided by the implementation
    *
    * @exception XQException  if the expression is in a closed state
    *
    */
  public java.util.TimeZone getImplicitTimeZone() throws XQException;

 /**
   * Binds a value to the given external variable or the context item.
   * The value is converted into an instance of the specified type according to
   * the casting from <code>xs:string</code> rules outlined in
   * <a href="http://www.w3.org/TR/xpath-functions/#casting-from-strings">
   * <i>17.1.1 Casting from xs:string and xs:untypedAtomic, XQuery 1.0 and
   * XPath 2.0 Functions and Operators</i></a>.
   * If the cast fails, or if there is a mismatch between the static and 
   * dynamic types, an <code>XQException</code> is thrown either by this
   * method or during query evaluation.
   *
   * @param varName          the name of the external variable to bind to
   * @param value            the lexical string value of the type
   * @param type             the item type of the bind
   *
   * @throws XQException     if (1) any of the arguments are <code>null</code>,
   *                         (2) given type is not an atomic type,
   *                         (3) the conversion of the value to an XDM instance failed,
   *                         (4) in case of an <code>XQPreparedExpression</code>,
   *                         the dynamic type of the bound value is not compatible 
   *                         with the static type of the variable,
   *                         (5) in case of an <code>XQPreparedExpression</code>, 
   *                         the variable is not defined in the prolog of the expression,
   *                         or (6) the expression is in a closed state
   */
  public void bindAtomicValue(QName varName, String value, XQItemType type)
         throws XQException;

  
 /**
   * Binds a value to the given external variable or the context item. The
   * value is converted into an instance of the specified type, which must
   * represent an <code>xs:string</code> or a type derived by restriction
   * from <code>xs:string</code>. If the specified type is <code>null</code>,
   * it defaults to <code>xs:string</code>.<br>
   *
   * Subsequently the value is converted into an instance of the specified
   * type according to the rule defined in <i>14.2 Mapping a Java Data Type to
   * an XQuery Data Type, XQuery API for Java (XQJ) 1.0,</i>. If the
   * conversion fails, or if there is a mismatch between the static and
   * dynamic types, an <code>XQException</code> is raised either by this
   * method, or during query evaluation.
   *
   * @param varName          the name of the external variable to bind to,
   *                         cannot be <code>null</code>
   * @param value            the value to be converted, cannot be 
   *                         <code>null</code>
   * @param type             the type of the value to be bound to the
   *                         external variable. The default type,
   *                         <code>xs:string</code>, is used in case 
   *                         <code>null</code> is specified
   * @throws XQException     if (1) the <code>varName</code> or
   *                         <code>value</code> argument is <code>null</code>,
   *                         (2) the conversion of the value to an XDM
   *                         instance failed, (3) in case of an 
   *                         <code>XQPreparedExpression</code>, the dynamic 
   *                         type of the bound value is not compatible with the 
   *                         static type of the variable, (4) in case of an
   *                         <code>XQPreparedExpression</code>, the variable
   *                         is not defined in the prolog of the expression, 
   *                         or (5) if the expression is in a closed state
   */
  public void bindString(QName varName, String value, XQItemType type)
         throws XQException;

 /**
   * Binds a value to the given external variable or the context item. 
   *
   * <br>
   * <br>
   *
   * If the value represents a well-formed XML document, it will be parsed
   * and results in a document node. 
   * The kind of the input type must be <code>null</code>, 
   * <code>XQITEMKIND_DOCUMENT_ELEMENT</code>, or
   * <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>.
   *
   * <br>
   * <br>
   *
   * The value is converted into an instance of the specified type according
   * to  the rules defined in <i>14.3 Mapping a Java XML document to an
   * XQuery document node, XQuery API for Java (XQJ) 1.0</i>.
   *
   * <br>
   * <br>
   *
   * If the conversion fails, or if there is a mismatch between the static
   * and dynamic types, an <code>XQException</code> is raised either by this
   * method, or during query evaluation. If the value is not well formed,
   * or if a kind of the input type other than the values list above is
   * specified, behavior is implementation defined and may raise an exception. 
   *
   * @param varName              the name of the external variable to bind to,
   *                             cannot be <code>null</code>
   * @param value                the value to be converted, cannot be 
   *                             <code>null</code>
   * @param baseURI              an optional base URI, can be <code>null</code>. It can
   *                             be used, for example, to resolve relative URIs and to
   *                             include in error messages.
   * @param type                 the type of the value for the created
   *                             document node. If <code>null</code> is specified,
   *                             it behaves as if
   *                             <code>XQDataFactory.createDocumentElementType(
   *                             XQDataFactory.createElementType(null, 
   *                             XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                             as the type parameter.  That is, the type represents the
   *                             XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   *
   * @exception XQException      if (1) the <code>varName</code> or <code>value</code>
   *                             argument is <code>null</code>, (2) the
   *                             conversion of the value to an XDM instance failed, 
   *                             (3) in case of an <code>XQPreparedExpression</code>,
   *                             the dynamic type of the bound value is not compatible 
   *                             with the static type of the variable, (4) in case of an
   *                             <code>XQPreparedExpression</code>, the variable is not
   *                             defined in the prolog of the expression,
   *                             or (5) if the expression is in a closed state
   */
  public void bindDocument(QName varName, String value, String baseURI, XQItemType type) throws XQException;


 /**
   * Binds a value to the given external variable or the context item. 
   *
   * <br>
   * <br>
   *
   * If the value represents a well-formed XML document, it will be parsed
   * and results in a document node. 
   * The kind of the input type must be <code>null</code>, 
   * <code>XQITEMKIND_DOCUMENT_ELEMENT</code>, or
   * <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>.
   *
   * <br>
   * <br>
   *
   * The value is converted into an instance of the specified type according
   * to  the rules defined in <i>14.3 Mapping a Java XML document to an
   * XQuery document node, XQuery API for Java (XQJ) 1.0</i>.
   *
   * <br>
   * <br>
   *
   * If the conversion fails, or if there is a mismatch between the static
   * and dynamic types, an <code>XQException</code> is raised either by this
   * method, or during query evaluation. If the value is not well formed,
   * or if a kind of the input type other than the values list above is
   * specified, behavior is implementation defined and may raise an exception. 
   *
   * @param varName              the name of the external variable to bind to,
   *                             cannot be <code>null</code>
   * @param value                the value to be converted, cannot be 
   *                             <code>null</code>
   * @param baseURI              an optional base URI, can be <code>null</code>. It can
   *                             be used, for example, to resolve relative URIs and to
   *                             include in error messages.
   * @param type                 the type of the value for the created
   *                             document node. If <code>null</code> is specified,
   *                             it behaves as if
   *                             <code>XQDataFactory.createDocumentElementType(
   *                             XQDataFactory.createElementType(null, 
   *                             XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                             as the type parameter.  That is, the type represents the
   *                             XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   * @exception XQException      if (1) the <code>varName</code> or <code>value</code>
   *                             argument is <code>null</code>, (2) the
   *                             conversion of the value to an XDM instance failed, 
   *                             (3) in case of an <code>XQPreparedExpression</code>,
   *                             the dynamic type of the bound value is not compatible 
   *                             with the static type of the variable, (4) in case of an
   *                             <code>XQPreparedExpression</code>, the variable is not
   *                             defined in the prolog of the expression,
   *                             or (5) if the expression is in a closed state
   */
  public void bindDocument(QName varName, Reader value, String baseURI, XQItemType type) throws XQException;

 /**
   * Binds a value to the given external variable or the context item. 
   *
   * <br>
   * <br>
   *
   * If the value represents a well-formed XML document, it will be parsed
   * and results in a document node. 
   * The kind of the input type must be <code>null</code>, 
   * <code>XQITEMKIND_DOCUMENT_ELEMENT</code>, or
   * <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>.
   *
   * <br>
   * <br>
   *
   * The value is converted into an instance of the specified type according
   * to  the rules defined in <i>14.3 Mapping a Java XML document to an
   * XQuery document node, XQuery API for Java (XQJ) 1.0</i>.
   *
   * <br>
   * <br>
   *
   * If the conversion fails, or if there is a mismatch between the static
   * and dynamic types, an <code>XQException</code> is raised either by this
   * method, or during query evaluation. If the value is not well formed,
   * or if a kind of the input type other than the values list above is
   * specified, behavior is implementation defined and may raise an exception. 
   *
   * @param varName              the name of the external variable to bind to,
   *                             cannot be <code>null</code>
   * @param value                the value to be converted, cannot be 
   *                             <code>null</code>
   * @param baseURI              an optional base URI, can be <code>null</code>. It can
   *                             be used, for example, to resolve relative URIs and to
   *                             include in error messages.
   * @param type                 the type of the value for the created
   *                             document node. If <code>null</code> is specified,
   *                             it behaves as if
   *                             <code>XQDataFactory.createDocumentElementType(
   *                             XQDataFactory.createElementType(null, 
   *                             XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                             as the type parameter.  That is, the type represents the
   *                             XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   * @exception XQException      if (1) the <code>varName</code> or <code>value</code>
   *                             argument is <code>null</code>, (2) the
   *                             conversion of the value to an XDM instance failed, 
   *                             (3) in case of an <code>XQPreparedExpression</code>,
   *                             the dynamic type of the bound value is not compatible 
   *                             with the static type of the variable, (4) in case of an
   *                             <code>XQPreparedExpression</code>, the variable is not
   *                             defined in the prolog of the expression,
   *                             or (5) if the expression is in a closed state
   */
  public void bindDocument(QName varName, InputStream value, String baseURI, XQItemType type) throws XQException;

 /**
  * Binds a value to the given external variable or the context item.
  *
  * <br>
  * <br>
  *
  * If the value represents a well-formed XML document, it results in a
  * document node.  The kind of the input type must be <code>null</code>,
  * <code>XQITEMKIND_DOCUMENT_ELEMENT</code> or <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>.
  *
  * <br>
  * <br>
  *
  * The value is converted into an instance of the specified type according
  * to  the rules defined in <i>14.3 Mapping a Java XML document to an
  * XQuery document node, XQuery API for Java (XQJ) 1.0</i>.
  *
  * <br>
  * <br>
  *
  * If the value is not well formed, or if a kind of the input type other
  * than the values list above is specified, behavior is implementation
  * defined and may raise an exception. If the conversion
  * fails, or if there is a mismatch between the static and dynamic types,
  * an <code>XQException</code> is raised either by this method, or during
  * query evaluation.
  *
  * @param varName              the name of the external variable to bind to, cannot
  *                             be <code>null</code>
  * @param value                the value to be converted, cannot be <code>null</code>
  * @param type                 the type of the value for the created
  *                             document node. If <code>null</code> is specified,
  *                             it behaves as if
  *                             <code>XQDataFactory.createDocumentElementType(
  *                             XQDataFactory.createElementType(null, 
  *                             XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
  *                             as the type parameter.  That is, the type represents the
  *                             XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
  * @exception XQException      if (1) the <code>varName</code> or <code>value</code>
  *                             argument is <code>null</code>, (2) the
  *                             conversion of the value to an XDM instance failed, 
  *                             (3) in case of an <code>XQPreparedExpression</code>,
  *                             the dynamic type of the bound value is not compatible 
  *                             with the static type of the variable, (4) in case of an
  *                             <code>XQPreparedExpression</code>, the variable is not
  *                             defined in the prolog of the expression,
  *                             or (5) if the expression is in a closed state
  */
  public void bindDocument(QName varName, javax.xml.stream.XMLStreamReader value, XQItemType type)
              throws XQException;

 /**
   * Binds a value to the given external variable or the context item
   * from the given <code>Source</code>. An XQJ
   * implementation must at least support the following implementations:
   * <ul>
   *   <li><code>javax.xml.transform.dom.DOMSource</code></li>
   *   <li><code>javax.xml.transform.sax.SAXSource</code></li>
   *   <li><code>javax.xml.transform.stream.StreamSource</code></li>
   * </ul>
   *
   * <br>
   * <br>
   *
   * If the value represents a well-formed XML document, it will result in a
   * document node. The kind of the input type must be <code>null</code>,
   * <code>XQITEMKIND_DOCUMENT_ELEMENT</code>, or
   * <code>XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code>.
   *
   * <br>
   * <br>
   *
   * The value is converted into an instance of the specified type according
   * to  the rules defined in <i>14.3 Mapping a Java XML document to an
   * XQuery document node, XQuery API for Java (XQJ) 1.0</i>.
   *
   * <br>
   * <br>
   *
   * If the value is not well formed, or if a kind of the input type other
   * than the values list above is specified, behavior is implementation
   * defined and may raise an exception. If the conversion fails, or if there 
   * is a mismatch between the static and dynamic types, an <code>XQException</code> 
   * is raised either by this method, or during query evaluation.
   *
   *  @param varName              the name of the external variable to bind to, cannot
   *                              be <code>null</code>
   *  @param value                the value to be converted, cannot be <code>null</code>
   *  @param type                 the type of the value for the created
   *                              document node. If <code>null</code> is specified,
   *                              it behaves as if
   *                              <code>XQDataFactory.createDocumentElementType(
   *                              XQDataFactory.createElementType(null, 
   *                              XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                              as the type parameter.  That is, the type represents the
   *                              XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   *  @exception XQException      if (1) the <code>varName</code> or <code>value</code>
   *                              argument is <code>null</code>, (2) the
   *                              conversion of the value to an XDM instance failed, 
   *                              (3) in case of an <code>XQPreparedExpression</code>,
   *                              the dynamic type of the bound value is not compatible 
   *                              with the static type of the variable, (4) in case of an
   *                              <code>XQPreparedExpression</code>, the variable is not
   *                              defined in the prolog of the expression,
   *                              or (5) if the expression is in a closed state
   */
  public void bindDocument(javax.xml.namespace.QName varName, javax.xml.transform.Source value, XQItemType type)
                  throws XQException;

 /**
  * Sets the implicit timezone
  *
  * @param implicitTimeZone     time zone to be set
  *
  * @exception XQException      if the expression is in a closed state
  */
  public void setImplicitTimeZone(java.util.TimeZone implicitTimeZone)
       throws XQException;

  /**
   * Binds a value to the given external variable. The dynamic type of the
   * value is derived from the <code>XQItem</code>. In case of a mismatch
   * between the static and dynamic types, an <code>XQException</code> is
   * raised either by this method, or during query evaluation.
   * 
   * @param varName             the name of the external variable to bind to, 
   *                            cannot be <code>null</code>
   *
   * @param value               the value to be bound, cannot be <code>null</code>
   *
   * @exception XQException     if (1) any of the arguments are <code>null</code>,
   *                            (2) in case of an <code>XQPreparedExpression</code>,
   *                            the dynamic type of the bound value is not compatible
   *                            with the static type of the variable,
   *                            (3) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the
   *                            expression, (4) the expression is in a closed state,
   *                            or (5) the specified item is closed
   */
  public void bindItem(QName varName, XQItem value) throws XQException;

  /**
   * Binds a value to the given external variable or the context item. The input sequence
   * is consumed from its current position to the end, after which the input sequence's
   * position will be set to point after the last item. The dynamic type of the value is
   * derived from the items in the sequence. In case of a mismatch between the static
   * and dynamic types, an <code>XQException</code> is be raised either by this method, or
   * during query evaluation.
   * 
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be bound, cannot be <code>null</code>
   *
   * @exception XQException     if (1) any of the arguments are <code>null</code>, 
   *                            (2) in case of an <code>XQPreparedExpression</code>,
   *                            the dynamic type of the bound value is not compatible
   *                            with the static type of the variable,
   *                            (3) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of
   *                            the expression, (4) the expression is in a closed
   *                            state, or (5) the specified sequence is closed
   */
  public void bindSequence(QName varName, XQSequence value) throws XQException;

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or during
   * query evaluation.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted, cannot be <code>null</code>
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> or <code>value</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindObject(QName varName, Object value, XQItemType type) throws XQException;

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or during
   * query evaluation.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindBoolean(QName varName, boolean value, XQItemType type) throws XQException; 

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or 
   * during query evaluation.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindByte(QName varName, byte value, XQItemType type) throws XQException;

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or
   * during query evaluations.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindDouble(QName varName, double value, XQItemType type) throws XQException; 

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or
   * during query evaluations.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindFloat(QName varName, float value, XQItemType type) throws XQException; 

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or 
   * during query evaluations.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindInt(QName varName, int value, XQItemType type) throws XQException; 

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or 
   * during query evaluation.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindLong(QName varName, long value, XQItemType type) throws XQException;

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or
   * during query evaluation.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted, cannot be <code>null</code>
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> or <code>value</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindNode (QName varName, Node value, XQItemType type) throws XQException;

  /**
   * Binds a value to the given external variable or the context item. The value is
   * converted into an instance of the specified type according to the rule defined in 
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the conversion fails, or if
   * there is a mismatch between the static and dynamic types, an
   * <code>XQException</code> is raised either by this method, or 
   * during query evaluation.
   *
   *
   * @param varName             the name of the external variable to bind to, cannot be
   *                            <code>null</code>
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to the external variable.
   *                            The default type of the value is used in case <code>null</code>
   *                            is specified
   *
   * @exception XQException     if (1) the <code>varName</code> argument
   *                            is <code>null</code>, (2) the conversion of the value to an 
   *                            XDM instance failed, (3) in case of an
   *                            <code>XQPreparedExpression</code>, the dynamic type of the bound
   *                            value is not compatible with the static type of the variable,
   *                            (4) in case of an <code>XQPreparedExpression</code>,
   *                            the variable is not defined in the prolog of the expression,
   *                            or (5) if the expression is in a closed state
   */
  public void bindShort(QName varName, short value, XQItemType type) throws XQException;

};
