package org.basex.query.util.list;

import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for values.
 *
 * @author BaseX Team 2005-21, BSD License
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

  /**
   * Creates an XQuery array from the contents of this list.
   * @return the array
   */
  public XQArray array() {
    final ArrayBuilder builder = new ArrayBuilder();
    for(int i = 0; i < size; i++) builder.append(list[i]);
    return builder.freeze();
  }

  @Override
  protected Value[] newArray(final int s) {
    return new Value[s];
  }
}
