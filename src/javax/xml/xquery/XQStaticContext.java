/*
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/** 
 * An <code>XQStaticContext</code> represents default values for various
 * <a href="http://www.w3.org/TR/xquery/#id-xq-static-context-components">
 * <i>XQuery Static Context Components</i></a>. Further it includes the
 * static XQJ properties for an <code>XQExpression</code> or
 * <code>XQPreparedExpression</code> object.
 * <p>
 * The following XQuery Static Context Components are supported through the
 * <code>XQStaticContext</code> interface:
 * <ul>
 * <li>Statically known namespaces
 * <li>Default element/type namespace
 * <li>Default function namespace
 * <li>Context item static type
 * <li>Default collation
 * <li>Construction mode
 * <li>Ordering mode
 * <li>Default order for empty sequences
 * <li>Boundary-space policy
 * <li>Copy-namespaces mode
 * <li>Base URI
 * </ul>
 * As described in the XQuery specification, each of these default values can
 * be overridden or augmented in the query prolog.<br>
 * <p>
 * In addition <code>XQStaticContext</code> includes the static XQJ properties
 * for an <code>XQExpression</code> or <code>XQPreparedExpression</code> object:
 * <ul>
 * <li>Binding mode
 * <li>Holdability of the result sequences
 * <li>Scrollability of the result sequences
 * <li>Query language
 * <li>Query timeout
 * </ul>
 * <p>
 * Note that <code>XQStaticContext</code> is a value object, changing attributes in
 * such object doesn't affect any existing <code>XQExpression</code> or
 * <code>XQPreparedExpression</code> object.<br>
 * In order to take effect, the application needs to explicitly change the <code>XQConnection</code>
 * default values, or specify an <code>XQStaticContext</code> object when creating an
 * <code>XQExpression</code> or <code>XQPreparedExpression</code>.
 * <pre>
 *  XQConnection conn = XQDatasource.getConnection();
 *  // get the default values from the implementation
 *  XQStaticContext cntxt = conn.getStaticContext();
 *  // change the base uri
 *  cntxt.setBaseURI("http://www.foo.com/xml/");
 *  // change the implementation defaults
 *   conn.setStaticContext(cntxt);
 * 
 *  // create an XQExpression using the new defaults
 *  XQExpression expr1 = conn.createExpression();
 * 
 *  // creat an XQExpression, using BaseURI "file:///root/user/john/"
 *  cntxt.setBaseURI("file:///root/user/john/");
 *  XQExpression expr2 = conn.createExpression(cntxt);
 *  ... 
 * </pre> 
 */
public interface XQStaticContext 
{
  /**  
   * Returns the prefixes of all the statically known namespaces.
   * Use the <code>getNamespaceURI</code> method to look up the namespace URI 
   * corresponding to a specific prefix.
   *
   * @return                    <code>String</code> array containing the namespace prefixes.
   *                            Cannot be <code>null</code>
   */
  public String[] getNamespacePrefixes(); 

  /** 
   * Retrieves the namespace URI associated with a prefix. An <code>XQException</code>
   * is thrown if an unknown prefix is specified, i.e. a prefix not returned by the
   * <code>getInScopeNamespacePrefixes</code> method.
   * 
   * @param prefix              the prefix for which the namespace URI is sought. Cannot 
   *                            be <code>null</code>
   * @return                    the namespace URI. Cannot be <code>null</code>
   * @exception XQException     if a <code>null</code> prefix is specified or if the prefix
   *                            is unknown
   */
  public String getNamespaceURI(String prefix) throws XQException;

  /**
   * Declares a namespace prefix and associates it with a namespace URI. If the namespace URI is
   * the empty string, the prefix is removed from the in-scope namespace definitions.
   * 
   * @param prefix              the prefix for the namespace URI
   * @param uri                 the namespace URI. An empty string
   *                            undeclares the specific prefix. Cannot be <code>null</code>
   * @throws XQException        if (1) a <code>null</code> prefix, or (2) a <code>null</code> namespace
   *                            URI is specified
   */
  public void declareNamespace(String prefix, String uri) throws XQException;

  /** 
   * Gets the URI of the default element/type namespace, the empty string
   * if not set.
   *
   * @return                    the URI of the default element/type namespace,
   *                            if set, else the empty string. Cannot be <code>null</code>
   */
  public String getDefaultElementTypeNamespace();
 
