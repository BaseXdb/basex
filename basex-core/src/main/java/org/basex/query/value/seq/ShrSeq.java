package org.basex.query.value.seq;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Sequence of items of type {@link Int xs:short}, containing at least two of them.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ShrSeq extends NativeSeq {
  /** Values. */
  private final short[] values;

  /**
   * Constructor.
   * @param values shorts
   */
  private ShrSeq(final short[] values) {
    super(values.length, AtomType.SHORT);
    this.values = values;
  }

  @Override
  public Int itemAt(final long pos) {
    return new Int(values[(int) pos], type);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final short[] tmp = new short[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public short[] toJava() {
    return values;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof ShrSeq ? Arrays.equals(values, ((ShrSeq) obj).values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final short[] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Int.get(values[0], AtomType.SHORT) :
      new ShrSeq(values);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  static Value get(final int size, final Value... values) throws QueryException {
    final ShortList tmp = new ShortList(size);
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof ShrSeq) {
        tmp.add(((ShrSeq) value).values);
      } else {
        for(final Item item : value) tmp.add((short) item.itr(null));
      }
    }
    return get(tmp.finish());
  }
}
