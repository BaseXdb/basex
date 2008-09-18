/*
 * Copyright © 2003, 2004, 2005, 2006, 2007 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import javax.xml.xquery.XQItem;

/** 
 * This interface represents a sequence of items as defined in the 
 * XDM. The sequence may be materialized or non-materialized. <p>
 * 
 * The <code>next</code> method is useful to position the 
 * <code>XQSequence</code> over the next item in the sequence. 
 * If the scrollability is <code>XQConstants.SCROLLTYPE_SCROLLABLE</code>,
 * then the <code>previous</code> method can be called to move backwards. 
 * 
 * In the case of a forward only sequence, the get methods may be only called
 * once per item. To perform multiple gets on an item, extract the item first
 * from the sequence using the <code>getItem</code> method and 
 * then operate on the <code>XQItem</code> object. 
 *  
 * <pre>
 * 
 *  XQPreparedExpression expr = conn.prepareExpression("for $i ..");
 *  XQSequence result = expr.executeQuery();
 * 
 *  // create the ItemTypes for string and integer
 *  XQItemType strType = conn.createAtomicType(XQItemType.XQBASETYPE_STRING);
 *  XQItemType intType = conn.createAtomicType(XQItemType.XQBASETYPE_INT);
 *
 *  // positioned before the first item
 *  while (result.next())
 *  {
 *    XQItemType type = result.getItemType();
 * 
 *    // If string, then get the string value out
 *    if (type.equals(strType))
 *      String str = result.getAtomicValue();
 *    else if (type.equals(intType))  // if it is an integer..
 *      int intval = result.getInt();
 * 
 *     ...
 *  }
 * 
 * </pre>
 *
 * In a sequence, the cursor may be positioned on an item, after the last item
 * or before the first item. The <code>getPosition</code> method 
 * returns the current position number. A value of 0 indicates
 * that it is  positioned before the first item, a value of <code>count() + 1</code>
 * indicates that it is positioned after the last item, and any other value
 * indicates that it is positioned on the item at that position.  <p>
 * For example, a position value of 1 indicates that it is positioned on 
 * the item at position 1. <p>
 *
 * The <code>isOnItem</code> method may be used to find out if the cursor
 * is positioned on the item. When the cursor is positioned on an item,
 * the <code>next</code> method call will move the cursor to be on the next item.
 * <p>
 * 
 * See also: <i>Section 12 Serialization, XQuery API for Java
 * (XQJ) 1.0</i>,  which describes some general information applicable
 * to various XQJ serialization methods.
 */
public interface XQSequence extends XQItemAccessor
{

  // ------------------------------------------------
  // Sequence Iterator operations
  // ------------------------------------------------
  /**
   * Moves the <code>XQSequence</code>'s position to the given item
   * number in this object. If the item number is positive, the
   * <code>XQSequence</code> moves to the given item number with
   * respect to the beginning of the <code>XQSequence</code>.  <p>
   * The first item is item 1, the second is item 2, and so on. <p>
   * If the given item number is negative, the <code>XQSequence</code>
   * positions itself on an absolute item position with respect to the
   * end of the sequence. 
   * <p>
   * For example, calling the method <code>absolute(-1)</code>
   * positions the <code>XQSequence</code> on the last item; 
   * calling the method <code>absolute(-2)</code> moves the
   * <code>XQSequence</code> to the next-to-last item, and so on. 
   * <code>absolute(0)</code> will position the sequence before the first item.
   * <p>
   * An attempt to position the sequence beyond the first/last item 
   * set leaves the current position to be before the first item or 
   * after the last item. <p>
   *
   * Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   * @param itempos             the item position to jump to
   * @return                    <code>true</code> if the current position is
   *                            within the sequence, <code>false</code> otherwise
   * @exception XQException     if (1) the sequence is forward only, or
   *                            (2) the sequence is in a closed state
   */
  public boolean absolute(int itempos) throws XQException;

  /** 
    * Move to the position after the last item.
    *
    * @exception XQException     if (1) the sequence is forward only,
    *                            or (2) the sequence is in a closed state
    */
  public void afterLast() throws XQException;

  /** 
    * Moves to the position before the first item.
    *
    * @exception XQException     if (1) the sequence is forward only,
    *                            or (2) the sequence is in a closed state
    */
  public void beforeFirst() throws XQException;

  /** 
    * Closes the sequence and frees all resources associated with this 
    * sequence. 
    *
    * Closing an <code>XQSequence</code> object also implicitly closes all
    * <code>XQItem</code> objects obtained from it.
    * 
    * All methods other than the <code>isClosed</code> 
    * or <code>close</code> method will raise exceptions when invoked 
    * after closing the sequence. 
    * Calling <code>close</code> on an <code>XQSequence</code> object
    * that is already closed has no effect.
    *
    * @exception XQException     if there are errors during closing of
    *                            the sequence
    */
  public void close() throws XQException;      // release resources

