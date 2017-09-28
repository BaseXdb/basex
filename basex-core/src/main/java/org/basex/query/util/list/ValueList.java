package org.basex.query.util.list;

import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for values.
 *
 * @author BaseX Team 2005-17, BSD License
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
   * Constructor, specifying an initial array capacity.
   * @param capacity array capacity
   */
  public ValueList(final int capacity) {
    super(new Value[capacity]);
  }

  /**
   * Creates an XQuery array from the contents of this list.
   * @return the array
   */
  public Array array() {
    final ArrayBuilder builder = new ArrayBuilder();
    for(int i = 0; i < size; i++) builder.append(list[i]);
    return builder.freeze();
  }

  @Override
  protected Value[] newList(final int s) {
    return new Value[s];
  }
}
