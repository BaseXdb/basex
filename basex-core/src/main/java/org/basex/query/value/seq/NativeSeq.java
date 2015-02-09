package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * Sequence of items, which are stored in their primitive/native representation.
 *
 * @author BaseX Team 2005-15, BSD License
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
  public final int writeTo(final Item[] arr, final int index) {
    final int w = Math.min((int) size, arr.length - index);
    for(int i = 0; i < w; i++) arr[index + i] = itemAt(i);
    return w;
  }

  @Override
  public final boolean homogeneous() {
    return true;
  }

  @Override
  public final Value materialize(final InputInfo ii) {
    return this;
  }

  @Override
  public Value atomValue(final InputInfo ii) throws QueryException {
    return this;
  }

  @Override
  public final long atomSize() {
    return size;
  }

  @Override
  public final SeqType seqType() {
    return SeqType.get(type, Occ.ONE_MORE);
  }
}