  /**
   * Checks if the sequence is closed.
   *
   * @return                     <code>true</code> if the sequence is in
   *                             a closed state, <code>false</code> otherwise
   */
  public boolean isClosed();

  /** 
   * Returns a number indicating the number of items in the sequence.
   *
   * @return                    the number of items in this sequence
   * @exception XQException     if (1) the sequence is forward-only, or (2)
   *                            the sequence is closed
   */
  public int  count() throws XQException;

  /** 
   * Gets the current cursor position. <p>
   * 0 indicates that the position is before the first item 
   * and <code>count() + 1</code> indicates position after the last item.
   * A specific position indicates that the cursor is positioned on
   * the item at that position. Use the <code>isOnItem</code>
   * method to verify if the cursor is positioned on the item.<p>
   *
   * Calling this method on an empty sequence 
   * will return <code>0</code>.
   *
   *
   * @return                    cursor position
   * @exception XQException     if (1) the sequence is forward-only, or (2)
   *                            the sequence is closed
   */
  public int getPosition() throws XQException;

  /** 
   * Check if the sequence is positioned on an item or not. 
   * Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   *
   * @return                    <code>true</code> if the sequence is currently
   *                            positioned on an item, <code>false</code> if sequence 
   *                            is positioned before the first item, or after the last
   *                            item
   * @exception XQException     if the sequence is in a closed state
   */
  public boolean isOnItem() throws XQException;

  /** 
   * Checks if the sequence is scrollable. 
   *
   * @return                    <code>true</code> if the sequence can be scrolled
   *                            backward or forward, <code>false</code> otherwise
   * @exception XQException     if the sequence is in a closed state
   */ 
  public boolean isScrollable() throws XQException;

  /** 
   * Moves to the first item in the sequence. The method returns 
   * <code>true</code>, if it was able to move to the first item in the sequence
   * <code>false</code>, otherwise. Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   * @return                    <code>true</code> if the sequence was positioned 
   *                            on the first item, <code>false</code> otherwise
   * @exception XQException     if (1) the sequence is forward only,
   *                            or (2) the sequence is in a closed state
   */
  public boolean first() throws XQException;

  /**
   * Get the current item as an immutable <code>XQItem</code> object. 
   * In case of an <code>XQResultSequence</code>, the item is an
   * <code>XQResultItem</code>.
   * In the case of forward only sequences, this method or any other 
   * get or write method may only be called once on the curent item. <p>
   *
   * The <code>XQItem</code> object is dependent on the sequence from which
   * it was created and is only valid for the duration of <code>XQSequence</code>
   * lifetime. Thus, the <code>XQSequence</code> is closed, this <code>XQItem</code>
   * object will be implicitly closed and it can no longer be used.
   * 
   * @return                    an <code>XQItem</code> object
   * @exception XQException     if (1) there are errors retrieving the item,
   *                            or (2) in the case of a forward only sequence,
   *                            a get or write method has already been invoked
   *                            on the current item. 
   */
  public XQItem getItem() throws XQException;

  /**
   * Read the entire sequence starting from the current position as an
   * <code>XMLStreamReader</code> object, as described in 
   * <i>Section 12.1 Serializing an XDM instance into a StAX event stream
   * (XMLStreamReader), XQuery API for Java (XQJ) 1.0</i>.
   *
   * Note that the serialization process might fail, in which case a 
   * <code>XQException</code> is thrown.
   * 
   * While the stream is being read, the application MUST NOT do any other
   * concurrent operations on the sequence. The operation on the stream is
   * undefined if the underlying sequence position or state is changed by
   * concurrent operations.
   * 
   * After all items are written to the stream, the current position of the
   * cursor is set to point after the last item.
   * 
   * Also, in the case of forward only sequences, this method may only be
   * called if the current item has not yet been read through any of the get
   * or write methods.
   *
   * @return                    an XML reader object as <code>XMLStreamReader</code>
   * @exception XQException     if (1) there are errors accessing any of the items
   *                            in the sequence, (2) the sequence is in a closed state,
   *                            (3) in the case of a forward only sequence, a get or write
   *                            method has already been invoked on the current item, or 
   *                            (4) in case of an error during serialization of the sequence
   *                            into a StAX event stream as defined in <i>Section 12.1 
   *                            Serializing an XDM instance into a StAX event stream
   *                            (XMLStreamReader), XQuery API for Java (XQJ) 1.0</i>
   */
  public javax.xml.stream.XMLStreamReader getSequenceAsStream()
    throws XQException;

