package org.basex.query.util.list;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for values.
 *
 * @author BaseX Team 2005-20, BSD License
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
   * Constructor with expected result size.
   * @param size expected result size (ignored if negative)
   * @throws QueryException query exception
   */
  public ValueList(final long size) throws QueryException {
    this(Seq.initialCapacity(size));
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public ValueList(final int capacity) {
    super(new Value[capacity]);
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
