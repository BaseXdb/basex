package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Bln xs:boolean}, containing at least two of them.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class BlnSeq extends NativeSeq {
  /** Values. */
  private final boolean[] values;

  /**
   * Constructor.
   * @param values bytes
   */
  private BlnSeq(final boolean[] values) {
    super(values.length, AtomType.BLN);
    this.values = values;
  }

  @Override
  public Bln itemAt(final long pos) {
    return Bln.get(values[(int) pos]);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof BlnSeq ? Arrays.equals(values, ((BlnSeq) obj).values) :
      super.equals(obj));
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final boolean[] tmp = new boolean[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public boolean[] toJava() {
    return values;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final boolean[] values) {
    return values.length == 0 ? Empty.SEQ : values.length == 1 ? Bln.get(values[0]) :
      new BlnSeq(values);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final int size, final Value... values) throws QueryException {
    final boolean[] tmp = new boolean[size];
    int t = 0;
    for(final Value value : values) {
      // speed up construction, depending on input
      final int vs = (int) value.size();
      if(value instanceof BlnSeq) {
        final BlnSeq seq = (BlnSeq) value;
        System.arraycopy(seq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(final Item item : value) tmp[t++] = item.bool(null);
      }
    }
    return get(tmp);
  }
}
