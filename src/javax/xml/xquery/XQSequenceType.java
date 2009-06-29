/*
 * Copyright � 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/** 
  * The <code>XQSequenceType</code> interface represents a sequence type as
  * <a href="http://www.w3.org/TR/xquery"><i>XQuery 1.0: An XML Query language</i></a>.
  * 
  * The <code>XQSequenceType</code> is the base interface for the 
  * <code>XQItemType</code> interface and contains an occurence indicator. 
  */
public interface XQSequenceType 
{
  /** Zero or one occurrences. */
  public static final int OCC_ZERO_OR_ONE  = 1;  // same as ? 
  /** Exactly one occurrence. */
  public static final int OCC_EXACTLY_ONE  = 2;  
  /** Zero or more occurrences. */
  public static final int OCC_ZERO_OR_MORE = 3;  // same as * 
  /** One or more occurrences. */
  public static final int OCC_ONE_OR_MORE  = 4;  // same as + 
  /** Empty sequence. */
  public static final int OCC_EMPTY = 5;         // empty sequence

  /**
    * Returns the type of the item in the sequence type.
    *
    * @return                    <code>XQItemType</code> representing the
    *                            item type in the sequence. <code>null</code>
    *                            is returned in case of an empty sequence.
    */
  public XQItemType getItemType();

  /**
    * Returns the occurrence indicator for the sequence type. 
    * One of:
    *
    * <p>
    * <table>
    * <tr><th>Description</th>
    *     <th>Value</th></tr>
    * <tr><td>Zero or one</td>
    *     <td><code>OCC_ZERO_OR_ONE</code></td></tr>
    * <tr><td>Exactly one</td>
    *     <td><code>OCC_EXACTLY_ONE</code></td></tr>
    * <tr><td>Zero or more</td>
    *     <td><code>OCC_ZERO_OR_MORE</code></td></tr>
    * <tr><td>One or more</td>
    *     <td><code>OCC_ONE_OR_MORE</code></td></tr>
    * <tr><td>Empty </td>
    *     <td><code>OCC_EMPTY</code></td></tr>
    * </table>
    * <br>
    *
    * @return                    <code>int</code> indicating the occurrence indicator
    */
  public int getItemOccurrence();

  /**
    * Returns a human-readable implementation-defined string representation of
    * the sequence type. 
    *
    * @return                    a <code>String</code> representation of the sequence type
    */
  public String toString();


  /**
    * Compares the specified object with this sequence type for equality. The result
    * is <code>true</code> only if the argument is a sequence type object which
    * represents the same XQuery sequence type.
    *
    * <br>
    * <br>
    *
    * In order to comply with the general contract of <code>equals</code> and
    * <code>hashCode</code> across different implementations the following
    * algorithm must be used.  Return <code>true</code> if and only if both
    * objects are <code>XQsequenceType</code> and:
    *
    * <ul>
    *    <li><code>getItemOccurrence()</code> is equal</li>
    *    <li>if not <code>OCC_EMPTY</code>, <code>getItemType()</code>
    *        is equal</li>
    * </ul>
    *
    * @param o                an <code>XQItemType</code> object representing an XQuery
    *                         sequence type
    * @return                 <code>true</code> if the input item type object represents
    *                         the same XQuery sequence type, <code>false</code> otherwise
    */
  public boolean equals(Object o);

  /**
    * Returns a hash code consistent with the definition of the equals method.
    *
    * <br>
    * <br>
    *
    * In order to comply with the general contract of <code>equals</code> and
    * <code>hashCode</code> across different implementations the following
    * algorithm must be used:
    * <pre>
    *  int hashCode;
    *  if (getItemOccurrence() == XQSequenceType.OCC_EMPTY)
    *  {
    *    hashCode = 1; 
    *  }
    *  else
    *  {
    *    hashCode = getItemOccurrence()*31 + getItemType().hashCode();
    *  }
    * </pre>
    *
    * @return                 hash code for this item type
    */
  public int hashCode();
}
