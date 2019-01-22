package org.basex.query.util.list;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for values.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * Constructor, specifying an initial array capacity.
   * @param capacity array capacity (can be negative)
   * @throws QueryException query exception
   */
  public ValueList(final long capacity) throws QueryException {
    this(capacity(capacity));
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
  protected Value[] newList(final int s) {
    return new Value[s];
  }

  /**
   * Returns the initial array capacity.
   * @param capacity specified capacity
   * @return real capacity
   * @throws QueryException query exception
   */
  public static int capacity(final long capacity) throws QueryException {
    if(capacity > Integer.MAX_VALUE - 2) throw RANGE_X.get(null, capacity);
    return Math.min(org.basex.util.Array.MAXINIT, Math.max(1, (int) capacity));
  }
}
