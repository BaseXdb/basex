/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/** 
 * This interface represents a sequence of items obtained as a result of 
 * evaluation XQuery expressions. The result sequence is tied to the 
 * <code>XQconnection</code> object on which the expression was evaluated.<p>
 * 
 * This sequence can be obtained by performing an <code>executeQuery</code> 
 * on the expression object. It represents a cursor-like interface.<p>
 * 
 * The <code>XQResultSequence</code> object is dependent on 
 * the connection and the expression from which it was created and is only
 * valid for the duration of those objects.  Thus, if any one of those objects is closed, 
 * this <code>XQResultSequence</code> object  will be implicitly closed
 * and it can no longer be used. Similarly  re-executing the expression
 * also implicitly closes the associated result sequences. <p>
 *
 * An XQJ driver is not required to provide finalizer methods for 
 * the connection and other objects. Hence it is strongly recommended that 
 * users call close method explicitly to free any resources. It is also 
 * recommended that they do so under a final block to ensure that the object
 * is closed even when there are exceptions. 
 * Not closing this object implicitly or explicitly might result in serious memory 
 * leaks.<p>
 *
 * When the <code>XQResultSequence</code> is closed any 
 * <code>XQResultItem</code> objects obtained from it
 * are also implicitly closed.<p>
 *
 * Example -
 * <pre>
 * 
 *  XQPreparedExpression expr = conn.prepareExpression("for $i ..");
 *  XQResultSequence result = expr.executeQuery();
 * 
 *  // create the ItemTypes for string and integer
 *  XQItemType strType = conn.createAtomicType(XQItemType.XQBASETYPE_STRING);
 *  XQItemType intType = conn.createAtomicType(XQItemType.XQBASETYPE_INT);
 *
 *  // posititioned before the first item
 *  while (result.next())
 *  {
 *    XQItemType type = result.getItemType();
 * 
 *    // If string, then get the string value out
 *    if (type.equals(strType))
 *      String str = result.getAtomicValue();
 *    else if (type.equals(intType))  // if it is an integer..
 *      int intval = result.getInt();
 *      ...
 *  }
 * 
 *  result.close(); // explicitly close the result sequence
 * 
 * </pre>
 */
public interface XQResultSequence extends XQSequence 
{

  /** 
    * Gets the XQuery connection associated with this result sequence 
    *
    * @return                   the connection associated with this result sequence
    * @exception XQException    if the result sequence is in a closed state
    */
  public XQConnection getConnection() throws XQException;

}
