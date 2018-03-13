package org.basex.query.value.seq;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Str xs:string}, containing at least two of them.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class StrSeq extends NativeSeq {
  /** Values. */
  private final byte[][] values;

  /**
   * Constructor.
   * @param values values
   */
  private StrSeq(final byte[][] values) {
    super(values.length, AtomType.STR);
    this.values = values;
  }

  @Override
  public Str itemAt(final long pos) {
    return Str.get(values[(int) pos]);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof StrSeq ? Array.equals(values, ((StrSeq) obj).values) :
      super.equals(obj));
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final byte[][] tmp = new byte[sz][];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public String[] toJava() {
    final String[] tmp = new String[(int) size];
    for(int v = 0; v < size; v++) tmp[v] = Token.string(values[v]);
    return tmp;
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items (will be invalidated by this call)
   * @return value
   */
  public static Value get(final TokenList items) {
    return items.isEmpty() ? Empty.SEQ : items.size() == 1 ? Str.get(items.get(0)) :
      new StrSeq(items.finish());
  }

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final byte[][] values) {
    return values.length == 0 ? Empty.SEQ : values.length == 1 ? Str.get(values[0]) :
      new StrSeq(values);
  }

  /**
   * Creates a sequence with the items in the specified expressions.
   * @param values values
   * @param size size of resulting sequence
   * @return value
   * @throws QueryException query exception
   */
  public static Value get(final Value[] values, final int size) throws QueryException {
    final byte[][] tmp = new byte[size][];
    int t = 0;
    for(final Value value : values) {
      // speed up construction, depending on input
      final int vs = (int) value.size();
      if(value instanceof StrSeq) {
        final StrSeq seq = (StrSeq) value;
        System.arraycopy(seq.values, 0, tmp, t, vs);
        t += vs;
      } else {
        for(int v = 0; v < vs; v++) tmp[t++] = value.itemAt(v).string(null);
      }
    }
    return get(tmp);
  }
}
