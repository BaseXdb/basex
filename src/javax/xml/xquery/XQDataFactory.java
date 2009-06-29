/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import javax.xml.namespace.QName;
import java.net.URI;
import org.w3c.dom.Node;
import java.io.Reader;
import java.io.InputStream;

/** 
 * This interface represents a factory to obtain sequences,  
 * item objects and types. 
 * <p>
 * 
 * The items, sequences and types obtained through this interface are
 * independent of any connection.   <p>
 * The items and sequences created are immutable. The <code>close</code> method can 
 * be called to close the item or sequence and release all resources associated
 * with this item or sequence.<p>
 */
public interface XQDataFactory 
{

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the casting from
   * <code>xs:string</code> rules outlined in
   * <a href="http://www.w3.org/TR/xpath-functions/#casting-from-strings">
   * <i>17.1.1 Casting from xs:string and xs:untypedAtomic, XQuery 1.0 and
   * XPath 2.0 Functions and Operators</i></a>. If the cast fails
   * an <code>XQException</code> is thrown.
   *
   * @param value               the lexical string value of the type
   * @param type                the item type
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     if (1) any of the arguments are <code>null</code>,
   *                            (2) given type is not an atomic type,
   *                            (3) the conversion of the value to an XDM
   *                            instance failed, or (4) the underlying object
   *                            implementing the interface is closed
   */
   public XQItem createItemFromAtomicValue(String value, XQItemType type)
         throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type, which must represent an
   * <code>xs:string</code> or a type derived by restriction from
   * <code>xs:string</code>. If the specified type is <code>null</code>, it
   * defaults to <code>xs:string</code>.<br>
   *
   * Subsequently the value is converted into an instance of the specified
   * type according to the rule defined in <i>14.2 Mapping a Java Data Type to
   * an XQuery Data Type, XQuery API for Java (XQJ) 1.0</i>. If the
   * conversion fails, an <code>XQException</code> will be thrown. 
   *
   * @param value                the value to be converted, cannot be 
   *                             <code>null</code>
   * @param type                 the type of the value to be bound to the
   *                             external variable. The default type, 
   *                             <code>xs:string</code>, is used in case
   *                             <code>null</code> is specified 
   * @return                     <code>XQItem</code> representing the resulting
   *                             item 
   * @exception XQException      if (1) the <code>value</code> argument is
   *                             <code>null</code>, (2) the conversion of the
   *                             value to an XDM instance failed, or (3) the
   *                             underlying object implementing the interface
   *                             is closed
   */
   public XQItem createItemFromString(String value, XQItemType type)
         throws XQException;

 /**
   * Creates an item from the given value.
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
   * If the value is not well formed, or if a kind of the input type other
   * than the values list above is specified, behavior is implementation
   * defined and may raise an exception. 
   *
   * @param value               the value to be converted, cannot be
   *                            <code>null</code>
   * @param baseURI             an optional base URI, can be <code>null</code>. It can
   *                            be used, for example, to resolve relative URIs and to
   *                            include in error messages.
   * @param type                the type of the value for the created
   *                            document node. If <code>null</code> is specified,
   *                            it behaves as if
   *                            <code>XQDataFactory.createDocumentElementType(
   *                            XQDataFactory.createElementType(null, 
   *                            XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                            as the type parameter.  That is, the type represents the
   *                            XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   *
   * @return                    <code>XQItem</code> representing the resulting
   *                            item 
   * @exception XQException     if (1) the value argument is <code>null</code>,
   *                            (2) the conversion of the value to an XDM instance
   *                            failed, or (3) the underlying object
   *                            implementing the interface is closed
   */
   public XQItem createItemFromDocument (String value, String baseURI, XQItemType type) throws XQException;

 /**
   * Creates an item from the given value.
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
   * If the value is not well formed, or if a kind of the input type other
   * than the values list above is specified, behavior is implementation
   * defined and may raise an exception. 
   *
   * @param value               the value to be converted, cannot be
   *                            <code>null</code>
   * @param baseURI             an optional base URI, can be <code>null</code>. It can
   *                            be used, for example, to resolve relative URIs and to
   *                            include in error messages.
   * @param type                the type of the value for the created
   *                            document node. If <code>null</code> is specified,
   *                            it behaves as if
   *                            <code>XQDataFactory.createDocumentElementType(
   *                            XQDataFactory.createElementType(null, 
   *                            XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                            as the type parameter.  That is, the type represents the
   *                            XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   * @return                    <code>XQItem</code> representing the resulting
   *                             item 
   * @exception XQException     if (1) the value argument is <code>null</code>,
   *                            (2) the conversion of the value to an XDM instance
   *                            failed, or (3) the underlying object
   *                            implementing the interface is closed
   */
   public XQItem createItemFromDocument (Reader value, String baseURI, XQItemType type) throws XQException;

