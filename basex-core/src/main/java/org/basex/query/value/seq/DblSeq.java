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

  @Override
  public Seq insert(final long pos, final Item item) {
    if(!(item instanceof Dbl)) return copyInsert(pos, item);
    final int p = (int) pos, n = values.length;
    final double[] out = new double[n + 1];
    System.arraycopy(values, 0, out, 0, p);
    out[p] = (byte) ((Dbl) item).dbl();
    System.arraycopy(values, p, out, p + 1, n - p);
    return new DblSeq(out);
  }

  @Override
  public Value remove(final long pos) {
    final int p = (int) pos, n = values.length - 1;
    if(n == 1) return itemAt(1 - pos);
    final double[] out = new double[n];
    System.arraycopy(values, 0, out, 0, p);
    System.arraycopy(values, p + 1, out, p, n - p);
    return new DblSeq(out);
  }

  @Override
  public Value reverse() {
    final int s = values.length;
    final double[] tmp = new double[s];
    for(int l = 0, r = s - 1; l < s; l++, r--) tmp[l] = values[r];
    return get(tmp);
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
