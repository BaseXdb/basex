package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Int xs:integer}, containing at least two of them.
 *
 * @author BaseX Team 2005-21, BSD License
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
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final long[] tmp = new long[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp, type);
  }

  /**
   * Returns the internal values.
   * @return values
   */
  public long[] values() {
    return values;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    if(mode == Simplify.DISTINCT) {
      final long[] tmp = new LongList((int) size).add(values).sort().distinct().finish();
      final int tl = tmp.length;
      if(seqType().type == AtomType.INTEGER) {
        // try to rewrite to range sequence
        int t = 0;
        while(++t < tl && tmp[0] + t == tmp[t]);
        if(t == tl) return cc.replaceWith(this, RangeSeq.get(tmp[0], tl, true));
      }
      // replace with new, sorted (possibly smaller) sequence
      return cc.replaceWith(this, get(tmp, type));
    }
    return super.simplifyFor(mode, cc);
  }

  @Override
  public Object toJava() {
    switch((AtomType) type) {
      case BYTE:
        final byte[] t1 = new byte[(int) size];
        for(int s = 0; s < size; s++) t1[s] = (byte) values[s];
        return t1;
      case SHORT:
      case UNSIGNED_BYTE:
        final short[] t2 = new short[(int) size];
        for(int s = 0; s < size; s++) t2[s] = (short) values[s];
        return t2;
      case INT:
      case UNSIGNED_SHORT:
        final int[] t3 = new int[(int) size];
        for(int s = 0; s < size; s++) t3[s] = (int) values[s];
        return t3;
      default:
        return values;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof IntSeq)) return super.equals(obj);
    final IntSeq is = (IntSeq) obj;
    return type == is.type && Arrays.equals(values, is.values);
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates an xs:integer sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final int[] values) {
    final LongList list = new LongList(values.length);
    for(final int value : values) list.add(value);
    return get(list.finish(), AtomType.INTEGER);
  }

  /**
   * Creates an xs:integer sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final long[] values) {
    return get(values, AtomType.INTEGER);
  }

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @param type type
   * @return value
   */
  public static Value get(final long[] values, final Type type) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Int.get(values[0], type) : new IntSeq(values, type);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param type item type
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  static Value get(final Type type, final int size, final Value... values) throws QueryException {
    final LongList tmp = new LongList(size);
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof IntSeq) {
        tmp.add(((IntSeq) value).values);
      } else {
        for(final Item item : value) tmp.add(item.itr(null));
      }
    }
    return get(tmp.finish(), type);
  }
}