  /**
   * Serializes the sequence starting from the current position to a String
   * according to the <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>.
   *
   * Serialization parameters, which influence how serialization is
   * performed, can be specified. Refer to the 
   * <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>
   * and <i>Section 12 Serialization, XQuery
   *  API for Java (XQJ) 1.0</i> for more information.
   * 
   * Reading the sequence during the serialization process performs implicit
   * next operations to read the items. 
   *
   * After all items are written to the stream, the current position of the
   * cursor is set to point after the last item.
   *
   * Also, in the case of forward only sequences, this method may only be
   * called if the current item has not yet been read through any of the get
   * or write methods. 
   * 
   * @param props               specifies the serialization parameters,
   *                            <code>null</code> is considered equivalent to an
   *                            empty <code>Properties</code> object
   * @return                    the serialized representation of the sequence
   * @exception XQException     if (1) there are errors accessing the items in 
   *                            the sequence, (2) there are errors
   *                            during serialization, (3) the sequence is in a closed state,
   *                            or (4) in the case of a forward only sequence, a get or
   *                            write method has already been invoked on the current item
   */
  public java.lang.String getSequenceAsString(java.util.Properties props) 
                 throws XQException;

  /** 
   * Checks if the current position is after the last item in the sequence.
   * Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   * @return                    <code>true</code> if the current position is
   *                            after the last item, <code>false</code> otherwise 
   * @exception XQException     if (1) the sequence is forward only, 
   *                            or (2) the sequence is in a closed state
   */
  public boolean isAfterLast() throws XQException;

  /** 
   * Checks if the current position before the first item in the sequence.
   * Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   * @return                    <code>true</code> if the current position is
   *                            before the first item, <code>false</code> otherwise 
   * @exception XQException     if (1) the sequence is forward only, 
   *                            or (2) the sequence is in a closed state
   */
  public boolean isBeforeFirst() throws XQException;

  /** 
   * Checks if the current position at the first item in the sequence.
   * Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   * @return                    <code>true</code> if the current position is at
   *                            the first item, <code>false</code> otherwise
   * @exception XQException     if (1) the sequence is forward only, 
   *                            or (2) the sequence is in a closed state
   */
  public boolean isFirst() throws XQException;

  /** 
    * Checks if the current position at the last item in the sequence.
    * Calling this method on an empty sequence 
    * will return <code>false</code>.
    *
    * @return                    <code>true</code> if the current position is at
    *                            the last item, <code>false</code> otherwise 
    * @exception XQException     if (1) the sequence is forward only,
    *                            or (2) the sequence is in a closed state
    */
  public boolean isLast() throws XQException;

  /** 
   * Moves to the last item in the sequence. This method returns
   * <code>true</code>, if it was able to move to the last item in the sequence
   * <code>false</code>, otherwise. Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   * @return                    <code>true</code> if the sequence was positioned
   *                            on the last item, <code>false</code> otherwise
   * @exception XQException     if (1) the sequence is forward only,
   *                            or (2) the sequence is in a closed state
   */
  public boolean last() throws XQException;

  /**
   * Moves to the next item in the sequence.  Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   * @return                    <code>true</code> if the new item is valid,
   *                            <code>false</code> if there are no more items 
   * @exception XQException     if the sequence is in a closed state
   */
  public boolean next() throws XQException;

  /** 
   * Moves to the previous item in the sequence.  Calling this method on an empty sequence 
   * will return <code>false</code>.
   *
   * @return                    <code>true</code> if the new current position is
   *                            within the sequence, (i.e., not before first);
   *                            <code>false</code> otherwise. 
   * @exception XQException     if (1) the sequence is forward only, or
   *                            (2) the sequence is in a closed state.
   */
  public boolean previous() throws XQException;   // only if scrollable

  /**
   * Moves the cursor a relative number of items, either positive or negative. 
   * Attempting to move beyond the first/last item in the sequence positions 
   * the sequence before/after the the first/last item.  
   * Calling <code>relative(0)</code> is valid, but does not change the cursor position.  
   * <p>
   * Note: Calling the method <code>relative(1)</code> is identical to calling the method 
   * <code>next</code> and calling the method <code>relative(-1)</code> is identical
   * to calling the method <code>previous()</code>. 
   * Calling this method on an empty sequence will return <code>false</code>.
   * 
   * @param itempos             the item position to jump to
   * @return                    <code>true</code> if the new current position is within
   *                            the sequence (i.e., not before first or after last);
   *                            <code>false</code> otherwise.
   * @exception XQException     if (1) the sequence is forward only, or
   *                            (2) the sequence is in a closed state.
   */
  public boolean relative (int itempos) throws XQException;

