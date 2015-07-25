package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Int xs:double}, containing at least two of them.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DblSeq extends NativeSeq {
  /** Values. */
  private final double[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private DblSeq(final double[] values) {
    super(values.length, AtomType.DBL);
    this.values = values;
  }

  @Override
  public Dbl itemAt(final long pos) {
    return Dbl.get(values[(int) pos]);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof DblSeq && Arrays.equals(values, ((DblSeq) cmp).values);
  }

  @Override
  public double[] toJava() {
    return values;
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items
   * @return value
   */
  public static Value get(final double[] items) {
    return items.length == 0 ? Empty.SEQ : items.length == 1 ? Dbl.get(items[0]) :
      new DblSeq(items);
  }

  /**
   * Creates a sequence with the items in the specified expressions.
   * @param values values
   * @param size size of resulting sequence
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Value[] values, final int size) throws QueryException {
    final double[] tmp = new double[size];
    int t = 0;
    for(final Value val : values) {
      // speed up construction, depending on input
      final int vs = (int) val.size();
      if(val instanceof DblSeq) {
        final DblSeq sq = (DblSeq) val;
        System.arraycopy(sq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = val.itemAt(v).dbl(null);
      }
    }
    return get(tmp);
  }
}
