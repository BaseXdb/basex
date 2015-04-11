package org.basex.query.value.seq;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Int xs:decimal}, containing at least two of them.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DecSeq extends NativeSeq {
  /** Values. */
  private final BigDecimal[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private DecSeq(final BigDecimal[] values) {
    super(values.length, AtomType.DEC);
    this.values = values;
  }

  @Override
  public Dec itemAt(final long pos) {
    return Dec.get(values[(int) pos]);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof DecSeq && Arrays.equals(values, ((DecSeq) cmp).values);
  }

  @Override
  public BigDecimal[] toJava() {
    return values;
  }

  @Override
  public Seq insert(final long pos, final Item item) {
    if(!(item instanceof Dec)) return copyInsert(pos, item);
    final int p = (int) pos, n = values.length;
    final BigDecimal[] out = new BigDecimal[n + 1];
    System.arraycopy(values, 0, out, 0, p);
    out[p] = ((Dec) item).dec(null);
    System.arraycopy(values, p, out, p + 1, n - p);
    return new DecSeq(out);
  }

  @Override
  public Value remove(final long pos) {
    final int p = (int) pos, n = values.length - 1;
    if(n == 1) return itemAt(1 - pos);
    final BigDecimal[] out = new BigDecimal[n];
    System.arraycopy(values, 0, out, 0, p);
    System.arraycopy(values, p + 1, out, p, n - p);
    return new DecSeq(out);
  }

  @Override
  public Value reverse() {
    final int s = values.length;
    final BigDecimal[] tmp = new BigDecimal[s];
    for(int l = 0, r = s - 1; l < s; l++, r--) tmp[l] = values[r];
    return get(tmp);
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items
   * @return value
   */
  private static Value get(final BigDecimal[] items) {
    return items.length == 0 ? Empty.SEQ : items.length == 1 ? Dec.get(items[0]) :
      new DecSeq(items);
  }

  /**
   * Creates a sequence with the items in the specified expressions.
   * @param values values
   * @param size size of resulting sequence
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Value[] values, final int size) throws QueryException {
    final BigDecimal[] tmp = new BigDecimal[size];
    int t = 0;
    for(final Value val : values) {
      // speed up construction, depending on input
      final int vs = (int) val.size();
      if(val instanceof DecSeq) {
        final DecSeq sq = (DecSeq) val;
        System.arraycopy(sq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = val.itemAt(v).dec(null);
      }
    }
    return get(tmp);
  }
}
