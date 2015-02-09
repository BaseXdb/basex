package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Int xs:float}, containing at least two of them.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FltSeq extends NativeSeq {
  /** Values. */
  private final float[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private FltSeq(final float[] values) {
    super(values.length, AtomType.FLT);
    this.values = values;
  }

  @Override
  public Flt itemAt(final long pos) {
    return Flt.get(values[(int) pos]);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof FltSeq && Arrays.equals(values, ((FltSeq) cmp).values);
  }

  @Override
  public float[] toJava() {
    return values;
  }

  @Override
  public Value reverse() {
    final int s = values.length;
    final float[] tmp = new float[s];
    for(int l = 0, r = s - 1; l < s; l++, r--) tmp[l] = values[r];
    return get(tmp);
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items
   * @return value
   */
  public static Value get(final float[] items) {
    return items.length == 0 ? Empty.SEQ : items.length == 1 ? Flt.get(items[0]) :
      new FltSeq(items);
  }

  /**
   * Creates a sequence with the items in the specified expressions.
   * @param values values
   * @param size size of resulting sequence
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Value[] values, final int size) throws QueryException {
    final float[] tmp = new float[size];
    int t = 0;
    for(final Value val : values) {
      // speed up construction, depending on input
      final int vs = (int) val.size();
      if(val instanceof FltSeq) {
        final FltSeq sq = (FltSeq) val;
        System.arraycopy(sq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = val.itemAt(v).flt(null);
      }
    }
    return get(tmp);
  }
}
