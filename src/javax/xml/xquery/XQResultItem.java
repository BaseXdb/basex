/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/** 
 * This interface represents an immutable item object obtained from an 
 * <code>XQResultSequence</code> using the <code>getItem</code> method.
 * <p>
 * 
 * A forward only result sequence does not support calling the getter methods
 * multiple times on the same item. To work around this case, 
 * the <code>getItem</code> method can be used to obtain a result item and then 
 * getter methods may be called multiple times on this item. <p>
 * 
 * The <code>XQResultItem</code> object is dependent on 
 * the connection, expression and the sequence from which it
 * was created and is only valid for the duration of those objects. 
 * Thus, if any one of those objects is closed, this <code>XQResultItem</code> object 
 * will be implicitly closed, and it can no longer be used.
 * Similarly  re-executing the expression also implicitly closes the 
 * associated result sequences, which in turn implicitly closes this result item. <p>
 *
 * An XQJ driver is not required to provide finalizer methods for 
 * the connection and other objects. Hence it is strongly recommended that 
 * users call close method explicitly to free any resources. It is also 
 * recommended that they do so under a final block to ensure that the object
 * is closed even when there are exceptions. 
 * Not closing this object implicitly or explicitly might result in serious memory 
 * leaks.<p>
 *
 * Example -
 * <pre>
 *
 *  XQPreparedExpression expr = conn.prepareExpression("for $i ..");
 *  XQResultSequence result = expr.executeQuery();
 *
 *  // posititioned before the first item
 *  while (result.next())
 *  {
 *    XQResultItem item = result.getItem();
 *    // perform multiple gets on this item 
 *    // get DOM
 *    org.w3.dom.Node node = item.getNode(); 
 *    // get SAX
 *    item.writeItemToSAX(saxHandler);
 * 
 *    item.close();  // good practice.  Item will get implicitly closed
 *                   // when the expression,  connection or sequence is closed.
 *  }
 *
 *  result.close(); // explicitly close the result sequence
 *
 * </pre>
 * 
 */
public interface XQResultItem extends XQItem 
{
  /**
    * Gets the XQuery connection associated with this result item 
    *
    * @return                    the connection associated with this result item
    * @exception XQException     if the result item is in a closed state
    */
  public XQConnection getConnection() throws XQException;
};