  /** 
   * Sets the URI of the default element/type namespace, the empty string
   * to make it unspecified.
   *
   * @param uri                 the namespace URI of the default element/type namespace,
   *                            the empty string to make it unspecified. 
   *                            Cannot be <code>null</code>.
   * @exception XQException     if a <code>null</code> uri is specified
   */
  public void setDefaultElementTypeNamespace(String uri) throws XQException;

  /** 
   * Gets the URI of the default function namespace, the empty string 
   * if not set.
   *
   * @return                    the URI of the default function namespace,
   *                            if set, else the empty string. Cannot be <code>null</code>
   */
  public String getDefaultFunctionNamespace();
  
  /** 
   * Sets the URI of the default function namespace, the empty string
   * to make it unspecified.
   *
   * @param uri                 the namespace URI of the default function namespace,
   *                            the empty string to make it unspecified. 
   *                            Cannot be <code>null</code>.
   * @exception XQException     if a <code>null</code> URI is specified
   */
  public void setDefaultFunctionNamespace(String uri) throws XQException; 

  /** 
   * Gets the static type of the context item. <code>null</code> if unspecified.
   *
   * @return                    the static type of the context item,
   *                            if set, else <code>null</code>
   */
  public XQItemType getContextItemStaticType();
   
  /** 
   * Sets the static type of the context item, specify <code>null</code>
   * to make it unspecified.
   *
   * @param contextItemType     the static type of the context item; 
   *                            <code>null</code> if unspecified.
   * @exception XQException     if the <code>contextItemType</code> is not
   *                            a valid <code>XQItemType</code>
   */
  public void setContextItemStaticType(XQItemType contextItemType) 
     throws XQException;
  
  /** 
    * Gets the URI of the default collation.
    *
    * @return                    the URI of the default collation.
    *                            Cannot be <code>null</code>.
    */
  public String getDefaultCollation();   
  
  /** 
   * Sets the URI of the default collation.
   *
   * @param uri                 the namespace URI of the default collation. 
   *                            Cannot be <code>null</code>.
   * @exception XQException     if a <code>null</code> URI is specified
   */
  public void setDefaultCollation(String uri) throws XQException;   

  /** 
    * Gets the construction mode defined in the static context.
    *
    * @return                    construction mode value. One of: 
    *                            <code>XQConstants.CONSTRUCTION_MODE_PRESERVE</code>,
    *                            <code>XQConstants.CONSTRUCTION_MODE_STRIP</code>
    */
  public int getConstructionMode();
  
  /**
   * Sets the construction mode in the static context.
   * 
   * @param mode                 construction mode value. One of: 
   *                             <code>XQConstants.CONSTRUCTION_MODE_PRESERVE</code>,
   *                             <code>XQConstants.CONSTRUCTION_MODE_STRIP</code>.
   * @throws XQException         the specified mode is different from
   *                             <code>XQConstants.CONSTRUCTION_MODE_PRESERVE</code>,
   *                             <code>XQConstants.CONSTRUCTION_MODE_STRIP</code> 
   */
  public void setConstructionMode(int mode) throws XQException;

  /** 
   * Gets the ordering mode defined in the static context.
   *
   * @return                    ordering mode value. One of: 
   *                            <code>XQConstants.ORDERING_MODE_ORDERED</code>,
   *                            <code>XQConstants.ORDERING_MODE_UNORDERED</code>.
   */
  public int getOrderingMode();

  /**
   * Sets the ordering mode in the static context.
   * 
   * @param mode                 ordering mode value. One of: 
   *                             <code>XQConstants.ORDERING_MODE_ORDERED</code>,
   *                             <code>XQConstants.ORDERING_MODE_UNORDERED</code>.
   * @throws XQException         the specified mode is different from
   *                             <code>XQConstants.ORDERING_MODE_ORDERED</code>,
   *                             <code>XQConstants.ORDERING_MODE_UNORDERED</code> 
   */
  public void setOrderingMode(int mode) throws XQException;

  /** 
    * Gets the default order for empty sequences defined in the static context.
    *
    * @return                    default order for empty sequences value. One of: 
    *                            <code>XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST</code>,
    *                            <code>XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST</code>.
    */
  public int getDefaultOrderForEmptySequences();
  
  /**
   * Sets the default order for empty sequences in the static context.
   * 
   * @param order                the default order for empty sequences. One of: 
   *                             <code>XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST</code>,
   *                             <code>XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST</code>.
   * @throws XQException         the specified order for empty sequences is different from
   *                             <code>XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST</code>,
   *                             <code>XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST</code> 
   */
  public void setDefaultOrderForEmptySequences(int order) throws XQException;

