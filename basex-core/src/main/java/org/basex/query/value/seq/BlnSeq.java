package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Bln xs:boolean}, containing at least two of them.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BlnSeq extends NativeSeq {
  /** Values. */
  private final boolean[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private BlnSeq(final boolean[] values) {
    super(values.length, AtomType.BLN);
    this.values = values;
  }

  @Override
  public Bln itemAt(final long pos) {
    return Bln.get(values[(int) pos]);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof BlnSeq && Arrays.equals(values, ((BlnSeq) cmp).values);
  }

  @Override
  public boolean[] toJava() {
    return values;
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items
   * @return value
   */
  public static Value get(final boolean[] items) {
    return items.length == 0 ? Empty.SEQ : items.length == 1 ? Bln.get(items[0]) :
      new BlnSeq(items);
  }

  /**
   * Creates a sequence with the items in the specified expressions.
   * @param values values
   * @param size size of resulting sequence
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Value[] values, final int size) throws QueryException {
    final boolean[] tmp = new boolean[size];
    int t = 0;
    for(final Value val : values) {
      // speed up construction, depending on input
      final int vs = (int) val.size();
      if(val instanceof BlnSeq) {
        final BlnSeq sq = (BlnSeq) val;
        System.arraycopy(sq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = val.itemAt(v).bool(null);
      }
    }
    return get(tmp);
  }
}
