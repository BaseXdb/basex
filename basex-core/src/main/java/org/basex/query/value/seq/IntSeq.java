package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Int xs:integer}, containing at least two of them.
 *
 * @author BaseX Team 2005-18, BSD License
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
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof IntSeq)) return super.equals(obj);
    final IntSeq is = (IntSeq) obj;
    return type == is.type && Arrays.equals(values, is.values);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final long[] tmp = new long[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp, type);
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
        final int[] t3 = new int[(int) size];
        for(int s = 0; s < size; s++) t3[s] = (int) values[s];
        return t3;
      default:
        return values;
    }
  }

  /**
   * Returns the internal values.
   * @return values
   */
  public long[] values() {
    return values;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @param type type
   * @return value
   */
  public static Value get(final long[] values, final Type type) {
    return values.length == 0 ? Empty.SEQ : values.length == 1 ? Int.get(values[0], type) :
      new IntSeq(values, type);
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
    for(final Value value : values) {
      // speed up construction, depending on input
      final int vs = (int) value.size();
      if(value instanceof IntSeq) {
        final IntSeq seq = (IntSeq) value;
        System.arraycopy(seq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = value.itemAt(v).itr(null);
      }
    }
    return get(tmp, type);
  }
}
