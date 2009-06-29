/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/** 
 * This interface represents a common interface for accessing the values of
 * an XQuery item. 
 * All the get functions raise an exception if the underlying sequence object 
 * is not positioned on an item (e.g. if the sequence is positioned before
 * the first item or after the last item). 
 * <br>
 * <br>
 * Example -
 * <pre>
 * 
 *  XQPreparedExpression expr = conn.prepareExpression("for $i ..");
 *  XQSequence result = expr.executeQuery();
 *
 *  // create the ItemTypes for string and integer
 *  XQItemType strType = conn.createAtomicType(XQItemType.XQBASETYPE_STRING);
 *  XQItemType intType = conn.createAtomicType(XQItemType.XQBASETYPE_INTEGER);
 * 
 *  // posititioned before the first item
 *  while (result.next())
 *  {
 *    // If string or any of its subtypes, then get the string value out
 * 
 *    if (result.instanceOf(strType))
 *      String str = result.getAtomicValue();
 *    else if (result.instanceOf(intType))
 *       // if it is exactly an int
 *      int intval = result.getInt();
 *      ...
 * 
 *    // Alternatively, you can get the exact type out.
 *    XQItemType type = result.getItemType();
 * 
 *    // Now perform the comparison..
 *    if (type.equals(intType))
 *    { ... };
 *  
 *  }
 * </pre>
 *
 * See also: 
 * <ul>
 * <li> <i>Table 6 - XQuery Atomic Types and Corresponding Java Object Types,
 *  XQuery API for Java (XQJ) 1.0</i>, for mapping of XQuery atomic
 * types to Java object types. For example, if the XQuery value returned is 
 * of type <code>xs:unsignedByte</code>, then calling the <code>getObject()</code> method 
 * will return a Java object of type <code>java.lang.Short</code>.
 * </li>
 * <li> <i>Table 7 - XQuery Node Types and Corresponding Java Object Types
 *  XQuery API for Java (XQJ) 1.0</i>, for the mapping of XQuery node types
 * to the corresponding Java object types. For example, if the XQuery value
 * returned is an element node, then calling the <code>getObject()</code> or 
 * <code>getNode()</code> method will return a Java object of type 
 * <code>org.w3.dom.Element</code>.
 * </li>
 * </ul>
 * 
 * <p/>
 */
public interface XQItemAccessor 
{

  /**
   * Gets the current item as a <code>boolean</code>. 
   * The current item must be an atomic value of type <code>xs:boolean</code>
   * or a subtype.
   * 
   * @return                    a <code>boolean</code> representing the current item
   * @exception XQException     if (1) the conversion of the current item to a
   *                            <code>boolean</code> fails, (2) if there are
   *                            errors accessing the current item, (3) if the
   *                            underlying sequence or item is in a closed state,
   *                            or (4) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public boolean getBoolean() throws XQException;
 
  /**
   * Gets the current item as a <code>byte</code>.
   * The current item must be an atomic value of type <code>xs:decimal</code>
   * or a subtype, and its value must be in the value space of <code>byte</code>. 
   *
   * @return                    a <code>byte</code> representing the current item
   * @exception XQException     if (1) the conversion of the current item to a
   *                            <code>byte</code> fails, (2) if there are
   *                            errors accessing the current item, (3) if the
   *                            underlying sequence or item is in a closed state,
   *                            or (4) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public byte getByte() throws XQException;

  /**
   * Gets the current item as a <code>double</code>.
   * The current item must be an atomic value of type <code>xs:double</code>
   * or a subtype.
   *
   * @return                    a <code>double</code> representing the current item
   * @exception XQException     if (1) the conversion of the current item to a
   *                            <code>double</code> fails, (2) if there are
   *                            errors accessing the current item, (3) if the
   *                            underlying sequence or item is in a closed state,
   *                            or (4) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public double getDouble() throws XQException; 

  /** 
   * Gets the current item as a <code>float</code>.
   * The current item must be an atomic value of type <code>xs:float</code>
   * or a subtype. 
   *
   * @return                    a <code>float</code> representing the current item
   * @exception XQException     if (1) the conversion of the current item to a
   *                            <code>float</code> fails, (2) if there are
   *                            errors accessing the current item, (3) if the
   *                            underlying sequence or item is in a closed state,
   *                            or (4) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public float getFloat() throws XQException;

  /**
   * Gets the current item as an <code>int</code>.
   * The current item must be an atomic value of type <code>xs:decimal</code>
   * or a subtype, and its value must be in the value space of <code>int</code>.
   *
   * @return                    an <code>int</code> representing the current item
   * @exception XQException     if (1) the conversion of the current item to a
   *                            <code>int</code> fails, (2) if there are
   *                            errors accessing the current item, (3) if the
   *                            underlying sequence or item is in a closed state,
   *                            or (4) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public int getInt() throws XQException;
 
  /**
   * Gets the type of the item.
   * <br>
   *
   * On a forward only sequence this method can be called independent of any
   * other get or write method. It will not raise an error if such method has
   * been called already, nor will it affect subsequent invocations of any
   * other get or write method.
   *
   * @return                    the type of the item
   * @exception XQException     if (1) there are errors accessing the type of the item,
   *                            or (2) the underlying sequence or item is in a closed state
   */
  public XQItemType getItemType() throws XQException;