  /** 
    * Gets the boundary-space policy defined in the static context.
    *
    * @return                    the boundary-space policy value. One of: 
    *                            <code>XQConstants.BOUNDARY_SPACE_PRESERVE</code>,
    *                            <code>XQConstants.BOUNDARY_SPACE_STRIP</code>.
    */
  public int getBoundarySpacePolicy();

  /**
   * Sets the boundary-space policy in the static context.
   * 
   * @param policy               boundary space policy. One of: 
   *                             <code>XQConstants.BOUNDARY_SPACE_PRESERVE</code>,
   *                             <code>XQConstants.BOUNDARY_SPACE_STRIP</code>.
   * @throws XQException         the specified mode is different from
   *                             <code>XQConstants.BOUNDARY_SPACE_PRESERVE</code>,
   *                             <code>XQConstants.BOUNDARY_SPACE_STRIP</code> 
   */
  public void setBoundarySpacePolicy(int policy) throws XQException; 
  
  /** 
   * Gets the preserve part of the copy-namespaces mode
   * defined in the static context.
   *
   * @return                    construction mode value. One of: 
   *                            <code>XQConstants.COPY_NAMESPACES_MODE_PRESERVE</code>,
   *                            <code>XQConstants.COPY_NAMESPACES_MODE_NO_PRESERVE</code>.
   */
 public int getCopyNamespacesModePreserve();

 /**
  * Sets the preserve part of the copy-namespaces mode in the static context.
  * 
  * @param mode                 ordering mode value. One of: 
  *                             <code>XQConstants.COPY_NAMESPACES_MODE_PRESERVE</code>,
  *                             <code>XQConstants.COPY_NAMESPACES_MODE_NO_PRESERVE</code>.
  * @throws XQException         the specified mode is different from
  *                             <code>XQConstants.COPY_NAMESPACES_MODE_PRESERVE</code>,
  *                             <code>XQConstants.COPY_NAMESPACES_MODE_NO_PRESERVE</code> 
  */
 public void setCopyNamespacesModePreserve(int mode) throws XQException;
 
 /** 
   * Gets the inherit part of the copy-namespaces mode
   * defined in the static context.
   *
   * @return                    construction mode value. One of:
   *                            <code>XQConstants.COPY_NAMESPACES_MODE_INHERIT</code>,
   *                            <code>XQConstants.COPY_NAMESPACES_MODE_NO_INHERIT</code>.
   */
 public int getCopyNamespacesModeInherit();  
 
 /**
  * Sets the inherit part of the copy-namespaces mode in the static context.
  * 
  * @param mode                 ordering mode value. One of: 
  *                             <code>XQConstants.COPY_NAMESPACES_MODE_INHERIT</code>,
  *                             <code>XQConstants.COPY_NAMESPACES_MODE_NO_INHERIT</code>.
  * @throws XQException         the specified mode is different from
  *                             <code>XQConstants.COPY_NAMESPACES_MODE_INHERIT</code>,
  *                             <code>XQConstants.COPY_NAMESPACES_MODE_NO_INHERIT</code> 
  */
 public void setCopyNamespacesModeInherit(int mode) throws XQException;
  
  /** 
    * Gets the Base URI, if set in the static context, else the empty string.
    *
    * @return                    the base URI, if set, else the empty string.
    *                            Cannot be <code>null</code>..
    */
  public String getBaseURI();

  /** 
   * Sets the Base URI in the static context, specify the empty string to make it undefined.
   *
   * @param baseUri             the new baseUri, or empty string to make it undefined.
   *                            Cannot be <code>null</code>.
   * @exception XQException     if a <code>null</code> base uri is specified
   */
  public void setBaseURI(String baseUri) throws XQException;

 /**
  * Gets the value of the binding mode property.
  * By default an XQJ implementation operates in immediate binding mode.
  *
  * @return                     the binding mode. One of 
  *                             <code>XQConstants.BINDING_MODE_IMMEDIATE</code>,
  *                             or<code>XQConstants.BINDING_MODE_DEFERRED</code>.
  */
  public int getBindingMode();
 
