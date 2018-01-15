package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Sequence of items of type {@link Int xs:byte}, containing at least two of them.
 *
 * @author BaseX Team 2005-18, BSD License
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
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof BytSeq ? Arrays.equals(values, ((BytSeq) obj).values) :
      super.equals(obj));
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final byte[] tmp = new byte[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public byte[] toJava() {
    return values;
  }

  // STATIC METHODS =====================================================================

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final byte[] values) {
    return values.length == 0 ? Empty.SEQ : values.length == 1 ? Int.get(values[0], AtomType.BYT) :
      new BytSeq(values);
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
    for(final Value value : values) {
      // speed up construction, depending on input
      final int vs = (int) value.size();
      if(value instanceof BytSeq) {
        final BytSeq seq = (BytSeq) value;
        System.arraycopy(seq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = (byte) value.itemAt(v).itr(null);
      }
    }
    return get(tmp);
  }
}
