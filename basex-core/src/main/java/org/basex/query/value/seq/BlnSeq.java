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
 * Sequence of items of type {@link Bln xs:boolean}, containing at least two of them.
 *
 * @author BaseX Team 2005-22, BSD License
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
    super(values.length, AtomType.BOOLEAN);
    this.values = values;
  }

  @Override
  public Bln itemAt(final long pos) {
    return Bln.get(values[(int) pos]);
  }

  @Override
  public Value reverse(final QueryContext qc) {
    final int sz = (int) size;
    final boolean[] tmp = new boolean[sz];
    for(int i = 0; i < sz; i++) tmp[sz - i - 1] = values[i];
    return get(tmp);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    if(mode == Simplify.DISTINCT) {
      // replace with new sequence
      boolean f = false, t = false;
      for(final boolean b : values) {
        if(b) t = true;
        else f = true;
      }
      return cc.simplify(this, f && t ? new BlnSeq(new boolean[] { true, false }) : Bln.get(t));
    }
    return super.simplifyFor(mode, cc);
  }

  @Override
  public boolean[] toJava() {
    return values;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || (obj instanceof BlnSeq ? Arrays.equals(values, ((BlnSeq) obj).values) :
      super.equals(obj));
  }

  // STATIC METHODS ===============================================================================

  /**
   * Creates a sequence with the specified items.
   * @param values values
   * @return value
   */
  public static Value get(final boolean[] values) {
    final int vl = values.length;
    return vl == 0 ? Empty.VALUE : vl == 1 ? Bln.get(values[0]) : new BlnSeq(values);
  }

  /**
   * Creates a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @return value
   * @throws QueryException query exception
   */
  static Value get(final int size, final Value... values) throws QueryException {
    final BoolList tmp = new BoolList(size);
    for(final Value value : values) {
      // speed up construction, depending on input
      if(value instanceof BlnSeq) {
        tmp.add(((BlnSeq) value).values);
      } else {
        for(final Item item : value) tmp.add(item.bool(null));
      }
    }
    return get(tmp.finish());
  }
}
