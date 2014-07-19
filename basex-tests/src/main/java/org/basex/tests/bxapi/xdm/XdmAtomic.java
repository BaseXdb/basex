package org.basex.tests.bxapi.xdm;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.tests.bxapi.*;
import org.basex.util.*;

/**
 * Wrapper for representing XQuery items.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class XdmAtomic extends XdmItem {
  /** Wrapped item. */
  private final Item item;

  /**
   * Constructor.
   * @param item item
   */
  XdmAtomic(final Item item) {
    this.item = item;
  }

  @Override
  public boolean getBoolean() {
    try {
      return item.bool(null);
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  @Override
  public String getString() {
    try {
      return Token.string(item.string(null));
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  @Override
  public SeqType getType() {
    return item.seqType();
  }

  @Override
  public Item internal() {
    return item;
  }

  @Override
  public String toString() {
    return item.toString();
  }
}
