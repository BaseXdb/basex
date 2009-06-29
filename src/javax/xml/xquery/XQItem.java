/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/** 
 * This interface represents an item in the XDM.
 */
public interface XQItem extends XQItemAccessor
{

 /**
  * Close the item and release all the resources associated with this item. 
  * No method other than the <code>isClosed</code> or <code>close</code> method
  * may be called once the item is closed.
  * Calling close on an <code>XQItem</code> object that is already closed has
  * no effect.
  * 
  * @exception XQException     if there is an error during closing the item
  */
  public void close() throws XQException;

 /**
  * Checks if the item is closed.
  *
  * @return                     <code>boolean</code> <code>true</code> if the item 
  *                             is in a closed state, <code>false</code> otherwise
  */
  public boolean isClosed();
}
