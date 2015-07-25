package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Int xs:integer}, containing at least two of them.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class IntSeq extends NativeSeq {
  /** Values. */
  private final long[] values;

  /**
   * Constructor.
   * @param values values
   * @param type type
   */
  private IntSeq(final long[] values, final Type type) {
    super(values.length, type);
    this.values = values;
  }

  @Override
  public Int itemAt(final long pos) {
    return Int.get(values[(int) pos], type);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof IntSeq)) return false;
    final IntSeq is = (IntSeq) cmp;
    return type == is.type && Arrays.equals(values, is.values);
  }

  @Override
  public Object toJava() {
    switch((AtomType) type) {
      case BYT:
        final byte[] t1 = new byte[(int) size];
        for(int s = 0; s < size; s++) t1[s] = (byte) values[s];
        return t1;
      case SHR:
      case UBY:
        final short[] t2 = new short[(int) size];
        for(int s = 0; s < size; s++) t2[s] = (short) values[s];
        return t2;
      case INT:
      case USH:
        final short[] t3 = new short[(int) size];
        for(int s = 0; s < size; s++) t3[s] = (short) values[s];
        return t3;
      default:
        return values;
    }
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items
   * @param type type
   * @return value
   */
  public static Value get(final long[] items, final Type type) {
    return items.length == 0 ? Empty.SEQ : items.length == 1 ? Int.get(items[0], type) :
      new IntSeq(items, type);
  }

  /**
   * Creates a sequence with the items in the specified expressions.
   * @param values values
   * @param size size of resulting sequence
   * @param type item type
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Value[] values, final int size, final Type type)
      throws QueryException {

    final long[] tmp = new long[size];
    int t = 0;
    for(final Value val : values) {
      // speed up construction, depending on input
      final int vs = (int) val.size();
      if(val instanceof IntSeq) {
        final IntSeq sq = (IntSeq) val;
        System.arraycopy(sq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = val.itemAt(v).itr(null);
      }
    }
    return get(tmp, type);
  }
}
