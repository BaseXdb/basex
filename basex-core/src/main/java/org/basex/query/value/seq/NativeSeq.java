package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Sequence of items, which are stored in their primitive/native representation.
 *
 * @author BaseX Team 2005-21, BSD License
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
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw EBV_X.get(ii, this);
  }

  @Override
  public final void cache(final boolean lazy, final InputInfo ii) { }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) {
    return this;
  }

  @Override
  public final long atomSize() {
    return size;
  }

  /**
   * {@inheritDoc}
   * Because this function will mostly be invoked recursively, the standard implementation
   * will be called, because its runtime outweighs the possibly higher memory consumption.
   */
  @Override
  public final Value insert(final long pos, final Item item, final QueryContext qc) {
    return copyInsert(pos, item, qc);
  }

  /**
   * {@inheritDoc}
   * Because this function will mostly be invoked recursively, the standard implementation
   * will be called, because its runtime outweighs the possibly higher memory consumption.
   */
  @Override
  public final Value remove(final long pos, final QueryContext qc) {
    return copyRemove(pos, qc);
  }
}