 /**
   * Creates an item from the given value.
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
   * If the value is not well formed, or if a kind of the input type other
   * than the values list above is specified, behavior is implementation
   * defined and may raise an exception. 
   *
   * @param value               the value to be converted, cannot be
   *                            <code>null</code>
   * @param baseURI             an optional base URI, can be <code>null</code>. It can
   *                            be used, for example, to resolve relative URIs and to
   *                            include in error messages.
   * @param type                the type of the value for the created
   *                            document node. If <code>null</code> is specified,
   *                            it behaves as if
   *                            <code>XQDataFactory.createDocumentElementType(
   *                            XQDataFactory.createElementType(null, 
   *                            XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                            as the type parameter.  That is, the type represents the
   *                            XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   * @return                    <code>XQItem</code> representing the resulting
   *                             item 
   * @exception XQException     if (1) the value argument is <code>null</code>,
   *                            (2) the conversion of the value to an XDM instance
   *                            failed, or (3) the underlying object
   *                            implementing the interface is closed
   */
   public XQItem createItemFromDocument (InputStream value, String baseURI, XQItemType type) throws XQException;

 /**
   * Creates an item from the given value.
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
   * defined and may raise an exception. 
   *
   * @param value                the value to be converted, cannot be <code>null</code>
   * @param type                 the type of the value for the created
   *                             document node. If <code>null</code> is specified,
   *                             it behaves as if
   *                             <code>XQDataFactory.createDocumentElementType(
   *                             XQDataFactory.createElementType(null, 
   *                             XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                             as the type parameter.  That is, the type represents the
   *                             XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   * @return                     <code>XQItem</code> representing the resulting
   *                             item 
   * @exception XQException      if (1) the value argument is <code>null</code>, (2)
   *                             the conversion of the value to an XDM instance failed,
   *                             or (3) the underlying object implementing the interface
   *                             is closed
   */
   public XQItem createItemFromDocument (javax.xml.stream.XMLStreamReader value, XQItemType type)
        throws XQException;

