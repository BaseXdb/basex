package org.basex.query.value.seq;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.util.*;

/**
 * Sequence of items, which are stored in their primitive/native representation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class NativeSeq extends Seq {
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
    throw CONDTYPE.get(ii, this);
  }

  @Override
  public final int writeTo(final Item[] arr, final int start) {
    final int w = Math.min((int) size, arr.length - start);
    for(int i = 0; i < w; i++) arr[start + i] = itemAt(i);
    return w;
  }

  @Override
  public final boolean homogeneous() {
    return true;
  }

  @Override
  public final SeqType type() {
    return SeqType.get(type, Occ.ONE_MORE);
  }
}
