package org.basex.tests.w3c.qt3api;

import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.util.Token;

/**
 * Wrapper for representing XQuery items.
 */
class XQAtomic extends XQItem {
  /** Wrapped item. */
  private final Item item;

  /**
   * Constructor.
   * @param it item
   */
  XQAtomic(final Item it) {
    item = it;
  }

  @Override
  public boolean getBoolean() {
    try {
      return item.bool(null);
    } catch(final QueryException ex) {
      throw new XQException(ex);
    }
  }

  @Override
  public String getString() {
    try {
      return Token.string(item.string(null));
    } catch(final QueryException ex) {
      throw new XQException(ex);
    }
  }

  @Override
  public SeqType getType() {
    return item.type();
  }

  @Override
  public String toString() {
    return item.toString();
  }

  // PACKAGE PROTECTED METHODS ================================================

  @Override
  Item internal() {
    return item;
  }
}
