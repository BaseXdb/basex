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
 * @author BaseX Team 2005-21, BSD License
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
    super(values.length, AtomType.STRING);
    this.values = values;
  }

  @Override
  public Str itemAt(final long pos) {
    return Str.get(values[(int) pos]);
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

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof StrSeq ? Array.equals(values, ((StrSeq) obj).values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified items.
   * @param items items (will be invalidated by this call)
   * @return value
   */
  public static Value get(final TokenList items) {
    return items.isEmpty() ? Empty.VALUE : items.size() == 1 ? Str.get(items.get(0)) :
      new StrSeq(items.finish());
  }

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final byte[][] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Str.get(values[0]) : new StrSeq(values);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  static Value get(final int size, final Value... values) throws QueryException {
    final TokenList tmp = new TokenList(size);
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof StrSeq) {
        tmp.add(((StrSeq) value).values);
      } else {
        for(final Item item : value) tmp.add(item.string(null));
      }
    }
    return get(tmp);
  }
}