 /**
  * Sets the binding mode property.
  * By default an XQJ implementation operates in immediate binding mode.
  *
  * @param bindingMode          the binding mode. One of:
  *                             <code>XQConstants.BINDING_MODE_IMMEDIATE</code>,
  *                             or<code>XQConstants.BINDING_MODE_DEFERRED</code>.
  * @throws XQException         the specified mode is different from
  *                             <code>XQConstants.BINDING_MODE_IMMEDIATE</code>,
  *                             <code>XQConstants.BINDING_MODE_DEFERRED</code> 
  */
  public void setBindingMode(int bindingMode) throws XQException;

  /**
   * Gets the value of the holdability property.
   *
   * @return                    the type of a result's holdability. One of: 
   *                            <code>XQConstants.HOLDTYPE_HOLD_CURSORS_OVER_COMMIT</code>,
   *                            or <code>XQConstants.HOLDTYPE_CLOSE_CURSORS_AT_COMMIT</code>.
   */
  public int getHoldability();

  /** 
   * Sets the holdability property. 
   *
   * @param holdability         the holdability of the result. One of: 
   *                            <code>XQConstants.HOLDTYPE_HOLD_CURSORS_OVER_COMMIT</code>,
   *                            or <code>XQConstants.HOLDTYPE_CLOSE_CURSORS_AT_COMMIT</code>.
   * @exception XQException     the specified holdability is different from
   *                            <code>XQConstants.HOLDTYPE_HOLD_CURSORS_OVER_COMMIT</code>,
   *                            <code>XQConstants.HOLDTYPE_CLOSE_CURSORS_AT_COMMIT</code>  
   */
  public void setHoldability(int holdability) throws XQException;

  /**
   * Gets the value of the language type and version property.
   * By default an XQJ implementation's default is <code>XQConstants.LANGTYPE_XQUERY</code>.
   *
   * @return                    the language type and version. One of: 
   *                            <code>XQConstants.LANGTYPE_XQUERY</code>,
   *                            or <code>XQConstants.LANGTYPE_XQUERYX</code>
   *                            or a negative value indicating a vendor specific
   *                            query language type and version.
   */
  public int getQueryLanguageTypeAndVersion();
  
  /** 
   * Sets the input query language type and version. 
   * When this is set to a particular language type and version, then the 
   * query is assumed to be in that language and version.
   * 
   * @param langType            the query language type and version of the
   *                            inputs. One of: <code>XQConstants.LANGTYPE_XQUERY</code>
   *                            (default), or <code>XQConstants.LANGTYPE_XQUERYX</code>.
   *                            A negative number indicates a vendor specific 
   *                            query language type and version.
   * @exception XQException     the specified langtype is different from
   *                            <code>XQConstants.LANGTYPE_XQUERY</code>,
   *                            <code>XQConstants.LANGTYPE_XQUERYX</code> and is not negative  
   */
  public void setQueryLanguageTypeAndVersion(int langType) throws XQException;

  /**
   * Gets the value of the scrollability property.
   * By default query results are forward only.
   *
   * @return                     the type of a result's scrollability. One of: 
   *                            <code>XQConstants.SCROLLTYPE_FORWARD_ONLY</code>, or
   *                            <code>XQConstants.SCROLLTYPE_SCROLLABLE</code>.
   */
  public int getScrollability();
  
  /** 
   * Sets the scrollability of the result sequence.
   * By default query results are forward only.
   *
   * @param scrollability       the scrollability of the result. One of: 
   *                            <code>XQConstants.SCROLLTYPE_FORWARD_ONLY</code>, or
   *                            <code>XQConstants.SCROLLTYPE_SCROLLABLE</code>.
   * @exception XQException     the specified crollability type is different from
   *                            <code>XQConstants.SCROLLTYPE_FORWARD_ONLY</code>,
   *                            <code>XQConstants.SCROLLTYPE_SCROLLABLE</code>  
   */
  public void setScrollability(int scrollability) throws XQException;
 
  /**
   * Retrieves the number of seconds an implementation will wait for a 
   * query to execute.
   *
   * @return                    the query execution timeout value in seconds. 
   *                            A value of 0 indicates no limit.
   */
  public int getQueryTimeout();

  /**
   * Sets the number of seconds an implementation will wait for a 
   * query to execute. If the implementation does not support query timeout
   * it can ignore the specified timeout value.
   * It the limit is exceeded, the behavor of the query is the same as an
   * execution of a cancel by another thread.
   *
   * @param seconds             the query execution timeout value in seconds.
   *                            A value of 0 indicates no limit
   * @exception XQException     if the passed in value is negative
   */
  public void setQueryTimeout(int seconds) throws XQException;
  
}
