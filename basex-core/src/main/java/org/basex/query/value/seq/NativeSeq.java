package org.basex.query.value.seq;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Sequence of items, which are stored in their primitive/native representation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class NativeSeq extends Seq {
  /**
   * Constructor.
   * @param size number of items
   * @param type item type
   */
  NativeSeq(final int size, final Type type) {
    super(size, type);
  }

  @Override
  public final void cache(final boolean lazy, final InputInfo ii) { }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) {
    return this;
  }

  @Override
  public final boolean sameType() {
    return true;
  }

  @Override
  public void refineType() { }
}
