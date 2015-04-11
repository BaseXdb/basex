package org.basex.query.value.seq;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Sequence of items of type {@link Int xs:byte}, containing at least two of them.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BytSeq extends NativeSeq {
  /** Values. */
  private final byte[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private BytSeq(final byte[] values) {
    super(values.length, AtomType.BYT);
    this.values = values;
  }

  @Override
  public Int itemAt(final long pos) {
    return new Int(values[(int) pos], type);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof BytSeq && Token.eq(values, ((BytSeq) cmp).values);
  }

  @Override
  public byte[] toJava() {
    return values;
  }

  @Override
  public Seq insert(final long pos, final Item item) {
    if(!(item instanceof Int && item.type == AtomType.BYT)) return copyInsert(pos, item);
    final int p = (int) pos, n = values.length;
    final byte[] out = new byte[n + 1];
    System.arraycopy(values, 0, out, 0, p);
    out[p] = (byte) ((Int) item).itr();
    System.arraycopy(values, p, out, p + 1, n - p);
    return new BytSeq(out);
  }

  @Override
  public Value remove(final long pos) {
    final int p = (int) pos, n = values.length - 1;
    if(n == 1) return itemAt(1 - pos);
    final byte[] out = new byte[n];
    System.arraycopy(values, 0, out, 0, p);
    System.arraycopy(values, p + 1, out, p, n - p);
    return new BytSeq(out);
  }

  @Override
  public Value reverse() {
    final int s = values.length;
    final byte[] tmp = new byte[s];
    for(int l = 0, r = s - 1; l < s; l++, r--) tmp[l] = values[r];
    return get(tmp);
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items
   * @return value
   */
  public static Value get(final byte[] items) {
    return items.length == 0 ? Empty.SEQ : items.length == 1 ? Int.get(items[0], AtomType.BYT) :
      new BytSeq(items);
  }

  /**
   * Creates a sequence with the items in the specified expressions.
   * @param values values
   * @param size size of resulting sequence
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Value[] values, final int size) throws QueryException {
    final byte[] tmp = new byte[size];
    int t = 0;
    for(final Value val : values) {
      // speed up construction, depending on input
      final int vs = (int) val.size();
      if(val instanceof BytSeq) {
        final BytSeq sq = (BytSeq) val;
        System.arraycopy(sq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = (byte) val.itemAt(v).itr(null);
      }
    }
    return get(tmp);
  }
}
