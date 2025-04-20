package org.basex.query.value.array;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * A builder for creating an {@link XQArray} with single-item members.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ItemArrayBuilder implements ArrBuilder {
  /** Value builder. */
  private final ValueBuilder vb;

  /**
   * Constructor.
   * @param qc query context (required for interrupting running queries)
   * @param capacity initial capacity ({@link Integer#MIN_VALUE}: create no compact data structures)
   */
  public ItemArrayBuilder(final QueryContext qc, final long capacity) {
    vb = new ValueBuilder(qc, capacity);
  }

  @Override
  public ArrBuilder add(final Value value) {
    if(value.isItem()) {
      vb.add(value);
      return this;
    }
    final TreeArrayBuilder ab = new TreeArrayBuilder();
    for(final Value member : vb.value()) ab.add(member);
    return ab.add(value);
  }

  @Override
  public XQArray array(final ArrayType type) {
    return new ItemArray(vb.value());
  }
}