  /** 
   * Gets the current item as a Java <code>String</code>. The current item
   * must be an atomic value. This function casts the current item to an
   * <code>xs:string</code> value according to the casting rules defined in 
   * <a href="http://www.w3.org/TR/xpath-functions/#casting-from-strings">
   * <i>17.1.2 Casting to xs:string and xs:untypedAtomic, XQuery 1.0 and
   * XPath 2.0 Functions and Operators</i></a>,
   * and then returns the value as a Java <code>String</code>.
   *
   * @return                    the string representation of the item 
   * @exception XQException     if (1) there are errors accessing the item's value,
   *                            (2) the item is not an atomic value,
   *                            (3) there is an error when casting the
   *                            item to a string representation,
   *                            (4) the underlying sequence or item is in a
   *                            closed state, or (5) in the case of forward only
   *                            sequences, a get or write method was already
   *                            invoked on the current item
   */
   public String getAtomicValue() throws XQException;

  /**
   * Gets the current item as a <code>long</code>.
   * The current item must be an atomic value of type <code>xs:decimal</code>
   * or a subtype, and its value must be in the value space of <code>long</code>.
   *
   * @return                    a <code>long</code> representing the current item
   * @exception XQException     if (1) the conversion of the current item to a
   *                            <code>long</code> fails, (2) if there are
   *                            errors accessing the current item, (3) if the
   *                            underlying sequence or item is in a closed state,
   *                            or (4) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public long getLong() throws XQException;


  /**
   * Gets the item as a DOM node. The current item must be a node.
   * The type of the returned DOM node is governed by <i>Table 7 -
   * XQuery Node Types and Corresponding Java Object Types  XQuery
   * API for Java (XQJ) 1.0</i>
   *
   * The instance of the returned node is implementation dependent. The node
   * may be a reference or a copy of the internal state of the item. It is
   * advisable to make a copy of the node if the application plans to modify
   * it. 
   * 
   * @return                    a DOM node representing the current item
   * @exception XQException     if (1) if there are errors accessing the current item,
   *                            (2) the current item is not a node, (3) if the
   *                            underlying sequence or item is in a closed state,
   *                            or (4) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public org.w3c.dom.Node getNode() throws XQException;

  /**
   * Returns the URI for this item. If the item is a document node, then this
   * method returns the absolute URI of the resource from which the document
   * node was constructed. If the document URI is not available, then the
   * empty string is returned. If the document URI is available, the returned
   * value is the same as if <code>fn:document-uri</code> were evaluated on this document
   * node. If the item is of a node kind other than document node, then the
   * returned URI is implementation-defined.<br>
   *
   * On a forward only sequence this method can be called independent of any
   * other get or write method. It will not raise an error if such method has
   * been called already, nor will it affect subsequent invocations of any
   * other get or write method on the current item.
   *
   * @return                    the document URI for this document node or the empty string if
   *                            not available. For other node kinds, the result is
   *                            implementation-defined
   *
   * @exception XQException     if (1) if there are errors accessing the current item,
   *                            (2) the current item is not a node, (3) if the
   *                            underlying sequence or item is in a closed state
   */
  public java.net.URI getNodeUri() throws XQException;

