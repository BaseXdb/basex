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
