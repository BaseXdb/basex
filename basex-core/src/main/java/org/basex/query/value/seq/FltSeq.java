package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Int xs:float}, containing at least two of them.
 *
 * @author BaseX Team 2005-17, BSD License
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
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof FltSeq ? Arrays.equals(values, ((FltSeq) obj).values) :
      super.equals(obj));
  }

  @Override
  public Value insert(final long pos, final Item val) {
    if(val.type != type) return copyInsert(pos, val);
    final int sz = (int) size, ps = (int) pos;
    final float[] tmp = new float[sz + 1];
    System.arraycopy(values, 0, tmp, 0, ps);
    System.arraycopy(values, ps, tmp, ps + 1, sz - ps);
    tmp[ps] = ((Flt) val).flt();
    return get(tmp);
  }

  @Override
  public Value remove(final long pos) {
    final int sz = (int) size - 1, ps = (int) pos;
    final float[] tmp = new float[sz];
    System.arraycopy(values, 0, tmp, 0, ps);
    System.arraycopy(values, ps + 1, tmp, ps, sz - ps);
    return get(tmp);
  }

  @Override
  public Value reverse() {
    final int sz = (int) size;
    final float[] tmp = new float[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public float[] toJava() {
    return values;
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
