package org.basex.tests.bxapi.xdm;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.tests.bxapi.*;
import org.basex.util.*;

/**
 * Wrapper for representing XQuery values.
 */
public abstract class XdmValue implements Iterable<XdmItem> {
  /**
   * Returns a new XQuery value.
   * @param val value
   * @return result
   */
  public static XdmValue get(final Value val) {
    return val instanceof Empty ? XdmEmpty.EMPTY :
        val instanceof Item ? XdmItem.get((Item) val) :
      new XdmSequence((Seq) val);
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
   * @throws XQueryException exception
   */
  public boolean deepEqual(final XdmValue value) {
    try {
      return Compare.deep(internal(), value.internal(), null);
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
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
  public abstract Value internal();
}