 /**
   * Creates an item from the given <code>Source</code>. An XQJ
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
   * defined and may raise an exception.
   *
   * @param value                the value to be converted, cannot be <code>null</code>
   * @param type                 the type of the value for the created
   *                             document node. If <code>null</code> is specified,
   *                             it behaves as if
   *                             <code>XQDataFactory.createDocumentElementType(
   *                             XQDataFactory.createElementType(null, 
   *                             XQItemType.XQBASETYPE_XS_UNTYPED))</code> were passed in
   *                             as the type parameter.  That is, the type represents the
   *                             XQuery sequence type <code>document-node(element(*, xs:untyped))</code>
   * @return                     <code>XQItem</code> representing the resulting
   *                             item 
   * @exception XQException      if (1) the value argument is <code>null</code>, (2)
   *                             the conversion of the value to an XDM instance failed,
   *                             or (3) the underlying object implementing the interface
   *                             is closed
   */
   public XQItem createItemFromDocument(javax.xml.transform.Source value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted, cannot be
   *                            <code>null</code>
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     if (1) the <code>value</code> argument is
   *                            <code>null</code>, (2) the conversion of the value
   *                            to an XDM instance failed, or (3) the underlying object
   *                            implementing the interface is closed
   */
  public XQItem createItemFromObject(Object value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     (1) the conversion of the value
   *                            to an XDM instance failed, or (2) the underlying object
   *                            implementing the interface is closed
   */
  public XQItem createItemFromBoolean(boolean value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     (1) the conversion of the value
   *                            to an XDM instance failed, or (2) the underlying object
   *                            implementing the interface is closed
   */

  public XQItem createItemFromByte(byte value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     (1) the conversion of the value
   *                            to an XDM instance failed, or (2) the underlying object
   *                            implementing the interface is closed
   */
  public XQItem createItemFromDouble(double value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     (1) the conversion of the value
   *                            to an XDM instance failed, or (2) the underlying object
   *                            implementing the interface is closed
   */
  public XQItem createItemFromFloat(float value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     (1) the conversion of the value
   *                            to an XDM instance failed, or (2) the underlying object
   *                            implementing the interface is closed
   */
  public XQItem createItemFromInt(int value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     (1) the conversion of the value
   *                            to an XDM instance failed, or (2) the underlying object
   *                            implementing the interface is closed
   */
  public XQItem createItemFromLong(long value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted, cannot be
   *                            <code>null</code>
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     if (1) the <code>value</code> argument is
   *                            <code>null</code>, (2) the conversion of the value
   *                            to an XDM instance failed, or (3) the underlying object
   *                            implementing the interface is closed
   */
  public XQItem createItemFromNode(Node value, XQItemType type) throws XQException;

 /**
   * Creates an item from a given value. The value is converted into an
   * instance of the specified type according to the rule defined in
   * <i>14.2 Mapping a Java Data Type to an XQuery Data Type, XQuery API for
   * Java (XQJ) 1.0</i>. If the converstion fails, an
   * <code>XQException</code> will be thrown.
   *
   * @param value               the value to be converted
   * @param type                the type of the value to be bound to
   *                            the external variable. The default type of
   *                            the value is used in case <code>null</code> is
   *                            specified
   * @return                    <code>XQItem</code> representing the resulting item
   * @exception XQException     (1) the conversion of the value
   *                            to an XDM instance failed, or (2) the underlying object
   *                            implementing the interface is closed
   */
  public XQItem createItemFromShort(short value, XQItemType type) throws XQException;

  /** 
    * Creates a copy of the specified <code>XQItem</code>. This method can be used, for
    * example, to copy an <code>XQResultItem</code> object so that the new item is not
    * dependant on the connection.
    *
    * @param item                the <code>XQItem</code> to copy
    * @return                    <code>XQItem</code> independent of any underlying
    *                            <code>XQConnection</code> is created
    * @exception XQException     if (1) the specified item is <code>null</code>,
    *                            (2) the underlying object implementing the interface is
    *                            closed, (3) the specified item is closed
    */
   public XQItem createItem(XQItem item) throws XQException;

   /**
     * Creates a copy of the specified <code>XQSequence</code>. The newly created
     * <code>XQSequence</code> is scrollable and independent of any underlying
     * <code>XQConnection</code>. The new <code>XQSequence</code> will contain all
     * items from the specified <code>XQSequence</code> starting from its current
     * position. The copy process will implicitly perform next operations on the
     * specified sequence to read the items. All items are consumed, the current
     * position of the cursor is set to point after the last item.
     *
     * @param s                   input sequence
     * @return                    <code>XQSequence</code> representing a copy of
     *                            the input sequence
     * @exception XQException     if (1) there are errors accessing the items in
     *                            the specified sequence, (2) the specified sequence
     *                            is closed, (3) in the case of a forward only
     *                            sequence, a get or write method has already
     *                            been invoked on the current item, (4)
     *                            the <code>s</code> parameter is <code>null</code>,
     *                            or (5) the underlying object implementing the
     *                            interface is closed
     */
   public XQSequence createSequence(XQSequence s) throws XQException;

   /**
     * Creates an <code>XQSequence</code>, containing all the items from the
     * iterator. The newly created <code>XQSequence</code> is scrollable and
     * independent of any underlying <code>XQConnection</code>.
     *
     * If the iterator returns an <code>XQItem</code>, it is added to the
     * sequence. If the iterator returns any other object, an item is added to the
     * sequence following the rules from <i>14.2 Mapping a Java Data Type
     * to an XQuery Data Type, XQuery API for Java (XQJ) 1.0</i>.
     *
     * If the iterator does not return any items, then an empty sequence is created.
     *
     * @param i                   input iterator
     * @return                    <code>XQSequence</code> representing the sequence 
     *                            containing all items from the input iterator
     * @exception XQException     if (1) the conversion of any of the objects in the
     *                            iterator to item fails, (2)
     *                            the <code>i</code> parameter is <code>null</code>,
     *                            or (3) underlying object implementing the interface
     *                            is closed
     */
   public XQSequence createSequence(java.util.Iterator i) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing an XQuery atomic type.
    * The item kind of the item type will be <code>XQItemType.XQITEMKIND_ATOMIC</code>.
    * <br>
    * Example -
    * <pre>
    *  XQConnection conn = ...;
    *
    *  // to create xs:integer item type
    *  conn.createAtomicType(XQItemType.XQBASETYPE_INTEGER); 
    * </pre>
    *
    * @param basetype            one of the <code>XQItemType.XQBASETYPE_*</code>
    *                            constants. All basetype constants except the
    *                            following are valid: 
    * <pre> 
    *  XQItemType.XQBASETYPE_UNTYPED
    *  XQItemType.XQBASETYPE_ANYTYPE
    *  XQItemType.XQBASETYPE_IDREFS
    *  XQItemType.XQBASETYPE_NMTOKENS
    *  XQItemType.XQBASETYPE_ENTITIES
    *  XQItemType.XQBASETYPE_ANYSIMPLETYPE
    * </pre>
    *
    * @return                    a new <code>XQItemType</code> representing the atomic type
    * @exception XQException     if (1) an invalid <code>basetype</code> value is
    *                            passed in, or (2) the underlying object implementing
    *                            the interface is closed
    */
  public XQItemType createAtomicType(int basetype) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing an XQuery atomic type.
    * The item kind of the item type will be <code>XQItemType.XQITEMKIND_ATOMIC</code>.
    * <br>
    * Example -
    * <pre>
    *
    *  XQConnection conn = ...;
    *
    *  // to create po:hatsize atomic item type
    *  conn.createAtomicType(XQItemType.XQBASETYPE_INTEGER, 
    *                  new QName("http://www.hatsizes.com", "hatsize","po"), 
    *                  new URI("http://hatschema.com"));
    * </pre>
    *
    * @param basetype            one of the <code>XQItemType.XQBASETYPE_*</code>
    *                            constants. All basetype constants except the
    *                            following are valid: 
    * <pre> 
    *  XQItemType.XQBASETYPE_UNTYPED
    *  XQItemType.XQBASETYPE_ANYTYPE
    *  XQItemType.XQBASETYPE_IDREFS
    *  XQItemType.XQBASETYPE_NMTOKENS
    *  XQItemType.XQBASETYPE_ENTITIES
    *  XQItemType.XQBASETYPE_ANYSIMPLETYPE
    * </pre>
    * @param typename            the <code>QName</code> of the type. If the <code>QName</code>
    *                            refers to a predefinied type, it must match
    *                            the <code>basetype</code>. Can be <code>null</code>
    * @param schemaURI           the URI to the schema. Can be <code>null</code>. This can
    *                            only be specified if the typename is also specified
    * @return                    a new <code>XQItemType</code> representing the atomic type
    * @exception XQException     if (1) an invalid <code>basetype</code> value is
    *                            passed in, (2) the underlying object implementing
    *                            the interface is closed, (3) the implementation does
    *                            not support user-defined XML schema types, or (4)
    *                            if the <code>typename</code> refers to a predefinied
    *                            type and does not match <code>basetype</code>
    */
  public XQItemType createAtomicType(int basetype, QName typename, URI schemaURI) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery
    * <code>attribute(<i>nodename</i>, <i>basetype</i>)</code> type
    * with the given node name and base type. This method can be used to create
    * item type for attributes with a pre-defined schema type.
    * <br>
    * <br>
    * Example -
    * <pre>
    *
    *  XQConnection conn = ..; // An XQuery connection
    *
    *  - attribute() // no node name, pass null for the node name
    *
    *    conn.createAttributeType(null, XQItemType.XQBASETYPE_ANYSIMPLETYPE);
    *
    *  - attribute (*)  // equivalent to attribute()
    *
    *    conn.createAttributeType(null, XQItemType.XQBASETYPE_ANYSIMPLETYPE);
    *
    *  - attribute (person) // attribute of name person and any simple type.
    *
    *    conn.createAttributeType(new QName("person"), XQItemType.XQBASETYPE_ANYSIMPLETYPE); 
    *
    *  - attribute(foo:bar) // node name foo:bar, type is any simple type
    *
    *    conn.createAttributeType(new QName("http://www.foo.com", "bar","foo"), 
    *                             XQItemType.XQBASETYPE_ANYSIMPLETYPE);
    *
    *  - attribute(foo:bar, xs:integer) // node name foo:bar, type is xs:integer
    *
    *    conn.createAttributeType(new QName("http://www.foo.com", "bar","foo"), 
    *                             XQItemType.XQBASETYPE_INTEGER);
    * </pre>
    *
    * @param nodename           specifies the name of the node.<code>null</code>
    *                           indicates a wildcard for the node name
    * @param basetype           the base type of the attribute. One of the 
    *                           <code>XQItemType.XQBASETYPE_*</code>
    *                           other than <code>XQItemType.XQBASETYPE_UNTYPED</code>
    *                           or <code>XQItemType.XQBASETYPE_ANYTYPE</code>
    * @return                   a new <code>XQItemType</code> representing the XQuery
    *                           <code>attribute(<i>nodename</i>, <i>basetype</i>)</code> type
    * @exception XQException    if (1) the underlying object implementing the interface is closed or
    *                           (2) if the base type is one of:
    *                           <code>XQItemType.XQBASETYPE_UNTYPED</code>
    *                           or <code>XQItemType.XQBASETYPE_ANYTYPE</code>
    */
  public XQItemType createAttributeType(QName nodename, int basetype) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>attribute(<i>nodename</i>,<i>basetype</i>,<i>typename</i>,<i>schemaURI</i>)</code> type,
    * with the given node name, base type, schema type name and schema URI. The type name can
    * reference either pre-defined simple types or user-defined simple types.
    * <br>
    * <br>
    * Example -
    * <pre>
    *
    *  XQConnection conn = ..; // An XQuery connection
    *
    *  - attribute (name, employeename) // attribute name of type employeename 
    * 
    *  conn.createAttributeType(new QName("name"), XQItemType.XQBASETYPE_ANYSIMPLETYPE,
    *                           new QName("employeename"), null);
    * 
    *  - attribute (foo:bar, po:city) 
    *  where the prefix foo refers to the namespace http://www.foo.com and the
    *  prefix po refers to the namespace "http://www.address.com"
    *
    *  conn.createAttributeType(new QName("http://www.foo.com", "bar","foo"), 
    *                           XQItemType.XQBASETYPE_ANYSIMPLETYPE,
    *                           new QName("http://address.com", "address","po"), null);
    *
    *  - attribute (zip, zipcode) // attribute zip of type zipchode which derives from
    *                             // xs:string 
    *
    *  conn.createAttributeType(new QName("zip"), XQItemType.XQBASETYPE_STRING,
    *                           new QName("zipcode"), null);
    *
    *  - attribute(foo:bar, po:hatsize) 
    *  where the prefix foo refers to the namespace http://www.foo.com and the
    *  prefix po refers to the namespace "http://www.hatsizes.com" 
    *  with schema URI "http://hatschema.com"
    *
    *  conn.createAttributeType(new QName("http://www.foo.com", "bar","foo"), 
    *                  XQItemType.XQBASETYPE_INTEGER,
    *                  new QName("http://www.hatsizes.com", "hatsize","po"), 
    *                  new QName("http://hatschema.com"));
    * </pre>
    *
    * @param nodename           specifies the name of the node.<code>null</code>
    *                           indicates a wildcard for the node name
    * @param basetype           the base type of the attribute. One of the 
    *                           <code>XQItemTyupe.XQBASETYPE_*</code> constants
    *                           other than <code>XQItemType.XQBASETYPE_UNTYPED</code> or
    *                           <code>XQItemType.XQBASETYPE_ANYTYPE</code>
    * @param typename           the <code>QName</code> of the type. If the <code>QName</code>
    *                           refers to a predefinied type, it must match
    *                           the <code>basetype</code>. Can be <code>null</code>.
    * @param schemaURI          the URI to the schema. Can be <code>null</code>. This can
    *                           only be specified if the typename is also specified
    * @return                   a new <code>XQItemType</code> representing the XQuery
    *                           <code>attribute(<i>nodename</i>,<i>basetype</i>,
    *                           <i>typename</i>,<i>schemaURI</i>)</code> type.
    * @exception XQException    if (1) the underlying object implementing the interface is closed,
    *                           (2) if the base type is one of:
    *                           <code>XQItemType.XQBASETYPE_UNTYPED</code> or
    *                           <code>XQItemType.XQBASETYPE_ANYTYPE</code>,
    *                           (3) the schema URI is specified and the typename
    *                           is not specified, (4) the implementation does 
    *                           not support user-defined XML schema types, or (5)
    *                           if the <code>typename</code> refers to a predefinied
    *                           type and does not match <code>basetype</code>
    */
  public XQItemType createAttributeType(QName nodename, int basetype,
                                        QName typename, URI schemaURI) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>schema-attribute(<i>nodename</i>,<i>basetype</i>,<i>schemaURI</i>)</code> type,
    * with the given node name, base type, and schema URI. 
    * <br>
    * <br>
    * Example -
    * <pre>
    *
    *     XQConnection conn = ..; // An XQuery connection
    *
    *     - schema-attribute (name) // schema-attribute name, found in the schema 
    *                               // available at http://customerschema.com
    *
    *     conn.createSchemaAttributeType(new QName("name"), 
    *                  XQItemType.XQBASETYPE_STRING,
    *                  new URI(http://customerschema.com));
    * </pre>
    *
    * @param nodename           specifies the name of the node
    * @param basetype           the base type of the attribute. One of the 
    *                           <code>XQItemTyupe.XQBASETYPE_*</code> constants
    *                           other than <code>XQItemType.XQBASETYPE_UNTYPED</code> or
    *                           <code>XQItemType.XQBASETYPE_ANYTYPE</code>
    * @param schemaURI          the URI to the schema. Can be <code>null</code>
    * @return                   a new <code>XQItemType</code> representing the XQuery
    *                           <code>schema-attribute(<i>nodename</i>,<i>basetype</i>,
    *                           <i>schemaURI</i>)</code> type
    * @exception XQException    if (1) the node name is <code>null</code>,
    *                           (2) if the base type is one of:
    *                           <code>XQItemType.XQBASETYPE_UNTYPED</code> or
    *                           <code>XQItemType.XQBASETYPE_ANYTYPE</code>,
    *                           (3) the underlying object implementing the interface
    *                           is closed, or (4) the implementation does 
    *                           not support user-defined XML schema types
    */
  public XQItemType createSchemaAttributeType(QName nodename, int basetype,
                                              URI schemaURI) throws XQException;
   
  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery
    * <code>comment()</code> type. The <code>XQItemType</code> object will have 
    * the item kind set to <code>XQItemType.XQITEMKIND_COMMENT</code>.
    * <br>
    * <br>
    * Example -
    * <pre>
    *  XQConnection conn = ..; // An XQuery connection
    *  XQItemType cmttype = conn.createCommentType(); 
    *
    *  int itemkind = cmttype.getItemKind(); // will be XQItemType.XQITEMKIND_COMMENT
    * 
    *  XQExpression expr = conn.createExpression();
    *  XQSequence result = expr.executeQuery("&lt;!-- comments --&gt;");
    *
    *  result.next();
    *  boolean pi = result.instanceOf(cmttype);  // will be true
    * </pre>
    *
    * @return                   a new <code>XQItemType</code> representing the XQuery
    *                           <code>comment()</code> type
    * @exception XQException    if the underlying object implementing the interface is closed
    */
  public XQItemType createCommentType() throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>document-node(<i>elementType</i>)</code> type containing a single element.
    * The <code>XQItemType</code> object will have the item kind set to
    * <code>XQItemType.XQITEMKIND_DOCUMENT_ELEMENT</code> and the
    * base type set to the item type of the input <code>elementType</code>.
    *
    * @param elementType        an <code>XQItemType</code> object representing an XQuery
    *                           <code>element()</code> type, cannot be <code>null</code>
    * @return                   a new <code>XQItemType</code> representing the XQuery
    *                           <code>document-node(<i>elementType</i>)</code> type containing
    *                           a single element
    * @exception XQException    if (1) the underlying object implementing the interface is
    *                           closed or (2) the <code>elementType</code> has an item kind
    *                           different from <code>XQItemType.XQITEMKIND_ELEMENT</code>,
    *                           (3) the <code>elementType</code> argument is <code>null</code>,
    *                           or (4) the implementation does not support user-defined XML 
    *                           schema types
    */
  public XQItemType createDocumentElementType(XQItemType elementType) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>document-node(<i>elementType</i>)</code> type containing a single 
    * <code>schema-element(...)</code>. The <code>XQItemType</code> object will
    * have the item kind set to <code>XQItemType.XQITEMKIND_DOCUMENT_SCHEMA_ELEMENT</code> and the
    * base type set to the item type of the input <code>elementType</code>.
    *
    * @param elementType        an <code>XQItemType</code> object representing an XQuery
    *                           <code>schema-element(...)</code> type, cannot be <code>null</code>
    * @return                   a new <code>XQItemType</code> representing the XQuery
    *                           <code>document-node(<i>elementType</i>)</code> type containing
    *                           a single <code>schema-element(...)</code> element
    * @exception XQException    if (1) the underlying object implementing the interface is
    *                           closed or (2) the <code>elementType</code> has an item kind
    *                           different from <code>XQItemType.XQITEMKIND_SCHEMA_ELEMENT</code>,
    *                           (3) the <code>elementType</code> argument is <code>null</code>,
    *                           (4) the implementation does not support user-defined XML
    *                           schema types 
    */
   public XQItemType createDocumentSchemaElementType(XQItemType elementType) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>document-node()</code> type. The <code>XQItemType</code> object will have the item kind
    * set to <code>XQItemType.XQITEMKIND_DOCUMENT</code>.
    *
    * @return                   a new <code>XQItemType</code> representing the XQuery 
    *                           <code>document-node()</code> type
    * @exception XQException    if the underlying object implementing the interface is closed
    */
  public XQItemType createDocumentType() throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>element(<i>nodename</i>, <i>basetype</i>)</code> type, with the
    * given node name and base type. This method can be used to create item
    * type for elements with a pre-defined schema type.
    * <br>
    * <br>
    * Example -
    * <pre>
    *  XQConnection conn = ..; // An XQuery connection
    *  - element() // no node name, pass null for the node name
    *
    *  conn.createElementType(null, XQItemType.XQBASETYPE_ANYTYPE);
    *
    *  - element (*)  // equivalent to element()
    *
    *  conn.createElementType(null, XQItemType.XQBASETYPE_ANYTYPE);
    *
    *  - element(person) // element of name person and any type.
    *
    *  conn.createElementType(new QName("person"), XQItemType.XQBASETYPE_ANYTYPE); 
    *
    *  - element(foo:bar) // node name foo:bar, type is anytype
    *
    *  conn.createElementType(new QName("http://www.foo.com", "bar","foo"), 
    *                         XQItemType.XQBASETYPE_ANYTYPE);
    *
    *  - element(foo:bar, xs:integer) // node name foo:bar, type is xs:integer
    *
    *  conn.createElementType(new QName("http://www.foo.com", "bar","foo"), 
    *                         XQItemType.XQBASETYPE_INTEGER);
    * </pre>
    * 
    * @param nodename           specifies the name of the node. <code>null</code>
    *                           indicates a wildcard for the node name
    * @param basetype           the base type of the item. One of the
    *                           <code>XQItemType.XQBASETYPE_*</code> constants
    * @return                   a new <code>XQItemType</code> representing the XQuery 
    *                           <code>element(<i>nodename</i>, <i>basetype</i>)</code> type
    * @exception XQException    if (1) the underlying object implementing the interface
    *                           is closed
    */
  public XQItemType createElementType(QName nodename, int basetype) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>element(<i>nodename</i>,<i>basetype</i>,<i>typename</i>,<i>schemaURI</i>,
    * <i>allowNill</i>)</code> type, given the node name, base type, schema type
    * name, schema URI, and nilled check. The type name can reference either pre-defined
    * schema types or user-defined types.
    * <br>
    * <br>
    * Example -
    * <pre>
    *  XQConnection conn = ..; // An XQuery connection
    *
    *  - element (person, employee) // element person of type employee
    * 
    *  conn.createElementType(new QName("person"), XQItemType.XQBASETYPE_ANYTYPE,
    *                         new QName("employee"), null ,false);
    * 
    *  - element(person, employee ? ) // element person of type employee, whose nilled 
    *                                 // property may be true or false. 
    * 
    *  conn.createElementType(new QName("person"), XQItemType.XQBASETYPE_ANYTYPE,
    *                         new QName("employee"), null ,true);
    * 
    *  - element(foo:bar, po:address) 
    *  where the prefix foo refers to the namespace http://www.foo.com and the
    *  prefix po refers to the namespace "http://www.address.com"
    *
    *  conn.createElementType(new QName("http://www.foo.com", "bar","foo"), 
    *               XQItemType.XQBASETYPE_ANYTYPE,
    *               new QName("http://address.com", "address","po"), null, false);
    *
    *  - element (zip, zipcode) // element zip of type zipchode which derives from
    *                           // xs:string 
    *
    *  conn.createElementType(new QName("zip"), XQItemType.XQBASETYPE_STRING,
    *                         new QName("zipcode"), null, false);
    *
    *  - element (*, xs:anyType ?)
    *
    *  conn.createElementType(null, XQItemType.XQBASETYPE_ANYTYPE, null, null, true);
    *
    *  - element(foo:bar, po:hatsize) 
    *  where the prefix foo refers to the namespace http://www.foo.com and the
    *  prefix po refers to the namespace "http://www.hatsizes.com" 
    *  with schema URI "http://hatschema.com"
    *
    *  conn.createElementType(new QName("http://www.foo.com", "bar","foo"), 
    *                      XQItemType.XQBASETYPE_INTEGER,
    *                      new QName("http://www.hatsizes.com", "hatsize","po"), 
    *                      new QName("http://hatschema.com"), false);
    *
    * </pre> 
    *
    * @param nodename           specifies the name of the element. <code>null</code>
    *                           indicates a wildcard for the node name
    * @param basetype           the base type of the item. One of the 
    *                           <code>XQItemType.XQBASETYPE_*</code> constants
    * @param typename           the <code>QName</code> of the type. If the <code>QName</code>
    *                           refers to a predefinied type, it must match
    *                           the <code>basetype</code>.  Can be <code>null</code>
    * @param schemaURI          the URI to the schema. Can be <code>null</code>. This can
    *                           only be specified if the typename is also specified
    * @param allowNill          the nilled property of the element
    * @return                   a new <code>XQItemType</code> representing the XQuery
    *                           <code>element(<i>nodename</i>,<i>basetype</i>,
    *                           <i>typename</i>,<i>schemaURI</i>,
    *                           <i>allowNill</i>)</code> type
    * @exception XQException    if (1) schemaURI is specified but the typename is not
    *                           specified, (2) the underlying object implementing the
    *                           interface is closed, (3) the implementation does 
    *                           not support user-defined XML schema types, or
    *                           (4) if the <code>typename</code> refers to a predefinied
    *                           type and does not match <code>basetype</code>
    */
  public XQItemType createElementType(QName nodename, int basetype, QName typename,
                                      URI schemaURI, boolean allowNill) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>schema-element(<i>nodename</i>,<i>basetype</i>,<i>schemaURI</i>)</code> 
    * type, given the node name, base type, and the schema URI. 
    * <br>
    * <br>
    * Example -
    * <pre>
    *     XQConnection conn = ..; // An XQuery connection
    *
    *     - schema-element (customer) // schema-element person, found in
    *                                 // the schema available at http://customerschema.com
    *
    *     conn.createElementType(new QName("customer"), XQItemType.XQBASETYPE_ANYTYPE,
    *                         new URI("http://customerschema.com"));
    *
    *  </pre>
    * 
    * @param nodename          specifies the name of the element
    * @param basetype          the base type of the item. One of the 
    *                          <code>XQItemType.XQBASETYPE_*</code> constants
    * @param schemaURI         the URI to the schema. Can be <code>null</code>
    * @return                  a new <code>XQItemType</code> representing the XQuery
    *                          <code>schema-element(<i>nodename</i>,<i>basetype</i>,
    *                          <i>schemaURI</i>)</code> type
    * @exception XQException   if (1) the node name is <code>null</code>, 
    *                          (2) the underlying object implementing the
    *                          interface is closed, or (3) the implementation does 
    *                          not support user-defined XML schema types
    */
  public XQItemType createSchemaElementType(QName nodename, int basetype,
                                            URI schemaURI) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery item type.
    * The <code>XQItemType</code> object will have the item kind set to
    * <code>XQItemType.XQITEMKIND_ITEM</code>.
    * <br>
    * <br>
    * Example -
    * <pre>
    *  XQConnection conn = ..; // An XQuery connection
    *  XQItemType typ = conn.createItemType(); // represents the XQuery item type "item()"
    * </pre> 
    *
    * @return                   a new <code>XQItemType</code> representing the XQuery
    *                           item type
    * @exception XQException    if the underlying object implementing the interface
    *                           is closed
    */
  public XQItemType createItemType() throws XQException;

  /**
   * Creates a new <code>XQItemType</code> object representing the XQuery <code>node()</code>
   * type. The <code>XQItemType</code> object will have the item kind set to
   * <code>XQItemType.XQITEMKIND_NODE</code>.
   *
   * @return                    a new <code>XQItemType</code> representing the
   *                            XQuery <code>node()</code> type
   * @exception XQException     if the underlying object implementing the interface
   *                            is closed
   */
  public XQItemType createNodeType() throws XQException;

  /**
   * Creates a new <code>XQItemType</code> object representing the XQuery 
   * <code>processing-instruction(<i>piTarget</i>)</code> type. The <code>XQItemType</code> object
   * will have the item kind set to <code>XQItemType.XQITEMKIND_PI</code>. A
   * string literal can be passed to match the PITarget of the processing instruction
   * as described in <a href="http://www.w3.org/TR/xquery/#id-matching-item">
   * <i>2.5.4.2 Matching an Item Type and an Item, XQuery 1.0: An XML Query Language</i></a>.
   * <br>
   * <br>
   *  Example -
   *  <pre>
   *   XQConnection conn = ..; // An XQuery connection
   *   XQItemType anypi = conn.createProcessingInstructionType(); 
   *   XQItemType foopi = conn.createProcessingInstructionType("foo-format");
   *
   *   XQExpression expr = conn.createExpression();
   *   XQSequence result = expr.executeQuery("&lt;?format role="output" ?&gt;");
   * 
   *   result.next();
   *   boolean pi = result.instanceOf(anypi);  // will be true
   *   pi = result.instanceOf(foopi);  // will be false
   * 
   *   XQExpression expr = conn.createExpression();
   *   XQSequence result = expr.executeQuery("&lt;?foo-format role="output" ?&gt;");
   *   
   *   result.next();
   *   boolean pi = result.instanceOf(anypi);  // will be true
   *   pi = result.instanceOf(foopi);  // will be true 
   * </pre> 
   *
   * @param piTarget           the string literal to match the processing
   *                           instruction's PITarget. A <code>null</code> string
   *                           value will match all processing instruction nodes
   * @return                   a new <code>XQItemType</code> representing the XQuery 
   *                           <code>processing-instruction(<i>piTarget</i>)</code> type
   * @exception XQException    if the underlying object implementing the interface is
   *                           closed
   */
  public XQItemType createProcessingInstructionType(String piTarget)
                                           throws XQException;

  /**
    * Creates a new sequence type from an item type and occurence indicator.
    *
    * @param item                the item type. This parameter must be <code>null</code> if
    *                            the occurance is <code>XQSequenceType.OCC_EMPTY</code>,
    *                            and cannot be <code>null</code> for any other
    *                            occurance indicator
    * @param occurence           The occurence of the item type, must be one of
    *                            <code>XQSequenceType.OCC_ZERO_OR_ONE</code>,
    *                            <code>XQSequenceType.OCC_EXACTLY_ONE</code>,
    *                            <code>XQSequenceType.OCC_ZERO_OR_MORE</code>,
    *                            <code>XQSequenceType.OCC_ONE_OR_MORE</code>,
    *                            <code>XQSequenceType.OCC_EMPTY</code>
    * @return                    a new <code>XQSequenceType</code> representing the
    *                            type of a sequence
    * @exception XQException     if (1) the <code>item</code> is <code>null</code>
    *                            and the occurance is not <code>XQSequenceType.OCC_EMPTY</code>,
    *                            (2) the <code>item</code> is not <code>null</code>
    *                            and the occurance is <code>XQSequenceType.OCC_EMPTY</code>,
    *                            (3) the occurence is not one of:
    *                            <code>XQSequenceType.OCC_ZERO_OR_ONE</code>,
    *                            <code>XQSequenceType.OCC_EXACTLY_ONE</code>,
    *                            <code>XQSequenceType.OCC_ZERO_OR_MORE</code>,
    *                            <code>XQSequenceType.OCC_ONE_OR_MORE</code>,
    *                            <code>XQSequenceType.OCC_EMPTY</code>
    *                            or (4) the underlying object implementing the
    *                            interface is closed
    */
  public  XQSequenceType createSequenceType(XQItemType item, int occurence) throws XQException;

  /**
    * Creates a new <code>XQItemType</code> object representing the XQuery 
    * <code>text()</code> type. The <code>XQItemType</code> object will have the
    * item kind set to <code>XQItemType.XQITEMKIND_TEXT</code>.
    *
    * @return                   a new <code>XQItemType</code> representing the XQuery 
    *                           <code>text()</code> type
    * @exception XQException    if the underlying object implementing the interface is
    *                           closed
    */
  public XQItemType createTextType() throws XQException;
};