  /** 
   * Gets the current item as an <code>Object</code>. 
   *
   * The data type of the returned object will be the Java <code>Object</code>
   * type as specified in <i>14.4 Mapping an XQuery Atomic Value to a
   * Java Object Type and 14.5 Mapping an XQuery Node to a Java Object Type,
   * XQuery API for Java (XQJ) 1.0</i>.
   *
   * @return                    an object representing the current item 
   * @exception XQException     if (1) if there are errors accessing the current item,
   *                            (2) if the underlying sequence or item is in a closed state,
   *                            or (3) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public java.lang.Object getObject() throws XQException;
 
  /**
    * Read the current item as an <code>XMLStreamReader</code> object, as described
    * in <i>Section 12.1 Serializing an XDM instance into a StAX event stream
    * (XMLStreamReader), XQuery API for Java (XQJ) 1.0</i>.
    *
    * Note that the serialization process might fail, in which case a 
    * <code>XQException</code> is thrown.
    * 
    * While the stream is being read, the application MUST NOT do any other
    * concurrent operations on the underlying item or sequence.
    * The operation on the stream is undefined if the underlying sequence
    * is repositioned or the state of the underlying item or sequence
    * is changed by concurrent operations.
    * 
    * @return                    an XML reader object as <code>XMLStreamReader</code>
    * @exception XQException     if (1) there are errors accessing the current item
    *                            or the underlying sequence, (2) the underlying sequence
    *                            or item is in a closed state, (3) in the case of a forward
    *                            only sequence, a get or write method has already been
    *                            invoked on the current item, or (4) in case of an error
    *                            during serialization of the current item into a StAX event
    *                            stream as defined in <i>Section 12.1 Serializing an XDM
    *                            instance into a StAX event stream (XMLStreamReader), XQuery
    *                            API for Java (XQJ) 1.0</i>
    */
   public javax.xml.stream.XMLStreamReader getItemAsStream() throws XQException;
  
  /**
   * Serializes the current item  according to the
   * <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>.
   * 
   * Serialization parameters, which influence how serialization is
   * performed, can be specified. Refer to the 
   * <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>
   * and <i>Section 12 Serialization, XQuery
   *  API for Java (XQJ) 1.0</i> for more information.
   * 
   * @param props               specifies the serialization parameters,
   *                            <code>null</code> is considered equivalent to an empty
   *                            <code>Properties</code> object
   * @return                    the serialized representation of the item
   * @exception XQException     if (1) there are errors accessing the current
   *                            item or the underlying sequence, (2) the underlying
   *                            sequence or item is in a closed state,
   *                            (3) in the case of a forward only sequence,
   *                            a get or write method has already been
   *                            invoked on the current item, or (4)
   *                            if there are errors during serialization
   */
   public java.lang.String getItemAsString(java.util.Properties props)
                 throws XQException;

  /**
   * Gets the current item as a <code>short</code>.
   * The current item must be an atomic value of type <code>xs:decimal</code>
   * or a subtype, and its value must be in the value space of <code>short</code>.
   *
   * @return                    a <code>short</code> representing the current item
   * @exception XQException     if (1) the conversion of the current item to a
   *                            <code>short</code> fails, (2) if there are
   *                            errors accessing the current item, (3) if the
   *                            underlying sequence or item is in a closed state,
   *                            or (4) in the case of forward only sequences, a
   *                            get or write method was already invoked on the
   *                            current item
   */
  public short getShort() throws XQException;

