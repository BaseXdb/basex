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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class NativeSeq extends Seq {
  /**
   * Constructor.
   * @param s number of items
   * @param t item type
   */
  protected NativeSeq(final int s, final Type t) {
    super(s, t);
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) throws QueryException {
    throw CONDTYPE.thrw(ii, this);
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
