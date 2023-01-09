package org.basex.query.util.list;

import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for values.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ValueList extends ObjectList<Value, ValueList> {
  /**
   * Default constructor.
   */
  public ValueList() {
    this(1);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public ValueList(final long capacity) {
    super(new Value[Array.checkCapacity(capacity)]);
  }

  @Override
  protected Value[] newArray(final int s) {
    return new Value[s];
  }
}