  /**
   * Checks if the item "matches" an item type, as defined in
   * <a href="http://www.w3.org/TR/xquery/#id-matching-item"><i>2.5.4.2
   * Matching an Item Type and an Item, XQuery 1.0: An XML Query Language</i></a>.
   *
   * You can use this method to first check the type of the result before 
   * calling the specific get methods. 
   * <br>
   * <br>
   * Example -
   * <pre>
   *  ...
   *  XQItemType strType = conn.createAtomicType(XQItemType.XQBASETYPE_STRING);
   *  XQItemType nodeType = conn.createNodeType();
   *
   *  XQSequence result = preparedExpr.executeQuery();
   *  while (result.next())
   *  {
   *     // Generic check for node.. 
   *     if (result.instanceOf(nodeType))
   *        org.w3.dom.Node node = result.getNode();
   *     else if (result.instanceOf(strType))
   *        String str = result.getAtomicValue();
   *   }
   * </pre>
   * <br>
   * If either the type of the <code>XQItemAccessor</code> or the input 
   * <code>XQItemType</code> is not a built-in type, then this method is 
   * allowed to raise exception if it can NOT determine the instanceOf 
   * relationship due to the lack of the access of
   * the XML schema that defines the user defined schema types if the
   * <code>XQMetaData.isUserDefinedXMLSchemaTypeSupported()</code> method
   * returns <code>false</code>.
   * <br>
   * Otherwise, this method must determine if the type of the
   * <code>XQItemAccessor</code>  is an instance of the input
   * <code>XQItemType</code>. Note even if 
   * <code>isUserDefinedXMLSchemaTypeSupported()</code> returns <code>false</code>,
   * an XQJ implementation may still be able to determine the instanceOf
   * relationship for certain cases involving user defined schema type.
   * For example, if the type of an <code>XQItemAccessor</code> is of
   * <code>mySchema:hatSize</code> sequence type and the input parameter 
   * <code>XQItemType</code> is of <code>item()</code> sequence type, 
   * the return value for instanceOf relationship should always be <code>true</code>
   * even though the XQJ implementation does not know the precise type
   * information of <code>mySchema:hatSize</code> type defined in XML
   * schema <code>'mySchema'</code>.
   *
   * @param type                item type to match
   * @return                    <code>true</code> if this item matches
   *                            the input item type as defined in
   *                            <a href="http://www.w3.org/TR/xquery/#id-matching-item">
   *                            <i>2.5.4.2 Matching an Item Type and an Item,
   *                            XQuery 1.0: An XML Query Language</i></a>,
   *                            and <code>false</code> if it does not
   * @exception XQException     if (1) there are errors accessing the item's
   *                            type, (2) if the underlying sequence or item
   *                            is in a closed state, (3) if the implementation
   *                            is unable to determine the schema definition of a
   *                            user defined schema type, or (4) the <code>type</code>
   *                            parameter is <code>null</code> 
   */
  public boolean instanceOf(XQItemType type) throws XQException;

