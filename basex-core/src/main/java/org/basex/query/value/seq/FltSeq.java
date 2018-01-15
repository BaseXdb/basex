package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Int xs:float}, containing at least two of them.
 *
 * @author BaseX Team 2005-18, BSD License
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
  public Value reverse(final QueryContext qc) {
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
   * @param values values
   * @return value
   */
  public static Value get(final float[] values) {
    return values.length == 0 ? Empty.SEQ : values.length == 1 ? Flt.get(values[0]) :
      new FltSeq(values);
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
    for(final Value value : values) {
      // speed up construction, depending on input
      final int vs = (int) value.size();
      if(value instanceof FltSeq) {
        final FltSeq seq = (FltSeq) value;
        System.arraycopy(seq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = value.itemAt(v).flt(null);
      }
    }
    return get(tmp);
  }
}
