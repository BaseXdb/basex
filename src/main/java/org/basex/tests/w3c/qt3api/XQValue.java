package org.basex.tests.w3c.qt3api;

import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.util.Compare;
import org.basex.util.Util;

/**
 * Wrapper for representing XQuery values.
 */
public abstract class XQValue implements Iterable<XQItem> {
  /**
   * Returns a new XQuery value.
   * @param val value
   * @return result
   */
  public static XQValue get(final Value val) {
    return val instanceof Empty ? XQEmpty.EMPTY :
        val instanceof Item ? XQItem.get((Item) val) :
      new XQSequence((Seq) val);
  }

  /**
   * Returns the base uri of a node.
   * @return node name
   */
  public String getBaseURI() {
    throw Util.notexpected("Item must be a node.");
  }

  /**
   * Returns the name of a node.
   * @return node name
   */
  public String getName() {
    throw Util.notexpected("Item must be a node.");
  }

  /**
   * Returns the boolean value.
   * @return node name
   */
  public boolean getBoolean() {
    throw Util.notexpected("Value has no boolean representation.");
  }

  /**
   * Returns the integer value.
   * @return node name
   */
  public final long getInteger() {
    try {
      return Long.parseLong(getString());
    } catch(final NumberFormatException ex) {
      throw Util.notexpected("Value has no integer representation.");
    }
  }

  /**
   * Returns the string value.
   * @return node name
   */
  public String getString() {
    throw Util.notexpected("Value has no string representation.");
  }

  /**
   * Returns the number of items stored in the value.
   * @return number of items
   */
  public abstract int size();

  /**
   * Checks if the two values are deep-equal, according to XQuery.
   * @param value second value
   * @return result of check
   * @throws XQException exception
   */
  public boolean deepEqual(final XQValue value) {
    try {
      return Compare.deep(internal(), value.internal(), null);
    } catch(final QueryException ex) {
      throw new XQException(ex);
    }
  }

  /**
   * Returns the value type.
   * @return value type
   */
  public abstract SeqType getType();

  @Override
  public abstract String toString();

  // PACKAGE PROTECTED METHODS ================================================

  /**
   * Returns the internal value representation.
   * Should be made invisible to other packages.
   * @return value
   */
  abstract Value internal();
}