 /**
   * Serializes the current item to a <code>Writer</code> according to
   * <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>.
   *
   * Serialization parameters, which influence how serialization is
   * performed, can be specified. Refer to the 
   * <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>
   * and <i>Section 12 Serialization, XQuery
   * API for Java (XQJ) 1.0</i> for more information.
   * 
   * @param os                  the output stream into which the current item is
   *                            to be serialized 
   * @param props               specifies the serialization parameters,
   *                            <code>null</code> is considered equivalent to an empty
   *                            <code>Properties</code> object
   * @exception XQException     if (1) there are errors accessing the current
   *                            item or the underlying sequence, (2) the underlying
   *                            sequence or item is in a closed state,
   *                            (3) in the case of a forward only sequence, a get or
   *                            write method has already been invoked on the current item,
   *                            (4) if there are errors during serialization, or 
   *                            (5) the <code>os</code> parameter is <code>null</code>
   */
  public void writeItem(java.io.OutputStream os, java.util.Properties props)
       throws XQException;
  /**
   * Serializes the current item to a <code>Writer</code> according to
   * <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>.
   * 
   * Serialization parameters, which influence how serialization is
   * performed, can be specified. Refer to the 
   * <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>
   * and <i>Section 12 Serialization, XQuery
   * API for Java (XQJ) 1.0</i> for more information.
   * <br>
   * <br>
   * Warning: When outputting to a <code>Writer</code>, make sure the writer's encoding
   * matches the encoding parameter if specified as a property or the default
   * encoding.
   * 
   * @param ow                  the writer object into which the current item is to be
   *                            serialized 
   * @param props               specifies the serialization parameters,
   *                            <code>null</code> is considered equivalent to an empty
   *                            <code>Properties</code> object
   * @exception XQException     if (1) there are errors accessing the current
   *                            item or the underlying sequence, (2) the underlying
   *                            sequence or item is in a closed state,
   *                            (3) in the case of a forward only sequence, a get or
   *                            write method has already been invoked on the current item,
   *                            (4) if there are errors during serialization, or
   *                            (5) the <code>ow</code> parameter is <code>null</code>
   */
  public void writeItem(java.io.Writer ow, java.util.Properties props)
       throws XQException;

  /**
   * Writes the current item to a SAX handler, as described in
   * in <i>Section 12.2 Serializing an XDM instance into a SAX event stream,
   * XQuery API for Java (XQJ) 1.0</i>.
   *
   * Note that the serialization process might fail, in
   * which case a <code>XQException</code> is thrown. 
   *
   * The specified <code>org.xml.sax.ContentHandler</code> can optionally implement the
   * <code>org.xml.sax.LexicalHandler</code> interface. An implementation must check if the
   * specified <code>ContentHandler</code> implements <code>LexicalHandler</code>.
   * If the handler is a <code>LexicalHandler</code> comment nodes are reported, otherwise
   * they will be silently ignored.
   *
   * @param saxhdlr             the SAX content handler, optionally a lexical handler
   * @exception XQException     if (1) there are errors accessing the current
   *                            item or the underlying sequence, (2) the underlying
   *                            sequence or item is in a closed state, (3) in the case
   *                            of a forward only sequence, a get or write method has
   *                            already been invoked on the current item, (4) in case
   *                            of an error while serializing the XDM instance
   *                            into a SAX event stream, or (5) the <code>saxhdlr</code>
   *                            parameter is <code>null</code>
   */
   public void writeItemToSAX(org.xml.sax.ContentHandler saxhdlr)
      throws XQException;

  /**
   * Writes the current item to a <code>Result</code>. First the item is
   * normalized as described in <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>. Subsequently it is
   * serialized to the <code>Result</code> object.<br>
   *
   * Note that the normalization process can fail, in which case an
   * <code>XQException</code> is thrown.
   *
   * An XQJ implementation must at least support the following implementations:
   * <ul>
   *   <li><code>javax.xml.transform.dom.DOMResult</code></li>
   *   <li><code>javax.xml.transform.sax.SAXResult</code></li>
   *   <li><code>javax.xml.transform.stream.StreamResult</code></li>
   * </ul>
   *
   * @param result              the result object into which the item is to be serialized
   * @exception XQException     if (1) there are errors accessing the current
   *                            item or the underlying sequence, (2) the underlying
   *                            sequence or item is in a closed state, (3) in the case
   *                            of a forward only sequence, a get or write method has
   *                            already been invoked on the current item, (4) in case
   *                            of an error while serializing the current item into the
   *                            <code>Result</code> object, or (5) the <code>result</code>
   *                            parameter is <code>null</code>
   */
   public void writeItemToResult(javax.xml.transform.Result result)
                        throws XQException;

};