  /**
   * Serializes the sequence starting from the current position to an
   * <code>OutputStream</code> according to the 
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
   * Reading the sequence during the serialization process performs implicit
   * next operations to read the items. 
   *
   * After all items are written to the stream, the current position of the
   * cursor is set to point after the last item.
   *
   * Also, in the case of forward only sequences, this method may only be
   * called if the current item has not yet been read through any of the
   * get or write methods. 
   * 
   * @param os                  the output stream into which the sequence is to be serialized 
   * @param props               specifies the serialization parameters,
   *                            <code>null</code> is considered equivalent to an empty
   *                            <code>Properties</code> object
   * @exception XQException     if (1) there are errors accessing the items in 
   *                            the sequence, (2) there are errors
   *                            during serialization, (3) the sequence is in a closed state,
   *                            (4) in the case of a forward only sequence, a get or
   *                            write method has already been invoked on the current item,
   *                            or (5) the <code>os</code> parameter is <code>null</code>
   */
  public void writeSequence(java.io.OutputStream os, java.util.Properties props)
       throws XQException;

  /**
   * Serializes the sequence starting from the current position to a Writer
   * according to the  <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
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
   * <br>
   * <br> 
   *
   * Reading the sequence during the serialization process performs implicit
   * next operations to read the items. 
   *
   * After all items are written to the stream, the current position of the
   * cursor is set to point after the last item.
   *
   * Also, in the case of forward only sequences, this method may only be
   * called if the current item has not yet been read through any of the get
   * or write methods. 
   *
   * @param ow                  the writer object into which the sequence is to be serialized 
   * @param props               specifies the serialization parameters,
   *                            <code>null</code> is considered equivalent to an empty
   *                            <code>Properties</code> object
   * @exception XQException     if (1) there are errors accessing the items in 
   *                            the sequence, (2) there are errors during serialization,
   *                            (3) the sequence is in a closed state,
   *                            (4) in the case of a forward only sequence, a get or
   *                            write method has already been invoked on the current item,
   *                            or (5) the <code>ow</code> parameter is <code>null</code>
   */
  public void writeSequence(java.io.Writer ow, java.util.Properties props)
       throws XQException;

  /**
   * Writes the entire sequence starting from the current position to a SAX
   * handler, as described in <i>Section 12.2 Serializing an XDM instance
   * into a SAX event stream, XQuery API for Java (XQJ) 1.0</i>.
   *
   * Note that the serialization process might fail, in which case a 
   * <code>XQException</code> is thrown.  
   *
   * After all items are written to the stream, the current position of the
   * cursor is set to point after the last item.
   *
   * Also, in the case of forward only sequences, this method may only be called
   * if the current item has not yet been read through any of the get or write
   * methods.
   *
   * The specified <code>org.xml.sax.ContentHandler</code> can optionally implement the
   * <code>org.xml.sax.LexicalHandler</code> interface. An implementation must check if the
   * specified <code>ContentHandler</code> implements <code>LexicalHandler</code>.
   * If the handler is a <code>LexicalHandler</code> comment nodes are reported, otherwise
   * they will be silently ignored.
   *
   * @param saxhdlr             the SAX content handler, optionally a lexical handler
   * @exception XQException     if (1) there are errors accessing any of the items
   *                            in the sequence, (2) the sequence is in a closed 
   *                            state, (3) in the case of a forward only sequence, a get or
   *                            write method has already been invoked on the current item, 
   *                            (4) in case of an error during serializing the XDM instance
   *                            into a SAX event stream, or (5) the <code>saxhdlr</code>
   *                            parameter is <code>null</code>
   */
   public void writeSequenceToSAX(org.xml.sax.ContentHandler saxhdlr)
        throws XQException;
  /**
   * Writes the entire sequence starting from the current position to a
   * <code>Result</code>. First the sequence is normalized as described in
   * <a href="http://www.w3.org/TR/xslt-xquery-serialization/">
   * <i>XSLT 2.0 and XQuery 1.0 serialization</i></a>.  Subsequently it is serialized
   * to the <code>Result</code> object.<br>
   *
   * Note that the normalization process can fail, in which case an
   * <code>XQException</code> is thrown. 
   *
   * An XQJ implementation must at least support the following
   * implementations:
   * <br>
   * <ul>
   *   <li><code>javax.xml.transform.dom.DOMResult</code></li>
   *   <li><code>javax.xml.transform.sax.SAXResult</code></li>
   *   <li><code>javax.xml.transform.stream.StreamResult</code></li>
   * </ul>
   * <br>
   * 
   * @param result              the result object into which the sequence
   *                             is to be serialized
   * @exception XQException      if (1) there are errors accessing any of the items
   *                             in the sequence, (2) the sequence is in a closed 
   *                             state, (3) in the case of a forward only sequence, a get or
   *                             write method has already been invoked on the current item, 
   *                             (4) in case of an error while serializing the sequence
   *                             into the <code>Result</code> object, or (5) the
   *                             <code>result</code> parameter is <code>null</code>
   */
   public void writeSequenceToResult(javax.xml.transform.Result result) throws XQException;
   
};

